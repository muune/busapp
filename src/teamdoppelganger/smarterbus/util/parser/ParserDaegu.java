package teamdoppelganger.smarterbus.util.parser;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
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
import teamdoppelganger.smarterbus.util.common.StaticCommonFuction;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smart.lib.CommonConstants;

public class ParserDaegu extends CommonParser {

    String mLineUrl = "http://businfo.daegu.go.kr/ba/route/rtbspos.do";
    String mStopUrl = "http://businfo.daegu.go.kr/ba/route/rtbsarr.do";  //"http://m.businfo.go.kr/bp/m/realTime.do";

    String mLineDetailUrl = "http://businfo.daegu.go.kr/ba/route/rtbsarr.do";

    public ParserDaegu(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_DAE_GU._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_DAE_GU._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_GU._cityId);


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

        String selectBusStopQry = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.CITY_DAE_GU._engName + "_ROUTE",
                CommonConstants.BUS_ROUTE_ID1, sItem.tempId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRouteName = "";
        if (cursor.moveToNext()) {
            busRouteName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
        }
        cursor.close();


        getStopInforOfDirection(sItem.busStopItem, sItem.tempId, busRouteName); //정방향


        sItem.depthIndex = 0;

        return sItem;
    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();
        ArrayList<String> tempApiIdList = new ArrayList<String>();


        String param;


        String url = String.format("http://m.businfo.go.kr/bp/m/realTime.do?act=arrInfoRouteList&bsId=%s&bsNm=", sItem.apiId);


        String parseStr = RequestCommonFuction.getSource(url, false, "", "euc-kr");
        if (parseStr != null) {
            Document document = Jsoup.parse(parseStr);
            Elements tdTags = document.select("li[class=nx]");
            for (int i = 0; i < tdTags.size(); i++) {
                if (tdTags.get(i).select("a").size() > 0
                        && tdTags.get(i).select("a").attr("href").contains("realTime")) {
                    Elements aTags = tdTags.get(i).select("a");
                    String[] routeInfo = aTags.attr("href").split("bsId=")[1].replaceAll("'", "").split("&");

                    String stopApiId = routeInfo[0];

                    String routeApiId = "";
                    if (routeInfo[2].split("roId=").length > 1) {
                        routeApiId = routeInfo[2].split("roId=")[1];
                    }

                    String routeName = routeInfo[3].split("roNo=")[1];
                    String direction = routeInfo[4].split("=")[1];


                    String subName = "";

                    if (!aTags.text().contains("전체")) {
                        subName = aTags.text().replace("\\(", "").replace(")", "").trim().replace("->", "-");
                    }

                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteName = routeName.trim();
                    busRouteItem.busRouteApiId = routeApiId.trim();
                    busRouteItem.busStopApiId = stopApiId.trim();
                    busRouteItem.direction = direction.trim();
                    busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_GU._cityId);
                    busRouteItem.plusParsingNeed = 1;

                    localBusRouteItems.add(busRouteItem);
                }
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
            BusRouteItem busRouteItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String param;
        param = String.format("act=findByArr&bsId=%s&bsNm=%s&routeId=%s&routeNo=%s&moveDir=%s&winc_id=%s", busRouteItem.busStopApiId,
                URLEncoder.encode(busRouteItem.busStopName), busRouteItem.busRouteApiId, URLEncoder.encode(busRouteItem.busRouteName), busRouteItem.direction, busRouteItem.busStopArsId);


        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, param, "euc-kr");

        if (parseStr != null) {
            Document document = Jsoup.parse(parseStr);
            Elements tBodys = document.select("tbody");

            for (int j = 0; j < tBodys.size(); j++) {
                Elements bodyRow = tBodys.get(j).select("tr[class=body_row2]");


                if (bodyRow.size() > 0) {

                    String arrMsg = "";
                    if (bodyRow.size() == 4) {

                        try {
                            int stop = Integer.parseInt(bodyRow.get(2).select("td[class=body_col4]").get(0).text().split(" ")[0]);
                            int min = Integer.parseInt(bodyRow.get(3).select("td[class=body_col4]").get(0).text().split(" ")[0]);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = min;
                            arriveItem.remainStop = stop;
                            arriveItem.state = Constants.STATE_ING;

                            busRouteItem.arriveInfo.add(arriveItem);
                        } catch (Exception e) {
                        }
                        ;

                    } else if (bodyRow.size() == 5) {

                        try {
                            int stop = Integer.parseInt(bodyRow.get(3).select("td[class=body_col4]").get(0).text().split(" ")[0]);
                            int min = Integer.parseInt(bodyRow.get(4).select("td[class=body_col4]").get(0).text().split(" ")[0]);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = min;
                            arriveItem.remainStop = stop;
                            arriveItem.state = Constants.STATE_ING;

                            busRouteItem.arriveInfo.add(arriveItem);
                        } catch (Exception e) {
                        }
                        ;

                    }

                } else {
                }

                busRouteItem.plusParsingNeed = 0;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_GU._cityId);

            }

