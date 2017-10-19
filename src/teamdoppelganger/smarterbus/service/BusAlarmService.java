package teamdoppelganger.smarterbus.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import com.smart.lib.CommonConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.AlarmItem;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.DepthAlarmItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;
import teamdoppelganger.smarterbus.util.common.GetData.GetDataListener;

public class BusAlarmService extends Service implements GetDataListener {

    GetData mGetData;
    SQLiteDatabase mDatabase;
    public HashMap<Integer, String> mHashLocation;

    BusRouteItem mBusRouteItem;

    int mMin;

    int mCurrentMin = -9999;
    int mCurrentStop = -9999;
    boolean mIsArrive = false;
    boolean mIsPrepare = false;

    NotiSetting mNotiSetting;

    int mTargetOrder;
    String mTargetBusId;
    int mTargetRemainMin;
    int mTargetRemainStop;

    String mBusColor;
    SharedPreferences mPref;

    int mMint;
    int mTarget;

    boolean mIsFirst = true;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int what = msg.what;

            switch (what) {

                case 0:

                    if (mBusRouteItem == null) {
                        init();
                    }

                    mGetData.startAlarmService(mBusRouteItem);
                    mHandler.sendEmptyMessageDelayed(0, 1000 * 10);
                    break;
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
        mDatabase = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);

        mHashLocation = new HashMap<Integer, String>();

        setCityInfor(mDatabase);

        mGetData = new GetData(this, mDatabase, mHashLocation);

        mNotiSetting = new NotiSetting(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mIsFirst = true;

        mHandler.removeMessages(0);

        NotiSetting.IS_NOTI_DEL = false;

        if (intent != null) {

            mBusRouteItem = (BusRouteItem) intent.getExtras().getSerializable(Constants.INTENT_BUSROUTEITEM);

            mTarget = intent.getExtras().getInt(Constants.ALARM_BUS_ORDER);
            mMin = intent.getExtras().getInt(Constants.ALARM_BUS_MIN);
            mBusColor = intent.getExtras().getString(Constants.ALARM_BUS_COLOR);

            AlarmItem alarmItem = new AlarmItem();
            alarmItem.busRouteItem = mBusRouteItem;
            alarmItem.target = mTarget;
            alarmItem.min = mMin;
            alarmItem.BusColor = mBusColor;

            saveAlarmFile(alarmItem);

            if (mMin == 0) {
                mMin = 1;
            } else if (mMin == 1) {
                mMin = 3;
            } else if (mMin == 2) {
                mMin = 5;
            } else if (mMin == 3) {
                mMin = 10;
            }

            mTargetOrder = mTarget;


            ArriveItem arriveItem = mBusRouteItem.arriveInfo.get(mTarget);

            mTargetRemainMin = arriveItem.remainMin;
            mTargetRemainStop = arriveItem.remainStop;

            mTargetBusId = arriveItem.plainNum;

        } else {

            init();

            if (mMin == 0) {
                mMin = 1;
            } else if (mMin == 1) {
                mMin = 3;
            } else if (mMin == 2) {
                mMin = 5;
            } else if (mMin == 3) {
                mMin = 10;
            }

            mTargetOrder = mTarget;

            ArriveItem arriveItem = mBusRouteItem.arriveInfo.get(mTarget);
            mTargetRemainMin = arriveItem.remainMin;
            mTargetRemainStop = arriveItem.remainStop;
            mTargetBusId = arriveItem.plainNum;

        }

        mHandler.sendEmptyMessage(0);

        return super.onStartCommand(intent, flags, startId);
    }

    public void init() {

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
        mDatabase = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);
        mHashLocation = new HashMap<Integer, String>();
        setCityInfor(mDatabase);
        mGetData = new GetData(this, mDatabase, mHashLocation);
        mNotiSetting = new NotiSetting(this);

        AlarmItem alarmItem = readAlarmFile();
        mBusRouteItem = alarmItem.busRouteItem;
        mBusColor = alarmItem.BusColor;
        mTarget = alarmItem.target;
        mMin = alarmItem.min;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //파일 삭제 필요
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void findCurrentBusByBusId(DepthItem item) {

        DepthAlarmItem alarmItems = (DepthAlarmItem) item;
        ArrayList<ArriveItem> alarmItemList = alarmItems.busAlarmItem;

        for (int i = 0; i < alarmItemList.size(); i++) {
            ArriveItem arrItem = alarmItemList.get(i);

            if (arrItem.plainNum != null) {
                if (arrItem.plainNum.equals(mTargetBusId)) {

                    mCurrentMin = arrItem.remainMin;
                    mCurrentStop = arrItem.remainStop;


                    if (arrItem.state == Constants.STATE_PREPARE) {

                        mIsPrepare = true;

                    } else {

                        if (mMin >= arrItem.remainMin) {
                            mIsArrive = true;

                        }
                    }

                }
            }
        }
    }

