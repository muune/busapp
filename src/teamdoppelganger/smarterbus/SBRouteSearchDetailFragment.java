package teamdoppelganger.smarterbus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.smart.lib.CommonConstants;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import teamdoppelganger.smarterbus.adapter.CommonAdapter;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.service.BusAlarmService;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;
import teamdoppelganger.smarterbus.util.common.GetData.GetDataListener;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.BlockRelative;
import teamdoppelganger.smarterbus.util.widget.LineImageView;


public class SBRouteSearchDetailFragment extends SBBaseFragment implements GetDataListener, OnItemClickListener {

    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mHashLocation;

    public HashMap<Integer, String> mTerminus;
    HashMap<Integer, String> mBusTypeHash;

    View mView;

    Handler mHandler;
    Runnable mRefreshRunnable;

    boolean mIsInit = false;
    boolean mIsTimeRequired = false;

    String depStationName = null;
    String arvStationName = null;

    int depPos = 0;
    int arvPos = 0;

    SBDialog mTimeRequiredDialog;

    public SBRouteSearchDetailFragment() {

    }

    @SuppressLint("ValidFragment")
    public SBRouteSearchDetailFragment(SQLiteDatabase db,
                                       LocalDBHelper localDBHelper) {
        super(R.layout.routesearch_detail, db, localDBHelper);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mIsTimeRequired = false;
        mTimeRequiredBtn.setSelected(false);
        mTimeRequiredBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.sub_btn_normal));
        depStationName = null;
        arvStationName = null;
        depPos = 0;
        arvPos = 0;
    }


    public void onLayoutFinish(View view) {

        mHashLocation = ((SBInforApplication) getActivity().getApplicationContext()).mHashKoLocation;
        mHashLocationEng = ((SBInforApplication) getActivity().getApplicationContext()).mHashLocation;
        mBusTypeHash = ((SBInforApplication) getActivity().getApplicationContext()).mBusTypeHash;

        mBusStopItem = new ArrayList<BusStopItem>();
        mDetailAdapter = new DetailAdapter();
        initView(view);

        checkFavorite();

    }

    ;


    private void checkFavorite() {


        String sql = null;

        if (mBusStopItem != null) {

            if (mBusRouteItem.localInfoId.equals(CommonConstants.CITY_GU_MI._cityId)) {
                sql = String.format("SELECT * FROM %s where %s='%s' and %s='%s' and %s='%s' and %s='%s' and %s='%s'", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_TYPEID_F, mBusRouteItem.busRouteApiId,
                        LocalDBHelper.TABLE_TYPEID2_F, mBusRouteItem.busRouteApiId2, LocalDBHelper.TABLE_CITY_F, mBusRouteItem.localInfoId, LocalDBHelper.TABLE_TYPE_F, Constants.FAVORITE_TYPE_BUS, LocalDBHelper.TABLE_TYPE_NICK2, mBusRouteItem.busRouteName);
            } else {
                sql = String.format("SELECT * FROM %s where %s='%s' and %s='%s' and %s='%s' and %s='%s'", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_TYPEID_F, mBusRouteItem.busRouteApiId,
                        LocalDBHelper.TABLE_TYPEID2_F, mBusRouteItem.busRouteApiId2, LocalDBHelper.TABLE_TYPE_F, Constants.FAVORITE_TYPE_BUS, LocalDBHelper.TABLE_CITY_F, mBusRouteItem.localInfoId);
            }

        }


        Cursor cursor = getLocalDBHelper().getReadableDatabase().rawQuery(sql, null);

        if (cursor.getCount() > 0) {
            if (cursor.moveToNext()) {
                mFavoriteId = cursor.getInt(0);
            }
            mFavoBtn.setSelected(true);
        }

        cursor.close();

    }


    GetData mGetData;
    ListView mList;
    BlockRelative mPreLayout;
    LinearLayout mDetailRoute;

    BusRouteItem mBusRouteItem;
    ArrayList<BusStopItem> mBusStopItem;
    DetailAdapter mDetailAdapter;

    TextView mDetailTxt1, mDetailTxt2, mDetailTxt3, mDetailTxt4, mDetailTxt5;
    TextView mCheckTxt;

    ImageView mLineImage;

    //header
    TextView mRouteNameTxt;
    Button mRefreshBtn, mFavoBtn, mPathBtn, mTimeTableBtn, mInforBtn, mTimeRequiredBtn;

    BusStopItem mMemoryBusStopItem;
    String stOrd;
    int mFavoriteId = 999999;

    /**
     * 최초에만 아이템의 값을 채워넣는다.
     *
     * @param item
     * @param isOrderRefesh
     */
    public void setBusRouteItem(BusRouteItem item, boolean isOrderRefesh,
                                HashMap<Integer, String> hashLocationEng, HashMap<Integer, String> terminus, HashMap<Integer, String> hashLocationKo) {
        if (!isOrderRefesh) {

            mTerminus = terminus;
            mHashLocation = hashLocationKo;

            mHashLocationEng = hashLocationEng;


            mBusRouteItem = item;


            String localEnName = mHashLocationEng.get(Integer.parseInt(mBusRouteItem.localInfoId));
            String sql = String.format("SELECT * FROM %s_route where _id='%s'", localEnName, mBusRouteItem._id);


            Cursor cursor = getBusDbSqlite().rawQuery(sql, null);
            if (cursor.moveToNext()) {

                String isUrl = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TIME_TALBE_URL));
                String routeId2 = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));

                if (isUrl.equals("O")) {
                    mBusRouteItem.isTimeTable = true;
                } else {
                    mBusRouteItem.isTimeTable = false;
                }

                mBusRouteItem.busRouteApiId2 = routeId2;


            }
            cursor.close();

        }

        if (mCheckTxt != null) {
            mCheckTxt.setVisibility(View.GONE);
        }

        mGetData = new GetData(this, getBusDbSqlite(), mHashLocationEng);
        mGetData.startBusStopParsing(item);

        if (mHandler == null) {
            mHandler = new Handler();
            mRefreshRunnable = new Runnable() {
                @Override
                public void run() {
                    setBusRouteItem(mBusRouteItem, true, mHashLocationEng, mTerminus, mHashLocation);
                    mHandler.postDelayed(mRefreshRunnable, 15000);
                }
            };

            mHandler.postDelayed(mRefreshRunnable, 15000);
        }
    }


    public void setBusMemory(BusStopItem busStopItem) {

        mMemoryBusStopItem = busStopItem;
        stOrd = mBusRouteItem.stOrd;
    }


    private void initView(View view) {

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName("버스 노선");
        t.send(new HitBuilders.AppViewBuilder().build());

        mList = (ListView) view.findViewById(R.id.detaillistView);
        mPreLayout = (BlockRelative) view.findViewById(R.id.progressLayout);
        mCheckTxt = (TextView) view.findViewById(R.id.netCheckTxt);

        mPreLayout.setVisibility(View.VISIBLE);

        mView = view;

        setHeader(view);
        mList.setAdapter(mDetailAdapter);
        mList.setOnItemClickListener(this);

        mList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

                if (mDetailRoute != null && mDetailRoute.getVisibility() == View.VISIBLE) {
                    mDetailRoute.setVisibility(View.GONE);
                    mLineImage.setVisibility(View.GONE);
                    mInforBtn.setSelected(false);
                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub

            }
        });


        mList.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {


                final BusStopItem busStopItem = mBusStopItem.get(position);

                busStopItem.localInfoId = mBusRouteItem.localInfoId;


                LayoutInflater _inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = _inflater.inflate(R.layout.common_list, null);

                final ListView listView = (ListView) view.findViewById(R.id.list);

                String[] settingList = getResources().getStringArray(R.array.longStopSelect);
                ArrayList<String> arGeneral = new ArrayList<String>();

                for (int i = 0; i < settingList.length; i++) {
                    arGeneral.add(settingList[i]);
                }


                final CommonAdapter commonAdapter = new CommonAdapter(arGeneral, getActivity(), CommonAdapter.TYPE_COMMON_TEXTVIEW2);

                final SBDialog dialog = new SBDialog(getActivity());
                dialog.setViewLayout(view.findViewById(R.id.common_list_layout));
                dialog.setTitleLayout("선택");
                dialog.setOnShowListener(new OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {
                        listView.setAdapter(commonAdapter);
                    }
                });
                dialog.setCancelable(true);
                dialog.show();

                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                            long arg3) {

                        dialog.dismiss();

                        if (position == 0) {

                            FavoriteAndHistoryItem favoriteAndHistoryItem = new FavoriteAndHistoryItem();
                            favoriteAndHistoryItem.busRouteItem = mBusRouteItem;
                            favoriteAndHistoryItem.busRouteItem.busStopApiId = busStopItem.apiId;
                            favoriteAndHistoryItem.busRouteItem.busStopArsId = busStopItem.arsId;
                            favoriteAndHistoryItem.busRouteItem.busStopName = busStopItem.name;
                            favoriteAndHistoryItem.busRouteItem.tmpId = busStopItem.tempId2;
                            favoriteAndHistoryItem.nickName = busStopItem.name;
                            favoriteAndHistoryItem.nickName2 = mBusRouteItem.busRouteName;
                            favoriteAndHistoryItem.type = Constants.FAVORITE_TYPE_BUS_STOP;


                            String selectSql;
                            if (favoriteAndHistoryItem.busRouteItem.busRouteArsId == null) {
                                selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' "
                                        , LocalDBHelper.TABLE_FAVORITE_NAME, Constants.FAVORITE_TYPE_BUS_STOP, busStopItem.localInfoId, favoriteAndHistoryItem.busRouteItem.busRouteApiId
                                        , 0,
                                        favoriteAndHistoryItem.busRouteItem.busStopApiId, favoriteAndHistoryItem.busRouteItem.busStopArsId);

                            } else {


                                if (busStopItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_BU_SAN._cityId))) {

                                    selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' " +
                                                    "and temp1 = '%s'"
                                            , LocalDBHelper.TABLE_FAVORITE_NAME, Constants.FAVORITE_TYPE_BUS_STOP, busStopItem.localInfoId, favoriteAndHistoryItem.busRouteItem.busRouteApiId
                                            , favoriteAndHistoryItem.busRouteItem.busRouteArsId,
                                            favoriteAndHistoryItem.busRouteItem.busStopApiId, favoriteAndHistoryItem.busRouteItem.busStopArsId, busStopItem.tempId2);


                                } else {
                                    selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' "
                                            , LocalDBHelper.TABLE_FAVORITE_NAME, Constants.FAVORITE_TYPE_BUS_STOP, busStopItem.localInfoId, favoriteAndHistoryItem.busRouteItem.busRouteApiId
                                            , favoriteAndHistoryItem.busRouteItem.busRouteArsId,
                                            favoriteAndHistoryItem.busRouteItem.busStopApiId, favoriteAndHistoryItem.busRouteItem.busStopArsId);

                                }

                            }
                            Cursor cursor = getLocalDBHelper().getReadableDatabase().rawQuery(selectSql, null);
                            if (cursor.getCount() == 0) {
                                Intent intent = new Intent(getActivity(), SBEditFavoriteActivity.class);
                                intent.putExtra(Constants.INTENT_FAVORITEITEM, favoriteAndHistoryItem);
                                startActivityForResult(intent, 100);
                            } else {
                                Toast.makeText(getActivity(), "즐겨찾기에 등록 되어 있습니다.", Toast.LENGTH_SHORT).show();
                            }
                            cursor.close();


                        }
                    }
                });


                return true;

            }


        });


        if (!mBusRouteItem.isTimeTable) {
            mTimeTableBtn.setVisibility(View.GONE);
        } else {
            mTimeTableBtn.setVisibility(View.VISIBLE);
        }


    }


    /**
     * header 로직 추가
     */
    private void setHeader(View view) {

        mLineImage = (ImageView) view.findViewById(R.id.line2);
        mRouteNameTxt = ((TextView) view.findViewById(R.id.stopName));

        mRefreshBtn = ((Button) view.findViewById(R.id.refreshBtn));
        mFavoBtn = ((Button) view.findViewById(R.id.favoriteBtn));
        mPathBtn = ((Button) view.findViewById(R.id.pathBtn));
        mTimeRequiredBtn = ((Button) view.findViewById(R.id.timeRequired));
        mTimeTableBtn = ((Button) view.findViewById(R.id.timeTable));
        mInforBtn = ((Button) view.findViewById(R.id.inforBtn));

        if (!mIsInit) {
            mInforBtn.setSelected(true);
            mIsInit = true;
        }

        mIsTimeRequired = false;

        mDetailTxt1 = ((TextView) view.findViewById(R.id.detail1));
        mDetailTxt2 = ((TextView) view.findViewById(R.id.detail2));
        mDetailTxt3 = ((TextView) view.findViewById(R.id.detail3));
        mDetailTxt4 = ((TextView) view.findViewById(R.id.detail4));
        mDetailTxt5 = ((TextView) view.findViewById(R.id.detail5));

        mDetailRoute = (LinearLayout) view.findViewById(R.id.detailRoute);


        ((LinearLayout) getActivity().findViewById(R.id.routeDetailHeader)).setVisibility(View.VISIBLE);
        ((LinearLayout) getActivity().findViewById(R.id.stopDetailHeader)).setVisibility(View.GONE);

        String color = mBusTypeHash.get(mBusRouteItem.busType);
        ((TextView) getActivity().findViewById(R.id.headerBusNum)).setText(mBusRouteItem.busRouteName);

        ((TextView) getActivity().findViewById(R.id.headerBusNum)).setTextColor(Color.parseColor("#" + color));


        ((TextView) getActivity().findViewById(R.id.headerBusType)).setText(getBusName(getBusDbSqlite(), mBusRouteItem.busType));
        ((TextView) getActivity().findViewById(R.id.headerRouteStartEnd)).setText(mTerminus.get(Integer.parseInt(mBusRouteItem.startStop)) + "-"
                + mTerminus.get(Integer.parseInt(mBusRouteItem.endStop)));
        ((TextView) getActivity().findViewById(R.id.headerRouteLocation)).setText(" | " + mHashLocation.get(Integer.parseInt(mBusRouteItem.localInfoId)));


        mRefreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mHandler != null) {
                    mHandler.removeCallbacks(mRefreshRunnable);
                }

                setBusRouteItem(mBusRouteItem, true, mHashLocationEng, mTerminus, mHashLocation);

                if (mHandler != null) {
                    mHandler.postDelayed(mRefreshRunnable, 15000);
                }

                playVibe();
            }
        });


        mPathBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                playVibe();

                Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                        SBInforApplication.TrackerName.APP_TRACKER);
                t.enableAdvertisingIdCollection(true);
                t.setScreenName("버스 노선 지도");
                t.send(new HitBuilders.AppViewBuilder().build());


                String engName = mHashLocationEng.get(Integer.parseInt(mBusRouteItem.localInfoId));

                if (engName.equals(CommonConstants.CITY_JE_JU._engName)
                        || engName.equals(CommonConstants.CITY_YANG_SAN._engName)) {

                    Toast.makeText(getActivity(), "경로 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }

                SBMapFragment mapFragment = new SBMapFragment(getBusDbSqlite(), getLocalDBHelper());

                mapFragment.setBusStopItem(Integer.parseInt(mBusRouteItem.localInfoId), mHashLocationEng.get(Integer.parseInt(mBusRouteItem.localInfoId)));
                mapFragment.setBusRouteItem(mBusRouteItem);
                mapFragment.setMode(Constants.MAP_MODE_ROUTE_PATH);

                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, mapFragment).addToBackStack("search").commit();


            }
        });

        mFavoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                playVibe();


                if (mFavoriteId != 999999) {

                    getLocalDBHelper().deleteFavorite(mFavoriteId);
                    mFavoBtn.setSelected(false);
                    mFavoriteId = 999999;
                    Toast.makeText(getActivity(), "즐겨 찾기에서  삭제 되었습니다", Toast.LENGTH_SHORT).show();
                    return;
                }


                String findSql = String.format("SELECT *FROM %s", LocalDBHelper.TABLE_FAVORITE_NAME);
                Cursor findCursor = getLocalDBHelper().getReadableDatabase().rawQuery(findSql, null);

                if (findCursor != null && findCursor.getCount() >= 25) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_favorite), Toast.LENGTH_SHORT).show();
                    return;
                }


                BusRouteItem busRouteItem = mBusRouteItem;

                String nickName = "";
                if (busRouteItem.endStop != null) {
                    nickName = mTerminus.get(Integer.parseInt(mBusRouteItem.endStop));
                }

                FavoriteAndHistoryItem favoriteAndHistoryItem = new FavoriteAndHistoryItem();
                favoriteAndHistoryItem.busRouteItem = mBusRouteItem;
                favoriteAndHistoryItem.nickName = nickName;
                favoriteAndHistoryItem.nickName2 = mBusRouteItem.busRouteName;
                favoriteAndHistoryItem.type = Constants.FAVORITE_TYPE_BUS;

                Intent intent = new Intent(getActivity(), SBEditFavoriteActivity.class);
                intent.putExtra(Constants.INTENT_FAVORITEITEM, favoriteAndHistoryItem);
                startActivityForResult(intent, 100);


            }
        });

        mTimeRequiredBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playVibe();

                if (mIsTimeRequired) {
                    mIsTimeRequired = false;
                    mTimeRequiredBtn.setSelected(false);
                    mTimeRequiredBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.sub_btn_normal));
                    depStationName = null;
                    arvStationName = null;
                    depPos = 0;
                    arvPos = 0;
                    if (mDetailAdapter != null) {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                } else {
                    mIsTimeRequired = true;
                    mTimeRequiredBtn.setSelected(true);
                    mTimeRequiredBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.sub_btn_pressed));


                    if (mDetailAdapter != null) {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                }

            }
        });

        mTimeTableBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playVibe();
                if (mBusRouteItem != null) {

                    String localEnName = mHashLocationEng.get(Integer.parseInt(mBusRouteItem.localInfoId));
                    String url = null;
                    String sendType = "get";
                    String preUrl = "", urlParam = "";

                    if (localEnName.equals("GEOJE")
                            || localEnName.equals("SUNCHEON")
                            || localEnName.equals(CommonConstants.CITY_MIR_YANG._engName)) {
                        sendType = "post";
                    }


                    if (localEnName.equals("CHANGWON")
                            || localEnName.equals("DAEJEON")
                            || localEnName.equals("BUSAN")
                            ) {

                        url = Constants.SERVER_TIMETABLE + localEnName + "/" + localEnName + "_" + mBusRouteItem.busRouteApiId + ".html";


                    } else if (localEnName.equals("CHEONAN")) {

                        url = Constants.SERVER_TIMETABLE + localEnName + "/" + localEnName + "_" + mBusRouteItem.busRouteName
                                + "_" + mBusRouteItem.busRouteApiId + "_" + mBusRouteItem.busRouteApiId2 + ".html";

                    } else {

                        String urslSql = String.format("SELECT * FROM %s where %s='%s'", CommonConstants.TBL_CITY, CommonConstants.CITY_EN_NAME, localEnName);
                        Cursor urlGetCursor = getBusDbSqlite().rawQuery(urslSql, null);

                        if (urlGetCursor.moveToNext()) {
                            String cityPrefix = urlGetCursor.getString(urlGetCursor.getColumnIndex(CommonConstants.CITY_TIME_TABLE_PREFIX));
                            String[] param = urlGetCursor.getString(urlGetCursor.getColumnIndex(CommonConstants.CITY_TIME_TABLE_PARAM)).split(",");

                            if (cityPrefix != null && cityPrefix.length() > 0) {


                                if (cityPrefix.contains("?")) {
                                    preUrl = cityPrefix.split("\\?")[0];
                                    urlParam = cityPrefix.split("\\?")[1];
                                }

                                ArrayList<String> paramValue = new ArrayList<String>();

                                String sql = String.format("SELECT *FROM %s_Route where %s='%s'  and  %s='%s'", localEnName
                                        , CommonConstants.BUS_ROUTE_ID1, mBusRouteItem.busRouteApiId, CommonConstants.BUS_ROUTE_NAME, mBusRouteItem.busRouteName);


                                Cursor cursor = getBusDbSqlite().rawQuery(sql, null);

                                if (cursor.moveToNext() && param.length > 0) {

                                    for (int i = 0; i < param.length; i++) {

                                        if (param[i].contains("yyyy")) {
                                            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat(param[i], Locale.KOREA);
                                            Date currentTime = new Date();
                                            String mTime = mSimpleDateFormat.format(currentTime);
                                            paramValue.add(mTime);

                                        } else {
                                            paramValue.add(cursor.getString(cursor.getColumnIndex(param[i])));
                                        }
                                    }


                                    if (paramValue.size() == 1) {
                                        if (urlParam.equals("")) {

                                            if (localEnName.equals(CommonConstants.CITY_PO_HANG._engName)) {

                                                String value = "";
                                                if (paramValue.get(0).contains("(")) {
                                                    value = paramValue.get(0).substring(0, paramValue.get(0).indexOf("(")).toString();
                                                } else {
                                                    value = paramValue.get(0);
                                                }

                                                value = value.replace("지선", "");


                                                url = String.format(cityPrefix, value);

                                            } else {
                                                url = String.format(cityPrefix, paramValue.get(0));
                                            }


                                        } else {

                                            if (localEnName.equals(CommonConstants.CITY_DAE_GU._engName)) {
                                                url = String.format(urlParam, URLEncoder.encode(paramValue.get(0)));
                                                urlParam = String.format(urlParam, URLEncoder.encode(paramValue.get(0)));
                                            } else if (localEnName.equals(CommonConstants.CITY_GUN_SAN._engName)) {
                                                String tempStr = paramValue.get(0);
                                                tempStr = (tempStr.length() == 1) ? "0" + tempStr : tempStr;
                                                url = String.format(urlParam, tempStr);
                                                urlParam = String.format(urlParam, tempStr);
                                            } else {
                                                url = String.format(urlParam, paramValue.get(0));
                                                urlParam = String.format(urlParam, paramValue.get(0));
                                            }

                                        }

                                    } else if (paramValue.size() == 2) {


                                        if (urlParam.equals("")) {

                                            url = String.format(cityPrefix, paramValue.get(0), paramValue.get(1));

                                        } else {

                                            if (localEnName.equals(CommonConstants.CITY_YANG_SAN._engName)) {
                                                String value = "";
                                                int size = 10 - paramValue.get(0).length();

                                                value = paramValue.get(0);
                                                for (int i = 0; i < size; i++) {
                                                    value = "0" + value;
                                                }

                                                url = "aaa";

                                                urlParam = String.format(urlParam, value, paramValue.get(1));

                                            } else {
                                                url = "aaa";
                                                urlParam = String.format(urlParam, paramValue.get(0), paramValue.get(1));
                                            }


                                        }

                                    } else if (paramValue.size() == 3) {


                                        if (urlParam.equals("")) {
                                            url = String.format(cityPrefix, paramValue.get(0), paramValue.get(1), paramValue.get(2));
                                        } else {
                                            url = String.format(urlParam, paramValue.get(0), paramValue.get(1), paramValue.get(2));
                                            urlParam = String.format(urlParam, paramValue.get(0), paramValue.get(1), paramValue.get(2));
                                        }


                                    } else if (paramValue.size() == 4) {

                                        if (urlParam.equals("")) {
                                            url = String.format(cityPrefix, paramValue.get(0), paramValue.get(1), paramValue.get(2), paramValue.get(3));
                                        } else {
                                            url = "aa";
                                            urlParam = String.format(urlParam, paramValue.get(0), paramValue.get(1), paramValue.get(2), paramValue.get(3));
                                        }
                                    }

                                }
                                cursor.close();
                            }


                        }
                        urlGetCursor.close();
                    }


                    if (url != null && url.length() > 0) {
                        Intent intent = new Intent(getActivity(), SBWebView.class);

                        if (urlParam.equals("")) {
                            intent.putExtra(Constants.INTENT_URL, url);
                        } else {
                            intent.putExtra(Constants.INTENT_URL, preUrl);
                            intent.putExtra(Constants.INTENT_URL_PARAM, urlParam);
                        }

                        intent.putExtra(Constants.INTENT_URL_SNED_TYPE, sendType);

                        getActivity().startActivity(intent);
                    } else {


                    }

                }
            }
        });


        mInforBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playVibe();
                if (mDetailRoute.getVisibility() == View.VISIBLE) {
                    mDetailRoute.setVisibility(View.GONE);
                    mLineImage.setVisibility(View.GONE);


                    mInforBtn.setSelected(false);
                } else {
                    mDetailRoute.setVisibility(View.VISIBLE);
                    mLineImage.setVisibility(View.VISIBLE);


                    mInforBtn.setSelected(true);
                }


            }
        });


        if (mBusRouteItem != null) {

            String localEnName = mHashLocationEng.get(Integer.parseInt(mBusRouteItem.localInfoId));

            String sql = String.format("SELECT *FROM %s_Route where %s='%s'  and  %s='%s'", localEnName
                    , CommonConstants.BUS_ROUTE_ID1, mBusRouteItem.busRouteApiId, CommonConstants.BUS_ROUTE_NAME, mBusRouteItem.busRouteName);
            Cursor cursor = getBusDbSqlite().rawQuery(sql, null);

            String tmpResult = "";
            if (cursor.moveToNext()) {
                //String url   = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TIME_TALBE_URL));
                String start = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_FRIST_TIME));
                String end = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_LAST_TIME));
                String term = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_TERM));
                String distance = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_DISTANCE));

                String busType = getBusName(getBusDbSqlite(), mBusRouteItem.busType);


                if (busType != null && busType.length() > 0) {
                    busType = "유형 : " + busType;
                    mDetailTxt1.setText(busType);
                    mDetailTxt1.setVisibility(View.VISIBLE);
                }

                if (start != null && start.length() > 0) {

                    int hour = Integer.parseInt(start) / 60;
                    int min = Integer.parseInt(start) % 60;

                    if (hour == 0) hour = 24;


                    String resultTime = "";

                    if (String.valueOf(hour).length() == 1) {
                        resultTime = "0" + String.valueOf(hour);
                    } else {
                        resultTime = String.valueOf(hour);
                    }


                    if (String.valueOf(min).length() == 1) {

                        if (min == 0) {
                            resultTime = resultTime + ":00";
                        } else {
                            resultTime = resultTime + ":0" + String.valueOf(min);
                        }

                    } else {
                        resultTime = resultTime + ":" + String.valueOf(min);
                    }

                    start = "첫차 : " + resultTime;

                    mDetailTxt2.setText(start);
                    mDetailTxt2.setVisibility(View.VISIBLE);
                }

                if (end != null && end.length() > 0) {

                    int hour = Integer.parseInt(end) / 60;
                    int min = Integer.parseInt(end) % 60;

                    if (hour == 0) hour = 24;


                    String resultTime = "";

                    if (String.valueOf(hour).length() == 1) {
                        resultTime = "0" + String.valueOf(hour);
                    } else {
                        resultTime = String.valueOf(hour);
                    }


                    if (String.valueOf(min).length() == 1) {

                        if (min == 0) {
                            resultTime = resultTime + ":00";
                        } else {
                            resultTime = resultTime + ":0" + String.valueOf(min);
                        }

                    } else {
                        resultTime = resultTime + ":" + String.valueOf(min);
                    }

                    end = "막차 : " + resultTime;
                    mDetailTxt3.setText(end);
                    mDetailTxt3.setVisibility(View.VISIBLE);
                }

                if (term != null && term.length() > 0 && !term.contains("null")) {

                    try {
                        term = "배차간격 : " + Integer.parseInt(term) + "분";
                    } catch (Exception e) {
                        term = "배차간격 : " + term;
                    }


                    mDetailTxt4.setText(term);
                    mDetailTxt4.setVisibility(View.VISIBLE);
                }

                if (distance != null && distance.length() > 0) {
                    if (distance.equals("0")) {
                        mDetailTxt5.setVisibility(View.GONE);
                    } else {
                        distance = "운행거리 : " + distance + "km";
                        mDetailTxt5.setText(distance);
                        mDetailTxt5.setVisibility(View.VISIBLE);
                    }

                }

            } else {
            }
            cursor.close();


        }

        String detailInforResult = "";


    }


    class DetailAdapter extends BaseAdapter {

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mBusStopItem.size();
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
            final int curPos = position;

            if (view == null) {
                viewHolder = new ViewHolder();
                view = getActivity().getLayoutInflater().inflate(R.layout.busstop_row,
                        parent, false);

                viewHolder.text = (TextView) view.findViewById(R.id.stopName);
                viewHolder.num = (TextView) view.findViewById(R.id.stopNum);
                viewHolder.busImg = (LineImageView) view.findViewById(R.id.busImg);
                viewHolder.returnBus = (ImageView) view.findViewById(R.id.returnImg);
                viewHolder.depBus = (ImageButton) view.findViewById(R.id.depatureImg);
                viewHolder.arvBus = (ImageButton) view.findViewById(R.id.arriveImg);

                view.setTag(viewHolder);
            } else {

                viewHolder = (ViewHolder) view.getTag();
            }


            BusStopItem item = mBusStopItem.get(position);
            BusStopItem item2 = null;
            boolean preIsPlainNum = false;
            if (position != 0) {
                item2 = mBusStopItem.get(position - 1);
                if (item2.plainNum != null && !item2.plainNum.equals("null")) {
                    preIsPlainNum = true;
                }
            } else {
                item2 = new BusStopItem();
                item2.position = 0;
            }

            viewHolder.text.setText(item.name);
            final String stationName = item.name;
            if (item.arsId == null || item.arsId.equals("0")) {
                viewHolder.num.setVisibility(View.GONE);
            } else {
                viewHolder.num.setVisibility(View.VISIBLE);
                viewHolder.num.setText(item.arsId);
            }


            if (position == 0) {
                viewHolder.busImg.setType(Constants.LINE_START);
            } else if (position == mBusStopItem.size() - 1) {
                viewHolder.busImg.setType(Constants.LINE_END);
            } else {
                viewHolder.busImg.setType(Constants.LINE_NOMAL);
            }

            String color = mBusTypeHash.get(mBusRouteItem.busType);

            if (item.isTurn) {
                viewHolder.returnBus.setVisibility(View.VISIBLE);
            } else {
                viewHolder.returnBus.setVisibility(View.GONE);
            }

            if (mIsTimeRequired) {

                viewHolder.busImg.setBus(false, item.position, "", color.toUpperCase(), item2.position, preIsPlainNum);
                if (depStationName != null) {
                    viewHolder.depBus.setVisibility(View.GONE);
                    if (curPos != depPos) {
                        viewHolder.arvBus.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.arvBus.setVisibility(View.GONE);
                    }
                    viewHolder.arvBus.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            arvPos = curPos;
                            arvStationName = stationName;
                            showTimeRequiredDialog();
                            depStationName = null;
                            arvStationName = null;
                            mDetailAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    viewHolder.depBus.setVisibility(View.VISIBLE);
                    viewHolder.arvBus.setVisibility(View.GONE);
                    viewHolder.depBus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            depPos = curPos;
                            depStationName = stationName;
                            mDetailAdapter.notifyDataSetChanged();
                        }
                    });
                }

            } else {

                viewHolder.depBus.setVisibility(View.GONE);
                viewHolder.arvBus.setVisibility(View.GONE);

                if (item.isExist) {
                    viewHolder.busImg.setBus(true, item.position, item.plainNum, color.toUpperCase(), item2.position, preIsPlainNum);
                } else {
                    viewHolder.busImg.setBus(false, item.position, "", color.toUpperCase(), item2.position, preIsPlainNum);
                }

            }

            if (mMemoryBusStopItem != null) {
                if (item.arsId != null && mMemoryBusStopItem.arsId != null && !mMemoryBusStopItem.arsId.trim().equals("")) {

                    if (item.arsId.equals(mMemoryBusStopItem.arsId)) {
                        if (!item.arsId.trim().equals("") && !item.arsId.trim().equals("0") && !item.arsId.trim().equals("000000") && !item.arsId.trim().equals("00000")) {
                            view.setBackgroundColor(getResources().getColor(R.color.select_stop_color));
                        } else {
                            view.setBackgroundResource(0);
                        }
                    } else {
                        view.setBackgroundResource(0);
                    }
                } else {
                    view.setBackgroundResource(0);
                }
            } else {
                view.setBackgroundResource(0);
            }

            return view;

        }

        class ViewHolder {
            TextView text;
            TextView num;
            LineImageView busImg;
            ImageView returnBus;
            ImageButton depBus;
            ImageButton arvBus;
        }

    }

    private void showTimeRequiredDialog() {

        final View timeRequiredView = getActivity().getLayoutInflater().inflate(R.layout.timerequired_dialog, null, false);
        View titleView = getActivity().getLayoutInflater().inflate(R.layout.alarm_title, null, false);


        ((TextView) timeRequiredView.findViewById(R.id.depTxt)).setText(depStationName);
        ((TextView) timeRequiredView.findViewById(R.id.arvTxt)).setText(arvStationName);
        ((TextView) timeRequiredView.findViewById(R.id.distanceTxt)).setText("(" + Math.abs(depPos - arvPos) + "개 정류장 이동)");


        mTimeRequiredDialog = new SBDialog(getActivity());
        mTimeRequiredDialog.setViewLayout(timeRequiredView.findViewById(R.id.alarmsetting_layout));
        mTimeRequiredDialog.setTitleLayout("소요시간 안내");
        mTimeRequiredDialog.setTitleLayout(titleView.findViewById(R.id.alarm_title_layout));
        mTimeRequiredDialog.getPositiveButton("확인").setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mTimeRequiredDialog.dismiss();
            }
        });

        mTimeRequiredDialog.setCanceledOnTouchOutside(true);

        mTimeRequiredDialog.show();

    }


    @Override
    public void onCompleted(int type, DepthItem item) {

        try {
            if (type == Constants.PARSER_STOP_TYPE) {
                final DepthStopItem depthStopItem = (DepthStopItem) item;

                if (mBusStopItem != null && mBusStopItem.size() > 0 && depthStopItem.busStopItem.size() == 0)
                    return;

                mBusStopItem = (ArrayList<BusStopItem>) depthStopItem.busStopItem;


                if (mPreLayout.getVisibility() == View.VISIBLE) {
                    mPreLayout.setVisibility(View.GONE);
                    mList.setVisibility(View.VISIBLE);
                }

                if (mMemoryBusStopItem != null && mDetailAdapter != null) {
                    mDetailAdapter.notifyDataSetChanged();


                    for (int i = 0; i < mBusStopItem.size(); i++) {

                        if (mBusStopItem.get(i).arsId != null && mMemoryBusStopItem.arsId != null) {

                            if (mBusStopItem.get(i).arsId.trim().equals("")) continue;

                            if (mBusStopItem.get(i).arsId.equals(mMemoryBusStopItem.arsId)) {

                                if (mList != null) {
                                    if (i > 4) {
                                        if (stOrd != null) {
                                            if (i == Integer.parseInt(stOrd) - 1) {
                                                mList.setSelection(i - 4);
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            mList.setSelection(i - 4);
                                        }
                                    } else {
                                        mList.setSelection(i);
                                    }

                                }

                                break;
                            }
                        }

                    }

                    mDetailAdapter.notifyDataSetChanged();

                }

                if (item.depthIndex > 0) {
                    mGetData.depthBusStopParsing(depthStopItem);
                }

                for (int i = 0; i < mBusStopItem.size(); i++) {
                    if (mBusStopItem.get(i).plusParsingNeed > 0) {
                        mBusStopItem.get(i).index = i;
                        mGetData.startBusStopDetailParsing(mBusStopItem.get(i));
                    }
                }


                if (mBusStopItem.size() == 0) {
                    mCheckTxt.setVisibility(View.VISIBLE);
                } else {
                    mCheckTxt.setVisibility(View.GONE);
                }


            } else if (type == Constants.PARSER_STOP_DETAIL_TYPE) {


                DepthStopItem depthStopItem = (DepthStopItem) item;

                ArrayList<BusStopItem> stopItem = (ArrayList<BusStopItem>) depthStopItem.busStopItem;
                if (stopItem.size() == 1) {

                    mBusStopItem.set(stopItem.get(0).index, stopItem.get(0));

                    if (mBusStopItem.get(stopItem.get(0).index).localInfoId == CommonConstants.CITY_PO_HANG._engName) {
                        if (mBusStopItem.get(stopItem.get(0).index).isExist) {
                            mBusStopItem.get(stopItem.get(0).index - 1).isExist = true;
                            mBusStopItem.get(stopItem.get(0).index).isExist = false;
                        }
                    }

                    if (mDetailAdapter != null) {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                }

            } else if (type == Constants.PARSER_STOP_DEPTH_TYPE) {


                DepthStopItem depthStopItem = (DepthStopItem) item;


                for (int i = 0; i < mBusStopItem.size(); i++) {
                    mBusStopItem.get(i).isExist = depthStopItem.busStopItem.get(i).isExist;
                    mBusStopItem.get(i).plainNum = depthStopItem.busStopItem.get(i).plainNum;
                }

                if (mDetailAdapter != null) {
                    mDetailAdapter.notifyDataSetChanged();
                }

                if (depthStopItem.depthIndex > 0) {
                    mGetData.depthBusStopParsing(depthStopItem);
                }

            }
        } catch (Exception e) {
        }


    }


    public String getBusName(SQLiteDatabase db, int id) {

        String sql = String.format("SELECT *FROM %s where %s='%s'", CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, id);
        String busName = "";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            busName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_TYPE_BUS_TYPE));
        }

        cursor.close();

        return busName;

    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void refreshFragment() {
        super.refreshFragment();
        if (mView != null) {
            setHeader(mView);
            if (mHandler != null) {
                mHandler.removeCallbacks(mRefreshRunnable);
                mHandler.post(mRefreshRunnable);
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setBusRouteItem(mBusRouteItem, true, mHashLocationEng, mTerminus, mHashLocation);
            }
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mHandler != null) {
            if (mRefreshRunnable != null) {
                mHandler.removeCallbacks(mRefreshRunnable);
            }
        }

        if (mGetData != null) {
            mGetData.clear();
        }
    }

    @Override
    public void activityResult(int requestCode, int resultCode, Intent data) {
        super.activityResult(requestCode, resultCode, data);

        checkFavorite();
    }

    @Override
    public void onRefreshStop() {
        super.onRefreshStop();

        if (mHandler != null) {
            mHandler.removeCallbacks(mRefreshRunnable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGetData != null) {
            mGetData.clear();
        }

        if (mHandler != null) {
            if (mRefreshRunnable != null) {
                mHandler.removeCallbacks(mRefreshRunnable);
            }
        }

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        BusStopItem stopItem = mBusStopItem.get(position);
        stopItem.localInfoId = mBusRouteItem.localInfoId;

        String realNextStopItemName = null;
        BusStopItem nextStopItem = null;
        BusStopItem preStopItem = null;
        if (position < mBusStopItem.size() - 1) {
            nextStopItem = mBusStopItem.get(position + 1);
        }
        if (position == mBusStopItem.size() - 1) {
            preStopItem = mBusStopItem.get(mBusStopItem.size() - 2);
        }

        if ((stopItem.arsId == null && stopItem.apiId == null) || (stopItem.arsId != null && stopItem.arsId.equals("0"))) {

            final ArrayList<BusStopItem> listBusStopItem = new ArrayList<BusStopItem>();
            String cityName = mHashLocationEng.get(Integer.parseInt(stopItem.localInfoId));


            if (stopItem.name != null && stopItem.name.length() > 0) {

                String realteQry = String.format("SELECT *FROM %s_route where %s='%s'", cityName, CommonConstants._ID, mBusRouteItem._id);
                Cursor realateCursor = getBusDbSqlite().rawQuery(realteQry, null);

                String relateStop = null;
                if (realateCursor.moveToNext()) {
                    relateStop = realateCursor.getString(realateCursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
                }

                String[] relateSplit = relateStop.split("/");
                realateCursor.close();


                String sql = String.format("SELECT *FROM %s_stop where %s='%s'", cityName, CommonConstants.BUS_STOP_NAME, stopItem.name);
                Cursor cursor = getBusDbSqlite().rawQuery(sql, null);


                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
                    String arsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    String apiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));


                    int index = -1;
                    int nextIndex = -1;
                    if (relateSplit != null) {
                        for (int i = 0; i < relateSplit.length; i++) {
                            String relatetmpStop = relateSplit[i];
                            if (relatetmpStop.equals(String.valueOf(id))) {
                                index = i;
                            }
                        }


                        if (index != -1) {
                            if (index == relateSplit.length - 1) {
                                nextIndex = Integer.parseInt(relateSplit[(index - 1)]);
                            } else {
                                nextIndex = Integer.parseInt(relateSplit[(index + 1)]);
                            }
                        }
                    }

                    //삼학교 이상함


                    String nextIndexName = null;
                    if (nextIndex != -1) {

                        String nextQry = String.format("SELECT * FROM %s_stop where %s='%s'", cityName, CommonConstants._ID, nextIndex);
                        Cursor nextCursor = getBusDbSqlite().rawQuery(nextQry, null);
                        if (nextCursor.moveToNext()) {
                            nextIndexName = nextCursor.getString(nextCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                        }

                        nextCursor.close();
                    }


                    BusStopItem tmpBusStopItem = new BusStopItem();
                    tmpBusStopItem.arsId = arsId;
                    tmpBusStopItem.apiId = apiId;
                    tmpBusStopItem._id = id;
                    tmpBusStopItem.name = stopItem.name;
                    tmpBusStopItem.localInfoId = mBusRouteItem.localInfoId;
                    tmpBusStopItem.tmpString = nextIndexName;

                    listBusStopItem.add(tmpBusStopItem);


                    if (preStopItem != null) {
                        if (preStopItem.name.equals(nextIndexName)) {
                            realNextStopItemName = nextIndexName;
                            break;
                        }

                    } else {
                        if (nextStopItem != null && nextStopItem.name.equals(nextIndexName)) {
                            realNextStopItemName = nextIndexName;
                            break;
                        }

                    }


                }

                cursor.close();


                if (listBusStopItem.size() == 0) {
                    Toast.makeText(getActivity(), "정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                } else if (listBusStopItem.size() == 1) {

                    SBStopSearchDetailFragment routeDetail = new SBStopSearchDetailFragment(getBusDbSqlite(), getLocalDBHelper());
                    routeDetail.setBusStopItem(listBusStopItem.get(0), false, mHashLocationEng, mTerminus, mHashLocation);
                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, routeDetail).addToBackStack("search").commit();

                } else {

                    if (realNextStopItemName != null) {
                        for (int i = 0; i < listBusStopItem.size(); i++) {
                            if (listBusStopItem.get(i).tmpString != null) {
                                if (listBusStopItem.get(i).tmpString.equals(realNextStopItemName)) {

                                    SBStopSearchDetailFragment routeDetail = new SBStopSearchDetailFragment(getBusDbSqlite(), getLocalDBHelper());
                                    routeDetail.setBusStopItem(listBusStopItem.get(i), false, mHashLocationEng, mTerminus, mHashLocation);
                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, routeDetail).addToBackStack("search").commit();

                                    return;
                                }
                            }
                        }

                    }

                    LayoutInflater _inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = _inflater.inflate(R.layout.common_list, null);
                    final ListView listView = (ListView) view.findViewById(R.id.list);

                    String[] settingList = getResources().getStringArray(R.array.longStopSelect);
                    ArrayList<String> arGeneral = new ArrayList<String>();

                    for (int i = 0; i < listBusStopItem.size(); i++) {

                        String str = "";

                        if (listBusStopItem.get(i).tmpString != null) {
                            str = "[" + listBusStopItem.get(i).tmpString + "방면]" + listBusStopItem.get(i).name;
                        } else {
                            str = "[" + i + "]" + listBusStopItem.get(i).name;
                        }

						/*String str = "[" + listBusStopItem.get(i).arsId + "]" + listBusStopItem.get(i).name;*/

                        arGeneral.add(str);
                    }

                    final CommonAdapter commonAdapter = new CommonAdapter(arGeneral, getActivity(), CommonAdapter.TYPE_COMMON_TEXTVIEW2);

                    final SBDialog dialog = new SBDialog(getActivity());
                    dialog.setViewLayout(view.findViewById(R.id.common_list_layout));
                    dialog.setTitleLayout("정류장 선택");
                    dialog.setOnShowListener(new OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialog) {
                            listView.setAdapter(commonAdapter);
                        }
                    });
                    dialog.setCancelable(true);
                    dialog.show();


                    listView.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int position, long arg3) {

                            SBStopSearchDetailFragment routeDetail = new SBStopSearchDetailFragment(getBusDbSqlite(), getLocalDBHelper());
                            routeDetail.setBusStopItem(listBusStopItem.get(position), false, mHashLocationEng, mTerminus, mHashLocation);

                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, routeDetail).addToBackStack("search").commit();

                            dialog.dismiss();

                        }
                    });


                }


            } else {
                Toast.makeText(getActivity(), "정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }


        } else {

            stopItem.apiType = RequestCommonFuction.getApiTpye(Integer.parseInt(stopItem.localInfoId));
            String sql = RequestCommonFuction.getBusQury(stopItem, mHashLocationEng);
            Cursor cursor = getBusDbSqlite().rawQuery(sql, null);

            if (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
                String arsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                String apiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));

                stopItem.arsId = arsId;
                stopItem.apiId = apiId;
                stopItem._id = id;
            }


            SBStopSearchDetailFragment routeDetail = new SBStopSearchDetailFragment(getBusDbSqlite(), getLocalDBHelper());
            routeDetail.setBusStopItem(stopItem, false, mHashLocationEng, mTerminus, mHashLocation);

            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, routeDetail).addToBackStack("search").commit();

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
