package teamdoppelganger.smarterbus;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.admixer.AdAdapter;
import com.admixer.AdInfo;
import com.admixer.AdMixerManager;
import com.admixer.AdView;
import com.admixer.AdViewListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.LatLng;
import com.mocoplex.adlib.AdlibManager;
import com.smart.lib.CommonConstants;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.AutoItem;
import teamdoppelganger.smarterbus.item.TagoArrayItem;
import teamdoppelganger.smarterbus.item.TagoBusItem;
import teamdoppelganger.smarterbus.item.TagoItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.LineImageView;

public class SBPathResultActivity extends SherlockFragmentActivity {

    AutoItem mStartAutoItem, mEndAutoItem;
    TagoArrayItem mTagoItem;

    ListView mListView;
    PathAdapter mPathAdapter;
    SharedPreferences mPref;

    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;

    OnBackStackChangedListener mBackStackChangeListener;

    HashMap<Integer, String> mHashLocation;
    HashMap<Integer, String> mBusTypeHash;

    ImageView mCancelBtn;

    ProgressBar mProgress;

    public String mClassName;

    AdlibManager mAdlibManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smarterbus_result);

        mClassName = getClass().getSimpleName();

        mHashLocation = new HashMap<Integer, String>();

        if (mBackStackChangeListener == null) {
            getListener();
            getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangeListener);
        }

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);

        mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);
        mLocalDBHelper = new LocalDBHelper(getApplicationContext());
        mBusTypeHash = ((SBInforApplication) getApplicationContext()).mBusTypeHash;

        setCityInfor(mBusDbSqlite);
        mTagoItem = (TagoArrayItem) getIntent().getExtras().getSerializable(Constants.INTENT_TAGOITEM);
        mStartAutoItem = (AutoItem) getIntent().getExtras().getSerializable(Constants.INTENT_STARTITEM);
        mEndAutoItem = (AutoItem) getIntent().getExtras().getSerializable(Constants.INTENT_ENDITEM);

        ((TextView) findViewById(R.id.headerStopName)).setText(mStartAutoItem.name + " > " + mEndAutoItem.name);

        initView();

        if (mTagoItem.tagoArrayItem.get(0).mapId != null) {
            ((TextView) findViewById(R.id.resultCount)).setText("검색결과 " + mTagoItem.tagoArrayItem.size() + "건");
            mPathAdapter = new PathAdapter();
            mListView.setAdapter(mPathAdapter);

            mProgress.setVisibility(View.GONE);

            Tracker t = ((SBInforApplication) getApplication()).getTracker(
                    SBInforApplication.TrackerName.APP_TRACKER);
            t.enableAdvertisingIdCollection(true);
            t.setScreenName("경로 검색 결과");
            t.send(new HitBuilders.AppViewBuilder().build());
        }


        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {


                findViewById(R.id.listView).setVisibility(View.GONE);
                findViewById(R.id.coverLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.orderSection).setVisibility(View.GONE);


                SBMapFragment mapFragment = new SBMapFragment(mBusDbSqlite, mLocalDBHelper);


                TagoItem item = mTagoItem.tagoArrayItem.get(position);


                mapFragment.setMode(Constants.MAP_MODE_TAGO_PATH);
                mapFragment.setStartEndItem(mStartAutoItem, mEndAutoItem);

                if (item.mapId == null) {
                    mapFragment.setCityName(mHashLocation.get(Integer.parseInt(item.cityId)));


                }

                mapFragment.setTagoItem(mTagoItem);
                mapFragment.setSelectTagoPosition(position);
                getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, mapFragment).addToBackStack("search").commit();

                Tracker t = ((SBInforApplication) getApplication()).getTracker(
                        SBInforApplication.TrackerName.APP_TRACKER);
                t.enableAdvertisingIdCollection(true);
                t.setScreenName("경로 검색 결과 (지도)");
                t.send(new HitBuilders.AppViewBuilder().build());

            }
        });


        if (mTagoItem.tagoArrayItem.get(0).mapId == null) {
            new GetPathDistance().execute();
        }

    }

    public void setCityInfor(SQLiteDatabase db) {

        String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);

        Cursor cursor = db.rawQuery(tmpSql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
            String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
            mHashLocation.put(id, cityEnName);
        }
        cursor.close();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        findViewById(R.id.orderSection).setVisibility(View.VISIBLE);
    }


    private void initView() {

        mListView = (ListView) findViewById(R.id.listView);
        mCancelBtn = (ImageView) findViewById(R.id.cancelBtn);
        mProgress = (ProgressBar) findViewById(R.id.progress);

        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });

        mAdlibManager = new AdlibManager(Constants.ADLIB_INFO_API_KEY);
        mAdlibManager.onCreate(this);
        mAdlibManager.setBannerFailDelayTime(5);
        mAdlibManager.setAdsHandler(new Handler() {
            public void handleMessage(Message message) {
                try {
                    switch (message.what) {
                        case AdlibManager.DID_SUCCEED:
                            if (findViewById(R.id.adlib).getVisibility() == View.GONE) {
                                findViewById(R.id.adlib).setVisibility(View.VISIBLE);
                                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up);
                                findViewById(R.id.admixer_layout).startAnimation(slide_up);
                            }
                            break;
                        case AdlibManager.DID_ERROR:
                            break;
                    }
                } catch (Exception e) {

                }
            }
        });
        mAdlibManager.setAdsContainer(R.id.adlib);

    }


    class GetPathDistance extends AsyncTask<String, String, String> {

        ArrayList<TagoItem> _tagoArray;
        ArrayList<LatLng> _tagoStopItem;


        @Override
        protected String doInBackground(String... params) {


            for (int i = 0; i < mTagoItem.tagoArrayItem.size(); i++) {
                TagoItem item = mTagoItem.tagoArrayItem.get(i);
                if (item.mapId != null) {

                    break;
                }

                String sql = String.format("SELECT * FROM %s_route where _id=%s", mHashLocation.get(Integer.parseInt(item.cityId)), item.busId);
                Cursor cursor = mBusDbSqlite.rawQuery(sql, null);

                while (cursor.moveToNext()) {
                    mTagoItem.tagoArrayItem.get(i).busName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                    mTagoItem.tagoArrayItem.get(i).stopList = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
                    mTagoItem.tagoArrayItem.get(i).busType = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));

                    int startIndex = 0;
                    int endIndex = 0;
                    int tempIndex;
                    String[] stopList = mTagoItem.tagoArrayItem.get(i).stopList.split("/");
                    boolean isInverse = false;


                    for (int j = 0; j < stopList.length; j++) {
                        if (item.srcStopId.equals(stopList[j])) {
                            startIndex = j;
                        }
                        if (item.endStopId.equals(stopList[j])) {
                            endIndex = j;
                        }

                    }

                    if (startIndex > endIndex) {
                        tempIndex = startIndex;
                        startIndex = endIndex;
                        endIndex = tempIndex;
                        isInverse = true;
                        mTagoItem.tagoArrayItem.get(i).isInverse = isInverse;
                    }

                    mTagoItem.tagoArrayItem.get(i).startIndex = startIndex;
                    mTagoItem.tagoArrayItem.get(i).endIndex = endIndex;
                    mTagoItem.tagoArrayItem.get(i).gap = item.endIndex - item.startIndex;
                }

                cursor.close();

            }


            _tagoArray = mTagoItem.tagoArrayItem;

            ArrayList<TagoItem> removeList = new ArrayList<TagoItem>();

            LinkedHashMap<Integer, TagoItem> hashTmp = new LinkedHashMap<Integer, TagoItem>();

            for (int i = 0; i < _tagoArray.size(); i++) {

                _tagoStopItem = new ArrayList<LatLng>();

                TagoItem item = _tagoArray.get(i);

                if (item.stopList == null) continue;

                String[] stopList = item.stopList.split("/");

                TagoItem tempTagItem = hashTmp.get(item.busId);
                if (tempTagItem != null) {

                }

                String sql = "";


                int endValue = 0;

                for (int j = item.startIndex; j < item.endIndex + 1; j++) {

                    String tmpSql = String.format("SELECT %s,%s,%s as b from %s_stop where _id=%s ", CommonConstants.BUS_STOP_LOCATION_X, CommonConstants.BUS_STOP_LOCATION_Y, j, mHashLocation.get(Integer.parseInt(item.cityId)), stopList[j]);

                    if (sql.equals("")) {
                        sql = tmpSql;
                    } else {
                        sql = sql + " UNION " + tmpSql;
                    }


                }

                sql = sql.trim() + " order by b;";


                if (sql.equals(" order by b;")) {
                    removeList.add(_tagoArray.get(i));
                    continue;
                }


                Cursor cursor = mBusDbSqlite.rawQuery(sql, null);


                if (cursor != null) {

                    cursor.moveToFirst();

                }

                while (cursor.moveToNext()) {
                    double locationX = cursor.getDouble(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X)) / 1e6;
                    double locationY = cursor.getDouble(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y)) / 1e6;

                    Location locTmp = new Location("locTmp");
                    locTmp.setLatitude(locationY);
                    locTmp.setLongitude(locationX);

					/*tmpLocationStart.dis*/


                    LatLng latLang = new LatLng(locationY, locationX);
                    _tagoStopItem.add(latLang);
                    endValue++;

                }

                if (_tagoStopItem.size() < 2) continue;

                cursor.close();

                double distance = 0, walk = 0, walk2 = 0;
                for (int j = 0; j < _tagoStopItem.size() - 1; j++) {

                    Location locationStart = new Location("start");
                    Location locationEnd = new Location("end");

                    locationStart.setLatitude(_tagoStopItem.get(j).latitude);
                    locationStart.setLongitude(_tagoStopItem.get(j).longitude);

                    locationEnd.setLatitude(_tagoStopItem.get(j + 1).latitude);
                    locationEnd.setLongitude(_tagoStopItem.get(j + 1).longitude);

                    distance = distance + locationStart.distanceTo(locationEnd);


                }


                if (_tagoStopItem.size() > 0) {
                    //걷는 거리 산정
                    Location tempLocationStart = new Location("start");
                    Location tempLocationEnd = new Location("end");

                    if (item.isInverse) {
                        tempLocationStart.setLatitude(mEndAutoItem.latitude);
                        tempLocationStart.setLongitude(mEndAutoItem.longtude);

                        tempLocationEnd.setLatitude(mStartAutoItem.latitude);
                        tempLocationEnd.setLongitude(mStartAutoItem.longtude);
                    } else {
                        tempLocationStart.setLatitude(mStartAutoItem.latitude);
                        tempLocationStart.setLongitude(mStartAutoItem.longtude);

                        tempLocationEnd.setLatitude(mEndAutoItem.latitude);
                        tempLocationEnd.setLongitude(mEndAutoItem.longtude);
                    }


                    Location tempLocationStartStop = new Location("startStop");
                    tempLocationStartStop.setLatitude(_tagoStopItem.get(0).latitude);
                    tempLocationStartStop.setLongitude(_tagoStopItem.get(0).longitude);


                    Location tempLocationEndStop2 = new Location("endStop");
                    tempLocationEndStop2.setLatitude(_tagoStopItem.get(endValue - 1).latitude);
                    tempLocationEndStop2.setLongitude(_tagoStopItem.get(endValue - 1).longitude);

                    if (tempLocationStart.distanceTo(tempLocationEndStop2) < tempLocationEnd.distanceTo(tempLocationEndStop2)
                            || tempLocationStart.distanceTo(tempLocationStartStop) > tempLocationEnd.distanceTo(tempLocationStartStop)) {
                        continue;
                    }


                    walk2 = tempLocationEnd.distanceTo(tempLocationEndStop2);
                    walk = tempLocationStart.distanceTo(tempLocationStartStop) + walk2;

                }


                String ff = String.format("%.1f", (walk) / 1000);


                item.totalDistance = String.valueOf(distance + walk);
                item.totalWalk = String.valueOf(walk);


                if (tempTagItem != null) {

                    if (Double.parseDouble(tempTagItem.totalWalk) > Double.parseDouble(item.totalWalk)) {
                        if (Double.parseDouble(tempTagItem.totalDistance) > Double.parseDouble(item.totalDistance) + 200) {
                            hashTmp.put(item.busId, item);
                        }


                    }
                } else {

                    hashTmp.put(item.busId, item);

                }

            }


            _tagoArray.clear();

            Iterator iterator = hashTmp.entrySet().iterator();
            double min = 0;
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();

                TagoItem tagItem = (TagoItem) entry.getValue();
                tagItem.busId = (Integer) entry.getKey();

                if (min == 0) {
                    min = Double.parseDouble(tagItem.totalDistance);
                } else {

                    if (min > Double.parseDouble(tagItem.totalDistance)) {
                        min = Double.parseDouble(tagItem.totalDistance);
                    }
                }
            }
            iterator = hashTmp.entrySet().iterator();


            while (iterator.hasNext()) {

                Entry entry = (Entry) iterator.next();

                TagoItem tagItem = (TagoItem) entry.getValue();
                tagItem.busId = (Integer) entry.getKey();

                if (min + 5 * 1000 > Double.parseDouble(tagItem.totalDistance)) {
                    _tagoArray.add(tagItem);
                }

            }

            Collections.sort(_tagoArray, myComparator);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mTagoItem.tagoArrayItem = _tagoArray;

            if (mPathAdapter != null) {
                mPathAdapter.notifyDataSetChanged();
            } else {
                mPathAdapter = new PathAdapter();
                mListView.setAdapter(mPathAdapter);
            }

            ((TextView) findViewById(R.id.resultCount)).setText("검색결과 " + mTagoItem.tagoArrayItem.size() + "건");
            mProgress.setVisibility(View.GONE);
        }

    }


    private final static Comparator<TagoItem> myComparator = new Comparator<TagoItem>() {

        private final Collator collator = Collator.getInstance();

        @Override

        public int compare(TagoItem object1, TagoItem object2) {
            return (Double.parseDouble(object1.totalDistance) > Double.parseDouble(object2.totalDistance) ? 1 : -1);
        }
    };


    class PathAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTagoItem.tagoArrayItem.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder viewHolder = null;

            if (view == null) {
                viewHolder = new ViewHolder();
                view = getLayoutInflater().inflate(R.layout.tago_result_row,
                        parent, false);

                viewHolder.detailResult = (TextView) view.findViewById(R.id.detailResult);
                viewHolder.detailResult2 = (TextView) view.findViewById(R.id.detailResult2);
                viewHolder.detailResult3 = (TextView) view.findViewById(R.id.detailResult3);
                viewHolder.patchResult = (TextView) view.findViewById(R.id.patchResult);
                viewHolder.pathFee = (TextView) view.findViewById(R.id.patchFee);
                viewHolder.detailInformation = (TextView) view.findViewById(R.id.detailInformation);

                viewHolder.blank1 = (TextView) view.findViewById(R.id.blank1);
                viewHolder.blank2 = (TextView) view.findViewById(R.id.blank2);


                view.setTag(viewHolder);
            } else {

                viewHolder = (ViewHolder) view.getTag();
            }

            String result = "", result2 = "", result3 = "";
            String pathResult = "";
            String fee = "";

            TagoItem item = mTagoItem.tagoArrayItem.get(position);
            if (item.mapId != null) {
                for (int i = 0; i < item.mTagoBusList.size(); i++) {

                    TagoBusItem tagoBusItem = item.mTagoBusList.get(i);

                    if (i == 0) {
                        result = tagoBusItem.busNo;
                        pathResult = tagoBusItem.startName;
                    } else if (i == 1) {
                        result2 = tagoBusItem.busNo;
                        pathResult = pathResult + " > " + tagoBusItem.startName;
                    } else if (i == 2) {
                        result3 = tagoBusItem.busNo;
                        pathResult = pathResult + " > " + tagoBusItem.startName;
                    }

                    if (item.mTagoBusList.size() - 1 == i) {
                        pathResult = pathResult + " > " + tagoBusItem.endName;
                    }


                }

                String totalResult = "";
                if (item.totalTime != null) {
                    totalResult = "약" + item.totalTime + "분";
                }

                if (item.totalDistance != null) {

                    try {
                        String temp = String.valueOf(Math.round(Float.parseFloat(item.totalDistance) / 1000));

                        if (Integer.parseInt(item.totalDistance) > 1000) {
                            temp = String.format("%.1f", Float.parseFloat(item.totalDistance) / 1000) + "Km";
                        } else {
                            temp = item.totalDistance + "m";
                        }


                        totalResult = totalResult + " | " + temp;
                    } catch (Exception e) {
                    }
                    ;
                }

                if (item.totalWalk != null) {
                    totalResult = totalResult + "(" + item.totalWalk + "M)";
                }

                try {
                    viewHolder.detailResult.setTextColor(Color.parseColor("#000000"));
                } catch (Exception e) {
                }
                ;

                if (result.equals("")) {
                    viewHolder.detailResult.setVisibility(View.GONE);
                } else {
                    viewHolder.detailResult.setVisibility(View.VISIBLE);
                    viewHolder.detailResult.setText(result);
                }

                if (result2.equals("")) {
                    viewHolder.detailResult2.setVisibility(View.GONE);
                    viewHolder.blank1.setVisibility(View.GONE);
                    viewHolder.blank2.setVisibility(View.GONE);

                } else {
                    viewHolder.blank1.setVisibility(View.VISIBLE);
                    viewHolder.detailResult2.setVisibility(View.VISIBLE);
                    viewHolder.detailResult2.setText(result2);
                }

                if (result3.equals("")) {
                    viewHolder.detailResult3.setVisibility(View.GONE);
                    viewHolder.blank2.setVisibility(View.GONE);
                } else {
                    viewHolder.blank2.setVisibility(View.VISIBLE);
                    viewHolder.detailResult3.setVisibility(View.VISIBLE);
                    viewHolder.detailResult3.setText(result3);

                }

                viewHolder.detailInformation.setText(totalResult);
                viewHolder.patchResult.setText(pathResult);

            } else {


                result = item.busName;
                viewHolder.detailResult.setVisibility(View.VISIBLE);
                viewHolder.detailResult.setText(result);

                try {
                    String color = mBusTypeHash.get(Integer.parseInt(item.busType));
                    viewHolder.detailResult.setTextColor(Color.parseColor("#" + color));
                } catch (Exception e) {
                }
                ;


                viewHolder.detailResult2.setVisibility(View.GONE);
                viewHolder.detailResult3.setVisibility(View.GONE);

                viewHolder.blank1.setVisibility(View.GONE);
                viewHolder.blank2.setVisibility(View.GONE);


                String tempDistance = "";
                String tempWalk = "";
                if (item.totalDistance != null) {
                    tempDistance = String.valueOf(Math.round(Float.parseFloat(item.totalDistance) / 1000));
                    if (Double.parseDouble(item.totalDistance) > 1000) {
                        tempDistance = String.format("%.1f", Float.parseFloat(item.totalDistance) / 1000) + "Km";
                    } else {
                        tempDistance = Math.round(Float.parseFloat(item.totalDistance)) + "m";
                    }
                }


                if (item.totalWalk != null) {
                    tempWalk = String.valueOf(Math.round(Float.parseFloat(item.totalWalk) / 1000));
                    if (Double.parseDouble(item.totalWalk) > 1000) {
                        tempWalk = String.format("%.1f", Float.parseFloat(item.totalWalk) / 1000) + "Km";
                    } else {
                        tempWalk = Math.round(Float.parseFloat(item.totalWalk)) + "m";
                    }
                }


                pathResult = item.srcStopName + " > " + item.dstStopName;
                viewHolder.patchResult.setText(pathResult);
                viewHolder.detailInformation.setText(tempDistance + "(걷는거리:" + tempWalk + ")");

            }

            return view;

        }


        class ViewHolder {
            TextView detailResult, detailResult2, detailResult3, patchResult, pathFee, detailInformation;
            TextView num;
            LineImageView busImg;
            TextView blank1, blank2;

        }

    }


    private void getListener() {
        mBackStackChangeListener = new OnBackStackChangedListener() {
            public void onBackStackChanged() {

                FragmentManager manager = getSupportFragmentManager();
                if (manager.getBackStackEntryCount() == 0) {

                    findViewById(R.id.listView).setVisibility(View.VISIBLE);
                    findViewById(R.id.coverLayout).setVisibility(View.GONE);

                }


            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdlibManager != null)
            mAdlibManager.onResume(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdlibManager != null)
            mAdlibManager.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdlibManager != null)
            mAdlibManager.onDestroy(this);

    }


}
