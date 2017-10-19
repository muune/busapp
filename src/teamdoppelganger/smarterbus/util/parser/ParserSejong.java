package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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


public class ParserSejong extends CommonParser {

    public ParserSejong(SQLiteDatabase db) {
        super(db);

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
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_SE_JONG._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_SE_JONG._engName + "_Stop",
                        CommonConstants._ID, ids[i]);


                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_SE_JONG._cityId);


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
        depthStopItem.depthIndex = 1;
        depthStopItem.tempId = rItem.busRouteApiId;

        return depthStopItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String param = String.format("busStopId=%s", sItem.apiId);
        String[] referer = {"Origin", "http://mbis.sejong.go.kr", "Referer", "http://mbis.sejong.go.kr/mobile/traffic/m_arrive_info", "Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource("http://mbis.sejong.go.kr/mobile/traffic/searchBusStopRoute.do", true, param, "utf-8", referer);
        HashMap<String, Integer> hash = new HashMap<String, Integer>();

        if (parseStr != null) {
            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonAry = jsonObject.getJSONArray("busStopRouteList");


                for (int i = 0; i < jsonAry.length(); i++) {

                    JSONObject object = jsonAry.getJSONObject(i);
                    String routeName = object.getString("route_name");
                    int remainStop = -1, remainMin = -1;
                    String routeApiId = object.getString("route_id");


                    try {

                        if (object.getString("provide_type").contains("출발")) {
                            remainMin = Integer.parseInt(object.getString("provide_type").split("시")[0]);
                            remainStop = Integer.parseInt(object.getString("provide_type").split(" ")[1].split("분")[0]);
                            BusRouteItem routeItem;
                            routeItem = new BusRouteItem();
                            routeItem.busRouteName = routeName;
                            routeItem.busRouteApiId = routeApiId;
                            routeItem.localInfoId = String.valueOf(CommonConstants.CITY_SE_JONG._cityId);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_PREPARE;
                            arriveItem.remainMin = remainMin;
                            arriveItem.remainStop = remainStop;
                            arriveItem.remainSecond = -9999;
                            routeItem.arriveInfo.add(arriveItem);
                            localBusRouteItems.add(routeItem);
                            hash.put(routeApiId, localBusRouteItems.size() - 1);

                        } else {

                            remainMin = Integer.parseInt(object.getString("provide_type").replace("약", "").split("분")[0]);
                            remainStop = Integer.parseInt(object.getString("rstop").replace("약", "").split("구")[0]);

                            BusRouteItem routeItem;

                            if (hash.get(routeApiId) == null) {

                                routeItem = new BusRouteItem();
                                routeItem.busRouteName = routeName;
                                routeItem.busRouteApiId = routeApiId;
                                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_SE_JONG._cityId);

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_ING;
                                arriveItem.remainMin = remainMin;
                                arriveItem.remainStop = remainStop;
                                routeItem.arriveInfo.add(arriveItem);
                                localBusRouteItems.add(routeItem);
                                hash.put(routeApiId, localBusRouteItems.size() - 1);
                            } else {

                                int index = hash.get(routeApiId);


                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_ING;
                                arriveItem.remainMin = remainMin;
                                arriveItem.remainStop = remainStop;
                                localBusRouteItems.get(index).arriveInfo.add(arriveItem);

                            }

                        }

                    } catch (Exception e) {
                        // TODO: handle exception
                    }


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
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {


        String param = String.format("busRouteId=%s", sItem.tempId);
        String parseStr = RequestCommonFuction.getSource("http://bis.sejong.go.kr/web/traffic/searchBusRealLocationDetail.do", true, param, "utf-8");

        if (parseStr != null) {
            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonAry = jsonObject.getJSONArray("busRealLocList");


                for (int i = 0; i < jsonAry.length(); i++) {

                    JSONObject object = jsonAry.getJSONObject(i);
                    String plainNum = object.getString("plate_no");
                    String stopApiId = object.getString("stop_id");

                    for (int j = 0; j < sItem.busStopItem.size(); j++) {

                        if (sItem.busStopItem.get(j).apiId.equals(stopApiId)) {
                            sItem.busStopItem.get(j).isExist = true;
                            if (plainNum.length() > 5) {
                                plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                sItem.busStopItem.get(j).plainNum = plainNum;
                            }
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

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        String param = String.format("busStopId=%s", sItem.busStopApiId);
        String[] referer = {"Origin", "http://mbis.sejong.go.kr", "Referer", "http://mbis.sejong.go.kr/mobile/traffic/m_arrive_info", "Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource("http://mbis.sejong.go.kr/mobile/traffic/searchBusStopRoute.do", true, param, "utf-8", referer);

        if (parseStr != null) {
            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonAry = jsonObject.getJSONArray("busStopRouteList");


                for (int i = 0; i < jsonAry.length(); i++) {

                    JSONObject object = jsonAry.getJSONObject(i);
                    String routeName = object.getString("route_name");
                    int remainStop = -1, remainMin = -1;
                    String routeApiId = object.getString("route_id");

                    try {

                        if (sItem.busRouteApiId.equals(routeApiId)) {
                            remainMin = Integer.parseInt(object.getString("provide_type").replace("약", "").split("분")[0]);
                            remainStop = Integer.parseInt(object.getString("rstop").replace("약", "").split("구")[0]);

                            BusRouteItem routeItem = new BusRouteItem();
                            routeItem.busRouteName = routeName;
                            routeItem.busRouteApiId = routeApiId;
                            routeItem.localInfoId = String.valueOf(CommonConstants.CITY_SE_JONG._cityId);


                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainMin = remainMin;
                            arriveItem.remainStop = remainStop;
                            routeItem.arriveInfo.add(arriveItem);
                            arriveItems.add(arriveItem);

                        }


                    } catch (Exception e) {
                        // TODO: handle exception
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

        String param = String.format("busStopId=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String[] referer = {"Origin", "http://mbis.sejong.go.kr", "Referer", "http://mbis.sejong.go.kr/mobile/traffic/m_arrive_info", "Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource("http://mbis.sejong.go.kr/mobile/traffic/searchBusStopRoute.do", true, param, "utf-8", referer);

        if (parseStr != null) {
            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonAry = jsonObject.getJSONArray("busStopRouteList");


                for (int i = 0; i < jsonAry.length(); i++) {

                    JSONObject object = jsonAry.getJSONObject(i);
                    String routeName = object.getString("route_name");
                    int remainStop = -1, remainMin = -1;
                    String routeApiId = object.getString("route_id");

                    try {

                        if (favoriteAndHistoryItem.busRouteItem.busRouteName.equals(routeName)) {

                            BusRouteItem routeItem = new BusRouteItem();
                            routeItem.busRouteName = routeName;
                            routeItem.busRouteApiId = routeApiId;
                            routeItem.localInfoId = String.valueOf(CommonConstants.CITY_SE_JONG._cityId);

                            ArriveItem arriveItem = new ArriveItem();

                            if (object.getString("provide_type").contains("출발")) {
                                remainMin = Integer.parseInt(object.getString("provide_type").split("시")[0]);
                                remainStop = Integer.parseInt(object.getString("provide_type").split(" ")[1].split("분")[0]);

                                arriveItem.state = Constants.STATE_PREPARE;
                                arriveItem.remainSecond = -9999;
                            } else {
                                remainMin = Integer.parseInt(object.getString("provide_type").replace("약", "").split("분")[0]);
                                remainStop = Integer.parseInt(object.getString("rstop").replace("약", "").split("구")[0]);

                                arriveItem.state = Constants.STATE_ING;
                            }

                            arriveItem.remainMin = remainMin;
                            arriveItem.remainStop = remainStop;

                            routeItem.arriveInfo.add(arriveItem);
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }


                    } catch (Exception e) {
                        // TODO: handle exception
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
