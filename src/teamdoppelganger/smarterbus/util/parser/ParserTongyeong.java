package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

//http://bms.tongyeong.go.kr/
//http://bms.tongyeong.go.kr/stationInfo.do
//받아오는 사이트가 느림

//http://bms.tongyeong.go.kr/mobile  모바일 페이지도 있다
//디사하니까 되는 페이지가 있다. but 좀 나중에 현재 시스템 불안정

public class ParserTongyeong extends CommonParser {

    public ParserTongyeong(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();

        String param = String.format("prmOperation=getStationListByRouteID&prmRouteID=%s", rItem.busRouteApiId);
        String htmlResult = RequestCommonFuction.getSource("http://bms.tongyeong.go.kr/realTimeBusInfoResult.do", true, param, "euc-kr");

        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("ul[class=resultList_ul]");

            for (int j = 0; j < ddTag.size(); j++) {
                Elements pTag = ddTag.get(j).select("p");
                String stopName = pTag.text().split("\\[")[0];
                String stopArsId = pTag.text().split("\\[")[1].replace("]", "");


                Elements aTag = pTag.select("a");
                String tmpStr = aTag.attr("href");

                String[] tmpResult = tmpStr.split("setStationPos\\(")[1].replace("')", "").replaceAll("'", "").split(",");
                String stopApiId = tmpResult[1];

                BusStopItem stopItem = new BusStopItem();
                stopItem.apiId = stopApiId;
                stopItem.arsId = stopArsId;
                stopItem.name = stopName;
                stopItem.localInfoId = String.valueOf(CommonConstants.CITY_TONG_YEONG._cityId);
                busStopItem.add(stopItem);

            }
        }
        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(busStopItem);

