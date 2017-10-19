package teamdoppelganger.smarterbus.util.parser;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;


public class ParserGunggi extends CommonParser {


    String localName = CommonConstants.CITY_GYEONG_GI._engName;
    String localId = String.valueOf(CommonConstants.CITY_GYEONG_GI._cityId);


    public ParserGunggi(SQLiteDatabase db) {
        super(db);
    }

    String mLineUrl = "http://www.gbis.go.kr/ws/rest/busarrivalservice/station";
    String mLineUrl2 = "http://m.gbis.go.kr/jsp/routeInfo.jsp";//"http://m.gbis.go.kr/BusLine.action";
    String mStopUrl = "http://www.gbis.go.kr/ws/rest/buslocationservice";
    String mStopUrl2 = "http://m.gbis.go.kr/jsp/busArrival.jsp";

    String mAlarmUrl = "http://m.gbis.go.kr/jsp/routeInfoArrival.jsp";


    String mKey = "ZWwoooXb0G4VE24Qfm2%2BciiAalL%2FUvFVKG7kqdaepp58mgRFrcdjPFUniNP2RLJvGmBD0KpPMDyqxeMcSwoviQ%3D%3D";


    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {


        ArrayList<BusStopItem> localBusStopItems = new ArrayList<BusStopItem>();
        String selectBusStopQry = String.format("SELECT %s,%s FROM %s where %s='%s'", CommonConstants.BUS_ROUTE_RELATED_STOPS, CommonConstants.BUS_ROUTE_TURN_STOP_IDX, CommonConstants.CITY_GYEONG_GI._engName + "_Route",
                CommonConstants.BUS_ROUTE_ID1, rItem.busRouteApiId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {
            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
            int turnStopIdx = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TURN_STOP_IDX));

            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GYEONG_GI._engName + "_Stop",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusStopItem stopItem = new BusStopItem();

                    stopItem.name = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    stopItem.apiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    stopItem.arsId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));

                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GYEONG_GI._cityId);

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
        depthStopItem.depthIndex = 2;


        return depthStopItem;
    }


    @Override
    public DepthStopItem getDepthStopList(DepthStopItem sItem) {

        if (sItem.depthIndex == 2) {

            String whereType = null;

            if (sItem.busStopItem.size() > 0) {

                String stopQry = String.format("SELECT * From %s where %s='%s'", CommonConstants.CITY_GYEONG_GI._engName + "_Stop",
                        CommonConstants.BUS_STOP_API_ID, sItem.busStopItem.get(0).apiId);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    whereType = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));
                }
                stopCursor.close();
            }

            String parseStr = null;


            if (whereType == null
                    || whereType.equals("")) {


                sItem.depthIndex = 1;

            } else if (whereType.equals("의왕")) {


                parseStr = RequestCommonFuction.getSource("http://bis.uw21.net/mobile/busLocation.jsp?routeId=" + sItem.tempId, false, "", "utf-8");


                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);
                    Elements seqElements = doc.select("table[class=box6]").select("tr");


                    for (int i = 0; i < seqElements.size(); i++) {


                        if (seqElements.get(i).select("td").size() == 3) {
                            try {
                                String stopApiId = seqElements.get(i).select("td").get(0).attr("onclick").split("stationId=")[1].replace("'", "");


                                if (seqElements.get(i).select("td").get(1).select("img").size() > 0) {
                                    String plainNum = seqElements.get(i).select("td").get(1).select("a").attr("onclick").split("\\(")[1].split(",")[0].replace("'", "");

                                    for (int k = 0; k < sItem.busStopItem.size(); k++) {
                                        if (sItem.busStopItem.get(k).apiId.substring(2).equals(stopApiId)) {
                                            sItem.busStopItem.get(k).isExist = true;
                                            if (plainNum.length() > 5) {
                                                plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                                sItem.busStopItem.get(k).plainNum = plainNum;
                                            }
                                            break;
                                        }

                                    }

                                }

                            } catch (IndexOutOfBoundsException e) {

                            }


                        }
                    }


                    sItem.depthIndex = 0;

                }


            } else if (whereType.equals("군포1")) {


                parseStr = RequestCommonFuction.getSource("http://bis.gunpo21.net/mobile/busLocation.jsp?routeId=" + sItem.tempId, false, "", "utf-8");


                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);
                    Elements seqElements = doc.select("table[class=box6]").select("tr");

                    for (int i = 0; i < seqElements.size(); i++) {


                        if (seqElements.get(i).select("td").size() == 3) {
                            String stopApiId = seqElements.get(i).select("td").get(0).attr("onclick").split("stationId=")[1].replace("'", "");


                            if (seqElements.get(i).select("td").get(1).select("img").size() > 0) {
                                String plainNum = seqElements.get(i).select("td").get(1).select("a").attr("onclick").split("\\(")[1].split(",")[0].replace("'", "");

                                for (int k = 0; k < sItem.busStopItem.size(); k++) {
                                    if (sItem.busStopItem.get(k).apiId.substring(2).equals(stopApiId)) {
                                        sItem.busStopItem.get(k).isExist = true;
                                        if (plainNum.length() > 5) {
                                            plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                            sItem.busStopItem.get(k).plainNum = plainNum;
                                        }
                                        break;
                                    }

                                }


                            }


                        }
                    }


                    sItem.depthIndex = 0;

                }


            } else if (whereType.equals("부천")) {

                String param = String.format("ROUTE_ID=%s", sItem.tempId);
                String parseStr2 = RequestCommonFuction.getSource("http://m.bcits.go.kr/jsp/submain/bus/route_info.jsp", true, param, "utf-8");

                ArrayList<Integer> pushTmpAry = new ArrayList<Integer>();


                if (parseStr2 != null) {

                    Document doc2 = Jsoup.parse(parseStr2);
                    Elements seqElements2 = doc2.select("div[class=busline]").select("li");


                    for (int j = 0; j < seqElements2.size(); j++)
                        if (seqElements2.get(j).select("p").hasClass("busi_no")) {
                            String arsId = seqElements2.get(j).select("span[class=fc_o]").text();
                            String plainNum = seqElements2.get(j).select("p[class=busi_no]").text();
                            if (plainNum.length() > 5) {
                                plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                            }


                            for (int i = 0; i < sItem.busStopItem.size(); i++) {

                                if (sItem.busStopItem.get(i).arsId.equals(arsId) && !pushTmpAry.contains(i)) {

                                    pushTmpAry.add(i);
                                    sItem.busStopItem.get(i).isExist = true;
                                    sItem.busStopItem.get(i).plainNum = plainNum;

                                    break;
                                }

                            }


                        }

                }


                sItem.depthIndex = 0;


            } else if (whereType.equals("남양주1")) {
                parseStr = RequestCommonFuction.getSource("http://bis.nyj.go.kr/mobile/service/m6_s03.htm?routeid=" + sItem.tempId, false, "", "utf-8");


                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);
                    Elements seqElements = doc.select("div[class=ab_bor]");

                    for (int i = 0; i < seqElements.size(); i++) {

                        int size = seqElements.get(i).select("p").size();

                        if (size == 3) {

                            String stopArsId = seqElements.get(i).select("p").get(1).text();

                            for (int k = 0; k < sItem.busStopItem.size(); k++) {
                                if (sItem.busStopItem.get(k).arsId.equals(stopArsId)) {
                                    sItem.busStopItem.get(k).isExist = true;

                                    break;
                                }

                            }
                        }
                    }

                    sItem.depthIndex = 0;

                }


            }


        } else if (sItem.depthIndex == 1) {


            String param = String.format("routeId=%s&cmd=%s", sItem.tempId, "searchRouteJson");
            String parseStr = RequestCommonFuction.getSource("http://www.gbis.go.kr/gbis2014/schBusAPI.action", true, param, "utf-8");


            if (parseStr != null) {
                JSONObject json;
                try {
                    json = new JSONObject(parseStr).getJSONObject("result").getJSONObject("realTime");


                    JSONArray jsonArray = json.getJSONArray("list");


                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);


                        if (jsonObject.has("busSapceList")) {
                            JSONArray detailList = (JSONArray) jsonObject.get("busSapceList");
                            String busPosition = jsonObject.getString("busPosition");
                            String plainNum = jsonObject.getString("busNo");
                            String apiId = jsonObject.getString("fromStationId");

                            if (busPosition != null) {
                                busPosition = busPosition.replace("[", "").replace("]", "").replace("\"", "");
                            }

                            if (plainNum != null) {
                                plainNum = plainNum.replace("[", "").replace("]", "").replace("\"", "");
                            }

                            for (int j = 0; j < detailList.length(); j++) {
                                JSONObject object = (JSONObject) detailList.get(j);


                                int remainSeat = -1;

                                try {
                                    remainSeat = Integer.parseInt(object.getString("remainSeatCnt1"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                for (int k = 0; k < sItem.busStopItem.size(); k++) {
                                    if (sItem.busStopItem.get(k).apiId.equals(apiId)) {

                                        if (plainNum.length() > 5) {

                                            plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());

                                            if (busPosition.equals("1")) {

                                                sItem.busStopItem.get(k).isExist = true;
                                                sItem.busStopItem.get(k).position = 2;
                                                if (remainSeat > 0) {
                                                    sItem.busStopItem.get(k).plainNum = plainNum + ";" + remainSeat;
                                                } else {
                                                    sItem.busStopItem.get(k).plainNum = plainNum;
                                                }


                                            } else if (busPosition.equals("2")) {

                                                sItem.busStopItem.get(k).isExist = true;
                                                sItem.busStopItem.get(k).position = 2;
                                                if (remainSeat > 0) {
                                                    sItem.busStopItem.get(k).plainNum = plainNum + ";" + remainSeat;
                                                } else {
                                                    sItem.busStopItem.get(k).plainNum = plainNum;
                                                }

                                            }


                                            break;

                                        }

                                    }


                                }
                                break;
                            }


                        } else {
                            String apiId = jsonObject.getString("fromStationId");
                            String plainNum = jsonObject.getString("busNo");
                            String busPosition = jsonObject.getString("busPosition");

                            if (busPosition != null) {
                                busPosition = busPosition.replace("[", "").replace("]", "").replace("\"", "");
                            }

                            if (plainNum != null) {
                                plainNum = plainNum.replace("[", "").replace("]", "").replace("\"", "");
                            }


                            for (int k = 0; k < sItem.busStopItem.size(); k++) {
                                if (sItem.busStopItem.get(k).apiId.equals(apiId)) {

                                    if (busPosition.equals("1")) {

                                        sItem.busStopItem.get(k).isExist = true;
                                        sItem.busStopItem.get(k).position = 2;
                                        if (plainNum.length() > 5) {
                                            plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                            sItem.busStopItem.get(k).plainNum = plainNum;
                                        }


                                    } else if (busPosition.equals("2")) {

                                        sItem.busStopItem.get(k).isExist = true;
                                        sItem.busStopItem.get(k).position = 2;
                                        if (plainNum.length() > 5) {
                                            plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                            sItem.busStopItem.get(k).plainNum = plainNum;
                                        }

                                    }


                                    break;
                                }

                            }


                        }

                    }


                } catch (JSONException e) {
                    parseStr = RequestCommonFuction.getSource("http://www.gbis.go.kr/ws/rest/buslocationservice?serviceKey=" + mKey + "&routeId=" +
                            sItem.tempId, false, "", "utf-8");

                    if (parseStr == null) {
                        parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/buslocationservice?serviceKey=" + mKey + "&routeId=" +
                                sItem.tempId, false, "", "utf-8");
                    }

                    if (parseStr == null) {
                        parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/buslocationservice?serviceKey=1234567890&routeId=" +
                                sItem.tempId, false, "", "utf-8");
                    }

                    if (parseStr != null) {

                        Document doc = Jsoup.parse(parseStr);

                        Elements seqElements = doc.select("busLocationList");


                        for (int i = 0; i < seqElements.size(); i++) {
                            String stationId = seqElements.get(i).select("stationId").text();
                            String plainNum = seqElements.get(i).select("plateNo").text();


                            for (int j = 0; j < sItem.busStopItem.size(); j++) {

                                if (sItem.busStopItem.get(j).apiId.equals(stationId)) {

                                    if (plainNum.length() > 5) {
                                        plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                        sItem.busStopItem.get(j).plainNum = plainNum;
                                    }

                                    break;
                                }

                            }


                        }

                    }
                    e.printStackTrace();
                }
            } else {

                parseStr = RequestCommonFuction.getSource("http://www.gbis.go.kr/ws/rest/buslocationservice?serviceKey=" + mKey + "&routeId=" +
                        sItem.tempId, false, "", "utf-8");


                if (parseStr == null) {
                    parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/buslocationservice?serviceKey=" + mKey + "&routeId=" +
                            sItem.tempId, false, "", "utf-8");
                }

                if (parseStr == null) {
                    parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/buslocationservice?serviceKey=1234567890&routeId=" +
                            sItem.tempId, false, "", "utf-8");
                }

                if (parseStr != null) {
                    Document doc = Jsoup.parse(parseStr);

                    Elements seqElements = doc.select("busLocationList");


                    for (int i = 0; i < seqElements.size(); i++) {
                        String stationId = seqElements.get(i).select("stationId").text();
                        String plainNum = seqElements.get(i).select("plateNo").text();


                        for (int j = 0; j < sItem.busStopItem.size(); j++) {

                            if (sItem.busStopItem.get(j).apiId.equals(stationId)) {

                                if (plainNum.length() > 5) {
                                    plainNum = plainNum.substring(0, 5) + " " + plainNum.substring(5, plainNum.length());
                                    sItem.busStopItem.get(j).plainNum = plainNum;
                                }

                                break;
                            }

                        }

                    }

                }


            }


            sItem.depthIndex = 0;
        }
        return sItem;
    }


    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();
        String selectBusStopQry = String.format("SELECT %s FROM %s where %s='%s'", CommonConstants.BUS_STOP_RELATED_ROUTES, CommonConstants.CITY_GYEONG_GI._engName + "_stop",
                CommonConstants.BUS_STOP_ARS_ID, sItem.arsId);
        Cursor cursor = mSqliteDb.rawQuery(selectBusStopQry, null);
        String busRelateStops;
        if (cursor.moveToNext()) {

            busRelateStops = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES));
            String[] ids = busRelateStops.split("/");

            for (int i = 0; i < ids.length; i++) {

                String stopQry = String.format("SELECT * From %s where %s=%s", CommonConstants.CITY_GYEONG_GI._engName + "_route",
                        CommonConstants._ID, ids[i]);

                Cursor stopCursor = mSqliteDb.rawQuery(stopQry, null);
                if (stopCursor.moveToNext()) {
                    BusRouteItem routeItem = new BusRouteItem();

                    routeItem.busRouteApiId = stopCursor.getString(stopCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    routeItem.busType = stopCursor.getInt(stopCursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                    routeItem.busStopApiId = sItem.apiId;
                    routeItem.busStopName = sItem.name;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GYEONG_GI._cityId);
                    localBusRouteItems.add(routeItem);

                }
                stopCursor.close();
            }

        }
        cursor.close();


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);
        depthRouteItem.depthIndex = 1;


        return depthRouteItem;


    }


    @Override
    public DepthRouteItem getDepthLineList(DepthRouteItem sItem) {

        super.getDepthLineList(sItem);

        String stopTmpApiId = null;
        String itemBusType = null;
        String busTmpRouteApiId = null;


        if (sItem != null && sItem.busRouteItem.size() > 0) {
            for (int i = 0; i < sItem.busRouteItem.size(); i++) {
                if (sItem.busRouteItem.get(i).busStopApiId != null) {

                    stopTmpApiId = sItem.busRouteItem.get(i).busStopApiId;

                    String selectSql = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, sItem.busRouteItem.get(i).busType);

                    Cursor cursor = getBusDb().rawQuery(selectSql, null);
                    if (cursor.moveToNext()) {

                        String busType = cursor.getString(cursor.getColumnIndex("busType"));
                        busTmpRouteApiId = sItem.busRouteItem.get(i).busRouteApiId;
                        itemBusType = busType;


                    }

                    cursor.close();
                    break;
                }
            }
        }

        if (stopTmpApiId == null) {

            sItem.depthIndex = 0;
            return sItem;
        } else {

            ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

            String param = null;
            String parseStr = null;

            if (itemBusType.equals("고양마을1")) {
                param = String.format("routeId=&stationName=&mobileNo=&stationId=%s", stopTmpApiId);
                parseStr = RequestCommonFuction.getSource(mStopUrl2, false, param, "utf-8");

            } else if (itemBusType.equals("부천마을1")) {

                parseStr = RequestCommonFuction.getSource("http://www.bcits.go.kr/jsp/popup/station_popup.jsp?STATION_ID=" + stopTmpApiId.substring(2), false, "", "euc-kr");

                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);
                    Elements seqElements = doc.select("table[class=tview tfix]").select("tr");

                    for (int i = 0; i < seqElements.size(); i++) {

                        Elements tdElements = seqElements.get(i).select("strong");

                        String busName = tdElements.get(0).text();

                        ArriveItem arriveItem = null;

                        if (tdElements.size() == 3) {
                            arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainStop = Integer.parseInt(tdElements.get(1).text());
                            arriveItem.remainMin = Integer.parseInt(tdElements.get(2).text());
                        }

                        if (arriveItem != null) {
                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {
                                if (sItem.busRouteItem.get(k).busRouteName != null
                                        && sItem.busRouteItem.get(k).busRouteName.equals(busName)) {

                                    sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    break;

                                }
                            }
                        }
                    }

                }

            } else if (itemBusType.equals("군포마을1")) {

                param = String.format("stationId=%s", stopTmpApiId.substring(2));

                parseStr = RequestCommonFuction.getSource("http://bis.gunpo21.net/mobile/busArrStation.jsp", false, param, "utf-8");

                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);
                    Elements seqElements = doc.select("table[class=box4]");

                    String routeApiId = null;

                    for (int i = 0; i < seqElements.size(); i++) {

                        Elements trElements = seqElements.get(i).select("tr");
                        ArriveItem arriveItem = new ArriveItem();

                        if (trElements.size() == 3) {

                            routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                            if (trElements.get(1).text().trim().contains("약")) {
                                arriveItem.remainMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                            }


                            if (trElements.get(2).text().trim().contains("정류소")) {

                                if (trElements.get(2).text().trim().contains("전전")) {
                                    arriveItem.remainStop = 2;
                                } else if (trElements.get(2).text().trim().contains("전 정류장")) {
                                    arriveItem.remainStop = 1;
                                } else if (trElements.get(2).text().trim().contains("번째")) {
                                    arriveItem.remainStop = Integer.parseInt(trElements.get(2).text().split("번째")[0].trim());
                                }


                            }


                        } else if (trElements.size() == 2) {


                            routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                            if (trElements.get(1).text().trim().contains("약")) {

                                int tmpMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                                arriveItem.remainMin = tmpMin;
                            }

                        }


                        if (routeApiId != null) {
                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(routeApiId)) {

                                    sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    break;

                                }
                            }
                        }


                    }


                }


            } else if (itemBusType.equals("용인마을1")) {

                String[] refrer = {"Referer", "http://www.bcits.go.kr/bcits/mahulbus.html"};
                String header = getHeader("http://bucheon.doppelsoft.com:8000/admn/AdmnMainAction.bis", true, "method=readInfoList&loginType=G", "euc-kr", refrer);


            } else if (itemBusType.equals("의왕마을1")) {


                param = String.format("stationId=%s", stopTmpApiId.substring(2));

                parseStr = RequestCommonFuction.getSource("http://bis.uw21.net/mobile/busArrStation.jsp", false, param, "utf-8");


                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);
                    Elements seqElements = doc.select("table[class=box4]");

                    String routeApiId = null;

                    for (int i = 0; i < seqElements.size(); i++) {

                        Elements trElements = seqElements.get(i).select("tr");
                        ArriveItem arriveItem = new ArriveItem();

                        if (trElements.size() == 3) {

                            routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                            if (trElements.get(1).text().trim().contains("약")) {
                                arriveItem.remainMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                            }


                            if (trElements.get(2).text().trim().contains("정류소")) {

                                if (trElements.get(2).text().trim().contains("전전")) {
                                    arriveItem.remainStop = 2;
                                } else if (trElements.get(2).text().trim().contains("전 정류장")) {
                                    arriveItem.remainStop = 1;
                                } else if (trElements.get(2).text().trim().contains("번째")) {
                                    arriveItem.remainStop = Integer.parseInt(trElements.get(2).text().split("번째")[0].trim());
                                }


                            }


                        } else if (trElements.size() == 2) {


                            routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                            if (trElements.get(1).text().trim().contains("약")) {

                                int tmpMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                                arriveItem.remainMin = tmpMin;
                            }

                        }


                        if (routeApiId != null) {
                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(routeApiId)) {

                                    if (arriveItem.remainMin != 0) {
                                        sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    }

                                    break;

                                }
                            }
                        }


                    }


                }


            } else if (itemBusType.equals("남양주마을1")) {


                param = String.format("stationId=%s&routeid=&nodeco", stopTmpApiId.substring(2));

                parseStr = RequestCommonFuction.getSource("http://bis.nyj.go.kr/BusSearch/m2_popStationInfo2.htm", false, param, "utf-8");

                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);

                    Elements seqElements = doc.select("tbody").select("tr");

                    for (int i = 0; i < seqElements.size(); i++) {
                        Elements tdElements = seqElements.get(i).select("td");

                        BusRouteItem routeItem = new BusRouteItem();
                        routeItem.busRouteName = tdElements.get(0).text();
                        String tmpString = tdElements.get(1).text().trim();

                        if (!tmpString.contains("도착")) {
                            int remainStop = -1, remainMin = -1;
                            try {
                                remainStop = Integer.parseInt(tmpString.split(" ")[0]);
                                remainMin = Integer.parseInt(tmpString.split("\\(")[1].split(" ")[0]);

                                ArriveItem arriveItem = new ArriveItem();
                                arriveItem.remainMin = remainMin;
                                arriveItem.remainStop = remainStop;

                                routeItem.arriveInfo.add(arriveItem);

                                for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                    if (sItem.busRouteItem.get(k).busRouteApiId != null
                                            && sItem.busRouteItem.get(k).busRouteName.equals(tmpString)) {

                                        sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                        break;

                                    }
                                }


                            } catch (Exception e) {

                            }


                        }


                    }


                }


            } else {

                param = String.format("serviceKey=%s&stationId=%s", mKey, stopTmpApiId);
                parseStr = RequestCommonFuction.getSource("http://www.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");

                if (parseStr != null && parseStr.contains("LIMITED")) {
                    param = String.format("serviceKey=%s&stationId=%s", mKey, stopTmpApiId);
                    parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");
                }

                if (parseStr != null && parseStr.contains("LIMITED")) {
                    param = String.format("serviceKey=%s&stationId=%s", "1234567890", stopTmpApiId);
                    parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");
                }


                if (parseStr != null) {

                    Document doc = Jsoup.parse(parseStr);

                    Elements seqElements = doc.select("busArrivalList");

                    for (int i = 0; i < seqElements.size(); i++) {

                        String remainStop1 = seqElements.get(i).select("locationNo1").text();
                        String remainStop2 = seqElements.get(i).select("locationNo2").text();

                        String remainMin1 = seqElements.get(i).select("predictTime1").text();
                        String remainMin2 = seqElements.get(i).select("predictTime2").text();

                        String routeId = seqElements.get(i).select("routeId").text();

                        String stOrd = null;
                        try {
                            stOrd = seqElements.get(i).select("staOrder").text();
                        } catch (Exception e) {
                        }
                        ;

                        if (!remainMin1.equals("")) {
                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = Integer.parseInt(remainMin1);
                            arriveItem.remainStop = Integer.parseInt(remainStop1);
                            arriveItem.state = Constants.STATE_ING;

                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(routeId)) {

                                    sItem.busRouteItem.get(k).stOrd = stOrd;
                                    sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    break;

                                }
                            }


                        } else {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_PREPARE_NOT;

                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(routeId)) {

                                    sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    break;

                                }
                            }

                        }


                        if (!remainMin2.equals("")) {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.remainMin = Integer.parseInt(remainMin2);

                            if (remainStop2.equals("")) {
                                arriveItem.remainStop = 0;
                            } else {
                                arriveItem.remainStop = Integer.parseInt(remainStop2);
                            }


                            arriveItem.state = Constants.STATE_ING;

                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(routeId)) {

                                    if (arriveItem.remainStop != 0) {
                                        sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                        break;
                                    }

                                }
                            }
                        } else {

                            ArriveItem arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_PREPARE_NOT;

                            for (int k = 0; k < sItem.busRouteItem.size(); k++) {

                                if (sItem.busRouteItem.get(k).busRouteApiId != null
                                        && sItem.busRouteItem.get(k).busRouteApiId.equals(routeId)) {

                                    sItem.busRouteItem.get(k).arriveInfo.add(arriveItem);
                                    break;

                                }
                            }
                        }
                    }

                }

            }

        }


        sItem.depthIndex = 0;

        return sItem;
    }


    @Override
    public DepthRouteItem getAlarmList(String... idStr) {


        String busStopApiId, busRouteApiId, busStopInnerId = null, ord = null;

        busStopApiId = idStr[0];
        busRouteApiId = idStr[1];


        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        String param = String.format("serviceKey=%s&&stationId=%s", mKey, busStopApiId);

        String parseStr = RequestCommonFuction.getSource(mLineUrl, false, param, "utf-8");

        if (parseStr == null) {
            param = String.format("serviceKey=%s&&stationId=%s", "1234567890", busStopApiId);
            parseStr = RequestCommonFuction.getSource(mLineUrl, false, param, "utf-8");
        }

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);

            Elements seqElements = doc.select("busArrivalList");

            for (int i = 0; i < seqElements.size(); i++) {

                String routeApiId = seqElements.get(i).select("routeId").text();
                String arrmsg1 = seqElements.get(i).select("predictTime1").text();
                String arrmsg2 = seqElements.get(i).select("predictTime2").text();
                String plain1 = seqElements.get(i).select("plateNo1").text();
                String plain2 = seqElements.get(i).select("plateNo2").text();


                if (routeApiId.equals(busRouteApiId)) {
                    BusRouteItem item = new BusRouteItem();
                    item.busAlarmDivideName = plain1;
                    item.busRouteApiId = busRouteApiId;
                    item.busRouteApiId2 = "";
                    item.busRouteSubName = "";
                    item.stopApiOfRoute = busStopApiId;
                    localBusRouteItems.add(item);
                    BusRouteItem item2 = new BusRouteItem();
                    item2.busAlarmDivideName = plain2;
                    item2.busRouteApiId = busRouteApiId;
                    item2.busRouteApiId2 = "";
                    item2.busRouteSubName = "";
                    item2.stopApiOfRoute = busStopApiId;
                    localBusRouteItems.add(item2);


                    DepthRouteItem depthRouteItem = new DepthRouteItem();
                    depthRouteItem.busRouteItem.addAll(localBusRouteItems);

                    return depthRouteItem;
                }
            }
        }


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;
    }


    @Override
    public DepthRouteItem getLineDetailList(
            BusRouteItem busRouteItems) {

        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        String param = String.format("routeId=%s&stationId=%s", busRouteItems.busRouteApiId, busRouteItems.busStopApiId);
        String parseStr = RequestCommonFuction.getSource("http://m.gbis.go.kr/jsp/routeInfoArrival.jsp", false, param, "utf-8");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            Elements seqElements = doc.select("ul");

            for (int i = 0; i < seqElements.size(); i++) {

                Elements liTags = seqElements.get(i).select("li");
                if (liTags.size() == 2) {
                    for (int j = 0; j < liTags.size(); j++) {

                        Elements spanTag = liTags.get(j).select("span");
                        if (spanTag.size() != 2) continue;

                        String busUniqeNum = spanTag.get(0).text();
                        String busPartInfor = spanTag.get(1).text();

                        if (busPartInfor.contains("/")) {
                            ArriveItem item = new ArriveItem();
                            if (busUniqeNum != null && busUniqeNum.trim().length() > 0) {
                                item.plainNum = busUniqeNum.substring(1, busUniqeNum.length() - 1);
                            }

                            String[] inforSplit = busPartInfor.split("/");
                            int remainStop = Integer.parseInt(inforSplit[0].split("번")[0]);
                            int remainMin = Integer.parseInt(inforSplit[1].split("약")[1].trim().split("분")[0]);

                            item.remainMin = remainMin;
                            item.remainStop = remainStop;
                            item.state = Constants.STATE_ING;

                            busRouteItems.arriveInfo.add(item);

                        } else if (busPartInfor.contains("도착 예정 버스가")) {
                            ArriveItem item = new ArriveItem();
                            item.state = Constants.STATE_PREPARE_NOT;

                            busRouteItems.arriveInfo.add(item);
                        }
                    }
                }
            }


            busRouteItems.plusParsingNeed = 0;
            localBusRouteItems.add(busRouteItems);

        }
        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;
    }


    @Override
    public DepthFavoriteItem getDepthRefreshList(DepthFavoriteItem sItem) {

        super.getDepthRefreshList(sItem);
        FavoriteAndHistoryItem favoriteAndHistoryItem = sItem.favoriteAndHistoryItems.get(0);


        String strBusType = null;
        String strBusTypeName = null;

        String strTypeSql = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.CITY_GYEONG_GI._engName + "_Route", CommonConstants.BUS_ROUTE_ID1, favoriteAndHistoryItem.busRouteItem.busRouteApiId);
        Cursor cursorType = getBusDb().rawQuery(strTypeSql, null);
        if (cursorType.moveToNext()) {
            strBusType = cursorType.getString(cursorType.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
        }
        cursorType.close();

        String selectSql = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, strBusType);

        Cursor cursor = getBusDb().rawQuery(selectSql, null);
        if (cursor.moveToNext()) {
            strBusTypeName = cursor.getString(cursor.getColumnIndex("busType"));
        }

        cursor.close();


        if (strBusTypeName.equals("부천마을1")) {


            String parseStr = RequestCommonFuction.getSource("http://www.bcits.go.kr/jsp/popup/station_popup.jsp?STATION_ID=" + favoriteAndHistoryItem.busRouteItem.busStopApiId.substring(2), false, "", "euc-kr");


            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("table[class=tview tfix]").select("tr");


                for (int i = 0; i < seqElements.size(); i++) {

                    Elements tdElements = seqElements.get(i).select("strong");

                    String busName = tdElements.get(0).text();

                    ArriveItem arriveItem = null;

                    if (tdElements.size() == 3) {

                        if (favoriteAndHistoryItem.busRouteItem.busRouteName.equals(busName)) {
                            arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainStop = Integer.parseInt(tdElements.get(1).text());
                            arriveItem.remainMin = Integer.parseInt(tdElements.get(2).text());
                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                        }
                    }


                }

            }


        } else if (strBusTypeName.equals("의왕마을1")) {

            String param = String.format("stationId=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId.substring(2));

            String parseStr = RequestCommonFuction.getSource("http://bis.uw21.net/mobile/busArrStation.jsp", false, param, "utf-8");


            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("table[class=box4]");

                String routeApiId = null;

                for (int i = 0; i < seqElements.size(); i++) {

                    Elements trElements = seqElements.get(i).select("tr");
                    ArriveItem arriveItem = new ArriveItem();

                    if (trElements.size() == 3) {

                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {
                            arriveItem.remainMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                        }


                        if (trElements.get(2).text().trim().contains("정류소")) {

                            if (trElements.get(2).text().trim().contains("전전")) {
                                arriveItem.remainStop = 2;
                            } else if (trElements.get(2).text().trim().contains("전 정류장")) {
                                arriveItem.remainStop = 1;
                            } else if (trElements.get(2).text().trim().contains("번째")) {
                                arriveItem.remainStop = Integer.parseInt(trElements.get(2).text().split("번째")[0].trim());
                            }

                            if (routeApiId != null && arriveItem.remainMin != 0) {


                                if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeApiId)) {
                                    arriveItem.state = Constants.STATE_ING;
                                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                                }
                            }


                        }


                    } else if (trElements.size() == 2) {


                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {

                            int tmpMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainMin = tmpMin;


                            if (routeApiId != null && arriveItem.remainMin != 0) {

                                if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeApiId)) {

                                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                                }
                            }
                        }


                    }


                }


            }


        } else if (strBusTypeName.equals("군포마을1")) {

            String param = String.format("stationId=%s", favoriteAndHistoryItem.busRouteItem.busStopApiId.substring(2));

            String parseStr = RequestCommonFuction.getSource("http://bis.gunpo21.net/mobile/busArrStation.jsp", false, param, "utf-8");


            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("table[class=box4]");

                String routeApiId = null;

                for (int i = 0; i < seqElements.size(); i++) {

                    Elements trElements = seqElements.get(i).select("tr");
                    ArriveItem arriveItem = new ArriveItem();

                    if (trElements.size() == 3) {

                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {
                            arriveItem.remainMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                        }


                        if (trElements.get(2).text().trim().contains("정류소")) {

                            if (trElements.get(2).text().trim().contains("전전")) {
                                arriveItem.remainStop = 2;
                            } else if (trElements.get(2).text().trim().contains("전 정류장")) {
                                arriveItem.remainStop = 1;
                            } else if (trElements.get(2).text().trim().contains("번째")) {
                                arriveItem.remainStop = Integer.parseInt(trElements.get(2).text().split("번째")[0].trim());
                            }


                        }

                        if (routeApiId != null) {

                            if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeApiId)) {
                                arriveItem.state = Constants.STATE_ING;
                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            }

                        }


                    } else if (trElements.size() == 2) {


                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {

                            int tmpMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                            arriveItem.remainMin = tmpMin;
                        }

                        if (routeApiId != null) {

                            if (favoriteAndHistoryItem.busRouteItem.busRouteApiId.equals(routeApiId)) {
                                arriveItem.state = Constants.STATE_ING;
                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            }
                        }

                    }
                }
            }


        } else {


            String param = String.format("serviceKey=%s&stationId=%s", mKey, favoriteAndHistoryItem.busRouteItem.busStopApiId);
            String parseStr = RequestCommonFuction.getSource("http://www.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");


            if (parseStr != null && parseStr.contains("LIMITED")) {
                param = String.format("serviceKey=%s&stationId=%s", mKey, favoriteAndHistoryItem.busRouteItem.busStopApiId);
                parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");
            }

            if (parseStr != null && parseStr.contains("LIMITED")) {
                param = String.format("serviceKey=%s&stationId=%s", "1234567890", favoriteAndHistoryItem.busRouteItem.busStopApiId);
                parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");
            }


            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);

                Elements seqElements = doc.select("busArrivalList");

                for (int i = 0; i < seqElements.size(); i++) {

                    String remainStop1 = seqElements.get(i).select("locationNo1").text();
                    String remainStop2 = seqElements.get(i).select("locationNo2").text();

                    String remainMin1 = seqElements.get(i).select("predictTime1").text();
                    String remainMin2 = seqElements.get(i).select("predictTime2").text();

                    String routeId = seqElements.get(i).select("routeId").text();


                    if (!remainMin1.equals("")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(remainMin1);
                        arriveItem.remainStop = Integer.parseInt(remainStop1);
                        arriveItem.state = Constants.STATE_ING;


                        if (sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId != null
                                && sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId.equals(routeId)) {


                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                        }


                    } else {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                        if (sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId != null
                                && sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId.equals(routeId)) {


                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                        }

                    }


                    if (!remainMin2.equals("")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(remainMin2);


                        if (remainStop2.equals("")) {
                            arriveItem.remainStop = 0;
                        } else {
                            arriveItem.remainStop = Integer.parseInt(remainStop2);
                        }
                        arriveItem.state = Constants.STATE_ING;

                        if (sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId != null
                                && sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId.equals(routeId)) {

                            if (arriveItem.remainStop != 0) {
                                sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                            }

                        }
                    } else {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                        if (sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId != null
                                && sItem.favoriteAndHistoryItems.get(0).busRouteItem.busRouteApiId.equals(routeId)) {

                            sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);

                        }

                    }


                }


            }


        }


        return sItem;
    }


    @Override
    public DepthStopItem getStopDetailList(
            BusStopItem busStopItems) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        String strBusType = null;
        String strBusTypeName = null;

        String strTypeSql = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.CITY_GYEONG_GI._engName + "_Route", CommonConstants.BUS_ROUTE_ID1, sItem.busRouteApiId);
        Cursor cursorType = getBusDb().rawQuery(strTypeSql, null);
        if (cursorType.moveToNext()) {
            strBusType = cursorType.getString(cursorType.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
        }
        cursorType.close();

        String selectSql = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, strBusType);

        Cursor cursor = getBusDb().rawQuery(selectSql, null);
        if (cursor.moveToNext()) {
            strBusTypeName = cursor.getString(cursor.getColumnIndex("busType"));
        }

        cursor.close();


        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        if (strBusTypeName.equals("부천마을1")) {

            String parseStr = RequestCommonFuction.getSource("http://www.bcits.go.kr/jsp/popup/station_popup.jsp?STATION_ID=" + sItem.busStopApiId.substring(2), false, "", "euc-kr");

            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("table[class=tview tfix]").select("tr");

                for (int i = 0; i < seqElements.size(); i++) {

                    Elements tdElements = seqElements.get(i).select("strong");

                    String busName = tdElements.get(0).text();

                    if (tdElements.size() == 3) {

                        ArriveItem arriveItem = null;

                        if (sItem.busRouteName.equals(busName)) {
                            arriveItem = new ArriveItem();
                            arriveItem.state = Constants.STATE_ING;
                            arriveItem.remainStop = Integer.parseInt(tdElements.get(1).text());
                            arriveItem.remainMin = Integer.parseInt(tdElements.get(2).text());
                            arriveItems.add(arriveItem);
                        }


                    }

                }

            }

        } else if (strBusTypeName.equals("군포마을1")) {
            String param = String.format("stationId=%s", sItem.busStopApiId.substring(2));

            String parseStr = RequestCommonFuction.getSource("http://bis.gunpo21.net/mobile/busArrStation.jsp", false, param, "utf-8");


            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("table[class=box4]");

                String routeApiId = null;

                for (int i = 0; i < seqElements.size(); i++) {

                    Elements trElements = seqElements.get(i).select("tr");
                    ArriveItem arriveItem = new ArriveItem();

                    if (trElements.size() == 3) {

                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {
                            arriveItem.remainMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                        }


                        if (trElements.get(2).text().trim().contains("정류소")) {

                            if (trElements.get(2).text().trim().contains("전전")) {
                                arriveItem.remainStop = 2;
                            } else if (trElements.get(2).text().trim().contains("전 정류장")) {
                                arriveItem.remainStop = 1;
                            } else if (trElements.get(2).text().trim().contains("번째")) {
                                arriveItem.remainStop = Integer.parseInt(trElements.get(2).text().split("번째")[0].trim());
                            }


                        }

                        if (routeApiId != null) {


                            if (sItem.busRouteApiId.equals(routeApiId.trim())) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItems.add(arriveItem);
                            }
                        }


                    } else if (trElements.size() == 2) {


                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {

                            int tmpMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                            arriveItem.remainMin = tmpMin;
                        }

                        if (routeApiId != null) {


                            if (sItem.busRouteApiId.equals(routeApiId.trim())) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItems.add(arriveItem);
                            }
                        }

                    }


                }


            }

        } else if (strBusTypeName.equals("의왕마을1")) {
            String param = String.format("stationId=%s", sItem.busStopApiId.substring(2));

            String parseStr = RequestCommonFuction.getSource("http://bis.uw21.net/mobile/busArrStation.jsp", false, param, "utf-8");


            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);
                Elements seqElements = doc.select("table[class=box4]");

                String routeApiId = null;

                for (int i = 0; i < seqElements.size(); i++) {

                    Elements trElements = seqElements.get(i).select("tr");
                    ArriveItem arriveItem = new ArriveItem();


                    if (trElements.size() == 3) {

                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {
                            arriveItem.remainMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                        }


                        if (trElements.get(2).text().trim().contains("정류소")) {

                            if (trElements.get(2).text().trim().contains("전전")) {
                                arriveItem.remainStop = 2;
                            } else if (trElements.get(2).text().trim().contains("전 정류장")) {
                                arriveItem.remainStop = 1;
                            } else if (trElements.get(2).text().trim().contains("번째")) {
                                arriveItem.remainStop = Integer.parseInt(trElements.get(2).text().split("번째")[0].trim());
                            }


                        }

                        if (routeApiId != null) {


                            if (sItem.busRouteApiId.equals(routeApiId.trim())) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItems.add(arriveItem);
                            }
                        }


                    } else if (trElements.size() == 2) {


                        routeApiId = trElements.get(0).select("a").attr("href").split("routeId=")[1];

                        if (trElements.get(1).text().trim().contains("약")) {

                            int tmpMin = Integer.parseInt(trElements.get(1).text().split(" ")[1].split("분")[0]);
                            arriveItem.remainMin = tmpMin;
                        }

                        if (routeApiId != null) {


                            if (sItem.busRouteApiId.equals(routeApiId.trim())) {
                                arriveItem.state = Constants.STATE_ING;
                                arriveItems.add(arriveItem);
                            }
                        }

                    }


                }


            }
        } else {


            String param = String.format("serviceKey=%s&stationId=%s", mKey, sItem.busStopApiId);
            String parseStr = RequestCommonFuction.getSource("http://www.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");

            if (parseStr != null && parseStr.contains("LIMITED")) {
                param = String.format("serviceKey=%s&stationId=%s", mKey, sItem.busStopApiId);
                parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");
            }

            if (parseStr != null && parseStr.contains("LIMITED")) {
                param = String.format("serviceKey=%s&stationId=%s", "1234567890", sItem.busStopApiId);
                parseStr = RequestCommonFuction.getSource("http://openapi.gbis.go.kr/ws/rest/busarrivalservice/station", false, param, "utf-8");
            }



            if (parseStr != null) {

                Document doc = Jsoup.parse(parseStr);

                Elements seqElements = doc.select("busArrivalList");

                for (int i = 0; i < seqElements.size(); i++) {

                    String remainStop1 = seqElements.get(i).select("locationNo1").text();
                    String remainStop2 = seqElements.get(i).select("locationNo2").text();

                    String remainMin1 = seqElements.get(i).select("predictTime1").text();
                    String remainMin2 = seqElements.get(i).select("predictTime2").text();

                    String routeId = seqElements.get(i).select("routeId").text();


                    if (!remainMin1.equals("")) {
                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(remainMin1);
                        arriveItem.remainStop = Integer.parseInt(remainStop1);
                        arriveItem.state = Constants.STATE_ING;


                        if (sItem.busRouteApiId != null
                                && sItem.busRouteApiId.equals(routeId)) {
                            arriveItems.add(arriveItem);
                        }


                    } else {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                        if (sItem.busRouteApiId != null
                                && sItem.busRouteApiId.equals(routeId)) {


                            arriveItems.add(arriveItem);

                        }

                    }


                    if (!remainMin2.equals("")) {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.remainMin = Integer.parseInt(remainMin2);


                        if (remainStop2.equals("")) {
                            arriveItem.remainStop = 0;
                        } else {
                            arriveItem.remainStop = Integer.parseInt(remainStop2);
                        }
                        arriveItem.state = Constants.STATE_ING;

                        if (sItem.busRouteApiId != null
                                && sItem.busRouteApiId.equals(routeId)) {

                            if (arriveItem.remainStop != 0) {
                                arriveItems.add(arriveItem);
                            }

                        }
                    } else {

                        ArriveItem arriveItem = new ArriveItem();
                        arriveItem.state = Constants.STATE_PREPARE_NOT;

                        if (sItem.busRouteApiId != null
                                && sItem.equals(routeId)) {


                            arriveItems.add(arriveItem);

                        }

                    }

                }

            }

        }


        DepthAlarmItem depthAlarmItem = new DepthAlarmItem();
        depthAlarmItem.busAlarmItem.addAll(arriveItems);


        return depthAlarmItem;
    }


    public String getHeader(String urlString, boolean isPost, String valuePair, String encodingStyle, String[] referer) {

        String result = null;
        boolean isPass = false;

        HttpURLConnection cnx = null;

        try {

            if (!isPost) {
                if (!valuePair.trim().equals("")) {
                    urlString = urlString + "?" + valuePair;
                }
            }

            URL url = new URL(urlString);
            cnx = (HttpURLConnection) url.openConnection();
            cnx.setConnectTimeout(6000);
            cnx.setDoOutput(true);
            cnx.setDoInput(true);
            cnx.setUseCaches(true);


            cnx.setRequestProperty("content-type", "application/x-www-form-urlencoded");

            if (referer.length > 0) {
                String tmpKey = null;
                for (int i = 0; i < referer.length; i++) {
                    if (i % 2 == 0) {
                        tmpKey = referer[i].toString();
                    } else {
                        cnx.setRequestProperty(tmpKey, referer[i]);
                    }
                }
            }


            if (isPost) {
                cnx.setRequestMethod("POST");

                if (cnx.getOutputStream() == null)
                    return null;
                OutputStreamWriter outStream = new OutputStreamWriter(cnx.getOutputStream(), encodingStyle);
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(valuePair);
                writer.flush();
            } else {
                cnx.setRequestMethod("GET");
            }


            Map headerfields = cnx.getHeaderFields();

            String cookie = String.valueOf(headerfields.get("Set-Cookie"));
            return cookie.split(";")[0].substring(1);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {


            if (cnx != null) {
                cnx.disconnect();
            }
        }

        return result;

    }

}

