package teamdoppelganger.smarterbus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.webkit.WebView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.smart.lib.CommonConstants;

import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.item.AutoItem;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.TagoArrayItem;
import teamdoppelganger.smarterbus.item.TagoItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GeoPoint;
import teamdoppelganger.smarterbus.util.common.GeoTrans;
import teamdoppelganger.smarterbus.util.common.GetStringData;
import teamdoppelganger.smarterbus.util.common.GetStringData.GetStringDataListenr;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

public class SBMapFragment extends SBBaseFragment {

    GoogleMap mGoogleMap;
    String mApiId;
    String mRouteData;
    String mCityName;

    int mMode;

    ArrayList<LatLng> mPolyz = null;

    ArrayList<BusStopItem> mBusStopItemAry;
    ArrayList<Marker> mMarkerList;
    LatLngBounds.Builder mCenterBc;

    BusRouteItem mBusRouteItem;
    int mId;
    int mStartIndex, mEndIndex;


    boolean mIsCameraInit = false;


    Handler mHandler = null;
    Runnable mRunnable = null;

    public HashMap<Integer, String> mHashLocation;
    SharedPreferences mPref;

    TagoArrayItem mTagoAryItem;
    AutoItem mStartAutoItem, mEndAutoItem;
    int mTagoPosition;


    AsynDBData mAsynDBData;
    GetStringData getStringData;
    AsynTagoData mAsynTagoData;

    LatLng mLocationDe;

    WebView mWebView;

    public SBMapFragment() {
    }

    @SuppressLint("ValidFragment")
    public SBMapFragment(SQLiteDatabase db,
                         LocalDBHelper localDBHelper) {
        super(R.layout.map_route, db, localDBHelper);

        mPolyz = new ArrayList<LatLng>();
        mMarkerList = new ArrayList<Marker>();

        mHandler = new Handler();

        mCenterBc = new LatLngBounds.Builder();


        //주변 정류장을 위해서
        mBusStopMarker = new ArrayList<Marker>();
        mBusStopItems = new ArrayList<BusStopItem>();
        mMarkerOptions = new ArrayList<MarkerOptions>();
        mBusStopItemResult = new ArrayList<BusStopItem>();


    }


