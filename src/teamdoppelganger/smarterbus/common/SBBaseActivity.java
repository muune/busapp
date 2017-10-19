package teamdoppelganger.smarterbus.common;

import java.io.File;

import com.admixer.AdAdapter;
import com.admixer.AdInfo;
import com.admixer.AdMixerManager;
import com.admixer.AdView;
import com.admixer.AdViewListener;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class SBBaseActivity extends Activity {

    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;

    public String mClassName;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mClassName = getClass().getSimpleName();

        try {
            String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
            if (databaseExist(Constants.LOCAL_PATH + dbName)) {
                mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);
            }

            mLocalDBHelper = new LocalDBHelper(this);
        } catch (Exception e) {
        }

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


    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mBusDbSqlite != null &&
                mBusDbSqlite.isOpen()) {
            mBusDbSqlite.close();
        }


        if (mLocalDBHelper != null) {
            mLocalDBHelper.close();
        }

    }

    @Override
    public void onResume() {

        super.onResume();

    }


}
