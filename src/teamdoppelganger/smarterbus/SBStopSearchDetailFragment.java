package teamdoppelganger.smarterbus;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.adapter.CommonAdapter;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.service.BusAlarmService;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;
import teamdoppelganger.smarterbus.util.common.GetData.GetDataListener;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.BlockRelative;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cz.msebera.android.httpclient.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.smart.lib.CommonConstants;

import org.json.JSONObject;

@SuppressLint("ValidFragment")
public class SBStopSearchDetailFragment extends SBBaseFragment implements GetDataListener {

    SQLiteDatabase mDB = null;
    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mHashLocationKo;
    HashMap<Integer, String> mTerminus;
    HashMap<Integer, String> mBusTypeHash;

    String mSelectedColor;

    Handler mHandler;
    Runnable mRefreshRunnable, mRefreshClickRunnable, mCloseAlarmRunnable, mTimeOutRunnable, mPathClickRunnable;

    boolean mIsClickable = true, mIsPathClickable = true;

    public SBStopSearchDetailFragment() {

    }

    public SBStopSearchDetailFragment(SQLiteDatabase db,
                                      LocalDBHelper localDBHelper) {
        super(R.layout.stopsearch_detail, db, localDBHelper);


        mRefreshClickRunnable = new Runnable() {
            @Override
            public void run() {
                mIsClickable = true;
            }
        };

        mPathClickRunnable = new Runnable() {
            @Override
            public void run() {
                mIsPathClickable = true;
            }
        };

        mCloseAlarmRunnable = new Runnable() {
            @Override
            public void run() {
                if (mAlarmDialog != null) {
                    try {
                        if (mAlarmDialog.isShowing()) {
                            mAlarmDialog.dismiss();
                        }
                    } catch (Exception e) {
                    }
                    ;
                }
            }
        };

        mTimeOutRunnable = new Runnable() {

            @Override
            public void run() {

                try {
                    for (int i = 0; i < mBusRouteItem.size(); i++) {
                        BusRouteItem routeItem = mBusRouteItem.get(i);

                        if (routeItem.plusParsingNeed == 0 && !routeItem.isSection) {
                            mBusRouteItem.get(i).isEnd = true;
                        }
                    }

                    if (mDetailAdapter != null) {
                        mDetailAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ;
            }
        };

        mDB = db;

    }

    public void onLayoutFinish(View view) {

        mHashLocationKo = ((SBInforApplication) getActivity().getApplicationContext()).mHashKoLocation;
        mHashLocationEng = ((SBInforApplication) getActivity().getApplicationContext()).mHashLocation;
        mBusTypeHash = ((SBInforApplication) getActivity().getApplicationContext()).mBusTypeHash;

        mBusRouteItem = new ArrayList<BusRouteItem>();
        mDetailAdapter = new DetailAdapter();
        initView(view);

        checkFavorite();

    }

    private void checkFavorite() {

        String sql = null;

        if (mBusStopItem != null) {
            if (mBusStopItem.apiType == Constants.API_TYPE_3) {

                sql = String.format("SELECT * FROM %s where %s='%s' and %s='%s' and %s=%s ", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_TYPEID4_F, mBusStopItem.arsId
                        , LocalDBHelper.TABLE_CITY_F, mBusStopItem.localInfoId, LocalDBHelper.TABLE_TYPE_F, Constants.STOP_TYPE);
            } else if (mBusStopItem.apiType == Constants.API_TYPE_1) {
                sql = String.format("SELECT * FROM %s where %s='%s' and %s='%s' and %s=%s", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_TYPEID3_F, mBusStopItem.apiId
                        , LocalDBHelper.TABLE_CITY_F, mBusStopItem.localInfoId, LocalDBHelper.TABLE_TYPE_F, Constants.STOP_TYPE);
            } else if (mBusStopItem.apiType == Constants.API_TYPE_2) {
                sql = String.format("SELECT * FROM %s where %s='%s' and %s='%s' and %s='%s'  and %s=%s", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_TYPEID4_F, mBusStopItem.arsId,
                        LocalDBHelper.TABLE_TYPEID3_F, mBusStopItem.apiId, LocalDBHelper.TABLE_CITY_F, mBusStopItem.localInfoId, LocalDBHelper.TABLE_TYPE_F, Constants.STOP_TYPE);
            } else if (mBusStopItem.apiType == Constants.API_TYPE_4) {
                sql = String.format("SELECT * FROM %s where %s='%s' and %s='%s'", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_TEMP1, mBusStopItem.tempId2, LocalDBHelper.TABLE_TYPEID3_F, mBusStopItem.apiId);
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

    BusStopItem mBusStopItem;
    ArrayList<BusRouteItem> mBusRouteItem;
    ArrayList<BusRouteItem> mSpeciBusRouteItems;
    ArrayList<BusRouteItem> mAlarmRouteItem;
    DetailAdapter mDetailAdapter;


    //header
    TextView mStopNameTxt;
    Button mRefreshBtn, mFavoBtn, mPathBtn, mPlusHomeBtn;


    //alarm
    AlertDialog mAlarmAlert;
    LinearLayout malarmContents;
    ProgressBar mAlarmProgess;
    ListView mAlarmList;
    AlaramAdapter mAlarmAdapter;

    int mCurrentParsingIndex = 0;
    int mDefaultPosition = 0;
    int mFavoriteId = 999999;

    View mView;
    SBDialog mAlarmDialog;


    public void setBusStopItem(BusStopItem item, boolean isOrderRefesh, HashMap<Integer, String> hashLocationEng,
                               HashMap<Integer, String> terminus, HashMap<Integer, String> hashLocationKo) {


        if (!isOrderRefesh) {

            mBusStopItem = item;

            mHashLocationEng = hashLocationEng;
            mTerminus = terminus;
            mHashLocationKo = hashLocationKo;


            mBusStopItem.apiType = RequestCommonFuction.getApiTpye(Integer.parseInt(mBusStopItem.localInfoId));


            if (mBusStopItem._id == 0) {

                String sql = RequestCommonFuction.getBusQury(mBusStopItem, mHashLocationEng);


                Cursor cursor = getBusDbSqlite().rawQuery(sql, null);

                if (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
                    String desScription = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));
                    mBusStopItem._id = id;
                    mBusStopItem.tempId2 = desScription;
                }

                cursor.close();
            }


        }

        if (mBusRouteItem == null) {
            mBusRouteItem = new ArrayList<BusRouteItem>();
        }

        if (mSpeciBusRouteItems == null) {
            mSpeciBusRouteItems = new ArrayList<BusRouteItem>();
        }

        if (mAlarmRouteItem == null) {
            mAlarmRouteItem = new ArrayList<BusRouteItem>();
        }


        mGetData = new GetData(this, getBusDbSqlite(), mHashLocationEng);
        mGetData.startRelateRoute(mBusStopItem);


        if (mHandler == null) {
            mHandler = new Handler();
            mRefreshRunnable = new Runnable() {
                @Override
                public void run() {

                    for (int i = 0; i < mBusRouteItem.size(); i++) {
                        BusRouteItem routeItem = mBusRouteItem.get(i);

                        if (routeItem.plusParsingNeed == 0 && !routeItem.isSection) {
                            mBusRouteItem.get(i).isEnd = false;
                        }
                    }

                    mGetData.clear();
                    setBusStopItem(mBusStopItem, true, mHashLocationEng, mTerminus, mHashLocationKo);
                    mHandler.postDelayed(mRefreshRunnable, 15000);
                }
            };

            mHandler.postDelayed(mRefreshRunnable, 15000);
        }

    }


    private void initView(View view) {

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName("정류장");
        t.send(new HitBuilders.AppViewBuilder().build());

        mList = (ListView) view.findViewById(R.id.listView);
        mPreLayout = (BlockRelative) view.findViewById(R.id.progressLayout);

        mPreLayout.setVisibility(View.VISIBLE);
        mList.setVisibility(View.GONE);

        mView = view;
        setHeader(view);
        mList.setAdapter(mDetailAdapter);


        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int selectPosition,
                                    long arg3) {


                if (mDefaultPosition != 0) {
                    if (selectPosition <= mDefaultPosition) {
                        return;
                    }
                }


                if (mBusRouteItem.get(selectPosition).isSection) return;

                mSelectedColor = mBusTypeHash.get(mBusRouteItem.get(selectPosition).busType);
                showSelectDialog(selectPosition + mDefaultPosition);

            }
        });

    }

