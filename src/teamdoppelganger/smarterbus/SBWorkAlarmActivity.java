package teamdoppelganger.smarterbus;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.util.common.FavoriteCommonFunction;
import teamdoppelganger.smarterbus.util.common.GetDataExt;
import teamdoppelganger.smarterbus.util.common.WorkOnOffCommonFunction;

/**
 * Created by muune on 2017-09-28.
 */

public class SBWorkAlarmActivity extends SBBaseNewActivity {

    private WorkOnOffCommonFunction.FavoriteListAdapter listAdapter;
    private GetDataExt getDataExt;
    private String type = "";
    private TextView workAlarm_title;
    private Context context;

    List<WorkFavoriteItem> favoriteItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );

        setContentView(R.layout.workonoff_alarm);
        context = this;

        workAlarm_title = (TextView)findViewById(R.id.workAlarm_title);
        listAdapter = new WorkOnOffCommonFunction.FavoriteListAdapter(this, WorkOnOffCommonFunction.LIST_TYPE_SBWorkAlarmActivity);
        RecyclerView workAlarmList = (RecyclerView) findViewById(R.id.workAlarmList);
        workAlarmList.setLayoutManager(new GridLayoutManager(this, 1));
        workAlarmList.setHasFixedSize(true);
        workAlarmList.setAdapter(listAdapter);


        getDataExt = GetDataExt.instance(new GetDataExt.GetDataExtListener() {
            @Override
            public void onComplete(int type, DepthItem item) {
                Log.i("bs", "onComplete");
                listAdapter.render(favoriteItemList);
            }
        }, getBusDbSqlite(), (SBInforApplication) getApplicationContext());


        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("bs", "onNewIntent");
        if( intent != null ) {
            Bundle ext = intent.getExtras();
            if (ext != null) {
                type = ext.getString("type");
                Log.i("bs", "onNewIntent:" + type);
                if(getLocalDBHelper().workOffIsactive() || getLocalDBHelper().workOnIsactive()){ //알람이 울리도록 설정되어 있다면 다음 알람을 추가함.
                    WorkOnOffCommonFunction.alarmAdd(context, getLocalDBHelper(), type.equals("출근"));
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("bs", "onResume:" + type);
        render();
    }


    private void render(){
        Log.i("bs", "render:" + type);
        if(workAlarm_title != null) workAlarm_title.setText(type + "길 알림");
        favoriteItemList = type.equals("출근") ? getLocalDBHelper().workonfavoriteList() : getLocalDBHelper().workofffavoriteList();
        listAdapter.render(favoriteItemList);
        //getDataExt.refreshService(FavoriteCommonFunction.getFavoriteAndHistoryItemList(getBusDbSqlite(), (SBInforApplication)getApplicationContext(), favoriteItemList));
    }

    public void close(View view){
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();

        if(list.size() == 0){
            Intent intent = new Intent(getApplicationContext(), SBMainNewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finish();
    }
}
