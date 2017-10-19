package teamdoppelganger.smarterbus.util.parser;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.smart.lib.CommonConstants;

import android.database.Cursor;
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

public class ParserIncheon extends CommonParser {

    String mLineUrl = "http://bus.incheon.go.kr/iw/jsp/mapService/retrieveBstopRouteCmd.ajax";
    String mLineUrl2 = "http://bus.incheon.go.kr/iwcm/retrievebusstopcararriveinfo.laf";
    String mStopUrl = "http://m.ictr.or.kr/localbus/busRoutePath.do";


    public ParserIncheon(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {

        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();

        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_IN_CHEON._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_IN_CHEON._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_IN_CHEON._cityId);

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

        String param = String.format("routeid=%s", sItem.tempId);


        String parseString = RequestCommonFuction.getSource("http://appbus.ifez.go.kr/web/busRoutePath.asp", false, param, "utf-8");

        HashMap<Integer, String> mHashStr = new HashMap<Integer, String>();

        int count = 0;
        if (parseString != null) {
            Document doc = Jsoup.parse(parseString);
            Elements liElements = doc.select("ul#nodeList").select("li").select("div");

            for (int i = 0; i < liElements.size(); i++) {

                Elements divEl = liElements.get(i).select("div");

                if (divEl != null && divEl.size() > 2) {
                    String arsId = liElements.get(i).select("div").get(2).text().trim();

                    for (int j = 1; j < divEl.size() - 2; j++) {

                        mHashStr.put(count, arsId + "/" + (j - (divEl.size() - 2)));
                        count++;

                    }
                }
            }
        }

        String parseStr = RequestCommonFuction.getSource("http://appbus.ifez.go.kr/web/sub/getBusRoutePathJson.asp", false, param, "euc-kr");

        if (parseStr != null && mHashStr.size() > 0) {

            JSONArray json;
            try {
                json = new JSONArray(parseStr);
                for (int j = 0; j < json.length(); j++) {
                    JSONObject object = (JSONObject) json.get(j);

                    String plainNum = object.getString("BUSNUM");
                    int position = Integer.parseInt(object.getString("PATHSEQ")) - 1;
                    String tmpStr = mHashStr.get(position);
                    String arsId = tmpStr.split("/")[0];
                    String remainPosition = tmpStr.split("/")[1];

                    for (int k = 0; k < sItem.busStopItem.size(); k++) {
                        if (sItem.busStopItem.get(k).arsId.equals(arsId)) {
                            sItem.busStopItem.get(k).isExist = true;
                            sItem.busStopItem.get(k).plainNum = plainNum;
                            //sItem.busStopItem.get(k).position = 1;//Integer.parseInt(remainPosition);
                            break;
                        }
                    }

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }


        sItem.depthIndex = 0;

        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        //실시간 버스 도착 정보를 가져옴
        String parseStr = RequestCommonFuction.getSource(mLineUrl2, false, "routeid=111111111&bstopid=" + sItem.apiId, "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);

            Elements tdTag = doc.select("td");
            int tempCount = 1;
            boolean isNext = false;
            String tempRouteNum = null, tempRouteTime = null;
            HashMap<String, String> tempSaveRealTimePair = new HashMap<String, String>();

            for (int i = 0; i < tdTag.size(); i++) {

                Element td = tdTag.get(i);


                if (td.attr("align").equals("center")
                        && td.attr("bgcolor").equals("#FFFFFF")) {

                    int tempNum = tempCount % 6;

                    if (tempNum == 1) {
                        tempRouteNum = td.text();
                    } else if (tempNum == 0) {
                        tempRouteTime = td.text();
                        isNext = true;
                    }


                    if (isNext) {
                        tempSaveRealTimePair.put(tempRouteNum, tempRouteTime);

                        BusRouteItem routeItem = new BusRouteItem();
                        routeItem.busRouteName = tempRouteNum;
                        routeItem.localInfoId = String.valueOf(CommonConstants.CITY_IN_CHEON._cityId);

                        ArriveItem arriveItem = new ArriveItem();

                        if (td.text().contains("분")) {
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainMin = Integer.parseInt(td.text().split("분")[0]);

                            if (td.text().contains("초")) {
                                arriveItem.remainSecond = Integer.parseInt(td.text().split("분")[1].trim().split("초")[0]);
                            }

                            routeItem.arriveInfo.add(arriveItem);
                        } else if (td.text().contains("초")) {
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainMin = 0;
                            routeItem.arriveInfo.add(arriveItem);
                        }

                        localBusRouteItems.add(routeItem);
                        tempRouteNum = "";
                        tempRouteTime = "";
                        isNext = false;
                    }

                    tempCount++;
                }
            }

        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;
    }


    public String getArrivalValue() {


        return null;

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

        //실시간 버스 도착 정보를 가져옴
        String parseStr = RequestCommonFuction.getSource(mLineUrl2, false, "routeid=111111111&bstopid=" + sItem.busStopApiId, "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);

            Elements tdTag = doc.select("td");
            int tempCount = 1;
            boolean isNext = false;
            String tempRouteNum = null, tempRouteTime = null;
            HashMap<String, String> tempSaveRealTimePair = new HashMap<String, String>();

            for (int i = 0; i < tdTag.size(); i++) {

                Element td = tdTag.get(i);


                if (td.attr("align").equals("center")
                        && td.attr("bgcolor").equals("#FFFFFF")) {

                    int tempNum = tempCount % 6;

                    if (tempNum == 1) {
                        tempRouteNum = td.text();
                    } else if (tempNum == 0) {
                        tempRouteTime = td.text();
                        isNext = true;
                    }


                    if (isNext) {
                        tempSaveRealTimePair.put(tempRouteNum, tempRouteTime);

                        BusRouteItem routeItem = new BusRouteItem();
                        routeItem.busRouteName = tempRouteNum;


                        if (sItem.busRouteName.equals(tempRouteNum)
                                || sItem.busRouteName.split("[(]")[0].equals(tempRouteNum)
                                || sItem.busRouteName.equals("M" + tempRouteNum)) {
                            ArriveItem arriveItem = new ArriveItem();

                            if (td.text().contains("분")) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItem.remainMin = Integer.parseInt(td.text().split("분")[0]);
                                routeItem.arriveInfo.add(arriveItem);
                            } else if (td.text().contains("초")) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItem.remainMin = 0;
                                routeItem.arriveInfo.add(arriveItem);
                            }

                            arriveItems.add(arriveItem);
                        }

                        tempRouteNum = "";
                        tempRouteTime = "";
                        isNext = false;
                    }

                    tempCount++;
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

        String parseStr = RequestCommonFuction.getSource(mLineUrl2, false, "routeid=111111111&bstopid=" + favoriteAndHistoryItem.busRouteItem.busStopApiId, "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            Elements tdTag = doc.select("td");
            int tempCount = 1;
            boolean isNext = false;
            String tempRouteNum = null, tempRouteTime = null;
            HashMap<String, String> tempSaveRealTimePair = new HashMap<String, String>();

            for (int i = 0; i < tdTag.size(); i++) {

                Element td = tdTag.get(i);


                if (td.attr("align").equals("center")
                        && td.attr("bgcolor").equals("#FFFFFF")) {

                    int tempNum = tempCount % 6;

                    if (tempNum == 1) {
                        tempRouteNum = td.text();
                    } else if (tempNum == 0) {
                        tempRouteTime = td.text();
                        isNext = true;
                    }


                    if (isNext) {
                        tempSaveRealTimePair.put(tempRouteNum, tempRouteTime);

                        BusRouteItem routeItem = new BusRouteItem();
                        routeItem.busRouteName = tempRouteNum;

                        if (tempRouteNum.equals(favoriteAndHistoryItem.busRouteItem.busRouteName)
                                || tempRouteNum.equals(favoriteAndHistoryItem.busRouteItem.busRouteName.split("[(]")[0])
                                || ("M" + tempRouteNum).equals(favoriteAndHistoryItem.busRouteItem.busRouteName)
                                || ("M" + tempRouteNum).equals(favoriteAndHistoryItem.busRouteItem.busRouteName.split("[(]")[0])) {
                            ArriveItem arriveItem = new ArriveItem();

                            if (td.text().contains("분")) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItem.remainMin = Integer.parseInt(td.text().split("분")[0]);

                                if (td.text().contains("초")) {
                                    arriveItem.remainSecond = Integer.parseInt(td.text().split("분")[1].trim().split("초")[0]);
                                }

                                routeItem.arriveInfo.add(arriveItem);
                            } else if (td.text().contains("초")) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItem.remainMin = 0;
                                routeItem.arriveInfo.add(arriveItem);
                            }

                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }

                        tempRouteNum = "";
                        tempRouteTime = "";
                        isNext = false;
                    }

                    tempCount++;
                }
            }
        }


        return sItem;
    }


}

