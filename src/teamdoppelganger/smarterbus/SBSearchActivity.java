package teamdoppelganger.smarterbus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mocoplex.adlib.AdlibManager;
import com.mocoplex.adlib.platform.nativeads.AdlibNativeHelper;

import java.util.ArrayList;
import java.util.List;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseNewActivity;
import teamdoppelganger.smarterbus.util.common.SearchCommonFunction;

/**
 * Created by muune on 2017-09-28.
 */

public class SBSearchActivity extends SBBaseNewActivity implements View.OnClickListener {
    private int mType = Constants.BUS_TYPE;
    private boolean isSearchMode = false;

    private SearchCommonFunction.SearchAdapter listAdapter;
    public AdlibManager aManager;
    private AdlibNativeHelper nativeHelper = null;
    private TextView searchEdit;
    private RecyclerView recyclerView;

    private Button busBtn;
    private Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        findViewById(R.id.back).setOnClickListener(this);

        busBtn = (Button) findViewById(R.id.busBtn);
        stopBtn = (Button) findViewById(R.id.stopBtn);
        busBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        listAdapter = new SearchCommonFunction.SearchAdapter(this);
        recyclerView = (RecyclerView) findViewById(R.id.listView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(listAdapter);

        nativeHelper = this.getNativeHelper(recyclerView);
        aManager = this.getAdlibManager();

        searchEdit = (EditText) findViewById(R.id.searchEdit);
        searchEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        //키보드 내리게 적용
                        return true;
                    default:
                        return false;
                }
            }
        });
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //isKeyboardShow = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                //busAdapter.setFilter(filter(busTempList, s.toString()));
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.busBtn:
                tabClick(view.getId());
                break;
            case R.id.stopBtn:
                tabClick(view.getId());
                break;
        }
    }

    private void tabClick(int id) {
        mType = id == R.id.busBtn ? Constants.BUS_TYPE : Constants.STOP_TYPE;
        render();
    }

    @Override
    public void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        busBtn.setBackgroundColor(mType == Constants.BUS_TYPE ? Color.parseColor("#0099cb") : Color.parseColor("#EEEEEE"));
        stopBtn.setBackgroundColor(mType == Constants.BUS_TYPE ? Color.parseColor("#EEEEEE") : Color.parseColor("#0099cb"));

        if (isSearchMode) {

        } else { //최근검색기록
            // mType이 버스인지 정류장인지에 따라 다른 리스트를 보내준다.
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < 105; i++) {
                list.add(i);
            }
            listAdapter.render(list, mType, isSearchMode);
        }
        recyclerView.smoothScrollToPosition(0);
    }


}
