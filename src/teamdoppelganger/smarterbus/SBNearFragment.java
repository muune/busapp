package teamdoppelganger.smarterbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.lib.viewpagerindicator.TabPageIndicator;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.NearbyStationDialog;
import teamdoppelganger.smarterbus.util.widget.NearbyStationDialog.LocationCatchListener;
import teamdoppelganger.smarterbus.util.widget.RealtiveTouchLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.smart.lib.CommonConstants;

/**
 * 부산시 exception (tempid2 활용)
 *
 * @author DOPPELSOFT4
 */
@SuppressLint("ValidFragment")
public class SBNearFragment extends SBBaseFragment implements OnMarkerClickListener {

    private static final int MSG_FIND_LOCATION = 0;


    GoogleMap mGoogleMap;
    RealtiveTouchLayout mRelativeLayout;
    ListView mListView;
    RelativeLayout mProgressLayout;

    boolean mIsFirst = true;
    boolean mIsInit = false;
    boolean isLocationUse = false;

    Location mLocation;
    Marker mCurrentMarker;

    ArrayList<Marker> mBusStopMarker;
    ArrayList<MarkerOptions> mMarkerOptions;
    ArrayList<BusStopItem> mBusStopItems;
    ArrayList<BusStopItem> mBusStopItemResult;
    ArrayList<TempRouteItem> mTempRouteItem;
    AutoCompleteTextView mAutoCompleteTextView;
    ArrayList<String> mTempRouteName;


    Runnable mRunnable;

    int mLat = 0, mLong = 0;
    boolean mIsCancel = false;
    boolean mIsSetLocationFirst = false;

    LocalAsync mLocalAsync;

    NearListAdapter mNearListAdapter;

    AlertDialog mLocationAgreeDialog;
    NearbyStationDialog mNearbyStationDialog;

    SharedPreferences mPref;

    Handler mHandler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_FIND_LOCATION:


