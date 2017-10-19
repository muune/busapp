package teamdoppelganger.smarterbus.util.parser;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

public class ParserUlsan extends CommonParser {

    public ParserUlsan(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_UL_SAN._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_UL_SAN._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_UL_SAN._cityId);


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
        LinkedHashMap<String, BusStopItem> hashBusStop = new LinkedHashMap<String, BusStopItem>();


        String url = String.format("http://apis.its.ulsan.kr:8088/Service4.svc/BusLocationInfo.xo?ctype=A&routeid=%s", sItem.tempId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8");


        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("BusLocationInfoTable");

            for (int j = 0; j < ddTag.size(); j++) {

                Element eleTag = ddTag.get(j);

                String plainNum = eleTag.select("BUSNO").text().trim();
                String stopId = eleTag.select("STOPID").text().trim();
                String name = eleTag.select("STOPNAME").text().trim();

                plainNum = plainNum.replace("자", "자 ");


                for (int k = 0; k < sItem.busStopItem.size(); k++) {

                    if (sItem.busStopItem.get(k).arsId.equals(stopId)) {
                        sItem.busStopItem.get(k).isExist = true;
                        sItem.busStopItem.get(k).plainNum = plainNum;
                        break;
                    }

                }

            }

        }


        sItem.depthIndex = 0;

        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        String url = String.format("http://apis.its.ulsan.kr:8088/Service4.svc/AllBusArrivalInfo.xo?ctype=A&stopid=%s", sItem.arsId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8");


        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("AllBusArrivalInfoTable");

            for (int j = 0; j < ddTag.size(); j++) {

                Element eleTag = ddTag.get(j);


                int status = Integer.parseInt(eleTag.select("STATUS").text().trim());
                int remainStop = Integer.parseInt(eleTag.select("REMAINSTOPCNT").text().trim());
                int time = Integer.parseInt(eleTag.select("REMAINTIME").text().trim());

                BusRouteItem busRouteItem = new BusRouteItem();

                busRouteItem.busRouteApiId = eleTag.select("ROUTEID").text().trim();
                busRouteItem.busRouteName = eleTag.select("BUSNO").text().trim();
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_UL_SAN._cityId);


                if (status == 0) {

                    int min = time / 60;

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = min;
                    arriveItem.remainSecond = time % 60;
                    if (arriveItem.remainMin == 0 && arriveItem.remainSecond == 0) {
                        arriveItem.state = Constants.STATE_END;
                    } else {
                        arriveItem.state = Constants.STATE_ING;
                    }

                    arriveItem.remainStop = remainStop;
                    busRouteItem.arriveInfo.add(arriveItem);
                }

                localBusRouteItems.add(busRouteItem);


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
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String[] information = busRouteItem.busRouteApiId.split(":");

        String no = information[0];
        String brtClass = information[1];
        String brtType = information[2];
        String brtDirection = information[3];


        String url = String.format("http://m.its.ulsan.kr/m/002/001/arrInfo.do?brtNo=%s&brtDirection=%s&brtClass=%s&bnodeOldid=%s&stopServiceid=%s&stopName=%s&brtType=%s", no, brtDirection, brtClass, busRouteItem.tmpId, busRouteItem.busStopArsId, busRouteItem.busStopArsId, URLEncoder.encode(busRouteItem.busStopName), brtType);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8");


        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("dl[class=arr]");


            for (int i = 0; i < ddTag.size(); i++) {

                if (ddTag.get(i).select("span").size() > 0) {
                    try {
                        String min = ddTag.get(i).select("span").text().split("분")[0];
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.state = Constants.STATE_ING;
                        busRouteItem.arriveInfo.add(arriveItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            busRouteItem.plusParsingNeed = 0;


            localBusRouteItems.add(busRouteItem);
        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        // TODO Auto-generated method stub
        return depthRouteItem;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);
        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String url = String.format("http://apis.its.ulsan.kr:8088/Service4.svc/AllBusArrivalInfo.xo?ctype=A&stopid=%s", favoriteAndHistoryItem.busRouteItem.busStopArsId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8");

        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("AllBusArrivalInfoTable");

            for (int j = 0; j < ddTag.size(); j++) {

                Element eleTag = ddTag.get(j);

                int remainStop = Integer.parseInt(eleTag.select("REMAINSTOPCNT").text().trim());
                int time = Integer.parseInt(eleTag.select("REMAINTIME").text().trim());

                BusRouteItem busRouteItem = new BusRouteItem();

                busRouteItem.busRouteApiId = eleTag.select("ROUTEID").text().trim();
                busRouteItem.busRouteName = eleTag.select("BUSNO").text().trim();
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_UL_SAN._cityId);


                if (remainStop != 0) {

                    if (busRouteItem.busRouteApiId.trim().equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId.trim())) {
                        int min = time / 60;

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = min;
                        arriveItem.remainSecond = time % 60;

                        if (arriveItem.remainMin == 0 && arriveItem.remainSecond == 0) {
                            arriveItem.state = Constants.STATE_END;
                        } else {
                            arriveItem.state = Constants.STATE_ING;
                        }

                        arriveItem.remainStop = remainStop;


                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }

                }

            }
        }


        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);


        return sItem;
    }

    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        String url = String.format("http://apis.its.ulsan.kr:8088/Service4.svc/AllBusArrivalInfo.xo?ctype=A&stopid=%s", sItem.busStopArsId);
        String htmlResult = RequestCommonFuction.getSource(url, false, "", "utf-8");


        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("AllBusArrivalInfoTable");

            for (int j = 0; j < ddTag.size(); j++) {

                Element eleTag = ddTag.get(j);

                int remainStop = Integer.parseInt(eleTag.select("REMAINSTOPCNT").text().trim());
                int time = Integer.parseInt(eleTag.select("REMAINTIME").text().trim());

                BusRouteItem busRouteItem = new BusRouteItem();

                busRouteItem.busRouteApiId = eleTag.select("ROUTEID").text().trim();
                busRouteItem.busRouteName = eleTag.select("BUSNO").text().trim();
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_UL_SAN._cityId);


                if (remainStop != 0) {

                    if (busRouteItem.busRouteApiId.equals(sItem.busRouteApiId)) {
                        int min = time / 60;

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = min;
                        arriveItem.remainSecond = time % 60;

                        if (arriveItem.remainMin == 0 && arriveItem.remainSecond == 0) {
                            arriveItem.state = Constants.STATE_END;
                        } else {
                            arriveItem.state = Constants.STATE_ING;
                        }

                        arriveItem.remainStop = remainStop;
                        busRouteItem.arriveInfo.add(arriveItem);

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
