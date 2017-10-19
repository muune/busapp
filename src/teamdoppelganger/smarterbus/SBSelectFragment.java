package teamdoppelganger.smarterbus;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.smart.lib.CommonConstants;

import java.util.ArrayList;
import java.util.HashMap;

import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.BusRouteItem;
import teamdoppelganger.smarterbus.item.BusStopItem;
import teamdoppelganger.smarterbus.item.CommonItem;
import teamdoppelganger.smarterbus.item.DepthRouteItem;
import teamdoppelganger.smarterbus.item.DepthStopItem;
import teamdoppelganger.smarterbus.item.SearchItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;

public class SBSelectFragment extends SBBaseFragment {
	
	
	BusStopItem mBusStopItem;
	BusRouteItem mBusRouteItem;
	
	DragSortListView mListView;
	CustomListAdapter mCustomAdapter; 

		
	ArrayList<BusRouteItem> mBusRouteItemAry;
	ArrayList<BusStopItem> mBusStopItemAry;

	SearchItem mStartSearchItem, mEndSearchItem;
	HashMap<Integer,String> mTerminus;
	HashMap<Integer,String> mHashLocationKo;
	HashMap<Integer,String> mHashLocationEng;
	HashMap<Integer,String> mBusTypeHash;
	
	
	SBSelectFragmentListener mSBFragmentListener;
	
	boolean mIsStopWidgetMode = false;
	
	public interface SBSelectFragmentListener{
		public void  onChange(CommonItem item);
	}
	
	public void setSBSelectFragmentListener(SBSelectFragmentListener l){
		mSBFragmentListener = l;
	}

    public SBSelectFragment(){}

    @SuppressLint("ValidFragment")
	public SBSelectFragment(int id, SQLiteDatabase db,
			LocalDBHelper localDBHelper) {
		super(R.layout.selectfragment, db, localDBHelper);
		
		
		mBusRouteItemAry = new ArrayList<BusRouteItem>();
		mBusStopItemAry = new ArrayList<BusStopItem>();
	}

	
	@Override
	public void onLayoutFinish(View view) {	
		super.onLayoutFinish(view);		
		initView(view);
		
		((SBInforApplication)getActivity().getApplicationContext()).setTerminus(getBusDbSqlite());
        ((SBInforApplication)getActivity().getApplicationContext()).setCityInfor(getBusDbSqlite());	
        ((SBInforApplication)getActivity().getApplicationContext()).setCityKoInfor(getBusDbSqlite());
        ((SBInforApplication)getActivity().getApplicationContext()).setBusType(getBusDbSqlite());
		
		
		
		
		mTerminus = ((SBInforApplication)getActivity().getApplicationContext()).mTerminus;
		mHashLocationKo = ((SBInforApplication)getActivity().getApplicationContext()).mHashKoLocation;
		mHashLocationEng = ((SBInforApplication)getActivity().getApplicationContext()).mHashLocation;
		mBusTypeHash = ((SBInforApplication)getActivity().getApplicationContext()).mBusTypeHash;
		
		
		
		if(mBusStopItem!=null){
			
			DepthRouteItem  depthRouteItem = getRelateRoutes(mBusStopItem, getBusDbSqlite());
			mBusRouteItemAry = depthRouteItem.busRouteItem;
			
		}else if(mBusRouteItem!=null){
			
			DepthStopItem  depthStopItem = getRelateStops(mBusRouteItem, getBusDbSqlite());
			mBusStopItemAry = depthStopItem.busStopItem;
			
		}
		
		
		mCustomAdapter.notifyDataSetChanged();
		
		
	}
	
	
	
