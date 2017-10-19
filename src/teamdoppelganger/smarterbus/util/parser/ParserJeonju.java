package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
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
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;


public class ParserJeonju extends CommonParser {


    public final String mLineUrl = "http://openapi.jeonju.go.kr/jeonjubus/openApi/traffic/bus_location_busstop_list_common.do";
    public final String mStopUrl = "http://openapi.jeonju.go.kr/jeonjubus/openApi/traffic/bus_location_low_busstop_infomation_common.do";
    public final String mKey = "ZWwoooXb0G4VE24Qfm2%2BciiAalL%2FUvFVKG7kqdaepp58mgRFrcdjPFUniNP2RLJvGmBD0KpPMDyqxeMcSwoviQ%3D%3D";


    public ParserJeonju(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String parseStr = RequestCommonFuction.getSource(mLineUrl, false, "ServiceKey=" + mKey + "&brtStdid=" + rItem.busRouteApiId + "&lKey=", "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);

            Elements elements = doc.select("list[class=object]");
            for (int i = 0; i < elements.size(); i++) {

                String busStopApiId = elements.get(i).select("bnodeId").text();
                String busStopArsId = elements.get(i).select("stopId").text();
                String busStopName = elements.get(i).select("stopKname").text();
                String busNum = elements.get(i).select("busNo").text();

                BusStopItem busStopItem = new BusStopItem();
                busStopItem.name = busStopName;
                busStopItem.arsId = busStopArsId;
                busStopItem.apiId = busStopApiId;
                busStopItem.localInfoId = String.valueOf(CommonConstants.CITY_JEON_JU._cityId);


                if (busNum != null && busNum.trim().length() > 0) {
                    busStopItem.isExist = true;
                    busStopItem.plainNum = "전북70자 " + busNum;
                }

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

        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, "ServiceKey=" + mKey + "&stopStdid=" + sItem.apiId, "utf-8");


        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            Elements elements = doc.select("list[class=object]");
            for (int i = 0; i < elements.size(); i++) {

                String busApiId = elements.get(i).select("brtStdid").text();
                String bidNo = elements.get(i).select("bidNo").text(); //버스 번호
                String brtId = elements.get(i).select("brtId").text(); //노선번호
                String rStop = elements.get(i).select("rStop").text(); //남은 정류장
                String rTime = elements.get(i).select("rTime").text(); //도착예정시간 (초단위)
                String brtViaName = elements.get(i).select("brtVianame").text(); //방면
                String brtDirection = elements.get(i).select("brtVianame").text(); //노선방향

                BusRouteItem busRouteItem = new BusRouteItem();
                busRouteItem.busRouteName = bidNo;
                busRouteItem.busRouteApiId = busApiId;
                busRouteItem.nextDirctionName = brtViaName;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_JEON_JU._cityId);

                if (rTime != null && Integer.parseInt(rTime) > 0) {
                    int remainMin = Integer.parseInt(rTime) / 60;

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = remainMin;
                    arriveItem.remainStop = Integer.parseInt(rStop);

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
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, "ServiceKey=" + mKey + "&stopStdid=" + sItem.busStopApiId, "utf-8");

        if (parseStr != null) {


            Document doc = Jsoup.parse(parseStr);

            Elements elements = doc.select("list[class=object]");
            for (int i = 0; i < elements.size(); i++) {

                String busApiId = elements.get(i).select("brtStdid").text();
                String bidNo = elements.get(i).select("bidNo").text(); //버스 번호
                String brtId = elements.get(i).select("brtId").text(); //노선번호
                String rStop = elements.get(i).select("rStop").text(); //남은 정류장
                String rTime = elements.get(i).select("rTime").text(); //도착예정시간 (초단위)
                String brtViaName = elements.get(i).select("brtVianame").text(); //방면
                String brtDirection = elements.get(i).select("brtVianame").text(); //노선방향

                BusRouteItem busRouteItem = new BusRouteItem();
                busRouteItem.busRouteName = bidNo;
                busRouteItem.busRouteApiId = busApiId;
                busRouteItem.nextDirctionName = brtViaName;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_JEON_JU._cityId);

                if (rTime != null && rTime.length() > 0) {
                    if (sItem.busRouteApiId.equals(busApiId)) {
                        int remainMin = Integer.parseInt(rTime) / 60;

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = Integer.parseInt(rStop);


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


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, "ServiceKey=" + mKey + "&stopStdid=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "utf-8");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            Elements elements = doc.select("list[class=object]");
            for (int i = 0; i < elements.size(); i++) {

                String busApiId = elements.get(i).select("brtStdid").text();
                String bidNo = elements.get(i).select("bidNo").text(); //버스 번호
                String brtId = elements.get(i).select("brtId").text(); //노선번호
                String rStop = elements.get(i).select("rStop").text(); //남은 정류장
                String rTime = elements.get(i).select("rTime").text(); //도착예정시간 (초단위)
                String brtViaName = elements.get(i).select("brtVianame").text(); //방면
                String brtDirection = elements.get(i).select("brtVianame").text(); //노선방향

                BusRouteItem busRouteItem = new BusRouteItem();
                busRouteItem.busRouteName = bidNo;
                busRouteItem.busRouteApiId = busApiId;
                busRouteItem.nextDirctionName = brtViaName;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_JEON_JU._cityId);

                if (rTime != null && rTime.length() > 0) {

                    if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(busApiId)) {
                        int remainMin = Integer.parseInt(rTime) / 60;

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = remainMin;
                        arriveItem.remainStop = Integer.parseInt(rStop);


                        busRouteItem.arriveInfo.add(arriveItem);

                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }

                }

            }

        }

        return sItem;
    }

}

