package teamdoppelganger.smarterbus.util.parser;

import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

public class ParserJangseng extends CommonParser {


    public ParserJangseng(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();
        String url = String
                .format("http://bis.jangseong.go.kr/app/mobile/bus_realtime/bus_location2.jsp?lID=%s&hdMode=NAME_L&txtKeyword=%s",
                        rItem.busRouteApiId, "11");
        String htmlResult = RequestCommonFuction.getSource(url, false, "",
                "utf-8");

        if (htmlResult != null) {
            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("div[id=tblLocationU]").select("tr");

            for (int j = 0; j < ddTag.size(); j++) {

                if (ddTag.get(j).select("td").size() < 2)
                    continue;
                String stopName = ddTag.get(j).select("td").get(0).attr("title");
                String stopArsId = ddTag
                        .get(j)
                        .select("td")
                        .get(0)
                        .select("span")
                        .text()
                        .substring(
                                1,
                                ddTag.get(j).select("td").get(0).select("span")
                                        .text().length() - 2);
                boolean isBus = false;
                if (ddTag.get(j).select("td").get(1).select("img").attr("src")
                        .contains("mb06")) {
                    isBus = true;
                }

                BusStopItem stopItem = new BusStopItem();
                stopItem.name = stopName;
                stopItem.isExist = isBus;
                stopItem.arsId = stopArsId;
                stopItem.apiId = stopArsId;
                stopItem.localInfoId = String.valueOf(CommonConstants.CITY_JANGSEONG._cityId);

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

        String param = String.format("searchBusStopName=&searchType=search&searchLeft=&searchRight=&searchTop=&searchBottom=&searchBusStopId=%s&txtStationName=", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource("http://bis.jangseong.go.kr/gjmap/realTimeBusInfo.do?action=stationArriveInfo", true, param, "utf-8");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements elements = doc.select("li");


            for (int i = 0; i < elements.size(); i++) {
                try {

                    int lastindex1 = elements.get(i).text().lastIndexOf("(");
                    int lastindex2 = elements.get(i).text().lastIndexOf(")");

                    String num = elements.get(i).text().split("번")[0];
                    String where = elements.get(i).text().substring(lastindex1, elements.get(i).text().length() - 1).split("\\(")[1].split(" ")[0];
                    String min = elements.get(i).text().substring(lastindex2, elements.get(i).text().length() - 1).split("\\)")[1].split("분")[0].split(" ")[2];

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = num;
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(min);
                    arriveItem.remainStop = Integer.parseInt(where);


                    routeItem.arriveInfo.add(arriveItem);

                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_JANGSEONG._cityId);
                    localBusRouteItems.add(routeItem);
                    

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;


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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        // TODO Auto-generated method stub
        return null;
    }

}
