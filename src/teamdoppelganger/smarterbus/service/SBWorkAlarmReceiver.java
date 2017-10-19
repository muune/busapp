package teamdoppelganger.smarterbus.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import teamdoppelganger.smarterbus.SBMainNewActivity;
import teamdoppelganger.smarterbus.SBWorkAlarmActivity;

/**
 * Created by muune on 2017-10-10.
 */

public class SBWorkAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context $context, Intent $intent ){
        int pid = $intent.getIntExtra("pid", 0);
        String type = $intent.getStringExtra("type");
        Log.i("bs", "SBWorkAlarmReceiver pid:"+pid);
        Log.i("bs", "SBWorkAlarmReceiver type:"+type);
        if(pid == 0 || (!type.equals("출근") && !type.equals("퇴근")) ){ //오류임
            Log.i("bs", "SBWorkAlarmReceiver onReceive ERROR!!");
        }else {
            Intent intent = new Intent( $context, SBWorkAlarmActivity.class );
            intent.putExtra("pid", pid);
            intent.putExtra("type", type);
            //intent.putExtra("intentType", "WORKONOFF_ALARM");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS|Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.
            PendingIntent pi = PendingIntent.getActivity($context, pid, intent, PendingIntent.FLAG_ONE_SHOT);
            try {
                pi.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }

    }
}
