package teamdoppelganger.smarterbus.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBMainActivity;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

public class SBBaseFragment extends Fragment {

    int mLayoutId;
    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;

    Handler mHandler;
    ArrayList<Runnable> mRunnableList;

    private View mView;

    Vibrator mVibe;

    SharedPreferences mPref;

    public LBSAgreedListener mLBSAgreedListner;

    public interface LBSAgreedListener {
        public void cancel();
    }

    public void setLBSAgreedListner(LBSAgreedListener listener) {
        mLBSAgreedListner = listener;
    }

    public SBBaseFragment() {

    }

    @SuppressLint("ValidFragment")
    public SBBaseFragment(int id, SQLiteDatabase db, LocalDBHelper localDBHelper) {

        mLayoutId = id;
        mBusDbSqlite = db;
        mLocalDBHelper = localDBHelper;
        mHandler = new Handler();
        mRunnableList = new ArrayList<Runnable>();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mVibe = (Vibrator) getActivity().getSystemService(
                Context.VIBRATOR_SERVICE);

        View view;
        LinearLayout layout = null;

        try {
            view = inflater.inflate(mLayoutId, null);

            layout = new LinearLayout(getActivity());
            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            layout.setGravity(Gravity.LEFT | Gravity.TOP);
            layout.addView(view);
            onLayoutFinish(view);

        } catch (NullPointerException e) {
            Intent intent = new Intent(getActivity(), SBMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        } catch (Resources.NotFoundException e) {
            Intent intent = new Intent(getActivity(), SBMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void onLayoutFinish(View view) {

    }

    public void selectedPage() {

    }

    public void unSelectedPage() {

    }

    public void pageChange() {

    }

    public void setActionbarTitle(String title) {
    }

    public SQLiteDatabase getBusDbSqlite() {
        return mBusDbSqlite;
    }

    public LocalDBHelper getLocalDBHelper() {
        return mLocalDBHelper;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void addRunnable(Runnable runnable) {
        mRunnableList.add(runnable);
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();

            for (Runnable runnable : mRunnableList)
                mHandler.removeCallbacks(runnable);
        } catch (Exception e) {
        }
        ;

    }

    public void refreshFragment() {

    }

    public void activityResult(int requestCode, int resultCode, Intent data) {
    }

    public void setProgress(int visible) {
        try {
            getActivity().findViewById(R.id.mainProgress)
                    .setVisibility(visible);
        } catch (Exception e) {
        }
    }

    public void playVibe() {

        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null
                && !mPref.getBoolean(Constants.SETTING_VIBRATE,
                Constants.SETTING_VIBRATE_ON)) {
            return;
        }

        if (mVibe != null) {
            mVibe.vibrate(20);
        }
    }

    public void onRefreshStop() {

    }

    public void saveSearchMode(int mode) {
        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            mPref.edit().putInt(Constants.SETTING_SEARCHMODE, mode).commit();

        }

    }

    public int getSearchMode() {

        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            return mPref.getInt(Constants.SETTING_SEARCHMODE,
                    Constants.SETTING_SEARCHMODE_STOP);
        }

        return 0;
    }

    public boolean isOnline(Context context) { // network 연결 상태 확인
        try {
            ConnectivityManager conMan = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            State wifi = conMan.getNetworkInfo(1).getState(); // wifi
            if (wifi == NetworkInfo.State.CONNECTED
                    || wifi == NetworkInfo.State.CONNECTING) {
                return true;
            }

            State mobile = conMan.getNetworkInfo(0).getState(); // mobile
            if (mobile == NetworkInfo.State.CONNECTED
                    || mobile == NetworkInfo.State.CONNECTING) {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }


    public void setRecetBusStop(int x, int y) {
        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            mPref.edit().putInt(Constants.SETTING_LOCATION_X, x).putInt(Constants.SETTING_LOCATION_Y, y).commit();
        }
    }

    public int getRecentStopX() {

        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            return mPref.getInt(Constants.SETTING_LOCATION_X,
                    0);
        }

        return 0;
    }

    public int getRecentStopY() {

        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            return mPref.getInt(Constants.SETTING_LOCATION_Y,
                    0);
        }

        return 0;
    }


    public void setFavoriteMode(int mode) {
        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            mPref.edit().putInt(Constants.SETTING_FAVORITE_MODE, mode).commit();
        }
    }

    public int getFavoriteListMode() {

        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            return mPref.getInt(Constants.SETTING_FAVORITE_MODE,
                    Constants.SETTING_FAOVIRET_TICKET);
        }

        return Constants.SETTING_FAOVIRET_TICKET;
    }


    public void setPrefTypeMode(String type, int mode) {
        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {
            mPref.edit().putInt(type, mode).commit();
        }
    }


    public int getPrefTypeMode(String type) {

        int returnResult = 0;

        if (mPref == null) {
            try {
                mPref = PreferenceManager
                        .getDefaultSharedPreferences(getActivity());
            } catch (Exception e) {
            }
        }

        if (mPref != null) {

            if (type == Constants.SETTING_FAOVIRET_LIST_TYPE) {
                returnResult = mPref.getInt(type,
                        Constants.SETTING_FAOVIRET_LIST_1);
            } else if (type == Constants.SETTING_FAOVIRET_TICKET_TYPE) {
                returnResult = mPref.getInt(type,
                        Constants.SETTING_FAOVIRET_TICKET_1);
            }
        }

        return returnResult;
    }

}
