package teamdoppelganger.smarterbus;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.SBFavoriteFragment.SBFavoriteFragmentListener;
import teamdoppelganger.smarterbus.SBSearchFragment.SBSearchFragmentListener;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.CommonItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.item.WidgetStopItem;
import teamdoppelganger.smarterbus.service.WidgetProvider2x1;
import teamdoppelganger.smarterbus.service.WidgetProvider4x2;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.FixedTabsView;
import teamdoppelganger.smarterbus.util.widget.KeyboardCheckLayout;
import teamdoppelganger.smarterbus.util.widget.KeyboardCheckLayout.OnSoftKeyboardListener;
import teamdoppelganger.smarterbus.util.widget.TicketLayout;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;


public class SBWidgetActivity extends SherlockFragmentActivity implements OnSoftKeyboardListener {

    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;

    SharedPreferences mPref;
    private LocalInforItem mLocalSaveInfor;

    int mWidgetId;

    private FixedTabsView mTabView;
    private ViewPager mViewPager;

    private int mMode;

    SBAdapter mAdapter;
    TextView mTopic;

    TicketLayout mTicket;
    TextView mOkBtn;
    TextView onCancelBtn;
    TextView mStopTicket;

    CheckBox mAllCheck;


    int mPage = 0;

    private BusStopItem mTempBusStopItem;
    private BusRouteItem mTempBusRouteItem;

    FavoriteAndHistoryItem mFavoriteAndHistoryItem1;
    FavoriteAndHistoryItem mFavoriteAndHistoryItem2;

    ArrayList<BusRouteItem> mBusRouteItemAry;

    int mFavoritePageCount = 1;
    int mSearchPageCount = 1;

    KeyboardCheckLayout mKeyboardCheckLayout;

    HashMap<Integer, String> mBusTypeHash;

    @Override
    protected void onCreate(Bundle arg0) {

        super.onCreate(arg0);
        setContentView(R.layout.widget_activity);

        mKeyboardCheckLayout = (KeyboardCheckLayout) findViewById(R.id.keyboardCheck);
        mKeyboardCheckLayout.setOnSoftKeyboardListener(this);


        mTopic = (TextView) findViewById(R.id.widget_activity_topic);
        mOkBtn = (TextView) findViewById(R.id.ok);
        onCancelBtn = (TextView) findViewById(R.id.cancel);
        mStopTicket = (TextView) findViewById(R.id.stopTicket);
        mAllCheck = (CheckBox) findViewById(R.id.allCheck);

        mFavoriteAndHistoryItem1 = new FavoriteAndHistoryItem();
        mFavoriteAndHistoryItem2 = new FavoriteAndHistoryItem();

        setResult(RESULT_CANCELED);

        mWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (mWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (AppWidgetManager.getInstance(this).getAppWidgetInfo(mWidgetId).provider.toString().contains("2x1")) {

                mMode = Constants.WIDGET_MODE_BUS_STOP;
                mTopic.setText("도착정보 위젯 생성");


                mFavoriteAndHistoryItem1.type = Constants.FAVORITE_TYPE_BUS_STOP;
                mFavoriteAndHistoryItem2.type = Constants.FAVORITE_TYPE_BUS_STOP;


            } else {

                mMode = Constants.WIDGET_MODE_STOP;
                mTopic.setText("정류소 위젯 생성");


                mFavoriteAndHistoryItem1.type = Constants.FAVORITE_TYPE_STOP;
                mFavoriteAndHistoryItem2.type = Constants.FAVORITE_TYPE_STOP;


            }


        }

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);

        try {

            mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            e.printStackTrace();

            Toast.makeText(this, "DB가 설치되지 않았습니다\n전국 스마트 버스 어플리케이션을 한 번 실행해 주세요", Toast.LENGTH_LONG).show();
            finish();

            return;
        }

        mLocalDBHelper = new LocalDBHelper(getApplicationContext());
        mLocalSaveInfor = ((SBInforApplication) getApplicationContext()).mLocalSaveInfor;


