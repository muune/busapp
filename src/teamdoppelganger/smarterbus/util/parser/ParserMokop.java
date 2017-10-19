package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthAlarmItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserMokop extends CommonParser {


    public ParserMokop(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_MOK_PO._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_MOK_PO._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_MOK_PO._cityId);


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
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = "http://bis.mokpo.go.kr/mp/bis/searchBusStopRoute.do";
        String parseStr = RequestCommonFuction.getSource(url, true, "busStopId=" + sItem.apiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("busStopRouteList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    BusRouteItem routeItem = new BusRouteItem();

                    String name = object.getString("route_name");
                    String where = object.getString("rstop");
                    String min = object.getString("provide_type");

                    if (name.contains("번")) {
                        name = name.split("번")[0];
                    }

                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = object.getString("route_id");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_MOK_PO._cityId);


                    if (min.contains("약")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min.split("약")[1].split("분후")[0]);
                        arriveItem.remainStop = Integer.parseInt(where.split("구간전")[0]);
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);
                    }


                    localBusRouteItems.add(routeItem);


                }
            } catch (Exception e) {
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
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {

        super.getDepthStopList(sItem);

        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();

        String htmlResult = RequestCommonFuction.getSource("http://bis.mokpo.go.kr/mp/bis/searchBusRealLocationDetail.do", true, "busRouteId=" + sItem.tempId, "utf-8");


        if (htmlResult != null) {

            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("busRealLocList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    String carNum = object.getString("plate_no");
                    carNum = carNum.substring(0, 5) + " " + carNum.substring(5, carNum.length());
                    String stop_id = object.getString("stop_id");

                    for (int k = 0; k < sItem.busStopItem.size(); k++) {

                        if (sItem.busStopItem.get(k).apiId.equals(stop_id)) {
                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = carNum;
                            break;
                        }

                    }
                }
            } catch (Exception e) {
            }
        }


        sItem.depthIndex = 0;


        return sItem;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = "http://bis.mokpo.go.kr/mp/bis/searchBusStopRoute.do";
        String parseStr = RequestCommonFuction.getSource(url, true, "busStopId=" + sItem.busStopApiId, "utf-8");


        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("busStopRouteList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    BusRouteItem routeItem = new BusRouteItem();

                    String name = object.getString("route_name");
                    String where = object.getString("rstop");
                    String min = object.getString("provide_type");

                    if (name.contains("번")) {
                        name = name.split("번")[0];
                    }

                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = object.getString("route_id");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_MOK_PO._cityId);


                    if (min.contains("약")) {
                        if (sItem.busRouteName.equals(name.split(" ")[0])) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = Integer.parseInt(min.split("약")[1].split("분후")[0]);
                            arriveItem.remainStop = Integer.parseInt(where.split("구간전")[0]);
                            arriveItem.state = Constants.STATE_ING;
                            arriveItems.add(arriveItem);
                        }
                    }
                }
            } catch (Exception e) {
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

        String url = "http://bis.mokpo.go.kr/mp/bis/searchBusStopRoute.do";
        String parseStr = RequestCommonFuction.getSource(url, true, "busStopId=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("busStopRouteList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    BusRouteItem routeItem = new BusRouteItem();

                    String name = object.getString("route_name");
                    String where = object.getString("rstop");
                    String min = object.getString("provide_type");

                    if (name.contains("번")) {
                        name = name.split("번")[0];
                    }

                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = object.getString("route_id");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_MOK_PO._cityId);


                    if (min.contains("약")) {
                        if (favoriteAndHistoryItem.busRouteItem.busRouteName.equals(name.split(" ")[0])) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = Integer.parseInt(min.split("약")[1].split("분후")[0]);
                            arriveItem.remainStop = Integer.parseInt(where.split("구간전")[0]);
                            arriveItem.state = Constants.STATE_ING;
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }


        return sItem;
    }

}