    private void setHeader(View view) {


        mStopNameTxt = ((TextView) view.findViewById(R.id.stopName));
        mRefreshBtn = ((Button) view.findViewById(R.id.refreshBtn));
        mFavoBtn = ((Button) view.findViewById(R.id.favoriteBtn));
        mPathBtn = ((Button) view.findViewById(R.id.pathBtn));
        mPlusHomeBtn = (Button) view.findViewById(R.id.addShortcutBtn);


        ((LinearLayout) getActivity().findViewById(R.id.routeDetailHeader)).setVisibility(View.GONE);
        ((LinearLayout) getActivity().findViewById(R.id.stopDetailHeader)).setVisibility(View.VISIBLE);


        ((TextView) getActivity().findViewById(R.id.headerStopName)).setText(mBusStopItem.name);
        ((TextView) getActivity().findViewById(R.id.headerStopNum)).setText(mBusStopItem.arsId);

		
		/*mStopNameTxt.setText(mBusStopItem.name);*/

        mPathBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mIsPathClickable) {
                    return;
                }

                mIsPathClickable = false;

                if (mHandler != null) {
                    mHandler.postDelayed(mPathClickRunnable, 1500);
                }

                // Get tracker.
                Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                        SBInforApplication.TrackerName.APP_TRACKER);
                t.enableAdvertisingIdCollection(true);
                t.setScreenName("정류장 지도");
                t.send(new HitBuilders.AppViewBuilder().build());

                String sql = String.format("SELECT * FROM %s_stop where %s='%s'", mHashLocationEng.get(Integer.parseInt(mBusStopItem.localInfoId))
                        , CommonConstants._ID, mBusStopItem._id);

                Cursor cursor = getBusDbSqlite().rawQuery(sql, null);
                if (cursor.moveToNext()) {
                    String locationX = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
                    String locationY = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));

                    if (!locationX.equals("0") && !locationY.equals("0")) {

                        SBMapFragment mapFragment = new SBMapFragment(getBusDbSqlite(), getLocalDBHelper());
                        mapFragment.setBusStopItem(mBusStopItem._id, mHashLocationEng.get(Integer.parseInt(mBusStopItem.localInfoId)));
                        mapFragment.setMode(Constants.MAP_MODE_STOP_PATH);

                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, mapFragment).addToBackStack("search").commit();


                    } else {

                        Toast.makeText(getActivity(), "정류장 위치정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();

                    }


                } else {

                    Toast.makeText(getActivity(), "정류장 위치정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();

                }


                playVibe();
            }
        });


        mRefreshBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playVibe();

                if (mHandler != null) {
                    mHandler.removeCallbacks(mRefreshRunnable);
                }
                if (mGetData != null) {
                    mGetData.clear();
                }


                setBusStopItem(mBusStopItem, true, mHashLocationEng, mTerminus, mHashLocationKo);

                if (mHandler != null) {
                    mHandler.postDelayed(mRefreshRunnable, 15000);
                    mHandler.postDelayed(mRefreshClickRunnable, 10000);
                }


            }
        });

        mFavoBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                playVibe();
                if (mFavoriteId != 999999) {

                    getLocalDBHelper().deleteFavorite(mFavoriteId);
                    Toast.makeText(getActivity(), "즐겨 찾기에서  삭제 되었습니다", Toast.LENGTH_SHORT).show();
                    mFavoBtn.setSelected(false);
                    mFavoriteId = 999999;
                    return;
                }

                sendBusStopLocationData();

                String findSql = String.format("SELECT *FROM %s ", LocalDBHelper.TABLE_FAVORITE_NAME);
                Cursor findCursor = getLocalDBHelper().getReadableDatabase().rawQuery(findSql, null);

                if (findCursor != null && findCursor.getCount() >= 25) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_favorite), Toast.LENGTH_SHORT).show();
                    return;
                }

                BusStopItem busStopItem = mBusStopItem;

                String nickName = "", nickName2 = "";

                if (!(busStopItem.arsId == null || busStopItem.arsId.trim().length() == 0)) {
                    nickName2 = busStopItem.arsId;
                }

                nickName = busStopItem.name;

                FavoriteAndHistoryItem favoriteAndHistoryItem = new FavoriteAndHistoryItem();
                favoriteAndHistoryItem.busStopItem = mBusStopItem;
                favoriteAndHistoryItem.nickName = nickName;
                favoriteAndHistoryItem.nickName2 = nickName2;
                favoriteAndHistoryItem.type = Constants.FAVORITE_TYPE_STOP;

                Intent intent = new Intent(getActivity(), SBEditFavoriteActivity.class);
                intent.putExtra(Constants.INTENT_FAVORITEITEM, favoriteAndHistoryItem);
                getActivity().startActivityForResult(intent, 100);

            }
        });


        mPlusHomeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                playVibe();
                FavoriteAndHistoryItem favoriteAndHistoryItem = new FavoriteAndHistoryItem();
                favoriteAndHistoryItem.busStopItem = mBusStopItem;
                favoriteAndHistoryItem.nickName = mBusStopItem.name;
                favoriteAndHistoryItem.type = Constants.FAVORITE_TYPE_STOP;
                shortcutIntent(favoriteAndHistoryItem);

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        checkFavorite();
    }


    class DetailAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mBusRouteItem.size();
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

            BusRouteItem item = mBusRouteItem.get(position);

            if (view == null) {
                viewHolder = new ViewHolder();
                view = getActivity().getLayoutInflater().inflate(R.layout.businfor_row,
                        parent, false);

                viewHolder.text = (TextView) view.findViewById(R.id.lineName);
                viewHolder.textDetail = (TextView) view.findViewById(R.id.lineDetail);
                viewHolder.time = (TextView) view.findViewById(R.id.linetime);
                viewHolder.time2 = (TextView) view.findViewById(R.id.linetime2);
                viewHolder.itemHeaderTitle = (TextView) view.findViewById(R.id.itemHeaderTitle);
                viewHolder.textSub = (TextView) view.findViewById(R.id.lineSubName);

                viewHolder.realItemSection = (RelativeLayout) view.findViewById(R.id.realItemSection);
                viewHolder.itemHeadSection = (RelativeLayout) view.findViewById(R.id.itemHeaderSection);
                viewHolder.progressBar = (ProgressBar) view.findViewById(R.id.rowProgress);

                view.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) view.getTag();

            }


            if (item.isSection) {

                viewHolder.itemHeadSection.setVisibility(View.VISIBLE);
                viewHolder.realItemSection.setVisibility(View.GONE);
                viewHolder.itemHeaderTitle.setText(item.busRouteName);
                viewHolder.progressBar.setVisibility(View.GONE);

            } else {

                if (item.isEnd) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                } else {
                    viewHolder.progressBar.setVisibility(View.VISIBLE);
                }

                viewHolder.itemHeadSection.setVisibility(View.GONE);
                viewHolder.realItemSection.setVisibility(View.VISIBLE);
                viewHolder.text.setText(item.busRouteName);

                try {

                    String color = mBusTypeHash.get(item.busType);
                    viewHolder.text.setTextColor(Color.parseColor("#" + color));

                } catch (Exception e) {
                }

                if (item.nextDirctionName == null || item.nextDirctionName.trim().length() == 0) {
                    viewHolder.textDetail.setText("");
                } else if (item.nextDirctionName.equals("end")) {
                    viewHolder.textDetail.setText("현재 마지막 정류장");
                } else {
                    viewHolder.textDetail.setText("다음정류장:" + item.nextDirctionName);
                }

                if (item.arriveInfo.size() > 0) {

                    ArriveItem arriveItem = ((ArriveItem) item.arriveInfo.get(0));

                    if (arriveItem.state == Constants.STATE_PREPARE) {
                        try {
                            if (arriveItem.remainSecond == -9999) {
                                viewHolder.time.setText(arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발");
                            } else {
                                viewHolder.time.setText(getResources().getString(R.string.state_prepare));
                            }
                        } catch (Exception e) {
                            viewHolder.time.setText(getResources().getString(R.string.state_prepare));
                        }

                    } else if (arriveItem.state == Constants.STATE_ING) {

                        String resultStr = "";

                        if (arriveItem.remainMin != -1) {

                            if (arriveItem.remainMin != 0) {
                                resultStr = arriveItem.remainMin + "분";
                            }

                        }

                        if (arriveItem.remainSecond != -1) {
                            resultStr = resultStr + arriveItem.remainSecond + "초";
                        }


                        if (arriveItem.remainStop != -1) {
                            if (resultStr.equals("")) {
                                resultStr = arriveItem.remainStop + "정류장 전";
                            } else {
                                resultStr = resultStr + " (" + arriveItem.remainStop + "정류장 전)";
                            }
                        }

                        viewHolder.time.setText(resultStr);

                    } else if (arriveItem.state == Constants.STATE_END) {
                        viewHolder.time.setText(getResources().getString(R.string.state_end));
                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                        viewHolder.time.setText(getResources().getString(R.string.state_prepare_not));
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        viewHolder.time.setText(getResources().getString(R.string.state_near));
                    }

                } else {
                    viewHolder.time.setText("");
                }


                if (item.arriveInfo.size() > 1) {

                    ArriveItem arriveItem = ((ArriveItem) item.arriveInfo.get(1));
                    if (arriveItem.state == Constants.STATE_PREPARE) {

                        viewHolder.time2.setText(getResources().getString(R.string.state_prepare));

                    } else if (arriveItem.state == Constants.STATE_ING) {

                        String resultStr = "";

                        if (arriveItem.remainMin != -1) {

                            if (arriveItem.remainMin != 0) {
                                resultStr = arriveItem.remainMin + "분";
                            }

                        }

                        if (arriveItem.remainSecond != -1) {
                            resultStr = resultStr + arriveItem.remainSecond + "초";
                        }

                        if (arriveItem.remainStop != -1) {

                            if (resultStr.equals("")) {
                                resultStr = arriveItem.remainStop + "정류장 전";
                            } else {
                                resultStr = resultStr + " (" + arriveItem.remainStop + "정류장 전)";
                            }

                        }

                        viewHolder.time2.setText(resultStr);

                    } else if (arriveItem.state == Constants.STATE_END) {

                        viewHolder.time2.setText(getResources().getString(R.string.state_end));

                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {

                        viewHolder.time2.setText(getResources().getString(R.string.state_prepare_not));

                    } else if (arriveItem.state == Constants.STATE_NEAR) {

                        viewHolder.time2.setText(getResources().getString(R.string.state_near));

                    }

                } else {

                    viewHolder.time2.setText("");

                }

                if (item.arriveInfo.size() < 2) {

                    viewHolder.time2.setVisibility(View.GONE);

                } else {

                    viewHolder.time2.setVisibility(View.VISIBLE);

                }


                if (item.busRouteSubName == null || item.busRouteSubName.trim().length() == 0) {
                    viewHolder.textSub.setText("");
                } else {
                    if (item.direction != null) {
                        viewHolder.textSub.setText("(" + item.busRouteSubName + ")" + "[" + item.direction + "]");
                    } else {
                        viewHolder.textSub.setText("(" + item.busRouteSubName + ")");
                    }
                }

            }

            return view;

        }

        class ViewHolder {

            TextView text;
            TextView textSub;
            TextView textDetail;
            TextView time, time2;
            TextView itemHeaderTitle;

            RelativeLayout realItemSection;
            RelativeLayout itemHeadSection;

            ProgressBar progressBar;

        }

    }


    class SpecialDetailAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSpeciBusRouteItems.size();
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
                view = getActivity().getLayoutInflater().inflate(R.layout.businfor_row, parent, false);

                viewHolder.text = (TextView) view.findViewById(R.id.lineName);
                viewHolder.textDetail = (TextView) view.findViewById(R.id.lineDetail);
                viewHolder.time = (TextView) view.findViewById(R.id.linetime);
                viewHolder.time2 = (TextView) view.findViewById(R.id.linetime2);
                viewHolder.itemHeaderTitle = (TextView) view.findViewById(R.id.itemHeaderTitle);
                viewHolder.textSub = (TextView) view.findViewById(R.id.lineSubName);

                viewHolder.realItemSection = (RelativeLayout) view.findViewById(R.id.realItemSection);
                viewHolder.itemHeadSection = (RelativeLayout) view.findViewById(R.id.itemHeaderSection);

                view.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) view.getTag();

            }


            BusRouteItem item = mSpeciBusRouteItems.get(position);

            if (item.isSection) {

                viewHolder.itemHeadSection.setVisibility(View.VISIBLE);
                viewHolder.realItemSection.setVisibility(View.GONE);

                viewHolder.itemHeaderTitle.setText(item.busRouteName);

            } else {

                viewHolder.itemHeadSection.setVisibility(View.GONE);
                viewHolder.realItemSection.setVisibility(View.VISIBLE);


                viewHolder.text.setText(item.busRouteName);
                if (item.nextDirctionName == null || item.nextDirctionName.trim().length() == 0) {

                    viewHolder.textDetail.setText("");
                } else if (item.nextDirctionName.equals("end")) {
                    viewHolder.textDetail.setText("현재 마지막 정류장");
                } else {
                    viewHolder.textDetail.setText("다음정류장:" + item.nextDirctionName);
                }

                if (item.arriveInfo.size() > 0) {

                    ArriveItem arriveItem = ((ArriveItem) item.arriveInfo.get(0));
                    if (arriveItem.state == Constants.STATE_PREPARE) {
                        viewHolder.time.setText("출발 준비중");
                    } else if (arriveItem.state == Constants.STATE_ING) {

                        String resultStr = "";

                        if (arriveItem.remainMin != -1) {

                            resultStr = arriveItem.remainMin + "분";
                        }


                        if (arriveItem.remainStop != -1) {
                            if (resultStr.equals("")) {
                                resultStr = arriveItem.remainStop + "정류장 전";
                            } else {
                                resultStr = resultStr + "(" + arriveItem.remainStop + "정류장 전)";
                            }
                        }

                        viewHolder.time.setText(resultStr);
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        viewHolder.time.setText("도착 예정");
                    }
                }


                if (item.arriveInfo.size() > 1) {

                    ArriveItem arriveItem = ((ArriveItem) item.arriveInfo.get(1));
                    if (arriveItem.state == Constants.STATE_PREPARE) {
                        viewHolder.time2.setText("출발 준비중");
                    } else if (arriveItem.state == Constants.STATE_ING) {


                        String resultStr = "";

                        if (arriveItem.remainMin != -1) {

                            resultStr = arriveItem.remainMin + "분";
                        }


                        if (arriveItem.remainStop != -1) {
                            if (resultStr.equals("")) {
                                resultStr = arriveItem.remainStop + "정류장 전";
                            } else {
                                resultStr = resultStr + "(" + arriveItem.remainStop + "정류장 전)";
                            }
                        }

                        viewHolder.time2.setText(resultStr);
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        viewHolder.time2.setText("도착 예정");
                    }

                }


                if (item.arriveInfo.size() < 2) {
                    viewHolder.time2.setVisibility(View.GONE);
                } else {
                    viewHolder.time2.setVisibility(View.VISIBLE);
                }


                if (item.busRouteSubName == null || item.busRouteSubName.trim().length() == 0) {
                    viewHolder.textSub.setText("");
                } else {
                    viewHolder.textSub.setText("(" + item.busRouteSubName + ")");
                }

            }

            return view;

        }


        class ViewHolder {
            TextView text;
            TextView textSub;
            TextView textDetail;
            TextView time, time2;
            TextView itemHeaderTitle;

            RelativeLayout realItemSection;
            RelativeLayout itemHeadSection;
        }

    }


    class AlaramAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAlarmRouteItem.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            ViewHolder viewHolder = null;

            if (view == null) {
                viewHolder = new ViewHolder();
                view = getActivity().getLayoutInflater().inflate(R.layout.alarm_row,
                        parent, false);

                viewHolder.checkedTxtView = (CheckedTextView) view.findViewById(R.id.checkAlarmTxt);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            return view;

        }

        class ViewHolder {
            CheckedTextView checkedTxtView;

        }

    }


    private void showSelectDialog(final int selectPosition) {

        final BusRouteItem busRouteItem = mBusRouteItem.get(selectPosition - mDefaultPosition);

        LayoutInflater _inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = _inflater.inflate(R.layout.common_list, null);

        final ListView listView = (ListView) view.findViewById(R.id.list);

        String[] settingList = getResources().getStringArray(R.array.longSelect);
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

                    if (mDefaultPosition != 0) {
                        if (selectPosition <= mDefaultPosition) {
                            return;
                        }
                    }

                    //error point
                    BusRouteItem routeItem = mBusRouteItem.get(selectPosition - mDefaultPosition);
                    routeItem.localInfoId = mBusStopItem.localInfoId;

                    if (routeItem.busRouteApiId == null
                            || routeItem.busRouteApiId.equals("0")
                            || routeItem.busRouteApiId.trim().equals("")) {


                        String tmpSql = "";

                        if (routeItem.busRouteSubName != null && routeItem.busRouteSubName.length() > 0) {

                            tmpSql = String.format("SELECT *FROM %s where %s='%s' and %s='%s'", mHashLocationEng.get(Integer.parseInt(mBusStopItem.localInfoId)) + Constants.TABLE_BUS,
                                    CommonConstants.BUS_ROUTE_NAME, routeItem.busRouteName, CommonConstants.BUS_ROUTE_SUB_NAME, routeItem.busRouteSubName);

                        } else {
                            tmpSql = String.format("SELECT *FROM %s where %s='%s'", mHashLocationEng.get(Integer.parseInt(mBusStopItem.localInfoId)) + Constants.TABLE_BUS,
                                    CommonConstants.BUS_ROUTE_NAME, routeItem.busRouteName);
                        }

                        Cursor tmpCursor = mDB.rawQuery(tmpSql, null);
                        if (tmpCursor.moveToNext()) {

                            String apiId = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                            String apiId2 = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                            routeItem.busRouteApiId = apiId;
                            routeItem.busRouteApiId2 = apiId2;

                        }

                        if (routeItem.busRouteApiId == null || routeItem.busRouteApiId.equals("0")
                                || routeItem.busRouteApiId.trim().equals("")) {
                            return;
                        }
                    }


                    if (routeItem.busRouteApiId2 == null) {
                    }


                    SBRouteSearchDetailFragment routeDetail = new SBRouteSearchDetailFragment(getBusDbSqlite(), getLocalDBHelper());
                    routeDetail.setBusRouteItem(routeItem, false, mHashLocationEng, mTerminus, mHashLocationKo);
                    routeDetail.setBusMemory(mBusStopItem);

                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.coverLayout, routeDetail).addToBackStack("search").commit();


                } else if (position == 1) {

                    String findSql = String.format("SELECT *FROM %s ", LocalDBHelper.TABLE_FAVORITE_NAME);
                    Cursor findCursor = getLocalDBHelper().getReadableDatabase().rawQuery(findSql, null);

                    if (findCursor != null && findCursor.getCount() >= 25) {
                        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.max_favorite), Toast.LENGTH_SHORT).show();
                        return;
                    }


                    final BusRouteItem busRouteItem = mBusRouteItem.get(selectPosition);

                    FavoriteAndHistoryItem favoriteAndHistoryItem = new FavoriteAndHistoryItem();
                    favoriteAndHistoryItem.busRouteItem = busRouteItem;
                    favoriteAndHistoryItem.busRouteItem.busStopApiId = mBusStopItem.apiId;
                    favoriteAndHistoryItem.busRouteItem.busStopArsId = mBusStopItem.arsId;
                    favoriteAndHistoryItem.busRouteItem.busStopName = mBusStopItem.name;
                    favoriteAndHistoryItem.busRouteItem.tmpId = mBusStopItem.tempId2;
                    favoriteAndHistoryItem.nickName = mBusStopItem.name;
                    favoriteAndHistoryItem.nickName2 = busRouteItem.busRouteName;
                    favoriteAndHistoryItem.type = Constants.FAVORITE_TYPE_BUS_STOP;

                    String selectSql;
                    if (favoriteAndHistoryItem.busRouteItem.busRouteArsId == null) {
                        selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' "
                                , LocalDBHelper.TABLE_FAVORITE_NAME, Constants.FAVORITE_TYPE_BUS_STOP, busRouteItem.localInfoId, favoriteAndHistoryItem.busRouteItem.busRouteApiId
                                , 0,
                                favoriteAndHistoryItem.busRouteItem.busStopApiId, favoriteAndHistoryItem.busRouteItem.busStopArsId);
                    } else {
                        selectSql = String.format("SELECT * from %s where type=%s and city=%s and typeId='%s'and typeId2='%s' and typeId3='%s' and typeId4='%s' "
                                , LocalDBHelper.TABLE_FAVORITE_NAME, Constants.FAVORITE_TYPE_BUS_STOP, busRouteItem.localInfoId, favoriteAndHistoryItem.busRouteItem.busRouteApiId
                                , favoriteAndHistoryItem.busRouteItem.busRouteArsId,
                                favoriteAndHistoryItem.busRouteItem.busStopApiId, favoriteAndHistoryItem.busRouteItem.busStopArsId);
                    }


                    Cursor cursor = getLocalDBHelper().getReadableDatabase().rawQuery(selectSql, null);
                    if (cursor.getCount() == 0) {
                        Intent intent = new Intent(getActivity(), SBEditFavoriteActivity.class);
                        intent.putExtra(Constants.INTENT_FAVORITEITEM, favoriteAndHistoryItem);
                        startActivityForResult(intent, 100);
                        sendBusStopLocationData();
                    } else {
                        Toast.makeText(getActivity(), "즐겨찾기에 등록 되어 있습니다.", Toast.LENGTH_SHORT).show();
                    }

                    cursor.close();

                } else if (position == 2) {

                    final BusRouteItem busRouteItem = mBusRouteItem.get(selectPosition);

                    if (mAlarmAlert != null && mAlarmAlert.isShowing()) {
                        return;
                    }

                    boolean isAlarmAble = false;
                    if (RequestCommonFuction.getAlarmAndWigetAble(Integer.parseInt(busRouteItem.localInfoId))) {
                        for (int i = 0; i < busRouteItem.arriveInfo.size(); i++) {
                            ArriveItem item = busRouteItem.arriveInfo.get(i);
                            if (item.state == Constants.STATE_ING) {
                                isAlarmAble = true;
                                break;
                            }
                        }
                    }

                    if (!isAlarmAble) {
                        Toast.makeText(getActivity(), "버스 알람을 실행 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showAlarmSettingDialog(selectPosition);

                }


            }
        });
    }


    private void sendBusStopLocationData() {

        try {
            String sql = String.format("SELECT * FROM %s_stop where %s='%s'", mHashLocationEng.get(Integer.parseInt(mBusStopItem.localInfoId))
                    , CommonConstants._ID, mBusStopItem._id);

            Cursor cursor = getBusDbSqlite().rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int locationX = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
                int locationY = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));
                String adId = ((SBInforApplication) getActivity().getApplicationContext()).getADID();
                if (locationX > locationY && adId != "") {
                    //전송
                    String url = String.format("http://ace-sync.toast.com/applog?sid=bus_app&adid=%s&busstop=%s", adId, ((locationY / 1e6) + "_" + (locationX / 1e6)));
                    new AsyncHttpClient().get(url, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                            super.onFailure(statusCode, headers, responseString, throwable);
                        }
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //alarm 설정 다이알로그
    private void showAlarmSettingDialog(final int position) {

        final BusRouteItem busRouteItem = mBusRouteItem.get(position);
        busRouteItem.tmpId = mBusStopItem.tempId2;

        final View alarmSettingView = getActivity().getLayoutInflater().inflate(R.layout.alarmsetting, null, false);
        View titleView = getActivity().getLayoutInflater().inflate(R.layout.alarm_title, null, false);


        TextView titleTxt = (TextView) titleView.findViewById(R.id.title);
        TextView subTitleTxt = (TextView) titleView.findViewById(R.id.subTitle);

        final RadioGroup choiceBus = (RadioGroup) alarmSettingView.findViewById(R.id.choiceBus);
        RadioButton c_bus1 = (RadioButton) alarmSettingView.findViewById(R.id.c_bus1);
        RadioButton c_bus2 = (RadioButton) alarmSettingView.findViewById(R.id.c_bus2);

        final RadioGroup choice2 = (RadioGroup) alarmSettingView.findViewById(R.id.choice2);
        final RadioButton c2_1 = (RadioButton) alarmSettingView.findViewById(R.id.c2_1);
        final RadioButton c2_2 = (RadioButton) alarmSettingView.findViewById(R.id.c2_2);
        final RadioButton c2_3 = (RadioButton) alarmSettingView.findViewById(R.id.c2_3);
        final RadioButton c2_4 = (RadioButton) alarmSettingView.findViewById(R.id.c2_4);


        //init
        c2_1.setEnabled(false);
        c2_2.setEnabled(false);
        c2_3.setEnabled(false);
        c2_4.setEnabled(false);
        c2_1.setBackgroundResource(R.drawable.btn_minute_l_disabled);
        c2_1.setTextColor(getResources().getColor(R.color.alarm_disable_color));
        c2_2.setBackgroundResource(R.drawable.btn_minute_middle_disabled);
        c2_2.setTextColor(getResources().getColor(R.color.alarm_disable_color));
        c2_3.setBackgroundResource(R.drawable.btn_minute_middle_disabled);
        c2_3.setTextColor(getResources().getColor(R.color.alarm_disable_color));
        c2_4.setBackgroundResource(R.drawable.btn_minute_r_disabled);
        c2_4.setTextColor(getResources().getColor(R.color.alarm_disable_color));


        if (busRouteItem.arriveInfo.size() == 2) {

        }
        if (busRouteItem.arriveInfo.size() > 1) {
            c_bus1.setEnabled(true);
            c_bus2.setEnabled(true);
        } else if (busRouteItem.arriveInfo.size() == 1) {

            c_bus2.setBackgroundResource(R.drawable.btn_bus_r_disable);
            c_bus2.setTextColor(getResources().getColor(R.color.alarm_disable_color));

            c_bus1.setEnabled(true);
            c_bus2.setEnabled(false);
        } else if (busRouteItem.arriveInfo.size() == 0) {

            c_bus1.setBackgroundResource(R.drawable.btn_minute_l_disabled);
            c_bus1.setTextColor(getResources().getColor(R.color.alarm_disable_color));
            c_bus2.setBackgroundResource(R.drawable.btn_bus_r_disable);
            c_bus1.setTextColor(getResources().getColor(R.color.alarm_disable_color));


            c_bus1.setEnabled(false);
            c_bus2.setEnabled(false);
        }


        c_bus1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                int min = busRouteItem.arriveInfo.get(0).remainMin;

                choice2.clearCheck();


                if (min > 1) {

                    c2_1.setBackgroundResource(R.drawable.radio_time_left);
                    c2_1.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_1.setEnabled(true);
                } else {

                    c2_1.setBackgroundResource(R.drawable.btn_minute_l_disabled);
                    c2_1.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_1.setEnabled(false);
                }


                if (min > 3) {

                    c2_2.setBackgroundResource(R.drawable.radio_time_center);
                    c2_2.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_2.setEnabled(true);
                } else {

                    c2_2.setBackgroundResource(R.drawable.btn_minute_middle_disabled);
                    c2_2.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_2.setEnabled(false);
                }

                if (min > 5) {

                    c2_3.setBackgroundResource(R.drawable.radio_time_center);
                    c2_3.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_3.setEnabled(true);
                } else {

                    c2_3.setBackgroundResource(R.drawable.btn_minute_middle_disabled);
                    c2_3.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_3.setEnabled(false);
                }

                if (min > 10) {

                    c2_4.setBackgroundResource(R.drawable.radio_time_right);
                    c2_4.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_4.setEnabled(true);
                } else {

                    c2_4.setBackgroundResource(R.drawable.btn_minute_r_disabled);
                    c2_4.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_4.setEnabled(false);
                }


            }
        });


        c_bus2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                int min = busRouteItem.arriveInfo.get(1).remainMin;

                choice2.clearCheck();


                if (min > 1) {
                    c2_1.setBackgroundResource(R.drawable.radio_time_left);
                    c2_1.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_1.setEnabled(true);
                } else {
                    c2_1.setBackgroundResource(R.drawable.btn_minute_l_disabled);
                    c2_1.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_1.setEnabled(false);
                }


                if (min > 3) {
                    c2_2.setBackgroundResource(R.drawable.radio_time_center);
                    c2_2.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_2.setEnabled(true);
                } else {
                    c2_2.setBackgroundResource(R.drawable.btn_minute_middle_disabled);
                    c2_2.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_2.setEnabled(false);
                }

                if (min > 5) {
                    c2_3.setBackgroundResource(R.drawable.radio_time_center);
                    c2_3.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_3.setEnabled(true);
                } else {
                    c2_3.setBackgroundResource(R.drawable.btn_minute_middle_disabled);
                    c2_3.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_3.setEnabled(false);
                }

                if (min > 10) {
                    c2_4.setBackgroundResource(R.drawable.radio_time_right);
                    c2_4.setTextColor(getResources().getColorStateList(R.drawable.radio_txt_color));

                    c2_4.setEnabled(true);
                } else {
                    c2_4.setBackgroundResource(R.drawable.btn_minute_r_disabled);
                    c2_4.setTextColor(getResources().getColor(R.color.alarm_disable_color));

                    c2_4.setEnabled(false);
                }


            }
        });

        if (busRouteItem.localInfoId.equals(CommonConstants.CITY_SEO_UL._engName)) {

        }

        c_bus1.setText(getResources().getString(R.string.first_bus));
        c_bus2.setText(getResources().getString(R.string.second_bus));
        c2_1.setText("1" + getResources().getString(R.string.alarm_min));
        c2_2.setText("3" + getResources().getString(R.string.alarm_min));
        c2_3.setText("5" + getResources().getString(R.string.alarm_min));
        c2_4.setText("10" + getResources().getString(R.string.alarm_min));

        String title = busRouteItem.busRouteName + " " + busRouteItem.busStopName;
        SpannableString spanTitle = new SpannableString(title);// 스팬어블 할당
        spanTitle.setSpan(new ForegroundColorSpan(Color.parseColor("#" + mSelectedColor)), 0, busRouteItem.busRouteName.length(), 0);
        spanTitle.setSpan(new ForegroundColorSpan(Color.BLACK), (busRouteItem.busRouteName + " ").length(),
                spanTitle.length(), 0);
        titleTxt.setText(spanTitle);

        mAlarmDialog = new SBDialog(getActivity());
        mAlarmDialog.setViewLayout(alarmSettingView.findViewById(R.id.alarmsetting_layout));
        mAlarmDialog.setTitleLayout(titleView.findViewById(R.id.alarm_title_layout));
        mAlarmDialog.getPositiveButton("확인").setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                int firstIndex = choiceBus.indexOfChild(alarmSettingView.findViewById(choiceBus.getCheckedRadioButtonId()));
                int secondIndex = choice2.indexOfChild(alarmSettingView.findViewById(choice2.getCheckedRadioButtonId()));

                if (secondIndex == 0) {
                    if (!c2_1.isChecked()) {
                        Toast.makeText(getActivity(), "시간을 선택해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (secondIndex == -1) {
                    Toast.makeText(getActivity(), "시간을 선택해주세요", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (secondIndex == 2) {
                    secondIndex = 1;
                } else if (secondIndex == 4) {
                    secondIndex = 2;
                } else if (secondIndex == 6) {
                    secondIndex = 3;
                }


                Intent intent = new Intent(getActivity(), BusAlarmService.class);
                intent.putExtra(Constants.INTENT_BUSROUTEITEM, busRouteItem);
                intent.putExtra(Constants.ALARM_BUS_ORDER, firstIndex);
                intent.putExtra(Constants.ALARM_BUS_MIN, secondIndex);
                intent.putExtra(Constants.ALARM_BUS_COLOR, mSelectedColor);

                getActivity().startService(intent);
                mAlarmDialog.dismiss();
            }
        });
        mAlarmDialog.getNegativeButton("취소").setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarmDialog.dismiss();
            }
        });
        mAlarmDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mHandler.postDelayed(mCloseAlarmRunnable, 1000 * 30);
            }
        });

        mAlarmDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mHandler != null) {
                    mHandler.removeCallbacks(mCloseAlarmRunnable);
                }
            }
        });

        mAlarmDialog.show();

    }

    @Override
    public void onCompleted(int type, DepthItem item) {

        try {
            if (type == Constants.PARSER_LINE_TYPE) {

                DepthRouteItem depthStopItem = (DepthRouteItem) item;
                ArrayList<BusRouteItem> busRouteItem = (ArrayList<BusRouteItem>) depthStopItem.busRouteItem;

                if (RequestCommonFuction.getSpecialInfoGetAble(Integer.parseInt(mBusStopItem.localInfoId))) {

                    for (int i = 0; i < mBusRouteItem.size(); i++) {
                        BusRouteItem routeItem = mBusRouteItem.get(i);

                        if (routeItem.plusParsingNeed == 0 && !routeItem.isSection) {
                            mBusRouteItem.get(i).isEnd = true;
                        }
                    }


                    for (int i = 0; i < busRouteItem.size(); i++) {
                        busRouteItem.get(i).isEnd = true;
                        mBusRouteItem.add(i, busRouteItem.get(i));
                    }

                    if (busRouteItem != null && busRouteItem.size() > 0) {
                        BusRouteItem tmpRouteItem = new BusRouteItem();
                        tmpRouteItem.isSection = true;
                        tmpRouteItem.busRouteName = "출발 예정 버스";
                        mBusRouteItem.add(0, tmpRouteItem);
                        mDefaultPosition = busRouteItem.size();
                    }

                    if (mDetailAdapter != null && mBusRouteItem.size() > 0) {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                } else {

                    ArrayList<Integer> tempPrePosition = new ArrayList<Integer>();
                    for (int i = 0; i < busRouteItem.size(); i++) {

                        BusRouteItem tmpItem = busRouteItem.get(i);

                        if (!tmpItem.isSection) {

                            for (int j = 0; j < mBusRouteItem.size(); j++) {

                                BusRouteItem compareBusRouteItem = mBusRouteItem.get(j);

                                if (compareBusRouteItem.isSection) {

                                } else {

                                    if (tmpItem.busRouteApiId == null || tmpItem.busRouteApiId.trim().length() == 0) {
                                        if (compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)
                                                || compareBusRouteItem.busRouteName.split("[(]")[0].equals(tmpItem.busRouteName)
                                                || compareBusRouteItem.busRouteName.split("[(]")[0].equals("M" + tmpItem.busRouteName)
                                                || compareBusRouteItem.busRouteName.equals("M" + tmpItem.busRouteName)
                                                || (tmpItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_GIM_CHEON._cityId))
                                                & compareBusRouteItem.busRouteName.split("\\(")[0].endsWith(tmpItem.busRouteName))) {

                                            if (tmpItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_GU_MI._cityId))) {

                                                if (compareBusRouteItem.busRouteSubName.equals(tmpItem.busRouteSubName.replaceAll("_", "-"))) {

                                                    mBusRouteItem.get(j).arriveInfo.clear();
                                                    mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    mBusRouteItem.get(j).direction = tmpItem.direction;
                                                    mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    mBusRouteItem.get(j).tmpId = tmpItem.tmpId;

                                                }
                                            } else if (tmpItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_CHIL_GOK._cityId))) {


                                                if (compareBusRouteItem.busRouteSubName.equals(tmpItem.busRouteSubName)) {

                                                    mBusRouteItem.get(j).arriveInfo.clear();
                                                    mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    mBusRouteItem.get(j).direction = tmpItem.direction;
                                                    mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    mBusRouteItem.get(j).tmpId = tmpItem.tmpId;

                                                }

                                            } else {

                                                mBusRouteItem.get(j).arriveInfo.clear();
                                                mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                mBusRouteItem.get(j).direction = tmpItem.direction;
                                                mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                mBusRouteItem.get(j).tmpId = tmpItem.tmpId;

                                            }


                                        }

                                    } else if (tmpItem.busRouteApiId2 == null || tmpItem.busRouteApiId2.length() == 0) {

                                        if (tmpItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_DAE_GU._cityId))) {

                                            if (tmpItem.busRouteApiId.length() > 0) {

                                                if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)) {

                                                    mBusRouteItem.get(j).arriveInfo.clear();
                                                    mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    mBusRouteItem.get(j).direction = tmpItem.direction;
                                                    mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    mBusRouteItem.get(j).tmpId = tmpItem.tmpId;


                                                }

                                            } else {
                                                if (compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)
                                                        || compareBusRouteItem.busRouteName.split("[(]")[0].equals(tmpItem.busRouteName)
                                                        || compareBusRouteItem.busRouteName.split("[(]")[0].equals("M" + tmpItem.busRouteName)
                                                        || compareBusRouteItem.busRouteName.equals("M" + tmpItem.busRouteName)) {

                                                    mBusRouteItem.get(j).arriveInfo.clear();
                                                    mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    mBusRouteItem.get(j).direction = tmpItem.direction;
                                                    mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    mBusRouteItem.get(j).tmpId = tmpItem.tmpId;
                                                }

                                            }

                                        } else {

                                            if (!tempPrePosition.contains(j)) {
                                                if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)) {
                                                    tempPrePosition.add(j);
                                                    mBusRouteItem.get(j).arriveInfo.clear();
                                                    mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                                    mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                                    mBusRouteItem.get(j).direction = tmpItem.direction;
                                                    mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                                    mBusRouteItem.get(j).tmpId = tmpItem.tmpId;
                                                    if (tmpItem.stOrd != null)
                                                        mBusRouteItem.get(j).stOrd = tmpItem.stOrd;
                                                    break;
                                                }

                                            }
                                        }

                                    } else {

                                        if (compareBusRouteItem.busRouteApiId.equals(tmpItem.busRouteApiId)
                                                && compareBusRouteItem.busRouteApiId2.equals(tmpItem.busRouteApiId2)
                                                && (compareBusRouteItem.busRouteName.equals(tmpItem.busRouteName)
                                                || compareBusRouteItem.busRouteName.split("[(]")[0].equals(tmpItem.busRouteName)
                                                || compareBusRouteItem.busRouteName.split("[(]")[0].equals("M" + tmpItem.busRouteName)
                                                || compareBusRouteItem.busRouteName.equals("M" + tmpItem.busRouteName))) {


                                            mBusRouteItem.get(j).arriveInfo.clear();
                                            mBusRouteItem.get(j).arriveInfo.addAll(tmpItem.arriveInfo);
                                            mBusRouteItem.get(j).plusParsingNeed = tmpItem.plusParsingNeed;
                                            mBusRouteItem.get(j).direction = tmpItem.direction;
                                            mBusRouteItem.get(j).isAlarmAble = tmpItem.isAlarmAble;
                                            mBusRouteItem.get(j).tmpId = tmpItem.tmpId;


                                            break;
                                        }

                                    }

                                }


                            }


                        }
                    }

                    for (int i = 0; i < mBusRouteItem.size(); i++) {
                        BusRouteItem routeItem = mBusRouteItem.get(i);

                        if (routeItem.plusParsingNeed == 0 && !routeItem.isSection) {
                            mBusRouteItem.get(i).isEnd = true;
                        }
                    }


                    if (mDetailAdapter != null && mBusRouteItem.size() > 0) {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                    boolean isDetailParsing = false;
                    for (int i = 0; i < mBusRouteItem.size(); i++) {

                        BusRouteItem routeItem = mBusRouteItem.get(i);

                        if (routeItem.plusParsingNeed > 0 && !routeItem.isSection) {
                            mGetData.startBusRouteDetailParsing(routeItem);
                            isDetailParsing = true;
                        }
                    }


                    if (item.depthIndex > 0) {

                        depthStopItem.busRouteItem.clear();
                        depthStopItem.busRouteItem.addAll(mBusRouteItem);
                        mGetData.depthBusRouteParsing(depthStopItem);

                    }


                    if (isDetailParsing) {
                        if (mHandler != null) {
                            mHandler.postDelayed(mTimeOutRunnable, 10000);
                        }

                    }


                }


            } else if (type == Constants.PARSER_ALALM_TYPE) {
                DepthRouteItem depthStopItem = (DepthRouteItem) item;
                mAlarmRouteItem = (ArrayList<BusRouteItem>) depthStopItem.busRouteItem;

                if (mAlarmAlert.isShowing()) {
                    mAlarmProgess.setVisibility(View.GONE);
                    malarmContents.setVisibility(View.VISIBLE);
                    mAlarmAdapter.notifyDataSetChanged();
                }
            } else if (type == Constants.PARSER_LINE_DETAIL_TYPE) {


                DepthRouteItem depthStopItem = (DepthRouteItem) item;

                ArrayList<BusRouteItem> routeItem = (ArrayList<BusRouteItem>) depthStopItem.busRouteItem;
                if (routeItem.size() == 1) {
                    routeItem.get(0).isEnd = true;

                    mBusRouteItem.set(routeItem.get(0).index, routeItem.get(0));

                    if (mDetailAdapter != null && mBusRouteItem.size() > 0) {
                        mDetailAdapter.notifyDataSetChanged();
                    }

                }
            } else if (type == Constants.PARSER_STOP_TYPE) {
            } else if (type == Constants.PARSER_RELATE_ROUTE_TYPE) {

                DepthRouteItem depthStopItem = (DepthRouteItem) item;
                mBusRouteItem = (ArrayList<BusRouteItem>) depthStopItem.busRouteItem;

                for (int i = 0; i < mBusRouteItem.size(); i++) {
                    BusRouteItem routeItem = mBusRouteItem.get(i);

                    if (!routeItem.isSection) {
                        mGetData.startNextStop(routeItem);
                    }

                }


                if (mDetailAdapter != null && mBusRouteItem.size() > 0) {
                    mDetailAdapter.notifyDataSetChanged();
                }

                mGetData.startBusRouteParsing(mBusStopItem);


            } else if (type == Constants.PARSER_NEXT_STOP) {

                DepthRouteItem depthStopItem = (DepthRouteItem) item;

                ArrayList<BusRouteItem> routeItem = (ArrayList<BusRouteItem>) depthStopItem.busRouteItem;


                if (routeItem.size() == 1) {

                    if (mDetailAdapter != null && mBusRouteItem.size() > 0) {
                        mDetailAdapter.notifyDataSetChanged();
                    }
                }

            } else if (type == Constants.PARSER_LINE_DEPTH_TYPE) {

                DepthRouteItem depthRouteItem = (DepthRouteItem) item;

                for (int i = 0; i < depthRouteItem.busRouteItem.size(); i++) {
                    mBusRouteItem.get(i).arriveInfo = depthRouteItem.busRouteItem.get(i).arriveInfo;
                }

                if (mDetailAdapter != null && mBusRouteItem.size() > 0) {
                    mDetailAdapter.notifyDataSetChanged();
                }

            }

            if (mPreLayout.getVisibility() == View.VISIBLE) {
                mPreLayout.setVisibility(View.GONE);
                mList.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {

        }


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
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGetData != null) {
            mGetData.clear();
        }
    }


    public void shortcutIntent(FavoriteAndHistoryItem item) {


        Intent shortcutIntent = new Intent(getActivity(), SBDetailActivity.class);
        final Intent putShortCutIntent = new Intent();

        FileOutputStream fos;
        String tmpFileName = String.valueOf(System.currentTimeMillis());

        try {
            if (item.type == Constants.FAVORITE_TYPE_BUS) {

                shortcutIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSROUTEITEM);
                fos = getActivity().openFileOutput(tmpFileName, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(item.busRouteItem);
                os.close();
                fos.close();


                putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.main_icon_bus));
                putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                        item.busRouteItem.busRouteName);


            } else if (item.type == Constants.FAVORITE_TYPE_STOP) {

                shortcutIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                fos = getActivity().openFileOutput(tmpFileName, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(item.busStopItem);
                os.close();
                fos.close();


                putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                        Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.main_icon_stop));
                putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                        item.nickName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        shortcutIntent.putExtra(Constants.INTENT_FILENAME, tmpFileName);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        putShortCutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
                shortcutIntent);
        putShortCutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getActivity().sendBroadcast(putShortCutIntent);

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            Toast.makeText(getActivity(), "홈에 바로가기가 추가 되었습니다.", Toast.LENGTH_SHORT).show();
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

            if (mRefreshClickRunnable != null) {
                mHandler.removeCallbacks(mRefreshClickRunnable);
            }

            if (mCloseAlarmRunnable != null) {
                mHandler.removeCallbacks(mCloseAlarmRunnable);
            }

            if (mTimeOutRunnable != null) {
                mHandler.removeCallbacks(mTimeOutRunnable);
            }

            if (mPathClickRunnable != null) {
                mHandler.removeCallbacks(mPathClickRunnable);
            }

        }

    }


}
