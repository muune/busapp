package teamdoppelganger.smarterbus.util.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;

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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ParserGumi extends CommonParser {

    public ParserGumi(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GU_MI._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GU_MI._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.tempId2 = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GU_MI._cityId);


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

        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();
        String htmlResult = RequestCommonFuction.getSource("http://bis.gumi.go.kr/map/RoutePosition.do", true, "route_id=" + sItem.tempId, "utf-8");

        htmlResult = "{busPosition:" + htmlResult + "}";


        if (htmlResult != null) {

            JSONObject json;

            try {

                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("busPosition");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int k = Integer.parseInt(object.getString("WAYPOINT_ORD"));
                    if (k > 0)
                        k--;
                    sItem.busStopItem.get(k).isExist = true;
                    String plainNum = object.getString("BUS_NO");

                    if (plainNum.length() > 5) {
                        plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                    }

                    sItem.busStopItem.get(k).plainNum = plainNum;

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

        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String tmpId;
        if (Integer.parseInt(sItem.arsId) > 10000) {
            tmpId = String.valueOf(Integer.parseInt(sItem.arsId) - 10000);
        } else {
            tmpId = sItem.arsId;
        }

        String parseStr = RequestCommonFuction.getSource("http://bis.gumi.go.kr/moMap/mAjaxBusStopResult.do", true, "station_id=" + tmpId, "utf-8");


        if (parseStr != null) {
            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("div[class=stops_list01]").select("ul").select("li");

                for (int i = 0; i < seqElements.size(); i++) {

                    String apiId = seqElements.get(i).select("div[class=con_view01]").attr("id").split("_")[0];
                    String busNum = seqElements.get(i).select("div[class=con_view01]").select("p").select("strong").text();
                    String subName = seqElements.get(i).select("div[class=con_view01]").select("p").select("span").text().replaceAll("\\p{Z}", "").replaceAll("\\(", "").replaceAll("\\）", "");
                    String whenInfo = seqElements.get(i).select("div[class=con_view02]").select("p").select("span").text();

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteSubName = subName;
                    routeItem.busRouteApiId = apiId;

                    if (!whenInfo.contains("도착정보가")) {

                        String where = seqElements.get(i).select("div[class=con_view02]").select("p").select("strong[class=cl_blue").text();
                        String when = seqElements.get(i).select("div[class=con_view03]").select("p").select("strong[class=cl_blue").text();
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.remainMin = Integer.parseInt(when);
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);

                    }

                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GU_MI._cityId);
                    localBusRouteItem.add(routeItem);

                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItem);


        return depthRouteItem;
    }

    @Override
    public DepthRouteItem getAlarmList(String... idStr) {
        return null;
    }

    @Override
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        return null;
    }

    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String tmpId;
        if (Integer.parseInt(sItem.busStopArsId) > 10000) {
            tmpId = String.valueOf(Integer.parseInt(sItem.busStopArsId) - 10000);
        } else {
            tmpId = sItem.busStopArsId;
        }


        String parseStr = RequestCommonFuction.getSource("http://bis.gumi.go.kr/moMap/mAjaxBusStopResult.do", true, "station_id=" + tmpId, "utf-8");


        if (parseStr != null) {
            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("div[class=stops_list01]").select("ul").select("li");

                for (int i = 0; i < seqElements.size(); i++) {

                    String apiId = seqElements.get(i).select("div[class=con_view01]").attr("id").split("_")[0];
                    String busNum = seqElements.get(i).select("div[class=con_view01]").select("p").select("strong").text();
                    String subName = seqElements.get(i).select("div[class=con_view01]").select("p").select("span").text().replaceAll("\\p{Z}", "").replaceAll("\\(", "").replaceAll("\\）", "");
                    String whenInfo = seqElements.get(i).select("div[class=con_view02]").select("p").select("span").text();

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteSubName = subName;
                    routeItem.busRouteApiId = apiId;

                    if (!whenInfo.contains("도착정보가")) {

                        if (sItem.busRouteName.equals(busNum) && sItem.busRouteApiId.equals(apiId)) {
                            String where = seqElements.get(i).select("div[class=con_view02]").select("p").select("strong[class=cl_blue").text();
                            String when = seqElements.get(i).select("div[class=con_view03]").select("p").select("strong[class=cl_blue").text();
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainStop = Integer.parseInt(where);
                            arriveItem.remainMin = Integer.parseInt(when);
                            arriveItem.state = Constants.STATE_ING;
                            routeItem.arriveInfo.add(arriveItem);
                            arriveItems.add(arriveItem);
                        }

                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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


        String tmpId;
        if (Integer.parseInt(favoriteAndHistoryItem.busRouteItem.busStopArsId) > 10000) {
            tmpId = String.valueOf(Integer.parseInt(favoriteAndHistoryItem.busRouteItem.busStopArsId) - 10000);
        } else {
            tmpId = favoriteAndHistoryItem.busRouteItem.busStopArsId;
        }


        String parseStr = RequestCommonFuction.getSource("http://bis.gumi.go.kr/moMap/mAjaxBusStopResult.do", true, "station_id=" + tmpId, "utf-8");


        if (parseStr != null) {
            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("div[class=stops_list01]").select("ul").select("li");

                for (int i = 0; i < seqElements.size(); i++) {

                    String apiId = seqElements.get(i).select("div[class=con_view01]").attr("id").split("_")[0];
                    String busNum = seqElements.get(i).select("div[class=con_view01]").select("p").select("strong").text();
                    String subName = seqElements.get(i).select("div[class=con_view01]").select("p").select("span").text().replaceAll("\\p{Z}", "").replaceAll("\\(", "").replaceAll("\\）", "");
                    String whenInfo = seqElements.get(i).select("div[class=con_view02]").select("p").select("span").text();

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteSubName = subName;
                    routeItem.busRouteApiId = apiId;

                    if (!whenInfo.contains("도착정보가")) {

                        if (favoriteAndHistoryItem.busRouteItem.busRouteName.equals(busNum) && apiId.equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId)) {
                            String where = seqElements.get(i).select("div[class=con_view02]").select("p").select("strong[class=cl_blue").text();
                            String when = seqElements.get(i).select("div[class=con_view03]").select("p").select("strong[class=cl_blue").text();
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainStop = Integer.parseInt(where);
                            arriveItem.remainMin = Integer.parseInt(when);
                            arriveItem.state = Constants.STATE_ING;
                            routeItem.arriveInfo.add(arriveItem);
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return sItem;

    }

}




