package teamdoppelganger.smarterbus;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

import teamdoppelganger.smarterbus.SBSelectFragment.SBSelectFragmentListener;
import teamdoppelganger.smarterbus.adapter.CommonAdapter;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.common.SBInforApplication.TrackerName;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.CommonItem;
import teamdoppelganger.smarterbus.item.DepthFavoriteItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.lib.pageDragDrop.DragDropGrid;
import teamdoppelganger.smarterbus.lib.pageDragDrop.DragDropGrid.DragAndDropElement;
import teamdoppelganger.smarterbus.lib.pageDragDrop.DragDropGrid.DragSource;
import teamdoppelganger.smarterbus.lib.pageDragDrop.ItemSimpleAdapter;
import teamdoppelganger.smarterbus.lib.pageDragDrop.ItemSimpleAdapter.DragView;
import teamdoppelganger.smarterbus.lib.pageDragDrop.ItemSimpleAdapter.ItemAdapterListener;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;
import teamdoppelganger.smarterbus.util.common.GetData.GetDataListener;
import teamdoppelganger.smarterbus.util.common.ImageStorage;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.smart.lib.CommonConstants;


/**
 * 즐겨찾기 클래스
 *
 * @author DOPPELSOFT4
 */
@SuppressLint("ValidFragment")
public class SBFavoriteFragment extends SBBaseFragment implements ItemAdapterListener

{

    private DragDropGrid mGridView;
    private TextView mEditBtn;
    private CheckBox mCheckBox;
    private TextView mNothingContents;

    ArrayList<FavoriteAndHistoryItem> mFavoriteAndHistoryItem = new ArrayList<FavoriteAndHistoryItem>();
    ArrayList<FavoriteAndHistoryItem> mTmpFavoriteAndHistoryItem = new ArrayList<FavoriteAndHistoryItem>();
    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mTerminus;
    HashMap<Integer, String> mBusTypeHash;


    int mWidth, mHeight;
    int mWindowWidth, mWindowHeight;

    int mMode = Constants.WIDGET_MODE_NOTHING;
    int mWidgetId;

    int _id;

    boolean mEditMode = false;

    InnerAsync mInnerAsync;

    ArrayList<GetData> mGetDataList;

    SharedPreferences mPref;
    //ItemAdapter mItemAdapter;
    ItemSimpleAdapter mItemSimpleAdapter;
    ScrollView mScrollView;


    LinearLayout mListLayout;
    DragSortListView mDragSortListView;
    DragBaseAdapter mDragBaseAdapter;


    boolean isInit = true;

    boolean mIsClick = false;
    boolean mIsListMode = false;

    Runnable mRunnable;
    Runnable mRefreshRunnable;
    Handler mHandler;

    SBSelectFragment mSBselectFragment;
    SBFavoriteFragmentListener mSBFavoriteFragmentListener;


    TextView mSortModeTxt;
    TextView mChoiceFavoriteTypeBtn;
    LinearLayout mEventbannerLayout;
    LinearLayout mSortLayout;

    ArrayList<Integer> mProgressPositionList;

    public interface SBFavoriteFragmentListener {
        public void clickItem(FavoriteAndHistoryItem item);
    }

    public void setSBFavoriteFragmentListener(SBFavoriteFragmentListener l) {
        this.mSBFavoriteFragmentListener = l;
    }

    public SBFavoriteFragment() {

    }

    public SBFavoriteFragment(int id, SQLiteDatabase db,
                              LocalDBHelper localDBHelper) {
        super(R.layout.favorite, db, localDBHelper);
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    @Override
    public void onLayoutFinish(View view) {
        super.onLayoutFinish(view);

        mGetDataList = new ArrayList<GetData>();
        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mHashLocationEng = ((SBInforApplication) getActivity().getApplicationContext()).mHashLocation;
        mTerminus = ((SBInforApplication) getActivity().getApplicationContext()).mTerminus;
        mBusTypeHash = ((SBInforApplication) getActivity().getApplicationContext()).mBusTypeHash;


        mProgressPositionList = new ArrayList<Integer>();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float density = getResources().getDisplayMetrics().density;
        mWindowWidth = outMetrics.widthPixels;
        mWindowHeight = outMetrics.heightPixels;
        mWidth = (int) (outMetrics.widthPixels - getResources().getDimension(R.dimen.ticket_margin) * 2);
        mHeight = (int) getResources().getDimension(R.dimen.ticket_height);

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mIsClick = false;
            }
        };


