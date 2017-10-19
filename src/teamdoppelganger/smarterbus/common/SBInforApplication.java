package teamdoppelganger.smarterbus.common;

import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBDialog;
import teamdoppelganger.smarterbus.adapter.CommonAdapter;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.item.LocalItem;
import teamdoppelganger.smarterbus.item.LocalNotiItem;
import teamdoppelganger.smarterbus.item.NotificationItem;
import teamdoppelganger.smarterbus.item.RecentItem;
import teamdoppelganger.smarterbus.util.common.CommonNotiLib;
import teamdoppelganger.smarterbus.util.common.CommonNotiLib.GetDataListener;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.bs.bsSetting;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.smart.lib.CommonConstants;

public class SBInforApplication extends Application implements GetDataListener {

    public LocalInforItem mLocalSaveInfor;
    // cityId, locationName
    public HashMap<Integer, String> mHashLocation;
    public HashMap<Integer, String> mHashKoLocation;
    public HashMap<Integer, String> mTerminus;
    public HashMap<Integer, String> mBusTypeHash;
    public boolean isLocationChange = false;


    SharedPreferences mPref;
    CommonNotiLib mCommonNotiLib;
    boolean mIsFirstScan = false;

    boolean mIsNotChange = false;

    Context mContext;

    private static final String PROPERTY_ID = "UA-42810086-2";

    private String ADID = "";

    public bsSetting setting;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics
                    .newTracker(PROPERTY_ID) : analytics
                    .newTracker(R.xml.global_tracker);
            t.enableAdvertisingIdCollection(true);
            t.enableAutoActivityTracking(true);

            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*
        //bs 추가됨 -----------
        setting = bsSetting.pool(getApplicationContext(), "setting.json");
        if(!setting.isLoaded()){
            System.exit(0);
            return;
        }

        //bsSQLite engine = bsSQLite.pool(getApplicationContext(), setting.getString("db.name"));
        //DAO.init(engine);
        //---------------------
        */

        mHashLocation = new HashMap<Integer, String>();
        mHashKoLocation = new HashMap<Integer, String>();

        mTerminus = new HashMap<Integer, String>();
        mBusTypeHash = new HashMap<Integer, String>();
        mLocalSaveInfor = new LocalInforItem();


        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mCommonNotiLib = new CommonNotiLib();
        mCommonNotiLib.setDataListener(this);
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



