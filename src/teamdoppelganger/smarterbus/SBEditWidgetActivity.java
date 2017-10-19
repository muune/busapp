package teamdoppelganger.smarterbus;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import com.smart.lib.CommonConstants;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.service.WidgetProvider2x1;
import teamdoppelganger.smarterbus.service.WidgetProvider4x2;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.widget.TicketLayout;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SBEditWidgetActivity extends SBBaseActivity implements OnClickListener {
	
	TextView mNameTxt1, mNameTxt2, mStopWidget;
	EditText mNameEditTxt1, mNameEditTxt2;
	
	Button mOkBtn, mCancelBtn;
	
	ImageView mColor1,mColor2,mColor3,mColor4,mColor5;
	CheckBox mColorD1,mColorD2,mColorD3,mColorD4,mColorD5;

	FavoriteAndHistoryItem mFavoriteAndHistoryItem;
	HashMap<Integer, String> mHashLocationEng;
	int mType;
	String mBaseName;
	
	ArrayList<String> mRouteName = new ArrayList<String>();
	int mWidgetId;
	SharedPreferences mPref;
	
	ArrayList<FavoriteAndHistoryItem> mItemArry = new ArrayList<FavoriteAndHistoryItem>();
	
	ArrayList<BusRouteItem> mBusRouteItemList = new ArrayList<BusRouteItem>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.widget_edit);
		
		initData();
		initView();
		
	}
	
	
	private void initData(){
		
		mWidgetId = getIntent().getExtras().getInt("widget_id");
		
		mFavoriteAndHistoryItem = (FavoriteAndHistoryItem) getIntent().getExtras().getSerializable(Constants.INTENT_FAVORITEITEM);
		mType = mFavoriteAndHistoryItem.type;		
		
		mHashLocationEng = ((SBInforApplication)getApplicationContext()).mHashLocation;
		
		
		if(mType != Constants.FAVORITE_TYPE_STOP)
			return;
		
		String enCityName = mHashLocationEng.get(Integer.parseInt(mFavoriteAndHistoryItem.busStopItem.localInfoId));
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    String dbName = mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
	    
		SQLiteDatabase mBusDbSqlite = SQLiteDatabase.openDatabase(
				Constants.LOCAL_PATH + dbName, null, SQLiteDatabase.OPEN_READONLY);	
	
		
		ArrayList<String> relatedRoute = mFavoriteAndHistoryItem.busStopItem.relatedRoutes;
		
		for(int i = 0; i < relatedRoute.size(); i++){
			String query = "SELECT * FROM " + enCityName + "_Route where _id=" + relatedRoute.get(i);
			
			Cursor c = mBusDbSqlite.rawQuery(query, null);
			if(c.moveToNext()){
				
				BusRouteItem item = new BusRouteItem();
				item.busRouteApiId = c.getString(c.getColumnIndex("routeId1"));
				item.busRouteApiId2 = c.getString(c.getColumnIndex("routeId2"));
				item.busRouteName = c.getString(c.getColumnIndex("routeName"));
				item.localInfoId = mFavoriteAndHistoryItem.busStopItem.localInfoId;
				item.busStopArsId = mFavoriteAndHistoryItem.busStopItem.arsId;
				
				mRouteName.add(item.busRouteName);
				
				mBusRouteItemList.add(item);
			}
			
			c.close();
		}

	}
	
	private void setList(){
		ListView list = (ListView)findViewById(android.R.id.list);
		list.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, mRouteName));
		
		mOkBtn = (Button)findViewById(R.id.ok);
		mOkBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				for(int i = 0; i < 3; i++){
					FavoriteAndHistoryItem item = new FavoriteAndHistoryItem();
					item.busStopItem = mFavoriteAndHistoryItem.busStopItem;
					item.busRouteItem = mBusRouteItemList.get(i);
					mItemArry.add(item);
				}

				
				FileOutputStream fos;
				try {
					fos = openFileOutput(String.valueOf(mWidgetId)+"_type2", Context.MODE_PRIVATE);
					ObjectOutputStream os = new ObjectOutputStream(fos);
		            os.writeObject(mItemArry);
		            os.close();
		            fos.close();

		            
		            Intent intent = new Intent();
		    		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
		    		setResult(RESULT_OK, intent);
		    		
		            WidgetProvider4x2.showResult(SBEditWidgetActivity.this, mWidgetId, 0, null);
					finish();
		            
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
	}
	
	
	private void initView(){
		
		setList();
				
		mNameTxt1 = (TextView)findViewById(R.id.name1);
		mNameTxt2 = (TextView)findViewById(R.id.name2);
		mStopWidget = (TextView)findViewById(R.id.stopWidget);
		
		
		mNameEditTxt1 = (EditText)findViewById(R.id.nameEdit1);
		mNameEditTxt2 = (EditText)findViewById(R.id.nameEdit2);
				
		mColor1 = (ImageView)findViewById(R.id.color1);
		mColor2 = (ImageView)findViewById(R.id.color2);
		mColor3 = (ImageView)findViewById(R.id.color3);
		mColor4 = (ImageView)findViewById(R.id.color4);
		mColor5 = (ImageView)findViewById(R.id.color5);
		
		mColorD1 = (CheckBox)findViewById(R.id.colorD1);
		mColorD2 = (CheckBox)findViewById(R.id.colorD2);
		mColorD3 = (CheckBox)findViewById(R.id.colorD3);
		mColorD4 = (CheckBox)findViewById(R.id.colorD4);
		mColorD5 = (CheckBox)findViewById(R.id.colorD5);

		mColor1.setOnClickListener(this);
		mColor2.setOnClickListener(this);
		mColor3.setOnClickListener(this);
		mColor4.setOnClickListener(this);
		mColor5.setOnClickListener(this);		
		
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
		display.getMetrics(outMetrics);

		float density  = getResources().getDisplayMetrics().density;
		int width = (int) (outMetrics.widthPixels/2 - getResources().getDimension(R.dimen.ticket_margin));
		int height = outMetrics.heightPixels;
		
		if(mFavoriteAndHistoryItem.type == Constants.FAVORITE_TYPE_BUS_STOP){

		}else if(mFavoriteAndHistoryItem.type == Constants.FAVORITE_TYPE_STOP){

			mNameEditTxt2.setVisibility(View.GONE);
			mNameTxt2.setVisibility(View.GONE);
			
			
			mStopWidget.setText(mFavoriteAndHistoryItem.nickName);
			mNameEditTxt1.setText(mFavoriteAndHistoryItem.nickName);
			
			mNameEditTxt1.setText(mFavoriteAndHistoryItem.busStopItem.name);
			mStopWidget.setText(mFavoriteAndHistoryItem.busStopItem.name);
			
			mNameEditTxt1.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					mStopWidget.setText(s.toString());
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					
				}
				
			});
			
		}

		
		mNameEditTxt2.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if(mNameEditTxt1.getText().length()==0){
					mStopWidget.setText(mBaseName);
				}else{
					mStopWidget.setText(s.toString());
				}
				
			}
			
		});
		
				
	}



	@Override
	public void onClick(View v) {
		
		int id = v.getId();
		
		switch(id){
		case R.id.color1:
			break;
		case R.id.color2:
			break;
		case R.id.color3:
			break;
		case R.id.color4:
			break;
		case R.id.color5:
			break;
		}
		
		
	}

}
