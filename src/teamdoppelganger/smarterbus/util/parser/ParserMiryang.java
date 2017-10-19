package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
import com.smart.lib.CommonConstants.CityInfo;

public class ParserMiryang extends CommonParser {

    LinkedHashMap<String, BusStopItem> mHash = new LinkedHashMap<String, BusStopItem>();

    public ParserMiryang(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_MIR_YANG._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_MIR_YANG._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_MIR_YANG._cityId);


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


        ArrayList<BusStopItem> localBusStopItem = new ArrayList<BusStopItem>();


        String url = "http://bis.miryang.go.kr/bus_map/04.asp?lineId=" + sItem.tempId;

        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("div[class=bus_line]");

            int k = 0;

            for (int i = 0; i < seqElements.size(); i++) {

                Elements seqElements1 = seqElements.get(i).select("dl");
                String endStr = "";

                if (i % 2 == 0) {

                    for (int j = 0; j < seqElements1.size(); j++) {

                        String strID = seqElements1.get(j).select("dd").select("span").attr("onmouseout");
                        String plainNum = seqElements1.get(j).select("dt").select("span").attr("class");

                        if (plainNum.equals("bus_no")) {
                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = seqElements.get(i).select("dt").select("span").text().trim();
                        }

                        if (strID.equals("fnStopArriveOut();")) {
                            k++;
                        }

                    }

                } else {


                    for (int j = seqElements1.size() - 1; j >= 0; j--) {

                        String strID = seqElements1.get(j).select("dd").select("span").attr("onmouseout");
                        String plainNum = seqElements1.get(j).select("dt").select("span").attr("class");

                        if (plainNum.equals("bus_no")) {
                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = seqElements.get(i).select("dt").select("span").text().trim();
                        }

                        if (strID.equals("fnStopArriveOut();")) {
                            k++;
                        }

                    }

                }

            }

        }

        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(localBusStopItem);

        sItem.depthIndex = 0;

        return sItem;
    }


    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String url = "http://bis.miryang.go.kr/bus_map/ajax/station_list.asp";


        String parseStr = RequestCommonFuction.getSource(url, false, "schType=arrive_info_main&keyword1=" + sItem.arsId, "euc-kr");


        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("root").select("result2");

            for (int i = 0; i < seqElements.size(); i++) {

                String num = seqElements.get(i).attr("LINE_NAME").replace("번", "");
                String where = seqElements.get(i).attr("REMAIN_CNT");
                String min = seqElements.get(i).attr("REMAIN_TIME");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = num;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_MIR_YANG._cityId);


                if (min.contains("분")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min.split(" ")[0].trim());
                    arriveItem.remainStop = Integer.parseInt(where.trim());
                    arriveItem.state = Constants.STATE_ING;

                    routeItem.arriveInfo.add(arriveItem);
                }

                localBusRouteItem.add(routeItem);

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
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {

        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {

        return null;
    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        String url = "http://bis.miryang.go.kr/bus_map/ajax/station_list.asp";
        String parseStr = RequestCommonFuction.getSource(url, false, "schType=arrive_info_main&keyword1=" + favoriteAndHistoryItem.busRouteItem.busStopArsId, "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("root").select("result2");

            for (int i = 0; i < seqElements.size(); i++) {

                String num = seqElements.get(i).attr("LINE_NAME").replace("번", "");
                String where = seqElements.get(i).attr("REMAIN_CNT");
                String min = seqElements.get(i).attr("REMAIN_TIME");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = num;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_MIR_YANG._cityId);


                if (min.contains("분") && favoriteAndHistoryItem.busRouteItem.busRouteName.equals(num)) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min.split(" ")[0].trim());
                    arriveItem.remainStop = Integer.parseInt(where.trim());
                    arriveItem.state = Constants.STATE_ING;
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                }

            }
        }

        return sItem;
    }

    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = "http://bis.miryang.go.kr/bus_map/ajax/station_list.asp";

        String parseStr = RequestCommonFuction.getSource(url, false, "schType=arrive_info_main&keyword1=" + sItem.busStopArsId, "euc-kr");


        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("root").select("result2");

            for (int i = 0; i < seqElements.size(); i++) {

                String num = seqElements.get(i).attr("LINE_NAME").replace("번", "");
                String where = seqElements.get(i).attr("REMAIN_CNT");
                String min = seqElements.get(i).attr("REMAIN_TIME");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = num;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_MIR_YANG._cityId);


                if (min.contains("분")) {
                    if (sItem.busRouteName.equals(num)) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min.split(" ")[0].trim());
                        arriveItem.remainStop = Integer.parseInt(where.trim());
                        arriveItem.state = Constants.STATE_ING;
                        arriveItems.add(arriveItem);
                    }
                }

            }
        }

        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;
    }

}
