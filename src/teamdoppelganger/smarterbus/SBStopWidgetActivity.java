package teamdoppelganger.smarterbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.smart.lib.CommonConstants;

public class SBStopWidgetActivity extends SBBaseActivity {
	
	Spinner mSpinner;
	EditText mSearchEdit;
	ListView mSearchListView;
	
	
    private SQLiteDatabase mBusDbSqlite;
    private LocalDBHelper mLocalDBHelper;
    HashMap<Integer,String> mTerminus;
    
    HashMap<Integer, String> mHashLocationEng;
    HashMap<Integer, String> mHashLocationKo;
    
    SharedPreferences mPref;
    
    
    String mCityEnName;
    SchAdapter mShAdapter;
    
    HashMap<String,Integer> mCityIdList;
	
    int mWidgetId;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sbstop_widget);
		
		mWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		setResult(RESULT_CANCELED);
		
		
		mSpinner = (Spinner)findViewById(R.id.locationSpin);
		mSearchEdit = (EditText)findViewById(R.id.searchEdit);
		mSearchListView = (ListView)findViewById(R.id.listView);
		
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
		mBusDbSqlite = SQLiteDatabase.openDatabase(Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);	
	    mLocalDBHelper = new LocalDBHelper(getApplicationContext());
		
	    mHashLocationEng = ((SBInforApplication)getApplicationContext()).mHashLocation;
        mHashLocationKo= ((SBInforApplication)getApplicationContext()).mHashKoLocation;
        mTerminus = ((SBInforApplication)getApplicationContext()).mTerminus;
        mCityIdList = new HashMap<String,Integer>();
        
		String tmpSql = String.format("SELECT *FROM %s", CommonConstants.TBL_CITY);
		Cursor cursor = mBusDbSqlite.rawQuery(tmpSql, null);
		while(cursor.moveToNext()){
			int id =cursor.getInt(cursor.getColumnIndex(CommonConstants.CITY_ID));
			String enName = cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_EN_NAME));
			mCityIdList.put(enName, id);
		}	
		cursor.close();
        
		final ArrayList<String> spinnerlist = new ArrayList<String>();
		final ArrayList<String> enList = new ArrayList<String>();
		
		Iterator iterator = mHashLocationKo.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry entry = (Entry)iterator.next();
			String value = (String) entry.getValue();
			spinnerlist.add(value);
		}
		
		
		iterator = mHashLocationEng.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry entry = (Entry)iterator.next();
			String value = (String) entry.getValue();
			enList.add(value);
		}

		mShAdapter = new SchAdapter(this, null, true);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, spinnerlist);
		mSpinner.setAdapter(adapter);		
		mSpinner.setSelection(19);
		mSearchListView.setAdapter(mShAdapter);
		
		
		mCityEnName = "SEOUL";
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				
				mCityEnName = enList.get(position);
				
				Cursor cursor = getCursorByName(mCityEnName,"");
				mShAdapter.changeCursor(cursor);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}

		});			
		
		
		
		mSearchEdit.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				// TODO Auto-generated method stub
				mShAdapter.getFilter().filter(s.toString());

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
		
		
		mSearchListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				
				FavoriteAndHistoryItem favoriteAndHistoryItem = new FavoriteAndHistoryItem();
				favoriteAndHistoryItem.type = Constants.FAVORITE_TYPE_STOP;
				
				Cursor cursor = (Cursor) mShAdapter.getItem(position);
				
				String busStopName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
				String busStopApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
				String busStopDesc = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_DESC));
				
				String busStopArsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
				String busStopLocationID= cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID));	
				String[] busStopRelatedRoute = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES)).split("/");	
									
				int id = cursor.getInt(cursor.getColumnIndex(CommonConstants._ID));
				
				double locationX = (double) cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_X));
				double locationY = (double) cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_STOP_LOCATION_Y));					
					
				ArrayList<String> busStopRelatedRouteList = new ArrayList<String>();
				for(int i = 0 ; i < busStopRelatedRoute.length; i++)
					busStopRelatedRouteList.add(busStopRelatedRoute[i]);
				
				favoriteAndHistoryItem.busStopItem.apiId = busStopApiId;
				favoriteAndHistoryItem.busStopItem.arsId = busStopArsId;
				favoriteAndHistoryItem.busStopItem.name = busStopName;
				favoriteAndHistoryItem.busStopItem.localInfoId = busStopLocationID;					
				favoriteAndHistoryItem.busStopItem._id = id;
				favoriteAndHistoryItem.busStopItem.relatedRoutes = busStopRelatedRouteList;
				
				//목표 예외
				if(busStopLocationID.equals(String.valueOf(CommonConstants.CITY_MOK_PO._cityId))){
					favoriteAndHistoryItem.busStopItem.apiId = busStopArsId;
				}
				
				Intent intent = new Intent(getApplicationContext(), SBEditWidgetActivity.class);
				intent.putExtra(Constants.INTENT_FAVORITEITEM, favoriteAndHistoryItem);
				intent.putExtra("widget_id", mWidgetId);
				startActivity(intent);
				
				Intent intent2= new Intent();
				intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
	    		setResult(RESULT_OK, intent2);
	    		
				finish();
				
			}			
		});
		
	}
	
	
	@Override
	public void onBackPressed() {
		AppWidgetHost host = new AppWidgetHost(this, 0);
		host.deleteAppWidgetId(mWidgetId);
		super.onBackPressed();
	}
	
	class SchAdapter extends CursorAdapter   {

		String _highLightStr;
		int _searchMode;

		public SchAdapter(Context context, Cursor c, boolean autoRequery) {
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
			
			if(cursor==null || cursor.getCount()==0)  return;
			
			TextView text1 = (TextView) view.findViewById(R.id.lineName);
			TextView text2 = (TextView) view.findViewById(R.id.lineDetail);
			TextView locationText = (TextView) view.findViewById(R.id.locationTxt);
		
			
			if(_searchMode== Constants.SERCH_MODE_STOP){
				String busStopName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME));
				String busStopApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
				String busStopArsId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
				String busStopLocationName= mHashLocationKo.get(Integer.parseInt(cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID))));		
							
				SpannableStringBuilder sp=null;
				if(_highLightStr!=null  && busStopName.indexOf(_highLightStr)!=-1){									
					sp = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_STOP_NAME)));
					sp.setSpan(new ForegroundColorSpan(Color.RED), busStopName.indexOf(_highLightStr),busStopName.indexOf(_highLightStr)+_highLightStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);				
				}
				
				if(sp!=null){
					text1.setText(sp);
				}else{
					text1.setText(busStopName);
				}

				text2.setText(busStopArsId);
				locationText.setText(busStopLocationName);

			}else if(_searchMode == Constants.SERCH_MODE_BUS){

				String busRouteName = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
				String busRouteApiId = cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
				String busRouteLocationName= mHashLocationKo.get(Integer.parseInt(cursor.getString(cursor.getColumnIndex(CommonConstants.CITY_ID))));
				
				int startStopId = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
				int endStopId = cursor.getInt(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
				
				
				SpannableStringBuilder sp=null;
				if(_highLightStr!=null  && busRouteName.indexOf(_highLightStr)!=-1){									
					sp = new SpannableStringBuilder(cursor.getString(cursor.getColumnIndex(CommonConstants.BUS_ROUTE_NAME)));
					sp.setSpan(new ForegroundColorSpan(Color.RED), busRouteName.indexOf(_highLightStr),busRouteName.indexOf(_highLightStr)+_highLightStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);				
				}
				
				if(sp!=null){
					text1.setText(sp);
				}else{
					text1.setText(busRouteName);
				}
				
				text2.setText(mTerminus.get(startStopId) + "->" + mTerminus.get(endStopId));
				locationText.setText(busRouteLocationName);
				
			}else if(_searchMode == Constants.SERCH_MODE_ROUTE){
				
			}
			
			return;

		}
		
		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			// TODO Auto-generated method stub
			
			if(constraint==null || constraint.toString().length()==0){
				_highLightStr=null;
			}else{
				_highLightStr = constraint.toString();
			}			
					
			return getCursorByName(mCityEnName,constraint.toString());

		}

		
		public void setAdapterMode(int mode){
			_searchMode = mode;
		}

	}
	
	
	private Cursor getCursorByName(String cityEnName,String searchStr){
		
		String qryStopInfor = String.format("SELECT *,'%s' as %s from %s_Stop where %s like '%%%s%%' limit 400", mCityIdList.get(cityEnName),CommonConstants.CITY_ID,cityEnName, CommonConstants.BUS_STOP_NAME,searchStr);
		Cursor stopCur = getBusDbSqlite().rawQuery(qryStopInfor, null);
		return stopCur;

	}

}
