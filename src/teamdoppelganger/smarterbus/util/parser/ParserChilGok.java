package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;

import android.database.sqlite.SQLiteDatabase;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthAlarmItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserChilGok extends CommonParser {

    public ParserChilGok(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();

        String url = String.format("http://bus.chilgok.go.kr/GCBIS/web/map03/getBrtBusPosList.do?brtId=%s&brtDirection=%s&brtClass=%s&_=1389946891967", rItem.busRouteName, rItem.busRouteApiId, rItem.busRouteApiId2);
        String[] refrer = {"Referer", "http://bus.chilgok.go.kr/GCBIS/web/map03/main.do", "x-requested-with", "XMLHttpRequest"};
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8", refrer);

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("tbody").select("tr");
            for (int i = 0; i < seqElements.size(); i++) {

                if (seqElements.get(i).select("span").size() > 0) {

                    busStopItem.get(busStopItem.size() - 1).isExist = true;

                } else {
                    if (seqElements.get(i).select("td").size() > 1) {

                        String name = seqElements.get(i).select("td").get(0).text();
                        String arsId = seqElements.get(i).select("td").get(1).text();

                        BusStopItem item = new BusStopItem();
                        item.name = name;
                        item.arsId = arsId;
                        item.localInfoId = String.valueOf(CommonConstants.CITY_CHIL_GOK._cityId);
                        busStopItem.add(item);
                    }
                }
            }
        }


        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(busStopItem);

        return depthStopItem;

    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String tmpId;

        tmpId = sItem.arsId.substring(1);

        String url = String.format("http://bus.chilgok.go.kr/GCBIS/web/map04/srchBusArr.do?stopId=%s&_=1390181188326", tmpId);
        String[] refrer = {"Referer", "http://bus.chilgok.go.kr/GCBIS/web/map04/main.do", "x-requested-with", "XMLHttpRequest"};
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8", refrer);

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("tbody").select("tr");
            for (int i = 0; i < seqElements.size(); i++) {

                if (seqElements.get(i).select("a").size() == 3) {
                    String busNum = seqElements.get(i).select("a").get(0).text();
                    String subName = seqElements.get(i).select("a").get(1).text();
                    String when = seqElements.get(i).select("a").get(2).text();

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteSubName = subName;


                    if (when.contains("분후")) {
                        when = when.replace("분후", "").trim();
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(when);
                        routeItem.arriveInfo.add(arriveItem);
                    }

                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHIL_GOK._cityId);
                    localBusRouteItem.add(routeItem);
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
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String tmpId;
        tmpId = sItem.busStopArsId.substring(1);

        String url = String.format("http://bus.chilgok.go.kr/GCBIS/web/map04/srchBusArr.do?stopId=%s&_=1390181188326", tmpId);
        String[] refrer = {"Referer", "http://bus.chilgok.go.kr/GCBIS/web/map04/main.do", "x-requested-with", "XMLHttpRequest"};
        String parseStr = RequestCommonFuction.getSource(url, false, "", "utf-8", refrer);

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements seqElements = doc.select("tbody").select("tr");
            for (int i = 0; i < seqElements.size(); i++) {

                if (seqElements.get(i).select("a").size() == 3) {
                    String busNum = seqElements.get(i).select("a").get(0).text();
                    String subName = seqElements.get(i).select("a").get(1).text();
                    String when = seqElements.get(i).select("a").get(2).text();

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = busNum;
                    routeItem.busRouteSubName = subName;

                    if (when.contains("분후") && sItem.busRouteName.equals(busNum) && sItem.busRouteSubName.equals(subName)) {
                        when = when.replace("분후", "").trim();
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(when);
                        routeItem.arriveInfo.add(arriveItem);

                        arriveItems.add(arriveItem);
                    }

                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_CHIL_GOK._cityId);

                }
            }
        }


        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;
    }


}
