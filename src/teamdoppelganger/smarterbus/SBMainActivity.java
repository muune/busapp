package teamdoppelganger.smarterbus;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.admixer.AdAdapter;
import com.admixer.AdInfo;
import com.admixer.AdMixerManager;
import com.admixer.AdView;
import com.admixer.AdViewListener;
import com.mocoplex.adlib.AdlibConfig;
import com.mocoplex.adlib.AdlibManager;

import java.util.HashMap;
import java.util.Map;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBBaseFragment.LBSAgreedListener;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.common.SBInforApplication.TrackerName;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.item.NotificationItem;
import teamdoppelganger.smarterbus.item.RecentItem;
import teamdoppelganger.smarterbus.lib.viewpagerindicator.IconPagerAdapter;
import teamdoppelganger.smarterbus.lib.viewpagerindicator.TabPageIndicator;
import teamdoppelganger.smarterbus.util.common.CommonNotiLib;
import teamdoppelganger.smarterbus.util.common.CommonNotiLib.GetDataListener;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.ImageStorage;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

/**
 * fragment들의 교환이 일어나는 클래스
 *
 * @author DOPPELSOFT4
 */
@SuppressLint("NewApi")
public class SBMainActivity extends SherlockFragmentActivity implements GetDataListener {
    private static final int[] CONTENT = new int[]{R.string.menu1, R.string.menu2, R.string.menu3, R.string.menu4, R.string.menu5};
    private static final int[] ICONS = new int[]{
            R.drawable.ic_1,
            R.drawable.ic_2,
            R.drawable.ic_3,
            R.drawable.ic_4,
            R.drawable.ic_5,
    };

    SBAdapter mAdapter;
    TabPageIndicator mIndicator;

    int mPostion = -1;
    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;
    private LocalInforItem mLocalSaveInfor;


    SharedPreferences mPref;
    public ViewPager mPager;


    boolean mContinuousBackButtonPressed;

    Handler mMainHandler;
    Runnable mClearBackButtonStatus;

    CommonNotiLib mCommonNotiLib;

    AdlibManager mAdlibManager;

