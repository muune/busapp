package teamdoppelganger.smarterbus.util.parser;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

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
import teamdoppelganger.smarterbus.util.common.StaticCommonFuction;

/**
 * 부산쪽은  id를 2개를 이용해야되는 문제점들이 있어서 (사이트가 두개라서...)
 * <p>
 * 한쪽은 http://mits.busan.go.kr/bus_no.jsp
 * 하나로 사용하기위해서 디비값을 끌어와서 사용하는 방식으로 적용
 *
 * @author DOPPELSOFT4
 */
public class ParserBusan extends CommonParser {


    String mStopUrl = "http://bus.busan.go.kr/busanBIMS/Ajax/busStopList.asp?optBusNum=";
    //String mStopUrl2 = "http://121.174.75.12/bims/Menu03/code04_02.aspx";
    String mStopUrl2 = "http://121.174.75.24/bims_web/popup2/RealTimeBus.aspx";
    String mLineUrl = "http://bus.busan.go.kr/busanBIMS/Ajax/map_Arrival.asp";

    public ParserBusan(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {
        // TODO Auto-generated method stub

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_BU_SAN._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_BU_SAN._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.tempId2 = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_BU_SAN._cityId);


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

        depthStopItem.tempId = rItem.busRouteApiId2;

        depthStopItem.depthIndex = 1;

        return depthStopItem;

    }


    @Override
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {

        super.getDepthStopList(sItem);

        try {

            String busNo = sItem.tempId;
            if (busNo.equals("88(A)")) {
                busNo = "88";
            } else if (busNo.equals("88-1A")) {
                busNo = "88-1";
            }

            //String parseStr = RequestCommonFuction.getSource("http://121.174.75.12/bims/Menu03/Code04_01.aspx?bno=" + busNo, true, "", "euc-kr");
            String parseStr = RequestCommonFuction.getSource("http://61.43.246.207/bus/rlmv.asp", false, "lineid=" + busNo, "euc-kr");

            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements ddTag = doc.select("linelist");

                for (int i = 0; i < ddTag.size(); i++) {

                    Element eleTag = ddTag.get(i);

                    String plainNum = eleTag.select("busnum").text().trim();

                    if (plainNum != "") {

                        sItem.busStopItem.get(i).isExist = true;
                        sItem.busStopItem.get(i).plainNum = plainNum;


                    }
                }
            }

        } catch (Exception e) {

        }


        sItem.depthIndex = 0;


        return sItem;
    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String tmpId = "";
        if (sItem.arsId.length() == 4) {
            tmpId = "0" + sItem.arsId;
        }

        String url = String.format("http://bus.busan.go.kr/busanBIMS/Ajax/map_Arrival.asp?optARSNO=%s", sItem.tempId2);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            Elements seqElements = doc.select("bus");
            for (int i = 0; i < seqElements.size(); i++) {

                String apiId = seqElements.get(i).attr("value6");
                String remainStop = seqElements.get(i).attr("value4");
                String remainMin = seqElements.get(i).attr("value5");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_BU_SAN._cityId);
                routeItem.busRouteApiId = apiId;

                if (remainStop != null && !remainStop.equals("-")) {

                    int remainStopInt = Integer.parseInt(remainStop);
                    int remainMinInt = Integer.parseInt(remainMin);

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMinInt;
                    arriveItem.remainStop = remainStopInt;


                    routeItem.arriveInfo.add(arriveItem);

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


        String url = String.format("http://bus.busan.go.kr/busanBIMS/Ajax/map_Arrival.asp?optARSNO=%s", sItem.tmpId);

        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");
        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("bus");

            for (int i = 0; i < seqElements.size(); i++) {

                String apiId = seqElements.get(i).attr("value6");
                String remainStop = seqElements.get(i).attr("value4");
                String remainMin = seqElements.get(i).attr("value5");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_BU_SAN._cityId);
                routeItem.busRouteApiId = apiId;

                if (remainStop != null && !remainStop.equals("-")) {

                    int remainStopInt = Integer.parseInt(remainStop);
                    int remainMinInt = Integer.parseInt(remainMin);

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMinInt;
                    arriveItem.remainStop = remainStopInt;
                    arriveItem.state = Constants.STATE_ING;


                    routeItem.arriveInfo.add(arriveItem);

                    if (apiId.equals(sItem.busRouteApiId)) {
                        arriveItems.add(arriveItem);
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


        String url = String.format("http://bus.busan.go.kr/busanBIMS/Ajax/map_Arrival.asp?optARSNO=%s", favoriteAndHistoryItem.busRouteItem.tmpId);

        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");
        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("bus");

            for (int i = 0; i < seqElements.size(); i++) {

                String apiId = seqElements.get(i).attr("value6");
                String remainStop = seqElements.get(i).attr("value4");
                String remainMin = seqElements.get(i).attr("value5");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_BU_SAN._cityId);
                routeItem.busRouteApiId = apiId;

                if (remainStop != null && !remainStop.equals("-")) {

                    int remainStopInt = Integer.parseInt(remainStop);
                    int remainMinInt = Integer.parseInt(remainMin);

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMinInt;
                    arriveItem.remainStop = remainStopInt;


                    routeItem.arriveInfo.add(arriveItem);

                    if (apiId.equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId)) {
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }

                }

            }

        }

        return sItem;

    }


}


