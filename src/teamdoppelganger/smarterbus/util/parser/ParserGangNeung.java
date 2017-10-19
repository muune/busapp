package teamdoppelganger.smarterbus.util.parser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

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

public class ParserGangNeung extends CommonParser {

    public ParserGangNeung(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GANG_NEUNG._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GANG_NEUNG._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GANG_NEUNG._cityId);


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

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};

        String url = "http://bis.gangneung.go.kr/route/getRouteView.do";
        String param = String.format("brtId=%s", sItem.tempId);
        String parseStr = RequestCommonFuction.getSource(url, false, param, "utf-8", reference);


        if (parseStr != null) {

            try {
                JSONObject json = new JSONObject(parseStr);
                JSONArray jsonArray = json.getJSONArray("jsonArray");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    int k = object.getInt("stopSeq");
                    String carNum = object.getString("busNo");
                    carNum = carNum.substring(0, 5) + " " + carNum.substring(5, carNum.length());

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


        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String url = String.format("http://bis.gangneung.go.kr/main/getMainArrivalInfo.do");
        String param = String.format("stopId=%s", sItem.apiId);

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonArray = jsonObject.getJSONArray("jsonArray");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = jsonArray.getJSONObject(i);
                    String busApiId = object.getString("routeId");
                    String busName = object.getString("routeName");

                    int remainTime = -1, remainStop = -1, remainSecond = -1;
                    try {
                        remainStop = Integer.valueOf(object.getString("currentStopSeq")) - Integer.valueOf(object.getString("bus1StopSeq"));
                    } catch (Exception e) {
                    }

                    try {
                        remainTime = object.getInt("time1ToArrival");
                        if (remainTime <= 180) {
                            remainTime = -99;
                        } else {
                            remainSecond = remainTime % 60;
                            remainTime = remainTime / 60;
                        }

                    } catch (Exception e) {

                    }


                    if (remainStop != -1 || remainTime != -1) {

                        BusRouteItem routeItem = new BusRouteItem();
                        routeItem.busRouteName = busName;
                        routeItem.busRouteApiId = busApiId;
                        routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GANG_NEUNG._cityId);

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainTime;
                        arriveItem.remainStop = remainStop;
                        arriveItem.remainSecond = remainSecond;
                        if (remainTime == -99)
                            arriveItem.state = Constants.STATE_NEAR;
                        else
                            arriveItem.state = Constants.STATE_ING;

                        routeItem.arriveInfo.add(arriveItem);

                        localBusRouteItem.add(routeItem);
                    }

                }


            } catch (JSONException e) {
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

        String url = String.format("http://bis.gangneung.go.kr/main/getMainArrivalInfo.do");
        String param = String.format("stopId=%s", sItem.busStopApiId);

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonArray = jsonObject.getJSONArray("jsonArray");


                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = jsonArray.getJSONObject(i);
                    String busApiId = object.getString("routeId");
                    String busName = object.getString("routeName");

                    int remainTime = -1, remainStop = -1, remainSecond = -1;
                    try {
                        remainStop = Integer.valueOf(object.getString("currentStopSeq")) - Integer.valueOf(object.getString("bus1StopSeq"));
                    } catch (Exception e) {
                    }

                    try {
                        remainTime = object.getInt("time1ToArrival");
                        if (remainTime <= 180) {
                            remainTime = -99;
                        } else {
                            remainSecond = remainTime % 60;
                            remainTime = remainTime / 60;
                        }

                    } catch (Exception e) {

                    }

                    if (sItem.busRouteApiId.equals(busApiId)) {
                        if (remainStop != -1 || remainTime != -1) {

                            BusRouteItem routeItem = new BusRouteItem();
                            routeItem.busRouteName = busName;
                            routeItem.busRouteApiId = busApiId;
                            routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GANG_NEUNG._cityId);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = remainTime;
                            arriveItem.remainStop = remainStop;
                            arriveItem.remainSecond = remainSecond;
                            if (remainTime == -99)
                                arriveItem.state = Constants.STATE_NEAR;
                            else
                                arriveItem.state = Constants.STATE_ING;

                            routeItem.arriveInfo.add(arriveItem);

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

        String url = String.format("http://bis.gangneung.go.kr/main/getMainArrivalInfo.do");
        String param = String.format("stopId=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonArray = jsonObject.getJSONArray("jsonArray");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = jsonArray.getJSONObject(i);
                    String busApiId = object.getString("routeId");
                    String busName = object.getString("routeName");

                    int remainTime = -1, remainStop = -1, remainSecond = -1;

                    try {
                        remainStop = Integer.valueOf(object.getString("currentStopSeq")) - Integer.valueOf(object.getString("bus1StopSeq"));
                    } catch (Exception e) {
                    }

                    try {
                        remainTime = object.getInt("time1ToArrival");
                        if (remainTime <= 180) {
                            remainTime = -99;
                        } else {
                            remainSecond = remainTime % 60;
                            remainTime = remainTime / 60;
                        }

                    } catch (Exception e) {

                    }


                    if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(busApiId)) {
                        if (remainStop != -1 || remainTime != -1) {

                            BusRouteItem routeItem = new BusRouteItem();
                            routeItem.busRouteName = busName;
                            routeItem.busRouteApiId = busApiId;
                            routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GANG_NEUNG._cityId);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = remainTime;
                            arriveItem.remainStop = remainStop;
                            arriveItem.remainSecond = remainSecond;
                            if (remainTime == -99)
                                arriveItem.state = Constants.STATE_NEAR;
                            else
                                arriveItem.state = Constants.STATE_ING;

                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                            arriveItems.add(arriveItem);
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
