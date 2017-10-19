package teamdoppelganger.smarterbus;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.service.NotiSetting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SBAlarmDialogActivity extends Activity {

    AlertDialog mDialog;
    int mUserSoundMode;
    String mBusColor;
    int mAlarmTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (NotiSetting.IS_NOTI_DEL) {
            Intent intent = new Intent(this, SBDownloadActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.alarm_dialog);
        String title = getIntent().getExtras().getString("title");
        String msg = getIntent().getExtras().getString("msg");
        String busId = getIntent().getExtras().getString("busId");
        mUserSoundMode = getIntent().getExtras().getInt("userSoundMode");
        mBusColor = getIntent().getExtras().getString(Constants.ALARM_BUS_COLOR);
        mAlarmTime = getIntent().getExtras().getInt("alarmTime");

        makeDialog(title, msg, busId);

        unlockScreen();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDialog == null)
            return;

        mDialog.dismiss();
        mDialog = null;

    }

    private void cancelNoti() {

        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotiSetting.IS_NOTI_DEL = true;
        noti.cancel(NotiSetting.NOTI_ID);

        if (NotiSetting.IS_FINAL_ALARM && mUserSoundMode != -1) {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audio.setRingerMode(mUserSoundMode);
        }

    }

    private void unlockScreen() {

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void makeDialog(String title, String msg, String busId) {

        final SBDialog dialog = new SBDialog(this);

        TextView titleTxt1 = new TextView(this);
        titleTxt1.setTextSize(20);
        titleTxt1.setTextColor(Color.parseColor("#" + mBusColor));
        titleTxt1.setText(title.split(" ")[0] + " ");
        dialog.setTitleLayout(titleTxt1);

        TextView titleTxt2 = new TextView(this);
        titleTxt2.setTextSize(20);
        titleTxt2.setTextColor(Color.BLACK);
        titleTxt2.setText(title.split(" ")[1]);
        dialog.setTitleLayout(titleTxt2);

        TextView viewTxt1 = new TextView(this);
        viewTxt1.setTextSize(15);
        viewTxt1.setTextColor(Color.BLACK);
        viewTxt1.setText(msg);
        dialog.setViewLayout(viewTxt1);

        if (busId != null) {
            TextView viewTxt2 = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.topMargin = 10;
            viewTxt2.setLayoutParams(params);
            viewTxt2.setTextSize(15);
            viewTxt2.setTextColor(Color.DKGRAY);
            viewTxt2.setText(busId);
            dialog.setViewLayout(viewTxt2);
        }


        TextView viewTxt3 = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = 10;
        viewTxt3.setLayoutParams(params);
        viewTxt3.setTextSize(15);
        viewTxt3.setTextColor(Color.GRAY);
        viewTxt3.setText("도착 " + mAlarmTime + "분전 알람");
        dialog.setViewLayout(viewTxt3);

        dialog.getPositiveButton("창 닫기").setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NotiSetting.IS_FINAL_ALARM) {
                    cancelNoti();
                    NotiSetting.IS_FINAL_ALARM = false;
                }
                dialog.dismiss();
                finish();
            }
        });

        if (!NotiSetting.IS_FINAL_ALARM) {
            dialog.getNegativeButton("알람 삭제").setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelNoti();
                    NotiSetting.IS_FINAL_ALARM = false;
                    dialog.dismiss();
                    finish();
                }
            });
        }

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (NotiSetting.IS_FINAL_ALARM) {
                    cancelNoti();
                    NotiSetting.IS_FINAL_ALARM = false;
                }
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();

    }
}
