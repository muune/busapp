package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
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

public class ParserGwangju extends CommonParser {

    public ParserGwangju(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GWANG_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GWANG_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GWANG_JU._cityId);


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


        String url = String.format("http://bus.gjcity.net/busmap/busLocationListTemp2");
        String[] referer = {"Referer", "http://bus.gjcity.net/busmap/locationSearch"};
        String param = String.format("LINE_ID=%s", sItem.tempId);
        String htmlResult = RequestCommonFuction.getSource(url, true, param, "utf-8", referer);


        if (htmlResult != null) {

            JSONObject json;
            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);

                    String arsId = object.getString("ARS_ID");
                    String apiId = object.getString("BUSSTOP_ID");
                    String name = object.getString("BUSSTOP_NAME");
                    String carNum = object.getString("CARNO");

                    BusStopItem stopItem = new BusStopItem();
                    stopItem.arsId = arsId;
                    stopItem.apiId = apiId;
                    stopItem.name = name;
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GWANG_JU._cityId);


                    if (!carNum.equals("null")) {


                        for (int k = 0; k < sItem.busStopItem.size(); k++) {

                            if (sItem.busStopItem.get(k).arsId.equals(arsId)) {

                                stopItem.isExist = true;

                                if (carNum.length() > 5) {
                                    carNum = carNum.substring(0, 5) + " " + carNum.substring(5, carNum.length());
                                    stopItem.plainNum = carNum;
                                }


                                sItem.busStopItem.get(k).isExist = true;
                                sItem.busStopItem.get(k).plainNum = carNum;

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
        ArrayList<BusRouteItem> busRouteItem = new ArrayList<BusRouteItem>();

        String[] referer = {"Referer", "http://bus.gjcity.net/busmap/stationSearch"};
        String url = String.format("http://bus.gjcity.net/busmap/lineStationArriveInfoListTemp2?BUSSTOP_ID=%s", sItem.apiId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8", referer);


        if (htmlResult != null) {

            JSONObject json;
            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int remainMin = object.getInt("REMAIN_MIN");
                    int remainStop = object.getInt("REMAIN_STOP");
                    int busId = object.getInt("LINE_ID");


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = String.valueOf(busId);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GWANG_JU._cityId);

                    if (remainMin > 0) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = remainStop;
                        arriveItem.state = Constants.STATE_ING;
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


        String[] referer = {"Referer", "http://bus.gjcity.net/busmap/stationSearch"};
        String url = String.format("http://bus.gjcity.net/busmap/lineStationArriveInfoListTemp2?BUSSTOP_ID=%s", sItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8", referer);


        if (htmlResult != null) {

            JSONObject json;

            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int remainMin = object.getInt("REMAIN_MIN");
                    int remainStop = object.getInt("REMAIN_STOP");
                    int busId = object.getInt("LINE_ID");


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = String.valueOf(busId);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GWANG_JU._cityId);

                    if (remainMin > 0) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = remainStop;
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);


                        if (routeItem.busRouteApiId.equals(sItem.busRouteApiId)) {
                            arriveItems.add(arriveItem);
                        }
                        //busRouteItem.add(routeItem);
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


        String[] referer = {"Referer", "http://bus.gjcity.net/busmap/stationSearch"};
        String url = String.format("http://bus.gjcity.net/busmap/lineStationArriveInfoListTemp2?BUSSTOP_ID=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8", referer);


        if (htmlResult != null) {
            JSONObject json;
            try {
                json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("list");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = (JSONObject) jsonArray.get(i);
                    int remainMin = object.getInt("REMAIN_MIN");
                    int remainStop = object.getInt("REMAIN_STOP");
                    int busId = object.getInt("LINE_ID");


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteApiId = String.valueOf(busId);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GWANG_JU._cityId);

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
