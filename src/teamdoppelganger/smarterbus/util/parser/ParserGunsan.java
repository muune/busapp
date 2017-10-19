package teamdoppelganger.smarterbus.util.parser;

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

import com.smart.lib.CommonConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ParserGunsan extends CommonParser {

    public ParserGunsan(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GUN_SAN._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);

        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GUN_SAN._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.tempId2 = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GUN_SAN._cityId);


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
        String parseStr = RequestCommonFuction.getSource("http://its.gunsan.go.kr/BusLine.do", true, "oper=line&routeId=" + sItem.tempId + "&routeName=", "euc-kr");


        if (parseStr != null) {

            try {
                Document doc = Jsoup.parse(parseStr);
                Elements ddTag = doc.select("div[class=lineLocation]").select("table").select("tr");
                for (int j = 0; j < ddTag.size(); j++) {
                    if (ddTag.get(j).select("td").get(1).attr("class").equals("busNum")) {
                        sItem.busStopItem.get(j).plainNum = "전북71자 "+ddTag.get(j).select("td").get(1).text().trim();
                        sItem.busStopItem.get(j).isExist = true;
                    }
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


        String url = String.format("http://its.gunsan.go.kr/BusStop.do");
        String param = String.format("oper=arr&stopId=%s&searchStop=", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");


        if (parseStr != null) {

            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("div[class=stopResult]").select("ul").select("li");

                for (int i = 0; i < seqElements.size(); i++) {

                    String tempStr = seqElements.get(i).text();

                    String num = tempStr.split(" ")[0].replace("번", "").trim();
                    String where = tempStr.split(" ")[1].replace("번째", "").trim();
                    String min = tempStr.split(" ")[2].replace("전(약", "").replace("분", "").trim();


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = num;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GUN_SAN._cityId);

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min);
                    arriveItem.remainStop = Integer.parseInt(where);
                    arriveItem.state = Constants.STATE_ING;

                    routeItem.arriveInfo.add(arriveItem);


                    localBusRouteItem.add(routeItem);

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItem);


        return depthRouteItem;

    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);


        String url = String.format("http://its.gunsan.go.kr/BusStop.do");
        String param = String.format("oper=arr&stopId=%s&searchStop=", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");


        if (parseStr != null) {

            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("div[class=stopResult]").select("ul").select("li");

                for (int i = 0; i < seqElements.size(); i++) {

                    String tempStr = seqElements.get(i).text();

                    String num = tempStr.split(" ")[0].replace("번", "").trim();
                    String where = tempStr.split(" ")[1].replace("번째", "").trim();
                    String min = tempStr.split(" ")[2].replace("전(약", "").replace("분", "").trim();


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = num;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GUN_SAN._cityId);
                    if (favoriteAndHistoryItem.busRouteItem.busRouteName.equals(num)) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return sItem;

    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = String.format("http://its.gunsan.go.kr/BusStop.do");
        String param = String.format("oper=arr&stopId=%s&searchStop=", sItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");

        if (parseStr != null) {

            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("div[class=stopResult]").select("ul").select("li");

                for (int i = 0; i < seqElements.size(); i++) {

                    String tempStr = seqElements.get(i).text();

                    String num = tempStr.split(" ")[0].replace("번", "").trim();
                    String where = tempStr.split(" ")[1].replace("번째", "").trim();
                    String min = tempStr.split(" ")[2].replace("전(약", "").replace("분", "").trim();


                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = num;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GUN_SAN._cityId);


                    if (sItem.busRouteName.equals(num)) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arriveItem);
                        arriveItems.add(arriveItem);
                    }

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;

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
}