    @Override
    public void onLayoutFinish(View view) {
        super.onLayoutFinish(view);

        mWebView = (WebView) view.findViewById(R.id.web);


        mGoogleMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        mGoogleMap.setMyLocationEnabled(true);

        if (mGoogleMap != null) {

            double latitude = 37.564093; // default 시청역
            double longitude = 126.976681;

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 8.0f));
        }

        init();

        view.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {


                return false;
            }
        });

    }


    private void init() {


        mGoogleMap.setOnCameraChangeListener(new OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition position) {
                if (mIsCameraInit) return;
                mIsCameraInit = true;


                if (mMode == Constants.MAP_MODE_STOP_PATH) {
                    if (mCenterBc != null && mLocationDe != null) {
                        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mCenterBc.build(), 50));

                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocationDe.latitude, mLocationDe.longitude), 15.0f));
                        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocationDe, 15.0f));
                    }
                }
            }
        });


        if (mMode == Constants.MAP_MODE_ROUTE_PATH) {
            mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);

            mHashLocation = new HashMap<Integer, String>();
            String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);
            Cursor cursor = getBusDbSqlite().rawQuery(tmpSql, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
                String cityEnName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
                mHashLocation.put(id, cityEnName);
            }
            cursor.close();


            mAsynDBData = new AsynDBData();
            mAsynDBData.execute();


            String id = "0", id2 = "0";
            if (mBusRouteItem.busRouteApiId != null) {
                id = mBusRouteItem.busRouteApiId;
            }

            if (mBusRouteItem.busRouteApiId2 != null) {
                id2 = mBusRouteItem.busRouteApiId2;
            }

            String param = String.format("id=%s&id2=%s&name=%s&localName=%s&type=android", id, id2, mBusRouteItem.busRouteName, mCityName);

            getStringData = new GetStringData();
            getStringData.setGetStringDataListener(new GetStringDataListenr() {
                @Override
                public void onGetStringDataResult(String resultStr) {

                    String[] splitData = resultStr.split(",");
                    for (int i = 0; i < splitData.length; i++) {
                        String[] dataXY = splitData[i].split(" ");
                        if (dataXY.length > 1) {
                            LatLng latLng = new LatLng(Double.parseDouble(dataXY[1]), Double.parseDouble(dataXY[0]));
                            mPolyz.add(latLng);
                        }
                    }

                    for (int i = 0; i < mPolyz.size() - 1; i++) {
                        LatLng src = mPolyz.get(i);
                        LatLng dest = mPolyz.get(i + 1);
                        Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
                                .add(new LatLng(src.latitude, src.longitude),
                                        new LatLng(dest.latitude, dest.longitude))
                                .width(14).color(Color.BLUE));
                    }

                }
            });

            getStringData.execute(Constants.LOCAL_URL + Constants.PATH_GET_URL, "false", param, "utf-8");
        } else if (mMode == Constants.MAP_MODE_TAGO_PATH) {
            mAsynTagoData = new AsynTagoData();
            mAsynTagoData.execute();
        } else if (mMode == Constants.MAP_MODE_STOP_PATH) {


            String sql = String.format("SELECT *FROM %s_stop where _id=%s", mCityName, mId);
            Cursor cursor = getBusDbSqlite().rawQuery(sql, null);
            if (cursor.moveToNext()) {
                double latitude = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y)) / 1e6;
                double longtitude = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X)) / 1e6;

                String name = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_focused);
                mLocationDe = new LatLng(latitude, longtitude); // 위치 좌표 설정
                mCenterBc.include(mLocationDe);
                mMarkerOption = new MarkerOptions().position(mLocationDe).title(name).icon(icon);


                mLat = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));
                mLong = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));

                new LocalAsync().execute();

            }


            cursor.close();
        }

    }

    public void setBusStopItem(int id, String cityName) {
        mBusStopItemAry = new ArrayList<BusStopItem>();
        mId = id;
        mCityName = cityName;
    }


    public void setBusStopItem(String cityName) {
        mBusStopItemAry = new ArrayList<BusStopItem>();
        mCityName = cityName;
    }


    public void setCityName(String cityName) {
        mCityName = cityName;
    }


    public void setBusRouteItem(BusRouteItem busRouteItem) {
        mBusRouteItem = busRouteItem;
    }

    public void setTagoItem(TagoArrayItem tagoArrayItem) {
        mTagoAryItem = tagoArrayItem;
    }

    public void setSelectTagoPosition(int position) {
        mTagoPosition = position;
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    public void setStartEndItem(AutoItem startItem, AutoItem endItem) {
        mStartAutoItem = startItem;
        mEndAutoItem = endItem;
    }

    public void setIndex(int startIndex, int endIndex) {
        mStartIndex = startIndex;
        mEndIndex = endIndex;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }

        if (mAsynDBData != null) {
            mAsynDBData.cancel(true);
        }


    }


    class AsynDBData extends AsyncTask<String, String, String> {


        HashMap<String, String> mTmpHash = new HashMap<String, String>();

        @Override
        protected void onPreExecute() {

            setProgress(View.VISIBLE);
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            String sql = String.format("SELECT * FROM %s_route where %s=%s ", mCityName, CommonConstants._ID, mBusRouteItem._id);
            Cursor cursor = getBusDbSqlite().rawQuery(sql, null);
            ArrayList<String> relateArray = new ArrayList<String>();
            cursor.moveToNext();
            String[] relateStop = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS)).split("/");

            cursor.close();


            for (int i = 0; i < relateStop.length; i++) {
                sql = String.format("SELECT *FROM %s_stop where %s=%s", mCityName, CommonConstants._ID, relateStop[i]);
                Cursor tmpCursor = getBusDbSqlite().rawQuery(sql, null);

                if (tmpCursor.moveToNext()) {
                    String arsId = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                    String apiId = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));

                    String name = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                    double latitude = tmpCursor.getInt(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y)) / 1e6;
                    double longtitude = tmpCursor.getInt(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X)) / 1e6;


                    BusStopItem busStopItem = new BusStopItem();
                    busStopItem.arsId = arsId;
                    busStopItem.apiId = apiId;
                    busStopItem.name = name;
                    busStopItem.latitude = longtitude;
                    busStopItem.longtitude = latitude;


                    mBusStopItemAry.add(busStopItem);
                }

                tmpCursor.close();

            }


            return null;
        }


        @Override
        protected void onPostExecute(String results) {
            setProgress(View.GONE);
            super.onPostExecute(results);
            if (isCancelled()) return;

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.busstop);

            for (int i = 0; i < mBusStopItemAry.size(); i++) {
                BusStopItem item = mBusStopItemAry.get(i);


                double latitude;
                double longtitude;


                latitude = item.longtitude;
                longtitude = item.latitude;

                LatLng locationDe = new LatLng(latitude, longtitude); // 위치 좌표 설정

                mCenterBc.include(locationDe);

                String snippet = item.name + "/" + item.apiId + "/" + item.arsId + "/" + mId + "/";
                MarkerOptions markerOptions = new MarkerOptions().position(locationDe).title(item.name).snippet(snippet).icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_disabled_normal));
                Marker marker = mGoogleMap.addMarker(markerOptions);

                if (mGoogleMap != null) {
                    mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker marker) {

                            View viewWindow = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);
                            TextView name = (TextView) viewWindow.findViewById(R.id.name);
                            TextView subName = (TextView) viewWindow.findViewById(R.id.subname);


                            String[] splitString = marker.getSnippet().split("/");

                            name.setText(marker.getTitle());

                            if (splitString[2].equals("0")) {
                                subName.setVisibility(View.GONE);
                            } else {
                                subName.setText(splitString[2]);
                                subName.setVisibility(View.VISIBLE);
                            }


                            return viewWindow;

                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            return null;
                        }
                    });


                    mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

                        @Override
                        public void onInfoWindowClick(Marker marker) {

                            LatLng latLang = marker.getPosition();

                            if (marker.getSnippet() == null) return;

                            String[] splitStr = marker.getSnippet().split("/");
                            String name = splitStr[0];
                            String apiId = splitStr[1];
                            String arsId = splitStr[2];
                            String cityId = splitStr[3];

                            BusStopItem busStopItem = new BusStopItem();
                            busStopItem.name = name;
                            busStopItem.apiId = apiId;
                            busStopItem.arsId = arsId;
                            busStopItem.localInfoId = cityId;

                            Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                            sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                            sendIntent.putExtra("busInfor", busStopItem);
                            startActivity(sendIntent);

                        }
                    });

                }


            }


            CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLngBounds(mCenterBc.build(), 50);
            mGoogleMap.moveCamera(cameraUpdateFactory);

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }


    }


    class AsynTagoData extends AsyncTask<String, String, String> {

        ArrayList<XYItem> _pathItem;
        ArrayList<XYItem> _transferpathItem;
        ArrayList<XYItem> _tmpPathItem;

        String mode = "";

        @Override
        protected String doInBackground(String... params) {

            String mapId = null;
            _pathItem = new ArrayList<XYItem>();
            _transferpathItem = new ArrayList<SBMapFragment.XYItem>();
            _tmpPathItem = new ArrayList<SBMapFragment.XYItem>();   //중간 정류장 표시를 위해서


            mapId = mTagoAryItem.tagoArrayItem.get(mTagoPosition).mapId;

            if (mapId == null) {

                mode = "local";


                String sql = String.format("SELECT * FROM %s_route where %s=%s ", mCityName, CommonConstants._ID, mTagoAryItem.tagoArrayItem.get(mTagoPosition).busId);
                Cursor cursor = getBusDbSqlite().rawQuery(sql, null);
                ArrayList<String> relateArray = new ArrayList<String>();
                cursor.moveToNext();
                String[] relateStop = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS)).split("/");


                cursor.close();


                int startIndex = 0, endIndex = 0;
                for (int i = 0; i < relateStop.length; i++) {
                    sql = String.format("SELECT *FROM %s_stop where %s=%s", mCityName, CommonConstants._ID, relateStop[i]);
                    Cursor tmpCursor = getBusDbSqlite().rawQuery(sql, null);

                    if (tmpCursor.moveToNext()) {
                        String id = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants._ID));
                        String arsId = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
                        String apiId = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
                        String name = tmpCursor.getString(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
                        double latitude = tmpCursor.getInt(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y)) / 1e6;
                        double longtitude = tmpCursor.getInt(tmpCursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X)) / 1e6;


                        BusStopItem busStopItem = new BusStopItem();
                        busStopItem.arsId = arsId;
                        busStopItem.apiId = apiId;
                        busStopItem.latitude = longtitude;
                        busStopItem.longtitude = latitude;
                        busStopItem.name = name;


                        if (id.equals(mTagoAryItem.tagoArrayItem.get(mTagoPosition).srcStopId)) {

                            XYItem xyItem = new XYItem();
                            xyItem.x = longtitude;
                            xyItem.y = latitude;
                            xyItem.busStopItem = busStopItem;

                            _transferpathItem.add(xyItem);
                            startIndex = _transferpathItem.size() - 1;
                        } else if (id.equals(mTagoAryItem.tagoArrayItem.get(mTagoPosition).endStopId)) {
                            XYItem xyItem = new XYItem();
                            xyItem.x = longtitude;
                            xyItem.y = latitude;
                            xyItem.busStopItem = busStopItem;
                            _transferpathItem.add(xyItem);
                            endIndex = _transferpathItem.size() - 1;
                        }


                        if (mTagoAryItem.tagoArrayItem.get(mTagoPosition).startIndex < i && mTagoAryItem.tagoArrayItem.get(mTagoPosition).endIndex > i) {
                            XYItem xyItem = new XYItem();
                            xyItem.x = longtitude;
                            xyItem.y = latitude;

                            _tmpPathItem.add(xyItem);

                        }


                    }

                    tmpCursor.close();


                }


                if (startIndex > endIndex) {
                    mEndIndex = startIndex;
                    mStartIndex = endIndex;

                } else {
                    mEndIndex = endIndex;
                    mStartIndex = startIndex;
                }


            } else {

                String url = "http://tago.go.kr/dwr/call/plaincall/NaverTrafficService.getCourseMovingList.dwr";
                String param = String.format("callCount=1&c0-param0=string:%s&scriptSessionId=2FD2E24180FB718E873AD1815CC92FDB680&c0-scriptName=NaverTrafficService&c0-methodName=getCourseMovingList", mapId);

                String result = RequestCommonFuction.getSource(url, true, param, "utf-8");

                if (result == null) return null;


                TagoItem tagoItem = mTagoAryItem.tagoArrayItem.get(mTagoPosition);


                //대략 10으로 잡았음
                for (int k = 0; k < 10; k++) {
                    String strParam = String.format("s%s.utmkPath=", k);
                    try {
                        String[] pathList = result.split(strParam)[1].split(";")[0].replace("\"", "").trim().split("\\^");
                        String tempX = "0";
                        int tempSize = 0;
                        for (int i = 1; i < pathList.length + 1; i++) {
                            if (i % 2 == 0) {
                                String tempY = pathList[i - 1];

                                GeoPoint in_pt = new GeoPoint(Double.parseDouble(tempX), Double.parseDouble(tempY));//
                                GeoPoint utmKS_pt = GeoTrans.convert(GeoTrans.UTMK, GeoTrans.GEO, in_pt);


                                XYItem xyItem = new XYItem();
                                xyItem.x = utmKS_pt.getX();
                                xyItem.y = utmKS_pt.getY();

                                _pathItem.add(xyItem);

                                tempSize++;

                            } else {
                                tempX = pathList[i - 1];
                            }

                        }


                        _transferpathItem.add(_pathItem.get(_pathItem.size() - tempSize));


                    } catch (Exception e) {


                        _transferpathItem.add(_pathItem.get(_pathItem.size() - 1));
                        break;
                    }
                }


            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.busstop);
            for (int i = 0; i < _pathItem.size(); i++) {
                XYItem xyItem = _pathItem.get(i);
                LatLng latLng = new LatLng(xyItem.y, xyItem.x);
                mPolyz.add(latLng);
                mCenterBc.include(latLng);
            }

            for (int i = 0; i < mPolyz.size() - 1; i++) {
                LatLng src = mPolyz.get(i);
                LatLng dest = mPolyz.get(i + 1);
                Polyline line = mGoogleMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude))
                        .width(7).color(Color.BLUE));
            }

            if (mode.equals("local")) {
                if (_transferpathItem.size() > 0) {
                    for (int i = 0; i < _transferpathItem.size(); i++) {

                        BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_2_map);

                        XYItem xyItem = _transferpathItem.get(i);
                        String snippet = null;
                        if (xyItem.busStopItem != null) {
                            snippet = xyItem.busStopItem.name;
                        } else {
                        }

                        LatLng latLng = new LatLng(xyItem.y, xyItem.x);
                        mCenterBc.include(latLng);
                        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(startIcon));


                        //걷는 거리 산정
                        Location tempLocationStart = new Location("start");
                        tempLocationStart.setLatitude(mStartAutoItem.latitude);
                        tempLocationStart.setLongitude(mStartAutoItem.longtude);


                        Location tempLocationEnd = new Location("end");
                        tempLocationEnd.setLatitude(mEndAutoItem.latitude);
                        tempLocationEnd.setLongitude(mEndAutoItem.longtude);


                        Location tempLocationStartStop = new Location("startStop");
                        tempLocationStartStop.setLatitude(latLng.latitude);
                        tempLocationStartStop.setLongitude(latLng.longitude);

                        double walk = tempLocationStart.distanceTo(tempLocationStartStop);
                        double walk2 = tempLocationEnd.distanceTo(tempLocationStartStop);

                        try {
                            View ballonLayout = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_ballon_layout, null);
                            TextView numTxt = (TextView) ballonLayout.findViewById(R.id.name);

                            if (walk < walk2) {
                                numTxt.setText(mTagoAryItem.tagoArrayItem.get(mTagoPosition).busName + "승차" + "[" + snippet + "]");
                            } else {
                                numTxt.setText("하차" + "[" + snippet + "]");
                            }

                            mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), ballonLayout))).snippet(snippet));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }


                }
                for (int i = 0; i < _tmpPathItem.size(); i++) {
                    BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_disabled_normal);
                    XYItem xyItem = _tmpPathItem.get(i);
                    LatLng latLng = new LatLng(xyItem.y, xyItem.x);
                    mCenterBc.include(latLng);
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(startIcon));
                }


            } else {
                if (_transferpathItem.size() > 0) {
                    for (int i = 0; i < _transferpathItem.size(); i++) {

                        BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_2_map);
                        XYItem xyItem = _transferpathItem.get(i);
                        LatLng latLng = new LatLng(xyItem.y, xyItem.x);
                        mCenterBc.include(latLng);
                        String title = "";
                        String snippet = null;
                        if (xyItem.busStopItem != null) {
                            snippet = xyItem.busStopItem.name;
                        } else {
                        }


                        if (i == 0) {
                            title = mTagoAryItem.tagoArrayItem.get(mTagoPosition).mTagoBusList.get(i).busNo + "로 승차";

                            View ballonLayout = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_ballon_layout, null);
                            TextView numTxt = (TextView) ballonLayout.findViewById(R.id.name);
                            numTxt.setText(mTagoAryItem.tagoArrayItem.get(mTagoPosition).mTagoBusList.get(i).busNo + "승차");

                            if (snippet != null) {
                                mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), ballonLayout))).snippet(snippet));
                            } else {
                                mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), ballonLayout))));
                            }


                        } else if (i == _transferpathItem.size() - 1) {
                            title = "하차";

                            View ballonLayout = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_ballon_layout, null);
                            TextView numTxt = (TextView) ballonLayout.findViewById(R.id.name);
                            numTxt.setText("하차");

                            if (snippet != null) {
                                mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), ballonLayout))).snippet(snippet));
                            } else {
                                mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), ballonLayout))));
                            }


                        } else {
                            title = mTagoAryItem.tagoArrayItem.get(mTagoPosition).mTagoBusList.get(i).busNo + "로 환승";

                            View ballonLayout = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_ballon_layout, null);
                            TextView numTxt = (TextView) ballonLayout.findViewById(R.id.name);
                            numTxt.setText(mTagoAryItem.tagoArrayItem.get(mTagoPosition).mTagoBusList.get(i).busNo + "환승");

                            mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), ballonLayout))));
                        }

                        Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(startIcon));
                    }

                }


            }


            if (mStartAutoItem != null) {
                BitmapDescriptor startIcon = BitmapDescriptorFactory.fromResource(R.drawable.start_pin);
                LatLng latLng = new LatLng(mStartAutoItem.latitude, mStartAutoItem.longtude);
                mCenterBc.include(latLng);
                Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(startIcon));
            }


            if (mEndAutoItem != null) {
                BitmapDescriptor endIcon = BitmapDescriptorFactory.fromResource(R.drawable.arrival_pin);
                LatLng latLng = new LatLng(mEndAutoItem.latitude, mEndAutoItem.longtude);
                mCenterBc.include(latLng);
                Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).icon(endIcon));

            }

            CameraUpdate cameraUpdateFactory = CameraUpdateFactory.newLatLngBounds(mCenterBc.build(), 50);
            mGoogleMap.moveCamera(cameraUpdateFactory);

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


    @Override
    public void onDetach() {
        super.onDetach();
        if (getStringData != null) {
            getStringData.cancel(true);
        }

        if (mAsynDBData != null) {
            mAsynDBData.cancel(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (android.os.Build.VERSION.SDK_INT <= 10) {
                SupportMapFragment fragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
                if (fragment != null)
                    getFragmentManager().beginTransaction().remove(fragment).commit();
            } else {
                MapFragment fragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
                if (fragment != null)
                    getActivity().getFragmentManager().beginTransaction().remove(fragment).commit();
            }
        } catch (IllegalStateException e) {
        }

    }


    class PathItem {
        ArrayList<XYItem> pathList;

        public PathItem() {
            pathList = new ArrayList<XYItem>();
        }
    }

    class XYItem {
        double x, y;
        BusStopItem busStopItem;
    }


    LocalAsync mLocalAsync;
    ArrayList<Marker> mBusStopMarker;
    ArrayList<BusStopItem> mBusStopItems;
    ArrayList<MarkerOptions> mMarkerOptions;
    ArrayList<BusStopItem> mBusStopItemResult;
    MarkerOptions mMarkerOption;

    int mLat = 0, mLong = 0;
    boolean mIsCancel = false;


    class LocalAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mGoogleMap.clear();


            mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker marker) {


                    View viewWindow = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);
                    TextView name = (TextView) viewWindow.findViewById(R.id.name);
                    TextView subName = (TextView) viewWindow.findViewById(R.id.subname);


                    if (marker.getSnippet() == null) return null;

                    String[] splitString = marker.getSnippet().split("/");

                    if (splitString.length < 2) {
                        return null;
                    }

                    name.setText(marker.getTitle());
                    subName.setText(splitString[2]);


                    return viewWindow;

                }

                @Override
                public View getInfoContents(Marker marker) {

                    return null;
                }
            });


            mGoogleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {

                    LatLng latLang = marker.getPosition();
                    if (marker.getSnippet() == null) return;

                    String[] splitStr = marker.getSnippet().split("/");
                    String name = splitStr[0];
                    String apiId = splitStr[1];
                    String arsId = splitStr[2];
                    String cityId = splitStr[3];

                    BusStopItem busStopItem = new BusStopItem();
                    busStopItem.name = name;
                    busStopItem.apiId = apiId;
                    busStopItem.arsId = arsId;
                    busStopItem.localInfoId = cityId;

                    Intent sendIntent = new Intent(getActivity(), SBDetailActivity.class);
                    sendIntent.putExtra(Constants.INTENT_SEND_TYPE, Constants.INTENT_BUSSTOPITEM);
                    sendIntent.putExtra("busInfor", busStopItem);
                    startActivity(sendIntent);

                }
            });


            if (mLocationDe != null && mGoogleMap != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocationDe.latitude, mLocationDe.longitude), 15.0f));
            }
		

            if (mBusStopMarker != null) {
                for (int i = 0; i < mBusStopMarker.size(); i++) {
                    mBusStopMarker.get(i).remove();
                }
            }

        }

        @Override
        protected String doInBackground(String... params) {

            try {
                mBusStopItems.clear();
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


                    if (isCancelled()) {
                        return null;
                    }
                    mBusStopItems.add(busStopItem);


                    LatLng locationDe = new LatLng(lat, longti); // 위치 좌표 설정
                    String snippet = busStopItem.name + "/" + busStopItem.apiId + "/" + busStopItem.arsId + "/" + cityId + "/";


                    MarkerOptions markerOptions = new MarkerOptions().position(locationDe).title(locationName).snippet(snippet).icon(BitmapDescriptorFactory.fromResource(R.drawable.stop_pin_disabled_normal));
                    mMarkerOptions.add(markerOptions);

                }

                getLoationCursor.close();
            } catch (Exception e) {

            }

            return null;
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            try {
                if (!isCancelled()) {
                    mBusStopItemResult.clear();
                    mBusStopItemResult.addAll(mBusStopItems);

                    if (mMarkerOptions != null) {
                        for (int i = 0; i < mMarkerOptions.size(); i++) {

                            if (!(mMarkerOption.getPosition().latitude == mMarkerOptions.get(i).getPosition().latitude
                                    && mMarkerOption.getPosition().longitude == mMarkerOptions.get(i).getPosition().longitude)) {

                                Marker marker = mGoogleMap.addMarker(mMarkerOptions.get(i));
                                mBusStopMarker.add(marker);
                            }


                        }
                    }

                }


            } catch (Exception e) {
            }

            if (mMarkerOptions != null) {
                mGoogleMap.addMarker(mMarkerOption);
            }

            setProgress(View.GONE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsCancel = true;
            setProgress(View.GONE);
        }


    }


}