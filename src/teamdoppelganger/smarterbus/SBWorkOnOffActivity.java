package teamdoppelganger.smarterbus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TextView;

import com.mocoplex.adlib.AdlibManager;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.item.WorkTimeItem;
import teamdoppelganger.smarterbus.util.common.WorkOnOffCommonFunction;

/**
 * Created by muune on 2017-09-28.
 */

public class SBWorkOnOffActivity extends SBBaseNewActivity implements View.OnClickListener {

    private WorkOnOffCommonFunction.FavoriteListAdapter listAdapter1;
    private WorkOnOffCommonFunction.FavoriteListAdapter listAdapter2;

    private TextView malarmOnOff;
    private TextView alarmText;
    private String type = "출근";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workonoff_activity);

        actionBarInit("출/퇴근 알림 설정");

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost) ;
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("출근").setContent(R.id.tab1).setIndicator("출근길"));
        tabHost.addTab(tabHost.newTabSpec("퇴근").setContent(R.id.tab2).setIndicator("퇴근길"));
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                type = s;
            }
        });

        findViewById(R.id.settingBtn).setOnClickListener(this);
        findViewById(R.id.addlistbtn1).setOnClickListener(this);
        findViewById(R.id.addlistbtn2).setOnClickListener(this);

        malarmOnOff = (TextView)findViewById(R.id.alarmOnOff);
        alarmText = (TextView)findViewById(R.id.alarmText);

        listAdapter1 = new WorkOnOffCommonFunction.FavoriteListAdapter(this, WorkOnOffCommonFunction.LIST_TYPE_SBWorkOnOffActivity);
        RecyclerView autoRecyclerView1 = (RecyclerView) findViewById(R.id.workOnOffList1);
        autoRecyclerView1.setLayoutManager(new GridLayoutManager(this, 1));
        autoRecyclerView1.setHasFixedSize(true);
        autoRecyclerView1.setAdapter(listAdapter1);

        listAdapter2 = new WorkOnOffCommonFunction.FavoriteListAdapter(this, WorkOnOffCommonFunction.LIST_TYPE_SBWorkOnOffActivity);
        RecyclerView autoRecyclerView2 = (RecyclerView) findViewById(R.id.workOnOffList2);
        autoRecyclerView2.setLayoutManager(new GridLayoutManager(this, 1));
        autoRecyclerView2.setHasFixedSize(true);
        autoRecyclerView2.setAdapter(listAdapter2);

        this.setAds();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.settingBtn : settingOpen(); break;
            case R.id.addlistbtn1: listAdd("출근"); break;
            case R.id.addlistbtn2: listAdd("퇴근"); break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render(){
        WorkTimeItem t1 = getLocalDBHelper().workOnGet();
        WorkTimeItem t2 = getLocalDBHelper().workOffGet();
        malarmOnOff.setText("알림: " + (getLocalDBHelper().workOnIsactive() || getLocalDBHelper().workOffIsactive() ? "켜짐" : "꺼짐"));
        String str = "";
        if(t1 != null) str += "(출)" + String.format("%02d:%02d", t1.hour, t1.min) + (t2 != null ? " / " : "");
        if(t2 != null) str += "(퇴)" + String.format("%02d:%02d", t2.hour, t2.min);
        alarmText.setText(str);

        listAdapter1.render(getLocalDBHelper().workonfavoriteList());
        listAdapter2.render(getLocalDBHelper().workofffavoriteList());
    }


    private void settingOpen(){
        Intent intent = new Intent(getApplicationContext(), SBWorkTimeSetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void listAdd(String type){
        Log.i("bs", "listAdd:"+type);
        Intent intent = new Intent(getApplicationContext(), SBWorkAddActivity.class);
        intent.putExtra("type", type);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void listDel(WorkFavoriteItem $c){
        if(type.equals("출근")) getLocalDBHelper().workonfavoriteDel($c.rowid);
        else getLocalDBHelper().workofffavoriteDel($c.rowid);
        render();
    }
}
