package teamdoppelganger.smarterbus.util.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import com.smart.lib.CommonConstants;

public class ParserChunggu extends CommonParser {

    public ParserChunggu(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_CHEONG_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_CHEONG_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONG_JU._cityId);


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

        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();
        LinkedHashMap<String, BusStopItem> mTempId = new LinkedHashMap<String, BusStopItem>();


        String htmlResult = RequestCommonFuction.getSource("http://m.dcbis.go.kr/rest/getRunBusDetail.json", false, "routeid=" + sItem.tempId, "utf-8");


        if (htmlResult != null) {
            try {
                JSONArray jsonArray = new JSONObject(htmlResult).getJSONArray("model");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);


                    String stopId = obj.getString("sid");
                    String plainNum = obj.getString("tagType");

                    if (plainNum.trim().length() > 0) {
                        for (int k = 0; k < sItem.busStopItem.size(); k++) {

                            if (stopId.equals(sItem.busStopItem.get(k).apiId)) {
                                if (plainNum.length() > 5) {
                                    plainNum = plainNum.substring(0, 3) + " " + plainNum.substring(3, plainNum.length());
                                    sItem.busStopItem.get(k).plainNum = plainNum;
                                    sItem.busStopItem.get(k).isExist = true;
                                    sItem.busStopItem.get(k).localInfoId = String.valueOf(CommonConstants.CITY_CHEONG_JU._cityId);
                                }
                            }

                        }
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        sItem.depthIndex = 0;
        return sItem;


    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {
        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        String parseStr = RequestCommonFuction.getSource("http://m.dcbis.go.kr/rest/getRouteRunBusDetail.json", false, "sid=" + sItem.apiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("model");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = obj.getString("BUSROUTENO");
                    routeItem.busRouteApiId = obj.getString("BUSROUTEID");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONG_JU._cityId);

                    try {
                        String min1 = obj.getString("PREDICTTRAVELTIMESEC");
                        String min2 = obj.getString("PREDICTTRAVELTIME");

                        int min = Integer.parseInt(min1);


                        if (min2.contains("진입중")) {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = 0;
                            arriveItem.remainSecond = 0;
                            arriveItem.state = Constants.STATE_NEAR;

                            routeItem.arriveInfo.add(arriveItem);

                            localBusRouteItems.add(routeItem);

                        } else {

                            if (min != 0) {

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = min / 60;
                                if (min % 60 != 0) {
                                    arriveItem.remainSecond = min % 60;
                                }
                                arriveItem.state = Constants.STATE_ING;

                                routeItem.arriveInfo.add(arriveItem);

                                localBusRouteItems.add(routeItem);
                            }
                        }


                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;


    }

    @Override
    public DepthRouteItem getAlarmList(String... idStr) {
        return null;
    }

    @Override
    public DepthRouteItem getLineDetailList(
            BusRouteItem busRouteItems) {
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(
            BusStopItem busStopItems) {
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        String parseStr = RequestCommonFuction.getSource("http://m.dcbis.go.kr/rest/getRouteRunBusDetail.json", false, "sid=" + sItem.busStopApiId, "utf-8");

        if (parseStr != null) {

            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("model");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = obj.getString("BUSROUTENO");
                    routeItem.busRouteApiId = obj.getString("BUSROUTEID");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONG_JU._cityId);

                    try {
                        String min1 = obj.getString("PREDICTTRAVELTIMESEC");
                        String min2 = obj.getString("PREDICTTRAVELTIME");

                        int min = Integer.parseInt(min1);

                        if (min2.contains("진입중")) {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = 1;
                            arriveItem.remainSecond = 0;
                            arriveItem.state = Constants.STATE_NEAR;


                            if (sItem.busRouteApiId.equals(routeItem.busRouteApiId)) {

                                arriveItems.add(arriveItem);

                            }

                        } else {

                            if (min != 0) {

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = min / 60;
                                if (min % 60 != 0) {
                                    arriveItem.remainSecond = min % 60;
                                }
                                arriveItem.state = Constants.STATE_ING;

                                if (sItem.busRouteApiId.equals(routeItem.busRouteApiId)) {

                                    arriveItems.add(arriveItem);
                                }


                            }
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                }
            } catch (JSONException e) {
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


        String parseStr = RequestCommonFuction.getSource("http://m.dcbis.go.kr/rest/getRouteRunBusDetail.json", false, "sid=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "utf-8");


        if (parseStr != null) {

            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("model");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.get(i);


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = obj.getString("BUSROUTENO");
                    routeItem.busRouteApiId = obj.getString("BUSROUTEID");
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONG_JU._cityId);

                    try {
                        String min1 = obj.getString("PREDICTTRAVELTIMESEC");
                        String min2 = obj.getString("PREDICTTRAVELTIME");

                        int min = Integer.parseInt(min1);

                        if (min2.contains("진입중")) {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = 0;
                            arriveItem.remainSecond = 0;
                            arriveItem.state = Constants.STATE_NEAR;


                            if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeItem.busRouteApiId)) {

                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                            }

                        } else {

                            if (min != 0) {

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = min / 60;
                                if (min % 60 != 0) {
                                    arriveItem.remainSecond = min % 60;
                                }
                                arriveItem.state = Constants.STATE_ING;

                                if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeItem.busRouteApiId)) {

                                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                                }


                            }
                        }

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        return sItem;
    }


}
