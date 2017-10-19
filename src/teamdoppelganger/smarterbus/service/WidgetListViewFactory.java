package teamdoppelganger.smarterbus.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.WidgetStopItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;
import teamdoppelganger.smarterbus.util.common.GetData.GetDataListener;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.smart.lib.CommonConstants;

@SuppressLint("NewApi")
public class WidgetListViewFactory implements RemoteViewsService.RemoteViewsFactory {

    Context mContext;
    WidgetStopItem mItem;
    int mWidgetId;

    RemoteViews mRemoteView;

    HashMap<String, String[]> mHashMap = new HashMap<String, String[]>();
    HashMap<Integer, String> mBusTypeHash = new HashMap<Integer, String>();

    boolean mIsFirst;
    boolean mIsFromRefresh;

    GetData getData;

    public WidgetListViewFactory(Context context, Intent intent) {

        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        mIsFirst = true;

        try {
            FileInputStream fis = mContext.openFileInput(String.valueOf(mWidgetId) + "_type2");
            ObjectInputStream os = new ObjectInputStream(fis);
            mItem = (WidgetStopItem) os.readObject();
            os.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        String dbName = pref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
        SQLiteDatabase mDatabase = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);

        setBusType(mDatabase);

    }

    @Override
    public int getCount() {
        return mItem.busRouteArray.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews row = new RemoteViews(mContext.getPackageName(), R.layout.widget_4x2_list_item);

        row.setViewVisibility(R.id.widget_type2_item_divider, View.VISIBLE);

        row.setTextViewText(R.id.widget_type2_bus_name, mItem.busRouteArray.get(position).busRouteName);

        String color = mBusTypeHash.get(mItem.busRouteArray.get(position).busType);
        row.setTextColor(R.id.widget_type2_bus_name, Color.parseColor("#" + color));


        String[] resultTime = mHashMap.get(mItem.busRouteArray.get(position).busRouteApiId
                + mItem.busRouteArray.get(position).busRouteApiId2 + mItem.busRouteArray.get(position).busRouteName);

        if (resultTime == null)
            resultTime = new String[2];

        row.setTextViewText(R.id.widget_type2_remain_first, resultTime[0]);
        row.setTextViewText(R.id.widget_type2_remain_second, resultTime[1]);

        if (resultTime[1] == null)
            row.setViewVisibility(R.id.widget_type2_item_divider, View.GONE);

        if (mItem.busRouteArray.get(position).localInfoId.equals(String.valueOf(CommonConstants.CITY_GU_MI._cityId))
                || mItem.busRouteArray.get(position).localInfoId.equals(String.valueOf(CommonConstants.CITY_DAE_GU._cityId))) {
            row.setTextViewText(R.id.widget_type2_bus_subname, mItem.busRouteArray.get(position).busRouteSubName);
        }

        return row;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    public void setBusType(SQLiteDatabase db) {

        if (mBusTypeHash.size() > 0) return;

        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_BUS_TYPE);
        Cursor cursor = db.rawQuery(tmpSql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
            String color = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_TYPE_COLOR));
            mBusTypeHash.put(id, color);
        }
    }

    public void refreshWidgetList(int widgetId) {

        mWidgetId = widgetId;

        mHashMap.clear();

        WidgetProvider4x2.showResult(mContext, mWidgetId, WidgetProvider4x2.DIM_VISIBLE, null);

        mIsFromRefresh = true;

        getRemainTimes();

    }

    @Override
    public void onDataSetChanged() {

        if (!mIsFromRefresh) {
            mIsFromRefresh = true;
            return;
        }

        WidgetProvider4x2.showResult(mContext, mWidgetId, WidgetProvider4x2.DIM_GONE, null);
    }

    @Override
    public void onDestroy() {
    }

    public void getRemainTimes() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        String dbName = pref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);

        final SQLiteDatabase busDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName,
                null, SQLiteDatabase.OPEN_READONLY);

        final HashMap<Integer, String> hashLocation = new HashMap<Integer, String>();

        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);
        Cursor cursor = busDbSqlite.rawQuery(tmpSql, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
            String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
            hashLocation.put(id, cityEnName);
        }
        cursor.close();

        try {

            getData = new GetData(new GetDataListener() {
                @Override
                public void onCompleted(int type, DepthItem item) {

                    if (type == Constants.PARSER_LINE_TYPE) {

                        final ArrayList<BusRouteItem> _routeItemAry = mItem.busRouteArray;

                        DepthRouteItem depthRouteItem = (DepthRouteItem) item;
                        ArrayList<BusRouteItem> busRouteItem = (ArrayList<BusRouteItem>) depthRouteItem.busRouteItem;


                        for (int i = 0; i < busRouteItem.size(); i++) {

                            BusRouteItem tmpItem = busRouteItem.get(i);

                            if (!tmpItem.isSection) {

                                for (int j = 0; j < _routeItemAry.size(); j++) {

                                    BusRouteItem compareBusRouteItem = _routeItemAry.get(j);

                                    if (tmpItem.busRouteApiId == null || tmpItem.busRouteApiId.trim().length() == 0) {


                                        if (compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)) {

                                            if (tmpItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_GU_MI._cityId))) {


                                                //구미같은 경우는 db에 문자가 하나 다르게들어가있다.
                                                if (compareBusRouteItem.busRouteSubName.equals(tmpItem.busRouteSubName.replaceAll("_", "-"))) {

                                                    _routeItemAry.get(j).arriveInfo.clear();
                                                    _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    _routeItemAry.get(j).direction = tmpItem.direction;
                                                    _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    _routeItemAry.get(j).tmpId = tmpItem.tmpId;

                                                }
                                            } else {

                                                _routeItemAry.get(j).arriveInfo.clear();
                                                _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                _routeItemAry.get(j).direction = tmpItem.direction;
                                                _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                _routeItemAry.get(j).tmpId = tmpItem.tmpId;

                                            }


                                        }


                                    } else if (tmpItem.busRouteApiId2 == null || tmpItem.busRouteApiId2.length() == 0) {

                                        if (tmpItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_DAE_GU._cityId))) {


                                            if (tmpItem.busRouteApiId.length() > 0) {


                                                if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)) {

                                                    _routeItemAry.get(j).arriveInfo.clear();
                                                    _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    _routeItemAry.get(j).direction = tmpItem.direction;
                                                    _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    _routeItemAry.get(j).tmpId = tmpItem.tmpId;

                                                }

                                            } else {

                                                if (compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)) {

                                                    _routeItemAry.get(j).arriveInfo.clear();
                                                    _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    _routeItemAry.get(j).direction = tmpItem.direction;
                                                    _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    _routeItemAry.get(j).tmpId = tmpItem.tmpId;
                                                }

                                            }


                                        } else {

                                            if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)) {
                                                _routeItemAry.get(j).arriveInfo.clear();
                                                _routeItemAry.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                _routeItemAry.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                _routeItemAry.get(j).direction = tmpItem.direction;
                                                _routeItemAry.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                _routeItemAry.get(j).tmpId = tmpItem.tmpId;
                                            }

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
                                                    resultStr = mContext.getResources().getString(R.string.state_end);
                                                } else if (arriveItem.state == Constants.STATE_PREPARE) {
                                                    try {
                                                        if (arriveItem.remainSecond == -9999) {
                                                            resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                                                        } else {
                                                            resultStr = mContext.getString(R.string.state_prepare);
                                                        }
                                                    } catch (Exception e) {
                                                        resultStr = mContext.getString(R.string.state_prepare);
                                                    }
                                                    ;
                                                } else if (arriveItem.state == Constants.STATE_NEAR) {

                                                } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                                                    resultStr = mContext.getResources().getString(R.string.state_prepare_not);
                                                }

                                                resultTime[j] = resultStr;

                                            }

                                            mHashMap.put(tmpRouteItem.get(0).busRouteApiId + tmpRouteItem.get(0).busRouteApiId2
                                                    + tmpRouteItem.get(0).busRouteName, resultTime);

                                            AppWidgetManager manager = AppWidgetManager.getInstance(mContext);
                                            manager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.widget_type2_list);

                                        }
                                    }
                                }, busDbSqlite, hashLocation);
                                localGetData.startBusRouteDetailParsing(routeItem);
                            }

                            String[] resultTime = new String[2];
                            if (routeItem.arriveInfo.size() > 0 && routeItem.plusParsingNeed == 0) {

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
                                        resultStr = mContext.getResources().getString(R.string.state_end);
                                    } else if (arriveItem.state == Constants.STATE_PREPARE) {
                                        try {
                                            if (arriveItem.remainSecond == -9999) {
                                                resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                                            } else {
                                                resultStr = mContext.getString(R.string.state_prepare);
                                            }
                                        } catch (Exception e) {
                                            resultStr = mContext.getString(R.string.state_prepare);
                                        }
                                        ;
                                    } else if (arriveItem.state == Constants.STATE_NEAR) {

                                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                                        resultStr = mContext.getResources().getString(R.string.state_prepare_not);
                                    }

                                    resultTime[j] = resultStr;
                                }

                            }

                            if (item.depthIndex > 0) {

                                depthRouteItem.busRouteItem.clear();
                                depthRouteItem.busRouteItem.addAll(_routeItemAry);
                                getData.depthBusRouteParsing(depthRouteItem);

                            } else {

                                mHashMap.put(_routeItemAry.get(i).busRouteApiId + _routeItemAry.get(i).busRouteApiId2
                                        + _routeItemAry.get(i).busRouteName, resultTime);


                            }


                        }


                    } else if (type == Constants.PARSER_LINE_DEPTH_TYPE) {
                        DepthRouteItem depthRouteItem = (DepthRouteItem) item;

                        for (int i = 0; i < depthRouteItem.busRouteItem.size(); i++) {

                            String[] resultTime = new String[2];
                            BusRouteItem routeItem = depthRouteItem.busRouteItem.get(i);

                            for (int j = 0; j < routeItem.arriveInfo.size(); j++) {
                                if (j == 1)
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
                                    resultStr = mContext.getResources().getString(R.string.state_end);
                                } else if (arriveItem.state == Constants.STATE_PREPARE) {
                                    try {
                                        if (arriveItem.remainSecond == -9999) {
                                            resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                                        } else {
                                            resultStr = mContext.getString(R.string.state_prepare);
                                        }
                                    } catch (Exception e) {
                                        resultStr = mContext.getString(R.string.state_prepare);
                                    }
                                    ;
                                } else if (arriveItem.state == Constants.STATE_NEAR) {

                                } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                                    resultStr = mContext.getResources().getString(R.string.state_prepare_not);
                                }

                                resultTime[j] = resultStr;

                                mHashMap.put(routeItem.busRouteApiId + routeItem.busRouteApiId2
                                        + routeItem.busRouteName, resultTime);

                            }


                        }


                    }


                    AppWidgetManager manager = AppWidgetManager.getInstance(mContext);
                    manager.notifyAppWidgetViewDataChanged(mWidgetId, R.id.widget_type2_list);


                }
            }, busDbSqlite, hashLocation);
            getData.startBusRouteParsing(mItem.favoriteAndHistoryItem.busStopItem);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
