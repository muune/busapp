package teamdoppelganger.smarterbus.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mocoplex.adlib.AdlibActivity;
import com.mocoplex.adlib.AdlibConfig;
import com.mocoplex.adlib.AdlibManager;
import com.smart.lib.CommonConstants;
import com.mocoplex.adlib.platform.nativeads.AdlibNativeHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBMainActivity;
import teamdoppelganger.smarterbus.bs.bsCursor;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.item.LocalItem;
import teamdoppelganger.smarterbus.item.WorkDayItem;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

/**
 * Created by muune on 2017-09-28.
 */

public class SBBaseNewActivity extends AdlibActivity {
    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;
    private SharedPreferences mPref;
    private AdlibManager amanager;
    private AdlibNativeHelper nativeHelper;

    private LocalInforItem mLocalInforItem;
    private ArrayList<LocalItem> mLocalEnCityName;

    public String mClassName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mClassName = getClass().getSimpleName();

        mLocalInforItem = ((SBInforApplication) getApplicationContext()).mLocalSaveInfor;
        mLocalEnCityName = new ArrayList<LocalItem>();

        try {
            String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
            if (databaseExist(Constants.LOCAL_PATH + dbName)) {
                mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);
            }
            mLocalDBHelper = new LocalDBHelper(this);

            ((SBInforApplication) getApplicationContext()).checkSetting(mLocalDBHelper, mBusDbSqlite, this);
            ((SBInforApplication) getApplicationContext()).setTerminus(mBusDbSqlite);
            ((SBInforApplication) getApplicationContext()).setCityInfor(mBusDbSqlite);
            ((SBInforApplication) getApplicationContext()).setCityKoInfor(mBusDbSqlite);
            ((SBInforApplication) getApplicationContext()).setBusType(mBusDbSqlite);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void actionBarInit(String $title){
        TextView title = (TextView) findViewById(R.id.actionbar_title);
        title.setText($title);

        ImageButton back = (ImageButton) findViewById(R.id.actionbar_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    public boolean databaseExist(String fullPath) {
        File dbFile = new File(fullPath);
        return dbFile.exists();
    }

    public void setBusDbSqlite() {
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
        if (databaseExist(Constants.LOCAL_PATH + dbName)) {
            mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);
        }
    }

    public SQLiteDatabase getBusDbSqlite() {
        return mBusDbSqlite;
    }
    public LocalDBHelper getLocalDBHelper() {
        return mLocalDBHelper;
    }


    protected AdlibManager getAdlibManager(){ //딱 한개만 쓸 경우에만 사용한다. 자동으로 라이플사이클 관리를 해준다.
        if(amanager == null){
            amanager = new AdlibManager(Constants.ADLIB_MAIN_API_KEY);
            amanager.onCreate(this);
        }
        return amanager;
    }

    protected AdlibNativeHelper getNativeHelper(ViewGroup view){ //딱 한개만 쓸 경우에만 사용한다. 자동으로 라이플사이클 관리를 해준다.
        if(nativeHelper == null) {
            nativeHelper = new AdlibNativeHelper(view);
            nativeHelper.registerScrollChangedListener();
        }
        return nativeHelper;
    }

    protected void initAds() { //메인이나, 처음 부르는 액티비티에서만 실행할것!
        AdlibConfig.getInstance().bindPlatform("ADAM", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewAdam");
        AdlibConfig.getInstance().bindPlatform("ADMOB", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewAdmob");
        AdlibConfig.getInstance().bindPlatform("CAULY", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewCauly");
        AdlibConfig.getInstance().bindPlatform("ADMIXER", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewAdmixer");
        AdlibConfig.getInstance().bindPlatform("MEZZO", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewMezzo");
        AdlibConfig.getInstance().bindPlatform("FACEBOOK", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewFacebook");

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            AdlibConfig.getInstance().bindPlatform("INMOBI", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewInmobi");
        }

        this.setBannerFailDelayTime(5);
        setAds();
    }
    public void setAds(){
        setAds(R.id.admixer_layout, R.id.adlib);
    }
    public void setAds(final int $layoutId, final int $containerId){
        this.setAdlibKey(Constants.ADLIB_MAIN_API_KEY);
        this.setAdsHandler(new Handler() {
            public void handleMessage(Message message) {
                try {
                    switch (message.what) {
                        case AdlibManager.DID_SUCCEED:
                            if (findViewById($containerId).getVisibility() == View.GONE) {
                                findViewById($containerId).setVisibility(View.VISIBLE);
                                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up);
                                findViewById($layoutId).startAnimation(slide_up);
                            }
                            break;
                        case AdlibManager.DID_ERROR:
                            break;
                    }
                } catch (Exception e) {
                }
            }
        });
        this.setAdsContainer($containerId);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(nativeHelper != null) nativeHelper.update();
    }


    @Override
    public void onResume() {
        super.onResume();
        if(amanager != null) amanager.onResume(this);
        if(nativeHelper != null) nativeHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(amanager != null) amanager.onPause(this);
        if(nativeHelper != null) nativeHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(amanager != null) amanager.onDestroy(this);
        if(nativeHelper != null) nativeHelper.onDestroy();

        if (mBusDbSqlite != null &&
                mBusDbSqlite.isOpen()) {
            mBusDbSqlite.close();
        }

        if (mLocalDBHelper != null) {
            mLocalDBHelper.close();
        }
    }

    private void setLocationInit(){
        if (mLocalEnCityName.size() != mLocalInforItem.localItems.size()) {
            setLocation();
        } else {
            for (int i = 0; i < mLocalInforItem.localItems.size(); i++) {
                boolean isExist = false;
                for (int j = 0; j < mLocalEnCityName.size(); j++) {
                    if (mLocalEnCityName.get(j).cityId == mLocalInforItem.localItems.get(i).cityId) {
                        isExist = true;
                    }
                }
                if (!isExist) {
                    setLocation();
                    break;
                }
            }
        }
    }
    private void setLocation() {
        mLocalEnCityName.clear();
        mLocalEnCityName = new ArrayList<LocalItem>();
        for (int i = 0; i < mLocalInforItem.localItems.size(); i++) {
            LocalItem localItem = mLocalInforItem.localItems.get(i);
            mLocalEnCityName.add(localItem);
        }
    }

    public List<BusStopItem> SearchBusStopItemList(String searchStr){
        setLocationInit();
        if (searchStr != null && searchStr.trim().length() == 0) {
            searchStr = "null";
        } else {
            searchStr = searchStr.replace("-", "");
        }

        String tableName = "";
        for (int i = 0; i < mLocalEnCityName.size(); i++) {
            String tempTable = String.format("SELECT *, (Select stopName from %s_STOP where _id=tb_%s_a.stopNextStopId) as nextStopName  from (SELECT *,'%s' as cityId FROM %s_STOP) as tb_%s_a"
                    , mLocalEnCityName.get(i).enName, mLocalEnCityName.get(i).cityId, mLocalEnCityName.get(i).cityId, mLocalEnCityName.get(i).enName
                    , mLocalEnCityName.get(i).cityId, mLocalEnCityName.get(i).enName);

            if (tableName.equals("")) {
                tableName = tempTable;
            } else {
                tableName = tableName + " union ALL " + tempTable;
            }
        }

        String qry1StopName = String.format("SELECT * from (%s) where  (%s<>%s ) and (%s like '%s%%' or %s like '%s%%') limit 500",
                tableName, CommonConstants.BUS_STOP_ARS_ID, "0", CommonConstants.BUS_STOP_ARS_ID, searchStr, CommonConstants.BUS_STOP_NAME, searchStr);

        String qryStopInfor = String.format("SELECT * from (%s) order by cityId", qry1StopName);

        Cursor cursor = mBusDbSqlite.rawQuery(qryStopInfor, null);

        List<BusStopItem> list = new ArrayList<>();
        if(cursor == null) return list;
        if(cursor.getCount() == 0) {
            cursor.close();
            return list;
        }

        if(cursor.moveToFirst()){
            do {
                String busStopName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                String busStopApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                String busStopArsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                String busStopLocationID = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID));
                String description = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));

                BusStopItem busStopItem = new BusStopItem();
                busStopItem.apiId = busStopApiId;
                busStopItem.arsId = busStopArsId;
                busStopItem.name = busStopName;
                busStopItem.localInfoId = busStopLocationID;
                busStopItem.tempId2 = description;
                busStopItem._id = id;

                list.add(busStopItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public List<BusRouteItem> SearchBusRouteItemList(String searchStr){
        setLocationInit();
        if (searchStr != null && searchStr.trim().length() == 0) {
            searchStr = "null";
        }

        String tableName = "";
        for (int i = 0; i < mLocalEnCityName.size(); i++) {
            String tempTable = String.format("SELECT *,'%s' as %s FROM %s_ROUTE", mLocalEnCityName.get(i).cityId, CommonConstants.CITY_ID, mLocalEnCityName.get(i).enName);
            if (tableName.equals("")) {
                tableName = tempTable;
            } else {
                tableName = tableName + " union ALL " + tempTable;
            }
        }

        String qryStopInfor = String.format(" Select * from ( SELECT * from (Select *,group_concat(DISTINCT cityId) as cityIds  from (%s) group by %s,%s,%s,%s,_id   having %s like '%s%%' or %s like '%s%%' or %s like '%s%%' or %s like '%s%%' or (routeType=%s and routeName like '%%%s') or (routeType=%s and routeName like '%%%s') or (routeType=%s and routeName like '%%%s') or (routeType=%s and routeName like '%%%s%%')) limit 600) order by cityId ",
                tableName, CommonConstants.BUS_ROUTE_NAME, CommonConstants.BUS_ROUTE_START_STOP_ID, CommonConstants.BUS_ROUTE_END_STOP_ID, CommonConstants.BUS_ROUTE_SUB_NAME, CommonConstants.BUS_ROUTE_NAME, searchStr, CommonConstants.BUS_ROUTE_NAME, "M" + searchStr, CommonConstants.BUS_ROUTE_NAME, "N" + searchStr, CommonConstants.BUS_ROUTE_NAME, "G" + searchStr, Constants.SEOUL_BUS_VILLAGE, searchStr, Constants.BUSAN_BUS_VILLAGE, searchStr, Constants.GONGJU_BUS_VILLAGE, searchStr, Constants.BOSEONG_BUS_VILLAGE, searchStr);

        Cursor cursor = mBusDbSqlite.rawQuery(qryStopInfor, null);
        List<BusRouteItem> list = new ArrayList<>();
        if(cursor == null) return list;
        if(cursor.getCount() == 0) {
            cursor.close();
            return list;
        }

        if(cursor.moveToFirst()) {
            do {
                String busRouteName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                String busRouteSubName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                String busRouteApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                String busRouteApiId2 = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                String busStart = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                String busEnd = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                String busRouteLocationID = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID));
                int busType = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));

                BusRouteItem busRouteItem = new BusRouteItem();
                busRouteItem.busRouteApiId = busRouteApiId;
                busRouteItem.busRouteApiId2 = busRouteApiId2;
                busRouteItem.busRouteName = busRouteName;
                busRouteItem.busRouteSubName = busRouteSubName;
                busRouteItem.localInfoId = busRouteLocationID;
                busRouteItem.startStop = busStart;
                busRouteItem.endStop = busEnd;
                busRouteItem.busType = busType;
                busRouteItem._id = id;

                list.add(busRouteItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }
}
