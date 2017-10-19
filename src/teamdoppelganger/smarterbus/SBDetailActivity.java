package teamdoppelganger.smarterbus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mocoplex.adlib.AdlibManager;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.common.Debug;

public class SBDetailActivity extends SherlockFragmentActivity {

    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;
    private LocalInforItem mLocalSaveInfor;

    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mHashLocationKo;
    HashMap<Integer, String> mTerminus;

    ImageView mCancelBtn;
    Button mBackBtn;
    OnBackStackChangedListener mBackStackChangeListener;
    SharedPreferences mPref;

    SBBaseFragment mCurSBBaseFragment = null;

    SBRouteSearchDetailFragment mRouteDetail = null;
    SBStopSearchDetailFragment mStopSearchDetail = null;

    public String mClassName;

    AdlibManager mAdlibManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smarterbus_detail);

        mClassName = getClass().getSimpleName();


        if (mBackStackChangeListener == null) {
            getListener();
            getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangeListener);
        }

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);

        mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);
        mLocalDBHelper = new LocalDBHelper(getApplicationContext());

        ((SBInforApplication) getApplicationContext()).setTerminus(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setCityInfor(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setCityKoInfor(mBusDbSqlite);
        ((SBInforApplication) getApplicationContext()).setBusType(mBusDbSqlite);

        mLocalSaveInfor = ((SBInforApplication) getApplicationContext()).mLocalSaveInfor;
        mHashLocationEng = ((SBInforApplication) getApplicationContext()).mHashLocation;
        mHashLocationKo = ((SBInforApplication) getApplicationContext()).mHashKoLocation;
        mTerminus = ((SBInforApplication) getApplicationContext()).mTerminus;

        String getType = getIntent().getExtras().getString(Constants.INTENT_SEND_TYPE);
        String fileName = null;

        try {
            fileName = getIntent().getExtras().getString(Constants.INTENT_FILENAME);
        } catch (Exception e) {
        }

        if (getType == null) {
            getType = Constants.INTENT_BUSSTOPITEM;
        }

        if (getType.equals(Constants.INTENT_BUSROUTEITEM)) {

            BusRouteItem busRouteItem = null;
            if (fileName != null) {
                FileInputStream fis;
                try {
                    fis = getApplicationContext().openFileInput(fileName);
                    ObjectInputStream is = new ObjectInputStream(fis);
                    busRouteItem = (BusRouteItem) is.readObject();
                    is.close();
                    fis.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                busRouteItem = (BusRouteItem) getIntent().getExtras().getSerializable("busInfor");
            }



            mRouteDetail = new SBRouteSearchDetailFragment(mBusDbSqlite, mLocalDBHelper);
            mRouteDetail.setBusRouteItem(busRouteItem, false, mHashLocationEng, mTerminus, mHashLocationKo);

            mCurSBBaseFragment = mRouteDetail;

            getSupportFragmentManager().beginTransaction().replace(R.id.coverLayout, mRouteDetail).commit();

        } else {

            BusStopItem busStopItem = null;
            if (fileName != null) {
                FileInputStream fis;
                try {
                    fis = getApplicationContext().openFileInput(fileName);
                    ObjectInputStream is = new ObjectInputStream(fis);
                    busStopItem = (BusStopItem) is.readObject();
                    is.close();
                    fis.close();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                busStopItem = (BusStopItem) getIntent().getExtras().getSerializable("busInfor");
            }


            mStopSearchDetail = new SBStopSearchDetailFragment(mBusDbSqlite, mLocalDBHelper);
            mStopSearchDetail.setBusStopItem(busStopItem, false, mHashLocationEng, mTerminus, mHashLocationKo);

            mCurSBBaseFragment = mStopSearchDetail;

            getSupportFragmentManager().beginTransaction().replace(R.id.coverLayout, mStopSearchDetail).commit();
        }


        mCancelBtn = (ImageView) findViewById(R.id.cancelBtn);
        mBackBtn = (Button) findViewById(R.id.backBtn);

        mBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                runMainActivity();
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

    private void getListener() {
        mBackStackChangeListener = new OnBackStackChangedListener() {
            public void onBackStackChanged() {

                FragmentManager manager = getSupportFragmentManager();

                if (manager != null) {
                    SBBaseFragment currFrag = (SBBaseFragment) manager.
                            findFragmentById(R.id.coverLayout);

                    currFrag.refreshFragment();

                    if (mCurSBBaseFragment != null) {
                        mCurSBBaseFragment.onRefreshStop();
                    }

                    mCurSBBaseFragment = currFrag;

                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAdlibManager != null)
            mAdlibManager.onDestroy(this);

        if (mBusDbSqlite != null) {
            if (mBusDbSqlite.isOpen()) {
                mBusDbSqlite.close();
            }
        }

        if (mLocalDBHelper != null) {
            if (mLocalDBHelper != null) {
                mLocalDBHelper.close();
            }
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        runMainActivity();
    }


    public void runMainActivity() {
        //shortcut 으로 바로 넘어올 시
        if (getIntent().getExtras().getString(Constants.INTENT_FILENAME) != null) {

            Intent intent = new Intent(getApplicationContext(), SBMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {

        super.onActivityResult(requestCode, resultCode, arg2);

        mCurSBBaseFragment.activityResult(requestCode, resultCode, arg2);


    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mAdlibManager != null)
            mAdlibManager.onPause(this);

        if (mCurSBBaseFragment != null) {
            mCurSBBaseFragment.onRefreshStop();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAdlibManager != null)
            mAdlibManager.onResume(this);

        if (mCurSBBaseFragment != null) {
            mCurSBBaseFragment.refreshFragment();
        }
    }



}
