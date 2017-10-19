package teamdoppelganger.smarterbus.util.widget;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.item.DepthItem;
import teamdoppelganger.smarterbus.item.FavoriteAndHistoryItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GetData;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TicketLayout extends RelativeLayout {

    View mView;
    ImageView mTicketColorView;
    FavoriteAndHistoryItem mItem;


    TextView mName1Txt, mSubNAme1Txt;
    TextView mArrive1Txt, mArrive2Txt;
    TextView mDetailTxt;

    Button mRefreshBtn, mDelBtn;

    TicketListener mTicketListener;

    boolean mIsEdit = false;

    public interface TicketListener {
        public void onRefresh(View view, int id, FavoriteAndHistoryItem item);
    }

    public void setTicketListener(TicketListener l) {
        mTicketListener = l;
    }


    public TicketLayout(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.ticket, null);
        this.addView(mView);

        initView(mView);
    }


    public TicketLayout(Context context, AttributeSet attrs) {

        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.ticket, null);
        this.addView(mView);
        initView(mView);

    }

    private void initView(View view) {

        mTicketColorView = (ImageView) view.findViewById(R.id.backColor);
        mName1Txt = (TextView) view.findViewById(R.id.name1);
        mSubNAme1Txt = (TextView) view.findViewById(R.id.subname1);
        mArrive1Txt = (TextView) view.findViewById(R.id.arrive_1);
        mArrive2Txt = (TextView) view.findViewById(R.id.arrive_2);
        mDetailTxt = (TextView) view.findViewById(R.id.detail);
        mRefreshBtn = (Button) view.findViewById(R.id.refreshBtn);
        mDelBtn = (Button) view.findViewById(R.id.delBtn);


        if (mIsEdit) {
            mDelBtn.setVisibility(View.VISIBLE);
        } else {
            mDelBtn.setVisibility(View.GONE);
        }

    }


    public void setBusDB() {

    }

    public void reset() {

        mName1Txt.setText("");
        mDetailTxt.setText("");

    }

    public void setFavoriteItem(final FavoriteAndHistoryItem item) {

        mItem = item;
        if (mItem.type == Constants.FAVORITE_TYPE_BUS) {

            mName1Txt.setText(item.busRouteItem.busRouteName);
            mDetailTxt.setText(item.nickName);

            mArrive1Txt.setVisibility(View.GONE);
            mArrive2Txt.setVisibility(View.GONE);
            mSubNAme1Txt.setVisibility(View.GONE);
            mRefreshBtn.setVisibility(View.GONE);

        } else if (mItem.type == Constants.FAVORITE_TYPE_STOP) {

            mName1Txt.setText(item.busStopItem.name);
            mDetailTxt.setText(item.nickName);

            mArrive1Txt.setVisibility(View.GONE);
            mArrive2Txt.setVisibility(View.GONE);
            mSubNAme1Txt.setVisibility(View.GONE);
            mRefreshBtn.setVisibility(View.GONE);

        } else if (mItem.type == Constants.FAVORITE_TYPE_BUS_STOP) {

            mName1Txt.setText(item.busRouteItem.busRouteName);
            mDetailTxt.setText(item.nickName);

            mArrive1Txt.setVisibility(View.GONE);
            mArrive2Txt.setVisibility(View.GONE);
            mSubNAme1Txt.setVisibility(View.GONE);
            mRefreshBtn.setVisibility(View.VISIBLE);


            mRefreshBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTicketListener != null) {
                        mTicketListener.onRefresh(mView, v.getId(), item);
                    }
                }
            });
        }


    }

    public void setViewWidth(int width) {

        mView.getLayoutParams().width = width;
        mView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.ticket_height);
        mView.requestLayout();

    }

    public void changeSetColor(int resId) {
        mTicketColorView.setImageResource(resId);
    }

    public void setType(int type) {

    }

    public void setEditMode(boolean isEdit) {
        mIsEdit = isEdit;
        if (isEdit) {
            mDelBtn.setVisibility(View.VISIBLE);
        } else {
            mDelBtn.setVisibility(View.GONE);
        }
    }

}
