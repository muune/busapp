package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

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

public class ParserHawsun extends CommonParser {

    public ParserHawsun(SQLiteDatabase db) {
        super(db);

    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_HWASUN._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);

        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_HWASUN._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_HWASUN._cityId);


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


        String param = String.format("LINE_ID=%s", sItem.tempId);
        String parseStr = RequestCommonFuction
                .getSource(
                        "http://m.bis.hwasun.go.kr/mobile/lineStationList",
                        true, param, "utf-8");


        if (parseStr != null) {
            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    String stopApiId = object.getString("BUSSTOP_ID");
                    boolean isBus = object.getString("BUS_ID") == null ? false : true;


                    if (isBus) {

                        String plainNum = object.getString("BUS_NO");

                        for (int k = 0; k < sItem.busStopItem.size(); k++) {
                            if (sItem.busStopItem.get(k).apiId.equals(stopApiId)) {
                                if (plainNum.trim().length() > 5) {
                                    plainNum = plainNum.substring(0, 5) + " " + plainNum.trim().substring(5, plainNum.length());
                                    sItem.busStopItem.get(k).plainNum = plainNum;
                                    sItem.busStopItem.get(k).isExist = true;
                                }


                                break;
                            }

                        }


                    }

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
        String param = String.format("BUSSTOP_ID=%s", sItem.apiId);
        String parseStr = RequestCommonFuction
                .getSource(
                        "http://m.bis.hwasun.go.kr/mobile/busArriveInfoList",
                        true, param, "utf-8");


        if (parseStr != null) {

            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    String busApiId = object.getString("LINE_ID");

                    int remainStop = 0;
                    int remainMin = 0;


                    try {
                        remainMin = Integer.parseInt(object.getString("REMAIN_MIN"));
                        remainStop = Integer.parseInt(object.getString("REMAIN_STOP"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = busApiId;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_HWASUN._cityId);
                    routeItem.busStopApiId = sItem.apiId;

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMin;
                    arriveItem.remainStop = remainStop;
                    arriveItem.state = Constants.STATE_ING;

                    routeItem.arriveInfo.add(arriveItem);

                    localBusRouteItems.add(routeItem);

                }


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;

    }

    @Override
    public DepthRouteItem getDepthLineList(DepthRouteItem sItem) {

        super.getDepthLineList(sItem);


        if (sItem.busRouteItem.size() > 0) {

        }


        sItem.depthIndex = 0;

        return sItem;

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


        String param = String.format("BUSSTOP_ID=%s", sItem.busStopApiId);
        String parseStr = RequestCommonFuction
                .getSource(
                        "http://m.bis.hwasun.go.kr/mobile/busArriveInfoList",
                        true, param, "utf-8");


        if (parseStr != null) {

            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    String busApiId = object.getString("LINE_ID");

                    int remainStop = 0;
                    int remainMin = 0;


                    try {
                        remainMin = Integer.parseInt(object.getString("REMAIN_MIN"));
                        remainStop = Integer.parseInt(object.getString("REMAIN_STOP"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMin;
                    arriveItem.remainStop = remainStop;
                    arriveItem.state = Constants.STATE_ING;


                    //결과값이 하나만 존재한다.
                    if (busApiId.equals(sItem.busRouteApiId)) {
                        arriveItems.add(arriveItem);
                        break;
                    }

                }


            } catch (JSONException e) {
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


        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);


        String param = String.format("BUSSTOP_ID=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction
                .getSource(
                        "http://m.bis.hwasun.go.kr/mobile/busArriveInfoList",
                        true, param, "utf-8");


        if (parseStr != null) {

            try {
                JSONArray jsonArray = new JSONObject(parseStr).getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    String busApiId = object.getString("LINE_ID");

                    int remainStop = 0;
                    int remainMin = 0;


                    try {
                        remainMin = Integer.parseInt(object.getString("REMAIN_MIN"));
                        remainStop = Integer.parseInt(object.getString("REMAIN_STOP"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMin;
                    arriveItem.remainStop = remainStop;
                    arriveItem.state = Constants.STATE_ING;


                    //결과값이 하나만 존재한다.
                    if (busApiId.equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId)) {
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        break;
                    }

                }


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


        return sItem;


    }


}
