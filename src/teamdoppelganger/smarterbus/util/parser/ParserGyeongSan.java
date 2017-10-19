package teamdoppelganger.smarterbus.util.parser;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

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

public class ParserGyeongSan extends CommonParser {


    public ParserGyeongSan(SQLiteDatabase db) {
        super(db);
        // TODO Auto-generated constructor stub
    }

    @Override
    public DepthStopItem getStopList(BusRouteItem rItem) {
        ArrayList<BusStopItem> busStopItem = new ArrayList<BusStopItem>();

        String url = String.format("http://bis.gbgs.go.kr/bs/buslineinfo/svc/getBusstopList.jsp");
        String param = String.format("busLineID=%s", rItem.busRouteApiId);

        String parseStr = RequestCommonFuction.getSource(url, true, param, "utf-8");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);

            //정방향
            Elements ddTag = doc.select("div#forwardPanel").select("td[class=col2]");

            int tmpCount = 1;
            HashMap<Integer, BusStopItem> mTempHash = new HashMap<Integer, BusStopItem>();
            for (int j = 0; j < ddTag.size(); j++) {

                if (ddTag.get(j).select("img").size() == 0) {

                    BusStopItem stopItem = new BusStopItem();
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GYEONG_SAN._cityId);
                    String text = ddTag.get(j).text().trim();


                    if (text.contains("[")) {
                        stopItem.isExist = true;
                        stopItem.name = text.split("]")[1].trim();
                    } else {
                        stopItem.name = text.trim();
                    }

                    if (text.length() > 0) {
                        if (tmpCount < 11) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 21) {
                            mTempHash.put((31 - tmpCount), stopItem);
                        } else if (tmpCount < 31) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 41) {
                            mTempHash.put((51 + 30 - tmpCount), stopItem);
                        } else if (tmpCount < 51) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 61) {
                            mTempHash.put((71 + 50 - tmpCount), stopItem);
                        } else if (tmpCount < 71) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 81) {
                            mTempHash.put((91 + 70 - tmpCount), stopItem);
                        } else if (tmpCount < 91) {
                            mTempHash.put(tmpCount, stopItem);
                        }
                    }
                    tmpCount++;
                }
            }

            for (int j = 1; j < 100; j++) {

                BusStopItem stopItem = mTempHash.get(j);
                if (stopItem != null) {
                    busStopItem.add(stopItem);
                }

            }


            ddTag = doc.select("div#backwardPanel").select("td[class=col2]");

            tmpCount = 1;
            mTempHash.clear();

            for (int j = 0; j < ddTag.size(); j++) {

                if (ddTag.get(j).select("img").size() == 0) {

                    BusStopItem stopItem = new BusStopItem();
                    stopItem.localInfoId = String.valueOf(CommonConstants.CITY_GYEONG_SAN._cityId);
                    String text = ddTag.get(j).text().trim();
                    if (text.contains("[")) {
                        stopItem.isExist = true;
                        stopItem.name = text.split("]")[1].trim();
                    } else {
                        stopItem.name = text.trim();
                    }

                    if (text.length() > 0) {
                        if (tmpCount < 11) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 21) {
                            mTempHash.put((31 - tmpCount), stopItem);
                        } else if (tmpCount < 31) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 41) {
                            mTempHash.put((51 + 30 - tmpCount), stopItem);
                        } else if (tmpCount < 51) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 61) {
                            mTempHash.put((71 + 50 - tmpCount), stopItem);
                        } else if (tmpCount < 71) {
                            mTempHash.put(tmpCount, stopItem);
                        } else if (tmpCount < 81) {
                            mTempHash.put((91 + 70 - tmpCount), stopItem);
                        } else if (tmpCount < 91) {
                            mTempHash.put(tmpCount, stopItem);
                        }
                    }

                    tmpCount++;
                }
            }

            for (int j = 1; j < 100; j++) {

                BusStopItem stopItem = mTempHash.get(j);
                if (stopItem != null) {
                    busStopItem.add(stopItem);
                }


            }

        }


        DepthStopItem depthStopItem = new DepthStopItem();
        depthStopItem.busStopItem.addAll(busStopItem);

        return depthStopItem;
    }

    @Override
    public DepthRouteItem getLineList(BusStopItem sItem) {
        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();


        String url = String.format("http://bis.gbgs.go.kr/bp/realInfo/src/getRouteList2.jsp");
        String param = String.format("menu=2&busStopId=%s&busStopNm=", sItem.apiId);

        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("a");

            for (int i = 0; i < ddTag.size(); i++) {
                if (ddTag.get(i).attr("href").contains("goRealInfo")) {
                    String[] splitInfor = ddTag.get(i).attr("href").split("goRealInfo\\(")[1].replace("\"", "").split(",");
                    String apiId = splitInfor[0];
                    String name = splitInfor[1];

                    BusRouteItem routeItem = new BusRouteItem();
                    routeItem.busRouteName = name;
                    routeItem.busRouteApiId = apiId;
                    routeItem.plusParsingNeed = 1;
                    routeItem.localInfoId = String.valueOf(CommonConstants.CITY_GYEONG_SAN._cityId);
                    localBusRouteItems.add(routeItem);

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
    public DepthRouteItem getLineDetailList(BusRouteItem busRouteItem) {
        ArrayList<BusRouteItem> localBusRouteItems = new ArrayList<BusRouteItem>();

        String url = "http://bis.gbgs.go.kr/bs/busgis/svc/getArrivalTable.jsp";
        String param = String.format("busLineID=%s&busLineNo=&busStopID=%s&busStopName=&bDraw=0", busRouteItem.busRouteApiId, busRouteItem.busStopApiId);
        

        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tbody");

            for (int i = 0; i < ddTag.size(); i++) {
                Elements col4 = ddTag.get(i).select("td[class=body_col4]");

                if (col4 != null && col4.size() == 3) {
                    String remainStop = col4.get(1).text().split(" ")[0];
                    String remainMin = col4.get(2).text().split(" ")[0];

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(remainMin);
                    arriveItem.remainStop = Integer.parseInt(remainStop);
                    arriveItem.state = Constants.STATE_ING;

                    busRouteItem.arriveInfo.add(arriveItem);
                }


            }

            localBusRouteItems.add(busRouteItem);

        }

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItems);

        return depthRouteItem;
    }

    @Override
    public DepthStopItem getStopDetailList(BusStopItem busStopItem) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public DepthAlarmItem getDepthAlarmList(BusRouteItem sItem) {

        ArrayList<ArriveItem> arriveItems = new ArrayList<ArriveItem>();


        String url = "http://bis.gbgs.go.kr/bs/busgis/svc/getArrivalTable.jsp";
        String param = String.format("busLineID=%s&busLineNo=&busStopID=%s&busStopName=&bDraw=0", sItem.busRouteApiId, sItem.busStopApiId);


        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");

        if (parseStr != null) {
            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tbody");

            for (int i = 0; i < ddTag.size(); i++) {
                Elements col4 = ddTag.get(i).select("td[class=body_col4]");

                if (col4 != null && col4.size() == 3) {
                    String remainStop = col4.get(1).text().split(" ")[0];
                    String remainMin = col4.get(2).text().split(" ")[0];

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(remainMin);
                    arriveItem.remainStop = Integer.parseInt(remainStop);
                    arriveItem.state = Constants.STATE_ING;

                    arriveItems.add(arriveItem);
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

        String url = "http://bis.gbgs.go.kr/bs/busgis/svc/getArrivalTable.jsp";
        String param = String.format("busLineID=%s&busLineNo=&busStopID=%s&busStopName=&bDraw=0", favoriteAndHistoryItem.busRouteItem.busRouteApiId, favoriteAndHistoryItem.busRouteItem.busStopApiId);


        String parseStr = RequestCommonFuction.getSource(url, true, param, "euc-kr");

        if (parseStr != null) {

            Document doc = Jsoup.parse(parseStr);
            Elements ddTag = doc.select("tbody");

            for (int i = 0; i < ddTag.size(); i++) {
                Elements col4 = ddTag.get(i).select("td[class=body_col4]");

                if (col4 != null && col4.size() == 3) {
                    String remainStop = col4.get(1).text().split(" ")[0];
                    String remainMin = col4.get(2).text().split(" ")[0];

                    ArriveItem arriveItem = new ArriveItem();
                    arriveItem.remainMin = Integer.parseInt(remainMin);
                    arriveItem.remainStop = Integer.parseInt(remainStop);
                    arriveItem.state = Constants.STATE_ING;

                    sItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo.add(arriveItem);
                }
            }
        }


        return sItem;
    }
}
