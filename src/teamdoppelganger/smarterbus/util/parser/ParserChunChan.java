package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ParserChunChan extends CommonParser {

    public ParserChunChan(SQLiteDatabase db) {
        super(db);

    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_CHUN_CHEON._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_CHUN_CHEON._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_CHUN_CHEON._cityId);


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
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {


        String url = "http://www.chbis.kr/serviceRoute.do";
        String param = String.format("prmOperation=getServiceRoute&prmRouteName=&prmRouteID=%s", sItem.tempId);
        String parseStr = RequestCommonFuction.getSource(url, true, param,
                "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            Elements trTag = doc.select("div[class=popup_table_scroll_01  busRoute").select("table").select("tbody").select("tr");

            int k = 0;

            for (int i = 0; i < trTag.size(); i++) {

                if (i % 2 == 0) {

                    Elements tdTag = trTag.get(i).select("td");

                    if ((i / 2) % 2 == 0) {

                        for (int j = 0; j < tdTag.size(); j++) {

                            if (tdTag.get(j).attr("class").equals("routeLineH") && tdTag.get(j).select("img").size() >= 1) {

                                if (tdTag.get(j).select("img").attr("src").contains("icon_bus")) {
                                    sItem.busStopItem.get(k++).isExist = true;
                                } else {
                                    sItem.busStopItem.get(k++).isExist = false;
                                }

                            }

                        }

                    } else {

                        for (int j = tdTag.size() - 1; j >= 0; j--) {

                            if (tdTag.get(j).attr("class").equals("routeLineH") && tdTag.get(j).select("img") != null) {

                                if (tdTag.get(j).select("img").attr("src").contains("icon_bus")) {
                                    sItem.busStopItem.get(k++).isExist = true;
                                } else {
                                    sItem.busStopItem.get(k++).isExist = false;
                                }

                            }
                        }
                    }
                }
            }

            sItem.depthIndex = 0;

        }

        return sItem;

    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();
        HashMap<String, BusRouteItem> mTempBusMap = new HashMap<String, BusRouteItem>();

        String url = String.format("http://www.chbis.kr/stationInfo.do",
                sItem.apiId);
        String param = String.format("prmOperation=getStationInfo&prmStationName=&prmStationID=%s", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource(url, true, param,
                "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("div[class=popup_table_scroll_02]");


            if (ddTag.size() != 2) return null;

            Elements td1Tag = ddTag.get(1).select("tr");

            for (int j = 0; j < td1Tag.size(); j++) {

                if (td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").size() < 2)
                    continue;
                String apiId = td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(0).select("input").attr("value");
                String name = td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(1).text().trim();

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = name;
                routeItem.busRouteApiId = apiId;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHUN_CHEON._cityId);

                mTempBusMap.put(name, routeItem);
            }


            Elements td2Tag = ddTag.get(0).select("tr");

            for (int j = 0; j < td2Tag.size(); j++) {

                if (td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").size() < 3)
                    continue;
                String name = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(0).text();
                String min = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(1).text();
                String where = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(3).text();

                BusRouteItem item = mTempBusMap.get(name);
                if (item != null) {

                    ArriveItem arrive = new ArriveItem();

                    if (min.contains("분")) {
                        min = min.replace("분", "");
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.state = Constants.STATE_ING;
                    }

                    if (where.contains("정류소전")) {

                        where = where.replace("정류소전", "");
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_ING;

                    } else if (where.contains("잠시후 도착")) {

                        arrive.remainMin = 0;
                        arrive.remainStop = 0;
                        arrive.state = Constants.STATE_NEAR;

                    }

                    item.arriveInfo.add(arrive);

                    mTempBusMap.put(name, item);
                }
            }
        }


        Collection<BusRouteItem> values = mTempBusMap.values();

        for (BusRouteItem item : values) {
            localBusRouteItems.add(item);
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
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        HashMap<String, BusRouteItem> mTempBusMap = new HashMap<String, BusRouteItem>();

        String url = String.format("http://www.chbis.kr/stationInfo.do",
                sItem.busStopApiId);
        String param = String.format("prmOperation=getStationInfo&prmStationName=&prmStationID=%s", sItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, true, param,
                "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("div[class=popup_table_scroll_02]");

            if (ddTag.size() != 2) return null;

            Elements td1Tag = ddTag.get(1).select("tr");
            for (int j = 0; j < td1Tag.size(); j++) {

                if (td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").size() < 2)
                    continue;
                String apiId = td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(0).select("input").attr("value");
                String name = td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(1).text().trim();

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = name;
                routeItem.busRouteApiId = apiId;

                mTempBusMap.put(name, routeItem);
            }


            Elements td2Tag = ddTag.get(0).select("tr");

            for (int j = 0; j < td2Tag.size(); j++) {

                if (td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").size() < 3)
                    continue;
                String name = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(0).text();
                String min = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(1).text();
                String where = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(3).text();

                BusRouteItem item = mTempBusMap.get(name);
                if (item != null) {

                    ArriveItem arrive = new ArriveItem();

                    if (min.contains("분")) {

                        min = min.replace("분", "");
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.state = Constants.STATE_ING;

                    }

                    if (where.contains("정류소전")) {

                        where = where.replace("정류소전", "");
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_ING;

                    } else if (where.contains("잠시후 도착")) {

                        arrive.remainMin = 0;
                        arrive.remainStop = 0;
                        arrive.state = Constants.STATE_NEAR;

                    }

                    item.arriveInfo.add(arrive);

                    mTempBusMap.put(name, item);
                }
            }


            Collection<BusRouteItem> values = mTempBusMap.values();
            for (BusRouteItem item : values) {

                if (item.busRouteApiId.equals(sItem.busRouteApiId)) {
                    if (item.arriveInfo != null && item.arriveInfo.size() > 0) {
                        arriveItems.addAll(item.arriveInfo);
                    }
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

        HashMap<String, BusRouteItem> mTempBusMap = new HashMap<String, BusRouteItem>();

        String url = String.format("http://www.chbis.kr/stationInfo.do",
                favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String param = String.format("prmOperation=getStationInfo&prmStationName=&prmStationID=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, true, param,
                "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("div[class=popup_table_scroll_02]");

            if (ddTag.size() != 2) return null;

            Elements td1Tag = ddTag.get(1).select("tr");

            for (int j = 0; j < td1Tag.size(); j++) {

                if (td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").size() < 2)
                    continue;
                String apiId = td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(0).select("input").attr("value");
                String name = td1Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(1).text().trim();

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = name;
                routeItem.busRouteApiId = apiId;

                mTempBusMap.put(name, routeItem);
            }


            Elements td2Tag = ddTag.get(0).select("tr");

            for (int j = 0; j < td2Tag.size(); j++) {

                if (td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").size() < 3)
                    continue;
                String name = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(0).text();
                String min = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(1).text();
                String where = td2Tag.get(j).select("td[class=text_12_08 padding_6_0_6_0]").get(3).text();

                BusRouteItem item = mTempBusMap.get(name);
                if (item != null) {

                    ArriveItem arrive = new ArriveItem();

                    if (min.contains("분")) {

                        min = min.replace("분", "");
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.state = Constants.STATE_ING;
                    }

                    if (where.contains("정류소전")) {

                        where = where.replace("정류소전", "");
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_ING;

                    } else if (where.contains("잠시후 도착")) {

                        arrive.remainMin = 0;
                        arrive.remainStop = 0;
                        arrive.state = Constants.STATE_NEAR;

                    }

                    item.arriveInfo.add(arrive);
                    mTempBusMap.put(name, item);
                }
            }


            Collection<BusRouteItem> values = mTempBusMap.values();
            for (BusRouteItem item : values) {

                if (item.busRouteApiId.equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId)) {
                    if (item.arriveInfo != null && item.arriveInfo.size() > 0) {
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.addAll(item.arriveInfo);
                    }
                }

            }
        }

        return sItem;
    }


}
