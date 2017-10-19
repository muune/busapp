package teamdoppelganger.smarterbus.util.parser;

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

public class ParserJeju extends CommonParser {

    public final String mLineUrl = "http://bus.jeju.go.kr/internet_2012_kor/jsp/map/result/routeList.jsp";
    public final String mStopUrl = "http://bus.jeju.go.kr/internet_2012_kor/jsp/map/busRouteResultAction.jsp";

    public ParserJeju(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }


    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_JE_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_JE_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_JU._cityId);


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


        ArrayList<BusRouteItem> busRouteItem = new ArrayList<BusRouteItem>();

        String url = String.format("http://bus.jeju.go.kr/data/search/arriveScheduleAndTransitLineByStationId");
        String param = String.format("stationId=%s&type=2", sItem.apiId);
        String htmlResult = RequestCommonFuction.getSource(url, true, param, "utf-8");


        if (htmlResult != null) {

            JSONObject json;
            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("routeInfoList");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int remainMin = object.getInt("predictTravTm");
                    int remainStop = object.getInt("remainStation");
                    int busId = object.getInt("routeId");


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = String.valueOf(busId);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_JU._cityId);

                    if (remainMin > 0) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = remainStop;
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);
                        busRouteItem.add(routeItem);
                    } else if (remainMin == 0 && remainStop == 1) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = 0;
                        arriveItem.remainStop = remainStop;
                        arriveItem.state = Constants.STATE_NEAR;
                        routeItem.arriveInfo.add(arriveItem);
                        busRouteItem.add(routeItem);
                    }

                }


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(busRouteItem);

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
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {


        String url = String.format("http://bus.jeju.go.kr/data/search/getRealTimeBusPositionByLineId");
        String param = String.format("lineId=%s", sItem.tempId);
        String htmlResult = RequestCommonFuction.getSource(url, true, param, "utf-8");


        if (htmlResult != null) {

            JSONArray json;
            try {
                json = new JSONArray(htmlResult);

                for (int i = 0; i < json.length(); i++) {
                    JSONObject object = (JSONObject) json.get(i);

                    String stopApiId = object.getString("currStationId");
                    String platNo = object.getString("plateNo");


                    for (int k = 0; k < sItem.busStopItem.size(); k++) {

                        if (sItem.busStopItem.get(k).apiId.equals(stopApiId)) {


                            if (platNo.length() > 5) {
                                platNo = platNo.substring(0, 5) + " " + platNo.substring(5, platNo.length());
                            }


                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = platNo;

                            break;
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
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        super.getDepthAlarmList(sItem);

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = String.format("http://bus.jeju.go.kr/data/search/arriveScheduleAndTransitLineByStationId");
        String param = String.format("stationId=%s&type=2", sItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource(url, true, param, "utf-8");


        if (htmlResult != null) {

            JSONObject json;
            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("routeInfoList");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int remainMin = object.getInt("predictTravTm");
                    int remainStop = object.getInt("remainStation");
                    int busId = object.getInt("routeId");


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = String.valueOf(busId);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_JU._cityId);

                    if (remainMin > 0) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = remainStop;
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);

                        if (routeItem.busRouteApiId.equals(sItem.busRouteApiId)) {
                            arriveItems.add(arriveItem);
                        }


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

        super.getDepthRefreshList(sItem);


        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = String.format("http://bus.jeju.go.kr/data/search/arriveScheduleAndTransitLineByStationId");
        String param = String.format("stationId=%s&type=2", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource(url, true, param, "utf-8");


        if (htmlResult != null) {

            JSONObject json;
            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("routeInfoList");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int remainMin = object.getInt("predictTravTm");
                    int remainStop = object.getInt("remainStation");
                    int busId = object.getInt("routeId");


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = String.valueOf(busId);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JE_JU._cityId);

                    if (remainMin > 0) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = remainStop;
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);

                        if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeItem.busRouteApiId)) {
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }


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
