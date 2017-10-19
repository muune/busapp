package teamdoppelganger.smarterbus.util.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.CommonItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.util.parser.CommonParser;
import teamdoppelganger.smarterbus.util.parser.ParserAsan;
import teamdoppelganger.smarterbus.util.parser.ParserBoseong;
import teamdoppelganger.smarterbus.util.parser.ParserBusan;
import teamdoppelganger.smarterbus.util.parser.ParserChangwon;
import teamdoppelganger.smarterbus.util.parser.ParserCheonan;
import teamdoppelganger.smarterbus.util.parser.ParserChilGok;
import teamdoppelganger.smarterbus.util.parser.ParserChunChan;
import teamdoppelganger.smarterbus.util.parser.ParserChung;
import teamdoppelganger.smarterbus.util.parser.ParserChunggu;
import teamdoppelganger.smarterbus.util.parser.ParserDaegu;
import teamdoppelganger.smarterbus.util.parser.ParserDaejeon;
import teamdoppelganger.smarterbus.util.parser.ParserGangNeung;
import teamdoppelganger.smarterbus.util.parser.ParserGeoJe;
import teamdoppelganger.smarterbus.util.parser.ParserGimCheon;
import teamdoppelganger.smarterbus.util.parser.ParserGimhae;
import teamdoppelganger.smarterbus.util.parser.ParserGongju;
import teamdoppelganger.smarterbus.util.parser.ParserGumi;
import teamdoppelganger.smarterbus.util.parser.ParserGunggi;
import teamdoppelganger.smarterbus.util.parser.ParserGunsan;
import teamdoppelganger.smarterbus.util.parser.ParserGwangju;
import teamdoppelganger.smarterbus.util.parser.ParserGwangyang;
import teamdoppelganger.smarterbus.util.parser.ParserGyeongSan;
import teamdoppelganger.smarterbus.util.parser.ParserGyeongJu;
import teamdoppelganger.smarterbus.util.parser.ParserHawsun;
import teamdoppelganger.smarterbus.util.parser.ParserIncheon;
import teamdoppelganger.smarterbus.util.parser.ParserJeCheon;
import teamdoppelganger.smarterbus.util.parser.ParserJeju;
import teamdoppelganger.smarterbus.util.parser.ParserJeonju;
import teamdoppelganger.smarterbus.util.parser.ParserJinju;
import teamdoppelganger.smarterbus.util.parser.ParserMiryang;
import teamdoppelganger.smarterbus.util.parser.ParserMokop;
import teamdoppelganger.smarterbus.util.parser.ParserPohang;
import teamdoppelganger.smarterbus.util.parser.ParserSejong;
import teamdoppelganger.smarterbus.util.parser.ParserSeoul;
import teamdoppelganger.smarterbus.util.parser.ParserSuncheon;
import teamdoppelganger.smarterbus.util.parser.ParserTongyeong;
import teamdoppelganger.smarterbus.util.parser.ParserUlsan;
import teamdoppelganger.smarterbus.util.parser.ParserWonju;
import teamdoppelganger.smarterbus.util.parser.ParserYangsan;
import teamdoppelganger.smarterbus.util.parser.ParserYeosu;
import teamdoppelganger.smarterbus.util.parser.ParserDamyang;
import teamdoppelganger.smarterbus.util.parser.ParserJangseng;
import teamdoppelganger.smarterbus.util.parser.ParserNaju;
import teamdoppelganger.smarterbus.util.parser.ParserYeongam;
import teamdoppelganger.smarterbus.util.parser.ParserMuan;
import teamdoppelganger.smarterbus.util.parser.ParserShinan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.smart.lib.CommonConstants;

public class GetData {

    CommonParser mCommonParser;
    GetDataListener mGetDataListener;
    SQLiteDatabase mSqliteDatabase;
    int mParsingType = 0;

    HashMap<Integer, String> mHashLocationEng;

    public HashMap<GetDataAsync, GetDataAsync> mHashMap;

    public interface GetDataListener {
        public void onCompleted(int type, DepthItem item);
    }

    public GetData(GetDataListener listener, SQLiteDatabase db,
                   HashMap<Integer, String> hashEng) {
        mGetDataListener = listener;
        mSqliteDatabase = db;
        mHashLocationEng = hashEng;

        mHashMap = new HashMap<GetData.GetDataAsync, GetData.GetDataAsync>();
    }


    public GetData(GetDataListener listener, SQLiteDatabase db) {
        mGetDataListener = listener;
        mSqliteDatabase = db;

        mHashMap = new HashMap<GetData.GetDataAsync, GetData.GetDataAsync>();
    }


