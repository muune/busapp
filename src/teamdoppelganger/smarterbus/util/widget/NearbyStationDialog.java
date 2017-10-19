package teamdoppelganger.smarterbus.util.widget;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBDialog;
import teamdoppelganger.smarterbus.util.widget.SmarterLocationManager.SmarterLocationListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class NearbyStationDialog {

    Context mContext;

    ProgressBar mNearDialogProgress;

    LocationCatchListener mLocationCatchListener;
    SmarterLocationManager mLocationManager;

    SBDialog mDialog;

    boolean mIsConntected;

    public NearbyStationDialog(Context context, boolean isConnected) {

        mContext = context;
        mIsConntected = isConnected;

        mLocationManager = new SmarterLocationManager(mContext, true, mIsConntected);
        mLocationManager.setSmarterLocationListener(new SmarterLocationListener() {
            @Override
            public void getPosition(Location location) {
                if (location == null) {
                    Toast.makeText(mContext, mContext.getString(R.string.dialog_msg_location_fail), Toast.LENGTH_SHORT).show();
                }
                mLocationCatchListener.onReceive(location);
                destroy();
            }

            @Override
            public void cancel() {
                destroy();
            }
        });
    }

    private void makeDialog() {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View nearbyDialogLayout = (RelativeLayout) inflater.inflate(R.layout.main_nearby_dialog, null);

        mNearDialogProgress = (ProgressBar) nearbyDialogLayout.findViewById(R.id.main_nearby_dialog_progress);
        mNearDialogProgress.setVisibility(View.VISIBLE);

        mDialog = new SBDialog(mContext);
        mDialog.setTitleLayout(mContext.getResources().getString(R.string.dialog_title_search_position));
        mDialog.setViewLayout(nearbyDialogLayout.findViewById(R.id.main_nearby_dialog_layout));
        mDialog.setCancelable(false);
        mDialog.getPositiveButton(mContext.getResources().getString(R.string.dialog_negative)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destroy();
            }
        });
        mDialog.show();
    }

    public void destroy() {
        mLocationManager.destroyManager();
        if (mDialog != null) mDialog.dismiss();
    }

    public void setLocationCatchListener(LocationCatchListener listener) {
        mLocationCatchListener = listener;
    }

    public interface LocationCatchListener {
        void onReceive(Location location);
    }

}
