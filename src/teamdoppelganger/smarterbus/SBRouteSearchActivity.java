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
 * Created by user on 2017-10-18.
 */

public class SBRouteSearchActivity extends SBBaseNewActivity implements View.OnClickListener {

    private TextView startName;
    private TextView endName;
    private String type = "최근검색";

    private ListAdapter0 listAdapter0;
//    private ListAdapter1 listAdapter1;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routesearch_activity);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("0").setContent(R.id.tab_recent).setIndicator("최근검색"));
        tabHost.addTab(tabHost.newTabSpec("1").setContent(R.id.tab_favorite).setIndicator("즐겨찾기"));
        tabHost.addTab(tabHost.newTabSpec("2").setContent(R.id.tab_map).setIndicator("지도"));

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
            type = tabId.equals("0") ? "최근검색" : tabId.equals("1") ?"즐겨찾기" : "지도";
            render();
            }
        });

        startName = (TextView) findViewById(R.id.startName);
        endName = (TextView) findViewById(R.id.endName);
        findViewById(R.id.delBtn).setOnClickListener(this);

        listAdapter0 = new SBRouteSearchActivity.ListAdapter0(SBRouteSearchActivity.this);
        RecyclerView autoRecyclerView0 = (RecyclerView) findViewById(R.id.listView_recent);
        autoRecyclerView0.setLayoutManager(new GridLayoutManager(this, 1));
        autoRecyclerView0.setHasFixedSize(true);
        autoRecyclerView0.setAdapter(listAdapter0);

//        listAdapter1 = new SBRouteSearchActivity.ListAdapter1(SBRouteSearchActivity.this);
//        RecyclerView autoRecyclerView1 = (RecyclerView) findViewById(R.id.listView_favorite);
//        autoRecyclerView1.setLayoutManager(new GridLayoutManager(this, 1));
//        autoRecyclerView1.setHasFixedSize(true);
//        autoRecyclerView1.setAdapter(listAdapter1);

//        mTerminus = ((SBInforApplication) getApplicationContext()).mTerminus;
//        onNewIntent(getIntent());
//        dataLoad();
    }

    private void render(){

        ArrayList<RouteItem> recentList = new ArrayList<RouteItem>();

        for (int i = 0; i < 2; i++){
            RouteItem recentItem = new RouteItem();
            recentItem.setDeparture("출발지 " + i);
            recentItem.setArrival("도착지 " + i);
            recentList.add(recentItem);
        }

        listAdapter0.render(recentList);

//        List<HashMap<String,String>> list1 = new ArrayList<>();
//        for (int i = 0; i < 25; i++) list1.add(new HashMap<String, String>(){});


       // listAdapter1.render(list1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //case R.id.delBtn : delRecentitem(); break;
            //case R.id.change_route_btn: changeRoute();break;
        }
    }

    private void delRecentitem(){
        //Todo 최근 검색 내역 삭제
        Toast.makeText(SBRouteSearchActivity.this, "최근 검색 내역 삭제!", Toast.LENGTH_SHORT).show();
        render();
    }

    private void changeRoute(){
        //Todo 출발지점 도착지점 바꾸기
        Toast.makeText(SBRouteSearchActivity.this, "출발지점 도착지점 바꾸기", Toast.LENGTH_SHORT).show();
    }

    static class ListHolder0 extends RecyclerView.ViewHolder {
        TextView startName, endName;
        ImageView delBtn;

        public ListHolder0(View itemView) {
            super(itemView);
            startName = (TextView)itemView.findViewById(R.id.startName);
            endName = (TextView)itemView.findViewById(R.id.endName);
            delBtn = (ImageView)itemView.findViewById(R.id.delBtn);
        }
    }

    class ListAdapter0 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        SBRouteSearchActivity context;
        private ArrayList<RouteItem> _c;

        public ListAdapter0(SBRouteSearchActivity context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.routesearch_recent_row, parent, false);
            viewHolder = new SBRouteSearchActivity.ListHolder0(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final RouteItem routeItem = _c.get(position);
            ((ListHolder0) holder).startName.setText(routeItem.getDeparture());
            ((ListHolder0) holder).endName.setText(routeItem.getArrival());
        }

        @Override
        public int getItemCount() { return _c == null ? 0 : _c.size(); }

        public void render(ArrayList<RouteItem> $c) {
            _c = $c;
            notifyDataSetChanged();
        }
    }

    public class RouteItem{
        private String departure;
        private String arrival;

        public String getDeparture(){
            return departure;
        }
        public String getArrival(){
            return arrival;
        }
        public void setDeparture(String d){
            this.departure = d;
        }
        public void setArrival(String a){
            this.arrival = a;
        }
    }
}



