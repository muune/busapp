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
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserPohang extends CommonParser {

    public ParserPohang(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_PO_HANG._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_PO_HANG._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_PO_HANG._cityId);


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

        String param = String.format("menuId=1&routeId=%s&cacheListLength=1&busLineTabMenuId=1", sItem.tempId);
        String htmlResult = RequestCommonFuction.getSource("http://mbis.pohang.go.kr/busLineSearch/busLineDetail.do", true, param, "utf-8");


        if (htmlResult != null) {

            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("ul[class=line_loc]").select("li");


            for (int i = 0; i < ddTag.size(); i++) {
                if (ddTag.get(i).select("li").attr("onclick").contains("goBStop")) {

                    String tmpString = ddTag.get(i).select("li").attr("onclick").split("Detail\\(")[1].replaceAll("'", "").replace(");", "");
                    String apiId = tmpString.split(",")[0];
                    String arsId = tmpString.split(",")[1];
                    String name = ddTag.get(i).select("dt").get(0).text();
                    BusStopItem item = new BusStopItem();

                    if (ddTag.get(i).select("p").size() > 0) {

                        for (int k = 0; k < sItem.busStopItem.size(); k++) {

                            if (sItem.busStopItem.get(k).apiId.equals(apiId)) {
                                sItem.busStopItem.get(k).isExist = true;
                                break;
                            }

                        }


                        item.isExist = true;
                    }
                    item.apiId = apiId;
                    item.arsId = arsId;
                    item.localInfoId = String.valueOf(CommonConstants.CITY_PO_HANG._cityId);
                    item.name = name;

                }
            }

        }

        sItem.depthIndex = 0;


        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> busRouteItem = new ArrayList<BusRouteItem>();

        String param = String.format("bstopId=%s", sItem.apiId);
        String htmlResult = RequestCommonFuction.getSource("http://bis2.ipohang.org/bis2/bstopBusArriveInfoAjax.do", true, param, "utf-8");

        if (htmlResult != null) {
            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray array = json.getJSONArray("busArriveInfoList");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = (JSONObject) array.get(i);
                    String name = object.getString("ROUTE_NAME");
                    String busId = object.getString("ROUTE_ID");
                    String min = object.getString("REST_TIME");
                    String remainStop = object.getString("REST_BSTOP");

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = busId;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_PO_HANG._cityId);

                    try {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.remainStop = Integer.parseInt(remainStop);
                        routeItem.arriveInfo.add(arriveItem);
                    } catch (Exception e) {
                    }

                    busRouteItem.add(routeItem);

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

        String param = String.format("bstopId=%s", busStopItems.apiId);
        String htmlResult = RequestCommonFuction.getSource("http://bis2.ipohang.org/bis2/bstopBusArriveInfoAjax.do", true, param, "utf-8");

        if (htmlResult != null) {
            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray array = json.getJSONArray("busArriveInfoList");

                for (int i = 0; i < array.length(); i++) {

                    JSONObject object = (JSONObject) array.get(i);
                    String id = object.getString("ROUTE_ID");
                    String stopId = object.getString("REST_BSTOP");

                    if (id.equals(busStopItems.tempId) &&
                            stopId.equals("1")) {
                        busStopItems.isExist = true;
                        busStopItems.plusParsingNeed = 0;
                    }


                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ArrayList<BusStopItem> localBusStopItem = new ArrayList<BusStopItem>();
        localBusStopItem.add(busStopItems);


        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(localBusStopItem);


        return depthStopItem;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String param = String.format("bstopId=%s", sItem.busStopApiId);
        String htmlResult = RequestCommonFuction.getSource("http://bis2.ipohang.org/bis2/bstopBusArriveInfoAjax.do", true, param, "utf-8");

        if (htmlResult != null) {
            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray array = json.getJSONArray("busArriveInfoList");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = (JSONObject) array.get(i);
                    String name = object.getString("ROUTE_NAME");
                    String busId = object.getString("ROUTE_ID");
                    String min = object.getString("REST_TIME");
                    String remainStop = object.getString("REST_BSTOP");

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = busId;
                    routeItem.localInfoId = CommonConstants.CITY_PO_HANG._engName;

                    try {

                        if (busId.equals(sItem.busRouteApiId)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = Integer.parseInt(min);
                            arriveItem.remainStop = Integer.parseInt(remainStop);
                            routeItem.arriveInfo.add(arriveItem);

                            arriveItems.add(arriveItem);
                        }


                    } catch (Exception e) {
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


}
