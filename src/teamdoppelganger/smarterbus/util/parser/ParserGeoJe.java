package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;

import android.database.sqlite.SQLiteDatabase;

import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserGeoJe extends CommonParser {

    public ParserGeoJe(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String param = String.format("searchLineId=%s&searchRoute=%s&searchType=locatoin", rItem.busRouteApiId, rItem.busRouteName);
        String parseStr = RequestCommonFuction.getSource("http://bis.geoje.go.kr/mobile/mobileMain.do?action=searchLineResult", true, param, "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements elements = doc.select("p[class=area6]");

            for (int i = 1; i < elements.size(); i++) {

                Elements spanTag = elements.get(i).select("span");

                String stopName = spanTag.get(0).text().split("\\[")[0];
                String stopArsId = spanTag.get(0).text().split("\\[")[1].replace("]", "");
                boolean isBus = false;
                String busName = null;
                if (spanTag.size() > 1) {
                    busName = spanTag.get(1).text();
                    isBus = true;
                }


                BusStopItem busStopItem = new BusStopItem();
                busStopItem.arsId = stopArsId;
                busStopItem.name = stopName;
                busStopItem.isExist = isBus;
                busStopItem.localInfoId = String.valueOf(CommonConstants.CITY_GEO_JE._cityId);

                localBusStopItems.add(busStopItem);

            }
        }
        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(localBusStopItems);


        return depthStopItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {
        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String param = String.format("searchBusStopName=&searchType=search&searchLeft=&searchRight=&searchTop=&searchBottom=&searchBusStopId=%s&txtStationName=", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource("http://bis.geoje.go.kr/map/realTimeBusInfo.do?action=stationArriveInfo", true, param, "utf-8");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements elements = doc.select("li");

            for (int i = 0; i < elements.size(); i++) {

                try {

                    int lastindex1 = elements.get(i).select("div").text().lastIndexOf("(");
                    int lastindex2 = elements.get(i).select("div").text().lastIndexOf(")");

                    String num = elements.get(i).select("div").text().split("번")[0];
                    String where = elements.get(i).select("div").text().substring(lastindex1, elements.get(i).select("div").text().length() - 1).split("\\(")[1].split(" ")[0];
                    String min = elements.get(i).select("div").text().substring(lastindex2, elements.get(i).select("div").text().length() - 1).split("\\)")[1].split("분")[0].split(" ")[2];

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = num;
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min);
                    arriveItem.remainStop = Integer.parseInt(where);

                    routeItem.arriveInfo.add(arriveItem);
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GEO_JE._cityId);
                    localBusRouteItems.add(routeItem);

                } catch (Exception e) {
                    e.printStackTrace();
                }

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

}
