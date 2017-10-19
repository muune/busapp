package teamdoppelganger.smarterbus;

import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.smart.lib.CommonConstants;

@SuppressLint("ValidFragment")
public class SBRecentSearchFragment extends SBBaseFragment {

    ArrayList<BusRouteItem> mBusRouteItem;
    ArrayList<BusStopItem> mBusStopItem;


    Button mRecentStopBtn, mRecentRouteBtn;
    TextView mEditBtn, mPartDelBtn;
    TextView mNothingContents;
    RecentAdapter mRecentAdapter;

    Runnable mRunnable;
    ListView mList;

    ArrayList<FavoriteAndHistoryItem> mFavoriteAndHistoryItem = new ArrayList<FavoriteAndHistoryItem>();
    HashMap<Integer, String> mTerminus;
    HashMap<Integer, String> mHashLocationKo;
    HashMap<Integer, String> mBusTypeHash;


    CustomAsync mCustomAsync;
    CheckBox mAllCheckBtn;


    //boolean mIsBusMode =false;
    boolean mIsEditAble = false;

    int mType = Constants.STOP_TYPE;


    public SBRecentSearchFragment(int id, SQLiteDatabase db,
                                  LocalDBHelper localDBHelper) {
        super(R.layout.recent, db, localDBHelper);


    }

    public SBRecentSearchFragment() {

    }

    @Override
    public void onLayoutFinish(View view) {
        super.onLayoutFinish(view);

        mTerminus = ((SBInforApplication) getActivity().getApplicationContext()).mTerminus;
        mHashLocationKo = ((SBInforApplication) getActivity().getApplicationContext()).mHashKoLocation;
        mBusTypeHash = ((SBInforApplication) getActivity().getApplicationContext()).mBusTypeHash;


        mBusRouteItem = new ArrayList<BusRouteItem>();
        mBusStopItem = new ArrayList<BusStopItem>();


        mRecentAdapter = new RecentAdapter(mType);
        initView(view);
        mList.setAdapter(mRecentAdapter);

        mCustomAsync = new CustomAsync();
        mCustomAsync.execute();
        mRecentStopBtn.setSelected(true);

    }

