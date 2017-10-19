package teamdoppelganger.smarterbus.util.parser;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
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

public class ParserYangsan extends CommonParser {

    public ParserYangsan(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_YANG_SAN._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_YANG_SAN._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_YANG_SAN._cityId);

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

        if (sItem.tempId.length() == 7)
            sItem.tempId = "000" + sItem.tempId;
        else if (sItem.tempId.length() == 8)
            sItem.tempId = "00" + sItem.tempId;
        else if (sItem.tempId.length() == 9)
            sItem.tempId = "0" + sItem.tempId;


        String url = "http://bus.yangsan.go.kr/yangsan_2016/bus_map/map2.php";

        String parseStr = RequestCommonFuction.getSource(url, true, "lineID=" + sItem.tempId, "euc-kr");

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

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String tmpId;
        if (Integer.parseInt(sItem.arsId) > 10000) {
            tmpId = String.valueOf(Integer.parseInt(sItem.arsId) - 10000);
        } else {
            tmpId = sItem.arsId;
        }

        String parseStr = RequestCommonFuction.getSource("http://bus.yangsan.go.kr/yangsan_2016/bus_map/ajax/station_list.php", false, "schType=arrive_info&keyword1=" + tmpId, "euc-kr");

        if (parseStr != null) {
            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("root").select("result2");

                for (int i = 0; i < seqElements.size(); i++) {

                    String apiId = seqElements.get(i).attr("sLIneID");
                    String busNum = seqElements.get(i).attr("sLineNM");
                    String plainNum = seqElements.get(i).attr("bus_no");
                    String where = seqElements.get(i).attr("DIFF_STOP");
                    String whenInfo = seqElements.get(i).attr("time");

                    if (apiId.startsWith("000")) {
                        apiId = apiId.substring(3, apiId.length());
                    } else if (apiId.startsWith("00")) {
                        apiId = apiId.substring(2, apiId.length());
                    } else if (apiId.startsWith("0")) {
                        apiId = apiId.substring(1, apiId.length());
                    }

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteApiId = apiId;

                    if (!whenInfo.equals("")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainStop = Integer.parseInt(where);
                        arriveItem.remainMin = Integer.parseInt(whenInfo);
                        arriveItem.state = Constants.STATE_ING;
                        arriveItem.plainNum = plainNum;
                        routeItem.arriveInfo.add(arriveItem);

                    }

                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_YANG_SAN._cityId);
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
    public DepthRouteItem getAlarmList(String... idStr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {

        String tempId = busRouteItem.busRouteApiId;

        int size = 10 - tempId.length();
        for (int i = 0; i < size; i++) {
            tempId = "0" + tempId;
        }


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = String.format("http://bus.yangsan.go.kr/busline_loc/pop_bus_location.php?lineID=%s&busStopID=%s&lineName=%s&busStopName=%s",
                tempId, busRouteItem.busStopArsId, URLEncoder.encode(busRouteItem.busRouteName), URLEncoder.encode(busRouteItem.busStopName));


        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");


        if (parseStr == null) {


            //임시 방편으로 주소 변경시 가져오도록 적용
            String tempHtml = RequestCommonFuction.getSource(Constants.TEMP_CHANGE_URL, false, "", "euc-kr");
            if (tempHtml != null) {

                try {
                    JSONArray jsonArrayTmp = new JSONObject(tempHtml).getJSONArray("item");

                    for (int k = 0; k < jsonArrayTmp.length(); k++) {
                        JSONObject jsonTmp = (JSONObject) jsonArrayTmp.get(k);
                        String cityId = jsonTmp.getString("cityId");
                        String address = jsonTmp.getString("address");

                        if (cityId.equals(CommonConstants.CITY_YANG_SAN._cityId)) {

                            url = String.format("%s/pop_bus_location.php?lineID=%s&busStopID=%s&lineName=%s&busStopName=%s",
                                    address, tempId, busRouteItem.busStopArsId, URLEncoder.encode(busRouteItem.busRouteName), URLEncoder.encode(busRouteItem.busStopName));
                            parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");
                            break;
                        }

                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }


        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("table[class=gb_t1 nmgl]").select("tr");


            for (int j = 0; j < ddTag.size(); j++) {

                if (ddTag.get(j).select("td").size() == 4) {
                    String busNum = ddTag.get(j).select("td").get(0).text();
                    String where = ddTag.get(j).select("td").get(2).text();
                    String min = ddTag.get(j).select("td").get(3).text();

                    if (min.contains("분")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min.split(" ")[0]);
                        arriveItem.remainStop = Integer.parseInt(where.split(" ")[0]);

                        busRouteItem.arriveInfo.add(arriveItem);

                    }

                    break;
                }


            }
        }
        localBusRouteItems.add(busRouteItem);

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;


        // TODO Auto-generated method

    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {


        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String tmpId;
        if (Integer.parseInt(sItem.busStopArsId) > 10000) {
            tmpId = String.valueOf(Integer.parseInt(sItem.busStopArsId) - 10000);
        } else {
            tmpId = sItem.busStopArsId;
        }

        String parseStr = RequestCommonFuction.getSource("http://bus.yangsan.go.kr/yangsan_2016/bus_map/ajax/station_list.php", false, "schType=arrive_info&keyword1=" + tmpId, "euc-kr");

        if (parseStr != null) {
            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("root").select("result2");

                for (int i = 0; i < seqElements.size(); i++) {

                    String apiId = seqElements.get(i).attr("sLIneID");
                    String busNum = seqElements.get(i).attr("sLineNM");
                    String plainNum = seqElements.get(i).attr("bus_no");
                    String where = seqElements.get(i).attr("DIFF_STOP");
                    String whenInfo = seqElements.get(i).attr("time");

                    if (apiId.startsWith("000")) {
                        apiId = apiId.substring(3, apiId.length());
                    } else if (apiId.startsWith("00")) {
                        apiId = apiId.substring(2, apiId.length());
                    } else if (apiId.startsWith("0")) {
                        apiId = apiId.substring(1, apiId.length());
                    }

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteApiId = apiId;

                    if (!whenInfo.equals("")) {

                        if (busNum.equals(sItem.busRouteName)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainStop = Integer.parseInt(where);
                            arriveItem.remainMin = Integer.parseInt(whenInfo);
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.plainNum = plainNum;
                            routeItem.arriveInfo.add(arriveItem);
                            arriveItems.add(arriveItem);
                        }

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
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        String tmpId;
        if (Integer.parseInt(favoriteAndHistoryItem.busRouteItem.busStopArsId) > 10000) {
            tmpId = String.valueOf(Integer.parseInt(favoriteAndHistoryItem.busRouteItem.busStopArsId) - 10000);
        } else {
            tmpId = favoriteAndHistoryItem.busRouteItem.busStopArsId;
        }

        String parseStr = RequestCommonFuction.getSource("http://bus.yangsan.go.kr/yangsan_2016/bus_map/ajax/station_list.php", false, "schType=arrive_info&keyword1=" + tmpId, "euc-kr");

        if (parseStr != null) {
            try {
                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("root").select("result2");

                for (int i = 0; i < seqElements.size(); i++) {

                    String apiId = seqElements.get(i).attr("sLIneID");
                    String busNum = seqElements.get(i).attr("sLineNM");
                    String plainNum = seqElements.get(i).attr("bus_no");
                    String where = seqElements.get(i).attr("DIFF_STOP");
                    String whenInfo = seqElements.get(i).attr("time");

                    if (apiId.startsWith("000")) {
                        apiId = apiId.substring(3, apiId.length());
                    } else if (apiId.startsWith("00")) {
                        apiId = apiId.substring(2, apiId.length());
                    } else if (apiId.startsWith("0")) {
                        apiId = apiId.substring(1, apiId.length());
                    }

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteApiId = apiId;

                    if (!whenInfo.equals("")) {

                        if (favoriteAndHistoryItem.busRouteItem.busRouteName.equals(busNum) && apiId.equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainStop = Integer.parseInt(where);
                            arriveItem.remainMin = Integer.parseInt(whenInfo);
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.plainNum = plainNum;
                            routeItem.arriveInfo.add(arriveItem);
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }

                    }

                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return sItem;

    }

}