    public String mClassName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smarterbus_main);

        mClassName = getClass().getSimpleName();

        mCommonNotiLib = new CommonNotiLib();
        mCommonNotiLib.setDataListener(this);


        mMainHandler = new Handler();
        mClearBackButtonStatus = new Runnable() {
            public void run() {
                mContinuousBackButtonPressed = false;
            }
        };

        Tracker t = ((SBInforApplication) getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_main));
        t.send(new HitBuilders.AppViewBuilder().build());


        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);


        //action bar
        if (dbName == null) {

        } else {

        }

        mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);

        mLocalDBHelper = new LocalDBHelper(getApplicationContext());
        mLocalSaveInfor = ((SBInforApplication) getApplicationContext()).mLocalSaveInfor;


        ((SBInforApplication) getApplicationContext()).checkSetting(mLocalDBHelper, mBusDbSqlite, SBMainActivity.this);
        ((SBInforApplication) getApplicationContext()).setTerminus(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setCityInfor(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setCityKoInfor(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setBusType(mBusDbSqlite);


        initAds() ;


        mAdapter = new SBAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.requestTransparentRegion(mPager);


        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub

            }
        });


        mIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mIndicator.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                invalidateOptionsMenu();

                if (mPostion != -1) {
                    ((SBBaseFragment) mAdapter.getItemFragment(mPostion)).unSelectedPage();
                    ((SBBaseFragment) mAdapter.getItemFragment(mPostion)).pageChange();

                }

                ((SBBaseFragment) mAdapter.getItemFragment(position)).selectedPage();
                mPostion = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {


            }

            @Override
            public void onPageScrollStateChanged(int arg0) {


            }
        });


        mIndicator.setViewPager(mPager);
        mPostion = 0;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    protected void onActivityResult(int requestCode, int arg1, Intent arg2) {
        super.onActivityResult(requestCode, arg1, arg2);
        if (requestCode == Constants.INTENT_RESULT_CODE_FROM_AUTO_1
                || requestCode == Constants.INTENT_RESULT_CODE_FROM_AUTO_2) {

            ((SBBaseFragment) mAdapter.getItemFragment(mPostion)).activityResult(requestCode, arg1, arg2);

        }
    }

    /**
     * fragmentAdapter에 문제가 있어서 FragementStatePageAdapter로 변경
     * 변경 후 saveState return 값을 null처리 후 해결
     * <p>
     * 이슈 니용: FragmentManagerImpl.saveFragmentBasicState exception이 발생 하였었음
     *
     * @author DOPPELSOFT4
     */

    class SBAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {

        private Map<Integer, Fragment> mFragmetItemList = new HashMap<Integer, Fragment>();

        public SBAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragmetItemList.get(position) == null) {
                SBBaseFragment sbBaseFragment = (SBBaseFragment) SBControlFragment.newInstance(position, mBusDbSqlite, mLocalDBHelper);

                if (position == 3) {
                    sbBaseFragment.setLBSAgreedListner(new LBSAgreedListener() {
                        @Override
                        public void cancel() {
                            mIndicator.setCurrentItem(2);
                        }
                    });
                }

                mFragmetItemList.put(position, sbBaseFragment);
            }


            return mFragmetItemList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {


            return getResources().getString(CONTENT[position]);
        }

        @Override
        public int getIconResId(int index) {
            return ICONS[index];
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // TODO Auto-generated method stub

        }

        @Override
        public Parcelable saveState() {
            // TODO Auto-generated method stub
            return null;
        }


        public Fragment getItemFragment(int position) {
            return mFragmetItemList.get(position);
        }
    }


    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().add(111, fragment).commit();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdlibManager.onDestroy(this);

        if (mBusDbSqlite != null &&
                mBusDbSqlite.isOpen()) {
            mBusDbSqlite.close();
        }

        if (mLocalDBHelper != null) {
            mLocalDBHelper.close();
        }

        if (mMainHandler != null && mClearBackButtonStatus != null) {
            mMainHandler.removeCallbacks(mClearBackButtonStatus);
        }


    }


    @Override
    protected void onResume() {
        super.onResume();

        mAdlibManager.onResume(this);

        try {
            if (mPostion != -1 && mAdapter != null) {
                ((SBBaseFragment) mAdapter.getItemFragment(mPostion)).selectedPage();
            }
        } catch (Exception e) {
        }

    }

    @Override
    protected void onPause() {

        mAdlibManager.onPause(this);

        try {

            if (mPostion != -1 && mAdapter != null) {
                ((SBBaseFragment) mAdapter.getItemFragment(mPostion)).unSelectedPage();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();

    }

    @Override
    public void onBackPressed() {

        if (!mContinuousBackButtonPressed) { // 거짓일 경우 : 연속된 클릭이 아닐 경우

            mContinuousBackButtonPressed = true;
            Toast.makeText(getApplicationContext(), "종료를 원하시면 뒤로 버튼을 한 번 더 눌러주세요.", Toast.LENGTH_SHORT).show();

            mMainHandler.removeCallbacks(mClearBackButtonStatus);
            mMainHandler.postDelayed(mClearBackButtonStatus, 2500);

        } else {

            super.onBackPressed();

        }

    }

    private void initAds() {

        AdlibConfig.getInstance().bindPlatform("ADAM", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewAdam");
        AdlibConfig.getInstance().bindPlatform("ADMOB", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewAdmob");
        AdlibConfig.getInstance().bindPlatform("CAULY", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewCauly");
        AdlibConfig.getInstance().bindPlatform("ADMIXER", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewAdmixer");
        AdlibConfig.getInstance().bindPlatform("MEZZO", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewMezzo");
        AdlibConfig.getInstance().bindPlatform("FACEBOOK", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewFacebook");

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            AdlibConfig.getInstance().bindPlatform("INMOBI", "teamdoppelganger.smarterbus.ads.SubAdlibAdViewInmobi");
        }

        mAdlibManager = new AdlibManager(Constants.ADLIB_MAIN_API_KEY);
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

        this.setAdsContainer(R.id.adlib);

    }

    private void setAdsContainer(int rid) {
        mAdlibManager.setAdsContainer(rid);
    }


    @Override
    public void onReceiveRecent(RecentItem item) {

        if (mPref != null) {

            int blockVersion = mPref.getInt(Constants.PREF_RECENTVERSION, 0);

            if (blockVersion < item.id) {
                if (mCommonNotiLib != null) {

                }

            }

        }
    }

    @Override
    public void onReceiveNoti(final NotificationItem item) {
        // TODO Auto-generated method stub


        if (item.buttonType == 2) {

            new AlertDialog.Builder(this).setTitle(item.title)
                    .setMessage(String.valueOf(Html.fromHtml(item.contents))).setPositiveButton("확인", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            }).setNegativeButton("다시보지않기", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                    if (mPref != null) {
                        mPref.edit().putInt(Constants.PREF_RECENTVERSION, item.id).commit();
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


            new AlertDialog.Builder(this).setTitle(item.title)
                    .setMessage(String.valueOf(Html.fromHtml(item.contents))).setPositiveButton("확인", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            }).setNegativeButton("다시보지않기", new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    if (mPref != null) {
                        mPref.edit().putInt(Constants.PREF_RECENTVERSION, item.id).commit();
                    }

                }
            }).setNeutralButton(buttonName, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                    if (item.buttonName == null || (item.buttonName != null && item.buttonName.trim().length() == 0)) {

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        Uri u = Uri.parse("market://details?id=teamdoppelganger.smarterbus");
                        i.setData(u);
                        startActivity(i);

                    } else {

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        Uri u = Uri.parse(item.link);
                        i.setData(u);
                        startActivity(i);

                    }


                }
            }).show();


        }
    }
}
