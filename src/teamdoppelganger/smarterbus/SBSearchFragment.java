package teamdoppelganger.smarterbus;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import teamdoppelganger.smarterbus.SBSelectFragment.SBSelectFragmentListener;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.common.SBInforApplication.TrackerName;
import teamdoppelganger.smarterbus.item.AutoItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.CommonItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.item.FindBusRouteItem;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.item.LocalItem;
import teamdoppelganger.smarterbus.item.NaverSearchResultItem;
import teamdoppelganger.smarterbus.item.SearchItem;
import teamdoppelganger.smarterbus.item.TagoArrayItem;
import teamdoppelganger.smarterbus.item.TagoBusItem;
import teamdoppelganger.smarterbus.item.TagoItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GeoPoint;
import teamdoppelganger.smarterbus.util.common.GeoTrans;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import teamdoppelganger.smarterbus.util.common.StaticCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.SmarterLocationManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smart.lib.CommonConstants;

@SuppressLint("ValidFragment")
public class SBSearchFragment extends SBBaseFragment implements OnClickListener, OnMarkerDragListener, OnMapLongClickListener {

    /**
     * 정류장, 버스, 경로
     */
    Spinner mSearchSpin;
    EditText mSearchEdit1, mSearchEdit2;
    RelativeLayout mSearchLayout1, mSearchLayout2;
    RelativeLayout mRouteSearchLayout;
    TextView mSearchActionBtn;
    Button mSearchModeBtn1, mSearchModeBtn2;
    LinearLayout mStartSelectBtn, mEndSelectBtn;
    ListView mListView, mSearchResultList;
    TextView mSearchDescription;

    ImageView mSearchEdit1Del, mSearchEdit2Del;

    RelativeLayout mMapLayout;
    GoogleMap mGoogleMap;

    SchAdapter mSchAdapter;
    ResultAdapter mResultAdapter;

    int mSearchMode;
    Boolean mIsInit = false;
    LocalInforItem mLocalInforItem;
    Runnable mRunnable;

    ArrayList<LocalItem> mLocalEnCityName;
    ArrayList<?> mResultList;
    ArrayList<FindBusRouteItem> mFindBusRouteItems;
    ArrayList<Marker> mMarkerList;
    ArrayList<String> mNotAllowTextAry;

    //경로 검색
    SearchItem mStartSearchItem, mEndSearchItem;
    HashMap<Integer, String> mTerminus;
    HashMap<Integer, String> mHashLocationKo;
    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mBusTypeHash;

    CustomAsync mCustomAsync;

    AutoItem mStartAutoItem = null, mEndAutoItem = null;
    ArrayList<FavoriteCheckItem> mFavoriteAndHistoryItem;

    Handler mHandler;

    int mWidgetMode = Constants.WIDGET_MODE_NOTHING;
    int mWidgetType;

    SBSelectFragment mSBselectFragment;


    SBSearchFragmentListener mSBSearchFragmentListener;

    Marker mStartMarker, mEndMarker;


    ArrayList<Integer> mHeadList;
    HashMap<String, Integer> mHeadString;


    Runnable mSearchRunnable;

    boolean isKeyboardShow = false;
    String mheadValue = null;
    int mHeadPosition = 0;

    public void setSBSearchFragmentListener(SBSearchFragmentListener l) {
        mSBSearchFragmentListener = l;
    }

    interface SBSearchFragmentListener {
        void setChange(CommonItem item);
    }


    public SBSearchFragment(int id, SQLiteDatabase db,
                            LocalDBHelper localDBHelper) {
        super(id, db, localDBHelper);
        mLocalEnCityName = new ArrayList<LocalItem>();
    }

    public SBSearchFragment() {

    }


    public void setWidgetMode(int widgetMode) {
        mWidgetMode = widgetMode;
    }

    public void setChecked(boolean isAllCheck) {

        mSBselectFragment.setChecked(isAllCheck);

    }

    public void setWidgetTypeMode(int type) {
        mWidgetType = type;
    }