	public void initView(View v){
		mListView = (DragSortListView)v.findViewById(R.id.listView);		
		mCustomAdapter  = new CustomListAdapter();
		mListView.setAdapter(mCustomAdapter);
		
		
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {

				if(mIsStopWidgetMode){
					
				}else{
					
					if(mBusStopItem!=null){							
						if(mSBFragmentListener!=null){
							mSBFragmentListener.onChange(mBusRouteItemAry.get(position));
						}						
					}else if(mBusRouteItem!=null){
						if(mSBFragmentListener!=null){
							mSBFragmentListener.onChange(mBusStopItemAry.get(position));
						}
					}
					
				}
			}
		});
		
		mListView.setDropListener(new DropListener() {			
			@Override
			public void drop(int from, int to) {
				
				if(from!=to){
					BusRouteItem item = mBusRouteItemAry.get(from);
					mBusRouteItemAry.remove(from);
					mBusRouteItemAry.add(to,item);
					
					mCustomAdapter.notifyDataSetChanged();
				}
			}
		});
		
		
	
		if(!mIsStopWidgetMode){
			
			mListView.setDragEnabled(false);

		}else{

			mListView.setDragEnabled(true);

		}
		
	
	}
	
	
	
	public void setBusStopItem(BusStopItem stopItem){
		mBusStopItem = stopItem;
		
		
		
		
	}
	
	
	public void setBusRouteItem(BusRouteItem routeItem){
		mBusRouteItem = routeItem;
		
		
	}
	
	
	public void setStopWidgetMode(boolean isStopWidget){
		mIsStopWidgetMode = isStopWidget;
	}
	
	
	class CustomListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			
			if(mBusStopItem!=null){
				return mBusRouteItemAry.size();
			}else{
				return mBusStopItemAry.size();
			}			
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
			View view = convertView;
			ViewHolder viewHolder = null;

			if (view == null) {
				viewHolder = new ViewHolder();
				view = getActivity().getLayoutInflater().inflate(R.layout.selectfrag_row,parent, false);
				
				viewHolder.title = (TextView) view.findViewById(R.id.lineName);
				viewHolder.sub = (TextView) view.findViewById(R.id.lineDetail);	
				viewHolder.location = (TextView) view.findViewById(R.id.locationTxt);
				viewHolder.check = (CheckBox)view.findViewById(R.id.check);
				viewHolder.dragHandler = (ImageView)view.findViewById(R.id.drag_handle);
				viewHolder.textLayout = (LinearLayout)view.findViewById(R.id.textLayout);
				
				

				view.setTag(viewHolder);
			} else {

				viewHolder = (ViewHolder) view.getTag();
			}
			
			if(mIsStopWidgetMode){
				viewHolder.location.setVisibility(View.GONE);
				viewHolder.check.setVisibility(View.VISIBLE);				
				viewHolder.dragHandler.setVisibility(View.VISIBLE);
			}else{
				viewHolder.location.setVisibility(View.VISIBLE);
				viewHolder.check.setVisibility(View.GONE);				
				viewHolder.dragHandler.setVisibility(View.GONE);
			}
			
			
			if(mBusStopItem!=null){
				
				BusRouteItem routeItem = mBusRouteItemAry.get(position);
				viewHolder.title.setText(routeItem.busRouteName);
				
				if(routeItem.isChecked){
					
					viewHolder.check.setChecked(true);
				}else{
					viewHolder.check.setChecked(false);
				}
				
				viewHolder.check.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						
						boolean isChecked = ((CheckBox)v).isChecked();
						
						mBusRouteItemAry.get(position).isChecked = isChecked;
						
					}
				});
				
				viewHolder.sub.setText(mTerminus.get(Integer.parseInt(routeItem.startStop)) + "->" + mTerminus.get(Integer.parseInt(routeItem.endStop)));
				viewHolder.location.setText(mHashLocationKo.get(Integer.parseInt(routeItem.localInfoId)));

			}else if(mBusRouteItem!=null){
				
				BusStopItem stopItem = mBusStopItemAry.get(position);
				viewHolder.title.setText(stopItem.name);
				
				viewHolder.sub.setText(stopItem.arsId);
				viewHolder.location.setText(mHashLocationKo.get(Integer.parseInt(stopItem.localInfoId)));
				
			}

			return view;
		}
		
		class ViewHolder {
			TextView title;
			TextView sub;
			TextView location;	
			CheckBox check;
			LinearLayout textLayout;
			ImageView dragHandler;
		}

		
	}
	
	
	private DepthStopItem getRelateStops(BusRouteItem routeItem,
			SQLiteDatabase db) {
		ArrayList<BusStopItem> localBusStopItem = new ArrayList<BusStopItem>();

		String localName = mHashLocationEng.get(Integer
				.parseInt(routeItem.localInfoId));
		String localId = routeItem.localInfoId;

		String getRelateRoutesQry = "";
		
		getRelateRoutesQry = String.format("Select * From %s where %s='%s' ", localName + "_route",
				CommonConstants._ID, routeItem._id);

		Cursor relateCursor = db.rawQuery(getRelateRoutesQry, null);
		String relateRoute = "";


		if (relateCursor.moveToFirst()) {
			relateRoute = relateCursor.getString(relateCursor
					.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));			
		}
		relateCursor.close();
		String[] relateSplit = relateRoute.split("/");

		int tmpBusType = -1;
		int tempCount = 0;

		for (int i = 0; i < relateSplit.length; i++) {

			String stopId = relateSplit[i];

			String routInforQry = String.format(
					"Select * From %s where %s=%s ", localName
							+ "_stop", CommonConstants._ID, stopId,
					CommonConstants.BUS_ROUTE_BUS_TYPE);


			Cursor routeCursor = db.rawQuery(routInforQry, null);

			if (routeCursor.moveToNext()) {

				String busStopName = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_STOP_NAME));
				String busStopArsId = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_STOP_ARS_ID));
				String busStopApiId = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
				String busStopDesc = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_STOP_DESC));
				String busRelateRoute = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES));
				String _id = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants._ID));
				
				
				BusStopItem busStopItem = new BusStopItem();
				busStopItem.name = busStopName;
				busStopItem.arsId = busStopArsId;
				busStopItem.apiId = busStopApiId;				
				busStopItem.localInfoId = _id;
				
				localBusStopItem.add(busStopItem);
							
				
			}

			routeCursor.close();

		}

		DepthStopItem depthRouteItem = new DepthStopItem();
		depthRouteItem.busStopItem.addAll(localBusStopItem);
		return depthRouteItem;

	}
	
	private DepthRouteItem getRelateRoutes(BusStopItem stopItem,
			SQLiteDatabase db) {
		ArrayList<BusRouteItem> localBusRouteItem = new ArrayList<BusRouteItem>();

		String localName = mHashLocationEng.get(Integer
				.parseInt(stopItem.localInfoId));
		String localId = stopItem.localInfoId;

		String getRelateRoutesQry = "";
		
		getRelateRoutesQry = String.format("Select * From %s where %s='%s' ", localName + "_stop",
				CommonConstants._ID, stopItem._id);


		Cursor relateCursor = db.rawQuery(getRelateRoutesQry, null);
		String relateRoute = "";


		if (relateCursor.moveToFirst()) {
			relateRoute = relateCursor.getString(relateCursor
					.getColumnIndex(CommonConstants.BUS_STOP_RELATED_ROUTES));
			
			stopItem.apiId = relateCursor.getString(relateCursor
					.getColumnIndex(CommonConstants.BUS_STOP_API_ID));
			stopItem.tempId2 = relateCursor.getString(relateCursor
					.getColumnIndex(CommonConstants.BUS_STOP_DESC));
		}

		relateCursor.close();
		String[] relateSplit = relateRoute.split("/");

		int tmpBusType = -1;
		int tempCount = 0;

		for (int i = 0; i < relateSplit.length; i++) {

			String routeId = relateSplit[i];

			String routInforQry = String.format(
					"Select * From %s where %s=%s order by %s desc, %s asc", localName
							+ "_route", CommonConstants._ID, routeId,
					CommonConstants.BUS_ROUTE_BUS_TYPE, CommonConstants.BUS_ROUTE_NAME);
			
			Cursor routeCursor = db.rawQuery(routInforQry, null);

			if (routeCursor.moveToNext()) {

				String busRouteName = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_ROUTE_NAME));
				String busRouteApi1 = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_ROUTE_ID1));
				String busRouteApi2 = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_ROUTE_ID2));
				String busRouteStartStop = routeCursor
						.getString(routeCursor
								.getColumnIndex(CommonConstants.BUS_ROUTE_START_STOP_ID));
				String busRouteEndStop = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_ROUTE_END_STOP_ID));
				String busRouteSub = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_ROUTE_SUB_NAME));
				String busType = routeCursor.getString(routeCursor
						.getColumnIndex(CommonConstants.BUS_ROUTE_BUS_TYPE));
				String busRouteRelateStop = routeCursor
						.getString(routeCursor
								.getColumnIndex(CommonConstants.BUS_ROUTE_RELATED_STOPS));
				int _id = routeCursor.getInt(routeCursor
						.getColumnIndex(CommonConstants._ID));

				BusRouteItem busRouteItem = new BusRouteItem();
				busRouteItem.busRouteName = busRouteName;
				busRouteItem.busRouteApiId = busRouteApi1;
				busRouteItem.busRouteApiId2 = busRouteApi2;
				busRouteItem.busRouteSubName = busRouteSub;
				busRouteItem.startStop = busRouteStartStop;
				busRouteItem.endStop = busRouteEndStop;
				busRouteItem.localInfoId = stopItem.localInfoId;
				busRouteItem.tmpId = stopItem.tempId2;
				
				
				busRouteItem.busType = Integer.parseInt(busType);
				busRouteItem.localInfoId = localId;
				

				busRouteItem.busStopApiId = stopItem.apiId;
				busRouteItem.busStopArsId = stopItem.arsId;
				busRouteItem.busStopName = stopItem.name;
				busRouteItem._stopId = stopItem._id;
				busRouteItem.relateStop = busRouteRelateStop;
				busRouteItem._id = _id;
				

				if (busRouteItem.busRouteApiId2 == null) {
					busRouteItem.busRouteApiId2 = "";
				}

				if (busRouteItem.busRouteSubName == null) {
					busRouteItem.busRouteSubName = "";
				}

				busRouteItem.index = tempCount;
				localBusRouteItem.add(busRouteItem);
				tempCount++;

			}

			routeCursor.close();

		}

		DepthRouteItem depthRouteItem = new DepthRouteItem();
		depthRouteItem.busRouteItem.addAll(localBusRouteItem);
		return depthRouteItem;

	}
	
	public String getBusName(SQLiteDatabase db, int id) {

		String sql = String.format("SELECT *FROM %s where %s=%s",
				CommonConstants.TBL_BUS_TYPE, CommonConstants._ID, id);
		String busName = "";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToNext()) {
			busName = cursor.getString(cursor
					.getColumnIndex(CommonConstants.BUS_TYPE_BUS_TYPE));
		}

		cursor.close();

		return busName;

	}
	
	public void setChecked(boolean isChecked){

		if(mBusRouteItemAry!=null){
			
			for(int i=0;i<mBusRouteItemAry.size();i++){
				mBusRouteItemAry.get(i).isChecked = isChecked;				
			}
			
			if(mCustomAdapter!=null){
				mCustomAdapter.notifyDataSetChanged();
			}
			
		}

	}
	
	
	public ArrayList<BusRouteItem> getSBSelctBusRouteList(){
		return mBusRouteItemAry;
	}
	
}
