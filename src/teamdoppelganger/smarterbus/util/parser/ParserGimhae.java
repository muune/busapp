package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

public class ParserGimhae extends CommonParser {

    public ParserGimhae(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GIM_HEA._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GIM_HEA._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GIM_HEA._cityId);


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
        String htmlResult = RequestCommonFuction.getSource("http://bus.gimhae.go.kr/ver4/map/inc/inc_result_bus_location.php?txtKeyword=" + sItem.tempId, false, "", "euc-kr");

        if (htmlResult != null) {

            Document doc = Jsoup.parse(htmlResult);
            Elements ddTag = doc.select("tbody").select("tr");

            for (int j = 0; j < ddTag.size(); j++) {

                Elements tdTag = ddTag.get(j).select("td");

                if (tdTag.size() == 0) continue;

                String arsId = null;
                String name = null;
                String apiId = null;
                boolean isBusExist = false;

                if (tdTag.size() == 2) {
                    if (!tdTag.get(0).text().contains("[")) continue;

                    arsId = tdTag.get(1).select("a").attr("onclick").split("fnBusstopFocus_naver\\(")[1].replace("')", "").replaceAll("'", "").split(",")[0];
                    arsId = String.valueOf((Integer.parseInt(arsId) + 1000));

                } else if (tdTag.size() == 3) {

                    if (!tdTag.get(0).text().contains("[")) continue;

                    arsId = tdTag.get(2).select("a").attr("onclick").split("fnBusstopFocus_naver\\(")[1].replace("')", "").replaceAll("'", "").split(",")[0];

                    arsId = String.valueOf((Integer.parseInt(arsId) + 1000));

                    for (int k = 0; k < sItem.busStopItem.size(); k++) {

                        if (sItem.busStopItem.get(k).arsId.equals(arsId)) {
                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = tdTag.get(1).select("a").attr("onclick").split("fnBusstopFocus_naver\\(")[1].replace("')", "").replaceAll("'", "").split(",")[0].substring(2);

                            break;
                        }

                    }


                    isBusExist = true;
                }


            }
        }


        sItem.depthIndex = 0;


        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        if (sItem.arsId.equals("-")) {

            DepthRouteItem depthRouteItem = new DepthRouteItem();
            depthRouteItem.busRouteItem.addAll(localBusRouteItems);

            return depthRouteItem;

        }

        String url = String.format("http://bus.gimhae.go.kr/ver4/map/ajax_get_data.php?mode=BUS_ARRIVAL&lobs=&menu=3&smenu=1&keyword=%s", Integer.parseInt(sItem.arsId) - 1000);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("data");

            for (int j = 0; j < ddTag.size(); j++) {


                String busName = ddTag.get(j).attr("d1");
                String min = ddTag.get(j).attr("d4");
                String curStation = ddTag.get(j).attr("d3");

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = busName;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GIM_HEA._cityId);

                routeItem.busStopArsId = sItem.arsId;
                routeItem.busStopName = sItem.name;

                if (min.contains("약")) {
                    min = min.replace("약", "").replace("분", "").trim();
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.state = Constants.STATE_ING;
                    routeItem.arriveInfo.add(arrive);
                } else if (min.contains("곧도착")) {
                    min = "0";
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
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

        String url = String.format("http://bus.gimhae.go.kr/ver4/map/ajax_get_data.php?mode=BUS_ARRIVAL&lobs=&menu=3&smenu=1&keyword=%s", Integer.parseInt(sItem.busStopArsId) - 1000);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");


        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("data");


            for (int j = 0; j < ddTag.size(); j++) {


                String busName = ddTag.get(j).attr("d1");
                String min = ddTag.get(j).attr("d4");
                String curStation = ddTag.get(j).attr("d3");


                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = busName;


                if (min.contains("약")) {

                    if (busName.equals(sItem.busRouteName)) {
                        min = min.replace("약", "").replace("분", "").trim();
                        ArriveItem arrive = new ArriveItem();
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.state = Constants.STATE_ING;
                        routeItem.arriveInfo.add(arrive);
                        arriveItems.add(arrive);
                    }
                } else if (min.contains("곧도착")) {
                    if (busName.equals(sItem.busRouteName)) {
                        min = "0";
                        ArriveItem arrive = new ArriveItem();
                        arrive.remainMin = Integer.parseInt(min);
                        arrive.state = Constants.STATE_NEAR;
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


        String url = String.format("http://bus.gimhae.go.kr/ver4/map/ajax_get_data.php?mode=BUS_ARRIVAL&lobs=&menu=3&smenu=1&keyword=%s", Integer.parseInt(favoriteAndHistoryItem.busRouteItem.busStopArsId) - 1000);
        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("data");


            for (int j = 0; j < ddTag.size(); j++) {


                String busName = ddTag.get(j).attr("d1");
                String min = ddTag.get(j).attr("d4");
                String curStation = ddTag.get(j).attr("d3");

                BusRouteItem routeItem = new BusRouteItem();
                routeItem.busRouteName = busName;
                routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GIM_HEA._cityId);


                if (min.contains("약") && busName.equals(favoriteAndHistoryItem.busRouteItem.busRouteName)) {
                    min = min.replace("약", "").replace("분", "").trim();
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.state = Constants.STATE_ING;
                    routeItem.arriveInfo.add(arrive);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arrive);
                } else if (min.contains("곧도착") && busName.equals(favoriteAndHistoryItem.busRouteItem.busRouteName)) {
                    min = "0";
                    ArriveItem arrive = new ArriveItem();
                    arrive.remainMin = Integer.parseInt(min);
                    arrive.state = Constants.STATE_NEAR;
                    routeItem.arriveInfo.add(arrive);
                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arrive);
                }

            }
        }


        return sItem;
    }

}
