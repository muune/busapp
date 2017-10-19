package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo.DetailedState;

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


public class ParserDaejeon extends CommonParser {

    public final String mLineUrl = "http://traffic.daejeon.go.kr/dwr/call/plaincall/dwrBusInfoService.selectUpRouteRunInfo.dwr";
    public final String mStopUrl = "http://traffic.daejeon.go.kr/dwr/call/plaincall/dwrBusInfoService.selectStationViewDao.dwr";

    public ParserDaejeon(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_DAE_JEON._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_DAE_JEON._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_JEON._cityId);

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

        stopListUpDown(true, sItem.tempId, localBusStopItems, sItem);
        stopListUpDown(false, sItem.tempId, localBusStopItems, sItem);


        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(localBusStopItems);

        sItem.depthIndex = 0;

        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String param = String.format("callCount=1&page=/map/busInfo/searchStation.do&httpSessionId=00000000.TATMS_Web2_servlet_engine1" +
                "&scriptSessionId=00000000&c0-scriptName=dwrBusInfoService&c0-methodName=selectStationDetailView&c0-id=0&c0-param0=string:%s" +
                "&batchId=3", sItem.apiId);
        String parseStr = RequestCommonFuction.getSource(mStopUrl, true, param, "utf-8");

        if (parseStr != null) {

            String[] tempInfor = parseStr.split("\\['ROUTE_TP']=");

            for (int i = 1; i < tempInfor.length; i++) {
                String[] detainInfor = tempInfor[i].split(";");

                String tempArrMsg = detainInfor[2].split("=")[1].replace("\"", "").trim().toString();
                String tempDst = detainInfor[3].split("=")[1].replace("\"", "").trim().toString();
                String arrmsg1 = StaticCommonFuction.unicodeToKo(tempArrMsg);
                String dst = StaticCommonFuction.unicodeToKo(tempDst);
                String routeName = detainInfor[4].split("=")[1].replace("\"", "").trim();


                BusRouteItem busRouteItem = new BusRouteItem();

                if (arrmsg1.contains("진입")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.state = Constants.STATE_NEAR;
                    busRouteItem.arriveInfo.add(arriveItem);
                } else if (arrmsg1.contains("분")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.state = Constants.STATE_ING;
                    arriveItem.remainMin = Integer.parseInt(arrmsg1.split("분")[0].trim());
                    busRouteItem.arriveInfo.add(arriveItem);
                } else if (arrmsg1.contains("출발예정")) {
                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.state = Constants.STATE_PREPARE_START;
                    arriveItem.elseStr = arrmsg1.split(" ")[0].trim();
                    busRouteItem.arriveInfo.add(arriveItem);
                }

                busRouteItem.busRouteName = routeName;
                busRouteItem.nextDirctionName = dst;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_JEON._cityId);

                String qry = String.format("SELECT *FROM %s where %s='%s'", CommonConstants.CITY_DAE_JEON._engName + "_Route",
                        CommonConstants.BUS_ROUTE_NAME, routeName);

                Cursor cursor = getBusDb().rawQuery(qry, null);
                if (cursor.moveToNext()) {
                    String routeApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    busRouteItem.busRouteApiId = routeApiId;
                }

                cursor.close();

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

    /**
     * 다운 리스트는 출발지에 종착지지점이 이미 명시되어있기때문에
     * startIndex를 2번째부터 시작함
     *
     * @param isUp
     * @param busApiId
     * @param localBusStopItems
     */
    private void stopListUpDown(boolean isUp, String busApiId, ArrayList<BusStopItem> localBusStopItems, DepthStopItem sItem) {

        String param;

        if (isUp) {
            param = String.format("callCount=1&page=/map/busInfo/searchRoute.do&httpSessionId=00000000.TATMS_Web1_servlet_engine" +
                    "&scriptSessionId=00000000&c0-scriptName=dwrBusInfoService&c0-methodName=selectUpRouteRunInfo&c0-id=0&c0-param0=string:%s" +
                    "&batchId=11", busApiId);
        } else {
            param = String.format("callCount=1&page=/map/busInfo/searchRoute.do&httpSessionId=00000000.TATMS_Web1_servlet_engine" +
                    "&scriptSessionId=00000000&c0-scriptName=dwrBusInfoService&c0-methodName=selectDownRouteRunInfo&c0-id=0&c0-param0=string:%s" +
                    "&batchId=11", busApiId);
        }

        String parseStr = RequestCommonFuction.getSource(mLineUrl, true, param, "utf-8");

        if (parseStr != null) {
            String[] tempInfor = parseStr.split("\\['BUSSTOP_SEQ']=");

            int startIndex = 1;

            if (!isUp) {
                startIndex = 2;
            }


            for (int i = 1; i < tempInfor.length; i++) {

                String[] detainInfor = tempInfor[i].split(";");
                String tempStopArsId, tempStopApiId, tempStopName, stopName, tempBusNo;

                BusStopItem stopItem = new BusStopItem();

                if (detainInfor.length == 18) {

                    tempBusNo = detainInfor[9].split("=")[1].replace("\"", "").trim().toString();
                    tempStopArsId = detainInfor[10].split("=")[1].replace("\"", "").trim().toString();
                    tempStopApiId = detainInfor[11].split("=")[1].replace("\"", "").trim().toString();
                    tempStopName = detainInfor[14].split("=")[1].replace("\"", "").toString();

                    for (int k = 0; k < sItem.busStopItem.size(); k++) {
                        if (sItem.busStopItem.get(k).apiId.equals(tempStopApiId.trim())) {
                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = "대전75 " + tempBusNo;
                            break;
                        }
                    }

                    stopItem.isExist = true;
                    stopItem.plainNum = "대전75 " + tempBusNo;

                } else {

                    if (detainInfor[11].contains("_TP")) continue;

                    tempStopArsId = detainInfor[7].split("=")[1].replace("\"", "").trim().toString();
                    tempStopApiId = detainInfor[8].split("=")[1].replace("\"", "").trim().toString();
                    tempStopName = detainInfor[11].split("=")[1].replace("\"", "").toString();

                }

                stopName = StaticCommonFuction.unicodeToKo(tempStopName);
                stopItem.apiId = tempStopApiId;
                stopItem.arsId = tempStopArsId;

                if (stopItem.arsId != null && !stopItem.arsId.equals("none")) {
                    stopItem.name = stopName;
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_JEON._cityId);
                    localBusStopItems.add(stopItem);
                }

            }

        }

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

        String param = String.format("callCount=1&page=/map/busInfo/searchStation.do&httpSessionId=00000000.TATMS_Web2_servlet_engine1" +
                "&scriptSessionId=00000000&c0-scriptName=dwrBusInfoService&c0-methodName=selectStationDetailView&c0-id=0&c0-param0=string:%s" +
                "&batchId=3", sItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(mStopUrl, true, param, "utf-8");
        String[] tempInfor = parseStr.split("\\['ROUTE_TP']=");


        for (int i = 1; i < tempInfor.length; i++) {

            String[] detainInfor = tempInfor[i].split(";");
            String tempArrMsg = detainInfor[2].split("=")[1].replace("\"", "").trim().toString();
            String tempDst = detainInfor[3].split("=")[1].replace("\"", "").trim().toString();
            String arrmsg1 = StaticCommonFuction.unicodeToKo(tempArrMsg);
            String dst = StaticCommonFuction.unicodeToKo(tempDst);
            String routeName = detainInfor[4].split("=")[1].replace("\"", "");

            BusRouteItem busRouteItem = new BusRouteItem();


            if (arrmsg1.contains("진입")) {
                ArriveItem arriveItem = new ArriveItem();
                if (routeName.equals(sItem.busRouteName)) {
                    arriveItem.state = Constants.STATE_NEAR;
                    busRouteItem.arriveInfo.add(arriveItem);
                    arriveItems.add(arriveItem);
                }
            } else if (arrmsg1.contains("분")) {
                ArriveItem arriveItem = new ArriveItem();
                if (routeName.equals(sItem.busRouteName)) {
                    arriveItem.state = Constants.STATE_ING;
                    arriveItem.remainMin = Integer.parseInt(arrmsg1.split("분")[0].trim());
                    arriveItems.add(arriveItem);
                }

            } else if (arrmsg1.contains("출발예정")) {
                ArriveItem arriveItem = new ArriveItem();
                if (routeName.equals(sItem.busRouteName)) {
                    arriveItem.state = Constants.STATE_PREPARE_START;
                    arriveItem.elseStr = arrmsg1.split(" ")[0].trim();
                    arriveItems.add(arriveItem);
                }
                busRouteItem.arriveInfo.add(arriveItem);
            }

            busRouteItem.busRouteName = routeName;
            busRouteItem.nextDirctionName = dst;
            busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_JEON._cityId);

        }


        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);

