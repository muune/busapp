package teamdoppelganger.smarterbus;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.StaticCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.KeyboardCheckLayout;
import teamdoppelganger.smarterbus.util.widget.KeyboardCheckLayout.OnSoftKeyboardListener;
import teamdoppelganger.smarterbus.util.widget.TicketLayout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.admixer.AdAdapter;
import com.admixer.AdInfo;
import com.admixer.AdMixerManager;
import com.admixer.AdView;
import com.admixer.AdViewListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mocoplex.adlib.AdlibManager;
import com.smart.lib.CommonConstants;

public class SBEditFavoriteActivity extends SBBaseActivity implements OnClickListener, OnSoftKeyboardListener {

    TextView mNameTxt1, mNameTxt2;
    EditText mNameEditTxt1, mNameEditTxt2;
    TextView mNameEditSubTxt1;


    ImageView mColor1, mColor2, mColor3, mColor4, mColor5;
    ImageView mColorD1, mColorD2, mColorD3, mColorD4, mColorD5;
    ImageView mColorCheck1, mColorCheck2, mColorCheck3, mColorCheck4, mColorCheck5;

    TicketLayout mTicket;

    FavoriteAndHistoryItem mItem = null;

    LinearLayout mColorLayout, mColorLayout2;
    RelativeLayout mSubColorLayout1, mSubColorLayout2, mSubColorLayout3, mSubColorLayout4, mSubColorLayout5;


    //inTicketLayout
    ImageView mTicketColorView;
    TextView mName1Txt, mSubNAme1Txt;
    TextView mArrive1Txt, mArrive2Txt;
    TextView mDetailTxt;
    TextView mStopTicket;
    Button mRefreshBtn, mDelBtn;
    ImageView mBtnImg;
    TextView mOkBtn, mCancelBtn;


    int mSelectGroupIndex = 1;
    int mSelectChildeIndex = 1;
    int mChoiceGroupIndex = 1;
    int mWidgetId;

    int mWidgeMode = Constants.WIDGET_MODE_NOTHING;


    ArrayList<BusRouteItem> mBusRouteItemAry;

    HashMap<Integer, String> mTerminus;
    HashMap<Integer, String> mHashLocationKo;
    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mBusTypeHash;

    KeyboardCheckLayout mKeyboardCheckLayout;

