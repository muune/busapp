package teamdoppelganger.smarterbus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lib.CommonConstants;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.FavoriteCommonFunction;
import teamdoppelganger.smarterbus.util.common.GetDataExt;
import teamdoppelganger.smarterbus.util.widget.LineImageView;

/**
 * Created by user on 2017-10-11.
 */

public class SBRideAlarmActivity extends SBBaseNewActivity implements View.OnClickListener{
    private FavoriteAndHistoryItem mFavoriteAndHistoryItem;
    private BusRouteItem mBusRouteItem;
    public HashMap<Integer, String> mTerminus;
    private GetDataExt mGetDataExt;

    private String type = "승차";
    private TextView info_title;
    private TextView info_route;
    private TextView info_station;

    private TextView off_direction1;
    private TextView off_direction2;

    private ListAdapter0 listAdapter0;
    private ListAdapter1 listAdapter1;

    private AlertDialog.Builder endAlarmDialog;
    private AlertDialog.Builder changeAlarmDialog;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ridealarm_activity);

        actionBarInit("알람 설정");

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("0").setContent(R.id.tab0).setIndicator("승차 알람"));
        tabHost.addTab(tabHost.newTabSpec("1").setContent(R.id.tab1).setIndicator("하차 알람"));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                type = tabId.equals("0") ? "승차" : "하차";
                render();
            }
        });

        info_title = (TextView) findViewById(R.id.info_title);
        info_route = (TextView) findViewById(R.id.info_route);
        info_station = (TextView) findViewById(R.id.info_station);
        off_direction1 = (TextView) findViewById(R.id.direction1);
        off_direction2 = (TextView) findViewById(R.id.direction2);

        findViewById(R.id.refreshBtn).setOnClickListener(this);
        findViewById(R.id.showRoute).setOnClickListener(this);
        findViewById(R.id.direction1).setOnClickListener(this);
        findViewById(R.id.direction2).setOnClickListener(this);

        listAdapter0 = new SBRideAlarmActivity.ListAdapter0(SBRideAlarmActivity.this);
        RecyclerView autoRecyclerView0 = (RecyclerView) findViewById(R.id.rideAlarmList0);
        autoRecyclerView0.setLayoutManager(new GridLayoutManager(this, 1));
        autoRecyclerView0.setHasFixedSize(true);
        autoRecyclerView0.setAdapter(listAdapter0);

        listAdapter1 = new SBRideAlarmActivity.ListAdapter1(SBRideAlarmActivity.this);
        RecyclerView autoRecyclerView1 = (RecyclerView) findViewById(R.id.rideAlarmList1);
        autoRecyclerView1.setLayoutManager(new GridLayoutManager(this, 1));
        autoRecyclerView1.setHasFixedSize(true);
        autoRecyclerView1.setAdapter(listAdapter1);

        // 알람 종료 팝업
        endAlarmDialog = new AlertDialog.Builder(this);
        // 알람 교체 팝업
        changeAlarmDialog = new AlertDialog.Builder(this);

        mTerminus = ((SBInforApplication) getApplicationContext()).mTerminus;
        onNewIntent(getIntent());
        dataLoad();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGetDataExt.pauseAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGetDataExt.run(Constants.PARSER_REFRSH_TYPE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGetDataExt.stopAll();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if( intent != null ) {
            Bundle ext = intent.getExtras();
            if (ext != null) {
                mFavoriteAndHistoryItem = (FavoriteAndHistoryItem)ext.get("item");
                mBusRouteItem = mFavoriteAndHistoryItem.busRouteItem;
            }
        }
    }

    private void dataLoad(){
        GetDataExt getDataExt = GetDataExt.instance(new GetDataExt.GetDataExtListener() {
            @Override
            public void onComplete(int type, DepthItem item) {
                //mGetDataExt.run(type);
                render();
            }
        }, getBusDbSqlite(), ((SBInforApplication) getApplicationContext()));
        mGetDataExt = getDataExt;

        //정류장 정보
        ArrayList<FavoriteAndHistoryItem> itemList = new ArrayList<FavoriteAndHistoryItem>();
        itemList.add(mFavoriteAndHistoryItem);
        getDataExt.readyRefreshService(itemList);
        getDataExt.run(Constants.PARSER_REFRSH_TYPE);

        //노선 정보
        //getDataExt.busStopParsing(mBusRouteItem);

        render();
    }
    public String getBusName(SQLiteDatabase db, int id) {
        String sql = String.format("SELECT *FROM %s where %s='%s'", CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, id);
        String busName = "";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) {
            busName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_TYPE_BUS_TYPE));
        }
        cursor.close();

        return busName;
    }
    private void render(){
        info_title.setText("["+ type +" 정보]");
        String busType = getBusName(getBusDbSqlite(), mBusRouteItem.busType);

        info_route.setText(type +" 노선 - "+ mBusRouteItem.busRouteName +"("+ mBusRouteItem.busRouteSubName + busType +"버스)");
        info_station.setText(type +" 정류장 - "+ mBusRouteItem.busStopName +"("+ mBusRouteItem.direction +" 방면)");

        String endStopName = mTerminus.get(Integer.parseInt(mBusRouteItem.endStop));
        off_direction1.setText(endStopName +" 방면");
        off_direction2.setText(mTerminus.get(Integer.parseInt(mBusRouteItem.startStop)) +" 방면");

        if(endStopName.equals(mBusRouteItem.direction)){
            off_direction1.setBackgroundColor(Color.parseColor("#FFFFFF"));
            off_direction2.setBackgroundColor(Color.parseColor("#0000FF"));
        }else{
            off_direction1.setBackgroundColor(Color.parseColor("#0000FF"));
            off_direction2.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }

        listAdapter0.render(mFavoriteAndHistoryItem.busRouteItem.arriveInfo);

        List<HashMap<String,String>> list1 = new ArrayList<>();
        for (int i = 0; i < 25; i++) list1.add(new HashMap<String, String>(){});
        listAdapter1.render(list1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.refreshBtn : refreshList(); break;
            case R.id.showRoute: showRoute(); break;
            case R.id.direction1: setDirection(0); break;
            case R.id.direction2: setDirection(1); break;
        }
    }

    private void refreshList(){
        //Todo 버스 도착 정보 새로고침
        Toast.makeText(SBRideAlarmActivity.this, "목록 새로고침!", Toast.LENGTH_SHORT).show();
        render();
    }

    private void showRoute(){
        //Todo 노선보기 : 해당 노선 정보 화면으로 이동됨
        Toast.makeText(SBRideAlarmActivity.this, "노선 보기", Toast.LENGTH_SHORT).show();
    }

    private void startRideOnAlarm(HashMap<String, String> $c){
        //Todo 다른 알람이 켜져있다면,
        changeAlarmDialog.setMessage("작동중인 승차 알람이 있습니다. \n" + "기존 알람을 중지하고 새로 알람을 시작하겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SBRideAlarmActivity.this, "승차 알람 교체", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("취소", null);
        changeAlarmDialog.show();
    }
    private void startRideOffAlarm(HashMap<String, String> $c){
        //Todo 다른 알람이 켜져있다면,
        changeAlarmDialog.setMessage("작동중인 하차 알람이 있습니다. \n" + "기존 알람을 중지하고 새로 알람을 시작하겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SBRideAlarmActivity.this, "하차 알람 교체", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("취소", null);
        changeAlarmDialog.show();
    }
    private void endRideOnAlarm(HashMap<String, String> $c){
        endAlarmDialog.setMessage("승차 알람을 종료하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SBRideAlarmActivity.this, "승차 알람 종료", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("취소", null);
        endAlarmDialog.show();
    }
    private void endRideOffAlarm(HashMap<String, String> $c){
        endAlarmDialog.setMessage("하차 알람을 종료하시겠습니까?").setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SBRideAlarmActivity.this, "하차 알람 종료", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("취소", null);
        endAlarmDialog.show();
    }

    private void setDirection(int i){
        //Todo 노선 방향 변경
        Toast.makeText(SBRideAlarmActivity.this, "방향 바꿈 : " + i, Toast.LENGTH_SHORT).show();
        render();
    }

    static class ListHolder0 extends RecyclerView.ViewHolder {
        TextView title, arrival_time, arrival_count, currStation, nextStation, arrival_none;
        LinearLayout layout_radio;
        RadioGroup radioGroup;
        ImageView rideon_alaram_btn;

        public ListHolder0(View itemView) {
            super(itemView);
            title = (TextView)itemView.findViewById(R.id.title);
            arrival_time = (TextView)itemView.findViewById(R.id.arrival_time);
            arrival_count = (TextView)itemView.findViewById(R.id.arrival_count);
            currStation = (TextView)itemView.findViewById(R.id.currStation);
            nextStation = (TextView)itemView.findViewById(R.id.nextStation);
            arrival_none = (TextView)itemView.findViewById(R.id.arrival_none);
            layout_radio = (LinearLayout)itemView.findViewById(R.id.layout_radio);
            radioGroup = (RadioGroup)itemView.findViewById(R.id.radioGroup);
            rideon_alaram_btn = (ImageView)itemView.findViewById(R.id.rideon_alaram_btn);
        }
    }
    class ListAdapter0 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        SBRideAlarmActivity context;
        private ArrayList<ArriveItem> _c;

        public ListAdapter0(SBRideAlarmActivity context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ridealarm_on_row, parent, false);
            viewHolder = new SBRideAlarmActivity.ListHolder0(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ArriveItem arriveItem = _c.get(position);
            ((ListHolder0) holder).title.setText((position == 0 ? "첫" : "두")+"번째 버스");

            if(arriveItem.state == Constants.STATE_ING) {
                ((ListHolder0) holder).arrival_none.setVisibility(View.GONE);

                ((ListHolder0) holder).arrival_time.setText((arriveItem.remainMin > 0 ? arriveItem.remainMin + "분" : "" )+ (arriveItem.remainSecond > 0 ? " "+ arriveItem.remainSecond + "초" : "" ));
                ((ListHolder0) holder).arrival_time.setVisibility(View.VISIBLE);
                ((ListHolder0) holder).arrival_count.setText(arriveItem.remainStop +"정류장 전");
                ((ListHolder0) holder).arrival_count.setVisibility(View.VISIBLE);
                ((ListHolder0) holder).currStation.setVisibility(View.VISIBLE);
                ((ListHolder0) holder).nextStation.setVisibility(View.VISIBLE);
                ((ListHolder0) holder).layout_radio.setVisibility(View.VISIBLE);

                ((ListHolder0) holder).rideon_alaram_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Todo 알람이 켜져 있으면 알람 종료 함수를 연결
                        //if(position == 0) endRideOnAlarm(obj);
                        //else startRideOnAlarm(obj);
                    }
                });
            }else{
                ((ListHolder0) holder).arrival_none.setVisibility(View.VISIBLE);

                ((ListHolder0) holder).arrival_time.setVisibility(View.GONE);
                ((ListHolder0) holder).arrival_count.setVisibility(View.GONE);
                ((ListHolder0) holder).currStation.setVisibility(View.GONE);
                ((ListHolder0) holder).nextStation.setVisibility(View.GONE);
                ((ListHolder0) holder).layout_radio.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return _c == null ? 0 : _c.size(); }

        public void render(ArrayList<ArriveItem> $c) {
            _c = $c;
            notifyDataSetChanged();
        }
    }

    static class ListHolder1 extends RecyclerView.ViewHolder {
        RelativeLayout rideoff_row;
        TextView stationName;
        Button select_btn;
        ImageView rideoff_alaram_btn;
        LineImageView busImg;

        public ListHolder1(View itemView) {
            super(itemView);
            rideoff_row = (RelativeLayout)itemView.findViewById(R.id.rideoff_row);
            stationName = (TextView)itemView.findViewById(R.id.stationName);
            select_btn = (Button)itemView.findViewById(R.id.select_btn);
            rideoff_alaram_btn = (ImageView)itemView.findViewById(R.id.rideoff_alaram_btn);
            busImg = (LineImageView) itemView.findViewById(R.id.busImg);
        }
    }
    class ListAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        SBRideAlarmActivity context;
        private List<HashMap<String,String>> _c;

        public ListAdapter1(SBRideAlarmActivity context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ridealarm_off_row, parent, false);
            viewHolder = new SBRideAlarmActivity.ListHolder1(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final HashMap<String, String> obj = _c.get(position);

            ((ListHolder1) holder).select_btn.setVisibility(View.GONE);

            // Todo 하차 정류장이면 배경색이 들어감
            if(position == 2) ((ListHolder1) holder).rideoff_row.setBackgroundColor(Color.rgb(255, 242, 204));
            ((ListHolder1) holder).rideoff_alaram_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Todo 알람이 켜져 있으면 알람 종료 함수를 연결
                    if(position == 2) endRideOffAlarm(obj);
                    else startRideOffAlarm(obj);
                }
            });

            if (position == 0) {
                ((ListHolder1) holder).busImg.setType(Constants.LINE_START);
            } else if (position == _c.size() - 1) {
                ((ListHolder1) holder).busImg.setType(Constants.LINE_END);
            } else {
                ((ListHolder1) holder).busImg.setType(Constants.LINE_NOMAL);
            }
        }

        @Override
        public int getItemCount() { return _c == null ? 0 : _c.size(); }

        public void render(List<HashMap<String,String>> $c) {
            _c = $c;
            notifyDataSetChanged();
        }
    }
}
