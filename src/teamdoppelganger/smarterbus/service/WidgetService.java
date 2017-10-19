package teamdoppelganger.smarterbus.service;

import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.WidgetStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;
import teamdoppelganger.smarterbus.util.common.GetData.GetDataListener;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.smart.lib.CommonConstants;

public class WidgetService extends Service implements GetDataListener {

    int mWidgetId;
    boolean mIsType1;

    int mListState;
    int mPage;

    WidgetStopItem mWidgetStopItem;
    HashMap<Integer, String> mBusTypeHash = new HashMap<Integer, String>();

    HashMap<String, String[]> mResultHash = new HashMap<String, String[]>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mWidgetId = intent.getExtras().getInt("widgetId");
        mIsType1 = intent.getExtras().getBoolean("isType1");
        mListState = intent.getExtras().getInt("list_state", 0);
        mWidgetStopItem = (WidgetStopItem) intent.getExtras().getSerializable("favoriteItemArry");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = pref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);

        final SQLiteDatabase busDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName,
                null, SQLiteDatabase.OPEN_READONLY);

        if (!mIsType1) {
            mPage = pref.getInt(String.valueOf(mWidgetId) + "widget_page", 0);

            WidgetProvider4x2.showResult(WidgetService.this, mWidgetId, WidgetProvider4x2.DIM_VISIBLE, null);

            mPage += mListState;

            pref.edit().putInt(String.valueOf(mWidgetId) + "widget_page", mPage).commit();

        }

        final HashMap<Integer, String> hashLocation = new HashMap<Integer, String>();

        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);
        Cursor cursor = busDbSqlite.rawQuery(tmpSql, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
            String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
            hashLocation.put(id, cityEnName);
        }
        cursor.close();



        ArrayList<FavoriteAndHistoryItem> favoriteItemArry = new ArrayList<FavoriteAndHistoryItem>();

        try {
            if (mIsType1) {

                WidgetProvider2x1.showResult(this, AppWidgetManager.getInstance(getApplicationContext()), mWidgetId, null, 1);

                favoriteItemArry.add((FavoriteAndHistoryItem) intent.getExtras().getSerializable(Constants.INTENT_FAVORITEITEM));
                for (int i = 0; i < favoriteItemArry.size(); i++) {
                    GetData getData = new GetData(this, busDbSqlite, hashLocation);
                    DepthFavoriteItem depthFavoriteItem = new DepthFavoriteItem();
                    depthFavoriteItem.favoriteAndHistoryItems.add(favoriteItemArry.get(i));
                    getData.startOneRefrshService(depthFavoriteItem);
                }

            } else {

                final GetData getData = new GetData(new GetDataListener() {

                    @Override
                    public void onCompleted(int type, DepthItem item) {

                        if (type == Constants.PARSER_LINE_TYPE) {

                            final ArrayList<BusRouteItem> _routeItemAry = mWidgetStopItem.busRouteArray;

                            DepthRouteItem depthRouteItem = (DepthRouteItem) item;
                            ArrayList<BusRouteItem> busRouteItem = (ArrayList<BusRouteItem>) depthRouteItem.busRouteItem;

                            for (int i = 0; i < busRouteItem.size(); i++) {

                                BusRouteItem tmpItem = busRouteItem.get(i);

                                if (!tmpItem.isSection) {

                                    for (int j = 0; j < _routeItemAry.size(); j++) {

                                        BusRouteItem compareBusRouteItem = _routeItemAry.get(j);

                                        if (tmpItem.busRouteApiId == null || tmpItem.busRouteApiId.trim().length() == 0) {

                                            if (compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)) {

                                                _routeItemAry.get(j).arriveInfo.clear();
                                                _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                _routeItemAry.get(j).direction = tmpItem.direction;
                                                _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                _routeItemAry.get(j).tmpId = tmpItem.tmpId;

                                            }

                                        } else if (tmpItem.busRouteApiId2 == null || tmpItem.busRouteApiId2.length() == 0) {

                                            if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)) {

                                                _routeItemAry.get(j).arriveInfo.clear();
                                                _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                _routeItemAry.get(j).direction = tmpItem.direction;
                                                _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                _routeItemAry.get(j).tmpId = tmpItem.tmpId;

                                            }

                                        } else {

                                            if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)
                                                    && compareBusRouteItem.busRouteApiId2.equals(tmpItem.busRouteApiId2)
                                                    && compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)) {

                                                _routeItemAry.get(j).arriveInfo.clear();
                                                _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                _routeItemAry.get(j).direction = tmpItem.direction;
                                                _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                _routeItemAry.get(j).tmpId = tmpItem.tmpId;

                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            for (int i = 0; i < _routeItemAry.size(); i++) {

                                if (i >= ((mPage * 4) + 4) || i < mPage * 4)
                                    continue;


                                BusRouteItem routeItem = _routeItemAry.get(i);

                                if (routeItem.plusParsingNeed > 0 && !routeItem.isSection) {

                                    GetData localGetData = new GetData(new GetDataListener() {

                                        @Override
                                        public void onCompleted(int type, DepthItem item) {

                                            DepthRouteItem depthStopItem = (DepthRouteItem) item;
                                            ArrayList<BusRouteItem> tmpRouteItem = (ArrayList<BusRouteItem>) depthStopItem.busRouteItem;
                                            if (tmpRouteItem.size() == 1) {
                                                _routeItemAry.set(tmpRouteItem.get(0).index, tmpRouteItem.get(0));

                                                String[] resultTime = new String[2];

                                                for (int j = 0; j < tmpRouteItem.get(0).arriveInfo.size(); j++) {
                                                    if (j > 1)
                                                        break;

                                                    ArriveItem arriveItem = tmpRouteItem.get(0).arriveInfo.get(j);
                                                    String resultStr = "";

                                                    if (arriveItem.remainMin != -1) {
                                                        resultStr = arriveItem.remainMin + "분";
                                                    }

                                                    if (arriveItem.remainSecond != -1) {
                                                        if (resultStr.startsWith("0"))
                                                            resultStr = "";

                                                        resultStr += " " + arriveItem.remainSecond + "초";
                                                    }

                                                    if (arriveItem.state == Constants.STATE_END) {
                                                        resultStr = getResources().getString(R.string.state_end);
                                                    } else if (arriveItem.state == Constants.STATE_PREPARE) {
                                                        try {
                                                            if (arriveItem.remainSecond == -9999) {
                                                                resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                                                            } else {
                                                                resultStr = getResources().getString(R.string.state_prepare);
                                                            }
                                                        } catch (Exception e) {
                                                            resultStr = getResources().getString(R.string.state_prepare);
                                                        }
                                                        ;
                                                    } else if (arriveItem.state == Constants.STATE_NEAR) {

                                                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                                                        resultStr = getResources().getString(R.string.state_prepare_not);
                                                    }

                                                    resultTime[j] = resultStr;
                                                }

                                                mResultHash.put(tmpRouteItem.get(0).busRouteName, resultTime);
                                                WidgetProvider4x2.showResult(WidgetService.this, mWidgetId, WidgetProvider4x2.DIM_GONE, mResultHash);

                                            }
                                        }
                                    }, busDbSqlite, hashLocation);
                                    localGetData.startBusRouteDetailParsing(routeItem);
                                }

                                if (routeItem.arriveInfo.size() > 0 && routeItem.plusParsingNeed == 0) {

                                    String[] resultTime = new String[2];

                                    for (int j = 0; j < routeItem.arriveInfo.size(); j++) {
                                        if (j > 1)
                                            break;

                                        ArriveItem arriveItem = routeItem.arriveInfo.get(j);
                                        String resultStr = "";

                                        if (arriveItem.remainMin != -1) {
                                            resultStr = arriveItem.remainMin + "분";
                                        }

                                        if (arriveItem.remainSecond != -1) {
                                            if (resultStr.startsWith("0"))
                                                resultStr = "";

                                            resultStr += " " + arriveItem.remainSecond + "초";
                                        }

                                        if (arriveItem.state == Constants.STATE_END) {
                                            resultStr = getResources().getString(R.string.state_end);
                                        } else if (arriveItem.state == Constants.STATE_PREPARE) {
                                            try {
                                                if (arriveItem.remainSecond == -9999) {
                                                    resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                                                } else {
                                                    resultStr = getResources().getString(R.string.state_prepare);
                                                }
                                            } catch (Exception e) {
                                                resultStr = getResources().getString(R.string.state_prepare);
                                            }
                                            ;
                                        } else if (arriveItem.state == Constants.STATE_NEAR) {

                                        } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                                            resultStr = getResources().getString(R.string.state_prepare_not);
                                        }

                                        resultTime[j] = resultStr;

                                    }
                                    mResultHash.put(_routeItemAry.get(i).busRouteName, resultTime);
                                }
                            }

                            if (_routeItemAry.get(0).arriveInfo.size() > 0 && _routeItemAry.get(0).plusParsingNeed == 0)
                                WidgetProvider4x2.showResult(
                                        WidgetService.this, mWidgetId, WidgetProvider4x2.DIM_GONE, mResultHash);

                        }
                    }
                }, busDbSqlite, hashLocation);
                getData.startBusRouteParsing(mWidgetStopItem.favoriteAndHistoryItem.busStopItem);

                stopSelf();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        ;

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    String setLineUp(String resultTime) {

        if (resultTime.contains("분")) {

            String route = resultTime.split("분")[0];
            String stop = resultTime.split("분")[1];

            int length = route.length();
            if (length == 1)
                route = " " + " " + " " + " " + route;
            else if (length == 2)
                route = " " + route;

            route += "분";

            if (stop.length() != 0) {
                stop = stop.replace("(", "");
                if (stop.split("정류장")[0].length() == 1)
                    stop = " " + " " + stop;

                stop = "(" + stop;
            }

            resultTime = route + stop;
        }

        return resultTime;
    }

    @Override
    public void onCompleted(int type, DepthItem item) {

        DepthFavoriteItem depthFavoritItem = (DepthFavoriteItem) item;

        ArrayList<ArriveItem> arriveItems = depthFavoritItem.favoriteAndHistoryItems.get(0).busRouteItem.arriveInfo;

        String[] resultTime = new String[2];

        for (int i = 0; i < arriveItems.size(); i++) {

            if (i > 1)
                break;

            ArriveItem arriveItem = arriveItems.get(i);
            String resultStr = "";

            if (arriveItem.state == Constants.STATE_END) {
                resultStr = getResources().getString(R.string.state_end);
            } else if (arriveItem.state == Constants.STATE_PREPARE) {
                try {
                    if (arriveItem.remainSecond == -9999) {
                        resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                    } else {
                        resultStr = getResources().getString(R.string.state_prepare);
                    }
                } catch (Exception e) {
                    resultStr = getResources().getString(R.string.state_prepare);
                }
                ;
            } else if (arriveItem.state == Constants.STATE_NEAR) {

            } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                resultStr = getResources().getString(R.string.state_prepare_not);
            } else if (arriveItem.state == Constants.STATE_ING) {

                if (arriveItem.remainMin != -1) {
                    resultStr = arriveItem.remainMin + "분";
                }

                if (arriveItem.remainSecond != -1) {
                    if (resultStr.startsWith("0"))
                        resultStr = "";

                    resultStr += " " + arriveItem.remainSecond + "초";
                }

                if (arriveItem.remainStop != -1) {
                    if (resultStr.equals("")) {
                        resultStr = arriveItem.remainStop + "정류장 전";
                    } else {
                        resultStr = resultStr + "(" + arriveItem.remainStop + "정류장 전)";
                    }
                }

            }

            resultTime[i] = resultStr;
        }

        WidgetProvider2x1.showResult(this, AppWidgetManager.getInstance(getApplicationContext()), mWidgetId, resultTime, 2);

        stopSelf();

    }
}
