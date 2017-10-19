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
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserNaju extends CommonParser {


    public ParserNaju(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_NA_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_NA_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_NA_JU._cityId);


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

        String url = String.format("http://bis.naju.go.kr:8080/json/busLocationInfo?LINE_ID=%s", sItem.tempId);

        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8");

        if (htmlResult != null) {

            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("BUSLOCATION_LIST");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    int k = object.getInt("SEQ") - 1;
                    String carNum = object.getString("BUS_NO");
                    carNum = carNum.substring(0, 5) + " " + carNum.substring(5, carNum.length());
                    String stop_id = object.getString("BUSSTOP_ID");

                    sItem.busStopItem.get(k).isExist = true;
                    sItem.busStopItem.get(k).plainNum = carNum;

                }
            } catch (Exception e) {
            }
        }

        sItem.depthIndex = 0;


        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = "http://bis.naju.go.kr:8080/json/arriveAppInfo";
        String parseStr = RequestCommonFuction.getSource(url, true, "BUSSTOP_ID=" + sItem.apiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("BUSSTOP_LIST");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    BusRouteItem routeItem = new BusRouteItem();

                    String name = object.getString("LINE_NAME");
                    String where = object.getString("REMAIN_STOP");
                    String min = object.getString("REMAIN_MIN");

                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = object.getString("LINE_ID");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_NA_JU._cityId);


                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min);
                    arriveItem.remainStop = Integer.parseInt(where);
                    arriveItem.state = Constants.STATE_ING;
                    routeItem.arriveInfo.add(arriveItem);

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
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = "http://bis.naju.go.kr:8080/json/arriveAppInfo";
        String parseStr = RequestCommonFuction.getSource(url, true, "BUSSTOP_ID=" + sItem.busStopApiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("BUSSTOP_LIST");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    BusRouteItem routeItem = new BusRouteItem();

                    String name = object.getString("LINE_NAME");
                    String where = object.getString("REMAIN_STOP");
                    String min = object.getString("REMAIN_MIN");

                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = object.getString("LINE_ID");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_NA_JU._cityId);

                    if (sItem.busRouteApiId.equals(routeItem.busRouteApiId)) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.state = Constants.STATE_ING;
                        arriveItems.add(arriveItem);
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

        String url = "http://bis.naju.go.kr:8080/json/arriveAppInfo";
        String parseStr = RequestCommonFuction.getSource(url, true, "BUSSTOP_ID=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("BUSSTOP_LIST");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    BusRouteItem routeItem = new BusRouteItem();

                    String name = object.getString("LINE_NAME");
                    String where = object.getString("REMAIN_STOP");
                    String min = object.getString("REMAIN_MIN");

                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = object.getString("LINE_ID");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_NA_JU._cityId);

                    if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeItem.busRouteApiId)) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.state = Constants.STATE_ING;
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }

                }
            } catch (Exception e) {
            }
        }

        return sItem;

    }

}
