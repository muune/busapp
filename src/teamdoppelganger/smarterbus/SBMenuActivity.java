package teamdoppelganger.smarterbus;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class SBMenuActivity extends Activity implements View.OnClickListener{
    private static final String[] MENU = new String[]{"지역 설정", "홈 화면 편집", "출/퇴근 알람", "설정", "공지사항"};
    private static final String[] APP = new String[]{"지하철종결자", "코미코", "핑크다이어리", "운수도원"};

    private MenuListAdapter listAdapter0;
    private AppListAdapter listAdapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        findViewById(R.id.menu_layout).setOnClickListener(this);

        listAdapter0 = new SBMenuActivity.MenuListAdapter(SBMenuActivity.this);
        RecyclerView menuList = (RecyclerView) findViewById(R.id.menuList);
        menuList.setLayoutManager(new GridLayoutManager(this, 1));
        menuList.setHasFixedSize(true);
        menuList.setAdapter(listAdapter0);
        listAdapter0.render(MENU);

        listAdapter1 = new SBMenuActivity.AppListAdapter(SBMenuActivity.this);
        RecyclerView appList = (RecyclerView) findViewById(R.id.appList);
        appList.setLayoutManager(new GridLayoutManager(this, 1));
        appList.setHasFixedSize(true);
        appList.setAdapter(listAdapter1);
        listAdapter1.render(APP);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.menu_layout :
                finish();
                overridePendingTransition(R.anim.nochange,R.anim.left_out);
                break;
            default: break;
        }
    }

    private void menuClick(int position){
        Intent intent;
        //Todo 메뉴 클릭 이벤트
        switch (position){
            case 0: Toast.makeText(getApplicationContext(), "지역 설정 팝업", Toast.LENGTH_SHORT).show(); break;
            case 1:
                intent = new Intent(getApplicationContext(), SBMainEditActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
            case 2:
                intent = new Intent(getApplicationContext(), SBWorkOnOffActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
            case 3: Toast.makeText(getApplicationContext(), "설정 페이지", Toast.LENGTH_SHORT).show(); break;
            case 4: Toast.makeText(getApplicationContext(), "공지사항 페이지", Toast.LENGTH_SHORT).show(); break;
            default: break;
        }
    }

    private void appClick(int position){
        //Todo app 클릭하면 설치여부에 따라 앱실행 or 마켓 페이지 이동
        switch (position){
            case 0:
                //메인 편하게 보기 위해서 임시로 넣어 놓음.
                Intent intent = new Intent(getApplicationContext(), SBMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "지하철종결자", Toast.LENGTH_SHORT).show();
                break;
            case 1: Toast.makeText(getApplicationContext(), "코미코", Toast.LENGTH_SHORT).show(); break;
            case 2: Toast.makeText(getApplicationContext(), "핑크다이어리", Toast.LENGTH_SHORT).show(); break;
            case 3: Toast.makeText(getApplicationContext(), "운수도원", Toast.LENGTH_SHORT).show(); break;
            default: break;
        }
    }

    static class MenuListHolder extends RecyclerView.ViewHolder{
        RelativeLayout row;
        TextView title, newIcon;

        public MenuListHolder(View itemView) {
            super(itemView);
            row = (RelativeLayout) itemView.findViewById(R.id.menu_row);
            title = (TextView) itemView.findViewById(R.id.menu_title);
            newIcon = (TextView) itemView.findViewById(R.id.menu_newIcon);
        }
    }
    private class MenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private SBMenuActivity context;
        private String[] _c;

        public MenuListAdapter(SBMenuActivity context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_row, parent, false);
            viewHolder = new MenuListHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            final String obj = _c[position];
            ((MenuListHolder) viewHolder).row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuClick(position);
                }
            });

            ((MenuListHolder) viewHolder).title.setText(obj);
            if(position != 4) ((MenuListHolder) viewHolder).newIcon.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return _c == null ? 0 : _c.length;
        }

        public void render(String[] $c) {
            _c = $c;
            notifyDataSetChanged();
        }
    }


    static class AppListHolder extends RecyclerView.ViewHolder{
        RelativeLayout row;
        ImageView appIcon;
        TextView appTitle;

        public AppListHolder(View itemView) {
            super(itemView);
            row = (RelativeLayout) itemView.findViewById(R.id.menu_app_row);
            appIcon = (ImageView) itemView.findViewById(R.id.menu_app_icon);
            appTitle = (TextView) itemView.findViewById(R.id.menu_app_title);
        }
    }
    private class AppListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private SBMenuActivity context;
        private String[] _c;

        public AppListAdapter(SBMenuActivity context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_app_row, parent, false);
            viewHolder = new AppListHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            final String obj = _c[position];
            ((AppListHolder) viewHolder).row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    appClick(position);
                }
            });
            ((AppListHolder) viewHolder).appTitle.setText(obj);
        }

        @Override
        public int getItemCount() {
            return _c == null ? 0 : _c.length;
        }

        public void render(String[] $c) {
            _c = $c;
            notifyDataSetChanged();
        }
    }
}
