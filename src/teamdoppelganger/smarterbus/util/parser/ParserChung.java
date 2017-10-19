package teamdoppelganger.smarterbus.util.parser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.smart.lib.CommonConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;

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

public class ParserChung extends CommonParser {

    public ParserChung(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_CHUNG_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);

        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_CHUNG_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);


                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_CHUNG_JU._cityId);


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


        String htmlParam = String.format("route_id=%s", sItem.tempId);
        String htmlResult = RequestCommonFuction.getSource("http://its.chungju.go.kr/mbis/ajax/RoutePosition.do", true, htmlParam, "utf-8");


        if (htmlResult != null) {
            try {


                JSONArray jsonArray = new JSONArray(htmlResult);

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject obj = (JSONObject) jsonArray.get(i);

                    String plainNum = obj.getString("PLATE_NO");

                    int lastStopOrd = obj.getInt("LATEST_STOP_ORD");

                    if (plainNum.length() > 5) {
                        plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                        sItem.busStopItem.get(lastStopOrd - 1).plainNum = plainNum;
                        sItem.busStopItem.get(lastStopOrd - 1).isExist = true;
                        sItem.busStopItem.get(lastStopOrd - 1).localInfoId = String.valueOf(CommonConstants.CITY_CHUNG_JU._cityId);
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


        String parseParam = String.format("stop_id=%s", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource("http://its.chungju.go.kr/mbis/mAjaxBusStopResult.do", true, parseParam, "utf-8");

        if (parseStr != null) {

            try {

                Document doc = Jsoup.parse(parseStr);
                org.jsoup.select.Elements elements = doc.select("ul[class=ul_list] li");

                for (int i = 0; i < elements.size(); i++) {
                    String id = elements.get(i).select("div[class=con_view01]").attr("id");
                    String busApiId = null;

                    if (id.contains("_")) {
                        busApiId = id.split("_")[0];

                    }

                    int remainMin = -1;
                    int remainStop = -1;

                    ArriveItem arriveItem = new ArriveItem();

                    if (elements.get(i).select("div[class=con_view02]").text().contains("운행 중인")) {
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                    } else if (elements.get(i).select("div[class=con_view02]").text().contains("잠시")) {
                        arriveItem.state = Constants.STATE_NEAR;

                    } else {
                        remainStop = Integer.parseInt(elements.get(i).select("div[class=con_view02]").select("strong").text());
                        arriveItem.state = Constants.STATE_ING;
                    }


                    if (elements.get(i).select("div[class=con_view03]").text().contains("-") ||
                            elements.get(i).select("div[class=con_view03]").text().contains("잠시")) {

                    } else {
                        try {
                            remainMin = Integer.parseInt(elements.get(i).select("div[class=con_view03]").select("strong").text());
                        } catch (Exception e) {

                        }
                    }

                    if (remainMin != -1) {
                        arriveItem.remainMin = remainMin;
                    }

                    if (remainStop != -1) {
                        arriveItem.remainStop = remainStop;
                    }


                    if (busApiId != null) {
                        BusRouteItem routeItem = new BusRouteItem();
                        routeItem.busRouteApiId = busApiId;
                        routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHUNG_JU._cityId);
                        routeItem.arriveInfo.add(arriveItem);

                        localBusRouteItems.add(routeItem);
                    }


                }


            } catch (Exception e) {
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


        String parseParam = String.format("stop_id=%s", sItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource("http://its.chungju.go.kr/mbis/mAjaxBusStopResult.do", true, parseParam, "utf-8");

        if (parseStr != null) {

            try {

                Document doc = Jsoup.parse(parseStr);
                org.jsoup.select.Elements elements = doc.select("ul[class=ul_list] li");

                for (int i = 0; i < elements.size(); i++) {
                    String id = elements.get(i).select("div[class=con_view01]").attr("id");
                    String busApiId = null;

                    if (id.contains("_")) {
                        busApiId = id.split("_")[0];
                    }

                    int remainMin = -1;
                    int remainStop = -1;

                    ArriveItem arriveItem = new ArriveItem();

                    if (elements.get(i).select("div[class=con_view02]").text().contains("운행 중인")) {
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                    } else if (elements.get(i).select("div[class=con_view02]").text().contains("잠시")) {
                        arriveItem.state = Constants.STATE_NEAR;

                    } else {
                        remainStop = Integer.parseInt(elements.get(i).select("div[class=con_view02]").select("strong").text());
                        arriveItem.state = Constants.STATE_ING;
                    }


                    if (elements.get(i).select("div[class=con_view03]").text().contains("-") ||
                            elements.get(i).select("div[class=con_view03]").text().contains("잠시")) {

                    } else {
                        try {
                            remainMin = Integer.parseInt(elements.get(i).select("div[class=con_view03]").select("strong").text());
                        } catch (Exception e) {

                        }
                    }

                    if (remainMin != -1) {
                        arriveItem.remainMin = remainMin;
                    }

                    if (remainStop != -1) {
                        arriveItem.remainStop = remainStop;
                    }


                    if (busApiId != null) {
                        if (sItem.busRouteApiId.equals(busApiId)) {

                            arriveItems.add(arriveItem);
                            break;
                        }
                    }

                }

            } catch (Exception e) {
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

        String parseParam = String.format("stop_id=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource("http://its.chungju.go.kr/mbis/mAjaxBusStopResult.do", true, parseParam, "utf-8");


        if (parseStr != null) {

            try {

                Document doc = Jsoup.parse(parseStr);
                org.jsoup.select.Elements elements = doc.select("ul[class=ul_list] li");

                for (int i = 0; i < elements.size(); i++) {
                    String id = elements.get(i).select("div[class=con_view01]").attr("id");
                    String busApiId = null;

                    if (id.contains("_")) {
                        busApiId = id.split("_")[0];

                    }

                    int remainMin = -1;
                    int remainStop = -1;

                    ArriveItem arriveItem = new ArriveItem();

                    if (elements.get(i).select("div[class=con_view02]").text().contains("운행 중인")) {
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                    } else if (elements.get(i).select("div[class=con_view02]").text().contains("잠시")) {
                        arriveItem.state = Constants.STATE_NEAR;

                    } else {
                        remainStop = Integer.parseInt(elements.get(i).select("div[class=con_view02]").select("strong").text());
                        arriveItem.state = Constants.STATE_ING;
                    }


                    if (elements.get(i).select("div[class=con_view03]").text().contains("-") ||
                            elements.get(i).select("div[class=con_view03]").text().contains("잠시")) {

                    } else {
                        try {
                            remainMin = Integer.parseInt(elements.get(i).select("div[class=con_view03]").select("strong").text());
                        } catch (Exception e) {

                        }
                    }

                    if (remainMin != -1) {
                        arriveItem.remainMin = remainMin;
                    }

                    if (remainStop != -1) {
                        arriveItem.remainStop = remainStop;
                    }


                    if (busApiId != null) {

                        if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(busApiId)) {


                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            break;
                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return sItem;
    }


}