        return depthAlarmItem;
    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);

        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();
        String param = String.format("callCount=1&page=/map/busInfo/searchStation.do&httpSessionId=00000000.TATMS_Web2_servlet_engine1" +
                "&scriptSessionId=00000000&c0-scriptName=dwrBusInfoService&c0-methodName=selectStationDetailView&c0-id=0&c0-param0=string:%s" +
                "&batchId=3", favoriteAndHistoryItem.busRouteItem.busStopApiId);
        String parseStr = RequestCommonFuction.getSource(mStopUrl, true, param, "utf-8");

        if (parseStr != null) {

            String[] tempInfor = parseStr.split("\\['ROUTE_TP']=");

            for (int i = 1; i < tempInfor.length; i++) {
                String[] detainInfor = tempInfor[i].split(";");

                String tempArrMsg = detainInfor[2].split("=")[1].replace("\"", "").trim().toString();
                String tempDst = detainInfor[3].split("=")[1].replace("\"", "").trim().toString();
                String arrmsg1 = StaticCommonFuction.unicodeToKo(tempArrMsg);
                String dst = StaticCommonFuction.unicodeToKo(tempDst);
                String routeName = detainInfor[4].split("=")[1].replace("\"", "");

                BusRouteItem busRouteItem = new BusRouteItem();

                if (arrmsg1.contains("진입")) {
                    ArriveItem arriveItem = new ArriveItem();
                    if (routeName.equals(favoriteAndHistoryItem.busRouteItem.busRouteName)) {
                        arriveItem.state = Constants.STATE_NEAR;
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }

                } else if (arrmsg1.contains("분")) {
                    ArriveItem arriveItem = new ArriveItem();
                    if (routeName.equals(favoriteAndHistoryItem.busRouteItem.busRouteName)) {
                        arriveItem.state = Constants.STATE_ING;
                        arriveItem.remainMin = Integer.parseInt(arrmsg1.split("분")[0].trim());
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }


                } else if (arrmsg1.contains("출발예정")) {
                    ArriveItem arriveItem = new ArriveItem();
                    if (routeName.equals(favoriteAndHistoryItem.busRouteItem.busRouteName)) {
                        arriveItem.state = Constants.STATE_PREPARE_START;
                        arriveItem.elseStr = arrmsg1.split(" ")[0].trim();
                        sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                    }
                }

                busRouteItem.busRouteName = routeName;
                busRouteItem.nextDirctionName = dst;
                busRouteItem.localInfoId = String.valueOf(CommonConstants.CITY_DAE_JEON._cityId);

            }

        }


        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);


        return sItem;
    }

}
