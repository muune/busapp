package teamdoppelganger.smarterbus.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import teamdoppelganger.smarterbus.util.common.WorkOnOffCommonFunction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

/**
 * Created by muune on 2017-10-13.
 */

public class BootReceiver extends BroadcastReceiver{ //핸드폰이 꺼지면 기존 알람이 다 지워집니다. 핸드폰이 켜지면 이 BootReceiver가 실행되고 알람이 설정되어 있다면 다시 알람을 추가하는 클래스입니다.
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            LocalDBHelper mLocalDBHelper = new LocalDBHelper(context);
            if(mLocalDBHelper.workOnIsactive()) WorkOnOffCommonFunction.alarmAdd(context, mLocalDBHelper, true);
            if(mLocalDBHelper.workOffIsactive()) WorkOnOffCommonFunction.alarmAdd(context, mLocalDBHelper, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