        mRefreshRunnable = new Runnable() {

            @Override
            public void run() {

                if (mMode != Constants.WIDGET_MODE_NOTHING || mFavoriteAndHistoryItem == null)
                    return;
                mGetDataList.clear();


                if (mGetDataList != null) {
                    for (int i = 0; i < mGetDataList.size(); i++) {
                        mGetDataList.get(i).clear();
                    }

                }

                if (!mEditMode && isOnline(getActivity())) {

                    mProgressPositionList.clear();
                    for (int i = 0; i < mFavoriteAndHistoryItem.size(); i++) {

                        if (mFavoriteAndHistoryItem.get(i).type == Constants.FAVORITE_TYPE_BUS_STOP) {
                            final int position = i;
                            GetData getData = new GetData(new GetDataListener() {

                                @Override
                                public void onCompleted(int type, DepthItem item) {

                                    if (mFavoriteAndHistoryItem.size() == 0) {
                                        if (mIsListMode) {
                                            if (mProgressPositionList != null) {
                                                mProgressPositionList.clear();
                                                if (mDragBaseAdapter != null) {
                                                    mDragBaseAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }


                                        return;
                                    }

                                    if (mIsListMode) {
                                        if (mProgressPositionList.size() > position) {
                                            mProgressPositionList.remove(position);
                                        }
                                        if (mDragBaseAdapter != null) {
                                            mDragBaseAdapter.notifyDataSetChanged();
                                        }

                                    } else {

                                        try {
                                            DragView dragView = (DragView) mGridView.getChildAt(position);
                                            dragView.setView(mFavoriteAndHistoryItem.get(position));

                                            dragView.findViewById(R.id.itemProgress).setVisibility(View.GONE);

                                            if (mItemSimpleAdapter != null) {
                                                mItemSimpleAdapter.notifyDataSetChanged();
                                            }
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                        ;

                                    }

                                }
                            }, getBusDbSqlite(), mHashLocationEng);

                            mGetDataList.add(getData);


                            mFavoriteAndHistoryItem.get(position).busRouteItem.arriveInfo.clear();
                            DepthFavoriteItem favoDepthFavoriteItem = new DepthFavoriteItem();
                            favoDepthFavoriteItem.favoriteAndHistoryItems.add(mFavoriteAndHistoryItem.get(position));

                            getData.startOneRefrshService(favoDepthFavoriteItem);

                            mProgressPositionList.add(position);

                            if (mIsListMode) {
                                try {
                                    if (mDragBaseAdapter != null) {
                                        mDragBaseAdapter.notifyDataSetChanged();
                                    }
                                } catch (Exception e) {
                                }
                                ;


                            } else {

                                try {
                                    DragView dragView = ((DragView) mGridView.getChildAt(position));
                                    dragView.findViewById(R.id.itemProgress).setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                }
                                ;
                            }


                        }

                    }
                }

                mHandler.postDelayed(mRefreshRunnable, 1000 * 20);


            }
        };

        initView(view);
        initBannerView(view);

    }

    private void initBannerView(View v) {

        String strEventBanner = mPref.getString(Constants.PREF_EVENT_NAME, Constants.PREF_EVENT_DEFAULT_NAME);
        Boolean boolEventBanner = mPref.getBoolean(Constants.PREF_BANNER_INFO_CHECK, true);

        if (strEventBanner.length() == 0) {
            mEventbannerLayout.setVisibility(View.GONE);
            return;
        }
        try {
            Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                    TrackerName.APP_TRACKER);
            t.enableAdvertisingIdCollection(true);
            t.setScreenName(getString(R.string.analytics_category_event_banner));
            t.send(new HitBuilders.AppViewBuilder().build());

            String[] split = strEventBanner.split("\\|");

            BannerHolder[] bannerInfo = new BannerHolder[split.length / 2];

            for (int i = 0; i < split.length; i += 2) {
                int idx = i / 2;
                bannerInfo[idx] = new BannerHolder();
                bannerInfo[idx].bannerName = split[i];
                if (split[i + 1].contains("market"))
                    bannerInfo[idx].URL = split[i + 1];
                else
                    bannerInfo[idx].URL = "http://" + split[i + 1];
            }

            shuffleArray(bannerInfo);

            if (strEventBanner.equals("")) {
                return;
            }

            mSortLayout = (LinearLayout) v.findViewById(R.id.sortLayout1);
            LinearLayout imgLayout = (LinearLayout) v.findViewById(R.id.imgLayout);

            Bitmap b = null;
            Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float density = getResources().getDisplayMetrics().density;
            int mWidth = (int) (outMetrics.widthPixels - getResources().getDimension(R.dimen.ticket_margin) * 2);
            int mHeight = (int) getResources().getDimension(R.dimen.ticket_height);

            int ticketWidth = (int) (outMetrics.widthPixels / 2 - getResources().getDimension(R.dimen.ticket_margin));
            int ticketHeight = (int) getResources().getDimension(R.dimen.ticket_height);

            for (int i = 0; i < bannerInfo.length; i++) {

                String imagename = bannerInfo[i].bannerName;

                if (ImageStorage.checkifImageExists(imagename)) {
                    File file = ImageStorage.getImage("/" + imagename + ".jpg");
                    String path = file.getAbsolutePath();
                    if (path != null) {
                        b = BitmapFactory.decodeFile(path);
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setTag(bannerInfo[i]);
                        imageView.setImageBitmap(b);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ticketWidth, ticketHeight);
                        if (i == 0)
                            lp.setMargins((int) getResources().getDimension(R.dimen.favorite_list_margin_left), 0, 0, 0);
                        else
                            lp.setMargins((int) getResources().getDimension(R.dimen.ticket_margin), 0, 0, 0);
                        imageView.setLayoutParams(lp);
                        imgLayout.addView(imageView);
                        imageView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                BannerHolder info = (BannerHolder) v.getTag();
                                Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(TrackerName.APP_TRACKER);
                                t.enableAdvertisingIdCollection(true);
                                t.send(new HitBuilders.EventBuilder().setCategory(getString(R.string.analytics_category_event_banner))
                                        .setAction(info.bannerName)
                                        .build());

                                Intent intent = null;

                                if (info.URL.contains("market")) {
                                    intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(info.URL));
                                    startActivity(intent);
                                } else {
                                    intent = new Intent(getActivity(), SBAdWebView.class);
                                    intent.putExtra(Constants.INTENT_URL, info.URL);
                                    getActivity().startActivity(intent);
                                }

                            }
                        });
                    }
                }

            }

            ImageView leftImg = (ImageView) v.findViewById(R.id.leftarrow);
            ImageView rightImg = (ImageView) v.findViewById(R.id.rightarrow);
            final HorizontalScrollView tabScrollView = (HorizontalScrollView) v.findViewById(R.id.horizontalId);

            rightImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tabScrollView.fullScroll(View.FOCUS_LEFT))
                        tabScrollView.fullScroll(View.FOCUS_RIGHT);
                }
            });

            leftImg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!tabScrollView.fullScroll(View.FOCUS_RIGHT))
                        tabScrollView.fullScroll(View.FOCUS_LEFT);
                }
            });

            if (boolEventBanner) {
                mSortLayout.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            mEventbannerLayout.setVisibility(View.GONE);
            mSortLayout.setVisibility(View.GONE);
        }

    }

    private void shuffleArray(BannerHolder[] array) {
        int index;
        BannerHolder info;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                info = array[i];
                array[i] = array[index];
                array[index] = info;

            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView(View v) {

        getValue();
        mFavoriteAndHistoryItem = mTmpFavoriteAndHistoryItem;

        if (mMode != Constants.WIDGET_MODE_NOTHING) {

            //위젯 모드일시에는 무조건 티켓 모드에서 고르도록 적용

            ((LinearLayout) v.findViewById(R.id.editLayout)).setVisibility(View.GONE);
            v.findViewById(R.id.favoriteLine).setVisibility(View.GONE);
            v.findViewById(R.id.gridview).setBackgroundResource(0);
            //v.findViewById(R.id.listMode).setVisibility(View.GONE);
            mIsListMode = false;


            v.findViewById(R.id.choiceFavoriteType).setVisibility(View.GONE);
            v.findViewById(R.id.sortModeTxt).setVisibility(View.GONE);
            v.findViewById(R.id.sortLayout).setVisibility(View.GONE);
            v.findViewById(R.id.eventbannerLayout).setVisibility(View.GONE);


        } else {

            int favoriteMode = getFavoriteListMode();

            if (favoriteMode == Constants.SETTING_FAOVIRET_TICKET) {
                mIsListMode = false;
                //((TextView)v.findViewById(R.id.sortModeTxt)).setText("티켓 모드");
            } else {
                mIsListMode = true;
                //((TextView)v.findViewById(R.id.sortModeTxt)).setText("리스트 모드");
            }


        }

        mWindowHeight = (int) (mWindowHeight - getResources().getDimension(R.dimen.spare_layout_height));

        mGridView = (DragDropGrid) v.findViewById(R.id.gridview);
        mEditBtn = (TextView) v.findViewById(R.id.editBtn);
        mScrollView = (ScrollView) v.findViewById(R.id.scroll);
        mNothingContents = (TextView) v.findViewById(R.id.nothingContents);

        mDragSortListView = (DragSortListView) v.findViewById(R.id.listViewFavo);
        mListLayout = (LinearLayout) v.findViewById(R.id.listLayout);

        mSortModeTxt = (TextView) v.findViewById(R.id.sortModeTxt);
        mChoiceFavoriteTypeBtn = (TextView) v.findViewById(R.id.choiceFavoriteType);
        mEventbannerLayout = (LinearLayout) v.findViewById(R.id.eventbannerLayout);
        mCheckBox = ((CheckBox) v.findViewById(R.id.checkBox));

        Boolean boolBanner = mPref.getBoolean(Constants.PREF_BANNER_INFO_CHECK, true);

        mCheckBox.setFocusableInTouchMode(false);
        mCheckBox.setClickable(false);

        if (boolBanner) {
            mCheckBox.setChecked(true);
        } else {
            mCheckBox.setChecked(false);
        }


        mChoiceFavoriteTypeBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                showChoiceFavoriteTypeDialog(mIsListMode);


            }
        });


        mEventbannerLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                SharedPreferences.Editor ed = mPref.edit();
                if (mCheckBox.isChecked()) {
                    mCheckBox.setChecked(false);
                    ed.putBoolean(Constants.PREF_BANNER_INFO_CHECK, false);
                    mSortLayout.setVisibility(View.GONE);
                    Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(TrackerName.APP_TRACKER);
                    t.enableAdvertisingIdCollection(true);
                    t.send(new HitBuilders.EventBuilder().setCategory(getString(R.string.analytics_category_event_banner))
                            .setAction("배너 해제")
                            .build());
                } else {
                    mCheckBox.setChecked(true);
                    ed.putBoolean(Constants.PREF_BANNER_INFO_CHECK, true);
                    mSortLayout.setVisibility(View.VISIBLE);
                    Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(TrackerName.APP_TRACKER);
                    t.enableAdvertisingIdCollection(true);
                    t.send(new HitBuilders.EventBuilder().setCategory(getString(R.string.analytics_category_event_banner))
                            .setAction("배너 설정")
                            .build());
                }
                ed.commit();

            }
        });


        if (mIsListMode) {

            mGridView.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);

            mDragBaseAdapter = new DragBaseAdapter();
            mDragSortListView.setAdapter(mDragBaseAdapter);

            mSortModeTxt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.list_list), null, null, null);
            mSortModeTxt.setCompoundDrawablePadding(8);
            mSortModeTxt.setText("리스트 정렬");

        } else {

            mGridView.setVisibility(View.VISIBLE);
            mListLayout.setVisibility(View.GONE);

            mItemSimpleAdapter = new ItemSimpleAdapter(getActivity(), mWindowWidth, mWidth, mHeight, mFavoriteAndHistoryItem, mMode, mBusTypeHash);
            mItemSimpleAdapter.setItemAdapterListener(this);
            mGridView.setRealHeight(mWindowHeight);
            mGridView.setAdapter(mItemSimpleAdapter);

            mGridView.setRowSize(mWidth, (int) getResources().getDimension(R.dimen.ticket_height));

            mSortModeTxt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.list_card), null, null, null);
            mSortModeTxt.setCompoundDrawablePadding(8);
            mSortModeTxt.setText("카드형 정렬");

        }

        mSortModeTxt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIsListMode) {
                    mSortModeTxt.setText("카드형 정렬");

                    mIsListMode = false;
                    setFavoriteMode(Constants.SETTING_FAOVIRET_TICKET);
                    mSortModeTxt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.list_card), null, null, null);
                    mSortModeTxt.setCompoundDrawablePadding(8);

                } else {

                    mSortModeTxt.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.list_list), null, null, null);
                    mSortModeTxt.setCompoundDrawablePadding(8);

                    mSortModeTxt.setText("리스트 정렬");
                    mIsListMode = true;
                    setFavoriteMode(Constants.SETTING_FAOVIRET_LIST);

                }


                if (mRefreshRunnable != null && mHandler != null) {
                    mHandler.removeCallbacks(mRefreshRunnable);
                    mHandler.postDelayed(mRefreshRunnable, 1000);
                }


                selectedPage();


            }
        });


        mDragSortListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {

                if (mMode == Constants.WIDGET_MODE_NOTHING) {
                    if (mEditMode) {

                    } else {

                        showDialog(position);
                    }

                }


                return false;
            }
        });


        mDragSortListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
                                    long arg3) {

                if (mMode == Constants.WIDGET_MODE_NOTHING) {

                    FavoriteAndHistoryItem item = null;
                    try {
                        item = mFavoriteAndHistoryItem.get(position);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;

                    }
                    ;


                    if (mEditMode) {

                        favoriteModifyDialog(position);


                    } else {

                        playVibe();

                        if (item.type == Constants.FAVORITE_TYPE_BUS) {
                            Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                            sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSROUTEITEM);
                            sendIntent.putExtra("busInfor", item.busRouteItem);
                            startActivity(sendIntent);
                        } else if (item.type == Constants.FAVORITE_TYPE_STOP) {
                            Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                            sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                            sendIntent.putExtra("busInfor", item.busStopItem);
                            startActivity(sendIntent);
                        } else if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {

                            GetData getData = new GetData(new GetDataListener() {

                                @Override
                                public void onCompleted(int type, DepthItem item) {

                                    if (mDragBaseAdapter != null) {
                                        if (mProgressPositionList.size() > position) {
                                            mProgressPositionList.remove(position);
                                        }

                                        mDragBaseAdapter.notifyDataSetChanged();
                                    }
                                }
                            }, getBusDbSqlite(), mHashLocationEng);

                            mFavoriteAndHistoryItem.get(position).busRouteItem.arriveInfo.clear();
                            DepthFavoriteItem favoDepthFavoriteItem = new DepthFavoriteItem();
                            favoDepthFavoriteItem.favoriteAndHistoryItems.add(mFavoriteAndHistoryItem.get(position));
                            mProgressPositionList.add(position);

                            getData.startOneRefrshService(favoDepthFavoriteItem);


                            try {
                                if (mDragBaseAdapter != null) {
                                    mDragBaseAdapter.notifyDataSetChanged();
                                }
                            } catch (Exception e) {
                            }
                            ;


                        }

                    }

                }


            }

        });

        mDragSortListView.setDropListener(new DropListener() {
            @Override
            public void drop(int from, int to) {

                if (from != to) {

                    FavoriteAndHistoryItem item1 = mFavoriteAndHistoryItem.get(from);
                    FavoriteAndHistoryItem item2 = mFavoriteAndHistoryItem.get(to);

                    mFavoriteAndHistoryItem.remove(from);
                    mFavoriteAndHistoryItem.add(to, item1);


                    for (int i = 0; i < mFavoriteAndHistoryItem.size(); i++) {
                        String updateSql = String.format("UPDATE %s SET %s=%s where _id=%s", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_ORDER, i, mFavoriteAndHistoryItem.get(i).id);
                        getLocalDBHelper().writeFavoriteValue(updateSql);
                    }


                    mDragBaseAdapter.notifyDataSetChanged();


                }

            }
        });


        mGridView.setDragSource(new DragSource() {

            @Override
            public void onDragStartPreced() {
                // TODO Auto-generated method stub

                playVibe();
            }

            @Override
            public void onDragEnded() {

                LinkedHashMap<Integer, FavoriteAndHistoryItem> hashFavorite = new LinkedHashMap<Integer, FavoriteAndHistoryItem>();
                for (int j = 0; j < mGridView.getChildCount(); j++) {

                    DragView dragView = ((DragView) mGridView.getChildAt(j));
                    hashFavorite.put(dragView.getIndex(), dragView.getItem());
                }

                Iterator iterator = hashFavorite.entrySet().iterator();
                int i = 0;

                mFavoriteAndHistoryItem.clear();
                while (iterator.hasNext()) {
                    Entry entry = (Entry) iterator.next();
                    ;
                    mFavoriteAndHistoryItem.add((FavoriteAndHistoryItem) entry.getValue());
                    String updateSql = String.format("UPDATE %s SET %s=%s where _id=%s", LocalDBHelper.TABLE_FAVORITE_NAME, LocalDBHelper.TABLE_ORDER, entry.getKey(), mFavoriteAndHistoryItem.get(i).id);
                    getLocalDBHelper().writeFavoriteValue(updateSql);
                    i++;
                }
                mItemSimpleAdapter.notifyDataSetChanged();

            }

            @Override
            public void onClickDragAndDropElement(DragAndDropElement v) {


            }

            @Override
            public void setOnItemClickListener(final int position) {


                if (mIsClick) return;
                FavoriteAndHistoryItem item = null;
                //싱크가 안맞아서 죽는 경우를 위해서 예외처리
                try {
                    item = mFavoriteAndHistoryItem.get(position);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;

                }
                ;


                mIsClick = true;
                mHandler.postDelayed(mRunnable, 300);


                if (mMode == Constants.WIDGET_MODE_BUS_STOP) {


                    if (mSBFavoriteFragmentListener != null) {


                        if (!RequestCommonFuction.getAlarmAndWigetAble(Integer.parseInt(item.busRouteItem.localInfoId))) {
                            Toast.makeText(getActivity(), "위젯을 생성 할 수 없는 지역입니다.", 1000).show();
                            return;
                        }
                        mSBFavoriteFragmentListener.clickItem(item);
                    }


                } else if (mMode == Constants.WIDGET_MODE_STOP) {


                    if (!RequestCommonFuction.getAlarmAndWigetAble(Integer.parseInt(item.busStopItem.localInfoId))) {
                        Toast.makeText(getActivity(), "위젯을 생성 할 수 없는 지역입니다.", 1000).show();
                        return;
                    }

                    mSBselectFragment = new SBSelectFragment(R.layout.selectfragment, getBusDbSqlite(), getLocalDBHelper());
                    mSBselectFragment.setBusStopItem(item.busStopItem);
                    mSBselectFragment.setStopWidgetMode(true);
                    mSBselectFragment.setSBSelectFragmentListener(new SBSelectFragmentListener() {
                        @Override
                        public void onChange(CommonItem item) {

                        }
                    });


                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameFavorite, mSBselectFragment).addToBackStack("search").commit();


                    if (mSBFavoriteFragmentListener != null) {
                        mSBFavoriteFragmentListener.clickItem(item);
                    }


                } else {


                    if (mEditMode) {

                        favoriteModifyDialog(position);

                    } else {

                        playVibe();

                        if (item.type == Constants.FAVORITE_TYPE_BUS) {
                            Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                            sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSROUTEITEM);
                            sendIntent.putExtra("busInfor", item.busRouteItem);
                            startActivity(sendIntent);
                        } else if (item.type == Constants.FAVORITE_TYPE_STOP) {
                            Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                            sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                            sendIntent.putExtra("busInfor", item.busStopItem);
                            startActivity(sendIntent);
                        } else if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {

                            GetData getData = new GetData(new GetDataListener() {

                                @Override
                                public void onCompleted(int type, DepthItem item) {

                                    DragView dragView = (DragView) mGridView.getChildAt(position);
                                    dragView.setView(mFavoriteAndHistoryItem.get(position));

                                    dragView.findViewById(R.id.itemProgress).setVisibility(View.GONE);

                                    if (mItemSimpleAdapter != null) {
                                        mItemSimpleAdapter.notifyDataSetChanged();
                                    }
                                }
                            }, getBusDbSqlite(), mHashLocationEng);

                            mFavoriteAndHistoryItem.get(position).busRouteItem.arriveInfo.clear();
                            DepthFavoriteItem favoDepthFavoriteItem = new DepthFavoriteItem();
                            favoDepthFavoriteItem.favoriteAndHistoryItems.add(mFavoriteAndHistoryItem.get(position));

                            getData.startOneRefrshService(favoDepthFavoriteItem);
                            DragView dragView = ((DragView) mGridView.getChildAt(position));
                            dragView.findViewById(R.id.itemProgress).setVisibility(View.VISIBLE);

                        }

                    }

                }


            }

            @Override
            public void setonItemLongClickListener(int position) {
                if (mMode == Constants.WIDGET_MODE_NOTHING) {
                    if (mEditMode) {
                    } else {
                        showDialog(position);
                    }

                }

            }
        });


        mEditBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditBtn.getText().equals("완료")) {

                    mEditBtn.setText("편집");
                    mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit), null, null, null);
                    mEditMode = false;


                    if (mIsListMode) {

                        mDragSortListView.setDragEnabled(false);

                        if (mDragBaseAdapter != null) {
                            mDragBaseAdapter.notifyDataSetChanged();
                        }

                    } else {

                        if (mItemSimpleAdapter != null) {
                            mItemSimpleAdapter.setEditMode(false);
                            mGridView.setEditMode(false);

                            mItemSimpleAdapter.notifyDataSetChanged();
                        }

                    }

                    if (mHandler != null && mRefreshRunnable != null) {
                        mHandler.removeCallbacks(mRefreshRunnable);
                        mHandler.postDelayed(mRefreshRunnable, 1000);
                    }


                } else {
                    mEditBtn.setText("완료");
                    mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_end), null, null, null);
                    mEditMode = true;

                    if (mIsListMode) {

                        mDragSortListView.setDragEnabled(true);

                        if (mDragBaseAdapter != null) {
                            mDragBaseAdapter.notifyDataSetChanged();
                        }


                    } else {

                        if (mItemSimpleAdapter != null) {
                            mItemSimpleAdapter.setEditMode(true);
                            mGridView.setEditMode(true);

                            mItemSimpleAdapter.notifyDataSetChanged();
                        }

                    }


                }
            }
        });


        if (mFavoriteAndHistoryItem.size() > 0) {
            if (mNothingContents != null) {
                mNothingContents.setVisibility(View.GONE);
            }
        } else {
            if (mNothingContents != null) {
                mNothingContents.setVisibility(View.VISIBLE);
            }
        }

        if (mHandler != null && mRefreshRunnable != null) {
            mHandler.removeCallbacks(mRefreshRunnable);
            mHandler.postDelayed(mRefreshRunnable, 1000);
        }

    }


    private void favoriteModifyDialog(int index) {

        if (mFavoriteAndHistoryItem == null || mFavoriteAndHistoryItem.size() == 0) return;

        final FavoriteAndHistoryItem item = mFavoriteAndHistoryItem.get(index);

        Intent intent = new Intent(getActivity(), SBEditFavoriteActivity.class);
        intent.putExtra(Constants.INTENT_FAVORITEITEM, item);
        startActivity(intent);

    }

    private void deleteDialog(final int index) {
        final SBDialog dialog = new SBDialog(getActivity());
        dialog.setViewLayout("선택한  즐겨찾기를 삭제하시겠습니까?");
        dialog.getPositiveButton("삭제").setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {


                if (mIsListMode) {
                    getLocalDBHelper().deleteFavorite(mFavoriteAndHistoryItem.get(index).id);

                    mFavoriteAndHistoryItem.remove(index);

                    if (mDragBaseAdapter != null) {
                        mDragBaseAdapter.notifyDataSetChanged();
                    }
                } else {

                    for (int i = 0; i < mGridView.getChildCount(); i++) {
                        if (((DragView) mGridView.getChildAt(i)).getIndex() == index) {

                            for (int j = 0; j < mFavoriteAndHistoryItem.size(); j++) {

                                if (((DragView) mGridView.getChildAt(i)).getItem().id == mFavoriteAndHistoryItem.get(j).id) {
                                    mFavoriteAndHistoryItem.remove(j);
                                    getLocalDBHelper().deleteFavorite(((DragView) mGridView.getChildAt(i)).getItem().id);
                                    break;
                                }
                            }
                            mGridView.removeViewAt(i);

                            mItemSimpleAdapter.setITem(mFavoriteAndHistoryItem);
                            mGridView.setAdapter(mItemSimpleAdapter);
                            mGridView.setEditMode(mEditMode);


                            if (mFavoriteAndHistoryItem.size() > 0) {
                                if (mNothingContents != null) {
                                    mNothingContents.setVisibility(View.GONE);
                                }
                            } else {
                                if (mNothingContents != null) {
                                    mNothingContents.setVisibility(View.VISIBLE);
                                }
                            }


                            break;
                        }
                    }


                    mItemSimpleAdapter.notifyDataSetChanged();

                }


                dialog.dismiss();
            }

        });
        dialog.getNegativeButton("취소").setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    private void showDialog(final int index) {

        if (mFavoriteAndHistoryItem.size() == 0 && index == -1) return;
        FavoriteAndHistoryItem item = null;

        try {
            item = mFavoriteAndHistoryItem.get(index);
        } catch (Exception e) {
            return;
        }

        String title = item.nickName;
        if (item.type == Constants.FAVORITE_TYPE_BUS) {
            title = item.busRouteItem.busRouteName + "번(" + mTerminus.get(Integer.parseInt(item.busRouteItem.endStop)) + ")";

        } else if (item.type == Constants.FAVORITE_TYPE_STOP) {
            title = item.nickName + "(" + item.nickName2 + ")";

        } else if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {
            title = item.busRouteItem.busRouteName + "번(" + item.nickName + ")";

        }


        ArrayList<String> stringList = new ArrayList<String>();


        View view = getActivity().getLayoutInflater().inflate(R.layout.common_list, null);
        final ListView listView = (ListView) view.findViewById(R.id.list);

        String[] contents = getResources().getStringArray(R.array.list_favorite);

        for (int i = 0; i < contents.length; i++) {
            if (item.type == Constants.FAVORITE_TYPE_BUS_STOP && i == 0) continue;
            stringList.add(contents[i]);
        }

        final CommonAdapter commonAdapter = new CommonAdapter(stringList, getActivity(), CommonAdapter.TYPE_COMMON_TEXTVIEW);

        final SBDialog dialog = new SBDialog(getActivity());
        dialog.setTitleLayout(title, 0xFF0998FF);
        dialog.setViewLayout(view);
        dialog.setCancelable(true);

        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                listView.setAdapter(commonAdapter);
            }
        });
        dialog.show();


        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {


                FavoriteAndHistoryItem item = mFavoriteAndHistoryItem.get(index);
                if (item.type == Constants.FAVORITE_TYPE_BUS
                        || item.type == Constants.FAVORITE_TYPE_STOP) {
                    switch (position) {
                        case 0:
                            shortcutIntent(mFavoriteAndHistoryItem.get(index));
                            dialog.dismiss();
                            break;

                        case 1:
                            favoriteModifyDialog(index);
                            dialog.dismiss();
                            break;

                        case 2:
                            deleteDialog(index);
                            dialog.dismiss();
                            break;
                    }

                } else {
                    switch (position) {
                        case 0:
                            favoriteModifyDialog(index);
                            dialog.dismiss();
                            break;
                        case 1:
                            deleteDialog(index);
                            dialog.dismiss();
                            break;
                        case 2:
                            break;

                    }
                }
            }
        });

    }


    private void getValue() {
        //데이터 가져오는 부분
        mTmpFavoriteAndHistoryItem.clear();


        Cursor cursor;

        if (mMode == Constants.WIDGET_MODE_NOTHING) {
            cursor = getLocalDBHelper().getFavoriteValue();
        } else {
            cursor = getLocalDBHelper().getFavoriteValue(mMode);
        }


        int count = 0;

        while (cursor.moveToNext()) {

            int _id = cursor.getInt(cursor.getColumnIndex(LocalDBHelper.TABLE_ID_F));
            int cityId = cursor.getInt(cursor.getColumnIndex(LocalDBHelper.TABLE_CITY_F));


            String type = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPE_F));
            String typeId1 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID_F));
            String typeId2 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID2_F));
            String typeId3 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID3_F));
            String typeId4 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID4_F));


            String nick = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPE_NICK));
            String nick2 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPE_NICK2));

            String color = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_COLOR_F));

            String temp1 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TEMP1));
            String temp2 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TEMP2));

            FavoriteAndHistoryItem item = new FavoriteAndHistoryItem();
            item.id = _id;
            item.nickName = nick;
            item.nickName2 = nick2;
            item.color = color;
            item.key = count;

            String cityEnName = ((SBInforApplication) getActivity().getApplicationContext()).mHashLocation.get(cityId);


            if (type.equals(String.valueOf(Constants.BUS_TYPE))) {

                String sql;

                if (typeId2 == null || typeId2.equals("")) {

                    sql = String.format("Select * From %s where %s='%s'",
                            cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1);

                } else {

                    if (cityEnName.equals(CommonConstants.CITY_GU_MI._engName) || cityEnName.equals(CommonConstants.CITY_CHIL_GOK._engName)) {

                        sql = String.format("Select * From %s where %s='%s' and %s='%s' and %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2
                                , CommonConstants.BUS_ROUTE_NAME, nick2);
                    } else {

                        sql = String.format("Select * From %s where %s='%s' and %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2);

                    }

                }


                Cursor busCursor = getBusDbSqlite().rawQuery(sql, null);

                if (busCursor.moveToNext()) {

                    item.type = Constants.FAVORITE_TYPE_BUS;
                    item.busRouteItem.busRouteApiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    item.busRouteItem.busRouteApiId2 = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                    item.busRouteItem.busRouteName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                    item.busRouteItem.busRouteSubName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                    item.busRouteItem.startStop = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                    item.busRouteItem.endStop = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                    item.busRouteItem.busType = busCursor.getInt(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                    item.busRouteItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));

                    item.busRouteItem.localInfoId = String.valueOf(cityId);
                    mTmpFavoriteAndHistoryItem.add(item);
                }
                busCursor.close();
            } else if (type.equals(String.valueOf(Constants.STOP_TYPE))) {

                String sql = "";

                int apiType = RequestCommonFuction.getApiTpye(cityId);


                if (apiType == Constants.API_TYPE_3) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_ARS_ID, typeId4);
                } else if (apiType == Constants.API_TYPE_1) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_API_ID, typeId3);
                } else if (apiType == Constants.API_TYPE_2) {


                    if (typeId3 == null || typeId3.trim().equals("") || typeId3.trim().equals("null")) {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    } else if (typeId4 == null || typeId4.trim().equals("") || typeId4.trim().equals("null")) {
                        typeId3 = String.valueOf(Integer.parseInt(typeId3));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3);

                    } else {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' and %s='%s'", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3, CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    }

                } else if (apiType == Constants.API_TYPE_4) {

                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_DESC, temp1);
                }


                Cursor busCursor = getBusDbSqlite().rawQuery(sql, null);
                if (busCursor.moveToNext()) {
                    item.type = Constants.FAVORITE_TYPE_STOP;
                    item.busStopItem.apiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    item.busStopItem.arsId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    item.busStopItem.name = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    item.busStopItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));
                    item.busStopItem.tempId2 = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                    item.busStopItem.localInfoId = String.valueOf(cityId);


                    mTmpFavoriteAndHistoryItem.add(item);
                }
                busCursor.close();
            } else if (type.equals(String.valueOf(Constants.FAVORITE_TYPE_BUS_STOP))) {


                String sql = "";

                int apiType = RequestCommonFuction.getApiTpye(cityId);


                if (apiType == Constants.API_TYPE_3) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_ARS_ID, typeId4);
                } else if (apiType == Constants.API_TYPE_1) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_API_ID, typeId3);
                } else if (apiType == Constants.API_TYPE_2) {

                    if (typeId3 == null || typeId3.trim().equals("") || typeId3.trim().equals("null")) {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    } else if (typeId4 == null || typeId4.trim().equals("") || typeId4.trim().equals("null")) {
                        typeId3 = String.valueOf(Integer.parseInt(typeId3));
                        sql = String.format(
                                "Select * From %s where %s='%s' ", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3);

                    } else {
                        typeId4 = String.valueOf(Integer.parseInt(typeId4));
                        sql = String.format(
                                "Select * From %s where %s='%s' and %s='%s'", cityEnName
                                        + "_stop", CommonConstants.BUS_STOP_API_ID,
                                typeId3, CommonConstants.BUS_STOP_ARS_ID,
                                typeId4);
                    }

                } else if (apiType == Constants.API_TYPE_4) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_DESC, temp1);
                }


                if (cityEnName.equals(CommonConstants.CITY_GU_MI._engName) || cityEnName.equals(CommonConstants.CITY_CHIL_GOK._engName)) {
                    if (typeId2.equals("null")) {
                        typeId2 = "";
                    }
                } else {
                    if (typeId2.equals("null") || typeId2.equals("0")) {
                        typeId2 = "";
                    }
                }

                String sql2 = null;

                if (cityEnName.equals(CommonConstants.CITY_GU_MI._engName) || cityEnName.equals(CommonConstants.CITY_CHIL_GOK._engName)) {

                    sql2 = String.format("Select * From %s where %s='%s' and %s='%s' and %s='%s'",
                            cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2
                            , CommonConstants.BUS_ROUTE_NAME, nick2);
                } else {

                    if (typeId2 == null || typeId2.equals("")) {

                        sql2 = String.format("Select * From %s where %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1);

                    } else {

                        sql2 = String.format("Select * From %s where %s='%s' and %s='%s'",
                                cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1, typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2);
                    }

                }

                Cursor busCursor2 = getBusDbSqlite().rawQuery(sql2, null);
                Cursor busCursor = getBusDbSqlite().rawQuery(sql, null);

                if (busCursor2.moveToNext() && busCursor.moveToNext()) {


                    item.type = Constants.FAVORITE_TYPE_BUS_STOP;
                    item.busRouteItem.busRouteApiId = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    item.busRouteItem.busRouteApiId2 = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                    item.busRouteItem.busRouteName = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                    item.busRouteItem.busRouteSubName = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                    item.busRouteItem.startStop = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                    item.busRouteItem.endStop = busCursor2.getString(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                    item.busRouteItem.busType = busCursor2.getInt(busCursor2.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));

                    if (temp2 != null && temp2.trim().length() > 0) {
                        item.busRouteItem.direction = temp2;
                    }

                    item.busRouteItem.busStopApiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    item.busRouteItem.busStopArsId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    item.busRouteItem.busStopName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    item.busRouteItem.tmpId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                    item.busStopItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));


                    item.busRouteItem.localInfoId = String.valueOf(cityId);
                    item.busStopItem.localInfoId = String.valueOf(cityId);

                    mTmpFavoriteAndHistoryItem.add(item);

                }


                busCursor.close();
                busCursor2.close();

            }

            count++;
        }
        cursor.close();
    }


    @Override
    public void selectedPage() {
        super.selectedPage();

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_favorite));
        t.send(new HitBuilders.AppViewBuilder().build());

        if (isInit) {

            isInit = false;
            mInnerAsync = new InnerAsync();
            mInnerAsync.execute();

        } else {


            mInnerAsync = new InnerAsync();
            mInnerAsync.execute();

        }

    }

    @Override
    public void unSelectedPage() {
        super.unSelectedPage();

        if (mInnerAsync != null) {
            mInnerAsync.cancel(true);
        }


        if (mRefreshRunnable != null && mRefreshRunnable != null) {
            mHandler.removeCallbacks(mRefreshRunnable);
        }


        if (mGetDataList != null) {
            for (int i = 0; i < mGetDataList.size(); i++) {
                mGetDataList.get(i).clear();
            }

        }


        if (mEditMode) {
            ((SBMainActivity) getActivity()).mPager.setEnabled(false);
            if (mSortModeTxt != null) {

            }
        } else {
            ((SBMainActivity) getActivity()).mPager.setEnabled(true);
        }

    }

    //
    @Override
    public void pageChange() {

        super.pageChange();
        mEditBtn.setText("편집");
        mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit), null, null, null);
        if (mSortModeTxt != null) {

        }

        if (mGetDataList != null) {
            for (int i = 0; i < mGetDataList.size(); i++) {
                mGetDataList.get(i).clear();
            }
        }

        if (mProgressPositionList != null) {
            mProgressPositionList.clear();
        }


        mEditMode = false;
        if (mIsListMode) {
            if (mDragSortListView != null) {
                mDragSortListView.setDragEnabled(false);
            }
        } else {
            if (mItemSimpleAdapter != null) {
                mItemSimpleAdapter.setEditMode(false);
                mGridView.setEditMode(false);
            }
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
            Toast.makeText(getActivity(), "홈에 바로가기가 추가 되었습니다.", 1000).show();
        }

    }


    class InnerAsync extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            if (!isCancelled()) {
                mTmpFavoriteAndHistoryItem.clear();
                getValue();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (!isCancelled()) {
                mFavoriteAndHistoryItem = mTmpFavoriteAndHistoryItem;

                if (mFavoriteAndHistoryItem.size() > 0) {
                    if (mNothingContents != null) {
                        mNothingContents.setVisibility(View.GONE);
                    }
                } else {
                    if (mNothingContents != null) {
                        mNothingContents.setVisibility(View.VISIBLE);
                    }
                }
                setFavorite();


            }

            if (mHandler != null && mRefreshRunnable != null) {
                mHandler.removeCallbacks(mRefreshRunnable);
                mHandler.postDelayed(mRefreshRunnable, 1000);
            }


        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

    public void setFavorite() {

        if (mIsListMode) {

            mGridView.setVisibility(View.GONE);
            mListLayout.setVisibility(View.VISIBLE);
            if (mDragBaseAdapter == null) {
                mDragBaseAdapter = new DragBaseAdapter();
                mDragSortListView.setAdapter(mDragBaseAdapter);
            } else {
                mDragBaseAdapter.notifyDataSetChanged();
            }


        } else {

            mGridView.setVisibility(View.VISIBLE);
            mListLayout.setVisibility(View.GONE);


            if (mItemSimpleAdapter != null) {
                mItemSimpleAdapter.setITem(mFavoriteAndHistoryItem);
                mGridView.setAdapter(mItemSimpleAdapter);

                if (mEditMode) {
                    if (mItemSimpleAdapter != null) {
                        mItemSimpleAdapter.setEditMode(true);
                    }

                    if (mGridView != null) {
                        mGridView.setEditMode(true);
                    }

                    if (mEditBtn != null) {
                        mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_end), null, null, null);
                    }

                }

                mScrollView.invalidate();
                mScrollView.requestLayout();

            } else {

                mItemSimpleAdapter = new ItemSimpleAdapter(getActivity(), mWindowWidth, mWidth, mHeight, mFavoriteAndHistoryItem, mMode, mBusTypeHash);
                mItemSimpleAdapter.setItemAdapterListener(this);
                mGridView.setRealHeight(mWindowHeight);
                mGridView.setAdapter(mItemSimpleAdapter);

                mGridView.setRowSize(mWidth, (int) getResources().getDimension(R.dimen.ticket_height));
            }

        }


    }


    public void setWidgetId(int widgetId) {
        mWidgetId = widgetId;
    }


    @Override
    public void onRefresh(final int position) {


        if (mMode == Constants.WIDGET_TYPE_BUS
                || mMode == Constants.WIDGET_MODE_BUS_STOP) {
            return;
        }


        GetData getData = new GetData(new GetDataListener() {

            @Override
            public void onCompleted(int type, DepthItem item) {

                for (int i = 0; i < mGridView.getChildCount(); i++) {

                    DragView dragView = (DragView) mGridView.getChildAt(i);
                    dragView.findViewById(R.id.itemProgress).setVisibility(View.GONE);


                    if (dragView.getIndex() == position) {
                        for (int j = 0; j < mFavoriteAndHistoryItem.size(); j++) {
                            if (dragView.getItem().id == mFavoriteAndHistoryItem.get(j).id) {

                                dragView.setView(mFavoriteAndHistoryItem.get(j));

                                break;
                            }
                        }
                        break;
                    }

                }


            }
        }, getBusDbSqlite(), mHashLocationEng);


        if (mFavoriteAndHistoryItem.size() > 0) {

            for (int i = 0; i < mGridView.getChildCount(); i++) {

                DragView dragView = (DragView) mGridView.getChildAt(i);

                if (dragView.getIndex() == position) {
                    for (int j = 0; j < mFavoriteAndHistoryItem.size(); j++) {
                        if (dragView.getItem().id == mFavoriteAndHistoryItem.get(j).id) {

                            mFavoriteAndHistoryItem.get(j).busRouteItem.arriveInfo.clear();
                            DepthFavoriteItem favoDepthFavoriteItem = new DepthFavoriteItem();
                            favoDepthFavoriteItem.favoriteAndHistoryItems.add(mFavoriteAndHistoryItem.get(j));

                            getData.startOneRefrshService(favoDepthFavoriteItem);
                            break;
                        }

                    }

                    break;
                }


            }


        }

    }

    @Override
    public void onDelete(final int position) {

        deleteDialog(position);

        // TODO Auto-generated method stub

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    public void setChecked(boolean isChecked) {
        mSBselectFragment.setChecked(isChecked);
    }


    //special
    public ArrayList<BusRouteItem> getSBSelctBusRouteList() {
        return mSBselectFragment.getSBSelctBusRouteList();
    }


    public void showChoiceFavoriteTypeDialog(final boolean isListMode) {

        final SBChoiceDialog sbChoiceDialog = new SBChoiceDialog(getActivity(), isListMode);

        if (isListMode) {
            sbChoiceDialog.setCurrentPosition(getPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE));
        } else {
            sbChoiceDialog.setCurrentPosition(getPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE));
        }


        sbChoiceDialog.setTitleLayout("테마 설정");
        sbChoiceDialog.getPositiveButton("확인");
        sbChoiceDialog.getNegativeButton("취소");

        //sbChoiceDialog.mPositiveBtn.setVisibility(View.GONE);


        sbChoiceDialog.mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE, Constants.SETTING_FAOVIRET_LIST_1);
                } else if (position == 1) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE, Constants.SETTING_FAOVIRET_LIST_2);
                } else if (position == 2) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE, Constants.SETTING_FAOVIRET_LIST_3);
                } else if (position == 3) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE, Constants.SETTING_FAOVIRET_LIST_4);
                } else if (position == 4) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE, Constants.SETTING_FAOVIRET_LIST_5);
                }


                sbChoiceDialog.dismiss();

                if (mDragBaseAdapter != null) {
                    mDragBaseAdapter.notifyDataSetChanged();
                }
            }
        });

        sbChoiceDialog.mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE, Constants.SETTING_FAOVIRET_TICKET_1);
                } else if (position == 1) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE, Constants.SETTING_FAOVIRET_TICKET_2);
                } else if (position == 2) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE, Constants.SETTING_FAOVIRET_TICKET_3);
                } else if (position == 3) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE, Constants.SETTING_FAOVIRET_TICKET_4);
                } else if (position == 4) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE, Constants.SETTING_FAOVIRET_TICKET_5);
                } else if (position == 5) {
                    setPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE, Constants.SETTING_FAOVIRET_TICKET_6);
                }

                sbChoiceDialog.dismiss();

                if (mGridView != null) {
                    mGridView.setViewChange();
                }

            }

        });

        sbChoiceDialog.mPositiveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                sbChoiceDialog.dismiss();

            }
        });


        sbChoiceDialog.mNegativeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                sbChoiceDialog.dismiss();
            }
        });

        ;

        sbChoiceDialog.show();

        WindowManager.LayoutParams lp = sbChoiceDialog.getWindow().getAttributes();

        if (isListMode) {
            lp.width = (int) getResources().getDimension(R.dimen.choice_dialog_width);
            ;
            lp.height = (int) getResources().getDimension(R.dimen.choice_dialog_height);
        } else {
            lp.width = lp.WRAP_CONTENT;
            lp.height = (int) getResources().getDimension(R.dimen.choice_dialog_height2);
        }

        sbChoiceDialog.getWindow().setAttributes(lp);


    }


    //dragSortListView
    class DragBaseAdapter extends BaseAdapter {

        LinearLayout.LayoutParams _layoutParams;

        @Override
        public int getCount() {

            return mFavoriteAndHistoryItem.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;

            if (convertView == null) {


                convertView = getActivity().getLayoutInflater().inflate(R.layout.favorite_list_row,
                        parent, false);


                viewHolder = new ViewHolder();

                viewHolder.arriveLayout = (LinearLayout) convertView.findViewById(R.id.arriveLayout);
                viewHolder.arrive1 = (TextView) convertView.findViewById(R.id.arrive_1);
                viewHolder.arrive2 = (TextView) convertView.findViewById(R.id.arrive_2);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.subName1 = (TextView) convertView.findViewById(R.id.subMain);
                viewHolder.subName2 = (TextView) convertView.findViewById(R.id.subMain2);
                viewHolder.iconImgView = (ImageView) convertView.findViewById(R.id.iconImg);
                viewHolder.drag_handle = (ImageView) convertView.findViewById(R.id.drag_handle);
                viewHolder.delBtn = (ImageView) convertView.findViewById(R.id.delImg);
                viewHolder.progress = (ProgressBar) convertView.findViewById(R.id.list_progress);

                viewHolder.colorLayout = (ImageView) convertView.findViewById(R.id.colorLayout);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            FavoriteAndHistoryItem item = mFavoriteAndHistoryItem.get(position);
            if (item.type == Constants.FAVORITE_TYPE_BUS) {

                viewHolder.name.setText(item.busRouteItem.busRouteName);
                viewHolder.subName1.setText(item.nickName);

                viewHolder.arriveLayout.setVisibility(View.GONE);


                viewHolder.iconImgView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bus));

                try {

                    String color = mBusTypeHash.get(item.busRouteItem.busType);
                    viewHolder.name.setTextColor(Color.parseColor("#"
                            + color));
                } catch (Exception e) {
                    viewHolder.name.setTextColor(Color
                            .parseColor("#00000"));
                }


            } else if (item.type == Constants.FAVORITE_TYPE_STOP) {

                viewHolder.name.setText(item.nickName);
                viewHolder.subName1.setText(item.nickName2);

                viewHolder.arriveLayout.setVisibility(View.GONE);


                viewHolder.iconImgView.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_stop));

            } else if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {
                viewHolder.name.setText(item.busRouteItem.busRouteName);
                viewHolder.subName1.setText(item.nickName2);

                viewHolder.arriveLayout.setVisibility(View.GONE);


                try {

                    String color = mBusTypeHash.get(item.busRouteItem.busType);
                    viewHolder.name.setTextColor(Color.parseColor("#"
                            + color));
                } catch (Exception e) {
                    viewHolder.name.setTextColor(Color
                            .parseColor("#000000"));
                }


                viewHolder.iconImgView.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_busstop));


            }

            // 도착 정보 표시 부분
            if (item.busRouteItem.arriveInfo.size() > 0) {

                if (item.busRouteItem.arriveInfo.size() > 0) {
                    if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {
                        viewHolder.arriveLayout.setVisibility(View.VISIBLE);
                        viewHolder.arrive1.setVisibility(View.VISIBLE);
                    }

                    ArriveItem arriveItem = item.busRouteItem.arriveInfo.get(0);
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
                            resultStr = resultStr + "(" + arriveItem.remainStop
                                    + "정류장 전)";
                        }
                    }

                    if (arriveItem.state == Constants.STATE_PREPARE) {
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

                    } else if (arriveItem.state == Constants.STATE_END) {
                        resultStr = getResources().getString(R.string.state_end);
                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                        resultStr = getResources().getString(R.string.state_prepare_not);
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        resultStr = getResources().getString(R.string.state_near);
                    }

                    viewHolder.arrive1.setText(resultStr);
                    viewHolder.arrive2.setVisibility(View.GONE);
                }

                if (item.busRouteItem.arriveInfo.size() > 1) {
                    viewHolder.arriveLayout.setVisibility(View.VISIBLE);

                    viewHolder.arrive1.setVisibility(View.VISIBLE);
                    viewHolder.arrive2.setVisibility(View.VISIBLE);

                    ArriveItem arriveItem = item.busRouteItem.arriveInfo.get(1);
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
                            resultStr = resultStr + "(" + arriveItem.remainStop
                                    + "정류장 전)";
                        }
                    }

                    if (arriveItem.state == Constants.STATE_PREPARE) {
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
                    } else if (arriveItem.state == Constants.STATE_END) {
                        resultStr = getResources().getString(R.string.state_end);
                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                        resultStr = getResources().getString(R.string.state_prepare_not);
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        resultStr = getResources().getString(R.string.state_near);
                    }

                    viewHolder.arrive2.setText(resultStr);
                } else {
                    viewHolder.arrive2.setVisibility(View.GONE);
                }
            } else {
                viewHolder.arriveLayout.setVisibility(View.GONE);
                viewHolder.arrive1.setVisibility(View.GONE);
                viewHolder.arrive2.setVisibility(View.GONE);
            }

            if (item.busRouteItem.arriveInfo.size() > 0) {
                viewHolder.subName1.setVisibility(View.VISIBLE);
                viewHolder.subName1.setText(item.nickName);


            } else {


                if (item.type == Constants.FAVORITE_TYPE_STOP) {
                    viewHolder.subName1.setVisibility(View.VISIBLE);
                    viewHolder.subName1.setText(item.nickName2);
                } else {
                    viewHolder.subName1.setVisibility(View.VISIBLE);
                    viewHolder.subName1.setText(item.nickName);
                }

            }

            if (mEditMode) {
                viewHolder.arrive1.setTextColor(getResources()
                        .getColor(R.color.arrive_nomal));
                viewHolder.arrive2.setTextColor(getResources()
                        .getColor(R.color.arrive_nomal));
            } else {
                viewHolder.arrive1.setTextColor(getResources()
                        .getColor(R.color.arrive_color));
                viewHolder.arrive2.setTextColor(getResources()
                        .getColor(R.color.arrive_color));
            }

            String packName = getActivity().getPackageName(); // 패키지명
            if (item.color != null) {

                if (mIsListMode) {

                    if (_layoutParams == null) {
                        _layoutParams = (android.widget.LinearLayout.LayoutParams) viewHolder.colorLayout.getLayoutParams();
                    }

                    int listType = getPrefTypeMode(Constants.SETTING_FAOVIRET_LIST_TYPE);

                    if (listType == Constants.SETTING_FAOVIRET_LIST_1) {

                        String resName = String.format("list_color_%s", item.color);
                        int resID = getResources().getIdentifier(resName,
                                "drawable", packName);


                        LinearLayout.LayoutParams layoutParams = _layoutParams;
                        layoutParams.width = (int) getResources().getDimension(R.dimen.favorite_list_width);
                        layoutParams.height = layoutParams.WRAP_CONTENT;
                        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                        viewHolder.colorLayout.setLayoutParams(layoutParams);
                        viewHolder.colorLayout.setImageResource(resID);


                        viewHolder.colorLayout.setImageResource(resID);

                    } else if (listType == Constants.SETTING_FAOVIRET_LIST_2) {

                        String resName = String.format("list2_color_%s", item.color);
                        int resID = getResources().getIdentifier(resName,
                                "drawable", packName);

                        LayoutParams layoutParams = _layoutParams;
                        layoutParams.height = layoutParams.MATCH_PARENT;
                        layoutParams.width = 10;
                        viewHolder.colorLayout.setLayoutParams(layoutParams);
                        viewHolder.colorLayout.setImageResource(resID);


                    } else if (listType == Constants.SETTING_FAOVIRET_LIST_3) {

                        String resName = String.format("list3_color_%s", item.color);
                        int resID = getResources().getIdentifier(resName,
                                "drawable", packName);


                        LinearLayout.LayoutParams layoutParams = _layoutParams;
                        layoutParams.gravity = Gravity.CENTER;
                        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.favorite_list_margin_left);
                        layoutParams.width = (int) getResources().getDimension(R.dimen.favorite_list_width);
                        layoutParams.height = layoutParams.WRAP_CONTENT;
                        layoutParams.width = layoutParams.WRAP_CONTENT;
                        viewHolder.colorLayout.setLayoutParams(layoutParams);


                        viewHolder.colorLayout.setImageResource(resID);

                    } else if (listType == Constants.SETTING_FAOVIRET_LIST_4) {


                        String resName = String.format("list4_color_%s", item.color);
                        int resID = getResources().getIdentifier(resName,
                                "drawable", packName);

                        LayoutParams layoutParams = _layoutParams;
                        layoutParams.height = layoutParams.MATCH_PARENT;
                        layoutParams.width = 6;
                        viewHolder.colorLayout.setLayoutParams(layoutParams);
                        viewHolder.colorLayout.setImageResource(resID);

                    } else if (listType == Constants.SETTING_FAOVIRET_LIST_5) {

                        String resName = String.format("list5_color_%s", item.color);
                        int resID = getResources().getIdentifier(resName,
                                "drawable", packName);

                        LinearLayout.LayoutParams layoutParams = _layoutParams;
                        layoutParams.width = (int) getResources().getDimension(R.dimen.favorite_list_width);
                        layoutParams.height = layoutParams.WRAP_CONTENT;
                        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.favorite_list_margin_left);
                        viewHolder.colorLayout.setLayoutParams(layoutParams);

                        viewHolder.colorLayout.setImageResource(resID);


                    }


                }


            } else {

                String resName = String.format("list2_color_1_1");
                int resID = getResources().getIdentifier(resName,
                        "drawable", packName);
                viewHolder.colorLayout.setImageResource(resID);
            }


            // 에디트 모드에 따라 del버튼을 보여주고 안보여주고를 처리함
            if (mEditMode) {

                viewHolder.drag_handle.setVisibility(View.VISIBLE);
                viewHolder.arriveLayout.setVisibility(View.GONE);
                viewHolder.progress.setVisibility(View.GONE);
                viewHolder.delBtn.setVisibility(View.VISIBLE);

            } else {
                viewHolder.drag_handle.setVisibility(View.GONE);
                viewHolder.delBtn.setVisibility(View.GONE);

                if (item.busRouteItem.arriveInfo.size() > 0) {
                    viewHolder.progress.setVisibility(View.GONE);
                } else {
                    if (mProgressPositionList.contains(position)) {
                        viewHolder.progress.setVisibility(View.VISIBLE);
                    } else {
                        viewHolder.progress.setVisibility(View.GONE);
                    }
                }


            }


            viewHolder.delBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    deleteDialog(position);

                }
            });


            return convertView;

        }

        class ViewHolder {


            LinearLayout arriveLayout;
            TextView arrive1, arrive2;

            ImageView colorLayout, iconImgView;
            ImageView drag_handle;
            ImageView delBtn;
            TextView name;
            TextView subName1, subName2;

            ProgressBar progress;
        }


    }

    class BannerHolder {

        public String bannerName = "";
        public String URL = "";

    }


}