        ((SBInforApplication) getApplicationContext()).checkSetting(mLocalDBHelper, mBusDbSqlite, SBWidgetActivity.this);
        ((SBInforApplication) getApplicationContext()).setTerminus(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setCityInfor(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setCityKoInfor(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setBusType(mBusDbSqlite);

        mBusTypeHash = ((SBInforApplication) getApplicationContext()).mBusTypeHash;

        mTicket = (TicketLayout) findViewById(R.id.ticket);

        if (mMode == Constants.WIDGET_MODE_BUS_STOP) {
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();  // deprecated
            int height = display.getHeight();  // deprecated
            int ticketWidth = (int) (width / 2 - getResources().getDimension(R.dimen.ticket_margin));
            mTicket.setViewWidth(ticketWidth);

            mTicket.invalidate();
            mTicket.requestLayout();

        } else {
            mTicket.setVisibility(View.GONE);
            mTicket.invalidate();
            mTicket.requestLayout();
            mStopTicket.setVisibility(View.VISIBLE);
        }


        ((ImageView) findViewById(R.id.btn_image)).setImageDrawable(getResources().getDrawable(R.drawable.ic_busstop));


        mTabView = (FixedTabsView) findViewById(R.id.widget_activity_tab);
        mViewPager = (ViewPager) findViewById(R.id.widget_activity_viewPager);

        mAdapter = new SBAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);


        mTabView.setViewPager(mViewPager);

        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 1) {

                    ((SBSearchFragment) mAdapter.getItem(arg0)).selectedPage();

                    findViewById(R.id.widget_activity_btn_favo).setSelected(false);
                    findViewById(R.id.widget_activity_btn_search).setSelected(true);

                    mPage = 1;

                    if (mMode == Constants.WIDGET_MODE_STOP) {

                        if (mSearchPageCount == 2) {
                            mAllCheck.setVisibility(View.VISIBLE);
                            findViewById(R.id.selectLayout).setVisibility(View.VISIBLE);
                        } else {
                            mAllCheck.setVisibility(View.GONE);
                            findViewById(R.id.selectLayout).setVisibility(View.GONE);
                        }


                        if (mFavoriteAndHistoryItem2.busStopItem != null) {
                            mStopTicket.setText(mFavoriteAndHistoryItem2.busStopItem.name);
                        }


                    } else {
                        findViewById(R.id.selectLayout).setVisibility(View.GONE);


                        if (mFavoriteAndHistoryItem2 != null) {
                            mTicket.setFavoriteItem(mFavoriteAndHistoryItem2);
                            mTicket.invalidate();
                            mTicket.requestLayout();
                        }
                    }


                } else if (arg0 == 0) {
                    ((SBFavoriteFragment) mAdapter.getItem(arg0)).selectedPage();

                    findViewById(R.id.widget_activity_btn_favo).setSelected(true);
                    findViewById(R.id.widget_activity_btn_search).setSelected(false);


                    mPage = 0;


                    if (mMode == Constants.WIDGET_MODE_STOP) {

                        if (mFavoritePageCount == 2) {
                            mAllCheck.setVisibility(View.VISIBLE);
                            findViewById(R.id.selectLayout).setVisibility(View.VISIBLE);
                        } else {
                            mAllCheck.setVisibility(View.GONE);
                            findViewById(R.id.selectLayout).setVisibility(View.GONE);
                        }

                        if (mFavoriteAndHistoryItem1.busStopItem != null) {
                            mStopTicket.setText(mFavoriteAndHistoryItem1.busStopItem.name);
                        }


                    } else {
                        findViewById(R.id.selectLayout).setVisibility(View.GONE);
                        if (mFavoriteAndHistoryItem1 != null) {
                            mTicket.setFavoriteItem(mFavoriteAndHistoryItem1);
                            mTicket.invalidate();
                            mTicket.requestLayout();
                        }
                    }

                }

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });


        mOkBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //정류소 위젯일시
                if (mMode == Constants.WIDGET_MODE_STOP) {

                    ArrayList<BusRouteItem> busRouteItemAry;

                    if (mPage == 0) {
                        busRouteItemAry = ((SBFavoriteFragment) mAdapter.getItem(mPage)).getSBSelctBusRouteList();
                        mBusRouteItemAry = busRouteItemAry;
                    } else {
                        busRouteItemAry = ((SBSearchFragment) mAdapter.getItem(mPage)).getSBSelctBusRouteList();
                        mBusRouteItemAry = busRouteItemAry;
                    }

                    boolean isPass = false;
                    if (busRouteItemAry != null) {

                        for (int i = 0; i < busRouteItemAry.size(); i++) {
                            if (busRouteItemAry.get(i).isChecked) {
                                isPass = true;
                                break;
                            }
                        }

                    }

                    if (isPass) {


                        if (mPage == 0) {
                            if (mFavoriteAndHistoryItem1 != null
                                    && mFavoriteAndHistoryItem1.busRouteItem != null) {

                                //임시적으로 적용

                                if (mFavoriteAndHistoryItem1.color == null
                                        || mFavoriteAndHistoryItem1.color.length() == 0) {
                                    mFavoriteAndHistoryItem1.color = "1_5";
                                }


                                startActivityFromSearch(mFavoriteAndHistoryItem1);

                            } else {

                            }
                        } else if (mPage == 1) {
                            if (mFavoriteAndHistoryItem2 != null
                                    && mFavoriteAndHistoryItem2.busRouteItem != null) {

                                //임시적으로 적용

                                if (mFavoriteAndHistoryItem2.color == null
                                        || mFavoriteAndHistoryItem2.color.length() == 0) {
                                    mFavoriteAndHistoryItem2.color = "1_5";
                                }


                                startActivityFromSearch(mFavoriteAndHistoryItem2);
                            } else {

                            }
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "버스를 선택해주세요", 1000).show();
                    }


                } else {

                    if (mPage == 0) {
                        if (mFavoriteAndHistoryItem1 != null
                                && mFavoriteAndHistoryItem1.busRouteItem != null) {
                            startActivityFromSearch(mFavoriteAndHistoryItem1);
                        } else {
                        }
                    } else if (mPage == 1) {
                        if (mFavoriteAndHistoryItem2 != null
                                && mFavoriteAndHistoryItem2.busRouteItem != null) {
                            startActivityFromSearch(mFavoriteAndHistoryItem2);
                        } else {
                        }
                    }


                }


            }
        });


        onCancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                onBackPressed();

            }
        });


        mAllCheck.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mPage == 0) {
                    ((SBFavoriteFragment) mAdapter.getItem(mPage)).setChecked(mAllCheck.isChecked());
                } else {
                    ((SBSearchFragment) mAdapter.getItem(mPage)).setChecked(mAllCheck.isChecked());
                }

            }
        });


        //init
        findViewById(R.id.selectLayout).setVisibility(View.GONE);
        mAllCheck.setVisibility(View.GONE);


    }

    class SBAdapter extends FragmentStatePagerAdapter {

        SBFavoriteFragment _SBFavoriteFragment;
        SBSearchFragment _SBSearchFragment;

        public SBAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {

            if (position == 0) {

                if (_SBFavoriteFragment != null)
                    return _SBFavoriteFragment;

                _SBFavoriteFragment = new SBFavoriteFragment(R.layout.favorite, mBusDbSqlite, mLocalDBHelper);
                _SBFavoriteFragment.setSBFavoriteFragmentListener(new SBFavoriteFragmentListener() {
                    @Override
                    public void clickItem(FavoriteAndHistoryItem item) {


                        mFavoriteAndHistoryItem1 = item;

                        if (mMode == Constants.WIDGET_MODE_STOP) {
                            mStopTicket.setText(mFavoriteAndHistoryItem1.busStopItem.name);
                            mFavoriteAndHistoryItem1.nickName = mFavoriteAndHistoryItem1.busStopItem.name;

                            mAllCheck.setVisibility(View.VISIBLE);

                            String resName = String.format("widget_top%s", String.valueOf(item.color));
                            int resID = getResources().getIdentifier(resName, "drawable", getPackageName());

                            findViewById(R.id.stopTicket).setBackgroundResource(resID);
                            findViewById(R.id.selectLayout).setVisibility(View.VISIBLE);


                        } else {

                            mTicket.setFavoriteItem(item);
                            mTicket.requestLayout();
                            String packName = getPackageName(); // 패키지명
                            String resName = String.format("tag_%s", item.color);
                            int resID = getResources().getIdentifier(resName, "drawable", packName);
                            mTicket.changeSetColor(resID);
                            findViewById(R.id.selectLayout).setVisibility(View.VISIBLE);

                        }


                        mFavoritePageCount = 2;


                    }
                });

                _SBFavoriteFragment.setMode(mMode);
                _SBFavoriteFragment.setWidgetId(mWidgetId);
                return _SBFavoriteFragment;

            } else {//임시

                if (_SBSearchFragment != null)
                    return _SBSearchFragment;

                _SBSearchFragment = new SBSearchFragment(R.layout.search, mBusDbSqlite, mLocalDBHelper);
                _SBSearchFragment.setWidgetMode(mMode);
                _SBSearchFragment.setSBSearchFragmentListener(new SBSearchFragmentListener() {

                    @Override
                    public void setChange(CommonItem item) {
                        // TODO Auto-generated method stub

                        onHidden();

                        if (item instanceof BusStopItem) {
                            mTempBusStopItem = (BusStopItem) item;


                            if (mPage == 0) {

                            } else {

                            }

                            if (mFavoriteAndHistoryItem2.busRouteItem == null) {
                                mFavoriteAndHistoryItem2.busRouteItem = new BusRouteItem();
                            }

                            mFavoriteAndHistoryItem2.busRouteItem.busStopName = mTempBusStopItem.name;
                            mFavoriteAndHistoryItem2.busRouteItem.busStopArsId = mTempBusStopItem.arsId;
                            mFavoriteAndHistoryItem2.busRouteItem.busStopApiId = mTempBusStopItem.apiId;
                            mFavoriteAndHistoryItem2.nickName = mTempBusStopItem.name;


                            if (mMode == Constants.WIDGET_MODE_STOP) {
                                mFavoriteAndHistoryItem2.busStopItem = mTempBusStopItem;
                                mStopTicket.setText(mFavoriteAndHistoryItem2.busStopItem.name);
                                mFavoriteAndHistoryItem2.nickName = mFavoriteAndHistoryItem2.busStopItem.name;
                            }


                            mTicket.setFavoriteItem(mFavoriteAndHistoryItem2);
                            mTicket.invalidate();
                            mTicket.requestLayout();
                        } else if (item instanceof BusRouteItem) {
                            mTempBusRouteItem = (BusRouteItem) item;
                            mFavoriteAndHistoryItem2.busRouteItem = mTempBusRouteItem;
                            mTicket.setFavoriteItem(mFavoriteAndHistoryItem2);
                            mTicket.invalidate();
                            mTicket.requestLayout();
                        }


                        if (mTempBusRouteItem != null && mTempBusStopItem != null) {


                            mFavoriteAndHistoryItem2.busRouteItem.busStopName = mTempBusStopItem.name;
                            mFavoriteAndHistoryItem2.busRouteItem.busStopArsId = mTempBusStopItem.arsId;
                            mFavoriteAndHistoryItem2.busRouteItem.busStopApiId = mTempBusStopItem.apiId;
                            mFavoriteAndHistoryItem2.nickName = mTempBusStopItem.name;


                            startActivityFromSearch(mFavoriteAndHistoryItem2);
                        }

                        if (mMode == Constants.WIDGET_MODE_STOP) {
                            mAllCheck.setVisibility(View.VISIBLE);
                            findViewById(R.id.selectLayout).setVisibility(View.VISIBLE);
                        }

                        mSearchPageCount = 2;

                    }
                });

                return _SBSearchFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public void onBackPressed() {


        mAllCheck.setVisibility(View.GONE);


        if (!(mSearchPageCount > 1 || mFavoritePageCount > 1)) {

        }


        if (mPage == 1) {

            mTempBusRouteItem = null;
            mTempBusStopItem = null;

            if (mSearchPageCount > 1) {
                mSearchPageCount--;
            }


        } else {

            if (mAllCheck.getVisibility() == View.VISIBLE) {
                mAllCheck.setVisibility(View.GONE);
            }


            if (mFavoritePageCount > 1) {
                mFavoritePageCount--;
            }
        }


        super.onBackPressed();
    }

    public void startActivityFromSearch(FavoriteAndHistoryItem item) {


        Intent intent = new Intent(this, SBEditFavoriteActivity.class);
        intent.putExtra(Constants.INTENT_FAVORITEITEM, item);
        intent.putExtra(Constants.WIDGET_ID, mWidgetId);


        if (mMode == Constants.WIDGET_MODE_BUS_STOP) {
            intent.putExtra(Constants.WIDGET_MODE, Constants.WIDGET_MODE_BUS_STOP);
            startActivityForResult(intent, 100);
        } else {
            intent.putExtra(Constants.WIDGET_MODE, Constants.WIDGET_MODE_STOP);
            startActivityForResult(intent, 200);
        }
    }


    boolean isMakeWidget = false;

    @Override
    protected void onActivityResult(int requestCode, int arg1, Intent arg2) {
        super.onActivityResult(requestCode, arg1, arg2);

        if (arg2 == null) return;

        if (arg2.getBooleanExtra(Constants.WIDGET_SUCCESS, false)) {

            if (requestCode == 100) {
                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
                setResult(RESULT_OK, intent);

                WidgetProvider2x1.showResult(getApplicationContext(), AppWidgetManager.getInstance(this), mWidgetId, null, 2);
                isMakeWidget = true;
                finish();


            } else if (requestCode == 200) {

                ArrayList<BusRouteItem> busRouteItemAry = new ArrayList<BusRouteItem>();


                for (int i = 0; i < mBusRouteItemAry.size(); i++) {
                    if (mBusRouteItemAry.get(i).isChecked) {
                        busRouteItemAry.add(mBusRouteItemAry.get(i));
                    }
                }


                FavoriteAndHistoryItem item = (FavoriteAndHistoryItem) arg2.getSerializableExtra(Constants.INTENT_FAVORITEITEM);

                WidgetStopItem widgetStopItem = new WidgetStopItem();
                widgetStopItem.favoriteAndHistoryItem = item;
                widgetStopItem.busRouteArray = busRouteItemAry;//mBusRouteItemAry;


                FileOutputStream fos;
                try {
                    fos = openFileOutput(String.valueOf(mWidgetId) + "_type2", Context.MODE_PRIVATE);
                    ObjectOutputStream os = new ObjectOutputStream(fos);
                    os.writeObject(widgetStopItem);
                    os.close();
                    fos.close();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                Intent intent = new Intent();
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
                setResult(RESULT_OK, intent);

                WidgetProvider4x2.showResult(getApplicationContext(), mWidgetId, 0, null);
                isMakeWidget = true;
                finish();

            }


        }


    }


    @Override
    public void onShown() {

        if ((mViewPager.getCurrentItem() == 1 && mSearchPageCount == 1)) {

            if (findViewById(R.id.widget_activity_preview_layout).getVisibility() != View.GONE) {
                try {
                    findViewById(R.id.widget_activity_preview_layout).setVisibility(View.GONE);
                    findViewById(R.id.widget_activity_tab).setVisibility(View.GONE);


                    findViewById(R.id.keyboardCheck).requestLayout();
                } catch (Exception e) {
                }
                ;
            }
        }


    }

    @Override
    public void onHidden() {
        try {
            findViewById(R.id.widget_activity_preview_layout).setVisibility(View.VISIBLE);
            findViewById(R.id.widget_activity_tab).setVisibility(View.VISIBLE);

            findViewById(R.id.keyboardCheck).requestLayout();
        } catch (Exception e) {
        }

        // TODO Auto-generated method stub

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (mLocalDBHelper != null) {
            mLocalDBHelper.close();
        }

        if (mBusDbSqlite != null) {
            mBusDbSqlite.close();
        }

        if (!isMakeWidget) {
            AppWidgetHost host = new AppWidgetHost(this, 0);
            host.deleteAppWidgetId(mWidgetId);
        }
    }


}
