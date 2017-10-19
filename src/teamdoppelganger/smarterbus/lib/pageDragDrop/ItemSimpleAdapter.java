package teamdoppelganger.smarterbus.lib.pageDragDrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.ArriveItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.lib.pageDragDrop.DragDropGrid.DragAndDropElement;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemSimpleAdapter implements DragDropGridAdapter {

    boolean mEditMode = false;

    int mWindowWidth;
    int mWidgetMode;
    int mItemWidth, mItemHeight;
    Context mContext;

    List<FavoriteAndHistoryItem> mItems;
    HashMap<Integer, String> mBusTypeHash;

    public boolean isDeleteLocation = false;

    public ItemAdapterListener mItemAdapterListener;

    ArrayList<DragView> mDragViewList;

    SharedPreferences mPref;


    public interface ItemAdapterListener {
        public void onRefresh(int position);

        public void onDelete(int position);
    }

    public void setItemAdapterListener(ItemAdapterListener l) {
        mItemAdapterListener = l;
    }


    public ItemSimpleAdapter(Context context, int windowWidth, int itemWidth,
                             int itemHeight, List<FavoriteAndHistoryItem> plugins,
                             int widgetMode, HashMap<Integer, String> busTypeHash) {

        mDragViewList = new ArrayList<DragView>();

        mWindowWidth = windowWidth;
        mItemWidth = itemWidth;
        mItemHeight = itemHeight;
        mContext = context;


        mItems = plugins;
        mBusTypeHash = busTypeHash;
        mWidgetMode = widgetMode;
        mPref = mPref = PreferenceManager
                .getDefaultSharedPreferences(context);

    }


    public void setITem(List<FavoriteAndHistoryItem> plugins) {
        mItems = plugins;
    }


    @Override
    public int itemCount() {
        return mItems.size();
    }

    @Override
    public View getView(final int position) {

        DragView r = new DragView(mContext, mItemWidth, mItems.get(position));

        r.setIndex(position);
        r.setLayoutParams(new ViewGroup.LayoutParams(getChildViewWidth(),
                mItemHeight));


        return r;

    }

    @Override
    public int rowCount() {

        if (mItems.size() == 1) {
            return 1;
        } else if (mItems.size() == 0) {
            return 0;
        } else {
            if (mItems.size() % 2 == 1) {
                return mItems.size() / 2 + 1;
            } else {
                return mItems.size() / 2;
            }
        }

    }

    @Override
    public int columnCount() {
        return 2;
    }

    @Override
    public int getChildViewWidth() {
        return mWindowWidth / columnCount();
    }

    @Override
    public int getChildViewHeight() {
        return mItemHeight + 30;
    }

    @Override
    public int getViewHeight() {

        if (rowCount() <= 2) {
            return getChildViewHeight();
        } else {
            return getChildViewHeight() * rowCount();
        }
    }

    @Override
    public int getHeaderViewHeight() {
        return 0;
    }

    @Override
    public int getFooterViewHeight() {
        return 0;
    }

    @Override
    public View getHeader() {
        return null;
    }

    @Override
    public View getFooter() {
        return null;
    }


    public void notifyDataSetChanged() {
    }

    public void setEditMode(boolean isEdit) {
        mEditMode = isEdit;
    }


    private final class ItemViewHolder {

        TextView mName1Txt, mSubNAme1Txt;
        TextView mArrive1Txt, mArrive2Txt;
        TextView mDetailTxt;
        ImageView mTicketColorView;

        ImageView mBtnImg;
        Button mRefreshBtn, mDelBtn;
        ProgressBar mProgress;

    }

    public class DragView extends RelativeLayout implements DragAndDropElement {

        private int mIndex;
        private View mView;
        private FavoriteAndHistoryItem _item;

        boolean _isEditMode = false;
        int mMode;

        LayoutInflater _layoutInflater;
        RelativeLayout.LayoutParams _params;

        public DragView(Context context, int width, FavoriteAndHistoryItem item) {
            super(context);

            _layoutInflater = LayoutInflater.from(context);
            mView = _layoutInflater.inflate(R.layout.ticket, null, false);

            int mode = getPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE);

            if (mode == Constants.SETTING_FAOVIRET_TICKET_1) {

                mView = _layoutInflater.inflate(R.layout.ticket, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_1;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_2) {
                mView = _layoutInflater.inflate(R.layout.ticket2, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_2;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_3) {
                mView = _layoutInflater.inflate(R.layout.ticket3, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_3;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_4) {
                mView = _layoutInflater.inflate(R.layout.ticket4, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_4;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_5) {
                mView = _layoutInflater.inflate(R.layout.ticket5, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_5;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_6) {
                mView = _layoutInflater.inflate(R.layout.ticket6, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_6;

            }


            _item = item;
            _params = new RelativeLayout.LayoutParams(
                    width / 2, (int) getResources().getDimension(R.dimen.ticket_height));
            _params.addRule(RelativeLayout.CENTER_IN_PARENT);

            setView(item);
            this.addView(mView, _params);
        }

        public void setEditMode(boolean editMode) {
            _isEditMode = editMode;
            setView(_item);
        }

        public void setViewChange() {

            this.removeAllViews();

            int mode = getPrefTypeMode(Constants.SETTING_FAOVIRET_TICKET_TYPE);

            if (mode == Constants.SETTING_FAOVIRET_TICKET_1) {

                mView = _layoutInflater.inflate(R.layout.ticket, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_1;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_2) {
                mView = _layoutInflater.inflate(R.layout.ticket2, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_2;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_3) {
                mView = _layoutInflater.inflate(R.layout.ticket3, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_3;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_4) {
                mView = _layoutInflater.inflate(R.layout.ticket4, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_4;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_5) {
                mView = _layoutInflater.inflate(R.layout.ticket5, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_5;

            } else if (mode == Constants.SETTING_FAOVIRET_TICKET_6) {
                mView = _layoutInflater.inflate(R.layout.ticket6, null, false);
                mMode = Constants.SETTING_FAOVIRET_TICKET_6;

            }

            setView(_item);
            this.addView(mView, _params);

        }

        public View getView() {
            return mView;
        }

        public FavoriteAndHistoryItem getItem() {
            return _item;
        }

        public void setItem(FavoriteAndHistoryItem item) {
            _item = item;
        }

        @Override
        public void onDragStartPreceding(View v) {
            if (v == this) {
                if (android.os.Build.VERSION.SDK_INT >= 11)
                    v.setAlpha(0.8f);
            }
        }

        @Override
        public void onDragEnded(View v) {
            if (v == this) {
                v.setAlpha(1);
            }
        }

        @Override
        public int getIndex() {
            return mIndex;
        }

        public void setView(FavoriteAndHistoryItem item) {

            final ItemViewHolder itemViewHolder = new ItemViewHolder();

            itemViewHolder.mTicketColorView = (ImageView) mView
                    .findViewById(R.id.backColor);
            itemViewHolder.mName1Txt = (TextView) mView
                    .findViewById(R.id.name1);


            itemViewHolder.mTicketColorView = (ImageView) mView
                    .findViewById(R.id.backColor);
            itemViewHolder.mName1Txt = (TextView) mView
                    .findViewById(R.id.name1);
            itemViewHolder.mSubNAme1Txt = (TextView) mView
                    .findViewById(R.id.subname1);
            itemViewHolder.mArrive1Txt = (TextView) mView
                    .findViewById(R.id.arrive_1);
            itemViewHolder.mArrive2Txt = (TextView) mView
                    .findViewById(R.id.arrive_2);
            itemViewHolder.mDetailTxt = (TextView) mView
                    .findViewById(R.id.detail);
            itemViewHolder.mRefreshBtn = (Button) mView
                    .findViewById(R.id.refreshBtn);
            itemViewHolder.mDelBtn = (Button) mView
                    .findViewById(R.id.delBtn);
            itemViewHolder.mBtnImg = (ImageView) mView
                    .findViewById(R.id.btn_image);
            itemViewHolder.mProgress = (ProgressBar) mView
                    .findViewById(R.id.itemProgress);


            itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#000000"));

            if (item.type == Constants.FAVORITE_TYPE_BUS) {

                itemViewHolder.mName1Txt.setText(item.busRouteItem.busRouteName);
                itemViewHolder.mDetailTxt.setText(item.nickName);

                itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
                itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
                itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
                itemViewHolder.mRefreshBtn.setVisibility(View.GONE);


                itemViewHolder.mBtnImg.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_bus));

                try {
                    String color = mBusTypeHash.get(item.busRouteItem.busType);
                    itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#"
                            + color));
                } catch (Exception e) {
                    itemViewHolder.mName1Txt.setTextColor(Color
                            .parseColor("#00000"));
                }
                ;

            } else if (item.type == Constants.FAVORITE_TYPE_STOP) {

                itemViewHolder.mName1Txt.setText(item.nickName);
                itemViewHolder.mDetailTxt.setText(item.nickName2);

                itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
                itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
                itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
                itemViewHolder.mRefreshBtn.setVisibility(View.GONE);

                itemViewHolder.mBtnImg.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_stop));

            } else if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {
                itemViewHolder.mName1Txt.setText(item.busRouteItem.busRouteName);
                itemViewHolder.mDetailTxt.setText(item.nickName2);

                itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
                itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
                itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);
                itemViewHolder.mRefreshBtn.setVisibility(View.VISIBLE);

                try {
                    String color = mBusTypeHash.get(item.busRouteItem.busType);

                    itemViewHolder.mName1Txt.setTextColor(Color.parseColor("#"
                            + color));
                } catch (Exception e) {
                    itemViewHolder.mName1Txt.setTextColor(Color
                            .parseColor("#000000"));
                }
                ;

                itemViewHolder.mBtnImg.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.ic_busstop));

                if (mWidgetMode == Constants.WIDGET_MODE_NOTHING) {
                    itemViewHolder.mRefreshBtn
                            .setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    itemViewHolder.mProgress.setVisibility(View.VISIBLE);
                                    if (mItemAdapterListener != null) {
                                        mItemAdapterListener.onRefresh(mIndex);
                                    }
                                }
                            });

                }

            }

            // 에디트 모드에 따라 del버튼을 보여주고 안보여주고를 처리함
            if (_isEditMode) {
                itemViewHolder.mDelBtn.setVisibility(View.VISIBLE);
                itemViewHolder.mRefreshBtn.setVisibility(View.GONE);

                itemViewHolder.mDelBtn.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction()) {
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

                        if (mItemAdapterListener != null) {
                            mItemAdapterListener.onDelete(mIndex);
                        }

                        isDeleteLocation = false;

                    }
                });

            } else {
                itemViewHolder.mDelBtn.setVisibility(View.GONE);

                if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {
                    itemViewHolder.mRefreshBtn.setVisibility(View.VISIBLE);
                }
            }


            // 도착 정보 표시 부분
            if (item.busRouteItem.arriveInfo.size() > 0) {

                if (item.busRouteItem.arriveInfo.size() > 0) {
                    if (item.type == Constants.FAVORITE_TYPE_BUS_STOP) {
                        itemViewHolder.mArrive1Txt.setVisibility(View.VISIBLE);
                    }

                    ArriveItem arriveItem = item.busRouteItem.arriveInfo.get(0);
                    String resultStr = "";

                    if (arriveItem.remainMin != -1) {

                        if (arriveItem.remainMin != 0) {
                            resultStr = arriveItem.remainMin + "분";
                        }

                    }

                    if (arriveItem.remainSecond != -1) {
                        resultStr = resultStr + arriveItem.remainSecond + "초";
                    }

                    if (arriveItem.remainStop != -1) {
                        if (resultStr.equals("")) {
                            resultStr = arriveItem.remainStop + "정류장 전";
                        } else {
                            resultStr = resultStr + "(" + arriveItem.remainStop
                                    + "정류장 전)";
                        }
                    }

                    if (arriveItem.state == Constants.STATE_PREPARE) {
                        try {
                            if (arriveItem.remainSecond == -9999) {
                                resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                            } else {
                                resultStr = mContext.getString(R.string.state_prepare);
                            }
                        } catch (Exception e) {
                            resultStr = mContext.getString(R.string.state_prepare);
                        }
                        ;
                    } else if (arriveItem.state == Constants.STATE_END) {
                        resultStr = mContext.getString(R.string.state_end);
                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                        resultStr = mContext.getString(R.string.state_prepare_not);
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        resultStr = mContext.getString(R.string.state_near);
                    }

                    itemViewHolder.mArrive1Txt.setText(resultStr);
                    itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
                }

                if (item.busRouteItem.arriveInfo.size() > 1) {

                    itemViewHolder.mArrive1Txt.setVisibility(View.VISIBLE);
                    itemViewHolder.mArrive2Txt.setVisibility(View.VISIBLE);

                    ArriveItem arriveItem = item.busRouteItem.arriveInfo.get(1);
                    String resultStr = "";

                    if (arriveItem.remainMin != -1) {

                        if (arriveItem.remainMin != 0) {
                            resultStr = arriveItem.remainMin + "분";
                        }

                    }

                    if (arriveItem.remainSecond != -1) {
                        resultStr = resultStr + arriveItem.remainSecond + "초";
                    }

                    if (arriveItem.remainStop != -1) {
                        if (resultStr.equals("")) {
                            resultStr = arriveItem.remainStop + "정류장 전";
                        } else {
                            resultStr = resultStr + "(" + arriveItem.remainStop
                                    + "정류장 전)";
                        }
                    }

                    if (arriveItem.state == Constants.STATE_PREPARE) {
                        try {
                            if (arriveItem.remainSecond == -9999) {
                                resultStr = arriveItem.remainMin + "시 " + ((arriveItem.remainStop == 0) ? arriveItem.remainStop + "0" : arriveItem.remainStop) + "분 출발";
                            } else {
                                resultStr = mContext.getString(R.string.state_prepare);
                            }
                        } catch (Exception e) {
                            resultStr = mContext.getString(R.string.state_prepare);
                        }
                        ;
                    } else if (arriveItem.state == Constants.STATE_END) {
                        resultStr = mContext.getString(R.string.state_end);
                    } else if (arriveItem.state == Constants.STATE_PREPARE_NOT) {
                        resultStr = mContext.getString(R.string.state_prepare_not);
                    } else if (arriveItem.state == Constants.STATE_NEAR) {
                        resultStr = mContext.getString(R.string.state_near);
                    }

                    itemViewHolder.mArrive2Txt.setText(resultStr);
                } else {
                    itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
                }
            } else {
                itemViewHolder.mArrive1Txt.setVisibility(View.GONE);
                itemViewHolder.mArrive2Txt.setVisibility(View.GONE);
            }

            if (item.busRouteItem.arriveInfo.size() > 0) {
                itemViewHolder.mDetailTxt.setVisibility(View.GONE);// .setText(item.nickName);
                itemViewHolder.mSubNAme1Txt.setVisibility(View.VISIBLE);
                itemViewHolder.mSubNAme1Txt.setText(item.nickName);


            } else {
                itemViewHolder.mDetailTxt.setVisibility(View.VISIBLE);// .setText(item.nickName);
                itemViewHolder.mSubNAme1Txt.setVisibility(View.GONE);

                if (item.type == Constants.FAVORITE_TYPE_STOP) {
                    itemViewHolder.mDetailTxt.setText(item.nickName2);
                } else {
                    itemViewHolder.mDetailTxt.setText(item.nickName);
                }

            }

            if (_isEditMode) {
                itemViewHolder.mArrive1Txt.setTextColor(mContext.getResources()
                        .getColor(R.color.arrive_nomal));
                itemViewHolder.mArrive2Txt.setTextColor(mContext.getResources()
                        .getColor(R.color.arrive_nomal));
            } else {
                itemViewHolder.mArrive1Txt.setTextColor(mContext.getResources()
                        .getColor(R.color.arrive_color));
                itemViewHolder.mArrive2Txt.setTextColor(mContext.getResources()
                        .getColor(R.color.arrive_color));
            }

            String packName = mContext.getPackageName(); // 패키지명
            if (item.color != null) {

                if (mMode == Constants.SETTING_FAOVIRET_TICKET_1) {

                    String resName = String.format("tag_%s", item.color);
                    int resID = mContext.getResources().getIdentifier(resName,
                            "drawable", packName);
                    itemViewHolder.mTicketColorView.setImageResource(resID);


                } else if (mMode == Constants.SETTING_FAOVIRET_TICKET_2) {

                    String resName = String.format("card1_color_%s", item.color);
                    int resID = mContext.getResources().getIdentifier(resName,
                            "drawable", packName);
                    itemViewHolder.mTicketColorView.setImageResource(resID);


                } else if (mMode == Constants.SETTING_FAOVIRET_TICKET_3) {

                    String resName = String.format("card2_color_%s", item.color);
                    int resID = mContext.getResources().getIdentifier(resName,
                            "drawable", packName);
                    itemViewHolder.mTicketColorView.setImageResource(resID);


                } else if (mMode == Constants.SETTING_FAOVIRET_TICKET_4) {

                    String resName = String.format("card7_color_%s", item.color);
                    int resID = mContext.getResources().getIdentifier(resName,
                            "drawable", packName);
                    itemViewHolder.mTicketColorView.setImageResource(resID);


                } else if (mMode == Constants.SETTING_FAOVIRET_TICKET_5) {

                    String resName = String.format("card8_color_%s", item.color);
                    int resID = mContext.getResources().getIdentifier(resName,
                            "drawable", packName);
                    itemViewHolder.mTicketColorView.setImageResource(resID);


                } else if (mMode == Constants.SETTING_FAOVIRET_TICKET_6) {

                    String resName = String.format("list10_color_%s", item.color);
                    int resID = mContext.getResources().getIdentifier(resName,
                            "drawable", packName);
                    itemViewHolder.mTicketColorView.setImageResource(resID);


                }


            } else {

                String resName = String.format("tag_1_1");
                int resID = mContext.getResources().getIdentifier(resName,
                        "drawable", packName);
                itemViewHolder.mTicketColorView.setImageResource(resID);
            }


            if (itemViewHolder.mName1Txt.getText().length() > 5 && itemViewHolder.mName1Txt.getText().length() < 8) {
                itemViewHolder.mName1Txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_txt_size2));

            } else if (itemViewHolder.mName1Txt.getText().length() > 7) {
                itemViewHolder.mName1Txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_txt_size3));

            } else {
                itemViewHolder.mName1Txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_txt_size));
            }

            if (itemViewHolder.mDetailTxt.getText().length() > 5 && itemViewHolder.mDetailTxt.getText().length() < 8) {
                itemViewHolder.mDetailTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_sub_txt_size2));

            } else if (itemViewHolder.mDetailTxt.getText().length() > 7) {
                itemViewHolder.mDetailTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_sub_txt_size3));

            } else {
                itemViewHolder.mDetailTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.card_sub_txt_size));
            }

        }

        @Override
        public void setIndex(final int index) {

            this.mIndex = index;

        }

    }

    public int getPrefTypeMode(String type) {

        int returnResult = 0;

        if (mPref != null) {

            if (type == Constants.SETTING_FAOVIRET_LIST_TYPE) {
                returnResult = mPref.getInt(type,
                        Constants.SETTING_FAOVIRET_LIST_1);
            } else if (type == Constants.SETTING_FAOVIRET_TICKET_TYPE) {
                returnResult = mPref.getInt(type,
                        Constants.SETTING_FAOVIRET_TICKET_1);
            }

        }

        return returnResult;

    }

}
