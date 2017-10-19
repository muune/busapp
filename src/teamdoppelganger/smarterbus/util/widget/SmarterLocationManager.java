package teamdoppelganger.smarterbus.util.widget;

import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBDialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;

public class SmarterLocationManager {

    Context mContext;

    LocationManager mLocationManager;
    Criteria mCriteria = new Criteria();
    String mProvider;
    SmarterLocationListener mSmarterLocationListener;

    int mHandlerCount = 0;
    final int MAX_WAIT_TIME = 3;

    SBDialog mDialog;

    @SuppressLint("NewApi")
    public SmarterLocationManager(Context context, boolean isRequestNewLocation, boolean isNetworkAble) {

        mContext = context;

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (!isRequestNewLocation) return;


        if (isNetworkAble) {
            mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
            mCriteria.setPowerRequirement(Criteria.POWER_LOW);
        } else {
            mCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            mCriteria.setPowerRequirement(Criteria.POWER_HIGH);
        }


        mProvider = mLocationManager.getBestProvider(mCriteria, true);
        if (mProvider == null) {
            showDialog();
            return;
        }

        mHandler.sendEmptyMessage(0);

    }

    public interface SmarterLocationListener {
        void getPosition(Location location);

        void cancel();
    }

    public void setSmarterLocationListener(SmarterLocationListener listener) {
        mSmarterLocationListener = listener;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (mHandlerCount == 0) {
                mLocationManager.requestLocationUpdates(mProvider, 0, 0, mLocationListener);
            } else if (mHandlerCount == MAX_WAIT_TIME) {
                if (mSmarterLocationListener != null)
                    mSmarterLocationListener.getPosition(getBestLastKnownLocation());

                destroyManager();

            }

            if (mHandlerCount < MAX_WAIT_TIME)
                mHandler.sendEmptyMessageDelayed(0, 1000);

            mHandlerCount++;
        }
    };

    LocationListener mLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            mLocationManager.removeUpdates(mLocationListener);
            mProvider = mLocationManager.getBestProvider(mCriteria, true);
            mLocationManager.requestLocationUpdates(mProvider, 0, 0, mLocationListener);

        }

        @Override
        public void onProviderEnabled(String provider) {
            if (!provider.equals(mLocationManager.getBestProvider(mCriteria, true))) return;

            mLocationManager.removeUpdates(mLocationListener);
            mProvider = mLocationManager.getBestProvider(mCriteria, true);
            mLocationManager.requestLocationUpdates(mProvider, 0, 0, mLocationListener);

        }

        @Override
        public void onProviderDisabled(String provider) {

            mLocationManager.removeUpdates(mLocationListener);
            mProvider = mLocationManager.getBestProvider(mCriteria, true);
            mLocationManager.requestLocationUpdates(mProvider, 0, 0, mLocationListener);

        }

        @Override
        public void onLocationChanged(Location location) {
            if (mSmarterLocationListener != null)
                mSmarterLocationListener.getPosition(location);

            destroyManager();

        }
    };

    public Location getBestLastKnownLocation() {

        List<String> matchingProviders = mLocationManager.getAllProviders();
        float bestAccuracy = Float.MAX_VALUE;
        Location bestResult = null;

        for (String provider : matchingProviders) {
            Location location = mLocationManager.getLastKnownLocation(provider);

            if (location != null) {
                float accuracy = location.getAccuracy();
                if (accuracy > bestAccuracy) continue;

                bestResult = location;
                bestAccuracy = accuracy;

            }
        }

        return bestResult;

    }

    @SuppressLint("NewApi")
    void showDialog() {

        mDialog = new SBDialog(mContext);
        mDialog.setTitleLayout(mContext.getString(R.string.dialog_title_search_position));
        mDialog.setViewLayout(mContext.getString(R.string.dialog_msg_location_permission));
        mDialog.getPositiveButton(mContext.getString(R.string.dialog_positive)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
                mDialog.dismiss();
            }
        });
        mDialog.getNegativeButton(mContext.getString(R.string.dialog_negative)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSmarterLocationListener != null) mSmarterLocationListener.cancel();
                mDialog.dismiss();
            }
        });
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mSmarterLocationListener != null) mSmarterLocationListener.cancel();
                mDialog.dismiss();
            }
        });
        mDialog.show();

    }

    public void destroyManager() {
        if (mLocationListener != null) mLocationManager.removeUpdates(mLocationListener);
        if (mHandler != null) mHandler.removeMessages(0);
    }


}