    public void findCurrentBusByLeftTime(DepthItem item, boolean isByMin) {

        DepthAlarmItem alarmItems = (DepthAlarmItem) item;
        ArrayList<ArriveItem> alarmItemList = alarmItems.busAlarmItem;


        if (alarmItemList.size() < mTargetOrder + 1) {
            if (mTargetOrder > 0) {
                mTargetOrder--;
                findCurrentBusByLeftTime(item, isByMin);
                return;
            } else {
                mCurrentMin = 0;
                mCurrentStop = 0;
                mIsArrive = true;
            }
        }

        if (alarmItemList.size() > 0) {
            ArriveItem arrItem = alarmItemList.get(mTargetOrder);

            if (mIsFirst) {
                mTargetRemainMin = arrItem.remainMin;
                mTargetRemainStop = arrItem.remainStop;
                mIsFirst = false;
            }

            int remainMinOrStop = isByMin ? arrItem.remainMin : arrItem.remainStop;
            int targetRemain = isByMin ? mTargetRemainMin : mTargetRemainStop;


            if (remainMinOrStop > 0 && remainMinOrStop <= targetRemain + 1) {

                mCurrentMin = arrItem.remainMin;
                mCurrentStop = arrItem.remainStop;

                if (isByMin)
                    mTargetRemainMin = mCurrentMin;
                else
                    mTargetRemainStop = mCurrentStop;

                if (arrItem.state == Constants.STATE_PREPARE) {

                    mIsPrepare = true;

                } else {
                    if (mMin >= arrItem.remainMin)
                        mIsArrive = true;
                }

            } else {
                if (mTargetOrder > 0) {
                    mTargetOrder--;
                    findCurrentBusByLeftTime(item, isByMin);
                } else {
                    mCurrentMin = 0;
                    mCurrentStop = 0;
                    mIsArrive = true;
                }
            }
        } else {
            mCurrentMin = 0;
            mCurrentStop = 0;
            mIsArrive = true;
        }
    }

    public void findCurrentBusByRemainStop(DepthItem item) {

        DepthAlarmItem alarmItems = (DepthAlarmItem) item;
        ArrayList<ArriveItem> alarmItemList = alarmItems.busAlarmItem;

        if (alarmItemList.size() < mTargetOrder + 1) {
            if (mTargetOrder > 0)
                mTargetOrder--;
            else {
                mCurrentMin = 0;
                mCurrentStop = 0;
                mIsArrive = true;
            }
        }

        ArriveItem arrItem = alarmItemList.get(mTargetOrder);

        if (arrItem.remainStop > 0 && arrItem.remainStop <= mTargetRemainStop) {

            mCurrentMin = arrItem.remainMin;
            mCurrentStop = arrItem.remainStop;
            mTargetRemainMin = mCurrentMin;

            if (arrItem.state == Constants.STATE_PREPARE) {

                mIsPrepare = true;

            } else {
                if (mMin >= arrItem.remainMin)
                    mIsArrive = true;
            }

        } else {
            if (mTargetOrder > 0) {
                mTargetOrder--;
                findCurrentBusByRemainStop(item);
            } else {
                mCurrentMin = 0;
                mCurrentStop = 0;
                mIsArrive = true;
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if (mBusRouteItem == null) {

        } else {

        }

        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mBusRouteItem == null) {

        } else {

        }
    }


    @Override
    public void onCompleted(int type, DepthItem item) {

        if (NotiSetting.IS_NOTI_DEL) {
            mHandler.removeMessages(0);
            onDestroy();
            return;
        }

        DepthAlarmItem alarmItems = (DepthAlarmItem) item;


        if (alarmItems.state == 0)
            return;

        if (mTargetBusId != null)
            findCurrentBusByBusId(item);
        else
            findCurrentBusByLeftTime(item, mTargetRemainMin != 0);

        String title = mBusRouteItem.busRouteName + " " + mBusRouteItem.busStopName;
        String msg = mCurrentMin + "분 후 도착 예정";//"(" + mCurrentStop + " 정거장 전)";
        if (mCurrentStop != -1)
            msg += "(" + mCurrentStop + " 정거장 전)";
        String tickerMsg = title + "\n" + msg;


        //알람 실시
        if (mIsArrive) {
            tickerMsg = mCurrentMin + " 분 후 버스가 도착합니다";
            mHandler.removeMessages(0);
            stopSelf();
        }

        mNotiSetting.call(tickerMsg, title, msg, mIsArrive, mTargetBusId, mBusColor, mMin);


    }

    public void setCityInfor(SQLiteDatabase db) {

        String tmpSql = String.format("SELECT * FROM %s", CommonConstants.TBL_CITY);
        Cursor cursor = db.rawQuery(tmpSql, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
            String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
            mHashLocation.put(id, cityEnName);
        }
        cursor.close();
    }


    private void saveAlarmFile(AlarmItem item) {
        FileOutputStream fos;
        String tmpFileName = String.valueOf("alarm");

        try {

            fos = openFileOutput(tmpFileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(item);
            os.close();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ;
    }

    private AlarmItem readAlarmFile() {
        FileInputStream fis;
        AlarmItem item = null;

        try {
            fis = openFileInput("alarm");
            ObjectInputStream os = new ObjectInputStream(fis);
            item = (AlarmItem) os.readObject();
            os.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return item;
    }

}