    public void startBusRouteParsing(BusStopItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_LINE_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);

    }

    public void startBusStopParsing(BusRouteItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_STOP_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);

    }

    public void depthBusRouteParsing(DepthRouteItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_LINE_DEPTH_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);

    }

    public void depthBusStopParsing(DepthStopItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_STOP_DEPTH_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);

    }

    // 순서 : String apiId, String apiId2, String location, String ord
    /*
	 * public void startAlarmListParsing(String ...value){ GetDataAsync
	 * getDataAsync = new GetDataAsync();
	 * getDataAsync.setTypeParsing(Constants.PARSER_ALALM_TYPE);
	 * getDataAsync.execute(value); }
	 */

    public void startBusRouteDetailParsing(BusRouteItem busRoueItem) {
		/*
		 * GetDataDetailAsync getDataDetailAsync = new GetDataDetailAsync();
		 * getDataDetailAsync.setTypeParsing(Constants.PARSER_LINE_DETAIL_TYPE);
		 * getDataDetailAsync.execute(busRoueItem);
		 */
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_LINE_DETAIL_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(busRoueItem);
    }

    public void startBusStopDetailParsing(BusStopItem busStopItem) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_STOP_DETAIL_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(busStopItem);
    }

    public void startRelateRoute(BusStopItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_RELATE_ROUTE_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);
    }

    public void startNextStop(BusRouteItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_NEXT_STOP);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);
    }

    public void startOneRefrshService(DepthFavoriteItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_REFRSH_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);
    }

    public void startAlarmService(BusRouteItem item) {
        GetDataAsync getDataAsync = new GetDataAsync();
        getDataAsync.setTypeParsing(Constants.PARSER_ALALM_TYPE);
        mHashMap.put(getDataAsync, getDataAsync);
        getDataAsync.execute(item);
    }

    /**
     * String 첫번째 파라미터는
     *
     * @author DOPPELSOFT4
     */
    class GetDataAsync extends AsyncTask<CommonItem, CommonItem, Void> {

        int _parsingType = 0;

        // ArrayList<?> _Item;
        DepthItem _Item;

        protected void setTypeParsing(int type) {
            _parsingType = type;
        }

        @Override
        protected Void doInBackground(CommonItem... params) {

            if (!isCancelled()) {
                String location = "";
                try {
                    if (params[0] instanceof BusStopItem) {

                        location = ((BusStopItem) params[0]).localInfoId;

                    } else if (params[0] instanceof BusRouteItem) {

                        location = ((BusRouteItem) params[0]).localInfoId;

                        if (location.equals("103") && ((BusRouteItem) params[0]).busRouteSubName.equals("경기")) {
                            location = "102";
                        } else if (location.equals("102") && ((BusRouteItem) params[0]).busRouteSubName.equals("인천")) {
                            location = "103";
                        }

                        ((BusRouteItem) params[0]).localInfoId = location;

                    } else if (params[0] instanceof DepthStopItem) {

                        location = ((DepthStopItem) params[0]).busStopItem.get(0).localInfoId;

                    } else if (params[0] instanceof DepthRouteItem) {

                        location = ((DepthRouteItem) params[0]).busRouteItem.get(0).localInfoId;

                    } else if (params[0] instanceof DepthFavoriteItem) {

                        location = ((DepthFavoriteItem) params[0]).favoriteAndHistoryItems.get(0).busRouteItem.localInfoId;

                    }

                    CommonParser commonParser = getParser(mHashLocationEng.get(Integer.parseInt(location)));
                    if (_parsingType == Constants.PARSER_LINE_TYPE) {

                        _Item = commonParser.getLineList((BusStopItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_STOP_TYPE) {  //노선 정보 보기

                        _Item = commonParser.getStopList((BusRouteItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_ALALM_TYPE) {

                        _Item = commonParser.getDepthAlarmList((BusRouteItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_LINE_DETAIL_TYPE) {

                        _Item = commonParser.getLineDetailList((BusRouteItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_STOP_DETAIL_TYPE) {

                        _Item = commonParser.getStopDetailList((BusStopItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_LINE_DEPTH_TYPE) {

                        _Item = commonParser.getDepthLineList((DepthRouteItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_STOP_DEPTH_TYPE) {

                        _Item = commonParser.getDepthStopList((DepthStopItem) params[0]);

                    } else if (_parsingType == Constants.PARSER_RELATE_ROUTE_TYPE) {

                        _Item = getRelateRoutes((BusStopItem) params[0], mSqliteDatabase);

                    } else if (_parsingType == Constants.PARSER_NEXT_STOP) {

                        _Item = getNextStop((BusRouteItem) params[0], mSqliteDatabase);

                    } else if (_parsingType == Constants.PARSER_REFRSH_TYPE) {

                        _Item = commonParser
                                .getDepthRefreshList((DepthFavoriteItem) params[0]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (mGetDataListener != null && !isCancelled()) {
                mGetDataListener.onCompleted(_parsingType, _Item);
                mHashMap.remove(this);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    public void clear() {
        Iterator iterator = mHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            mHashMap.get(entry.getKey()).cancel(true);
        }

        mHashMap.clear();

    }

    public void setParsingType(int type) {
        mParsingType = type;
    }

    public CommonParser getParser(String city) {

        CommonParser commonParser = null;

        if (city.equals(CommonConstants.CITY_A_SAN._engName)) {
            commonParser = new ParserAsan(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_BU_SAN)) {
            commonParser = new ParserBusan(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_CHANG_WON)) {
            commonParser = new ParserChangwon(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_CHEONN_AN._engName)) {
            commonParser = new ParserCheonan(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_CHUN_CHEON._engName)) {
            commonParser = new ParserChunChan(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_CHEONG_JU)) {
            commonParser = new ParserChunggu(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_DAE_GU._engName)) {
            commonParser = new ParserDaegu(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_DAE_JEON)) {
            commonParser = new ParserDaejeon(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_GIM_HEA._engName)) {
            commonParser = new ParserGimhae(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_GIM_CHEON._engName)) {
            commonParser = new ParserGimCheon(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_GU_MI)) {
            commonParser = new ParserGumi(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_GANG_NEUNG)) {
            commonParser = new ParserGangNeung(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_GYEONG_JU)) {
            commonParser = new ParserGyeongJu(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_GYEONG_GI)) {
            commonParser = new ParserGunggi(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_GUN_SAN)) {
            commonParser = new ParserGunsan(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_GYEONG_SAN._engName)) {
            commonParser = new ParserGyeongSan(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_GEO_JE._engName)) {
            commonParser = new ParserGeoJe(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_GONG_JU._engName)) {
            commonParser = new ParserGongju(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_IN_CHEON)) {
            commonParser = new ParserIncheon(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_JE_JU)) {
            commonParser = new ParserJeju(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_JE_CHEON._engName)) {
            commonParser = new ParserJeCheon(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_JIN_JU._engName)) {
            commonParser = new ParserJinju(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_MIR_YANG._engName)) {
            commonParser = new ParserMiryang(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_PO_HANG._engName)) {
            commonParser = new ParserPohang(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_SEO_UL._engName)) {
            commonParser = new ParserSeoul(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_SUN_CHEON)) {
            commonParser = new ParserSuncheon(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_TONG_YEONG)) {
            commonParser = new ParserTongyeong(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_UL_SAN._engName)) {
            commonParser = new ParserUlsan(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_WON_JU._engName)) {
            commonParser = new ParserWonju(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_YEO_SU._engName)) {
            commonParser = new ParserYeosu(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_YANG_SAN._engName)) {
            commonParser = new ParserYangsan(mSqliteDatabase);
        } else if (city.equals(Constants.CITY_UWANG)) {

        } else if (city.equals(CommonConstants.CITY_GWANG_YANG._engName)) {
            commonParser = new ParserGwangyang(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_JEON_JU._engName)) {
            commonParser = new ParserJeonju(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_MOK_PO._engName)) {
            commonParser = new ParserMokop(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_CHIL_GOK._engName)) {
            commonParser = new ParserChilGok(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_GWANG_JU._engName)) {
            commonParser = new ParserGwangju(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_NA_JU._engName)) {
            commonParser = new ParserNaju(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_HWASUN._engName)) {
            commonParser = new ParserHawsun(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_SE_JONG._engName)) {
            commonParser = new ParserSejong(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_CHUNG_JU._engName)) {
            commonParser = new ParserChung(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_DAMYANG._engName)) {
            commonParser = new ParserDamyang(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_JANGSEONG._engName)) {
            commonParser = new ParserJangseng(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_YEONGAM._engName)) {
            commonParser = new ParserYeongam(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_MUAN._engName)) {
            commonParser = new ParserMuan(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_SHINAN._engName)) {
            commonParser = new ParserShinan(mSqliteDatabase);
        } else if (city.equals(CommonConstants.CITY_BO_SEONG._engName)) {
            commonParser = new ParserBoseong(mSqliteDatabase);
        }

        return commonParser;
    }


    private DepthRouteItem getRelateRoutes(BusStopItem stopItem,
                                           SQLiteDatabase db) {
        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        String localName = mHashLocationEng.get(Integer
                .parseInt(stopItem.localInfoId));
        String localId = stopItem.localInfoId;

        String getRelateRoutesQry = "";


        if (stopItem.apiType == Constants.API_TYPE_1) {
            getRelateRoutesQry = String.format(
                    "Select * From %s where %s='%s' ", localName + "_stop",
                    CommonConstants.BUS_STOP_API_ID, stopItem.apiId);
        } else if (stopItem.apiType == Constants.API_TYPE_3) {

            stopItem.arsId = String.valueOf(stopItem.arsId);
            getRelateRoutesQry = String.format(
                    "Select * From %s where %s='%s' ", localName + "_stop",
                    CommonConstants.BUS_STOP_ARS_ID, stopItem.arsId);
        } else if (stopItem.apiType == Constants.API_TYPE_4) {


            getRelateRoutesQry = String.format(
                    "Select * From %s where %s='%s' ", localName + "_stop",
                    CommonConstants.BUS_STOP_DESC, stopItem.tempId2);


        } else {

            if (stopItem.apiId == null || stopItem.apiId.equals("") || stopItem.apiId.equals("null")) {
                stopItem.arsId = String.valueOf(stopItem.arsId);
                getRelateRoutesQry = String.format(
                        "Select * From %s where %s='%s' ", localName
                                + "_stop", CommonConstants.BUS_STOP_ARS_ID,
                        stopItem.arsId);
            } else if (stopItem.arsId == null || stopItem.arsId.equals("") || stopItem.arsId.equals("null")) {

                if (stopItem.apiId.length() > 9) {
                    stopItem.apiId = stopItem.apiId;
                } else {
                    stopItem.apiId = String.valueOf(Integer.parseInt(stopItem.apiId));
                }

                getRelateRoutesQry = String.format(
                        "Select * From %s where %s='%s' ", localName
                                + "_stop", CommonConstants.BUS_STOP_API_ID,
                        stopItem.apiId);

            } else {
                stopItem.arsId = String.valueOf(stopItem.arsId);
                getRelateRoutesQry = String.format(
                        "Select * From %s where %s='%s' and %s='%s'", localName
                                + "_stop", CommonConstants.BUS_STOP_API_ID,
                        stopItem.apiId, CommonConstants.BUS_STOP_ARS_ID,
                        stopItem.arsId);
            }

        }

        Cursor relateCursor = db.rawQuery(getRelateRoutesQry, null);
        String relateRoute = "";

        if (relateCursor.moveToFirst()) {
            relateRoute = relateCursor.getString(relateCursor
                    .getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES));

            stopItem.apiId = relateCursor.getString(relateCursor
                    .getColumnIndex(CommonConstants.BUS_STOP_API_ID));

            stopItem.tempId2 = relateCursor.getString(relateCursor
                    .getColumnIndex(CommonConstants.BUS_STOP_DESC));
        }
        relateCursor.close();

        String[] relateSplit = relateRoute.split("/");


        int tmpBusType = -1;
        int tempCount = 0;

        String routInforQry = null;
        boolean isError = false;

        for (int i = 0; i < relateSplit.length; i++) {
            String routeId = relateSplit[i];
            if (routeId == null || routeId.equals("")) {
                isError = true;
                break;
            }

            if (routInforQry == null) {
                routInforQry = String.format(
                        "Select * From %s where %s=%s ", localName
                                + "_route", CommonConstants._ID, routeId);
            } else {

                routInforQry = routInforQry + " UNION ALL " + String.format(
                        "Select * From %s where %s=%s ", localName
                                + "_route", CommonConstants._ID, routeId);
            }
        }

        if (!isError) {

            if (localName.equals(CommonConstants.CITY_SEO_UL._engName)) {
                routInforQry = routInforQry + String.format(
                        "  order by %s asc, %s asc", CommonConstants.BUS_ROUTE_BUS_TYPE, CommonConstants.BUS_ROUTE_NAME);
            } else {
                routInforQry = routInforQry + String.format(
                        "  order by %s desc, %s asc", CommonConstants.BUS_ROUTE_BUS_TYPE, CommonConstants.BUS_ROUTE_NAME);
            }

            Cursor routeCursor = db.rawQuery(routInforQry, null);
            while (routeCursor.moveToNext()) {
                String busRouteName = routeCursor.getString(routeCursor
                        .getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                String busRouteApi1 = routeCursor.getString(routeCursor
                        .getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                String busRouteApi2 = routeCursor.getString(routeCursor
                        .getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                String busRouteStartStop = routeCursor
                        .getString(routeCursor
                                .getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                String busRouteEndStop = routeCursor.getString(routeCursor
                        .getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                String busRouteSub = routeCursor.getString(routeCursor
                        .getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                String busType = routeCursor.getString(routeCursor
                        .getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                String busRouteRelateStop = routeCursor
                        .getString(routeCursor
                                .getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
                int _id = routeCursor.getInt(routeCursor
                        .getColumnIndex(CommonConstants._ID));

                BusRouteItem busRouteItem = new BusRouteItem();
                busRouteItem.busRouteName = busRouteName;
                busRouteItem.busRouteApiId = busRouteApi1;
                busRouteItem.busRouteApiId2 = busRouteApi2;
                busRouteItem.busRouteSubName = busRouteSub;
                busRouteItem.startStop = busRouteStartStop;
                busRouteItem.endStop = busRouteEndStop;
                busRouteItem.busType = Integer.parseInt(busType);
                busRouteItem.localInfoId = localId;


                busRouteItem.busStopApiId = stopItem.apiId;
                busRouteItem.busStopArsId = stopItem.arsId;
                busRouteItem.busStopName = stopItem.name;
                busRouteItem._stopId = stopItem._id;
                busRouteItem.relateStop = busRouteRelateStop;
                busRouteItem._id = _id;
                busRouteItem.tmpId = stopItem.tempId2;


                if (busRouteItem.busRouteApiId2 == null) {
                    busRouteItem.busRouteApiId2 = "";
                }

                if (busRouteItem.busRouteSubName == null) {
                    busRouteItem.busRouteSubName = "";
                }

                // busRouteItem.arriveInfo.add("");
                // busRouteItem.arriveInfo.add(busRouteItem.busRouteApiId);

                //tempCount 순서가 중요 나중에  각 아이템별로 파싱해오기위해서 필요
                if (tmpBusType != Integer.parseInt(busType)) {


                    BusRouteItem indexBusRouteItem = new BusRouteItem();
                    indexBusRouteItem.busRouteName = getBusName(db,
                            Integer.parseInt(busType));
                    indexBusRouteItem.isSection = true;
                    indexBusRouteItem.localInfoId = localId;
                    indexBusRouteItem.index = tempCount;

                    tmpBusType = Integer.parseInt(busType);
                    localBusRouteItem.add(indexBusRouteItem);
                    tempCount++;
                }

                busRouteItem.index = tempCount;
                localBusRouteItem.add(busRouteItem);
                tempCount++;
            }
            routeCursor.close();
        }


        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItem);
        return depthRouteItem;
    }

    // else
    private DepthRouteItem getNextStop(BusRouteItem routeItem, SQLiteDatabase db) {

        ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

        if (!routeItem.isSection && routeItem.relateStop != null
                && routeItem.relateStop.length() > 0) {

            String[] tmpSplit = routeItem.relateStop.split("/");
            int index = -1;

            for (int i = 0; i < tmpSplit.length; i++) {
                if (tmpSplit[i].equals(String.valueOf(routeItem._stopId))) {

                    if (i < tmpSplit.length - 1) {
                        index = i + 1;
                    } else if (i == tmpSplit.length - 1) {
                        index = 1000;
                    }
                    break;
                }
            }


            if (index == -1) {
                routeItem.nextDirctionName = "";
            } else if (index == 1000) {
                routeItem.nextDirctionName = "end";
            } else {
                String sql = String.format(
                        "SELECT * FROM %s where %s=%s",
                        mHashLocationEng.get(Integer
                                .parseInt(routeItem.localInfoId)) + "_stop",
                        CommonConstants._ID, tmpSplit[index]);
                Cursor cursor = db.rawQuery(sql, null);

                if (cursor.moveToNext()) {

                    String stopName = cursor.getString(cursor
                            .getColumnIndex(CommonConstants.BUS_STOP_NAME));

                    routeItem.nextDirctionName = stopName;
                }
            }

        }

        localBusRouteItem.add(routeItem);

        DepthRouteItem depthRouteItem = new DepthRouteItem();
        depthRouteItem.busRouteItem.addAll(localBusRouteItem);

        return depthRouteItem;
    }

    public String getBusName(SQLiteDatabase db, int id) {

        String sql = String.format("SELECT *FROM %s where %s=%s",
                CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, id);
        String busName = "";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            busName = cursor.getString(cursor
                    .getColumnIndex(CommonConstants.BUS_TYPE_BUS_TYPE));
        }

        cursor.close();

        return busName;

    }

    class RouteNameComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            return Integer.parseInt(o1) - Integer.parseInt(o2);
        }
    }

}
