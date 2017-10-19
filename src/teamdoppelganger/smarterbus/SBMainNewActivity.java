package teamdoppelganger.smarterbus;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.util.common.FavoriteCommonFunction;

/**
 * Created by muune on 2017-09-28.
 */

public class SBMainNewActivity extends SBBaseNewActivity implements View.OnClickListener {
    private Context context;

    private int viewType;
    private FavoriteListAdapter listAdapter;

    private AlertDialog editDialog;
    private TextView busNum_title;
    private TextView busNum;
    private EditText busText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        context = this;

        findViewById(R.id.main_menu).setOnClickListener(this);
        findViewById(R.id.searchEdit).setOnClickListener(this);
        findViewById(R.id.main_editBtn).setOnClickListener(this);

        listAdapter = new FavoriteListAdapter(this, viewType);
        RecyclerView autoRecyclerView = (RecyclerView) findViewById(R.id.favoriteList);
        autoRecyclerView.setLayoutManager(new GridLayoutManager(this, viewType+1));
        autoRecyclerView.setHasFixedSize(true);
        autoRecyclerView.setAdapter(listAdapter);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.favorite_edit2, null);
        busNum_title = (TextView) promptView.findViewById(R.id.busNum_title);
        busNum = (TextView) promptView.findViewById(R.id.busNum);
        busText = (EditText) promptView.findViewById(R.id.busText);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("즐겨찾기 편집").setView(promptView);
        alertDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Todo 즐겨찾기 편집
                Toast.makeText(context,"즐겨찾기 편집 : "+ busText.getText(),Toast.LENGTH_SHORT).show();
                render();
            }
        }).setNegativeButton("취소", null);
        editDialog = alertDialogBuilder.create();

        onNewIntent(getIntent());
        initBannerView();

        initAds();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i("bs", "onNewIntent");
        if( intent != null ) {
            Bundle ext = intent.getExtras();
            if (ext != null) {
                // viewType = 0 리스트형, viewType = 1 카드형
                viewType = 0;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.main_menu : menuOpen(); break;
            case R.id.searchEdit : searchOpen(); break;
            case R.id.main_editBtn :
                Intent intent = new Intent(getApplicationContext(), SBMainEditActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            default: break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void menuOpen(){
        Intent intent = new Intent(getApplicationContext(), SBMenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.left_in, R.anim.nochange);
    }
    private void searchOpen(){
        Intent intent = new Intent(getApplicationContext(), SBSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void initBannerView() {
        //Todo 배너 그리기
    }

    private void setRideAlarm(FavoriteAndHistoryItem item){
        Intent intent = new Intent(getApplicationContext(), SBRideAlarmActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    private void settingOpen(View view){
        PopupMenu settingPop = new PopupMenu(context, view);
        //Todo 정류장+노선일 경우 menuLayout은 R.menu.favorite_setting_pop2
        int menuLayout = R.menu.favorite_setting_pop1;
        settingPop.getMenuInflater().inflate(menuLayout, settingPop.getMenu());

        settingPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.settingPop_menu0: createShortCut(); break;
                    case R.id.settingPop_menu1: editPopOpen(); break;
                    case R.id.settingPop_menu2: favoriteDelete(); break;
                }
                return true;
            }
        });
        settingPop.show();
    }
    private void createShortCut(){
        //Todo 홈 바로가기 생성
        Toast.makeText(context,"홈 바로가기 생성",Toast.LENGTH_SHORT).show();
    }
    private void editPopOpen(){
        //Todo 즐겨찾기 수정 팝업에 값 넣기
        busNum_title.setText("노선번호");
        busNum.setText("9404");
        busText.setText("신정호저수지입구");
        editDialog.show();
    }
    private void favoriteDelete(){
        //Todo 즐겨찾기 삭제
        Toast.makeText(context,"즐겨찾기 삭제",Toast.LENGTH_SHORT).show();
    }

    private void render(){
        //Todo 즐겨찾기 목록 불러오기
        listAdapter.render(getLocalDBHelper().workonfavoriteSelectList());
    }

    public static class ListHolder extends RecyclerView.ViewHolder {
        public ImageView iconImgView, alarmBtn, settingBtn;
        public TextView name, subMain, subMain2, arrive_1, arrive_2;
        public LinearLayout arriveLayout;
        public ListHolder(View itemView) {
            super(itemView);
            iconImgView = (ImageView)itemView.findViewById(R.id.iconImg);
            alarmBtn = (ImageView)itemView.findViewById(R.id.alarmBtn);
            name = (TextView)itemView.findViewById(R.id.name);
            subMain = (TextView)itemView.findViewById(R.id.subMain);
            subMain2 = (TextView)itemView.findViewById(R.id.subMain2);
            settingBtn = (ImageView)itemView.findViewById(R.id.settingBtn);
            arriveLayout = (LinearLayout)itemView.findViewById(R.id.arriveLayout);
            arrive_1 = (TextView)itemView.findViewById(R.id.arrive_1);
            arrive_2 = (TextView)itemView.findViewById(R.id.arrive_2);
        }
    }

    public static class FavoriteListAdapter extends  RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private SBBaseNewActivity context;
        private List<WorkFavoriteItem> _c;
        private ArrayList<FavoriteAndHistoryItem> _bus;
        private int type;
        public FavoriteListAdapter(SBBaseNewActivity context, int type){
            this.context = context;
            this.type = type;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
            RecyclerView.ViewHolder viewHolder;
            int rowRayout = 0;
            if(type == 0)  rowRayout = R.layout.favorite_list_row2;
            else if(type == 1)  rowRayout = R.layout.favorite_card_row;

            View v = LayoutInflater.from(parent.getContext()).inflate(rowRayout, parent, false);
            viewHolder = new ListHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final WorkFavoriteItem obj = _c.get(position);
            final FavoriteAndHistoryItem bus = _bus.get(position);

            ((ListHolder) holder).name.setText(bus.busRouteItem.busRouteName);
            ((ListHolder) holder).subMain.setText(bus.busRouteItem.busStopName);
            ((ListHolder) holder).iconImgView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_busstop));
            try {
                String color = ((SBInforApplication) context.getApplicationContext()).mBusTypeHash.get(bus.busRouteItem.busType);
                ((ListHolder) holder).name.setTextColor(Color.parseColor("#" + color));
            } catch (Exception e) {
                ((ListHolder) holder).name.setTextColor(Color.parseColor("#000000"));
            }

            ((ListHolder) holder).alarmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SBMainNewActivity act = (SBMainNewActivity)context;
                    act.setRideAlarm(bus);
                }
            });

            ((ListHolder) holder).settingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SBMainNewActivity act = (SBMainNewActivity)context;
                    act.settingOpen(view);
                }
            });

        }

        @Override
        public int getItemCount() {
            return _c == null ? 0 : _c.size();
        }

        public void render(List<WorkFavoriteItem> $c) {
            _c = $c;
            _bus = FavoriteCommonFunction.getFavoriteAndHistoryItemList(context.getBusDbSqlite(), (SBInforApplication)context.getApplication(), _c);
            notifyDataSetChanged();
        }

        public void setType(int $type){
            type = $type;
        }
    }

}