    @Override
    public void onLayoutFinish(View view) {
        super.onLayoutFinish(view);

        mHandler = new Handler();
        mHeadList = new ArrayList<Integer>();
        mHeadString = new HashMap<String, Integer>();

        mSearchRunnable = new Runnable() {

            @Override
            public void run() {
                if (mSearchSpin != null) {
                    mSearchSpin.setSelection(getSearchMode());
                }
            }
        };

        mNotAllowTextAry = new ArrayList<String>();
        notAllowStr();

        mLocalInforItem = ((SBInforApplication) getActivity().getApplicationContext()).mLocalSaveInfor;
        mTerminus = ((SBInforApplication) getActivity().getApplicationContext()).mTerminus;
        mHashLocationKo = ((SBInforApplication) getActivity().getApplicationContext()).mHashKoLocation;
        mHashLocationEng = ((SBInforApplication) getActivity().getApplicationContext()).mHashLocation;
        mBusTypeHash = ((SBInforApplication) getActivity().getApplicationContext()).mBusTypeHash;

        mFavoriteAndHistoryItem = new ArrayList<FavoriteCheckItem>();
        mSearchEdit1 = (EditText) view.findViewById(R.id.searchEdit1);
        mSearchEdit2 = (EditText) view.findViewById(R.id.searchEdit2);

        mSearchEdit1Del = (ImageView) view.findViewById(R.id.seachEdit1Del);
        mSearchEdit2Del = (ImageView) view.findViewById(R.id.seachEdit2Del);

        mSearchModeBtn1 = (Button) view.findViewById(R.id.searchBtnMode1);
        mSearchModeBtn2 = (Button) view.findViewById(R.id.searchBtnMode2);
        mSearchActionBtn = (TextView) view.findViewById(R.id.searchActionBtn);

        mStartSelectBtn = (LinearLayout) view.findViewById(R.id.startSelectBtn);
        mEndSelectBtn = (LinearLayout) view.findViewById(R.id.endSelectBtn);

        mSearchLayout1 = (RelativeLayout) view.findViewById(R.id.search1);
        mSearchLayout2 = (RelativeLayout) view.findViewById(R.id.search2);
        mRouteSearchLayout = (RelativeLayout) view.findViewById(R.id.routeSearchLayout);
        mMapLayout = (RelativeLayout) view.findViewById(R.id.mapLayout);

        mSearchDescription = (TextView) view.findViewById(R.id.searchDescription);


        mStartSelectBtn.setOnClickListener(this);
        mEndSelectBtn.setOnClickListener(this);
        mSearchEdit1Del.setOnClickListener(this);
        mSearchEdit2Del.setOnClickListener(this);


        mSearchSpin = (Spinner) view.findViewById(R.id.searchSpin);
        mListView = (ListView) view.findViewById(R.id.listView);
        mSearchResultList = (ListView) view.findViewById(R.id.searchResult);


        mFindBusRouteItems = new ArrayList<FindBusRouteItem>();
        ArrayList<String> spinnerlist = new ArrayList<String>();
        mResultList = new ArrayList();
        mStartSearchItem = new SearchItem();
        mEndSearchItem = new SearchItem();

        spinnerlist.add(getResources().getString(R.string.search1));

        if (mWidgetMode != Constants.WIDGET_MODE_STOP) {
            spinnerlist.add(getResources().getString(R.string.search2));
        }

        if (mWidgetMode == Constants.WIDGET_MODE_NOTHING) {
            spinnerlist.add(getResources().getString(R.string.search3));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.simple_dropdown_item_1line_custom, spinnerlist);
        //스피너 속성
        mSearchSpin.setAdapter(adapter);


        mResultAdapter = new ResultAdapter();
        mSearchResultList.setAdapter(mResultAdapter);


        final Fragment frag = this;

        mSearchEdit1.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        //키보드 내리게 적용
                        if (mSearchEdit1 != null && isKeyboardShow) {
                            isKeyboardShow = false;
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mSearchEdit1.getWindowToken(), 0);
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });


        mListView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (mSearchEdit1 != null && isKeyboardShow) {
                    isKeyboardShow = false;
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mSearchEdit1.getWindowToken(), 0);

                }

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }

        });

        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchEdit1.getWindowToken(), 0);


                if (mSearchMode == Constants.SERCH_MODE_STOP) {
                    Cursor cursor = (Cursor) mSchAdapter.getItem(position);

                    String busStopName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    String busStopApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                    String busStopArsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    String busStopLocationID = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID));
                    String description = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                    int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));

                    int locationX = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
                    int locationY = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));

                    //preference에 저장
                    setRecetBusStop(locationX, locationY);

                    BusStopItem busStopItem = new BusStopItem();
                    busStopItem.apiId = busStopApiId;
                    busStopItem.arsId = busStopArsId;
                    busStopItem.name = busStopName;
                    busStopItem.localInfoId = busStopLocationID;
                    busStopItem.tempId2 = description;
                    busStopItem._id = id;

                    if (busStopLocationID.equals(String.valueOf(CommonConstants.CITY_MOK_PO._cityId))) {
                        busStopItem.apiId = busStopArsId;
                    }


                    if (mWidgetMode != Constants.WIDGET_MODE_NOTHING) {

                        boolean widgeAble = RequestCommonFuction.getAlarmAndWigetAble(Integer.parseInt(busStopItem.localInfoId));

                        if (!widgeAble) {

                            Toast.makeText(getActivity(), "위젯을 생성 할 수 없는 지역입니다.", Toast.LENGTH_SHORT).show();

                            return;
                        }


                        mSBselectFragment = new SBSelectFragment(R.layout.selectfragment, getBusDbSqlite(), getLocalDBHelper());
                        mSBselectFragment.setBusStopItem(busStopItem);

                        if (mWidgetMode == Constants.WIDGET_MODE_BUS_STOP) {
                            mSBselectFragment.setStopWidgetMode(false);
                        } else {
                            mSBselectFragment.setStopWidgetMode(true);
                        }


                        mSBselectFragment.setSBSelectFragmentListener(new SBSelectFragmentListener() {

                            @Override
                            public void onChange(CommonItem item) {
                                if (mSBSearchFragmentListener != null) {
                                    mSBSearchFragmentListener.setChange(item);
                                }
                                // TODO Auto-generated method stub

                            }
                        });
                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frame, mSBselectFragment).addToBackStack("search").commit();


                        if (mSBSearchFragmentListener != null) {
                            mSBSearchFragmentListener.setChange(busStopItem);
                        }


                    } else {


                        Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                        sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                        sendIntent.putExtra("busInfor", busStopItem);
                        startActivity(sendIntent);


                        new StaticCommonFuction().inputFavorteAndRecent(getBusDbSqlite(), getLocalDBHelper(),
                                false, busStopLocationID, Constants.STOP_TYPE, "", "", busStopApiId, busStopArsId, Constants.DEFAULT_COLOR, busStopName, "", description, "");

                    }


                } else if (mSearchMode == Constants.SERCH_MODE_BUS) {

                    Cursor cursor = (Cursor) mSchAdapter.getItem(position);

                    String busRouteName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                    String busRouteSubName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                    String busRouteApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                    String busRouteApiId2 = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
                    String busStart = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                    String busEnd = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
                    String busRouteLocationID = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID));
                    int busType = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));


                    int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));


                    BusRouteItem busRouteItem = new BusRouteItem();
                    busRouteItem.busRouteApiId = busRouteApiId;
                    busRouteItem.busRouteApiId2 = busRouteApiId2;
                    busRouteItem.busRouteName = busRouteName;
                    busRouteItem.busRouteSubName = busRouteSubName;
                    busRouteItem.localInfoId = busRouteLocationID;
                    busRouteItem.startStop = busStart;
                    busRouteItem.endStop = busEnd;
                    busRouteItem.busType = busType;
                    busRouteItem._id = id;


                    if (mWidgetMode != Constants.WIDGET_MODE_NOTHING) {

                        boolean widgeAble = RequestCommonFuction.getAlarmAndWigetAble(Integer.parseInt(busRouteItem.localInfoId));

                        if (!widgeAble) {
                            Toast.makeText(getActivity(), "위젯을 생성 할 수 없는 지역의 버스입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mSBselectFragment = new SBSelectFragment(R.layout.selectfragment, getBusDbSqlite(), getLocalDBHelper());
                        mSBselectFragment.setBusRouteItem(busRouteItem);

                        if (mWidgetMode == Constants.WIDGET_MODE_BUS_STOP) {
                            mSBselectFragment.setStopWidgetMode(false);
                        } else {
                            mSBselectFragment.setStopWidgetMode(true);
                        }


                        mSBselectFragment.setSBSelectFragmentListener(new SBSelectFragmentListener() {

                            @Override
                            public void onChange(CommonItem item) {
                                if (mSBSearchFragmentListener != null) {
                                    mSBSearchFragmentListener.setChange(item);
                                }
                                // TODO Auto-generated method stub

                            }
                        });


                        if (mSBSearchFragmentListener != null) {
                            mSBSearchFragmentListener.setChange(busRouteItem);
                        }

                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frame, mSBselectFragment).addToBackStack("search").commit();

                    } else {

                        Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                        sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSROUTEITEM);
                        sendIntent.putExtra("busInfor", busRouteItem);
                        startActivity(sendIntent);

                        new StaticCommonFuction().inputFavorteAndRecent(getBusDbSqlite(), getLocalDBHelper(),
                                false, busRouteLocationID, Constants.BUS_TYPE, busRouteApiId, busRouteApiId2, "", "", Constants.DEFAULT_COLOR, busRouteName, "", "", "");


                    }


                }
            }
        });


        mSearchActionBtn.setOnClickListener(this);
        mSearchModeBtn1.setOnClickListener(this);
        mSearchModeBtn2.setOnClickListener(this);

    }


    void setNowLocation() {

        Location location = new SmarterLocationManager(getActivity(), false, checkNetwordState()).getBestLastKnownLocation();

        double latitude = 37.564093; // default 시청역
        double longitude = 126.976681;

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        if (mGoogleMap != null)
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

    }

    private void setMode(int searchMode) {

        //경로 탐색
        if (searchMode == 2) {

            mSearchEdit2.setVisibility(View.VISIBLE);

            mSearchLayout1.setVisibility(View.VISIBLE);
            mSearchLayout2.setVisibility(View.VISIBLE);
            mSearchActionBtn.setVisibility(View.VISIBLE);
            mRouteSearchLayout.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);


            mSearchEdit1.setInputType(InputType.TYPE_NULL);
            mSearchEdit2.setInputType(InputType.TYPE_NULL);

            mSearchEdit1.setOnClickListener(this);
            mSearchEdit2.setOnClickListener(this);

            mSearchActionBtn.setEnabled(false);

            mSearchModeBtn1.performClick();


        } else {

            mSearchEdit1.setVisibility(View.VISIBLE);
            mSearchEdit2.setVisibility(View.GONE);
            mSearchLayout1.setVisibility(View.VISIBLE);
            mSearchLayout2.setVisibility(View.GONE);
            mRouteSearchLayout.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mSearchActionBtn.setVisibility(View.GONE);

            mSearchEdit1.setOnClickListener(null);
            mSearchEdit2.setOnClickListener(null);

            mSearchEdit1.setInputType(InputType.TYPE_CLASS_TEXT);
            mSearchEdit2.setInputType(InputType.TYPE_CLASS_TEXT);

        }


        mSearchMode = searchMode;


        mSearchEdit1.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isKeyboardShow = true;

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                for (int i = 0; i < mNotAllowTextAry.size(); i++) {
                    if (s.toString().contains(mNotAllowTextAry.get(i))) {
                        return;
                    }
                }

                if (mSearchMode == Constants.SERCH_MODE_ROUTE) return;

                // TODO Auto-generated method stub
                Log.i("bs", "s.toString():" + s.toString());
                mSchAdapter.getFilter().filter(s.toString());


            }
        });


    }

    @Override
    public void activityResult(int requestCode, int resultCode, Intent data) {
        super.activityResult(requestCode, resultCode, data);

        if (requestCode == Constants.INTENT_RESULT_CODE_FROM_AUTO_1) {

            if (data != null) {

                AutoItem autoItem = (AutoItem) data.getSerializableExtra(Constants.INTENT_AUTOITEM);
                autoItem.cityId = getCityId(autoItem.city1, autoItem.city2);
                mSearchEdit1.setText(autoItem.name);
                mStartAutoItem = autoItem;

            }

            itemRouteSearchCheck();

        } else if (requestCode == Constants.INTENT_RESULT_CODE_FROM_AUTO_2) {

            if (data != null) {

                AutoItem autoItem = (AutoItem) data.getSerializableExtra(Constants.INTENT_AUTOITEM);
                autoItem.cityId = getCityId(autoItem.city1, autoItem.city2);
                mSearchEdit2.setText(autoItem.name);

                mEndAutoItem = autoItem;

            }


            itemRouteSearchCheck();
        }

    }


    public void itemRouteSearchCheck() {
        if (mEndAutoItem != null && mStartAutoItem != null) {
            mSearchActionBtn.setEnabled(true);
        } else {
            mSearchActionBtn.setEnabled(false);
        }

    }

    public String getCityId(String city1, String city2) {

        String cityName = city1.substring(0, 2);

        String sql = String.format("SELECT * from %s where %s='%s'", CommonConstants.TBL_CITY, CommonConstants.CITY_NAME, city1.substring(0, 2));
        Cursor cursor = getBusDbSqlite().rawQuery(sql, null);

        String strID = null;
        if (cursor.moveToNext()) {
            strID = String.valueOf(cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID)));
        }
        cursor.close();

        if (strID == null) {

            sql = String.format("SELECT * from %s where %s='%s'", CommonConstants.TBL_CITY, CommonConstants.CITY_NAME, city2.substring(0, 2));
            cursor = getBusDbSqlite().rawQuery(sql, null);
            if (cursor.moveToNext()) {
                strID = String.valueOf(cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID)));
            }
            cursor.close();

            return strID;

        } else {

            return strID;
        }

    }

    private boolean compareAutoItem(AutoItem startAutoItem, AutoItem endAutoItem) {

        if (startAutoItem.cityId == null || endAutoItem.cityId == null) {
            return false;
        }

        if (!startAutoItem.cityId.equals(endAutoItem.cityId)) {
            return false;
        }


        return true;
    }


    class SchAdapter extends CursorAdapter {


        String _highLightStr;
        int _searchMode;


        public SchAdapter(Context context, Cursor c, boolean autoRequery, int searchMode) {
            super(context, c, autoRequery);


            // TODO Auto-generated constructor stub
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.line_row,
                    parent, false);

            return view;
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            if (cursor == null || cursor.getCount() == 0) return;

            int position = cursor.getPosition();

            TextView text1 = (TextView) view.findViewById(R.id.lineName);
            TextView text2 = (TextView) view.findViewById(R.id.lineDetail);
            TextView headTxt = (TextView) view.findViewById(R.id.headTxt);
            TextView locationText = (TextView) view.findViewById(R.id.locationTxt);
            TextView nextTxt = (TextView) view.findViewById(R.id.stopNextDetail);

            LinearLayout head = (LinearLayout) view.findViewById(R.id.head);


            if (_searchMode == Constants.SERCH_MODE_STOP) {

                nextTxt.setVisibility(View.VISIBLE);

                String busStopName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                String busStopApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                String busStopArsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                String description = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                String busStopLocationName = mHashLocationKo.get(Integer.parseInt(cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID))));
                String nextStopName = cursor.getString(cursor.getColumnIndex("nextStopName"));


                text1.setTextColor(Color.parseColor("#333333"));

                if (description == null
                        || description.equals("null") || description.equals("")) {
                    description = "";
                } else {
                    description = " - " + description + "마을";
                }

                SpannableStringBuilder sp = null;
                if (_highLightStr != null && busStopName.indexOf(_highLightStr) != -1) {
                    sp = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME)));
                    sp.setSpan(new ForegroundColorSpan(Color.RED), busStopName.indexOf(_highLightStr), busStopName.indexOf(_highLightStr) + _highLightStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                String nextStopStr = "[" + nextStopName + "방면]";

                text1.setTextColor(Color.parseColor("#333333"));
                text1.setText(busStopName);

                if (busStopApiId == null || busStopArsId.length() == 0) {
                    text2.setVisibility(View.GONE);
                } else {
                    text2.setVisibility(View.VISIBLE);


                    if (busStopLocationName.equals("서울")) {
                        text2.setText(busStopArsId.substring(0, 2) + "-" + busStopArsId.substring(2, busStopArsId.length()));
                    } else {
                        text2.setText(busStopArsId);
                    }

                }

                if (nextStopStr.contains("null")) {
                    nextTxt.setText("[종점]" + description);
                } else {
                    nextTxt.setText(nextStopStr + description);
                }


                if (mHeadString.get(busStopLocationName) == null) {

                    mHeadString.put(busStopLocationName, position);
                    head.setVisibility(View.VISIBLE);
                    headTxt.setText(busStopLocationName);


                } else {


                    int headPosition = mHeadString.get(busStopLocationName);

                    if (headPosition >= position) {
                        mHeadString.put(busStopLocationName, position);
                        head.setVisibility(View.VISIBLE);
                        headTxt.setText(busStopLocationName);

                    } else {
                        head.setVisibility(View.GONE);
                        headTxt.setText(busStopLocationName);
                    }

                }


                locationText.setText(busStopLocationName);
            } else if (_searchMode == Constants.SERCH_MODE_BUS) {

                nextTxt.setVisibility(View.GONE);

                String busRouteName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                String busRouteApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
                String busType = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
                String busSubName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
                String tmpLocalBus = cursor.getString(cursor.getColumnIndex("cityIds"));


                String busRouteLocationName = mHashLocationKo.get(Integer.parseInt(cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID))));

                int startStopId = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
                int endStopId = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));


                try {
                    String color = mBusTypeHash.get(Integer.parseInt(busType));
                    text1.setTextColor(Color.parseColor("#" + color));
                } catch (Exception e) {
                    text1.setTextColor(Color.parseColor("#333333"));
                }
                ;

                text1.setText(busRouteName);


                if (tmpLocalBus.contains(",")) {

                    String[] locationNames = tmpLocalBus.split(",");

                    busRouteLocationName = "";

                    for (int i = 0; i < locationNames.length; i++) {

                        String locName = mHashLocationKo.get(Integer.parseInt(locationNames[i]));

                        if (busRouteLocationName.equals("")) {
                            busRouteLocationName = locName;
                        } else {
                            busRouteLocationName = busRouteLocationName + "," + locName;
                        }
                    }
                }


                text2.setText(mTerminus.get(startStopId) + "->" + mTerminus.get(endStopId));

                if (busRouteLocationName.equals("경기"))
                    locationText.setText(busSubName);
                else
                    locationText.setText(busRouteLocationName);


                if (mHeadString.get(busRouteLocationName) == null) {

                    mHeadString.put(busRouteLocationName, position);
                    head.setVisibility(View.VISIBLE);
                    headTxt.setText(busRouteLocationName);


                } else {


                    int headPosition = mHeadString.get(busRouteLocationName);

                    if (headPosition >= position) {

                        mHeadString.put(busRouteLocationName, position);
                        head.setVisibility(View.VISIBLE);
                        headTxt.setText(busRouteLocationName);

                    } else {

                        head.setVisibility(View.GONE);
                        headTxt.setText(busRouteLocationName);

                    }

                }


            } else if (_searchMode == Constants.SERCH_MODE_ROUTE) {

            }

            return;

        }


        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            return super.getView(arg0, arg1, arg2);

        }


        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            // TODO Auto-generated method stub
            Log.i("bs", "runQueryOnBackgroundThread:" + constraint.toString());
            if (constraint == null || constraint.toString().length() == 0) {
                _highLightStr = null;
            } else {
                _highLightStr = constraint.toString();
            }


            return getCursorByName(constraint.toString(), _searchMode);
        }


        @Override
        public void notifyDataSetChanged() {
            // TODO Auto-generated method stub

            super.notifyDataSetChanged();

        }

        @Override
        public Filter getFilter() {
            // TODO Auto-generated method stub

            return super.getFilter();
        }

        public void setAdapterMode(int mode) {
            _searchMode = mode;
        }


        @Override
        protected void onContentChanged() {

            super.onContentChanged();
        }


    }


    private Cursor getCursorByName(String searchStr, int searchMode) {

        mHeadString.clear();
        mHeadPosition = 0;

        String qryStopInfor = null;

        if (searchStr != null && searchStr.trim().length() == 0) {
            searchStr = "null";

        } else {
            //검색 문제 수정
            if (searchMode == Constants.SERCH_MODE_STOP) {
                searchStr = searchStr.replace("-", "");
            }
        }


        if (searchMode == Constants.SERCH_MODE_STOP) {

            String tableName = "";
            for (int i = 0; i < mLocalEnCityName.size(); i++) {
                //String tempTable =  String.format("SELECT *,'%s' as %s FROM %s_STOP",mLocalEnCityName.get(i).cityId,CommonConstants.CITY_ID,mLocalEnCityName.get(i).enName );

                String tempTable = String.format("SELECT *, (Select stopName from %s_STOP where _id=tb_%s_a.stopNextStopId) as nextStopName  from (SELECT *,'%s' as cityId FROM %s_STOP) as tb_%s_a"
                        , mLocalEnCityName.get(i).enName, mLocalEnCityName.get(i).cityId, mLocalEnCityName.get(i).cityId, mLocalEnCityName.get(i).enName
                        , mLocalEnCityName.get(i).cityId, mLocalEnCityName.get(i).enName);

                if (tableName.equals("")) {
                    tableName = tempTable;
                } else {
                    tableName = tableName + " union ALL " + tempTable;
                }
            }


            String qry1StopName = String.format("SELECT * from (%s) where  (%s<>%s ) and (%s like '%s%%' or %s like '%s%%') limit 500",
                    tableName, CommonConstants.BUS_STOP_ARS_ID, "0", CommonConstants.BUS_STOP_ARS_ID, searchStr, CommonConstants.BUS_STOP_NAME, searchStr);

            qryStopInfor = String.format("SELECT * from (%s) order by cityId", qry1StopName);


        } else if (searchMode == Constants.SERCH_MODE_BUS) {
            String tableName = "";
            for (int i = 0; i < mLocalEnCityName.size(); i++) {
                String tempTable = String.format("SELECT *,'%s' as %s FROM %s_ROUTE", mLocalEnCityName.get(i).cityId, CommonConstants.CITY_ID, mLocalEnCityName.get(i).enName);
                if (tableName.equals("")) {
                    tableName = tempTable;
                } else {
                    tableName = tableName + " union ALL " + tempTable;
                }
            }

            qryStopInfor = String.format(" Select * from ( SELECT * from (Select *,group_concat(DISTINCT cityId) as cityIds  from (%s) group by %s,%s,%s,%s,_id   having %s like '%s%%' or %s like '%s%%' or %s like '%s%%' or %s like '%s%%' or (routeType=%s and routeName like '%%%s') or (routeType=%s and routeName like '%%%s') or (routeType=%s and routeName like '%%%s') or (routeType=%s and routeName like '%%%s%%')) limit 600) order by cityId ",
                    tableName, CommonConstants.BUS_ROUTE_NAME, CommonConstants.BUS_ROUTE_START_STOP_ID, CommonConstants.BUS_ROUTE_END_STOP_ID, CommonConstants.BUS_ROUTE_SUB_NAME, CommonConstants.BUS_ROUTE_NAME, searchStr, CommonConstants.BUS_ROUTE_NAME, "M" + searchStr, CommonConstants.BUS_ROUTE_NAME, "N" + searchStr, CommonConstants.BUS_ROUTE_NAME, "G" + searchStr, Constants.SEOUL_BUS_VILLAGE, searchStr, Constants.BUSAN_BUS_VILLAGE, searchStr, Constants.GONGJU_BUS_VILLAGE, searchStr, Constants.BOSEONG_BUS_VILLAGE, searchStr);


        } else if (searchMode == Constants.SERCH_MODE_ROUTE) {

            qryStopInfor = String.format("SELECT * FROM %s where  %s like '%%%s%% '  ", CommonConstants.CITY_EN_NAME + "_route", CommonConstants.BUS_ROUTE_NAME, searchStr);

        }


        Cursor stopCur = getBusDbSqlite().rawQuery(qryStopInfor, null);


        return stopCur;
    }


    class ViewHolder {
        TextView text;
        TextView time;
        TextView locationTxt;
        ImageView image;
    }


    private void loadMap() {
        try {
            mGoogleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2)).getMap();
            mGoogleMap.setOnMapLongClickListener(this);
            mGoogleMap.setOnMarkerDragListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void setLocation() {

        mLocalEnCityName.clear();
        mLocalEnCityName = new ArrayList<LocalItem>();
        for (int i = 0; i < mLocalInforItem.localItems.size(); i++) {

            LocalItem localItem = mLocalInforItem.localItems.get(i);
            mLocalEnCityName.add(localItem);

        }

    }

    @Override
    public void selectedPage() {
        super.selectedPage();

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_search));
        t.send(new HitBuilders.AppViewBuilder().build());

        if (mGoogleMap == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    loadMap();
                }
            };

            mHandler.postDelayed(mRunnable, 1000);
        }


        boolean isLocationChange = false;

        if (mLocalEnCityName.size() != mLocalInforItem.localItems.size()) {

            setLocation();
            isLocationChange = true;

        } else {

            for (int i = 0; i < mLocalInforItem.localItems.size(); i++) {
                boolean isExist = false;


                for (int j = 0; j < mLocalEnCityName.size(); j++) {
                    if (mLocalEnCityName.get(j).cityId == mLocalInforItem.localItems.get(i).cityId) {
                        isExist = true;
                    }
                }

                if (!isExist) {

                    setLocation();
                    isLocationChange = true;
                    break;
                }
            }
        }


        if (mIsInit && !isLocationChange) return;


        if (mCustomAsync != null) mCustomAsync.cancel(true);

        if (isLocationChange) {

            if (mSearchEdit1 != null) {
                mSearchEdit1.setText("");
            }

            mCustomAsync = new CustomAsync();
            mCustomAsync.execute();
        }

        mIsInit = true;

    }

    @Override
    public void unSelectedPage() {
        super.unSelectedPage();

        if (mCustomAsync != null) mCustomAsync.cancel(true);


        if (mSearchEdit1 != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchEdit1.getWindowToken(), 0);
        }

        if (mSearchEdit2 != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchEdit2.getWindowToken(), 0);
        }


        if (mHandler != null && mSearchRunnable != null) {
            mHandler.removeCallbacks(mSearchRunnable);
        }

    }

    private void showWhereDialog(boolean isStart) {

        if (isStart) {


        } else {

        }

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        String searchStr;
        Intent intent;

        LatLng latLang;

        switch (id) {
            case R.id.searchEdit1:

                intent = new Intent(getActivity(), SBAutoCompleteActivity.class);
                intent.putExtra(Constants.AUTO_INDEX, 1);
                getActivity().startActivityForResult(intent, Constants.INTENT_RESULT_CODE_FROM_AUTO_1);
                break;

            case R.id.searchEdit2:

                intent = new Intent(getActivity(), SBAutoCompleteActivity.class);
                intent.putExtra(Constants.AUTO_INDEX, 2);
                getActivity().startActivityForResult(intent, Constants.INTENT_RESULT_CODE_FROM_AUTO_2);
                break;

            case R.id.searchActionBtn:

                Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                        TrackerName.APP_TRACKER);
                t.enableAdvertisingIdCollection(true);
                t.send(new HitBuilders.EventBuilder().setCategory(getString(R.string.analytics_category_event))
                        .setAction(getString(R.string.analytics_action_searchFrag)).setLabel(getString(R.string.analytics_label_searchAction)).build());


                playVibe();
                searchResult();

                break;
            case R.id.searchBtnMode2:


                mSearchModeBtn1.setSelected(false);
                mSearchModeBtn2.setSelected(true);

                setNowLocation();
                mMapLayout.setVisibility(View.VISIBLE);
                playVibe();


                break;
            case R.id.searchBtnMode1:

                mSearchModeBtn1.setSelected(true);
                mSearchModeBtn2.setSelected(false);

                mMapLayout.setVisibility(View.GONE);
                new UseWebAsyn().execute();
                playVibe();

                break;
            case R.id.startSelectBtn:
                latLang = mGoogleMap.getCameraPosition().target;
                mStartAutoItem = new AutoItem();


                mStartAutoItem.latitude = latLang.latitude;
                mStartAutoItem.longtude = latLang.longitude;

                BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.start_pin);
                if (mStartMarker != null) {
                    mStartMarker.remove();
                }
                mStartMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLang).icon(startIcon));

                new GetAddress().execute(String.valueOf(latLang.latitude) + "," + String.valueOf(latLang.longitude), "start");

                break;

            case R.id.endSelectBtn:
                latLang = mGoogleMap.getCameraPosition().target;

                mEndAutoItem = new AutoItem();

                mEndAutoItem.latitude = latLang.latitude;
                mEndAutoItem.longtude = latLang.longitude;

                if (mEndMarker != null) {
                    mEndMarker.remove();
                }
                BitmapDescriptor arriveIcon = BitmapDescriptorFactory.fromResource(R.drawable.arrival_pin);
                mEndMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLang).icon(arriveIcon));

                new GetAddress().execute(String.valueOf(latLang.latitude) + "," + String.valueOf(latLang.longitude), "end");

                break;


            case R.id.seachEdit1Del:
                mSearchEdit1.setText("");

                if (mStartAutoItem != null) {
                    mStartAutoItem = null;
                }

                itemRouteSearchCheck();


                break;

            case R.id.seachEdit2Del:
                mSearchEdit2.setText("");

                if (mEndAutoItem != null) {
                    mEndAutoItem = null;
                }


                itemRouteSearchCheck();


                break;

        }
    }


    public TagoArrayItem searchLocal(int range) {

        TagoArrayItem tagoArrayItem = new TagoArrayItem();

        LinkedHashMap<Integer, TagoItem> hashTmp = new LinkedHashMap<Integer, TagoItem>();
        ArrayList<String> localEngNameList = new ArrayList<String>();

        String localEngName = mHashLocationEng.get(Integer.parseInt(mStartAutoItem.cityId));
        String endLocalEngName = mHashLocationEng.get(Integer.parseInt(mEndAutoItem.cityId));


        String startNearQuery = String.format("SELECT * from (%s_stop)  WHERE %s>'%d' and %s<'%d' and %s>'%d' and %s<'%d' ",
                localEngName, CommonConstants.BUS_STOP_LOCATION_Y, (int) (mStartAutoItem.latitude * 1e6 - range),
                CommonConstants.BUS_STOP_LOCATION_Y, (int) (mStartAutoItem.latitude * 1e6 + range),
                CommonConstants.BUS_STOP_LOCATION_X, (int) (mStartAutoItem.longtude * 1e6 - range),
                CommonConstants.BUS_STOP_LOCATION_X, (int) (mStartAutoItem.longtude * 1e6 + range));

        String endNearQuery = String.format("SELECT * from (%s_stop)  WHERE %s>'%d' and %s<'%d' and %s>'%d' and %s<'%d' ",
                endLocalEngName, CommonConstants.BUS_STOP_LOCATION_Y, (int) (mEndAutoItem.latitude * 1e6 - range),
                CommonConstants.BUS_STOP_LOCATION_Y, (int) (mEndAutoItem.latitude * 1e6 + range),
                CommonConstants.BUS_STOP_LOCATION_X, (int) (mEndAutoItem.longtude * 1e6 - range),
                CommonConstants.BUS_STOP_LOCATION_X, (int) (mEndAutoItem.longtude * 1e6 + range));


        Cursor cursor = getBusDbSqlite().rawQuery(startNearQuery, null);

        ArrayList<BusListItem> startBusList = new ArrayList<SBSearchFragment.BusListItem>();
        ArrayList<BusListItem> endBusList = new ArrayList<SBSearchFragment.BusListItem>();

        if (cursor != null) {
            cursor.moveToFirst();
        }

        while (cursor.moveToNext()) {

            BusListItem busListItem = new BusListItem();
            busListItem.stopId = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
            busListItem.stopName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
            busListItem.x = cursor.getDouble(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
            busListItem.y = cursor.getDouble(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));

            String[] list = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES)).split("/");
            for (int i = 0; i < list.length; i++) {
                busListItem.busNumList.add(list[i]);
            }
            startBusList.add(busListItem);
        }
        cursor.close();

        Cursor endCursor = getBusDbSqlite().rawQuery(endNearQuery, null);

        if (endCursor != null) {
            endCursor.moveToFirst();
        }

        while (endCursor.moveToNext()) {
            BusListItem busListItem = new BusListItem();
            busListItem.stopId = endCursor.getInt(endCursor.getColumnIndex(CommonConstants._ID));
            busListItem.stopName = endCursor.getString(endCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
            busListItem.x = endCursor.getDouble(endCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
            busListItem.y = endCursor.getDouble(endCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));

            String[] list = endCursor.getString(endCursor.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES)).split("/");
            for (int i = 0; i < list.length; i++) {
                busListItem.busNumList.add(list[i]);
            }
            endBusList.add(busListItem);

        }
        endCursor.close();


        Location locationStart = new Location("start");
        locationStart.setLatitude(mStartAutoItem.latitude);
        locationStart.setLongitude(mStartAutoItem.longtude);


        for (int i = 0; i < startBusList.size(); i++) {

            BusListItem busListItem = startBusList.get(i);

            Location locationBusStart = new Location("start");
            locationBusStart.setLatitude(busListItem.y / 1e6);
            locationBusStart.setLongitude(busListItem.x / 1e6);


            for (int j = 0; j < endBusList.size(); j++) {
                BusListItem endBusListItem = endBusList.get(j);
                Location locationEnd = new Location("end");
                locationEnd.setLatitude(endBusListItem.y / 1e6);
                locationEnd.setLongitude(endBusListItem.x / 1e6);

                for (int k = 0; k < endBusListItem.busNumList.size(); k++) {

                    if (endBusListItem.busNumList.get(k) == null || endBusListItem.busNumList.get(k).length() == 0)
                        continue;

                    int busId = Integer.parseInt(endBusListItem.busNumList.get(k));

                    if (busListItem.busNumList.contains(String.valueOf(busId))
                            && !busListItem.stopName.equals(endBusListItem.stopName)) {


                        double distance = locationStart.distanceTo(locationBusStart);
                        double compareDistance = locationBusStart.distanceTo(locationEnd);

                        TagoItem busItem;
                        busItem = new TagoItem();
                        busItem.totalWalk = String.valueOf(distance);
                        busItem.totalDistance = String.valueOf(compareDistance);
                        busItem.startLocX = busListItem.x / 1e6;
                        busItem.startLocY = busListItem.y / 1e6;
                        busItem.endLocX = endBusListItem.x / 1e6;
                        busItem.endLocY = endBusListItem.y / 1e6;


                        busItem.srcStopName = busListItem.stopName;
                        busItem.dstStopName = endBusListItem.stopName;

                        busItem.srcStopId = String.valueOf(busListItem.stopId);
                        busItem.endStopId = String.valueOf(endBusListItem.stopId);
                        busItem.cityId = mStartAutoItem.cityId;
                        busItem.busId = busId;

                        tagoArrayItem.tagoArrayItem.add(busItem);

                    }
                }
            }
        }


        return tagoArrayItem;

    }

    public void searchResult() {


        Location locationStart = new Location("start");
        locationStart.setLatitude(mStartAutoItem.longtude);
        locationStart.setLongitude(mStartAutoItem.latitude);

        Location locationEnd = new Location("end");
        locationEnd.setLatitude(mEndAutoItem.longtude);
        locationEnd.setLongitude(mEndAutoItem.latitude);

        double distance = locationStart.distanceTo(locationEnd);
        if (distance <= 500) {

            Toast.makeText(getActivity(), "500M 이하는 검색할 수 없습니다..", Toast.LENGTH_SHORT).show();
            return;
        }


        //경로 탐색
        if (mSearchMode == 2) {
            boolean isLocalSearchAble = false;
            if (mEndAutoItem != null) {
                isLocalSearchAble = compareAutoItem(mStartAutoItem, mEndAutoItem);
            }

            String param = "";
            if (isLocalSearchAble == false) {
                new GetTogoResult().execute("true");
            } else {
                new GetTogoResult().execute("false");
            }


        } else {
            if (mStartSearchItem == null) {
                if (mMarkerList.size() > 1) {
                    double latitude = mMarkerList.get(0).getPosition().latitude;
                    double longtitude = mMarkerList.get(0).getPosition().longitude;
                }
            }


            if (mEndSearchItem == null) {
                if (mMarkerList.size() > 1) {
                    double latitude = mMarkerList.get(1).getPosition().latitude;
                    double longtitude = mMarkerList.get(1).getPosition().longitude;
                }
            }


            Intent intent = new Intent(getActivity(), SBRouteSearchResultActivity.class);
            intent.putExtra("startLocation", mStartSearchItem);
            intent.putExtra("endLocation", mEndSearchItem);
            startActivity(intent);
        }

    }


    /**
     * searchFragment 에서 웹에서 데이터를 가져옴
     *
     * @author DOPPELSOFT4
     */
    class UseWebAsyn extends AsyncTask<String, String, Void> {

        ArrayList<FavoriteCheckItem> favoriteItem;

        @Override
        protected Void doInBackground(String... params) {

            favoriteItem = new ArrayList<FavoriteCheckItem>();
            //데이터 가져오는 부분

            Cursor cursor;
            cursor = getLocalDBHelper().getFavoriteValue(Constants.FAVORITE_TYPE_STOP);


            int count = 0;

            while (cursor.moveToNext()) {

                int _id = cursor.getInt(cursor.getColumnIndex(LocalDBHelper.TABLE_ID_F));
                int cityId = cursor.getInt(cursor.getColumnIndex(LocalDBHelper.TABLE_CITY_F));

                String type = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPE_F));
                String typeId1 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID_F));
                String typeId2 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID2_F));
                String typeId3 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID3_F));
                String typeId4 = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPEID4_F));


                String nick = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_TYPE_NICK));
                String color = cursor.getString(cursor.getColumnIndex(LocalDBHelper.TABLE_COLOR_F));


                FavoriteCheckItem item = new FavoriteCheckItem();
                item.id = _id;
                item.nickName = nick;
                item.color = color;
                item.key = count;

                String cityEnName = ((SBInforApplication) getActivity().getApplicationContext()).mHashLocation.get(cityId);
                String sql = "";

                int apiType = RequestCommonFuction.getApiTpye(cityId);


                if (apiType == Constants.API_TYPE_3) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_ARS_ID, typeId4);
                } else if (apiType == Constants.API_TYPE_1) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_API_ID, typeId3);
                } else if (apiType == Constants.API_TYPE_2) {
                    sql = String.format("SELECT * FROM %s_Stop where %s='%s' and %s='%s' ", cityEnName,
                            CommonConstants.BUS_STOP_API_ID, typeId3, CommonConstants.BUS_STOP_ARS_ID, typeId4);
                }

                if (sql.equals(""))
                    continue;

                Cursor busCursor = getBusDbSqlite().rawQuery(sql, null);
                if (busCursor != null && busCursor.getCount() > 0) {
                    if (busCursor.moveToNext()) {
                        item.type = Constants.FAVORITE_TYPE_STOP;
                        item.busStopItem.apiId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                        item.busStopItem.arsId = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));

                        item.busStopItem.latitude = busCursor.getDouble(busCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y)) / 1e6;
                        item.busStopItem.longtitude = busCursor.getDouble(busCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X)) / 1e6;


                        item.busStopItem.name = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                        item.busStopItem._id = busCursor.getInt(busCursor.getColumnIndex(CommonConstants._ID));

                        item.busStopItem.localInfoId = String.valueOf(cityId);
                        favoriteItem.add(item);
                    }
                }
                busCursor.close();


                count++;
            }
            cursor.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {


            mFavoriteAndHistoryItem.clear();
            mFavoriteAndHistoryItem.addAll(favoriteItem);

            if (mResultAdapter != null) {
                mResultAdapter.notifyDataSetChanged();
            }

            super.onPostExecute(result);
        }
    }


    private void notAllowStr() {
        mNotAllowTextAry.add("ㄱ");
        mNotAllowTextAry.add("ㄴ");
        mNotAllowTextAry.add("ㄷ");
        mNotAllowTextAry.add("ㄹ");
        mNotAllowTextAry.add("ㅁ");
        mNotAllowTextAry.add("ㅂ");
        mNotAllowTextAry.add("ㅅ");
        mNotAllowTextAry.add("ㅇ");
        mNotAllowTextAry.add("ㅈ");
        mNotAllowTextAry.add("ㅊ");
        mNotAllowTextAry.add("ㅋ");
        mNotAllowTextAry.add("ㅌ");
        mNotAllowTextAry.add("ㅍ");
        mNotAllowTextAry.add("ㅎ");
        mNotAllowTextAry.add("ㅏ");
        mNotAllowTextAry.add("ㅑ");
        mNotAllowTextAry.add("ㅓ");
        mNotAllowTextAry.add("ㅕ");
        mNotAllowTextAry.add("ㅗ");
        mNotAllowTextAry.add("ㅛ");
        mNotAllowTextAry.add("ㅜ");
        mNotAllowTextAry.add("ㅠ");
        mNotAllowTextAry.add("ㅡ");
        mNotAllowTextAry.add("ㅣ");
        mNotAllowTextAry.add("ㄲ");
        mNotAllowTextAry.add("ㄸ");
        mNotAllowTextAry.add("ㅉ");
        mNotAllowTextAry.add("ㅃ");
    }

    /**
     * 한글 파라미터 넘길시 urlENcoder처리를 해야함
     *
     * @param searchStr
     */
    private ArrayList<NaverSearchResultItem> getNaverSearchResult(String searchStr, String whatPosition) {

        ArrayList<NaverSearchResultItem> naverSearchList = new ArrayList<NaverSearchResultItem>();
        boolean isStrPosition = false;

        if (whatPosition.equals("start")) {
            isStrPosition = true;
        } else {
            isStrPosition = false;
        }

        String searchNaverUrl = "http://openapi.naver.com/search";
        String param = String.format("key=%s&query=%s&target=local", Constants.NAVER_API_KEY, URLEncoder.encode(searchStr));
        String result = RequestCommonFuction.getSource(searchNaverUrl, false, param, "utf-8");

        Document doc = Jsoup.parse(result);
        Elements elements = doc.select("item");
        if (elements == null || elements.size() == 0) return null;

        for (int i = 0; i < elements.size(); i++) {
            String title = elements.get(i).select("title").get(0).text();
            String address = elements.get(i).select("address").get(0).text();
            double mapX = Double.parseDouble(elements.get(i).select("mapx").get(0).text());
            double mapY = Double.parseDouble(elements.get(i).select("mapY").get(0).text());

            NaverSearchResultItem naverItem = new NaverSearchResultItem();
            naverItem.title = title;
            naverItem.address = address;
            naverItem.mapX = mapX;
            naverItem.mapY = mapY;
            naverItem.isStartPosition = isStrPosition;
            naverSearchList.add(naverItem);
        }

        return naverSearchList;
    }

    /**
     * tago 는 UTML좌표계를 사용한다.
     *
     * @param strSearchItem
     * @param endSearchItem
     * @return
     */
    private ArrayList<FindBusRouteItem> getTagoSearchResult(SearchItem strSearchItem, SearchItem endSearchItem) {

        ArrayList<FindBusRouteItem> findBusRouteItem = new ArrayList<FindBusRouteItem>();


        String searchNaverUrl = Constants.TAGO_URL;


        GeoPoint in_pt = new GeoPoint(strSearchItem.positionX, strSearchItem.positionY);//
        GeoPoint end_pt = new GeoPoint(endSearchItem.positionX, endSearchItem.positionY);//


        GeoPoint utmKS_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, in_pt);
        GeoPoint utmKE_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, end_pt);

        return findBusRouteItem;

    }


    class ResultAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFavoriteAndHistoryItem.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            ResultViewHolder viewHolder = null;

            if (view == null) {
                viewHolder = new ResultViewHolder();
                view = getActivity().getLayoutInflater().inflate(R.layout.search_result_row,
                        parent, false);

                viewHolder.title = (TextView) view.findViewById(R.id.titleResult);
                /*viewHolder.address = (TextView)view.findViewById(R.id.addressResult);*/
                viewHolder.setPoitionBtn = (TextView) view.findViewById(R.id.setPositionStart);
                viewHolder.setPositionEndBtn = (TextView) view.findViewById(R.id.setPositionEnd);


                view.setTag(viewHolder);

            } else {
                viewHolder = (ResultViewHolder) view.getTag();
            }


            final FavoriteCheckItem item = mFavoriteAndHistoryItem.get(position);
            viewHolder.title.setText(item.busStopItem.name);


            viewHolder.setPoitionBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    playVibe();
                    // TODO Auto-generated method stub
                    mStartAutoItem = new AutoItem();

                    mStartAutoItem.city1 = mHashLocationKo.get(Integer.parseInt(item.busStopItem.localInfoId));
                    mStartAutoItem.name = item.busStopItem.name;
                    mStartAutoItem.latitude = item.busStopItem.latitude;
                    mStartAutoItem.longtude = item.busStopItem.longtitude;
                    mStartAutoItem.cityId = getCityId(mStartAutoItem.city1, "");


                    mSearchEdit1.setText(item.busStopItem.name);


                    itemRouteSearchCheck();

                }
            });

            viewHolder.setPositionEndBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    playVibe();
                    // TODO Auto-generated method stub
                    mEndAutoItem = new AutoItem();

                    mEndAutoItem.city1 = mHashLocationKo.get(Integer.parseInt(item.busStopItem.localInfoId));
                    mEndAutoItem.name = item.busStopItem.name;
                    mEndAutoItem.latitude = item.busStopItem.latitude;
                    mEndAutoItem.longtude = item.busStopItem.longtitude;
                    mEndAutoItem.cityId = getCityId(mEndAutoItem.city1, "");

                    mSearchEdit2.setText(item.busStopItem.name);

                    itemRouteSearchCheck();

                }
            });


            return view;
        }


        class ResultViewHolder {
            TextView title;
            TextView address;
            TextView setPoitionBtn, setPositionEndBtn;
        }


    }


    //marker drag listener
    @Override
    public void onMarkerDrag(Marker marker) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMapLongClick(LatLng point) {

    }

    class CustomAsync extends AsyncTask<String, String, String> {


        Cursor _cursor;

        @Override
        protected String doInBackground(String... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (mSchAdapter == null) {
                mSchAdapter = new SchAdapter(getActivity(), _cursor, true, 0);
                mListView.setAdapter(mSchAdapter);
            }

            mSearchSpin.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {


                    setMode(position);

                    if (position == 0 || position == 1) {

                        mSchAdapter.setAdapterMode(mSearchMode);
                        Cursor cursor = getCursorByName("", mSearchMode);
                        mSchAdapter.changeCursor(cursor);
                        saveSearchMode(mSearchMode);

                    }

                    mSearchEdit1.setText("");

                    if (position == 0) {
                        mSearchEdit1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        mSearchEdit1.setPrivateImeOptions("defaultInputmode=korea");
                        mSearchEdit1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                        mSearchEdit1.setHint(Html.fromHtml("<small>" + getString(R.string.hint_stop_search) + "</small>"));
                    } else if (position == 1) {
                        mSearchEdit1.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        mSearchEdit1.setPrivateImeOptions("defaultInputmode=numeric");
                        mSearchEdit1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                        mSearchEdit1.setHint(Html.fromHtml("<small>" + getString(R.string.hint_bus_search) + "</small>"));
                    } else {
                        mSearchEdit1.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_course_start), null, null, null);
                        mSearchEdit1.setCompoundDrawablePadding(8);
                        mSearchEdit2.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_course_arrival), null, null, null);
                        mSearchEdit2.setCompoundDrawablePadding(8);
                        mSearchEdit1.setHint("");
                        mSearchEdit2.setHint("");


                        Location location = new SmarterLocationManager(getActivity(), false, checkNetwordState()).getBestLastKnownLocation();

                        double latitude = 37.564093; // default 시청역
                        double longitude = 126.976681;

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }


                        new GetAddress().execute(String.valueOf(latitude) + "," + String.valueOf(longitude), "start", "true");

                    }


                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    // TODO Auto-generated method stub

                }
            });


            //위젯 생성시 오류나는 부분 수정
            if (mWidgetMode == Constants.WIDGET_MODE_NOTHING) {
                mSearchMode = getSearchMode();
                setMode(getSearchMode());

                if (getSearchMode() == Constants.SETTING_SEARCHMODE_BUS) {
                    mSearchEdit1.setPrivateImeOptions("defaultInputmode=numeric");
                    mSearchEdit1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                    mSearchEdit1.setHint(Html.fromHtml("<small>" + getString(R.string.hint_bus_search) + "</small>"));
                } else if (getSearchMode() == Constants.SETTING_SEARCHMODE_STOP) {
                    mSearchEdit1.setPrivateImeOptions("defaultInputmode=korea");
                    mSearchEdit1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                    mSearchEdit1.setHint(Html.fromHtml("<small>" + getString(R.string.hint_stop_search) + "</small>"));
                }

            } else {
                //mSearchMode = Constants.WIDGET_MODE_STOP;
                setMode(Constants.SERCH_MODE_STOP);
                mSearchEdit1.setPrivateImeOptions("defaultInputmode=korea");
                mSearchEdit1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                mSearchEdit1.setHint(Html.fromHtml("<small>" + getString(R.string.hint_stop_search) + "</small>"));
            }


            if (mWidgetMode == Constants.WIDGET_MODE_NOTHING) {
                int tempMode = getSearchMode();
                mSearchSpin.setSelection(tempMode);

                if (!mIsInit) {
                    Cursor cursor = getCursorByName("", mSearchMode);
                    mSchAdapter.changeCursor(cursor);
                }
            }

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }


    class GetAddress extends AsyncTask<String, String, String> {


        String mode;
        String resultStr;

        boolean _isCur = false;

        @Override
        protected String doInBackground(String... params) {

            String latlong = params[0];
            mode = params[1];

            if (params.length > 2) {
                _isCur = true;
            }


            String url = "http://maps.googleapis.com/maps/api/geocode/xml?latlng=" + latlong + "&sensor=true";
            String result = RequestCommonFuction.getSource(url, false, "", "utf-8");


            try {
                Document doc = Jsoup.parse(result);
                Elements elements = doc.select("formatted_address");

                if (elements.size() > 0) {
                    String[] address = elements.get(0).text().split(" ");

                    if (address.length > 1) {
                        resultStr = address[address.length - 2] + " " + address[address.length - 1];


                        if (mode.equals("start")) {


                            if (mStartAutoItem == null) {
                                //현재 위치를 앱 시작시에 가져오는 부분이 있어서 추가해놓음
                                mStartAutoItem = new AutoItem();
                                mStartAutoItem.latitude = Double.parseDouble(params[0].split(",")[0]);
                                mStartAutoItem.longtude = Double.parseDouble(params[0].split(",")[1]);
                            }

                            mStartAutoItem.city1 = address[1];
                            mStartAutoItem.city2 = address[2];

                            mStartAutoItem.cityId = getCityId(address[1], address[2]);
                            mStartAutoItem.name = resultStr;


                        } else {

                            if (mEndAutoItem == null) {
                                mEndAutoItem = new AutoItem();
                            }


                            mEndAutoItem.city1 = address[1];
                            mEndAutoItem.city2 = address[2];


                            mEndAutoItem.cityId = getCityId(address[1], address[2]);
                            mEndAutoItem.name = resultStr;


                        }
                    }

                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            ;

            return null;

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (mode.equals("start")) {
                if (_isCur) {
                    mSearchEdit1.setText("현재 위치");
                } else {
                    mSearchEdit1.setText(resultStr);
                }
            } else {
                mSearchEdit2.setText(resultStr);
            }

            itemRouteSearchCheck();

        }
    }


    class GetTogoResult extends AsyncTask<String, String, String> {


        public boolean _isResult = false;
        public boolean _isTago = false;
        public TagoArrayItem _tagoItem;
        public boolean _isReCheck = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setProgress(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            if (params[0].equals("true")) {

                _isTago = true;
                _tagoItem = new TagoArrayItem();

                String url = "http://tago.go.kr/transportation/publicRoutingSearchResultWeb_return.service";

                GeoPoint in_pt = new GeoPoint(mStartAutoItem.longtude, mStartAutoItem.latitude);
                GeoPoint in2_pt = new GeoPoint(mEndAutoItem.longtude, mEndAutoItem.latitude);

                GeoPoint startOutput = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, in_pt);
                GeoPoint endOutput = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, in2_pt);


                String urlParam = String.format("searchType=0&resultCount=5&radius=500:1000&sortOpt=0&inType=2&specialOption=undefined,N,0,N&SX=%s&SY=%s&EX=%s&EY=%s", startOutput.getX(), startOutput.getY(), endOutput.getX(), endOutput.getY());
                String result = RequestCommonFuction.getSource(url, true, urlParam, "utf-8");


                if (result == null) return null;

                try {
                    Document doc = Jsoup.parse(result);
                    Elements elements = doc.select("path");

                    for (int i = 0; i < elements.size(); i++) {

                        String totalStationCount = elements.get(i).select("info").select("totalStationCount").text().trim();
                        String totalTime = elements.get(i).select("info").select("totalTime").text().trim();
                        String totalDistance = elements.get(i).select("info").select("totalDistance").text().trim();
                        String totalWalk = elements.get(i).select("info").select("totalWalk").text().trim();
                        String mapId = elements.get(i).select("info").select("mapObj").text().trim();

                        //갑자기 안되서 예외 조건 추가
                        if (mapId == null || mapId.equals("")) continue;

                        TagoItem tagoItem = new TagoItem();

                        tagoItem.totalDistance = totalDistance;
                        tagoItem.totalWalk = totalWalk;
                        tagoItem.totalStationCount = totalStationCount;
                        tagoItem.totalTime = totalTime;
                        tagoItem.mapId = mapId;

                        Elements subElements = elements.get(i).select("sub");


                        for (int j = 1; j < subElements.size() + 1; j++) {
                            if (j % 2 == 0) {
                                TagoBusItem tagoBusItem = new TagoBusItem();
                                tagoBusItem.busNo = subElements.get(j - 1).select("busNo").text().trim().replace("<![CDATA[ ", "").replace("]]>", "").trim();
                                tagoBusItem.startName = subElements.get(j - 1).select("startName").text().trim().replace("<![CDATA[ ", "").replace("]]>", "").trim();
                                tagoBusItem.endName = subElements.get(j - 1).select("endName").text().trim().replace("<![CDATA[ ", "").replace("]]>", "").trim();
                                tagoItem.mTagoBusList.add(tagoBusItem);

                            }
                        }

                        _tagoItem.tagoArrayItem.add(tagoItem);
                    }

                    _isResult = true;

                } catch (Exception e) {
                    e.printStackTrace();
                    _isResult = false;
                }
                ;
            } else {

                int runCount = 1;

                //검색 안될 시 범위를 넓힌다.
                while (runCount < 3) {
                    _tagoItem = searchLocal(4000 * runCount);

                    if (_tagoItem.tagoArrayItem.size() == 0) {
                        runCount++;
                    } else {
                        break;
                    }
                }

                _isResult = true;


            }


            if (_tagoItem.tagoArrayItem == null || _tagoItem.tagoArrayItem.size() == 0) {
                _isResult = false;

                if (!params[0].equals("true")) {
                    _isReCheck = true;
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            if (_isResult) {

                if (_tagoItem != null && _tagoItem.tagoArrayItem.size() > 0) {
                    Intent intent = new Intent(getActivity(), SBPathResultActivity.class);
                    intent.putExtra(Constants.INTENT_TAGOITEM, _tagoItem);
                    intent.putExtra(Constants.INTENT_STARTITEM, mStartAutoItem);
                    intent.putExtra(Constants.INTENT_ENDITEM, mEndAutoItem);

                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "결과값이 없습니다.", Toast.LENGTH_SHORT).show();
                }

            } else {
                if (_isReCheck) {
                    new GetTogoResult().execute("true");
                } else {
                    Toast.makeText(getActivity(), "결과값이 없습니다.", Toast.LENGTH_SHORT).show();
                }

            }

            setProgress(View.GONE);

        }

    }


    class BusListItem {
        int stopId;
        String stopName;
        double x;
        double y;
        ArrayList<String> busNumList = new ArrayList<String>();
    }

    class FavoriteCheckItem extends FavoriteAndHistoryItem {
        boolean isChecked = false;
    }

    //special
    public ArrayList<BusRouteItem> getSBSelctBusRouteList() {
        return mSBselectFragment.getSBSelctBusRouteList();
    }

    private boolean checkNetwordState() {

        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isNetworkConnected = false;

        if (connManager != null) {

            NetworkInfo state_3g = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo state_wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            boolean is3gConnected = false;
            boolean isWifiConnected = false;

            if (state_3g != null)
                is3gConnected = state_3g.isConnected();
            if (state_wifi != null)
                isWifiConnected = state_wifi.isConnected();

            isNetworkConnected = is3gConnected || isWifiConnected;
        }

        return isNetworkConnected;
    }
}