        return depthStopItem;


    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();
        HashMap<String, String> realTimeSaveItems = new HashMap<String, String>();
        String param = String.format("prmOperation=getStationInfo&prmStationName=&prmStationID=%s", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource("http://bms.tongyeong.go.kr/stationInfo.do", true, param, "euc-kr");

        Calendar oCalendar = Calendar.getInstance();
        int curHour = oCalendar.get(Calendar.HOUR_OF_DAY);
        int curMin = oCalendar.get(Calendar.MINUTE);

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tbody");

            if (ddTag.size() < 2) return null;

            Elements trTag = ddTag.get(0).select("tr");
            for (int i = 0; i < trTag.size(); i++) {
                Elements tdTag = trTag.get(i).select("td");

                if (tdTag.size() != 5) continue;

                String busNum = tdTag.get(0).text();
                String min = tdTag.get(2).text();
                String curLocation = tdTag.get(3).text();
                String where = tdTag.get(4).text();

                realTimeSaveItems.put(busNum.trim(), min + "/" + where);
            }


            trTag = ddTag.get(1).select("tr");
            for (int i = 0; i < trTag.size(); i++) {
                Elements tdTag = trTag.get(i).select("td");

                if (tdTag.size() != 5) continue;
                String apiId = tdTag.get(0).select("input").attr("value");
                String busName = tdTag.get(1).text();
                String realTime = realTimeSaveItems.get(busName.trim());

                BusRouteItem busRouteItem = new BusRouteItem();
                busRouteItem.busRouteApiId = apiId;
                busRouteItem.busRouteName = busName;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_TONG_YEONG._cityId);

                if (realTime != null) {

                    String min = realTime.split("\\/")[0].replace("분", "").trim();
                    String reaminStop = realTime.split("\\/")[1].replace("정류장전", "").trim();

                    if (min.contains("잠시후")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_NEAR;
                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (min.contains("시")) {


                        int stopHour = Integer.parseInt(min.split("시")[0].trim());
                        int stopMin = Integer.parseInt(min.split("시")[1].trim());

                        int remainHour = stopHour - curHour;
                        int remainMin = 0;

                        boolean isCan = false;

                        if (remainHour > 0) {

                            if (curMin == 0) {
                                remainMin = 60;
                            } else {
                                remainMin = (60 * remainHour) + stopMin - curMin;
                            }

                            isCan = true;

                        } else if (remainHour == 0) {

                            remainMin = stopMin - curMin;
                            isCan = true;

                        } else if (remainHour < 0) {


                        }


                        if (isCan) {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = remainMin;
                            arriveItem.state = Constants.STATE_ING;
                            busRouteItem.arriveInfo.add(arriveItem);

                        }


                    } else if (min.contains("운행종료")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_END;
                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (min.contains("출발대기")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE;
                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (min.contains("-")) {

                    } else {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(min);
                        arriveItem.state = Constants.STATE_ING;
                        busRouteItem.arriveInfo.add(arriveItem);
                    }


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
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        Calendar oCalendar = Calendar.getInstance();
        int curHour = oCalendar.get(Calendar.HOUR_OF_DAY);
        int curMin = oCalendar.get(Calendar.MINUTE);


        HashMap<String, String> realTimeSaveItems = new HashMap<String, String>();
        String param = String.format("prmOperation=getStationInfo&prmStationName=&prmStationID=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource("http://bms.tongyeong.go.kr/stationInfo.do", true, param, "euc-kr");


        Document doc = Jsoup.parse(parseStr);
        Elements ddTag = doc.select("tbody");

        if (ddTag.size() < 2) return null;

        Elements trTag = ddTag.get(0).select("tr");
        for (int i = 0; i < trTag.size(); i++) {
            Elements tdTag = trTag.get(i).select("td");

            if (tdTag.size() != 4) continue;

            String busNum = tdTag.get(0).text();
            String min = tdTag.get(1).text();
            String curLocation = tdTag.get(2).text();
            String where = tdTag.get(3).text();

            realTimeSaveItems.put(busNum.trim(), min + "/" + where);
        }


        trTag = ddTag.get(1).select("tr");
        for (int i = 0; i < trTag.size(); i++) {
            Elements tdTag = trTag.get(i).select("td");

            if (tdTag.size() != 5) continue;
            String apiId = tdTag.get(0).select("input").attr("value");
            String busName = tdTag.get(1).text();
            String realTime = realTimeSaveItems.get(busName.trim());

            BusRouteItem busRouteItem = new BusRouteItem();
            busRouteItem.busRouteApiId = apiId;
            busRouteItem.busRouteName = busName;
            busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_TONG_YEONG._cityId);

            if (realTime != null && busRouteItem.busRouteApiId.equals(apiId.trim())) {

                String min = realTime.split("\\/")[0].replace("분", "").trim();
                String reaminStop = realTime.split("\\/")[1].replace("정류장전", "").trim();


                if (min.contains("잠시후")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.state = Constants.STATE_NEAR;
                    busRouteItem.arriveInfo.add(arriveItem);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                } else if (min.contains("시")) {


                    int stopHour = Integer.parseInt(min.split("시")[0].trim());
                    int stopMin = Integer.parseInt(min.split("시")[1].trim());

                    int remainHour = stopHour - curHour;
                    int remainMin = 0;

                    boolean isCan = false;

                    if (remainHour > 0) {

                        if (curMin == 0) {
                            remainMin = 60;
                        } else {
                            remainMin = (60 * remainHour) + stopMin - curMin;
                        }

                        isCan = true;

                    } else if (remainHour == 0) {

                        remainMin = stopMin - curMin;
                        isCan = true;

                    } else if (remainHour < 0) {


                    }


                    if (isCan) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.state = Constants.STATE_ING;
                        busRouteItem.arriveInfo.add(arriveItem);
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                    }


                } else if (min.contains("운행종료")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.state = Constants.STATE_END;
                    busRouteItem.arriveInfo.add(arriveItem);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                } else if (min.contains("출발대기")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.state = Constants.STATE_PREPARE;
                    busRouteItem.arriveInfo.add(arriveItem);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                } else if (min.contains("-")) {

                } else {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min);
                    arriveItem.state = Constants.STATE_ING;
                    busRouteItem.arriveInfo.add(arriveItem);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                }


            }

        }


        return sItem;
    }


}
