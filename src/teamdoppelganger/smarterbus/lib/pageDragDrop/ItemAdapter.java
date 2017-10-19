package teamdoppelganger.smarterbus.lib.pageDragDrop;

import java.util.HashMap;
import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.lib.pageDragDrop.SpanVariableGridView.LayoutParams;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class ItemAdapter extends ArrayAdapter<FavoriteAndHistoryItem> implements SpanVariableGridView.CalculateChildrenPosition {
	
	boolean mEditMode = false;
	

	private final class ItemViewHolder {

		/*public TextView itemTitle;
		public TextView itemDescription;
		public ImageView itemIcon;*/
		
		TextView mName1Txt, mSubNAme1Txt;
		TextView mArrive1Txt, mArrive2Txt;
		TextView mDetailTxt;
		ImageView mTicketColorView;
		
		ImageView mBtnImg;
		Button mRefreshBtn, mDelBtn;
		ProgressBar mProgress;

	}

	private Context mContext;
	private LayoutInflater mLayoutInflater = null;
	
	private int mWidth, mHeight;
	private int mWidgetMode;
	
	public ItemAdapterListener mItemAdapterListener;
	
	public boolean isDeleteLocation = false;
	HashMap<Integer, String> mBusTypeHash;
	
	public interface ItemAdapterListener{
		public void onRefresh(int position);
		public void onDelete(int position);
	}
	
	public void setItemAdapterListener(ItemAdapterListener l){
		mItemAdapterListener = l;
	}

	private View.OnClickListener onRemoveItemListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			Integer position = (Integer) view.getTag();
			removeItem(getItem(position));

		}
	};

	public void insertItem(FavoriteAndHistoryItem item, int where) {

		if (where < 0 || where > (getCount() - 1)) {

			return;
		}

		insert(item, where);
	}

	public boolean removeItem(FavoriteAndHistoryItem item) {

		remove(item);

		return true;
	}

	public ItemAdapter(Context context, List<FavoriteAndHistoryItem> plugins, int width, int height, int widgetMode, HashMap<Integer, String> busTypeHash) {

		super(context, R.layout.ticket, plugins);

		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		
		
		mWidth = width/2;
		mHeight = height;
		mWidgetMode = widgetMode;
		mBusTypeHash = busTypeHash;
		
	}
	
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ItemViewHolder itemViewHolder;

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.ticket, parent, false);
			
			/*convertView.getLayoutParams().width = 100; //mWidth/2;
			convertView.getLayoutParams().height= 200; //(int) mContext.getResources().getDimension(R.dimen.ticket_height);
*/
			
			convertView.getLayoutParams().width = mWidth; //mWidth/2;
			convertView.getLayoutParams().height= (int) mContext.getResources().getDimension(R.dimen.ticket_height);
			
			convertView.requestLayout();
			
			itemViewHolder = new ItemViewHolder();
			/*itemViewHolder.itemTitle = (TextView) convertView.findViewById(R.id.textViewTitle);
			itemViewHolder.itemDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
			itemViewHolder.itemIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);*/
			
			
			itemViewHolder.mTicketColorView  = (ImageView)convertView.findViewById(R.id.backColor);
			itemViewHolder.mName1Txt = (TextView)convertView.findViewById(R.id.name1);
			itemViewHolder.mSubNAme1Txt = (TextView)convertView.findViewById(R.id.subname1);
			itemViewHolder.mArrive1Txt = (TextView)convertView.findViewById(R.id.arrive_1);
			itemViewHolder.mArrive2Txt = (TextView)convertView.findViewById(R.id.arrive_2);
			itemViewHolder.mDetailTxt = (TextView)convertView.findViewById(R.id.detail);
			itemViewHolder.mRefreshBtn = (Button)convertView.findViewById(R.id.refreshBtn);
			itemViewHolder.mDelBtn = (Button)convertView.findViewById(R.id.delBtn);
			itemViewHolder.mBtnImg = (ImageView)convertView.findViewById(R.id.btn_image);
			itemViewHolder.mProgress = (ProgressBar)convertView.findViewById(R.id.itemProgress);
			
			convertView.setTag(itemViewHolder);

		} else {

			itemViewHolder = (ItemViewHolder) convertView.getTag();
		}

		final FavoriteAndHistoryItem item = getItem(position);

		SpanVariableGridView.LayoutParams lp = new LayoutParams(convertView.getLayoutParams());
		//lp.span = item.getSpans();		
		convertView.setLayoutParams(lp);
		itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#000000"));
		
		
		if(item.type == Constants.FAVORITE_TYPE_BUS){
			
			itemViewHolder.mName1Txt.setText(item.busRouteItem.busRouteName);
			itemViewHolder.mDetailTxt.setText(item.nickName);
			
			itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
			itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
			itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
			itemViewHolder.mRefreshBtn.setVisibility(View.GONE);

			
			itemViewHolder.mBtnImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_bus));
			
			
			try{
				String color = mBusTypeHash.get(item.busRouteItem.busType);
                itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#" + color));
			}catch(Exception e){
				itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#00000"));
			};
			
			
		}else if(item.type == Constants.FAVORITE_TYPE_STOP){
			
			itemViewHolder.mName1Txt.setText(item.nickName);
			itemViewHolder.mDetailTxt.setText(item.nickName2);
			
            itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
			itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
			itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
			itemViewHolder.mRefreshBtn.setVisibility(View.GONE);
			
			itemViewHolder.mBtnImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_stop));
			
		}else if(item.type == Constants.FAVORITE_TYPE_BUS_STOP){

            itemViewHolder.mName1Txt.setText(item.busRouteItem.busRouteName);
			itemViewHolder.mDetailTxt.setText(item.nickName2);

			itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
			itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
			itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
			itemViewHolder.mRefreshBtn.setVisibility(View.VISIBLE);
			
			try{
				String color = mBusTypeHash.get(item.busRouteItem.busType);
                itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#" + color));
			}catch(Exception e){
				itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#000000"));
			};
			
			
			itemViewHolder.mBtnImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_busstop));
			
			if(mWidgetMode==Constants.WIDGET_MODE_NOTHING){
				itemViewHolder.mRefreshBtn.setOnClickListener(new OnClickListener() {				
					@Override
					public void onClick(View v) {
				
						itemViewHolder.mProgress.setVisibility(View.VISIBLE);
						if(mItemAdapterListener!=null){
							mItemAdapterListener.onRefresh(position);
						}
					}
				});
				
			}
			
		}		
		
		
		//에디트 모드에 따라 del버튼을 보여주고 안보여주고를 처리함
		if(mEditMode){			
			itemViewHolder.mDelBtn.setVisibility(View.VISIBLE);
			itemViewHolder.mRefreshBtn.setVisibility(View.GONE);
			
			
			itemViewHolder.mDelBtn.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {

					
					switch(event.getAction()){
					case MotionEvent.ACTION_DOWN:
						isDeleteLocation = true;
						
						break;
					case MotionEvent.ACTION_UP:
						
						break;
					case MotionEvent.ACTION_CANCEL:

						isDeleteLocation = false;
						break;

                    }
					
					
					return false;
					
					
				}
			});
			
			
			itemViewHolder.mDelBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {					
					
					
					
					if(mItemAdapterListener!=null){
						mItemAdapterListener.onDelete(position);
					}
					
					isDeleteLocation = false;
					
				}
			});
			
			
		}else{
			itemViewHolder.mDelBtn.setVisibility(View.GONE);
			
			if(item.type == Constants.FAVORITE_TYPE_BUS_STOP){
				itemViewHolder.mRefreshBtn.setVisibility(View.VISIBLE);
			}
		}
		
		
		
		//도착 정보 표시 부분 
		if(item.busRouteItem.arriveInfo.size()>0){
			
			if(item.busRouteItem.arriveInfo.size() >0){
				if(item.type == Constants.FAVORITE_TYPE_BUS_STOP){
					itemViewHolder.mArrive1Txt.setVisibility(View.VISIBLE);
				}
				
				
				ArriveItem arriveItem = item.busRouteItem.arriveInfo.get(0);
				String resultStr ="";
				
				if(arriveItem.remainMin!=-1){
					
					if(arriveItem.remainMin!=0){
						resultStr = arriveItem.remainMin+"분";
					}
					
				}
				
				if(arriveItem.remainSecond!=-1){
					resultStr = resultStr + arriveItem.remainSecond+"초";
				}
				
				
				if(arriveItem.remainStop!=-1){
					if(resultStr.equals("")){
						resultStr = arriveItem.remainStop +"정류장 전";
					}else{
						resultStr = resultStr +"(" + arriveItem.remainStop  +"정류장 전)";
					}
				}
				
				if(arriveItem.state == Constants.STATE_PREPARE){
					try {
						if(arriveItem.remainSecond == -9999){
							resultStr = arriveItem.remainMin+"시 "+ ((arriveItem.remainStop==0)? arriveItem.remainStop+"0" : arriveItem.remainStop)+"분 출발";
						}else{
							resultStr = mContext.getString(R.string.state_prepare);
						}
					}catch(Exception e){
						resultStr = mContext.getString(R.string.state_prepare);
					};
				}else if(arriveItem.state == Constants.STATE_END){
					resultStr = mContext.getString(R.string.state_end);
				}else if(arriveItem.state == Constants.STATE_PREPARE_NOT){
					resultStr = mContext.getString(R.string.state_prepare_not);
				}
				
				itemViewHolder.mArrive1Txt.setText(resultStr);					
				itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
			}
			
			
			if(item.busRouteItem.arriveInfo.size() >1){
				
				itemViewHolder.mArrive1Txt.setVisibility(View.VISIBLE);
				itemViewHolder.mArrive2Txt.setVisibility(View.VISIBLE);
				
				ArriveItem arriveItem = item.busRouteItem.arriveInfo.get(1);
				String resultStr ="";
				
				if(arriveItem.remainMin!=-1){
					
					if(arriveItem.remainMin!=0){
						resultStr = arriveItem.remainMin+"분";
					}
					
				}
				
				if(arriveItem.remainSecond!=-1){
					resultStr = resultStr + arriveItem.remainSecond+"초";
				}
				
				
				if(arriveItem.remainStop!=-1){
					if(resultStr.equals("")){
						resultStr = arriveItem.remainStop +"정류장 전";
					}else{
						resultStr = resultStr +"(" + arriveItem.remainStop  +"정류장 전)";
					}
				}
				
				if(arriveItem.state == Constants.STATE_PREPARE){
					try {
						if(arriveItem.remainSecond == -9999){
							resultStr = arriveItem.remainMin+"시 "+ ((arriveItem.remainStop==0)? arriveItem.remainStop+"0" : arriveItem.remainStop)+"분 출발";
						}else{
							resultStr = mContext.getString(R.string.state_prepare);
						}
					}catch(Exception e){
						resultStr = mContext.getString(R.string.state_prepare);
					};
				}else if(arriveItem.state == Constants.STATE_END){
					resultStr = mContext.getString(R.string.state_end);
				}else if(arriveItem.state == Constants.STATE_PREPARE_NOT){
					resultStr = mContext.getString(R.string.state_prepare_not);
				}
									
				itemViewHolder.mArrive2Txt.setText(resultStr);				
			}else{
				itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
			}
		}else{
			itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
			itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
		}
		
		if(item.busRouteItem.arriveInfo.size()>0){
			itemViewHolder.mDetailTxt.setVisibility(View.GONE);//.setText(item.nickName);
			itemViewHolder.mSubNAme1Txt.setVisibility(View.VISIBLE);
			itemViewHolder.mSubNAme1Txt.setText(item.nickName);
			
		}else{
			itemViewHolder.mDetailTxt.setVisibility(View.VISIBLE);//.setText(item.nickName);
			itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
			
			if(item.type == Constants.FAVORITE_TYPE_STOP){
				itemViewHolder.mDetailTxt.setText(item.nickName2);
			}else{
				itemViewHolder.mDetailTxt.setText(item.nickName);
			}
			
		}
		
		
		if(mEditMode){
			itemViewHolder.mArrive1Txt.setTextColor(mContext.getResources().getColor(R.color.arrive_nomal));
			itemViewHolder.mArrive2Txt.setTextColor(mContext.getResources().getColor(R.color.arrive_nomal));
		}else{
			itemViewHolder.mArrive1Txt.setTextColor(mContext.getResources().getColor(R.color.arrive_color));
			itemViewHolder.mArrive2Txt.setTextColor(mContext.getResources().getColor(R.color.arrive_color));
		}

        String packName = mContext.getPackageName(); // 패키지명
		if(item.color!=null){			
			String resName = String.format("tag_%s",item.color);
			int resID = mContext.getResources().getIdentifier(resName, "drawable", packName);
			
			itemViewHolder.mTicketColorView.setImageResource(resID);			
		}else{
			
			String resName = String.format("tag_1_1");
			int resID = mContext.getResources().getIdentifier(resName, "drawable", packName);
			itemViewHolder.mTicketColorView.setImageResource(resID);
		}



		return convertView;
	}

	@Override
	public void onCalculatePosition(View view, int position, int row, int column) {

	}
	
	public void setEditMode(boolean isEdit){
		mEditMode = isEdit;
	}
}