                    break;
            }

            return false;
        }
    });


    public SBNearFragment(int id, SQLiteDatabase db,
                          LocalDBHelper localDBHelper) {
        super(id, db, localDBHelper);
    }

    public SBNearFragment() {

    }


    @Override
    public void onLayoutFinish(View view) {
        super.onLayoutFinish(view);

        setActionbarTitle(getResources().getString(R.string.menu4));

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        mBusStopMarker = new ArrayList<Marker>();
        mBusStopItems = new ArrayList<BusStopItem>();
        mBusStopItemResult = new ArrayList<BusStopItem>();
        mTempRouteItem = new ArrayList<TempRouteItem>();
        mTempRouteName = new ArrayList<String>();
        mMarkerOptions = new ArrayList<MarkerOptions>();

        initView(view);

        mRunnable = new Runnable() {

            @Override
            public void run() {

            }
        };

    }


    private void setLocation() {

        if (mIsSetLocationFirst) return;

        GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        double latitude = 37.564093; // default 시청역
        double longitude = 126.976681;

        if (mLocation != null) {

            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();

        } else {

            float prefLatitude = mPref.getFloat(Constants.PREF_LAST_LOCATION_LAT, 0);
            float prefLongitude = mPref.getFloat(Constants.PREF_LAST_LOCATION_LON, 0);

            if (prefLatitude != 0 && prefLongitude != 0) {
                latitude = prefLatitude;
                longitude = prefLongitude;
            } else {

                if (getRecentStopY() != 0)
                    latitude = (double) getRecentStopY() / 1000000;
                if (getRecentStopX() != 0)
                    longitude = (double) getRecentStopX() / 1000000;
            }

        }

        mLat = (int) (latitude * 1e6);
        mLong = (int) (longitude * 1e6);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

        mNearListAdapter = new NearListAdapter();
        mListView.setAdapter(mNearListAdapter);

        mIsSetLocationFirst = true;
    }

    private void initView(View v) {

        mRelativeLayout = (RealtiveTouchLayout) v.findViewById(R.id.mapLayout);
        mListView = (ListView) v.findViewById(R.id.listView);
        mProgressLayout = (RelativeLayout) v.findViewById(R.id.progressLayout);

        View view = getActivity().getLayoutInflater().inflate(R.layout.map, null);
        mRelativeLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        mRelativeLayout.addView(view);

        mAutoCompleteTextView = (AutoCompleteTextView) v.findViewById(R.id.nearSearchEdit);
        mAutoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                String busRouteNum = mAutoCompleteTextView.getText().toString();
                mBusStopItemResult.clear();

                for (int i = 0; i < mBusStopItems.size(); i++) {

                    BusStopItem busStopItem = mBusStopItems.get(i);

                    if (busStopItem.relatedRoutes.contains(busRouteNum)) {

                        mBusStopMarker.get(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_focused));

                        mBusStopItemResult.add(busStopItem);
                    } else {
                        mBusStopMarker.get(i).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_disabled_normal));
                    }
                }


                if (mNearListAdapter != null) {
                    mNearListAdapter.notifyDataSetChanged();
                }


                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mAutoCompleteTextView.getWindowToken(), 0);


            }


        });


        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().length() == 0) {

                    mBusStopItemResult.clear();
                    mBusStopItemResult.addAll(mBusStopItems);

                    if (mNearListAdapter != null) {
                        mNearListAdapter.notifyDataSetChanged();
                    }

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


        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                BusStopItem busStopItem = mBusStopItemResult.get(position);
                Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                sendIntent.putExtra("busInfor", busStopItem);
                startActivity(sendIntent);
            }
        });

    }

    private void loadGoogleMap() {

        setProgress(View.VISIBLE);


        mGoogleMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map)).getMap();


        if (mGoogleMap == null) {
            setProgress(View.GONE);
            mIsInit = false;
            Toast.makeText(getActivity(), getResources().getString(R.string.dialog_msg_googlemap_null), Toast.LENGTH_SHORT).show();
            return;
        }

        mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {

                View viewWindow = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);
                TextView name = (TextView) viewWindow.findViewById(R.id.name);
                TextView subName = (TextView) viewWindow.findViewById(R.id.subname);


                String[] splitString = marker.getSnippet().split("/");

                name.setText(marker.getTitle());
                subName.setText(splitString[2]);


                return viewWindow;

            }

            @Override
            public View getInfoContents(Marker marker) {

                return null;
            }
        });


        mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mLocation = mGoogleMap.getMyLocation();
                loadGoogleMap();
                return false;
            }
        });

        mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {

                LatLng latLang = marker.getPosition();


                if (marker.getSnippet() == null) return;

                BusStopItem busStopItem = new BusStopItem();

                String[] splitStr = marker.getSnippet().split("/");
                String name = splitStr[0];
                String apiId = splitStr[1];
                String arsId = splitStr[2];
                String cityId = splitStr[3];


                busStopItem.name = name;
                busStopItem.apiId = apiId;
                busStopItem.arsId = arsId;
                busStopItem.localInfoId = cityId;

                if (splitStr.length > 4) {
                    String description = splitStr[4];
                    busStopItem.tempId2 = description;
                }


                Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                sendIntent.putExtra("busInfor", busStopItem);
                startActivity(sendIntent);

            }
        });

        mLocalAsync = new LocalAsync();


        mIsCancel = false;

        mLocalAsync.execute();
    }


    @Override
    public void selectedPage() {

        super.selectedPage();

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_boundary));
        t.send(new HitBuilders.AppViewBuilder().build());

        if (TabPageIndicator.NOW_POSITION != 3)
            return;

        if ((Constants.MARKET_TYPE == Constants.MARKET_ANDROID || Constants.MARKET_TYPE == Constants.MARKET_NAVER) && !mPref.getBoolean("gps_contents_agreed", false)) {
            lbsAgreeDialog();
            return;
        }

        if (mIsInit) {

        } else {
            mLocation = getBestLastKnownLocation();
            loadGoogleMap();
            mProgressLayout.setVisibility(View.VISIBLE);
            setNearbyStationDialog();
        }


        if (mGoogleMap != null) {
            try {
                mGoogleMap.setMyLocationEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ;

        }


        mIsInit = true;
    }

    @Override
    public void unSelectedPage() {
        super.unSelectedPage();

        if (mAutoCompleteTextView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mAutoCompleteTextView.getWindowToken(), 0);
        }


        try {
            mGoogleMap.setMyLocationEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class NearListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mBusStopItemResult.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            ViewHolder viewHolder;

            if (convertView == null) {

                convertView = getActivity().getLayoutInflater().inflate(R.layout.near_row,
                        parent, false);


                viewHolder = new ViewHolder();

                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.subName = (TextView) convertView.findViewById(R.id.subname);
                viewHolder.img = (ImageView) convertView.findViewById(R.id.icon);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            final BusStopItem stopItem = mBusStopItemResult.get(position);


            viewHolder.name.setText(stopItem.name);
            viewHolder.subName.setText(stopItem.arsId);

            viewHolder.img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {


                    BusStopItem stopItem = mBusStopItemResult.get(position);
                    LatLng latLang = new LatLng(stopItem.latitude, stopItem.longtitude);


                    for (int i = 0; i < mBusStopItems.size(); i++) {
                        if (mBusStopItems.get(i).arsId == stopItem.arsId
                                && mBusStopItems.get(i).apiId == stopItem.apiId
                                && mBusStopItems.get(i).name == stopItem.name) {

                            mBusStopMarker.get(i).showInfoWindow();


                            break;
                        }
                    }

                    mLat = (int) (latLang.latitude * 1e6);
                    mLong = (int) (latLang.longitude * 1e6);


                    CameraPosition cp = new CameraPosition.Builder().target((latLang))
                            .zoom(16).build();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cp));


                }
            });


            return convertView;
        }


        class ViewHolder {
            TextView name, subName;
            ImageView img;
        }
    }


    class TempRouteItem {
        String id;
        String name;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO Auto-generated method stub
        return false;
    }


    class LocalAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


            setLocation();


            if (mLocation != null) {
                LatLng loc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude()); // 위치 좌표 설정
                CameraPosition cp = new CameraPosition.Builder().target((loc))
                        .zoom(16).build();
            }


            mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                @Override
                public void onCameraChange(CameraPosition position) {

                    double currnetLat = position.target.latitude * 1e6;
                    double currentLong = position.target.longitude * 1e6;


                    if (mAutoCompleteTextView.getText().length() > 0) {
                        mRelativeLayout.resetTouchAction();
                        return;
                    }

                    if ((Math.abs((int) currnetLat - mLat) > 4000 ||
                            Math.abs((int) currentLong - mLong) > 4000) && mRelativeLayout.getTouchAction()) {


                        double latitude = position.target.latitude;
                        double longitude = position.target.longitude;

                        mRelativeLayout.resetTouchAction();
                        mLat = (int) currnetLat;
                        mLong = (int) currentLong;

                        if (mLocalAsync != null) {
                            mLocalAsync.cancel(true);
                        }
                        setProgress(View.VISIBLE);

                        mLocalAsync = new LocalAsync();


                        mIsCancel = false;


                        mLocalAsync.execute();


                    }


                }
            });


            //init
            if (mBusStopMarker != null) {
                for (int i = 0; i < mBusStopMarker.size(); i++) {
                    mBusStopMarker.get(i).remove();
                }
            }

        }

        @Override
        protected String doInBackground(String... params) {

            mBusStopItems.clear();
            mTempRouteName.clear();
            mMarkerOptions.clear();
            mBusStopMarker.clear();


            String citySql = String.format("Select *From %s", CommonConstants.TBL_CITY);
            Cursor cursor = getBusDbSqlite().rawQuery(citySql, null);
            String tableName = "";

            HashMap<Integer, String> hashMap = new HashMap<Integer, String>();

            while (cursor.moveToNext()) {
                String enName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
                String cityId = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID));
                String tempTable = String.format("SELECT *,'%s' as cityID FROM %s_STOP", cityId, enName);


                hashMap.put(Integer.parseInt(cityId), enName);

                if (tableName.equals("")) {
                    tableName = tempTable;
                } else {
                    tableName = tableName + " union ALL " + tempTable;
                }

                if (mIsCancel) {

                    return null;
                }
            }


            cursor.close();

            String qryStopInfor = String.format("SELECT * from (%s)  WHERE %s>'%d' and %s<'%d' and %s>'%d' and %s<'%d' and (%s<>%s )  ",
                    tableName, CommonConstants.BUS_STOP_LOCATION_Y, (int) (mLat - 4000),
                    CommonConstants.BUS_STOP_LOCATION_Y, (int) (mLat + 4000),
                    CommonConstants.BUS_STOP_LOCATION_X, (int) (mLong - 4000),
                    CommonConstants.BUS_STOP_LOCATION_X, (int) (mLong + 4000), CommonConstants.BUS_STOP_ARS_ID, "0");


            Cursor getLoationCursor = getBusDbSqlite().rawQuery(qryStopInfor, null);

            while (getLoationCursor.moveToNext()) {

                if (isCancelled()) {

                    return null;
                }

                String locationName = getLoationCursor.getString(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                String busStopApiId = getLoationCursor.getString(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                String busStopArsId = getLoationCursor.getString(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                String cityId = getLoationCursor.getString(getLoationCursor.getColumnIndex("cityID"));
                String description = getLoationCursor.getString(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));

                double locationX = getLoationCursor.getDouble(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
                double locationY = getLoationCursor.getDouble(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));


                String[] relatedRoutes = getLoationCursor.getString(getLoationCursor.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES)).split("/");


                double lat = (locationY / 1e6);
                double longti = (locationX / 1e6);

                BusStopItem busStopItem = new BusStopItem();
                busStopItem.name = locationName;
                busStopItem.latitude = lat;
                busStopItem.longtitude = longti;
                busStopItem.localInfoId = cityId;
                busStopItem.apiId = busStopApiId;
                busStopItem.arsId = busStopArsId;


                if (locationName.equals(CommonConstants.CITY_BU_SAN._engName)) {
                    busStopItem.tempId2 = description;
                }


                for (int i = 0; i < relatedRoutes.length; i++) {

                    String relateId = relatedRoutes[i];

                    String busQry = String.format("SELECT *FROM %s_route where %s='%s'", hashMap.get(Integer.parseInt(cityId)), CommonConstants._ID, relateId);

                    Cursor busCursor = getBusDbSqlite().rawQuery(busQry, null);
                    if (busCursor.moveToNext() && !mIsCancel) {
                        String busName = busCursor.getString(busCursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
                        if (!mTempRouteName.contains(busName)) {
                            mTempRouteName.add(busName);
                        }
                        busStopItem.relatedRoutes.add(busName);
                    }

                    if (isCancelled()) {

                        return null;
                    }

                    busCursor.close();

                }


                if (isCancelled()) {

                    return null;
                }
                mBusStopItems.add(busStopItem);


                LatLng locationDe = new LatLng(lat, longti); // 위치 좌표 설정
                String snippet = busStopItem.name + "/" + busStopItem.apiId + "/" + busStopItem.arsId + "/" + cityId + "/" + description;


                MarkerOptions markerOptions = new MarkerOptions().position(locationDe).title(locationName).snippet(snippet).icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_disabled_normal));
                mMarkerOptions.add(markerOptions);


            }

            getLoationCursor.close();


            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            try {
                if (!isCancelled()) {
                    mBusStopItemResult.clear();
                    mBusStopItemResult.addAll(mBusStopItems);


                    for (int i = 0; i < mMarkerOptions.size(); i++) {
                        Marker marker = mGoogleMap.addMarker(mMarkerOptions.get(i));
                        mBusStopMarker.add(marker);
                    }

                    if (mAutoCompleteTextView != null) {
                        mAutoCompleteTextView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, mTempRouteName));
                    }

                    if (mNearListAdapter != null) {
                        mNearListAdapter.notifyDataSetChanged();
                    }


                }


            } catch (Exception e) {
            }

            mProgressLayout.setVisibility(View.GONE);
            setProgress(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsCancel = true;

            setProgress(View.GONE);
        }


    }

    private void lbsAgreeDialog() {

        final SBDialog dialog = new SBDialog(getActivity());
        dialog.setTitleLayout(getString(R.string.dialog_title_lbs_agree));
        dialog.setViewLayout(getString(R.string.dialog_msg_lbs_agree));
        dialog.getPositiveButton(getString(R.string.dialog_positive)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor edit = mPref.edit();
                edit.putBoolean("gps_contents_agreed", true);
                edit.commit();
                selectedPage();
                dialog.dismiss();
            }
        });
        dialog.getNegativeButton(getString(R.string.dialog_negative)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLBSAgreedListner != null)
                    mLBSAgreedListner.cancel();
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    void setNearbyStationDialog() {

        mNearbyStationDialog = new NearbyStationDialog(getActivity(), checkNetwordState());
        mNearbyStationDialog.setLocationCatchListener(new LocationCatchListener() {

            @Override
            public void onReceive(Location location) {
                mLocation = location;
                mIsSetLocationFirst = false;

                if (mLocation != null) {
                    SharedPreferences.Editor ed = mPref.edit();
                    ed.putFloat(Constants.PREF_LAST_LOCATION_LAT, (float) mLocation.getLatitude());
                    ed.putFloat(Constants.PREF_LAST_LOCATION_LON, (float) mLocation.getLongitude());
                    ed.commit();
                }

                loadGoogleMap();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


        if (mNearbyStationDialog != null) {
            mNearbyStationDialog.destroy();
            mNearbyStationDialog.setLocationCatchListener(null);
        }


        if (mLocationAgreeDialog != null) {
            mLocationAgreeDialog.dismiss();
        }


        if (mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.setOnMyLocationChangeListener(null);
        }


    }

    public Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);


        return bitmap;
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

    public Location getBestLastKnownLocation() {

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        List<String> matchingProviders = locationManager.getAllProviders();
        float bestAccuracy = Float.MAX_VALUE;
        Location bestResult = null;

        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {

                float accuracy = location.getAccuracy();

                if (accuracy > bestAccuracy) continue;

                bestResult = location;
                bestAccuracy = accuracy;

            }
        }

        return bestResult;

    }
}
