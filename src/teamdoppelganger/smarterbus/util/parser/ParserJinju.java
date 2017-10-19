package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator.IsEmpty;

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

public class ParserJinju extends CommonParser {

    public ParserJinju(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_JIN_JU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_JIN_JU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_JIN_JU._cityId);


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

        String url = "http://bis.jinju.go.kr/MainBusRouteListAjax.do";
        String param = String.format("brt_id=%s", sItem.tempId);
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            try {
                JSONObject jsonObject = new JSONObject(parseStr);
                JSONArray jsonArray = jsonObject.getJSONArray("location");


                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);

                    String plainNum = object.getString("Bus_NO");
                    String stopArsId = object.getString("Stop_ID");

                    for (int k = 0; k < sItem.busStopItem.size(); k++) {

                        if (sItem.busStopItem.get(k).arsId.equals(stopArsId)) {
                            sItem.busStopItem.get(k).isExist = true;
                            if (plainNum.length() > 5) {
                                plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                sItem.busStopItem.get(k).plainNum = plainNum;
                            }
                            break;
                        }

                    }

                }

            } catch (JSONException e) {

            }
        }

        sItem.depthIndex = 0;

        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();


        String url = String.format("http://bis.jinju.go.kr/MainBusArrivalListAjax.do");
        String param = String.format("stop_id=%s", sItem.arsId);

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            if (parseStr != null) {

                try {
                    JSONObject jsonObject = new JSONObject(parseStr);
                    JSONArray jsonArray = jsonObject.getJSONObject("AllBusArrivalInfoResult").getJSONObject("AllBusArrivalInfo")
                            .getJSONObject("MsgBody").getJSONObject("BUSINFO").getJSONObject("CurrentAllBusArrivalInfo").getJSONArray("list");


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String busApiId = object.getString("routeId");
                        String busName = object.getString("lineNo");
                        int remainTime = -1, remainStop = -1, remainSecond = -1;

                        try {
                            remainStop = Integer.valueOf(object.getString("remainStopCnt"));
                        } catch (Exception e) {
                        }

                        try {
                            int tmpTime = Integer.valueOf(object.getString("remainTime"));
                            remainTime = tmpTime / 60;
                            remainSecond = tmpTime % 60;
                        } catch (Exception e) {

                        }


                        if (remainStop != -1 || remainTime != -1) {

                            BusRouteItem routeItem = new BusRouteItem();
                            routeItem.busRouteName = busName;
                            routeItem.busRouteApiId = busApiId;
                            routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JIN_JU._cityId);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = remainTime;
                            arriveItem.remainStop = remainStop;
                            arriveItem.remainSecond = remainSecond;
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


        String url = String.format("http://bis.jinju.go.kr/MainBusArrivalListAjax.do");
        String param = String.format("stop_id=%s", sItem.busStopArsId);

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            if (parseStr != null) {

                try {
                    JSONObject jsonObject = new JSONObject(parseStr);
                    JSONArray jsonArray = jsonObject.getJSONObject("AllBusArrivalInfoResult").getJSONObject("AllBusArrivalInfo")
                            .getJSONObject("MsgBody").getJSONObject("BUSINFO").getJSONObject("CurrentAllBusArrivalInfo").getJSONArray("list");


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String busApiId = object.getString("routeId");
                        String busName = object.getString("lineNo");
                        int remainTime = -1, remainStop = -1, remainSecond = -1;

                        try {
                            remainStop = Integer.valueOf(object.getString("remainStopCnt"));
                        } catch (Exception e) {
                        }

                        try {
                            int tmpTime = Integer.valueOf(object.getString("remainTime"));
                            remainTime = tmpTime / 60;
                            remainSecond = tmpTime % 60;
                        } catch (Exception e) {

                        }


                        if (sItem.busRouteApiId.equals(busApiId)) {

                            if (remainStop != -1 || remainTime != -1) {
                                BusRouteItem routeItem = new BusRouteItem();
                                routeItem.busRouteName = busName;
                                routeItem.busRouteApiId = busApiId;
                                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JIN_JU._cityId);

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = remainTime;
                                arriveItem.remainStop = remainStop;
                                arriveItem.remainSecond = remainSecond;
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


        String url = String.format("http://bis.jinju.go.kr/MainBusArrivalListAjax.do");
        String param = String.format("stop_id=%s", favoriteAndHistoryItem.busRouteItem.busStopArsId);

        String[] reference = {"Accept", "application/json, text/javascript, */*; q=0.01"};
        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8", reference);


        if (parseStr != null) {

            if (parseStr != null) {

                try {
                    JSONObject jsonObject = new JSONObject(parseStr);
                    JSONArray jsonArray = jsonObject.getJSONObject("AllBusArrivalInfoResult").getJSONObject("AllBusArrivalInfo")
                            .getJSONObject("MsgBody").getJSONObject("BUSINFO").getJSONObject("CurrentAllBusArrivalInfo").getJSONArray("list");


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.getJSONObject(i);
                        String busApiId = object.getString("routeId");
                        String busName = object.getString("lineNo");
                        int remainTime = -1, remainStop = -1, remainSecond = -1;
                        ;
                        try {
                            remainStop = Integer.valueOf(object.getString("remainStopCnt"));
                        } catch (Exception e) {
                        }

                        try {
                            int tmpTime = Integer.valueOf(object.getString("remainTime"));
                            remainTime = tmpTime / 60;
                            remainSecond = tmpTime % 60;
                        } catch (Exception e) {

                        }


                        if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(busApiId)) {

                            if (remainStop != -1 || remainTime != -1) {
                                BusRouteItem routeItem = new BusRouteItem();
                                routeItem.busRouteName = busName;
                                routeItem.busRouteApiId = busApiId;
                                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JIN_JU._cityId);

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = remainTime;
                                arriveItem.remainStop = remainStop;
                                arriveItem.remainSecond = remainSecond;
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
        }


        return sItem;


    }

}
