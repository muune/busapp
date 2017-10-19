package teamdoppelganger.smarterbus.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.preference.PreferenceManager;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.SBAlarmDialogActivity;
import teamdoppelganger.smarterbus.common.Constants;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class NotiSetting {

    NotificationManager mNotiManager;
    public static final int NOTI_ID = 1;
    public static boolean IS_NOTI_DEL = false;
    public static boolean IS_FINAL_ALARM = false;

    private Context mContext;
    private String mBusName = "";
    private String mLeftMin = "";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @SuppressLint("NewApi")
    public NotiSetting(Context context) {

        mContext = context;

        mNotiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public void call(String tickerText, String title, String msg, boolean isAlarm, String busId, String busColor, int alarmTime) {

        if (title.equals(mBusName) && mLeftMin.equals(msg))
            return;
        mNotiManager.cancel(NOTI_ID);

        mBusName = title;
        mLeftMin = msg;

        Notification noti;

        int userSoundMode = -1;

        if (isAlarm) {

            AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            userSoundMode = audio.getRingerMode();
        }

        Intent intent = new Intent(mContext, SBAlarmDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("title", title);
        intent.putExtra("msg", msg);
        intent.putExtra("busId", busId);
        intent.putExtra("userSoundMode", userSoundMode);
        intent.putExtra(Constants.ALARM_BUS_COLOR, busColor);
        intent.putExtra("alarmTime", alarmTime);

        PendingIntent content = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (isAlarm) {
            IS_FINAL_ALARM = true;
            try {
                content.send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT >= 11) {
            noti = new Notification.Builder(mContext)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setTicker(title)
                    .setSmallIcon(R.drawable.main_icon)
                    .setContentIntent(content)
                    .build();
        } else {
            noti = new Notification(R.drawable.main_icon, tickerText, System.currentTimeMillis());
            noti.setLatestEventInfo(mContext, title, msg, content);
        }

        if (isAlarm) {

            AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            userSoundMode = audio.getRingerMode();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
            boolean isSoundOn = pref.getBoolean(Constants.SETTING_ALARM, userSoundMode == AudioManager.RINGER_MODE_NORMAL);

            if (isSoundOn) {
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                noti.defaults |= Notification.DEFAULT_SOUND;
            } else {
                audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                noti.defaults |= Notification.DEFAULT_VIBRATE;
            }

            noti.flags |= Notification.FLAG_INSISTENT;

        }


        mNotiManager.notify(NOTI_ID, noti);

    }
}