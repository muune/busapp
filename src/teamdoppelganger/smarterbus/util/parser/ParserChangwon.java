package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import teamdoppelganger.smarterbus.util.common.StaticCommonFuction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

/**
 * @author DOPPELSOFT4
 */
public class ParserChangwon extends CommonParser {

    public final String mLineUrl = "http://mbus.changwon.go.kr/mobile/busArrStation.jsp";
    public final String mStopUrl = "http://mbus.changwon.go.kr/mobile/busLocation.jsp";


    public ParserChangwon(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_CHANG_WON._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_CHANG_WON._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_CHANG_WON._cityId);
                    stopItem.isExist = false;

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
        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();
        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, "routeId=" + sItem.tempId, "utf-8");


        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements elements = doc.select("tr");

            for (int i = 0; i < elements.size(); i++) {

                Elements tdElements = elements.get(i).select("td");

                if (tdElements.size() == 0) continue;

                if (tdElements.get(0).attr("onclick").contains("location.href")) {

                    BusStopItem stopItem = new BusStopItem();


                    String stopApiId = tdElements.get(0).attr("onclick").split("stationId=")[1].replace("'", "");
                    String stopName = tdElements.get(0).select("span").get(0).text();
                    String stopArsId = tdElements.get(0).select("span").get(1).text();


                    if (!tdElements.get(1).select("a").attr("onclick").contains("busInfoPop(''")) {

                        String plainNum = tdElements.get(1).select("a").attr("onclick").split("'")[1];

                        if (plainNum != null) {
                            if (plainNum.length() > 5) {
                                plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                            }

                            for (int j = 0; j < sItem.busStopItem.size(); j++) {

                                if (sItem.busStopItem.get(j).arsId.equals(stopArsId)) {
                                    sItem.busStopItem.get(j).isExist = true;
                                    sItem.busStopItem.get(j).plainNum = plainNum;
                                    break;
                                }

                            }

                        }

                    }


                }
            }
        }


        sItem.depthIndex = 0;


        // TODO Auto-generated method stub
        return sItem;
    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {
        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        String parseStr = RequestCommonFuction.getSource(mLineUrl, false, "stationId=" + sItem.apiId, "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            HashMap<String, String> tempSaveRealTimePair = new HashMap<String, String>();

            Elements elements = doc.select("table[class=box4]");


            for (int i = 0; i < elements.size(); i++) {
                Elements spanTags = elements.get(i).select("span");

                if (spanTags.size() > 0) {
                    String routeName = spanTags.get(0).text();
                    String busName1 = spanTags.get(1).text();
                    String arrmsg_main = spanTags.get(2).text();
                    String arrmsg_sub = spanTags.get(3).text();


                    String routeApiId = elements.get(i).select("a").attr("href").split("routeId=")[1];

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteApiId = routeApiId;
                    busRouteItem.busRouteName = routeName;

                    if (arrmsg_main.contains("약")) {
                        int min = Integer.parseInt(arrmsg_main.split("분")[0].replace("약", "").trim());
                        int remainStop = 0;
                        try {
                            remainStop = Integer.parseInt(arrmsg_sub.split("번")[0].trim());
                        } catch (Exception e) {
                        }
                        ;

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_ING;
                        arriveItem.remainMin = min;
                        arriveItem.remainStop = remainStop;
                        busRouteItem.arriveInfo.add(arriveItem);


                    } else if (arrmsg_main.contains("잠시")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_NEAR;
                        busRouteItem.arriveInfo.add(arriveItem);
                    }

                    busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_CHANG_WON._cityId);

                    localBusRouteItems.add(busRouteItem);
                }
            }
        }


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);


        return depthRouteItem;

    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        String parseStr = RequestCommonFuction.getSource(mLineUrl, false, "stationId=" + sItem.busStopApiId, "utf-8");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            HashMap<String, String> tempSaveRealTimePair = new HashMap<String, String>();

            Elements elements = doc.select("table[class=box4]");


            for (int i = 0; i < elements.size(); i++) {
                Elements spanTags = elements.get(i).select("span");

                if (spanTags.size() > 0) {
                    String routeName = spanTags.get(0).text();
                    String busName1 = spanTags.get(1).text();
                    String arrmsg_main = spanTags.get(2).text();
                    String arrmsg_sub = spanTags.get(3).text();


                    String routeApiId = elements.get(i).select("a").attr("href").split("routeId=")[1];

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteApiId = routeApiId;
                    busRouteItem.busRouteName = routeName;

                    if (arrmsg_main.contains("약")) {
                        int min = Integer.parseInt(arrmsg_main.split("분")[0].replace("약", "").trim());
                        int remainStop = 0;
                        try {
                            remainStop = Integer.parseInt(arrmsg_sub.split("번")[0].trim());
                        } catch (Exception e) {
                        }
                        ;

                        if (sItem.busRouteApiId.equals(routeApiId)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = min;
                            arriveItem.remainStop = remainStop;
                            arriveItem.state = Constants.STATE_ING;
                            busRouteItem.arriveInfo.add(arriveItem);

                            arriveItems.add(arriveItem);
                        }
                    } else if (arrmsg_main.contains("잠시")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_NEAR;
                        busRouteItem.arriveInfo.add(arriveItem);
                    }

                }
            }
        }

        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;
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
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        String parseStr = RequestCommonFuction.getSource(mLineUrl, false, "stationId=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "utf-8");
        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            HashMap<String, String> tempSaveRealTimePair = new HashMap<String, String>();

            Elements elements = doc.select("table[class=box4]");


            for (int i = 0; i < elements.size(); i++) {
                Elements spanTags = elements.get(i).select("span");

                if (spanTags.size() > 0) {
                    String routeName = spanTags.get(0).text();
                    String busName1 = spanTags.get(1).text();
                    String arrmsg_main = spanTags.get(2).text();
                    String arrmsg_sub = spanTags.get(3).text();


                    String routeApiId = elements.get(i).select("a").attr("href").split("routeId=")[1];

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteApiId = routeApiId;
                    busRouteItem.busRouteName = routeName;

                    if (arrmsg_main.contains("약")) {
                        int min = Integer.parseInt(arrmsg_main.split("분")[0].replace("약", "").trim());
                        int remainStop = 0;
                        try {
                            remainStop = Integer.parseInt(arrmsg_sub.split("번")[0].trim());
                        } catch (Exception e) {
                        }

                        if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeApiId)) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = min;
                            arriveItem.remainStop = remainStop;
                            arriveItem.state = Constants.STATE_ING;
                            busRouteItem.arriveInfo.add(arriveItem);


                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        } else if (arrmsg_main.contains("잠시")) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_NEAR;
                            busRouteItem.arriveInfo.add(arriveItem);
                        }
                    }

                }
            }
        }


        return sItem;
    }

}
