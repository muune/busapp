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
import android.text.NoCopySpan.Concrete;

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

public class ParserWonju extends CommonParser {

    public ParserWonju(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }


    //도착 예정 정보를 제공하지 않는다.
    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_WON_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_WON_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_WON_JU._cityId);


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


    //빠를데 있고 느릴데 있음
    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();
        String selectBusStopQry = String.format("SELECT %s FROM %s where %s='%s'", CommonConstants.BUS_STOP_RELATED_ROUTES, CommonConstants.CITY_WON_JU._engName + "_stop",
                CommonConstants.BUS_STOP_API_ID, sItem.apiId);


        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_WON_JU._engName + "_route",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusRouteItem routeItem = new BusRouteItem();

                    routeItem.busRouteApiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    routeItem.busRouteApiId2 = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                    routeItem.busType = stopCursor.getInt(stopCursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                    routeItem.busStopApiId = sItem.apiId;
                    routeItem.busStopName = sItem.name;

                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_WON_JU._cityId);
                    localBusRouteItems.add(routeItem);

                }
                stopCursor.close();
            }

        }
        cursor.close();


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);
        depthRouteItem.depthIndex = 1;


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

        return null;
    }


    @Override
    public DepthRouteItem getDepthLineList(DepthRouteItem sItem) {

        super.getDepthLineList(sItem);


        String sId = null;

        for (int i = 0; i < sItem.busRouteItem.size(); i++) {

            if (sItem.busRouteItem.get(i).busStopApiId != null) {
                sId = sItem.busRouteItem.get(i).busStopApiId;
                break;
            }
        }


        String param = String.format("station_id=%s", sId);
        String htmlResult = RequestCommonFuction.getSource("http://its.wonju.go.kr/map/AjaxRouteListByStop.do", false, param, "utf-8");


        if (htmlResult != null) {


            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("div[class=nw_table1 ]").select("tr");

            for (int i = 0; i < ddTag.size(); i++) {
                String ids = ddTag.get(i).attr("id");

                String id1 = ids.split("_")[0];
                String id2 = ids.split("_")[1];

                int remainStop = 0;
                int remainMin = 0;


                if (ddTag.get(i).select("td").size() > 1) {

                    if (ddTag.get(i).select("td").get(0).text().contains("정보없음")) {
                        for (int k = 0; k < sItem.busRouteItem.size(); k++) {
                            if (sItem.busRouteItem.get(k).busRouteApiId != null
                                    && sItem.busRouteItem.get(k).busRouteApiId.equals(id1)) {


                                if (sItem.busRouteItem.get(k).arriveInfo.size() == 0) {
                                    ArriveItem arriveItem = new ArriveItem();
                                    arriveItem.state = Constants.STATE_PREPARE_NOT;

                                    sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                }
                                break;

                            }
                        }
                    } else {


                        int state = -1;
                        if (ddTag.get(i).select("td").size() == 2) {

                            try {

                                if (ddTag.get(i).select("td").get(0).text().contains("차고지")) {
                                    state = Constants.STATE_PREPARE_START;
                                } else {

                                    remainStop = Integer.parseInt(ddTag.get(i).select("td").get(0).select("b").text());
                                    if (ddTag.get(i).select("td").get(1).text().contains("잠시후")) {
                                        state = Constants.STATE_NEAR;
                                    } else {
                                        remainMin = Integer.parseInt(ddTag.get(i).select("td").get(1).select("b").text());
                                    }

                                }


                            } catch (Exception e) {
                                e.printStackTrace();

                            }


                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {


                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(id1)) {


                                    if (sItem.busRouteItem.get(k).arriveInfo.size() == 0) {
                                        ArriveItem arriveItem = new ArriveItem();
                                        if (state == -1) {
                                            arriveItem.remainMin = remainMin;
                                            arriveItem.remainStop = remainStop;
                                            arriveItem.state = Constants.STATE_ING;
                                        } else {
                                            arriveItem.state = state;
                                        }

                                        sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    }

                                    break;

                                }
                            }

                        }
                    }
                }

            }
        }

        sItem.depthIndex = 0;

        return sItem;
    }


    @Override
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {

        super.getDepthStopList(sItem);


        String param = String.format("route_id=%s", sItem.tempId);
        String htmlResult = RequestCommonFuction.getSource("http://its.wonju.go.kr/map/RoutePosition.do", true, param, "utf-8");

        if (htmlResult != null) {
            try {
                JSONArray json = new JSONArray(htmlResult);

                for (int i = 0; i < json.length(); i++) {
                    JSONObject object = (JSONObject) json.get(i);
                    int index = object.getInt("STATION_ORD");
                    String plateNo = object.getString("PLATE_NO").trim();

                    plateNo = plateNo.replace("자", "자 ");

                    sItem.busStopItem.get(index - 1).isExist = true;
                    sItem.busStopItem.get(index - 1).plainNum = plateNo;


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
        int resultState = -1;  //-1 인경우가 정상  0인경우가 오류


        String param = String.format("station_id=%s", sItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource("http://its.wonju.go.kr/map/AjaxRouteListByStop.do", false, param, "utf-8");


        if (htmlResult != null) {


            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("div[class=nw_table1 ]").select("tr");

            for (int i = 0; i < ddTag.size(); i++) {
                String ids = ddTag.get(i).attr("id");

                String id1 = ids.split("_")[0];
                String id2 = ids.split("_")[1];

                int remainStop = 0;
                int remainMin = 0;


                //도착 예정 정보 없음
                if (ddTag.get(i).select("td").size() > 1) {
                    if (ddTag.get(i).select("td").get(0).text().contains("정보없음")) {

                        if (sItem.busRouteApiId != null
                                && sItem.busRouteApiId.equals(id1)) {


                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_PREPARE_NOT;


                            arriveItems.add(arriveItem);


                            break;
                        }


                    } else {


                        int state = -1;

                        if (ddTag.get(i).select("td").size() == 2) {
                            try {
                                if (ddTag.get(i).select("td").get(0).text().contains("차고지")) {
                                    state = Constants.STATE_PREPARE_START;
                                } else {

                                    remainStop = Integer.parseInt(ddTag.get(i).select("td").get(0).select("b").text());
                                    if (ddTag.get(i).select("td").get(1).text().contains("잠시후")) {
                                        state = Constants.STATE_NEAR;
                                    } else {
                                        remainMin = Integer.parseInt(ddTag.get(i).select("td").get(1).select("b").text());
                                    }

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            if (sItem.busRouteApiId != null
                                    && sItem.busRouteApiId.equals(id1)) {


                                ArriveItem arriveItem = new ArriveItem();
                                if (state == -1) {
                                    arriveItem.remainMin = remainMin;
                                    arriveItem.remainStop = remainStop;
                                    arriveItem.state = Constants.STATE_ING;
                                } else {
                                    arriveItem.state = state;
                                }

                                arriveItems.add(arriveItem);

                                break;

                            }


                        }
                    }
                }

            }


            if (arriveItems.size() == 0) {

                ArriveItem arriveItem = new ArriveItem();
                arriveItem.state = Constants.STATE_END;
                arriveItems.add(arriveItem);
            }

        } else {
            resultState = 0;
        }


        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);
        depthAlarmItem.state = resultState;


        return depthAlarmItem;
    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);


        String sId = null;

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        String param = String.format("station_id=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource("http://its.wonju.go.kr/map/AjaxRouteListByStop.do", false, param, "utf-8");


        if (htmlResult != null) {


            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("div[class=nw_table1 ]").select("tr");

            for (int i = 0; i < ddTag.size(); i++) {
                String ids = ddTag.get(i).attr("id");

                String id1 = ids.split("_")[0];
                String id2 = ids.split("_")[1];

                int remainStop = 0;
                int remainMin = 0;


                if (ddTag.get(i).select("td").size() > 0) {

                    if (ddTag.get(i).select("td").get(0).text().contains("정보없음")) {

                        if (favoriteAndHistoryItem.busRouteItem.busRouteApiId != null
                                && favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(id1)) {


                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_PREPARE_NOT;

                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                            break;
                        }


                    } else {


                        int state = -1;

                        if (ddTag.get(i).select("td").size() == 2) {
                            try {
                                if (ddTag.get(i).select("td").get(0).text().contains("차고지")) {
                                    state = Constants.STATE_PREPARE_START;
                                } else {

                                    remainStop = Integer.parseInt(ddTag.get(i).select("td").get(0).select("b").text());
                                    if (ddTag.get(i).select("td").get(1).text().contains("잠시후")) {
                                        state = Constants.STATE_NEAR;
                                    } else {
                                        remainMin = Integer.parseInt(ddTag.get(i).select("td").get(1).select("b").text());
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            if (favoriteAndHistoryItem.busRouteItem.busRouteApiId != null
                                    && favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(id1)) {


                                ArriveItem arriveItem = new ArriveItem();
                                if (state == -1) {
                                    arriveItem.remainMin = remainMin;
                                    arriveItem.remainStop = remainStop;
                                    arriveItem.state = Constants.STATE_ING;
                                } else {
                                    arriveItem.state = state;
                                }

                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                                break;

                            }


                        }
                    }

                }


            }
        }

        return sItem;
    }

}