    Handler mHandler;
    Runnable mRunnable;
    AdlibManager mAdlibManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_edit);

        mHandler = new Handler();
        findViewById(R.id.adlib).setVisibility(View.VISIBLE);

        mBusRouteItemAry = new ArrayList<BusRouteItem>();


        mTerminus = ((SBInforApplication) getApplicationContext()).mTerminus;
        mHashLocationKo = ((SBInforApplication) getApplicationContext()).mHashKoLocation;
        mHashLocationEng = ((SBInforApplication) getApplicationContext()).mHashLocation;
        mBusTypeHash = ((SBInforApplication) getApplicationContext()).mBusTypeHash;


        Tracker t = ((SBInforApplication) getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName("즐겨찾기 편집");
        t.send(new HitBuilders.AppViewBuilder().build());

        mItem = (FavoriteAndHistoryItem) getIntent().getExtras().getSerializable(Constants.INTENT_FAVORITEITEM);

        mWidgeMode = (int) getIntent().getExtras().getInt(Constants.WIDGET_MODE, Constants.WIDGET_MODE_NOTHING);

        if (mWidgeMode != Constants.WIDGET_MODE_NOTHING) {
            mWidgetId = getIntent().getExtras().getInt(Constants.WIDGET_ID);

        } else {

        }

        initView();
        setItem();

        if (mWidgeMode == Constants.WIDGET_MODE_STOP) {
            mTicket.setViewWidth(View.GONE);
            mStopTicket.setVisibility(View.VISIBLE);
            mStopTicket.setText(mItem.busStopItem.name);


        }


        mColorLayout.bringToFront();

        mRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    findViewById(R.id.ticketAndTxtLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.admixer_layout).setVisibility(View.VISIBLE);
                    findViewById(R.id.buttonLayout).setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }
                ;

            }
        };

    }


    private void setItem() {

        if (mItem != null) {

            if (mWidgeMode == Constants.WIDGET_MODE_STOP) {

                mNameTxt1.setText(getResources().getString(R.string.favorite_nick_stop));
                mNameEditTxt1.setText(mItem.nickName);


                mNameTxt2.setVisibility(View.GONE);
                mNameEditSubTxt1.setVisibility(View.GONE);
                mNameEditTxt2.setVisibility(View.GONE);


            } else {
                if (mItem.type == Constants.FAVORITE_TYPE_BUS) {


                    try {
                        String color = mBusTypeHash.get(mItem.busRouteItem.busType);
                        mName1Txt.setTextColor(Color.parseColor("#" + color));
                    } catch (Exception e) {
                        mName1Txt.setTextColor(Color.parseColor("#333333"));
                    }
                    ;


                    mName1Txt.setText(mItem.busRouteItem.busRouteName);
                    mNameEditSubTxt1.setText(mItem.busRouteItem.busRouteName);
                    mDetailTxt.setText(mItem.nickName);

                    mArrive1Txt.setVisibility(View.GONE);
                    mArrive2Txt.setVisibility(View.GONE);
                    mSubNAme1Txt.setVisibility(View.GONE);
                    mRefreshBtn.setVisibility(View.GONE);

                    mNameTxt1.setText(getResources().getString(R.string.favorite_nick_bus));
                    mNameTxt2.setText(getResources().getString(R.string.favorite_nick));

                    mNameEditTxt1.setVisibility(View.GONE);
                    mNameEditSubTxt1.setVisibility(View.VISIBLE);

                    mNameEditTxt2.setText(mItem.nickName);

                    mBtnImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_bus));

                } else if (mItem.type == Constants.FAVORITE_TYPE_STOP) {

                    mName1Txt.setText(mItem.nickName);
                    mDetailTxt.setText(mItem.nickName2);

                    mArrive1Txt.setVisibility(View.GONE);
                    mArrive2Txt.setVisibility(View.GONE);
                    mSubNAme1Txt.setVisibility(View.GONE);
                    mRefreshBtn.setVisibility(View.GONE);


                    mNameTxt1.setText(getResources().getString(R.string.favorite_nick_stop));
                    mNameTxt2.setText(getResources().getString(R.string.favorite_nick));

                    mNameEditTxt1.setText(mItem.nickName);
                    mNameEditTxt2.setText(mItem.nickName2);


                    mBtnImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop));

                } else if (mItem.type == Constants.FAVORITE_TYPE_BUS_STOP) {
                    mName1Txt.setText(mItem.busRouteItem.busRouteName);
                    mDetailTxt.setText(mItem.nickName);

                    mName1Txt.setVisibility(View.VISIBLE);
                    mArrive1Txt.setVisibility(View.GONE);
                    mArrive2Txt.setVisibility(View.GONE);
                    mSubNAme1Txt.setVisibility(View.GONE);
                    mRefreshBtn.setVisibility(View.VISIBLE);


                    mNameTxt1.setText(getResources().getString(R.string.favorite_nick_bus));
                    mNameTxt2.setText(getResources().getString(R.string.favorite_nick_stop));


                    mNameEditTxt2.setText(mItem.nickName);

                    mNameEditTxt1.setVisibility(View.GONE);
                    mNameEditSubTxt1.setVisibility(View.VISIBLE);
                    mNameEditSubTxt1.setText(mItem.busRouteItem.busRouteName);

                    mBtnImg.setImageDrawable(getResources().getDrawable(R.drawable.ic_busstop));


                    try {
                        String color = mBusTypeHash.get(mItem.busRouteItem.busType);
                        mName1Txt.setTextColor(Color.parseColor("#" + color));
                    } catch (Exception e) {
                        mName1Txt.setTextColor(Color.parseColor("#333333"));
                    }

                }

            }


        }

        if (mItem.color != null) {
            String[] splitIndex = mItem.color.split("_");
            mSelectGroupIndex = Integer.parseInt(splitIndex[0]);
            mChoiceGroupIndex = Integer.parseInt(splitIndex[0]);
            mSelectChildeIndex = Integer.parseInt(splitIndex[1]);
        } else {

            boolean isEnd = false;
            for (int i = 1; i < 6; i++) {
                for (int j = 1; j < 6; j++) {
                    String findSql = String.format("SELECT *FROM %s where color='%s'", LocalDBHelper.TABLE_FAVORITE_NAME, String.valueOf(i + "_" + j));
                    Cursor findCursor = getLocalDBHelper().getReadableDatabase().rawQuery(findSql, null);
                    if (findCursor.getCount() == 0) {

                        mSelectGroupIndex = i;
                        mChoiceGroupIndex = i;
                        mSelectChildeIndex = j;

                        isEnd = true;
                        break;
                    }
                    findCursor.close();
                }
                if (isEnd) {
                    break;
                }
            }


        }


        setGroupColor(mSelectGroupIndex);

    }


    private void initView() {


        mNameTxt1 = (TextView) findViewById(R.id.edit_name1);
        mNameTxt2 = (TextView) findViewById(R.id.edit_name2);


        mNameEditTxt1 = (EditText) findViewById(R.id.nameEdit1);
        mNameEditTxt2 = (EditText) findViewById(R.id.nameEdit2);

        mNameEditSubTxt1 = (TextView) findViewById(R.id.nameEdit1_SUB);
        mStopTicket = (TextView) findViewById(R.id.stopTicket);

        mColor1 = (ImageView) findViewById(R.id.color1);
        mColor2 = (ImageView) findViewById(R.id.color2);
        mColor3 = (ImageView) findViewById(R.id.color3);
        mColor4 = (ImageView) findViewById(R.id.color4);
        mColor5 = (ImageView) findViewById(R.id.color5);

        mColorD1 = (ImageView) findViewById(R.id.subColor1);
        mColorD2 = (ImageView) findViewById(R.id.subColor2);
        mColorD3 = (ImageView) findViewById(R.id.subColor3);
        mColorD4 = (ImageView) findViewById(R.id.subColor4);
        mColorD5 = (ImageView) findViewById(R.id.subColor5);


        mColorCheck1 = (ImageView) findViewById(R.id.subColor1_1);
        mColorCheck2 = (ImageView) findViewById(R.id.subColor2_1);
        mColorCheck3 = (ImageView) findViewById(R.id.subColor3_1);
        mColorCheck4 = (ImageView) findViewById(R.id.subColor4_1);
        mColorCheck5 = (ImageView) findViewById(R.id.subColor5_1);

        mKeyboardCheckLayout = (KeyboardCheckLayout) findViewById(R.id.keyboardCheck2);
        mKeyboardCheckLayout.setOnSoftKeyboardListener(this);


        mColorLayout = (LinearLayout) findViewById(R.id.colorSelectLayout1);
        mColorLayout2 = (LinearLayout) findViewById(R.id.colorLayout2);

        mSubColorLayout1 = (RelativeLayout) findViewById(R.id.subColorLayout1);
        mSubColorLayout2 = (RelativeLayout) findViewById(R.id.subColorLayout2);
        mSubColorLayout3 = (RelativeLayout) findViewById(R.id.subColorLayout3);
        mSubColorLayout4 = (RelativeLayout) findViewById(R.id.subColorLayout4);
        mSubColorLayout5 = (RelativeLayout) findViewById(R.id.subColorLayout5);


        mOkBtn = (TextView) findViewById(R.id.ok);
        mCancelBtn = (TextView) findViewById(R.id.cancel);

        mTicket = (TicketLayout) findViewById(R.id.ticket);


        mTicketColorView = (ImageView) findViewById(R.id.backColor);
        mName1Txt = (TextView) findViewById(R.id.name1);
        mSubNAme1Txt = (TextView) findViewById(R.id.subname1);
        mArrive1Txt = (TextView) findViewById(R.id.arrive_1);
        mArrive2Txt = (TextView) findViewById(R.id.arrive_2);
        mDetailTxt = (TextView) findViewById(R.id.detail);
        mRefreshBtn = (Button) findViewById(R.id.refreshBtn);
        mDelBtn = (Button) findViewById(R.id.delBtn);
        mBtnImg = (ImageView) findViewById(R.id.btn_image);


        mColor1.setOnClickListener(this);
        mColor2.setOnClickListener(this);
        mColor3.setOnClickListener(this);
        mColor4.setOnClickListener(this);
        mColor5.setOnClickListener(this);


        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated

        int ticketWidth = (int) (width / 2 - getResources().getDimension(R.dimen.ticket_margin));

        if (mWidgeMode != Constants.WIDGET_MODE_STOP) {
            mTicket.setViewWidth(ticketWidth);
            mTicket.invalidate();
            mTicket.requestLayout();
        } else {
            for (int i = 0; i < mTicket.getChildCount(); i++) {
                mTicket.getChildAt(i).setVisibility(View.GONE);
            }

        }


        mOkBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mWidgeMode != Constants.WIDGET_MODE_NOTHING) {

                    if (mWidgeMode == Constants.WIDGET_MODE_BUS_STOP) {
                        FavoriteAndHistoryItem item = mItem;
                        item.color = String.valueOf(mChoiceGroupIndex + "_" + mSelectChildeIndex);
                        item.nickName = mNameEditTxt2.getText().toString();

                        FileOutputStream fos;
                        try {
                            fos = openFileOutput(String.valueOf(mWidgetId) + "_type1", Context.MODE_PRIVATE);
                            ObjectOutputStream os = new ObjectOutputStream(fos);
                            os.writeObject(item);
                            os.close();
                            fos.close();

                            Intent intent = new Intent();
                            intent.putExtra(Constants.WIDGET_SUCCESS, true);
                            setResult(Constants.WIDGET_MODE_BUS_STOP, intent);


                            finish();

                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        //정류소 위젯

                        mItem.color = String.valueOf(mChoiceGroupIndex + "_" + mSelectChildeIndex);
                        mItem.nickName = mNameEditTxt1.getText().toString();


                        Intent intent = new Intent();
                        intent.putExtra(Constants.WIDGET_SUCCESS, true);
                        intent.putExtra(Constants.INTENT_FAVORITEITEM, mItem);
                        setResult(Constants.WIDGET_MODE_STOP, intent);

                        finish();

                    }


                } else {
                    if (mItem.type == Constants.FAVORITE_TYPE_STOP) {

                        if (mNameEditTxt1.getText().length() > 0) {
                            mItem.nickName = mNameEditTxt1.getText().toString();

                        }


                        if (mNameEditTxt2.getText().length() > 0) {
                            mItem.nickName2 = mNameEditTxt2.getText().toString();
                        }


                        if (mItem.id == -1) {
                            new StaticCommonFuction().inputFavorteAndRecent(getBusDbSqlite(), getLocalDBHelper(),
                                    true, mItem.busStopItem.localInfoId, Constants.STOP_TYPE, "", "", mItem.busStopItem.apiId, mItem.busStopItem.arsId,
                                    String.valueOf(mChoiceGroupIndex + "_" + mSelectChildeIndex), mItem.nickName, mItem.nickName2, mItem.busStopItem.tempId2, "");

                            Toast.makeText(SBEditFavoriteActivity.this, "즐겨 찾기에 추가 되었습니다", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();


                        }


                    } else if (mItem.type == Constants.FAVORITE_TYPE_BUS_STOP) {

                        mItem.nickName = mNameEditTxt2.getText().toString();

                        if (mItem.id == -1) {

                            boolean isPass = false;

                            if (mItem.busRouteItem.localInfoId.equals(String.valueOf(CommonConstants.CITY_SEO_UL._cityId))) {
                                if (mItem.busRouteItem.direction != null) {
                                    new StaticCommonFuction().inputFavorteAndRecent(getBusDbSqlite(), getLocalDBHelper(),
                                            true, mItem.busRouteItem.localInfoId, Constants.FAVORITE_TYPE_BUS_STOP, mItem.busRouteItem.busRouteApiId, mItem.busRouteItem.busRouteApiId2,
                                            mItem.busRouteItem.busStopApiId, mItem.busRouteItem.busStopArsId, String.valueOf(mChoiceGroupIndex + "_" + mSelectChildeIndex), mItem.nickName, mItem.nickName2,
                                            mItem.busRouteItem.tmpId, mItem.busRouteItem.direction);
                                    isPass = true;
                                }
                            }


                            if (!isPass) {
                                new StaticCommonFuction().inputFavorteAndRecent(getBusDbSqlite(), getLocalDBHelper(),
                                        true, mItem.busRouteItem.localInfoId, Constants.FAVORITE_TYPE_BUS_STOP, mItem.busRouteItem.busRouteApiId, mItem.busRouteItem.busRouteApiId2,
                                        mItem.busRouteItem.busStopApiId, mItem.busRouteItem.busStopArsId, String.valueOf(mChoiceGroupIndex + "_" + mSelectChildeIndex), mItem.nickName, mItem.nickName2,
                                        mItem.busRouteItem.tmpId, "");
                            }

                            Toast.makeText(SBEditFavoriteActivity.this, "즐겨 찾기에 추가 되었습니다", Toast.LENGTH_SHORT).show();


                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();

                        }


                    } else {

                        mItem.nickName = mNameEditTxt2.getText().toString();


                        if (mItem.id == -1) {
                            new StaticCommonFuction().inputFavorteAndRecent(getBusDbSqlite(), getLocalDBHelper(),
                                    true, mItem.busRouteItem.localInfoId, Constants.BUS_TYPE, mItem.busRouteItem.busRouteApiId, mItem.busRouteItem.busRouteApiId2,
                                    "", "", String.valueOf(mChoiceGroupIndex + "_" + mSelectChildeIndex), mItem.nickName, mItem.nickName2, "", "");

                            Toast.makeText(SBEditFavoriteActivity.this, "즐겨 찾기에 추가 되었습니다", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }


                    }


                    if (mItem.id != -1) {
                        getLocalDBHelper().updateFavorite(mItem.id, mItem.nickName, mItem.nickName2, String.valueOf(mSelectGroupIndex + "_" + mSelectChildeIndex));
                    }

                    finish();


                }


            }
        });


        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra(Constants.WIDGET_SUCCESS, false);


                finish();

            }
        });


        mNameEditTxt1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s.toString();
                mName1Txt.setText(s.toString());
                if (mWidgeMode == Constants.WIDGET_MODE_STOP) {
                    mStopTicket.setText(s.toString());
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });


        mNameEditTxt2.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                s.toString();
                mDetailTxt.setText(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });


        mNameEditTxt1.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == event.KEYCODE_ENTER
                        && mNameEditTxt2.getVisibility() == View.GONE) {

                    if (mHandler != null && mRunnable != null) {
                        mHandler.postDelayed(mRunnable, 200);
                    }
                }

                return false;
            }
        });


        mNameEditTxt2.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == event.KEYCODE_ENTER) {

                    if (mHandler != null && mRunnable != null) {
                        mHandler.postDelayed(mRunnable, 200);
                    }
                }

                return false;
            }
        });


    }


    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.color1:
                setGroupColor(1);

                break;
            case R.id.color2:
                setGroupColor(2);


                break;
            case R.id.color3:

                setGroupColor(3);
                break;
            case R.id.color4:
                setGroupColor(4);

                break;
            case R.id.color5:
                setGroupColor(5);

                break;

            case R.id.subColorLayout1:
                mChoiceGroupIndex = mSelectGroupIndex;
                setChildSelect(1);
                break;
            case R.id.subColorLayout2:
                mChoiceGroupIndex = mSelectGroupIndex;
                setChildSelect(2);
                break;
            case R.id.subColorLayout3:
                mChoiceGroupIndex = mSelectGroupIndex;
                setChildSelect(3);
                break;
            case R.id.subColorLayout4:
                mChoiceGroupIndex = mSelectGroupIndex;
                setChildSelect(4);

                break;
            case R.id.subColorLayout5:
                mChoiceGroupIndex = mSelectGroupIndex;
                setChildSelect(5);

                break;
        }


    }


    public void setGroupColor(int index) {

        if (index == 1) {
            mColor1.setBackgroundResource(R.drawable.color_box_top);
            mColor2.setBackgroundResource(0);
            mColor3.setBackgroundResource(0);
            mColor4.setBackgroundResource(0);
            mColor5.setBackgroundResource(0);
            setChildColor(1);
            setChildSelect(mSelectChildeIndex);
        } else if (index == 2) {
            mColor2.setBackgroundResource(R.drawable.color_box_top);
            mColor1.setBackgroundResource(0);
            mColor3.setBackgroundResource(0);
            mColor4.setBackgroundResource(0);
            mColor5.setBackgroundResource(0);
            setChildColor(2);
            setChildSelect(mSelectChildeIndex);
        } else if (index == 3) {
            mColor3.setBackgroundResource(R.drawable.color_box_top);
            mColor2.setBackgroundResource(0);
            mColor1.setBackgroundResource(0);
            mColor4.setBackgroundResource(0);
            mColor5.setBackgroundResource(0);
            setChildColor(3);
            setChildSelect(mSelectChildeIndex);
        } else if (index == 4) {
            mColor4.setBackgroundResource(R.drawable.color_box_top);
            mColor2.setBackgroundResource(0);
            mColor3.setBackgroundResource(0);
            mColor1.setBackgroundResource(0);
            mColor5.setBackgroundResource(0);
            setChildColor(4);
            setChildSelect(mSelectChildeIndex);
        } else if (index == 5) {
            mColor5.setBackgroundResource(R.drawable.color_box_top);
            mColor2.setBackgroundResource(0);
            mColor3.setBackgroundResource(0);
            mColor4.setBackgroundResource(0);
            mColor1.setBackgroundResource(0);
            setChildColor(5);
            setChildSelect(mSelectChildeIndex);
        }


    }

    public void setChildColor(int index) {

        mSelectGroupIndex = index;
        boolean[] isAlreadySelected = new boolean[5];

        for (int j = 1; j < 6; j++) {


            if ((mSelectGroupIndex + "_" + mSelectChildeIndex).equals(mSelectGroupIndex + "_" + j)) {
                isAlreadySelected[j - 1] = false;
            } else {

                String findSql = String.format("SELECT *FROM %s where color='%s'", LocalDBHelper.TABLE_FAVORITE_NAME, String.valueOf(mSelectGroupIndex + "_" + j));
                Cursor findCursor = getLocalDBHelper().getReadableDatabase().rawQuery(findSql, null);

                if (findCursor.moveToNext()) {
                    isAlreadySelected[j - 1] = true;
                } else {
                    isAlreadySelected[j - 1] = false;
                }

                findCursor.close();
            }

        }


        String packName = this.getPackageName(); // 패키지명
        for (int i = 1; i < 6; i++) {

            String resName = String.format("color_sub%s_%s", index, i);
            String resDisableName = String.format("color_sub%s_%s_disabled", index, i);

            int resID = getResources().getIdentifier(resName, "drawable", packName);
            int resDisableID = getResources().getIdentifier(resDisableName, "drawable", packName);

            //Drawable drawable = getResources().getDrawable(resID);
            if (i == 1) {
                if (isAlreadySelected[i - 1]) {
                    mColorD1.setImageResource(resDisableID);
                    mSubColorLayout1.setOnClickListener(null);
                } else {
                    mColorD1.setImageResource(resID);
                    mSubColorLayout1.setOnClickListener(this);
                }
            } else if (i == 2) {
                if (isAlreadySelected[i - 1]) {
                    mColorD2.setImageResource(resDisableID);
                    mSubColorLayout2.setOnClickListener(null);
                } else {
                    mColorD2.setImageResource(resID);
                    mSubColorLayout2.setOnClickListener(this);
                }
            } else if (i == 3) {
                if (isAlreadySelected[i - 1]) {
                    mColorD3.setImageResource(resDisableID);
                    mSubColorLayout3.setOnClickListener(null);
                } else {
                    mColorD3.setImageResource(resID);
                    mSubColorLayout3.setOnClickListener(this);
                }
            } else if (i == 4) {
                if (isAlreadySelected[i - 1]) {
                    mColorD4.setImageResource(resDisableID);
                    mSubColorLayout4.setOnClickListener(null);
                } else {
                    mColorD4.setImageResource(resID);
                    mSubColorLayout4.setOnClickListener(this);
                }
            } else if (i == 5) {
                if (isAlreadySelected[i - 1]) {
                    mColorD5.setImageResource(resDisableID);
                    mSubColorLayout5.setOnClickListener(null);
                } else {
                    mColorD5.setImageResource(resID);
                    mSubColorLayout5.setOnClickListener(this);
                }
            }


        }
    }


    public void setChildSelect(int index) {


        if (mSelectGroupIndex != mChoiceGroupIndex) {

            mColorCheck1.setImageBitmap(null);
            mColorCheck2.setImageBitmap(null);
            mColorCheck3.setImageBitmap(null);
            mColorCheck4.setImageBitmap(null);
            mColorCheck5.setImageBitmap(null);

            return;
        }


        if (index == 1) {
            mColorCheck1.setImageResource(R.drawable.ic_color_sub_focused);
            mColorCheck2.setImageBitmap(null);
            mColorCheck3.setImageBitmap(null);
            mColorCheck4.setImageBitmap(null);
            mColorCheck5.setImageBitmap(null);
            mSelectChildeIndex = 1;


        } else if (index == 2) {
            mColorCheck2.setImageResource(R.drawable.ic_color_sub_focused);
            mColorCheck1.setImageBitmap(null);
            mColorCheck3.setImageBitmap(null);
            mColorCheck4.setImageBitmap(null);
            mColorCheck5.setImageBitmap(null);
            mSelectChildeIndex = 2;
        } else if (index == 3) {
            mColorCheck3.setImageResource(R.drawable.ic_color_sub_focused);
            mColorCheck1.setImageBitmap(null);
            mColorCheck2.setImageBitmap(null);
            mColorCheck4.setImageBitmap(null);
            mColorCheck5.setImageBitmap(null);
            mSelectChildeIndex = 3;
        } else if (index == 4) {
            mColorCheck4.setImageResource(R.drawable.ic_color_sub_focused);
            mColorCheck1.setImageBitmap(null);
            mColorCheck2.setImageBitmap(null);
            mColorCheck3.setImageBitmap(null);
            mColorCheck5.setImageBitmap(null);
            mSelectChildeIndex = 4;
        } else if (index == 5) {
            mColorCheck5.setImageResource(R.drawable.ic_color_sub_focused);
            mColorCheck1.setImageBitmap(null);
            mColorCheck2.setImageBitmap(null);
            mColorCheck3.setImageBitmap(null);
            mColorCheck4.setImageBitmap(null);
            mSelectChildeIndex = 5;
        } else {


            mColorCheck1.setImageBitmap(null);
            mColorCheck2.setImageBitmap(null);
            mColorCheck3.setImageBitmap(null);
            mColorCheck4.setImageBitmap(null);
            mColorCheck5.setImageBitmap(null);

        }

        if (mWidgeMode != Constants.WIDGET_MODE_STOP) {
            setTicketColor();
        } else {
            setStopTicketColor();
        }
    }

    public void setStopTicketColor() {
        String packName = this.getPackageName(); // 패키지명

        String resName = String.format("widget_top%s", String.valueOf(mSelectGroupIndex + "_" + mSelectChildeIndex));
        int resID = getResources().getIdentifier(resName, "drawable", packName);


        mStopTicket.setBackgroundDrawable(getResources().getDrawable(resID));


    }

    public void setTicketColor() {

        String packName = this.getPackageName(); // 패키지명

        String resName = String.format("tag_%s", String.valueOf(mSelectGroupIndex + "_" + mSelectChildeIndex));
        int resID = getResources().getIdentifier(resName, "drawable", packName);

        mTicket.changeSetColor(resID);
    }


    @Override
    public void onShown() {
        findViewById(R.id.ticketAndTxtLayout).setVisibility(View.GONE);
        findViewById(R.id.admixer_layout).setVisibility(View.GONE);
        findViewById(R.id.buttonLayout).setVisibility(View.GONE);

    }


    @Override
    public void onHidden() {

        if (mHandler != null && mRunnable != null) {
            mHandler.postDelayed(mRunnable, 200);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

}

