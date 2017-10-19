package teamdoppelganger.smarterbus.util.parser;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

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

public class ParserGimCheon extends CommonParser {

    public ParserGimCheon(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GIM_CHEON._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GIM_CHEON._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GIM_CHEON._cityId);


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

        sItem.depthIndex = 0;

        return sItem;

    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = String.format("http://bus.gimcheon.go.kr/map/AjaxRouteListByStop.do?station_id=%s&route_id=", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tr[id=_]");

            for (int j = 0; j < ddTag.size(); j++) {


                String busName = ddTag.get(j).select("th").select("p").select("span").text();
                String where = ddTag.get(j).select("td").get(0).text();
                String min = ddTag.get(j).select("td").get(1).text();

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = busName;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GIM_CHEON._cityId);
                routeItem.busStopArsId = sItem.arsId;
                routeItem.busStopName = sItem.name;

                if (min.contains("분후")) {
                    min = min.replace("분후 도착예정", "").trim();
                    where = where.replace(" 정거장전", "").trim();
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.remainStop = Integer.parseInt(where);
                    arrive.state = Constants.STATE_ING;
                    routeItem.arriveInfo.add(arrive);
                }
                if (min.contains("잠시후")) {
                    min = "0";
                    where = "0";
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.remainStop = Integer.parseInt(where);
                    arrive.state = Constants.STATE_NEAR;
                    routeItem.arriveInfo.add(arrive);
                }

                localBusRouteItems.add(routeItem);
            }
        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

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


        String url = String.format("http://bus.gimcheon.go.kr/map/AjaxRouteListByStop.do?station_id=%s&route_id=", sItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tr[id=_]");

            for (int j = 0; j < ddTag.size(); j++) {


                String busName = ddTag.get(j).select("th").select("p").select("span").text();
                String where = ddTag.get(j).select("td").get(0).text();
                String min = ddTag.get(j).select("td").get(1).text();
                String fBusName = sItem.busRouteName.split("\\(")[0];

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = busName;

                if (min.contains("분후")) {
                    if (busName.equals(fBusName)) {
                        min = min.replace("분후 도착예정", "").trim();
                        where = where.replace(" 정거장전", "").trim();
                        ArriveItem arrive = new ArriveItem();
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arrive);
                        arriveItems.add(arrive);
                    }
                }

                if (min.contains("잠시후")) {
                    if (busName.equals(fBusName)) {
                        min = "1";
                        where = "1";
                        ArriveItem arrive = new ArriveItem();
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.remainStop = Integer.parseInt(where);
                        arrive.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arrive);
                        arriveItems.add(arrive);
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


        String url = String.format("http://bus.gimcheon.go.kr/map/AjaxRouteListByStop.do?station_id=%s&route_id=", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tr[id=_]");

            for (int j = 0; j < ddTag.size(); j++) {


                String busName = ddTag.get(j).select("th").select("p").select("span").text();
                String where = ddTag.get(j).select("td").get(0).text();
                String min = ddTag.get(j).select("td").get(1).text();
                String fBusName = (favoriteAndHistoryItem.busRouteItem.busRouteName).split("\\(")[0];

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = busName;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GIM_CHEON._cityId);


                if (min.contains("분후") && busName.equals(fBusName)) {
                    min = min.replace("분후 도착예정", "").trim();
                    where = where.replace(" 정거장전", "").trim();
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.remainStop = Integer.parseInt(where);
                    arrive.state = Constants.STATE_ING;
                    routeItem.arriveInfo.add(arrive);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arrive);
                }

                if (min.contains("잠시후") && busName.equals(fBusName)) {
                    min = "0";
                    where = "0";
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.remainStop = Integer.parseInt(where);
                    arrive.state = Constants.STATE_NEAR;
                    routeItem.arriveInfo.add(arrive);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arrive);
                }


            }
        }


        return sItem;

    }

}
