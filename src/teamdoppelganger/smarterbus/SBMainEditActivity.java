package teamdoppelganger.smarterbus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import teamdoppelganger.smarterbus.common.SBBaseNewActivity;


public class SBMainEditActivity extends SBBaseNewActivity implements View.OnClickListener {
    private Context context;

    private int viewType;
    private SBMainNewActivity.FavoriteListAdapter listAdapter;

    private CheckBox bannerCheckbox;
    private TextView viewTypeText;
    private PopupMenu viewTypePop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainedit_activity);
        context = this;

        actionBarInit("홈 화면 편집");

        bannerCheckbox = (CheckBox) findViewById(R.id.mainedit_banner_checkbox);
        viewTypeText = (TextView) findViewById(R.id.mainedit_viewType_text);

        bannerCheckbox.setOnClickListener(this);
        viewTypeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu viewTypePop = new PopupMenu(context, view);
                viewTypePop.getMenuInflater().inflate(R.menu.favorite_viewtype_pop, viewTypePop.getMenu());
                viewTypePop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.viewTypePop_menu1: viewType = 0; break;
                            case R.id.viewTypePop_menu2: viewType = 1; break;
                        }
                        setViewType();
                        return true;
                    }
                });
                viewTypePop.show();
            }
        });

        onNewIntent(getIntent());

        initAds();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if( intent != null ) {
            Bundle ext = intent.getExtras();
            if (ext != null) {

            }
        }
        // 임시로 viewType 지정
        // viewType = 0 리스트형, viewType = 1 카드형
        viewType = 1;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mainedit_banner_checkbox: setBannerHide(); break;
            case R.id.mainedit_viewType_text: setViewType(); break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render(){
        //Todo 즐겨찾기 목록 viewtype에 맞게 세팅
        listAdapter = new SBMainNewActivity.FavoriteListAdapter(this, viewType);
        RecyclerView autoRecyclerView = (RecyclerView) findViewById(R.id.favoriteList);
        autoRecyclerView.setLayoutManager(new GridLayoutManager(this, viewType+1));
        autoRecyclerView.setHasFixedSize(true);
        autoRecyclerView.setAdapter(listAdapter);

        //Todo 즐겨찾기 목록 불러오기
        listAdapter.render(getLocalDBHelper().workonfavoriteSelectList());
    }

    private void setBannerHide(){
        //Todo 이벤트 배너 숨기기 처리
        Toast.makeText(context, "이벤트 배너 숨기기 : "+ bannerCheckbox.isChecked(), Toast.LENGTH_SHORT).show();
    }

    private void setViewType(){
        //Todo 즐겨찾기 정렬 상태 db 저장

        render();
    }

}
