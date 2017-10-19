package teamdoppelganger.smarterbus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SBChoiceDialog extends AlertDialog {

    LinearLayout mCustomLayout;
    RelativeLayout mCustomLayout2;
    LinearLayout mTitleLayout;

    LinearLayout mBtnLayout;

    public Button mPositiveBtn;
    public Button mNegativeBtn;

    TextView mDefaultTitle;
    TextView mDefaultView;

    ImageView mBuleLine;

    public ListView mListView;
    public GridView mGridView;

    boolean mIsFromSetting;
    boolean mIsListMode;
    Context mContext;


    int mCurPosition;


    GridViewAdapter mGridViewAdapter;

    public SBChoiceDialog(Context context) {
        this(context, false);
        mContext = context;
    }

    public SBChoiceDialog(Context context, boolean isFromSetting) {
        super(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mContext = context;


        mIsFromSetting = isFromSetting;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.custom_dialog3, null);
        setView(v);
        mCustomLayout2 = (RelativeLayout) v.findViewById(R.id.cutom_dialog_parent);


        mTitleLayout = (LinearLayout) v.findViewById(R.id.custom_dialog_title);
        mBtnLayout = (LinearLayout) v.findViewById(R.id.custom_dialog_btn_parent);

        mPositiveBtn = (Button) v.findViewById(R.id.custom_dialog_btn_positive);
        mNegativeBtn = (Button) v.findViewById(R.id.custom_dialog_btn_negative);

        mDefaultTitle = (TextView) v.findViewById(R.id.custom_dialog_title_txt);
        mDefaultView = (TextView) v.findViewById(R.id.custom_dialog_view_txt);

        mBuleLine = (ImageView) v.findViewById(R.id.custom_dialog_line);
        mListView = (ListView) v.findViewById(R.id.selectListView);
        mGridView = (GridView) v.findViewById(R.id.gridView1);


        if (mIsFromSetting) {
            mListView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            SampleListAdapter simpleListAdapter = new SampleListAdapter();
            mListView.setAdapter(simpleListAdapter);
        } else {
            mListView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);

            mGridViewAdapter = new GridViewAdapter();
            mGridView.setAdapter(mGridViewAdapter);

        }

    }


    public void setCurrentPosition(int position) {
        mCurPosition = position;
    }

    public void setListMode(boolean isListMode) {
        mIsListMode = isListMode;
    }

    public void setTitleLayout(String string) {
        setTitleLayout(string, 0xFF0998FF);
    }

    public void setTitleLayout(String string, int color) {
        mTitleLayout.setVisibility(View.VISIBLE);
        mBuleLine.setVisibility(View.VISIBLE);
        mDefaultTitle.setVisibility(View.VISIBLE);
        mDefaultTitle.setText(string);
        mDefaultTitle.setTextColor(color);
    }

    public void setTitleLayout(View v) {
        mTitleLayout.setVisibility(View.VISIBLE);
        mTitleLayout.addView(v);
        mBuleLine.setVisibility(View.VISIBLE);
    }

    public void setViewLayout(String msg) {
        mDefaultView.setVisibility(View.VISIBLE);
        mDefaultView.setText(msg);
    }

    public void setViewLayout(View v) {
    }

    @Override
    public void show() {


        super.show();

    }

    public Button getPositiveButton(String text) {
        mPositiveBtn.setVisibility(View.VISIBLE);
        mBtnLayout.setVisibility(View.VISIBLE);
        if (text != null)
            mPositiveBtn.setText(text);
        return mPositiveBtn;
    }

    public Button getNegativeButton(String text) {
        mNegativeBtn.setVisibility(View.VISIBLE);
        mBtnLayout.setVisibility(View.VISIBLE);
        if (text != null)
            mNegativeBtn.setText(text);
        return mNegativeBtn;
    }


    class SampleListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 5;
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
        public View getView(int position, View convertView, ViewGroup parent) {


            convertView = getLayoutInflater().inflate(R.layout.favorite_list_image_row,
                    parent, false);


            ViewHolder viewHolder;


            convertView = getLayoutInflater().inflate(R.layout.favorite_list_row_sample,
                    parent, false);


            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.arriveLayout = (LinearLayout) convertView.findViewById(R.id.arriveLayout);
            viewHolder.arrive1 = (TextView) convertView.findViewById(R.id.arrive_1);
            viewHolder.arrive2 = (TextView) convertView.findViewById(R.id.arrive_2);
            viewHolder.name = (TextView) convertView.findViewById(R.id.name);
            viewHolder.subName1 = (TextView) convertView.findViewById(R.id.subMain);
            viewHolder.subName2 = (TextView) convertView.findViewById(R.id.subMain2);
            viewHolder.iconImgView = (ImageView) convertView.findViewById(R.id.iconImg);
            viewHolder.drag_handle = (ImageView) convertView.findViewById(R.id.drag_handle);
            viewHolder.delBtn = (ImageView) convertView.findViewById(R.id.delImg);
            viewHolder.progress = (ProgressBar) convertView.findViewById(R.id.list_progress);
            viewHolder.checkImg = (ImageView) convertView.findViewById(R.id.selectChecked);

            viewHolder.colorLayout = (ImageView) convertView.findViewById(R.id.colorLayout);

            if (mCurPosition == position) {
                viewHolder.checkImg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkImg.setVisibility(View.GONE);
            }


            String packName = mContext.getPackageName();

            viewHolder.delBtn.setVisibility(View.GONE);
            viewHolder.drag_handle.setVisibility(View.GONE);

            viewHolder.name.setText("홍대앞 사거리");
            viewHolder.subName1.setText("12-234");
            viewHolder.iconImgView.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.ic_stop));


            if (position == 0) {

                String resName = String.format("list_color_%s", "1_5");
                int resID = mContext.getResources().getIdentifier(resName,
                        "drawable", packName);
                viewHolder.colorLayout.setImageResource(resID);


            } else if (position == 1) {

                String resName = String.format("list2_color_%s", "2_4");
                int resID = mContext.getResources().getIdentifier(resName,
                        "drawable", packName);

                LayoutParams layoutParams = (LayoutParams) viewHolder.colorLayout.getLayoutParams();
                layoutParams.height = layoutParams.MATCH_PARENT;
                layoutParams.width = 10;
                viewHolder.colorLayout.setLayoutParams(layoutParams);
                viewHolder.colorLayout.setImageResource(resID);


            } else if (position == 2) {

                String resName = String.format("list3_color_%s", "2_1");
                int resID = mContext.getResources().getIdentifier(resName,
                        "drawable", packName);


                LayoutParams layoutParams = (LayoutParams) viewHolder.colorLayout.getLayoutParams();
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.leftMargin = (int) mContext.getResources().getDimension(R.dimen.favorite_list_margin_left);
                viewHolder.colorLayout.setLayoutParams(layoutParams);


                viewHolder.colorLayout.setImageResource(resID);


            } else if (position == 3) {

                String resName = String.format("list4_color_%s", "4_1");
                int resID = mContext.getResources().getIdentifier(resName,
                        "drawable", packName);

                LayoutParams layoutParams = (LayoutParams) viewHolder.colorLayout.getLayoutParams();
                layoutParams.height = layoutParams.MATCH_PARENT;
                layoutParams.width = 6;
                viewHolder.colorLayout.setLayoutParams(layoutParams);
                viewHolder.colorLayout.setImageResource(resID);

            } else if (position == 4) {

                String resName = String.format("list5_color_%s", "3_4");
                int resID = mContext.getResources().getIdentifier(resName,
                        "drawable", packName);

                LayoutParams layoutParams = (LayoutParams) viewHolder.colorLayout.getLayoutParams();
                layoutParams.height = layoutParams.WRAP_CONTENT;
                viewHolder.colorLayout.setLayoutParams(layoutParams);
                layoutParams.leftMargin = (int) mContext.getResources().getDimension(R.dimen.favorite_list_margin_left);

                viewHolder.colorLayout.setImageResource(resID);

            }

            return convertView;

        }


        class ViewHolder {

            LinearLayout arriveLayout;
            TextView arrive1, arrive2;

            ImageView colorLayout, iconImgView;
            ImageView drag_handle;
            ImageView checkImg;
            ImageView delBtn;
            TextView name;
            TextView subName1, subName2;

            ProgressBar progress;

        }

    }


    class GridViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return 6;
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

            ViewHolder viewHolder;

            if (convertView == null) {

                convertView = getLayoutInflater().inflate(R.layout.ticket_sample_1,
                        parent, false);

                viewHolder = new ViewHolder();
                viewHolder.imageSample = (ImageView) convertView.findViewById(R.id.imgSample);
                viewHolder.checkImg = (ImageView) convertView.findViewById(R.id.selectChecked);


                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            if (mCurPosition == position) {
                viewHolder.checkImg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkImg.setVisibility(View.GONE);
            }


            if (position == 0) {
                viewHolder.imageSample.setImageResource(R.drawable.preview_2);
            } else if (position == 1) {
                viewHolder.imageSample.setImageResource(R.drawable.preview_1);
            } else if (position == 2) {
                viewHolder.imageSample.setImageResource(R.drawable.preview_3);
            } else if (position == 3) {
                viewHolder.imageSample.setImageResource(R.drawable.preview_4);
            } else if (position == 4) {
                viewHolder.imageSample.setImageResource(R.drawable.preview_5);
            } else if (position == 5) {
                viewHolder.imageSample.setImageResource(R.drawable.preview_6);
            }

            return convertView;

        }

        class ViewHolder {
            ImageView imageSample;
            ImageView checkImg;
        }

    }
}
