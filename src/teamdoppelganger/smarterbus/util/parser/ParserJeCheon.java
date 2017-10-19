package teamdoppelganger.smarterbus.util.parser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthAlarmItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserJeCheon extends CommonParser {

    public ParserJeCheon(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_JE_CHEON._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_JE_CHEON._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_CHEON._cityId);


                    if (i == turnStopIdx) {
                        stopItem.isTurn = true;
                    } else {
                        stopItem.isTurn = false;
                    }

                    localBusStopItems.add(stopItem);

                }
                stopCursor.close();
            }

        }
        cursor.close();

        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(localBusStopItems);
        depthStopItem.tempId = rItem.busRouteApiId;
        depthStopItem.depthIndex = 1;

        return depthStopItem;
    }

    @Override
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {

        super.getDepthStopList(sItem);

        String url = String.format("http://its.okjc.net/m01/AjaxRealRoutePosition.json?route_id=%s", sItem.tempId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");

        if (parseStr != null) {
            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("position");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int k = object.getInt("LATEST_STOP_ORD");
                    String plainNum = object.getString("PLATE_NO");
                    plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                    sItem.busStopItem.get(k - 1).plainNum = plainNum;
                    sItem.busStopItem.get(k - 1).isExist = true;

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        sItem.depthIndex = 0;

        return sItem;

    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = String.format("http://its.okjc.net/AjaxArriveTime?stop_id=%s", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");


        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("div[class=route_list]");

            for (int j = 0; j < ddTag.size(); j++) {

                String busRouteId = ddTag.get(j).attr("id").split("__")[0];
                String busName = ddTag.get(j).attr("id").split("__")[1];
                String strArriveInfo = ddTag.get(j).select("div[class=route_info02]").text();
                String where = "";
                String min = "";

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteApiId = busRouteId;
                routeItem.busRouteName = busName;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_CHEON._cityId);
                routeItem.busStopArsId = sItem.arsId;
                routeItem.busStopName = sItem.name;


                if (strArriveInfo.contains("도착예정")) {

                } else {

                    String strArriveInfo1 = ddTag.get(j).select("div[class=route_info03]").text();

                    if (strArriveInfo1.contains("잠시 후")) {

                        min = "0";
                        where = "0";
                        ArriveItem arrive = new ArriveItem();
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_NEAR;
                        routeItem.arriveInfo.add(arrive);

                    } else {

                        min = strArriveInfo1.split(" 분")[0];
                        where = strArriveInfo.split(" 정거장")[0];
                        ArriveItem arrive = new ArriveItem();
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arrive);

                    }

                }

                localBusRouteItems.add(routeItem);

            }
        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;

    }

    @Override
    public DepthRouteItem getAlarmList(String... idStr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthRouteItem getLineDetailList(
            BusRouteItem busRouteItems) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(
            BusStopItem busStopItems) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        String url = String.format("http://its.okjc.net/AjaxArriveTime?stop_id=%s", sItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");

        String fBusApiId = sItem.busRouteApiId;

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("div[class=route_list]");

            for (int j = 0; j < ddTag.size(); j++) {

                String busRouteId = ddTag.get(j).attr("id").split("__")[0];
                String busName = ddTag.get(j).attr("id").split("__")[1];
                String strArriveInfo = ddTag.get(j).select("div[class=route_info02]").text();
                String where = "";
                String min = "";


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteApiId = busRouteId;
                routeItem.busRouteName = busName;


                if (strArriveInfo.contains("도착예정")) {
                } else {

                    String strArriveInfo1 = ddTag.get(j).select("div[class=route_info03]").text();

                    if (strArriveInfo1.contains("잠시 후")) {
                        if (fBusApiId.equals(busRouteId)) {
                            min = "1";
                            where = "1";
                            ArriveItem arrive = new ArriveItem();
                            arrive.remainMin = Integer.parseInt(min);
                            arrive.remainStop = Integer.parseInt(where);
                            arrive.state = Constants.STATE_NEAR;
                            routeItem.arriveInfo.add(arrive);
                            arriveItems.add(arrive);
                        }

                    } else {
                        if (fBusApiId.equals(busRouteId)) {
                            min = strArriveInfo1.split(" 분")[0];
                            where = strArriveInfo.split(" 정거장")[0];
                            ArriveItem arrive = new ArriveItem();
                            arrive.remainMin = Integer.parseInt(min);
                            arrive.remainStop = Integer.parseInt(where);
                            arrive.state = Constants.STATE_ING;
                            routeItem.arriveInfo.add(arrive);
                            arriveItems.add(arrive);
                        }
                    }
                }
            }
        }

        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;


    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = String.format("http://its.okjc.net/AjaxArriveTime?stop_id=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");

        String fBusApiId = favoriteAndHistoryItem.busRouteItem.busRouteApiId;

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("div[class=route_list]");

            for (int j = 0; j < ddTag.size(); j++) {

                String busRouteId = ddTag.get(j).attr("id").split("__")[0];
                String busName = ddTag.get(j).attr("id").split("__")[1];
                String strArriveInfo = ddTag.get(j).select("div[class=route_info02]").text();
                String where = "";
                String min = "";

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteApiId = busRouteId;
                routeItem.busRouteName = busName;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_CHEON._cityId);


                if (strArriveInfo.contains("도착예정")) {
				
                } else {


                    String strArriveInfo1 = ddTag.get(j).select("div[class=route_info03]").text();

                    if (strArriveInfo1.contains("잠시 후")) {
                        if (fBusApiId.equals(busRouteId)) {
                            min = "0";
                            where = "0";
                            ArriveItem arrive = new ArriveItem();
                            arrive.remainMin = Integer.parseInt(min);
                            arrive.remainStop = Integer.parseInt(where);
                            arrive.state = Constants.STATE_NEAR;
                            routeItem.arriveInfo.add(arrive);
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arrive);
                        }

                    } else {
                        if (fBusApiId.equals(busRouteId)) {
                            min = strArriveInfo1.split(" 분")[0];
                            where = strArriveInfo.split(" 정거장")[0];
                            ArriveItem arrive = new ArriveItem();
                            arrive.remainMin = Integer.parseInt(min);
                            arrive.remainStop = Integer.parseInt(where);
                            arrive.state = Constants.STATE_ING;
                            routeItem.arriveInfo.add(arrive);
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arrive);
                        }
                    }
                }
            }
        }

        return sItem;

    }

}