    public void setTerminus(SQLiteDatabase db) {

        if (mTerminus.size() > 0) return;


        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_TERMINUS);
        Cursor cursor = db.rawQuery(tmpSql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
            String terminusName = cursor.getString(cursor.getColumnIndex(CommonConstants.TERMINUS_NAME));
            mTerminus.put(id, terminusName);
        }
    }


    public void setCityInfor(SQLiteDatabase db) {

        if (mHashLocation.size() > 0) return;


        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);
        Cursor cursor = db.rawQuery(tmpSql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
            String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
            mHashLocation.put(id, cityEnName);
        }
        cursor.close();
    }

    public void setCityKoInfor(SQLiteDatabase db) {

        if (mHashKoLocation.size() > 0) return;

        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);
        Cursor cursor = db.rawQuery(tmpSql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
            String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_NAME));
            mHashKoLocation.put(id, cityEnName);
        }
        cursor.close();
    }


    public String checkSetting(LocalDBHelper localDBHelper,
                               SQLiteDatabase busDBSqlite, Context context) {


        mContext = context;
        mLocalSaveInfor.localItems.clear();
        String mLocalString = "";

        Cursor getLocalInforCur = localDBHelper.getSettingValue();
        while (getLocalInforCur.moveToNext()) {
            int cityId = getLocalInforCur.getInt(getLocalInforCur
                    .getColumnIndex(LocalDBHelper.TABLE3_LOCAL_F));

            String sql = String.format("SELECT * FROM %s where %s=%s",
                    CommonConstants.TBL_CITY, CommonConstants.CITY_ID, cityId);

            Cursor locationCur = busDBSqlite.rawQuery(sql, null);

            if (locationCur.moveToNext()) {

                LocalItem localItem = new LocalItem();
                String cityName = locationCur.getString(locationCur
                        .getColumnIndex(CommonConstants.CITY_NAME));
                String cityEnName = locationCur.getString(locationCur
                        .getColumnIndex(CommonConstants.CITY_EN_NAME));
                localItem.cityId = cityId;
                localItem.enName = cityEnName;
                localItem.koName = cityName;


                //지역을 추가로 입력
                if (mLocalString.equals("")) {
                    mLocalString = String.valueOf(localItem.cityId);
                } else {
                    mLocalString = mLocalString + "^" + String.valueOf(localItem.cityId);
                }


                mLocalSaveInfor.localItems.add(localItem);

            }
            locationCur.close();
        }
        getLocalInforCur.close();

        if (mLocalSaveInfor.localItems.size() == 0) {
            Toast.makeText(getApplicationContext(), "지역을 하나이상 선택해주세요", Toast.LENGTH_SHORT)
                    .show();
            showLocationDialog(localDBHelper, busDBSqlite, context);
            return mLocalString;
        } else {


            if (!mIsFirstScan) {
                mIsFirstScan = true;
                int versionCode;
                try {
                    versionCode = getPackageManager().getPackageInfo(getApplicationInfo().packageName, 0).versionCode;
                    mCommonNotiLib.getRecentVersion(versionCode, mLocalString);


                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }


            return mLocalString;
        }
    }

    public void showLocationDialog(final LocalDBHelper localDBHelper,
                                   final SQLiteDatabase busDBSqlite, final Context context) {

        LayoutInflater _inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = _inflater.inflate(R.layout.common_list, null);

        RelativeLayout parentLayout = (RelativeLayout) view.findViewById(R.id.common_list_layout);

        final ListView listView = (ListView) view.findViewById(R.id.list);

        ArrayList<LocalItem> tmpLocalItemList = new ArrayList<LocalItem>();
        final ArrayList<LocalItem> localItemList = new ArrayList<LocalItem>();


        //기본 데이터 입력
        String areaSql = String.format("SELECT * FROM %s", CommonConstants.TBL_AREA);
        Cursor areaCursor = busDBSqlite.rawQuery(areaSql, null);
        while (areaCursor.moveToNext()) {
            String enName = areaCursor.getString(areaCursor.getColumnIndex(CommonConstants.AREA_EN_NAME));
            String koName = areaCursor.getString(areaCursor.getColumnIndex(CommonConstants.AREA_NAME));
            int id = areaCursor.getInt(areaCursor.getColumnIndex(CommonConstants.AREA_ID));

            LocalItem localItem = new LocalItem();
            localItem.koName = koName;
            localItem.enName = enName;
            localItem.areaId = id;
            localItem.isArea = true;
            tmpLocalItemList.add(localItem);
        }
        areaCursor.close();


        for (int i = 0; i < tmpLocalItemList.size(); i++) {
            LocalItem localItem = tmpLocalItemList.get(i);
            String citySql = String.format("SELECT *FROM %s where %s='%s'", CommonConstants.TBL_CITY, CommonConstants.CITY_AREA_ID, localItem.areaId);
            Cursor cityCursor = busDBSqlite.rawQuery(citySql, null);

            localItemList.add(localItem);

            while (cityCursor.moveToNext()) {

                String name = cityCursor.getString(cityCursor.getColumnIndex(CommonConstants.CITY_NAME));
                String enName = cityCursor.getString(cityCursor.getColumnIndex(CommonConstants.CITY_EN_NAME));

                int cityId = cityCursor.getInt(cityCursor.getColumnIndex(CommonConstants.CITY_ID));
                int areaId = cityCursor.getInt(cityCursor.getColumnIndex(CommonConstants.CITY_AREA_ID));

                LocalItem item = new LocalItem();
                item.koName = name;
                item.enName = enName;
                item.cityId = cityId;
                item.areaId = areaId;
                localItemList.add(item);

            }
            cityCursor.close();
        }

        final ArrayList<Integer> localCityId = new ArrayList<Integer>();
        for (int i = 0; i < mLocalSaveInfor.localItems.size(); i++) {
            localCityId.add(mLocalSaveInfor.localItems.get(i).cityId);
            for (int j = 0; j < localItemList.size(); j++) {
                LocalItem item = localItemList.get(j);
                if (item.cityId == mLocalSaveInfor.localItems.get(i).cityId) {
                    item.isChecked = true;
                    break;
                }
            }
        }


        final CommonAdapter commonAdapter = new CommonAdapter(localItemList, context, CommonAdapter.TYPE_COMMON_CHECKBOX);

        String sql = null;
        sql = String.format("SELECT * FROM %s  ", CommonConstants.TBL_CITY);

        Cursor locationCur = busDBSqlite.rawQuery(sql, null);
        ArrayList<String> arrayCity = new ArrayList<String>();
        final ArrayList<Integer> arrayCityId = new ArrayList<Integer>();

        while (locationCur.moveToNext()) {
            String name = locationCur.getString(locationCur
                    .getColumnIndex(CommonConstants.CITY_NAME));
            int cityId = locationCur.getInt(locationCur
                    .getColumnIndex(CommonConstants.CITY_ID));

            arrayCity.add(name);
            arrayCityId.add(cityId);


        }
        locationCur.close();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item, arrayCity);

        final CharSequence[] items = arrayCity
                .toArray(new CharSequence[arrayCity.size()]);
        final boolean[] selectedItems = new boolean[arrayCity.size()];

        for (int i = 0; i < arrayCityId.size(); i++) {
            if (localCityId.contains(arrayCityId.get(i))) {
                selectedItems[i] = true;
            }
        }

        final SBDialog dialog = new SBDialog(context, true);
        dialog.setTitleLayout("지역설정");
        dialog.setViewLayout(parentLayout);
        dialog.setCancelable(false);
        dialog.getPositiveButton("ok").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int isSudoCheck = 0;

                for (int i = 0; i < commonAdapter.getLocalItemList().size(); i++) {

                    if (commonAdapter.getLocalItemList().get(i).koName.equals("경기")
                            || commonAdapter.getLocalItemList().get(i).koName.equals("서울")
                            || commonAdapter.getLocalItemList().get(i).koName.equals("인천")) {
                        if (commonAdapter.getLocalItemList().get(i).isChecked) {

                            isSudoCheck++;
                        }
                    }
                }


                if ((isSudoCheck > 0 && isSudoCheck < 3) && !mIsNotChange) {

                    final SBDialog subdialog = new SBDialog(context);
                    subdialog.setViewLayout("수도권 전체지역을 선택하시는 편이 좋습니다. 수도권 전체를 선택하시겠습니까?");
                    subdialog.getPositiveButton("ok").setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub


                            for (int i = 0; i < localItemList.size(); i++) {
                                if (localItemList.get(i).koName.equals("경기")
                                        || localItemList.get(i).koName.equals("서울")
                                        || localItemList.get(i).koName.equals("인천")) {
                                    localItemList.get(i).isChecked = true;

                                    if (commonAdapter != null) {
                                        commonAdapter.notifyDataSetChanged();
                                    }

                                }
                            }

                            subdialog.dismiss();


                        }

                    });
                    subdialog.getNegativeButton("cancel").setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            // TODO Auto-generated method stub
                            mIsNotChange = true;

                            subdialog.dismiss();

                        }

                    });
                    subdialog.show();


                    return;
                }


                for (int i = 0; i < commonAdapter.getLocalItemList().size(); i++) {
                    if (commonAdapter.getLocalItemList().get(i).isChecked
                            && !commonAdapter.getLocalItemList().get(i).isArea) {

                        localDBHelper.deleteSettingCity(commonAdapter.getLocalItemList().get(i).cityId);
                        localDBHelper.insertSettingCity(commonAdapter.getLocalItemList().get(i).cityId);

                    } else if (!commonAdapter.getLocalItemList().get(i).isChecked
                            && !commonAdapter.getLocalItemList().get(i).isArea) {
                        localDBHelper.deleteSettingCity(commonAdapter.getLocalItemList().get(i).cityId);
                    }
                }


                String cityResult = checkSetting(localDBHelper, busDBSqlite, context);
                dialog.dismiss();


                //지역 설정 완료 후 뜨도록 조정
                if (!mIsFirstScan) {
                    mIsFirstScan = true;
                    int versionCode;
                    try {
                        versionCode = getPackageManager().getPackageInfo(getApplicationInfo().packageName, 0).versionCode;
                        mCommonNotiLib.getRecentVersion(versionCode, cityResult);

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

                mIsNotChange = false;


            }
        });

        dialog.getNegativeButton("cancel").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (localCityId.size() == 0) {
                    Toast.makeText(getApplicationContext(), "지역을 하나이상 선택해주세요", Toast.LENGTH_SHORT).show();
                    showLocationDialog(localDBHelper, busDBSqlite, context);
                }
                dialog.dismiss();
            }
        });
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                listView.setAdapter(commonAdapter);
            }
        });
        dialog.show();

    }


    @Override
    public void onReceiveRecent(RecentItem item) {

        if (mPref != null) {

            int blockVersion = mPref.getInt(Constants.PREF_RECENTVERSION, 0);

            String localCityId = "";

            if (blockVersion < item.id) {

                if (item.localNotiItem.size() > 0) {

                    for (int i = 0; i < item.localNotiItem.size(); i++) {

                        LocalNotiItem localNotiItem = item.localNotiItem.get(i);

                        int localVersion = mPref.getInt(localNotiItem.cityId, 0);

                        if (localVersion < Integer.parseInt(localNotiItem.id)) {


                            if (localCityId.equals("")) {

                                localCityId = localNotiItem.id;
                            } else {

                                localCityId = localCityId + "^" + localNotiItem.id;

                            }

                        }
                    }
                }


                if (mCommonNotiLib != null) {
                    mCommonNotiLib.getNotification(item.id, localCityId);
                }


            } else {
                //지역 로컬 값이 있는지 확인

                if (item.localNotiItem.size() > 0) {

                    for (int i = 0; i < item.localNotiItem.size(); i++) {

                        LocalNotiItem localNotiItem = item.localNotiItem.get(i);

                        int localVersion = mPref.getInt(localNotiItem.cityId, 0);

                        if (localVersion < Integer.parseInt(localNotiItem.id)) {

                            if (localCityId.equals("")) {

                                localCityId = localNotiItem.id;
                            } else {

                                localCityId = localNotiItem.id + "^" + localNotiItem.id;

                            }

                        }
                    }
                }


                if (mCommonNotiLib != null) {
                    mCommonNotiLib.getNotification(0, localCityId);
                }


            }


        }


    }


    public void localNotiDialog(final NotificationItem item, String resultString) {

        new AlertDialog.Builder(mContext).setTitle("지역별 공지사항")
                .setMessage(String.valueOf(Html.fromHtml(resultString))).setPositiveButton("확인", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

            }
        }).setNegativeButton("다시보지않기", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub


                for (int i = 0; i < item.localNotiItem.size(); i++) {
                    LocalNotiItem localNotiItem = item.localNotiItem.get(i);

                    if (mPref != null) {
                        mPref.edit().putInt(localNotiItem.cityId, item.id).commit();
                    }

                }


            }
        }).create().show();


    }


    String mLocalNotiStrResult = "";

    @Override
    public void onReceiveNoti(final NotificationItem item) {
        // TODO Auto-generated method stub

        if (mContext == null) return;


        mLocalNotiStrResult = "";


        if (item.localNotiItem != null && item.localNotiItem.size() > 0) {

            for (int i = 0; i < item.localNotiItem.size(); i++) {
                LocalNotiItem localNotiItem = item.localNotiItem.get(i);

                if (mLocalNotiStrResult.equals("")) {
                    mLocalNotiStrResult = localNotiItem.body;
                } else {
                    mLocalNotiStrResult = mLocalNotiStrResult + "<br>-----------------------<br>" + localNotiItem.body;
                }
            }

        }


        if (item.title == null) {

            if (!mLocalNotiStrResult.equals("")) {
                localNotiDialog(item, mLocalNotiStrResult);
            }


        } else {

            if (item.buttonType == 2) {

                new AlertDialog.Builder(mContext).setTitle(item.title)
                        .setMessage(String.valueOf(Html.fromHtml(item.contents))).setPositiveButton("확인", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        if (!mLocalNotiStrResult.equals("")) {
                            localNotiDialog(item, mLocalNotiStrResult);
                        }

                    }
                }).setNegativeButton("다시보지않기", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        if (mPref != null) {
                            mPref.edit().putInt(Constants.PREF_RECENTVERSION, item.id).commit();
                        }

                        if (!mLocalNotiStrResult.equals("")) {
                            localNotiDialog(item, mLocalNotiStrResult);
                        }


                    }
                }).create().show();


            } else {

                String buttonName = "";

                if (item.buttonName == null || (item.buttonName != null && item.buttonName.trim().length() == 0)) {
                    buttonName = "별점 주러 가기";
                } else {
                    buttonName = item.buttonName;
                }


                new AlertDialog.Builder(mContext).setTitle(item.title)
                        .setMessage(String.valueOf(Html.fromHtml(item.contents))).setPositiveButton("확인", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        if (!mLocalNotiStrResult.equals("")) {
                            localNotiDialog(item, mLocalNotiStrResult);
                        }

                    }
                }).setNegativeButton("다시보지않기", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (mPref != null) {
                            mPref.edit().putInt(Constants.PREF_RECENTVERSION, item.id).commit();
                        }

                        if (!mLocalNotiStrResult.equals("")) {
                            localNotiDialog(item, mLocalNotiStrResult);
                        }

                    }
                }).setNeutralButton(buttonName, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        if (item.buttonName == null || (item.buttonName != null && item.buttonName.trim().length() == 0)) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri u = Uri.parse("market://details?id=teamdoppelganger.smarterbus");
                            i.setData(u);
                            startActivity(i);

                        } else {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri u = Uri.parse(item.link);
                            i.setData(u);
                            startActivity(i);

                        }

                    }
                }).show();


            }


        }


    }

    public String getADID() {
        return ADID;
    }

    public void setADID(String ADID) {
        this.ADID = ADID;
    }


}
