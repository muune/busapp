package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;

import org.json.JSONArray;
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

import android.database.sqlite.SQLiteDatabase;

public class ParserCheonan extends CommonParser {

    public ParserCheonan(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }


    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();

        String param = String.format("num1=%s&num2=%s", rItem.busRouteName, rItem.busRouteApiId2);
        String htmlResult = RequestCommonFuction.getSource("http://its.cheonan.go.kr/bis/getRouteStation.do", true, param, "utf-8");


        if (htmlResult != null) {

            htmlResult = "{\"busRealLocList\":" + htmlResult + "}";

            try {

                JSONObject json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("busRealLocList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    String stop_name = object.getString("STOP_NAME");
                    String apiId1 = object.getString("NODE_ID");
                    String apiId2 = "0";
                    try {
                        apiId2 = object.getString("SERVICE_ID");
                    } catch (Exception e) {
                    }

                    BusStopItem stopItem = new BusStopItem();
                    stopItem.name = stop_name;
                    stopItem.arsId = apiId2;
                    stopItem.apiId = apiId1;
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONN_AN._cityId);

                    try {
                        String carNum = object.getString("PLATE_NO");
                        carNum = carNum.substring(0, 5) + " " + carNum.substring(5, carNum.length());
                        stopItem.plainNum = carNum;
                        stopItem.isExist = true;
                    } catch (Exception e) {
                        stopItem.isExist = false;
                    }

                    busStopItem.add(stopItem);

                }

            } catch (Exception e) {
            }

        }

        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(busStopItem);


        return depthStopItem;


    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String htmlResult = RequestCommonFuction.getSource("http://its.cheonan.go.kr/bis/predictInfo.do", true, "stopId=" + sItem.apiId, "utf-8");


        if (htmlResult != null) {

            htmlResult = "{\"busStopRouteList\":" + htmlResult + "}";

            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("busStopRouteList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    String busNum = object.getString("ROUTE_NAME");
                    String busId = object.getString("ROUTE_ID");
                    String busId2 = object.getString("ROUTE_DIRECTION");
                    String type = object.getString("PROVIDE_TYPE");
                    String where = "";
                    String min = "";
                    String busPlainNum = "";

                    try {
                        where = object.getString("RSTOP");
                        min = object.getString("RTIME");
                        busPlainNum = object.getString("PLATE_NO");
                    } catch (Exception e) {
                    }

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteName = busNum;
                    busRouteItem.busRouteApiId = busId;
                    busRouteItem.busRouteApiId2 = busId2;
                    busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONN_AN._cityId);


                    if (type.equals("3")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.plainNum = busPlainNum;
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.state = Constants.STATE_ING;
                        busRouteItem.arriveInfo.add(arriveItem);
                    }

                    localBusRouteItem.add(busRouteItem);
                }
            } catch (Exception e) {
            }
        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItem);

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String htmlResult = RequestCommonFuction.getSource("http://its.cheonan.go.kr/bis/predictInfo.do", true, "stopId=" + sItem.busStopApiId, "utf-8");


        if (htmlResult != null) {

            htmlResult = "{\"busStopRouteList\":" + htmlResult + "}";

            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("busStopRouteList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    String busNum = object.getString("ROUTE_NAME");
                    String busId = object.getString("ROUTE_ID");
                    String busId2 = object.getString("ROUTE_DIRECTION");
                    String type = object.getString("PROVIDE_TYPE");
                    String where = "";
                    String min = "";
                    String busPlainNum = "";

                    try {
                        where = object.getString("RSTOP");
                        min = object.getString("RTIME");
                        busPlainNum = object.getString("PLATE_NO");
                    } catch (Exception e) {
                    }

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteName = busNum;
                    busRouteItem.busRouteApiId = busId;
                    busRouteItem.busRouteApiId2 = busId2;
                    busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONN_AN._cityId);


                    if (type.equals("3")) {
                        if (sItem.busRouteApiId.equals(busId)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.plainNum = busPlainNum;
                            arriveItem.remainMin = Integer.parseInt(min);
                            arriveItem.remainStop = Integer.parseInt(where);
                            arriveItem.state = Constants.STATE_ING;
                            arriveItems.add(arriveItem);
                        }
                    }

                }
            } catch (Exception e) {
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

        String htmlResult = RequestCommonFuction.getSource("http://its.cheonan.go.kr/bis/predictInfo.do", true, "stopId=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "utf-8");


        if (htmlResult != null) {

            htmlResult = "{\"busStopRouteList\":" + htmlResult + "}";

            try {
                JSONObject json = new JSONObject(htmlResult);
                JSONArray jsonArray = json.getJSONArray("busStopRouteList");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject object = (JSONObject) jsonArray.get(i);

                    String busNum = object.getString("ROUTE_NAME");
                    String busId = object.getString("ROUTE_ID");
                    String busId2 = object.getString("ROUTE_DIRECTION");
                    String type = object.getString("PROVIDE_TYPE");
                    String where = "";
                    String min = "";
                    String busPlainNum = "";

                    try {
                        where = object.getString("RSTOP");
                        min = object.getString("RTIME");
                        busPlainNum = object.getString("PLATE_NO");
                    } catch (Exception e) {
                    }

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteName = busNum;
                    busRouteItem.busRouteApiId = busId;
                    busRouteItem.busRouteApiId2 = busId2;
                    busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_CHEONN_AN._cityId);


                    if (type.equals("3")) {
                        if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(busId)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.plainNum = busPlainNum;
                            arriveItem.remainMin = Integer.parseInt(min);
                            arriveItem.remainStop = Integer.parseInt(where);
                            arriveItem.state = Constants.STATE_ING;
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }
                    }

                }
            } catch (Exception e) {
            }
        }

        return sItem;
    }


}