            localBusRouteItems.add(busRouteItem);

        }
        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;
    }

    @Override
    public DepthStopItem getStopDetailList(
            BusStopItem busStopItems) {
        // TODO Auto-generated method stub
        return null;
    }


    public void getStopInforOfDirection(ArrayList<BusStopItem> localBusStopItems, String busApiId, String busRouteName) {

        String param = String.format("act=findByPos&routeId=%s", busApiId); //정방향  1, 역방향 0
        String parseStr = RequestCommonFuction.getSource(mLineUrl, false, param, "euc-kr");

        if (parseStr != null) {
            Document document = Jsoup.parse(parseStr);
            Elements divTags = document.select("div[id=posForwardPanel]");
            if (divTags.size() > 0) {
                Elements tbodyTags = divTags.get(0).select("tbody").select("tr");

                String tempId = "-1";
                for (int i = 0; i < tbodyTags.size(); i++) {

                    Elements tdTags = tbodyTags.get(i).select("td");
                    if (tdTags.size() > 0) {

                        if (tdTags.get(0).select("div[class=rtbs_arrival]").size() > 0) {

                            for (int k = 0; k < localBusStopItems.size(); k++) {

                                if (tempId.equals(localBusStopItems.get(k).arsId)) {
                                    localBusStopItems.get(k).isExist = true;
                                    localBusStopItems.get(k).plainNum = tdTags.get(0).select("div[class=rtbs_depart]").text();
                                    break;
                                }

                            }

                        } else {
                            if (tdTags.get(0).attr("title").contains("클릭하시면")) {
                                int size = tdTags.get(0).text().trim().split("\\(").length - 1;
                                String name = tdTags.get(0).text().trim().split("\\(")[0];
                                String num = tdTags.get(0).text().trim().split("\\(")[size].replace(")", "");


                                tempId = num;  //String.valueOf(Integer.parseInt(num));

                            }

                        }

                    }
                }
            }

            divTags = document.select("div[id=posBackwardPanel]");
            if (divTags.size() > 0) {
                Elements tbodyTags = divTags.get(0).select("tbody").select("tr");

                String tempId = "-1";
                for (int i = 0; i < tbodyTags.size(); i++) {

                    Elements tdTags = tbodyTags.get(i).select("td");
                    if (tdTags.size() > 0) {

                        if (tbodyTags.get(i).select("div[class=rtbs_arrival]").size() > 0) {

                            int tempIndex = -1;
                            String tempPlainNum = "";
                            for (int k = 0; k < localBusStopItems.size(); k++) {

                                if (tempId.equals(localBusStopItems.get(k).arsId)) {
                                    tempIndex = k;
                                    tempPlainNum = tdTags.get(0).select("div[class=rtbs_depart]").text();
                                }
                            }

                            if (tempIndex != -1) {
                                localBusStopItems.get(tempIndex).isExist = true;
                                localBusStopItems.get(tempIndex).plainNum = tempPlainNum;
                            }


                        } else {
                            if (tdTags.get(0).attr("title").contains("클릭하시면")) {

                                int size = tdTags.get(0).text().trim().split("\\(").length - 1;
                                String name = tdTags.get(0).text().trim().split("\\(")[0];
                                String num = tdTags.get(0).text().trim().split("\\(")[size].replace(")", "");

                                tempId = num;

                            }

                        }

                    }
                }

            }
        }


    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String param;
        param = String.format("act=findByArr&bsId=%s&bsNm=%s&routeId=%s&routeNo=%s&moveDir=%s&winc_id=%s", sItem.busStopApiId,
                sItem.busStopName, sItem.busRouteApiId, sItem.busRouteName, sItem.direction, sItem.busStopArsId);

        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, param, "euc-kr");

        if (parseStr != null) {
            Document document = Jsoup.parse(parseStr);
            Elements tBodys = document.select("tbody");

            for (int j = 0; j < tBodys.size(); j++) {

                Elements bodyRow = tBodys.get(j).select("tr[class=body_row2]");

                if (bodyRow.size() > 0) {

                    String arrMsg = "";


                    if (bodyRow.size() == 4) {

                        try {
                            int stop = Integer.parseInt(bodyRow.get(2).select("td[class=body_col4]").get(0).text().split(" ")[0]);
                            int min = Integer.parseInt(bodyRow.get(3).select("td[class=body_col4]").get(0).text().split(" ")[0]);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = min;
                            arriveItem.remainStop = stop;
                            arriveItem.state = Constants.STATE_ING;

                            arriveItems.add(arriveItem);
                        } catch (Exception e) {
                        }
                        ;

                    } else if (bodyRow.size() == 5) {

                        try {
                            int stop = Integer.parseInt(bodyRow.get(3).select("td[class=body_col4]").get(0).text().split(" ")[0]);
                            int min = Integer.parseInt(bodyRow.get(4).select("td[class=body_col4]").get(0).text().split(" ")[0]);

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = min;
                            arriveItem.remainStop = stop;
                            arriveItem.state = Constants.STATE_ING;

                            arriveItems.add(arriveItem);
                        } catch (Exception e) {
                        }
                        ;

                    }


                } else {

                }

            }
        }

        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;
    }


    //direction 때문에 추가적으로 파싱을 한번 더 할 수 밖에 없다.
    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);


        String param;
        param = String.format("act=findByPath&bsId=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId);

        String parseStr = RequestCommonFuction.getSource(mStopUrl, false, param, "euc-kr");

        if (parseStr != null) {
            Document document = Jsoup.parse(parseStr);
            Elements tdTags = document.select("td[class=body_col2]");

            for (int i = 0; i < tdTags.size(); i++) {
                Element tdTag = tdTags.get(i);


                String[] routeInfo = tdTag.attr("onclick").split("viewArrival\\(")[1].replaceAll("'", "").split(",");

                String stopApiId = routeInfo[0];
                String routeApiId = routeInfo[2];
                String stopName = routeInfo[1];
                String direction = routeInfo[4];
                String routeName = routeInfo[3];
                String stopArsId = routeInfo[10];


                if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeApiId.trim())) {
                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteName = routeName.trim();
                    busRouteItem.busRouteApiId = routeApiId.trim();
                    busRouteItem.busStopApiId = stopApiId.trim();
                    busRouteItem.busStopName = stopName.trim();
                    busRouteItem.busStopArsId = stopArsId.trim();
                    busRouteItem.direction = direction.trim();
                    busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_GU._cityId);

                    param = String.format("act=findByArr&bsId=%s&bsNm=%s&routeId=%s&routeNo=%s&moveDir=%s&winc_id=%s", busRouteItem.busStopApiId,
                            busRouteItem.busStopName, busRouteItem.busRouteApiId, busRouteItem.busRouteName, busRouteItem.direction, busRouteItem.busStopArsId);


                    parseStr = RequestCommonFuction.getSource(mStopUrl, false, param, "euc-kr");

                    if (parseStr != null) {
                        document = Jsoup.parse(parseStr);
                        Elements tBodys = document.select("tbody");

                        for (int j = 0; j < tBodys.size(); j++) {
                            Elements bodyRow = tBodys.get(j).select("tr[class=body_row2]");

                            if (bodyRow.size() > 0) {

                                String arrMsg = "";


                                if (bodyRow.size() == 4) {

                                    try {
                                        int stop = Integer.parseInt(bodyRow.get(2).select("td[class=body_col4]").get(0).text().split(" ")[0]);
                                        int min = Integer.parseInt(bodyRow.get(3).select("td[class=body_col4]").get(0).text().split(" ")[0]);

                                        ArriveItem arriveItem = new ArriveItem();
                                        arriveItem.remainMin = min;
                                        arriveItem.remainStop = stop;
                                        arriveItem.state = Constants.STATE_ING;

                                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                                    } catch (Exception e) {
                                    }

                                } else if (bodyRow.size() == 5) {

                                    try {
                                        int stop = Integer.parseInt(bodyRow.get(3).select("td[class=body_col4]").get(0).text().split(" ")[0]);
                                        int min = Integer.parseInt(bodyRow.get(4).select("td[class=body_col4]").get(0).text().split(" ")[0]);

                                        ArriveItem arriveItem = new ArriveItem();
                                        arriveItem.remainMin = min;
                                        arriveItem.remainStop = stop;
                                        arriveItem.state = Constants.STATE_ING;

                                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                                    } catch (Exception e) {
                                    }

                                }

                            } else {

                            }
                        }

                        break;
                    }
                }
            }
        }


        return sItem;
    }


}
