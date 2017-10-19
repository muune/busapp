package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smart.lib.CommonConstants;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils.TruncateAt;

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

public class ParserSeoul extends CommonParser {

    String localName = CommonConstants.CITY_SEO_UL._engName;
    String localId = String.valueOf(CommonConstants.CITY_SEO_UL._cityId);

    public ParserSeoul(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    String mStopUrl = "http://m.seoul.go.kr/mobile/traffic/GetRouteInfoAndPos.do";
    String mLineUrl = "http://m.bus.go.kr/mBus/bus/getStationByUid.bms";
    String mOneBusOneStopUrl = "http://m.bus.go.kr/mBus/bus/getArrInfoByRoute.bms";


    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_SEO_UL._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_SEO_UL._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_SEO_UL._cityId);

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
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        int tempNum = 5 - sItem.arsId.trim().length();

        String tempArsId = "";
        for (int i = 0; i < tempNum; i++) {
            tempArsId = "0" + sItem.arsId;
        }

        if (tempArsId.equals("")) {
            tempArsId = sItem.arsId;
        }

        String parseStr = RequestCommonFuction.getSource(mLineUrl, true, "arsId=" + tempArsId, "euc-kr");

        JSONObject object;
        try {
            object = new JSONObject(parseStr);
            JSONArray objectTag = (JSONArray) object.get("resultList");

            for (int i = 0; i < objectTag.length(); i++) {
                JSONObject jsonObject = (JSONObject) objectTag.get(i);

                String busRouteName = jsonObject.getString("rtNm");
                String busRouteApiId = jsonObject.getString("busRouteId");

                String direction = jsonObject.getString("adirection");

                String arrmsg1 = jsonObject.getString("arrmsg1");
                String arrmsg2 = jsonObject.getString("arrmsg2");


                String traTime1 = jsonObject.getString("traTime1");
                String traTime2 = jsonObject.getString("traTime2");

                String busOrder = null;
                try {
                    busOrder = jsonObject.getString("staOrd");
                } catch (Exception e) {
                }
                ;

                String plainNo1 = null;
                String plainNo2 = null;

                try {
                    plainNo1 = jsonObject.getString("plainNo1");
                } catch (Exception e) {
                }
                ;

                try {
                    plainNo2 = jsonObject.getString("plainNo2");
                } catch (Exception e) {
                }
                ;


                String nextDirection = jsonObject.getString("adirection");

                try {
                    String busType = jsonObject.getString("busType1");
                } catch (Exception e) {
                    String busType = "0";
                }
                ;

                BusRouteItem busRouteItem = new BusRouteItem();

                if (busRouteApiId.equals("0")) {
                    busRouteApiId = "";
                }

                busRouteItem.busRouteApiId = busRouteApiId;
                busRouteItem.busRouteApiId2 = "";
                busRouteItem.busRouteSubName = "";
                busRouteItem.stOrd = busOrder;


                busRouteItem.busRouteName = busRouteName;
                busRouteItem.direction = direction;
                busRouteItem.localInfoId = localId;
                busRouteItem.busType = StaticCommonFuction.getBusType(
                        getBusDb(), busRouteApiId,
                        CommonConstants.CITY_SEO_UL._engName);


                if (arrmsg1.trim().length() > 0) {
                    String[] tSplit = arrmsg1.split("\\[");
                    if (tSplit != null && tSplit.length > 1) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(traTime1) / 60; // Integer.parseInt(tSplit[0].split("분")[0].trim());
                        if (Integer.parseInt(traTime1) % 60 > 0) {
                            arriveItem.remainSecond = Integer.parseInt(traTime1) % 60;
                        }


                        arriveItem.remainStop = Integer.parseInt(tSplit[1].split("번")[0].trim());
                        arriveItem.state = Constants.STATE_ING;

                        if (plainNo1 != null) arriveItem.plainNum = plainNo1;


                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (arrmsg1.trim().contains("곧 도착")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_NEAR;
                        if (plainNo1 != null) arriveItem.plainNum = plainNo1;
                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (arrmsg1.trim().contains("대기중") || arrmsg1.trim().contains("출발대기")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE;
                        if (plainNo1 != null) arriveItem.plainNum = plainNo1;
                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (arrmsg1.trim().contains("운행종료")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_END;
                        if (plainNo1 != null) arriveItem.plainNum = plainNo1;
                        busRouteItem.arriveInfo.add(arriveItem);
                    }


                }


                if (arrmsg2.trim().length() > 0) {

                    String[] tSplit = arrmsg2.split("\\[");
                    if (tSplit != null && tSplit.length > 1) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(traTime2) / 60;
                        if (Integer.parseInt(traTime2) % 60 > 0) {
                            arriveItem.remainSecond = Integer.parseInt(traTime2) % 60;
                        }
                        arriveItem.remainStop = Integer.parseInt(tSplit[1].split("번")[0].trim());
                        arriveItem.state = Constants.STATE_ING;
                        if (plainNo2 != null) arriveItem.plainNum = plainNo2;

                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (arrmsg2.trim().contains("곧 도착")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE;
                        if (plainNo2 != null) arriveItem.plainNum = plainNo2;

                        busRouteItem.arriveInfo.add(arriveItem);
                    } else if (arrmsg2.trim().contains("대기중") || arrmsg2.trim().contains("출발대기")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE;
                        if (plainNo2 != null) arriveItem.plainNum = plainNo2;

                        busRouteItem.arriveInfo.add(arriveItem);

                    } else if (arrmsg2.trim().contains("운행종료")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_END;
                        if (plainNo1 != null) arriveItem.plainNum = plainNo1;
                        busRouteItem.arriveInfo.add(arriveItem);
                    }

                }

                localBusRouteItems.add(busRouteItem);

            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;
    }


    @Override
    public DepthRouteItem getAlarmList(String... idStr) {

        String busStopApiId, busRouteApiId, busStopInnerId = null, ord = null;

        busStopApiId = idStr[0];
        busRouteApiId = idStr[1];

        if (idStr.length == 2) {
            ArrayList<String> tempOrdAndId = getOrdAndId(busRouteApiId,
                    busStopApiId);

            ord = tempOrdAndId.get(0);
            busStopInnerId = tempOrdAndId.get(1);

        } else if (idStr.length == 3) {
            ord = idStr[2];
            busStopInnerId = idStr[3];
        }

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String param = String.format("stId=%s&busRouteId=%s&ord=%s",
                busStopInnerId, busRouteApiId, ord);

        String parseStr = RequestCommonFuction.getSource(mOneBusOneStopUrl,
                true, param, "euc-kr");

        JSONObject object;
        try {
            object = new JSONObject(parseStr);

            JSONArray objectTag = (JSONArray) object.get("resultList");
            for (int i = 0; i < objectTag.length(); i++) {

                JSONObject jsonObject = (JSONObject) objectTag.get(i);

                String plain1 = jsonObject.getString("plainNo1");
                String arrmsg1 = jsonObject.getString("arrmsg1");

                // 1차 버스
                BusRouteItem item = new BusRouteItem();
                item.busAlarmDivideName = plain1;
                item.busRouteApiId = busRouteApiId;
                item.stopApiOfRoute = busStopApiId;
                //item.arriveInfo.add(arrmsg1);
                item.plusInforAry.add(ord);
                item.plusInforAry.add(busStopInnerId);
                localBusRouteItems.add(item);

                String plain2 = jsonObject.getString("plainNo2");
                String arrmsg2 = jsonObject.getString("arrmsg2");

                // 2차 버스
                BusRouteItem item2 = new BusRouteItem();
                item2.busAlarmDivideName = plain2;
                item2.busRouteApiId = busRouteApiId;
                item2.stopApiOfRoute = busStopApiId;
                //item2.arriveInfo.add(arrmsg2);
                item2.plusInforAry.add(ord);
                item2.plusInforAry.add(busStopInnerId);
                localBusRouteItems.add(item2);

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;

    }


    public ArrayList<String> getOrdAndId(String busApiId, String busStopApiId) {

        ArrayList<String> ord = new ArrayList<String>();

        JSONObject object;
        String parseStr = RequestCommonFuction.getSource(mStopUrl, true,
                "busRouteId=" + busApiId, "utf-8");

        try {
            object = new JSONObject(parseStr);

            JSONArray objectTag = (JSONArray) object.get("resultList");

            for (int i = 0; i < objectTag.length(); i++) {
                JSONObject jsonObject = (JSONObject) objectTag.get(i);

                String stationArsId = jsonObject.getString("arsId");
                String stationApiId = jsonObject.getString("arsId");

                String stationInnerId = jsonObject.getString("station");

                if (stationArsId.equals(busStopApiId)) {
                    ord.add(String.valueOf(i + 1));
                    ord.add(stationInnerId);
                    return ord;
                }

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return ord;
    }

    @Override
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItems) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItems) {

        String sql;

        if (busStopItems.apiId != null && busStopItems.apiId.trim().length() > 0) {
            sql = String.format("SELECT * FROM %s where %s='%s'", localName + "_STOP"
                    , CommonConstants.BUS_STOP_API_ID, busStopItems.apiId);


        } else {
            sql = String.format("SELECT * FROM %s where %s='%s'", localName + "_STOP"
                    , CommonConstants.BUS_STOP_NAME, busStopItems.name);
        }

        Cursor cursor = getBusDb().rawQuery(sql, null);
        while (cursor.moveToNext()) {

            String arsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
            String apiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
            String stopId = cursor.getString(cursor.getColumnIndex(CommonConstants._ID));
            busStopItems.arsId = arsId;
            busStopItems.apiId = apiId;
            busStopItems._id = Integer.parseInt(stopId);

            break;
        }
        cursor.close();


        busStopItems.plusParsingNeed = 0;
        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.add(busStopItems);


        // TODO Auto-generated method stub
        return depthStopItem;
    }

    @Override
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {

        String parseStr = RequestCommonFuction.getSource(mStopUrl, true,
                "busRouteId=" + sItem.tempId, "utf-8");

        JSONObject object = null;
        try {
            if (parseStr != null) {
                object = new JSONObject(parseStr);

                JSONArray objectTag = (JSONArray) object.get("resultList");

                for (int i = 0; i < objectTag.length(); i++) {
                    JSONObject jsonObject = (JSONObject) objectTag.get(i);

                    String stationArsId = jsonObject.getString("stationNo");
                    String stationApiId = jsonObject.getString("station");
                    String stationTempId = jsonObject.getString("station");

                    String stationName = jsonObject.getString("stationNm");
                    String isExist = jsonObject.getString("existYn");
                    Boolean transYn = Boolean.parseBoolean(jsonObject
                            .getString("transYn"));

                    double gpsX = Double.parseDouble(jsonObject.getString("gpsX"));
                    double gpsY = Double.parseDouble(jsonObject.getString("gpsY"));

                    BusStopItem busStopItem = new BusStopItem();

                    busStopItem.arsId = stationArsId;
                    busStopItem.apiId = stationApiId;

                    if (isExist.equals("Y")) {

                        for (int k = 0; k < sItem.busStopItem.size(); k++) {

                            if (sItem.busStopItem.get(k).apiId.equals(stationApiId.trim())) {
                                sItem.busStopItem.get(k).isExist = true;
                                String plainNum = jsonObject.getString("plainNo");

                                if (plainNum.contains("저상")) {
                                    plainNum = plainNum.replace("저상", "저상 ");
                                } else if (plainNum.length() > 5) {
                                    plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                }

                                sItem.busStopItem.get(k).plainNum = plainNum;

                                break;
                            }

                        }


                    } else {
                        busStopItem.isExist = false;
                    }

                    busStopItem.latitude = gpsX;
                    busStopItem.longtitude = gpsY;
                    busStopItem.name = stationName;
                    busStopItem.localInfoId = localId;
                    busStopItem.plusParsingNeed = 0;

                }
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sItem.depthIndex = 0;


        return sItem;
    }

    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        int tempNum = 5 - sItem.busStopArsId.trim().length();

        String tempArsId = "";
        for (int i = 0; i < tempNum; i++) {
            tempArsId = "0" + sItem.busStopArsId;
        }

        if (tempArsId.equals("")) {
            tempArsId = sItem.busStopArsId;
        }

        String parseStr = RequestCommonFuction.getSource(mLineUrl, true,
                "arsId=" + tempArsId, "euc-kr");

        if (parseStr != null) {
            JSONObject object;
            try {
                object = new JSONObject(parseStr);

                JSONArray objectTag = (JSONArray) object.get("resultList");

                for (int i = 0; i < objectTag.length(); i++) {
                    JSONObject jsonObject = (JSONObject) objectTag.get(i);

                    String busRouteName = jsonObject.getString("rtNm");
                    String busRouteApiId = jsonObject.getString("busRouteId");

                    String direction = jsonObject.getString("adirection");

                    String arrmsg1 = jsonObject.getString("arrmsg1");
                    String arrmsg2 = jsonObject.getString("arrmsg2");

                    String traTime1 = jsonObject.getString("traTime1");
                    String traTime2 = jsonObject.getString("traTime2");

                    String plainNo1 = null;  //jsonObject.getString("plainNo1");
                    String plainNo2 = null;  //jsonObject.getString("plainNo2");

                    try {
                        plainNo1 = jsonObject.getString("plainNo1");
                    } catch (Exception e) {
                    }
                    ;

                    try {
                        plainNo2 = jsonObject.getString("plainNo2");
                    } catch (Exception e) {
                    }


                    boolean isPass = false;

                    if (sItem.direction != null) {
                        isPass = true;
                    }

                    if (busRouteApiId.equals(sItem.busRouteApiId)) {

                        if (!sItem.direction.equals(direction)) {
                            continue;
                        }


                        if (arrmsg1.trim().length() > 0) {
                            String[] tSplit = arrmsg1.split("\\[");
                            if (tSplit != null && tSplit.length > 1) {
                                ArriveItem arriveItem = new ArriveItem();

                                arriveItem.remainMin = Integer.parseInt(traTime1) / 60; // Integer.parseInt(tSplit[0].split("분")[0].trim());
                                if (Integer.parseInt(traTime1) % 60 > 0) {
                                    arriveItem.remainSecond = Integer.parseInt(traTime1) % 60;
                                }

                                arriveItem.remainStop = Integer.parseInt(tSplit[1].split("번")[0].trim());
                                arriveItem.state = Constants.STATE_ING;

                                if (plainNo1 != null) arriveItem.plainNum = plainNo1;
                                arriveItems.add(arriveItem);
                            } else if (arrmsg1.trim().contains("대기중")) {


                            }
                        }

                        if (arrmsg2.trim().length() > 0) {

                            String[] tSplit = arrmsg2.split("\\[");
                            if (tSplit != null && tSplit.length > 1) {
                                ArriveItem arriveItem = new ArriveItem();
                                //arriveItem.remainMin = Integer.parseInt(tSplit[0].split("분")[0].trim());
                                arriveItem.remainMin = Integer.parseInt(traTime2) / 60; // Integer.parseInt(tSplit[0].split("분")[0].trim());
                                if (Integer.parseInt(traTime2) % 60 > 0) {
                                    arriveItem.remainSecond = Integer.parseInt(traTime2) % 60;
                                }
                                arriveItem.remainStop = Integer.parseInt(tSplit[1].split("번")[0].trim());
                                arriveItem.state = Constants.STATE_ING;
                                if (plainNo2 != null) arriveItem.plainNum = plainNo2;

                                arriveItems.add(arriveItem);
                            } else if (arrmsg2.trim().contains("대기중")) {

                            }
                        }
                    }

                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();

        int tempNum = 5 - favoriteAndHistoryItem.busRouteItem.busStopArsId.trim().length();

        String tempArsId = "";
        for (int i = 0; i < tempNum; i++) {
            tempArsId = "0" + favoriteAndHistoryItem.busRouteItem.busStopArsId;
        }

        if (tempArsId.equals("")) {
            tempArsId = favoriteAndHistoryItem.busRouteItem.busStopArsId;
        }


        String parseStr = RequestCommonFuction.getSource(mLineUrl, true,
                "arsId=" + tempArsId, "euc-kr");


        if (parseStr != null) {
            JSONObject object;
            try {
                object = new JSONObject(parseStr);

                JSONArray objectTag = (JSONArray) object.get("resultList");

                for (int i = 0; i < objectTag.length(); i++) {
                    JSONObject jsonObject = (JSONObject) objectTag.get(i);

                    String busRouteName = jsonObject.getString("rtNm");
                    String busRouteApiId = jsonObject.getString("busRouteId");

                    String direction = jsonObject.getString("adirection");

                    String arrmsg1 = jsonObject.getString("arrmsg1");
                    String arrmsg2 = jsonObject.getString("arrmsg2");

                    String traTime1 = jsonObject.getString("traTime1");
                    String traTime2 = jsonObject.getString("traTime2");


                    String plainNo1 = null;
                    String plainNo2 = null;

                    try {
                        plainNo1 = jsonObject.getString("plainNo1");
                    } catch (Exception e) {
                    }
                    ;

                    try {
                        plainNo2 = jsonObject.getString("plainNo2");
                    } catch (Exception e) {
                    }
                    ;


                    boolean isPass = false;
                    if (favoriteAndHistoryItem.busRouteItem.direction != null) {
                        isPass = true;
                    }

                    if (busRouteApiId.equals(favoriteAndHistoryItem.busRouteItem.busRouteApiId)) {

                        if (isPass) {


                            if (!favoriteAndHistoryItem.busRouteItem.direction.equals(direction)) {
                                continue;
                            }
                        }

                        if (arrmsg1.trim().length() > 0) {
                            String[] tSplit = arrmsg1.split("\\[");
                            if (tSplit != null && tSplit.length > 1) {
                                ArriveItem arriveItem = new ArriveItem();
                                //arriveItem.remainMin = Integer.parseInt(tSplit[0].split("분")[0].trim());
                                arriveItem.remainMin = Integer.parseInt(traTime1) / 60; // Integer.parseInt(tSplit[0].split("분")[0].trim());
                                if (Integer.parseInt(traTime1) % 60 > 0) {
                                    arriveItem.remainSecond = Integer.parseInt(traTime1) % 60;
                                }
                                arriveItem.remainStop = Integer.parseInt(tSplit[1].split("번")[0].trim());
                                arriveItem.state = Constants.STATE_ING;

                                if (plainNo1 != null) arriveItem.plainNum = plainNo1;
                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            } else if (arrmsg1.trim().contains("곧 도착")) {

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_NEAR;
                                if (plainNo1 != null) arriveItem.plainNum = plainNo1;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            } else if (arrmsg1.trim().contains("대기중") || arrmsg1.trim().contains("출발대기")) {

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_PREPARE;
                                if (plainNo1 != null) arriveItem.plainNum = plainNo1;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            } else if (arrmsg1.trim().contains("운행종료")) {
                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_END;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            }
                        }

                        if (arrmsg2.trim().length() > 0) {

                            String[] tSplit = arrmsg2.split("\\[");
                            if (tSplit != null && tSplit.length > 1) {
                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = Integer.parseInt(traTime2) / 60;
                                if (Integer.parseInt(traTime2) % 60 > 0) {
                                    arriveItem.remainSecond = Integer.parseInt(traTime2) % 60;
                                }
                                arriveItem.remainStop = Integer.parseInt(tSplit[1].split("번")[0].trim());
                                arriveItem.state = Constants.STATE_ING;
                                if (plainNo2 != null) arriveItem.plainNum = plainNo2;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            } else if (arrmsg2.trim().contains("곧 도착")) {
                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_NEAR;
                                if (plainNo2 != null) arriveItem.plainNum = plainNo2;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            } else if (arrmsg2.trim().contains("대기중") || arrmsg2.trim().contains("출발대기")) {
                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_PREPARE;
                                if (plainNo2 != null) arriveItem.plainNum = plainNo2;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            } else if (arrmsg2.trim().contains("운행종료")) {
                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.state = Constants.STATE_END;


                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            }
                        }
                    }


                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sItem;
    }


}
