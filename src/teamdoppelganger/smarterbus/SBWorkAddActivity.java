package teamdoppelganger.smarterbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.item.WorkFavoriteItem;
import teamdoppelganger.smarterbus.util.common.WorkOnOffCommonFunction;

/**
 * Created by muune on 2017-09-28.
 */

public class SBWorkAddActivity extends SBBaseNewActivity {

    private WorkOnOffCommonFunction.FavoriteListAdapter listAdapter;
    private String type = "출근";
    private TextView alarm_add_empty;
    private TextView alarm_add_desc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workonoff_listadd);

        actionBarInit("알림 항목 추가");

        alarm_add_empty = (TextView)findViewById(R.id.alarm_add_empty);
        alarm_add_desc = (TextView)findViewById(R.id.alarm_add_desc);

        listAdapter = new WorkOnOffCommonFunction.FavoriteListAdapter(this, WorkOnOffCommonFunction.LIST_TYPE_SBWorkAddActivity);
        RecyclerView workAddList = (RecyclerView) findViewById(R.id.workAddList);
        workAddList.setLayoutManager(new GridLayoutManager(this, 1));
        workAddList.setHasFixedSize(true);
        workAddList.setAdapter(listAdapter);


        this.setAds();

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if( intent != null ) {
            Bundle ext = intent.getExtras();
            if (ext != null) {
                type = ext.getString("type");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render(){
        List<WorkFavoriteItem> list = type.equals("출근") ? getLocalDBHelper().workonfavoriteSelectList() : getLocalDBHelper().workofffavoriteSelectList();
        alarm_add_empty.setVisibility(list.size() == 0 ? View.VISIBLE : View.GONE);
        alarm_add_desc.setVisibility(list.size() == 0 ? View.GONE : View.VISIBLE);
        listAdapter.render(list);
    }

    public void listAdd(WorkFavoriteItem obj){
        //listAdd(obj);
        if(type.equals("출근")) getLocalDBHelper().workonfavoriteAdd(obj.id);
        else getLocalDBHelper().workofffavoriteAdd(obj.id);

        //추가 성공 시 토스트 띄움
        Toast.makeText(this, type + "길 알림이 추가되었습니다.", Toast.LENGTH_SHORT).show();
        render();
    }
}