    private void initView(View view) {

        mRecentRouteBtn = (Button) view.findViewById(R.id.recentBusBtn);
        mRecentStopBtn = (Button) view.findViewById(R.id.recentStopBtn);
        mEditBtn = (TextView) view.findViewById(R.id.editBtn);
        mPartDelBtn = (TextView) view.findViewById(R.id.partDelBtn);
        mList = (ListView) view.findViewById(R.id.list);
        mAllCheckBtn = (CheckBox) view.findViewById(R.id.allCheck);

        mNothingContents = (TextView) view.findViewById(R.id.nothingContentsRecent);


        mList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {

                if (mType == Constants.BUS_TYPE) {

                    if (mIsEditAble) {

                        mBusRouteItem.get(position).isChecked = !mBusRouteItem.get(position).isChecked;
                        mRecentAdapter.notifyDataSetChanged();

                    } else {
                        BusRouteItem busRouteItem = mBusRouteItem.get(position);
                        Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                        sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSROUTEITEM);
                        sendIntent.putExtra("busInfor", busRouteItem);
                        startActivity(sendIntent);

                    }


                } else {
                    if (mIsEditAble) {
                        mBusStopItem.get(position).isChecked = !mBusStopItem.get(position).isChecked;
                        mRecentAdapter.notifyDataSetChanged();
                    } else {
                        BusStopItem busStopItem = mBusStopItem.get(position);
                        Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                        sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                        sendIntent.putExtra("busInfor", busStopItem);
                        startActivity(sendIntent);
                    }

                }
            }
        });

        mRecentRouteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mType = Constants.BUS_TYPE;
                mRecentStopBtn.setSelected(false);
                mRecentRouteBtn.setSelected(true);
                mAllCheckBtn.setChecked(false);
                if (mCustomAsync != null) {
                    mCustomAsync.cancel(true);
                }

                mCustomAsync = new CustomAsync();
                mCustomAsync.execute();
                playVibe();
            }
        });

        mRecentStopBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mType = Constants.STOP_TYPE;

                mRecentStopBtn.setSelected(true);
                mRecentRouteBtn.setSelected(false);
                mAllCheckBtn.setChecked(false);

                if (mCustomAsync != null) {
                    mCustomAsync.cancel(true);
                }

                mCustomAsync = new CustomAsync();
                mCustomAsync.execute();
                playVibe();
            }
        });


        mEditBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mIsEditAble) {
                    mIsEditAble = false;
                    mPartDelBtn.setVisibility(View.GONE);
                    mAllCheckBtn.setVisibility(View.GONE);

                    mEditBtn.setText("편집");
                    mAllCheckBtn.setChecked(false);
                    mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit), null, null, null);

                    mRecentAdapter.notifyDataSetChanged();
                } else {
                    mIsEditAble = true;
                    mPartDelBtn.setVisibility(View.VISIBLE);
                    mAllCheckBtn.setVisibility(View.VISIBLE);

                    mEditBtn.setText("완료");
                    mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_end), null, null, null);

                    mRecentAdapter.notifyDataSetChanged();
                }

                playVibe();

            }
        });


        mAllCheckBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mType == Constants.BUS_TYPE) {

                    for (int i = 0; i < mBusRouteItem.size(); i++) {
                        mBusRouteItem.get(i).isChecked = isChecked;
                        mRecentAdapter.notifyDataSetChanged();
                    }

                } else {
                    for (int i = 0; i < mBusStopItem.size(); i++) {
                        mBusStopItem.get(i).isChecked = isChecked;
                        mRecentAdapter.notifyDataSetChanged();
                    }

                }

                if (isChecked) {


                } else {


                }


            }
        });


        mPartDelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                playVibe();


                boolean isDeleteAble = false;
                for (int i = 0; i < mBusRouteItem.size(); i++) {
                    if (mBusRouteItem.get(i).isChecked) {
                        isDeleteAble = true;
                        break;
                    }
                }

                if (!isDeleteAble) {
                    for (int i = 0; i < mBusStopItem.size(); i++) {
                        if (mBusStopItem.get(i).isChecked) {
                            isDeleteAble = true;
                            break;
                        }
                    }
                }

                if (!isDeleteAble) {
                    Toast.makeText(getActivity(), "선택한 삭제 목록이 없습니다.", 1000).show();
                    return;
                }


                String routeOrStop = (mType == Constants.BUS_TYPE) ? "버스" : "정류장";
                final SBDialog dialog = new SBDialog(getActivity());
                dialog.setViewLayout("선택한 " + routeOrStop + "를 삭제하시겠습니까?");
                dialog.getPositiveButton("삭제").setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (mType == Constants.BUS_TYPE) {
                            //mBus

                            ArrayList<BusRouteItem> tempBusRouteItem = new ArrayList<BusRouteItem>();

                            for (int i = 0; i < mBusRouteItem.size(); i++) {
                                if (mBusRouteItem.get(i).isChecked) {

                                    tempBusRouteItem.add(mBusRouteItem.get(i));
                                }

                            }


                            for (int i = 0; i < tempBusRouteItem.size(); i++) {
                                mBusRouteItem.remove(tempBusRouteItem.get(i));
                                getLocalDBHelper().deleteHistory(Integer.parseInt(tempBusRouteItem.get(i).tmpId));
                            }


                        } else {

                            ArrayList<BusStopItem> tmpBusStopItem = new ArrayList<BusStopItem>();

                            for (int i = 0; i < mBusStopItem.size(); i++) {
                                if (mBusStopItem.get(i).isChecked) {

                                    tmpBusStopItem.add(mBusStopItem.get(i));
                                }
                            }

                            for (int i = 0; i < tmpBusStopItem.size(); i++) {
                                mBusStopItem.remove(tmpBusStopItem.get(i));
                                getLocalDBHelper().deleteHistory(Integer.parseInt(tmpBusStopItem.get(i).tempId));
                            }

                        }


                        mRecentAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }

                });
                dialog.getNegativeButton("취소").setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
    }

    private void getRecentData() {


    }

    class RecentAdapter extends BaseAdapter {

        LayoutInflater _inflater;
        int _type = 0;

        public RecentAdapter(int type) {
            _type = type;
            _inflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setType(int type) {
            _type = type;
        }

        @Override
        public int getCount() {

            if (_type == Constants.BUS_TYPE) {
                return mBusRouteItem.size();
            } else if (_type == Constants.STOP_TYPE) {
                return mBusStopItem.size();
            }

            return 0;

        }

        @Override
        public Object getItem(int position) {

            if (_type == Constants.BUS_TYPE) {
                return mBusRouteItem.get(position);
            } else if (_type == Constants.STOP_TYPE) {
                return mBusStopItem.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            ViewHolder viewHolder = null;

            if (v == null) {

                v = _inflater.inflate(R.layout.favorite_row, null);

                viewHolder = new ViewHolder();
                viewHolder.locationNameText = (TextView) v.findViewById(R.id.locationName);
                viewHolder.nameTxt = (TextView) v.findViewById(R.id.name);
                viewHolder.subNameTxt = (TextView) v.findViewById(R.id.subname);
                viewHolder.delCheck = (CheckBox) v.findViewById(R.id.checkBox);


                v.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) v.getTag();
            }

            if (_type == Constants.BUS_TYPE) {
                final BusRouteItem busRouteItem = mBusRouteItem.get(position);

                viewHolder.locationNameText.setText(mHashLocationKo.get(Integer.parseInt(busRouteItem.localInfoId)));
                viewHolder.nameTxt.setText(busRouteItem.busRouteName);
                viewHolder.subNameTxt.setVisibility(View.VISIBLE);
                viewHolder.subNameTxt.setText(mTerminus.get(Integer.parseInt(busRouteItem.startStop)) + "↔" + mTerminus.get(Integer.parseInt(busRouteItem.endStop)));


                if (busRouteItem.isChecked) {
                    viewHolder.delCheck.setChecked(true);
                } else {
                    viewHolder.delCheck.setChecked(false);
                }


                try {
                    String color = mBusTypeHash.get(busRouteItem.busType);
                    viewHolder.nameTxt.setTextColor(Color.parseColor("#" + color));
                } catch (Exception e) {
                    viewHolder.nameTxt.setTextColor(Color.parseColor("#333333"));
                }
                ;

                viewHolder.delCheck.setFocusableInTouchMode(false);
                viewHolder.delCheck.setClickable(false);


            } else if (_type == Constants.STOP_TYPE) {
                final BusStopItem busStopItem = mBusStopItem.get(position);

                viewHolder.locationNameText.setText(mHashLocationKo.get(Integer.parseInt(busStopItem.localInfoId)));
                viewHolder.nameTxt.setText(busStopItem.name);
                if (busStopItem.arsId == null || busStopItem.arsId.length() == 0) {
                    viewHolder.subNameTxt.setVisibility(View.GONE);
                } else {
                    viewHolder.subNameTxt.setVisibility(View.VISIBLE);
                    viewHolder.subNameTxt.setText(busStopItem.arsId);
                }


                try {

                    viewHolder.nameTxt.setTextColor(Color.parseColor("#333333"));
                } catch (Exception e) {
                }


                if (busStopItem.isChecked) {
                    viewHolder.delCheck.setChecked(true);
                } else {
                    viewHolder.delCheck.setChecked(false);
                }

                viewHolder.delCheck.setFocusableInTouchMode(false);
                viewHolder.delCheck.setClickable(false);

            }


            if (mIsEditAble) {
                viewHolder.delCheck.setVisibility(View.VISIBLE);
            } else {
                viewHolder.delCheck.setVisibility(View.GONE);
            }


            return v;
        }

        class ViewHolder {
            TextView nameTxt, subNameTxt;
            TextView locationNameText;
            CheckBox delCheck;
        }

    }


    @Override
    public void selectedPage() {
        super.selectedPage();

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_recent_search));
        t.send(new HitBuilders.AppViewBuilder().build());

        if (mCustomAsync != null) {
            mCustomAsync.cancel(true);
        }


        mCustomAsync = new CustomAsync();
        mCustomAsync.execute();

    }


    @Override
    public void unSelectedPage() {
        super.unSelectedPage();


        if (mCustomAsync != null) {
            mCustomAsync.cancel(true);
        }


        if (mIsEditAble) {
            mPartDelBtn.setVisibility(View.GONE);
            mAllCheckBtn.setVisibility(View.GONE);

            mIsEditAble = false;

            if (mEditBtn != null) {
                mEditBtn.setText("편집");
                mEditBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.btn_edit), null, null, null);
            }

            mRecentAdapter.notifyDataSetChanged();
        }


    }


    class CustomAsync extends AsyncTask<String, String, String> {


        ArrayList<BusRouteItem> tmpBusRouteItem;
        ArrayList<BusStopItem> tmpBusStopItem;


        @Override
        protected String doInBackground(String... params) {

            try {

                tmpBusRouteItem = new ArrayList<BusRouteItem>();
                tmpBusStopItem = new ArrayList<BusStopItem>();

                Cursor cursor = getLocalDBHelper().getHistoryValue(mType);
                int count = 0;
                while (cursor.moveToNext()) {

                    int _id = cursor.getInt(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_ID_F));
                    int cityId = cursor.getInt(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_CITY_F));

                    String type = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TYPE_F));
                    String typeId1 = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TYPEID_F));
                    String typeId2 = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TYPEID2_F));
                    String typeId3 = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TYPEID3_F));
                    String typeId4 = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TYPEID4_F));

                    String nick = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TYPE_NICK));
                    String color = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_COLOR_F));

                    String temp1 = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TEMP1));

                    String temp2 = cursor.getString(cursor
                            .getColumnIndex(LocalDBHelper.TABLE_TEMP2));


                    String cityEnName = ((SBInforApplication) getActivity()
                            .getApplicationContext()).mHashLocation.get(cityId);


                    if (type.equals(String.valueOf(Constants.BUS_TYPE))) {

                        String sql = "";

                        if (cityEnName.equals("CHILGOK")) {
                            sql = String.format(
                                    "Select * From %s where %s='%s' and %s='%s' and %s='%s'",
                                    cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1,
                                    typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2, CommonConstants.BUS_ROUTE_NAME, nick);
                        } else {
                            sql = String.format(
                                    "Select * From %s where %s='%s' and %s='%s'",
                                    cityEnName + "_route", CommonConstants.BUS_ROUTE_ID1,
                                    typeId1, CommonConstants.BUS_ROUTE_ID2, typeId2);
                        }
                        Cursor busCursor = getBusDbSqlite().rawQuery(sql, null);

                        if (busCursor.moveToNext()) {
                            BusRouteItem routeItem = new BusRouteItem();
                            routeItem._id = Integer.parseInt(busCursor.getString(busCursor.getColumnIndex(CommonConstants._ID)));
                            routeItem.tmpId = String.valueOf(_id);
                            routeItem.busRouteApiId = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                            routeItem.busRouteApiId2 = busCursor
                                    .getString(busCursor
                                            .getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                            routeItem.busRouteName = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                            routeItem.busRouteSubName = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                            routeItem.localInfoId = String.valueOf(cityId);
                            routeItem.busType = busCursor.getInt(busCursor
                                    .getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));

                            routeItem.startStop = String.valueOf(busCursor.getInt(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID)));
                            routeItem.endStop = String.valueOf(busCursor.getInt(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID)));

                            tmpBusRouteItem.add(routeItem);
                        }

                        busCursor.close();
                    } else if (type.equals(String.valueOf(Constants.STOP_TYPE))) {

                        String sql = "";

                        if (cityEnName.equals("BUSAN")) {
                            sql = String.format(
                                    "Select * From %s where %s='%s'",
                                    cityEnName + "_stop", CommonConstants.BUS_STOP_DESC, temp1);

                        } else {
                            sql = String.format(
                                    "Select * From %s where %s='%s' and %s='%s'",
                                    cityEnName + "_stop", CommonConstants.BUS_STOP_API_ID,
                                    typeId3, CommonConstants.BUS_STOP_ARS_ID, typeId4);
                        }

                        Cursor busCursor = getBusDbSqlite().rawQuery(sql, null);

                        if (busCursor.moveToNext()) {

                            BusStopItem stopItem = new BusStopItem();
                            stopItem.tempId = String.valueOf(_id);
                            stopItem.apiId = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                            stopItem.arsId = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                            stopItem.name = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_STOP_NAME));
                            stopItem.localInfoId = String.valueOf(cityId);
                            stopItem.tempId2 = busCursor.getString(busCursor
                                    .getColumnIndex(CommonConstants.BUS_STOP_DESC));
                            stopItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));


                            tmpBusStopItem.add(stopItem);
                        }

                        busCursor.close();
                    }

                    count++;


                }
                cursor.close();
            } catch (Exception e) {

            }

            getRecentData();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mBusStopItem.clear();
            mBusRouteItem.clear();

            mBusRouteItem.addAll(tmpBusRouteItem);
            mBusStopItem.addAll(tmpBusStopItem);

            mRecentAdapter.setType(mType);
            mRecentAdapter.notifyDataSetChanged();


            if (mType == Constants.BUS_TYPE) {
                if (mNothingContents != null) {
                    if (mBusRouteItem.size() == 0) {
                        mNothingContents.setVisibility(View.VISIBLE);
                    } else {
                        mNothingContents.setVisibility(View.GONE);
                    }
                }
            } else if (mType == Constants.STOP_TYPE) {
                if (mNothingContents != null) {
                    if (mBusStopItem.size() == 0) {
                        mNothingContents.setVisibility(View.VISIBLE);
                    } else {
                        mNothingContents.setVisibility(View.GONE);
                    }
                }
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

        }

    }

}