//package teamdoppelganger.smarterbus;
//
//import android.content.Intent;
//import android.content.res.Configuration;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.support.v7.widget.GridLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.EditorInfo;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TabHost;
//import android.widget.TextView;
//
//import com.mocoplex.adlib.AdlibAdViewContainer;
//import com.mocoplex.adlib.AdlibManager;
//import com.mocoplex.adlib.nativead.layout.AdlibNativeLayout;
//import com.mocoplex.adlib.platform.nativeads.AdlibNativeHelper;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import teamdoppelganger.smarterbus.common.Constants;
//import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
//import teamdoppelganger.smarterbus.item.BusStopItem;
//import teamdoppelganger.smarterbus.util.widget.LineImageView;
//
///**
// * Created by muune on 2017-09-28.
// */
//
//public class SBRouteSearchActivity extends SBBaseNewActivity implements View.OnClickListener {
//
//
//    //private String type = "버스";
//    //private BusAdapter busAdapter;
//
//    private TextView searchDeparture;
//    private TextView searchArrival;
//    private ArrayList<RouteItem> routeList;
//    private SBRecentSearchFragment.RecentAdapter recentAdapter;
//
//    private AdlibNativeHelper nativeHelper = null;
//    public AdlibManager amanager1;
//    public AdlibManager amanager2;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        //레이아웃 설정
//        setContentView(R.layout.routesearch_activity);
//
//        //탭 설정
//        TabHost tabHost = (TabHost) findViewById(R.id.tabHost) ;
//        tabHost.setup();
//        tabHost.addTab(tabHost.newTabSpec("최근검색").setContent(R.id.listView_recent).setIndicator("최근검색"));
//        tabHost.addTab(tabHost.newTabSpec("즐겨찾기").setContent(R.id.listView_favorite).setIndicator("즐겨찾기"));
//        tabHost.addTab(tabHost.newTabSpec("지도").setContent(R.id.listView_map).setIndicator("지도"));
//        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
//            @Override
//            public void onTabChanged(String s) {
//                type = s;
//            }
//        });
//
//        RecyclerView recentListView = (RecyclerView)findViewById(R.id.tab_recent);
//        recentListView.setLayoutManager(new GridLayoutManager(this, 1));
//        recentListView.setHasFixedSize(true);
//
//        recentAdapter = new SBRecentSearchFragment.RecentAdapter(this);
//
//        recentListView.setAdapter(recentAdapter);
//
//        nativeHelper = new AdlibNativeHelper(recentListView);
//        nativeHelper.registerScrollChangedListener();
//        amanager1 = this.getNewAdlibManager();
//        amanager2 = this.getNewAdlibManager();
//
//        searchDeparture = (EditText) findViewById(R.id.searchDepart);
//        searchDeparture.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                switch (actionId) {
//                    case EditorInfo.IME_ACTION_SEARCH:
//                        //키보드 내리게 적용
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//        });
//    }
//
//    class ListHolder1 extends RecyclerView.ViewHolder {
//
//        RelativeLayout recent_row;
//        TextView startName;
//        TextView endName;
//        Button delBtn;
//
//        public ListHolder1(View itemView) {
//            super(itemView);
//            recent_row = (RelativeLayout)itemView.findViewById(R.id.recentrow_layout);
//            startName = (TextView)itemView.findViewById(R.id.startName);
//            endName = (TextView)itemView.findViewById(R.id.endName);
//            delBtn = (Button)itemView.findViewById(R.id.delBtn);
//        }
//    }
//
//    class ListAdapter1 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//        SBRouteSearchActivity context;
//        private List<HashMap<String,String>> _c;
//
//        public ListAdapter1(SBRouteSearchActivity context) {
//            this.context = context;
//        }
//
//        @Override
//        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            RecyclerView.ViewHolder viewHolder;
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.routesearch_recent_row, parent, false);
//            viewHolder = new SBRouteSearchActivity.ListHolder1(v);
//            return viewHolder;
//        }
//
//        @Override
//        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
//            final HashMap<String, String> obj = _c.get(position);
//
////            ((SBRideAlarmActivity.ListHolder1) holder).select_btn.setVisibility(View.GONE);
////
////            // Todo 하차 정류장이면 배경색이 들어감
////            if(position == 2) ((SBRideAlarmActivity.ListHolder1) holder).rideoff_row.setBackgroundColor(Color.rgb(255, 242, 204));
////            ((SBRideAlarmActivity.ListHolder1) holder).rideoff_alaram_btn.setOnClickListener(new View.OnClickListener() {
////                @Override
////                public void onClick(View view) {
////                    // Todo 알람이 켜져 있으면 알람 종료 함수를 연결
////                    if(position == 2) endRideOffAlarm(obj);
////                    else startRideOffAlarm(obj);
////                }
////            });
////
////            if (position == 0) {
////                ((SBRideAlarmActivity.ListHolder1) holder).busImg.setType(Constants.LINE_START);
////            } else if (position == _c.size() - 1) {
////                ((SBRideAlarmActivity.ListHolder1) holder).busImg.setType(Constants.LINE_END);
////            } else {
////                ((SBRideAlarmActivity.ListHolder1) holder).busImg.setType(Constants.LINE_NOMAL);
////            }
//        }
//
//        @Override
//        public int getItemCount() { return _c == null ? 0 : _c.size(); }
//
//        public void render(List<HashMap<String,String>> $c) {
//            _c = $c;
//            notifyDataSetChanged();
//        }
//    }
//
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()){
//            //case R.id.path_search_btm : routeSearchOpen(); break;
//        }
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        nativeHelper.onResume();
//        render();
//    }
//
//    @Override
//    protected void onPause() {
//        nativeHelper.onPause();
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        nativeHelper.onDestroy();
//        super.onDestroy();
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        nativeHelper.update();
//    }
//
//    private void render(){
//        routeList = new ArrayList<RouteItem>(){};
//        int i = 0;
//        while(i++ < 5){
//            RouteItem r = new RouteItem();
//            routeList.add(r);
//        }
//        recentAdapter.render(routeList);
//    }
//
//
//}
