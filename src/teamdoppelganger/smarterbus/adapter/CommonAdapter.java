package teamdoppelganger.smarterbus.adapter;

import java.util.ArrayList;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.item.LocalItem;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class CommonAdapter extends BaseAdapter {


    public static final int TYPE_COMMON_TEXTVIEW = 0;
    public static final int TYPE_COMMON_CHECKBOX = 1;
    public static final int TYPE_COMMON_TEXTVIEW2 = 2;

    ArrayList<String> listString;
    ArrayList<LocalItem> localItemList;

    LayoutInflater _inflater;
    Context mContext;

    int mType;

    public CommonAdapter(ArrayList<?> listItem, Context context, int type) {

        localItemList = new ArrayList<LocalItem>();
        mType = type;
        mContext = context;

        if (mType == TYPE_COMMON_CHECKBOX) {
            localItemList = (ArrayList<LocalItem>) listItem;
        } else {
            listString = (ArrayList<String>) listItem;
        }


        _inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);


        // TODO Auto-generated constructor stub
    }


    public ArrayList<LocalItem> getLocalItemList() {
        return localItemList;
    }


    @Override
    public int getCount() {
        if (mType == TYPE_COMMON_CHECKBOX) {
            return localItemList.size();
        } else {
            return listString.size();
        }

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

        View v = convertView;

        return getTypeView(v, position);

    }


    public View getTypeView(View v, final int position) {

        if (mType == TYPE_COMMON_CHECKBOX) {


            final LocalItem item = localItemList.get(position);


            CustomViewHolder2 viewHolder = null;
            if (v == null) {

                v = _inflater.inflate(R.layout.common_list_checkbox_item, null);

                viewHolder = new CustomViewHolder2();
                v.setTag(viewHolder);
            } else {
                viewHolder = (CustomViewHolder2) v.getTag();
            }

            viewHolder.text1 = (TextView) v.findViewById(R.id.text1);
            viewHolder.checkBox = (CheckBox) v.findViewById(R.id.checkBox);

            if (!item.isArea) {
                LayoutParams layoutParams = (LayoutParams) viewHolder.checkBox.getLayoutParams();
                layoutParams.leftMargin = 60;
                viewHolder.checkBox.setLayoutParams(layoutParams);
            } else {
                LayoutParams layoutParams = (LayoutParams) viewHolder.checkBox.getLayoutParams();
                layoutParams.leftMargin = 20;
                viewHolder.checkBox.setLayoutParams(layoutParams);
            }


            if (item.isChecked) {
                viewHolder.checkBox.setChecked(true);

                if (!item.isArea) {

                    int tempIndex = -1;
                    boolean isNeedCheck = true;
                    for (int i = 0; i < localItemList.size(); i++) {

                        LocalItem localItem = localItemList.get(i);

                        if (localItem.areaId == item.areaId && localItem.isArea) {
                            tempIndex = i;
                        } else if (localItem.areaId == item.areaId) {
                            if (!localItem.isChecked) {
                                isNeedCheck = false;
                                break;
                            }
                        }
                    }

                    if (isNeedCheck && tempIndex != -1) {

                        if (!localItemList.get(tempIndex).isChecked) {
                            localItemList.get(tempIndex).isChecked = true;
                            notifyDataSetChanged();
                        }


                    }

                }


            } else {
                viewHolder.checkBox.setChecked(false);
            }

            viewHolder.checkBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    CheckBox checkBox = (CheckBox) v;
                    item.isChecked = checkBox.isChecked();
                    localItemList.set(position, item);

                    if (item.isArea) {

                        if (checkBox.isChecked()) {

                            for (int i = 0; i < localItemList.size(); i++) {

                                LocalItem localItem = localItemList.get(i);

                                if (localItem.areaId == item.areaId) {
                                    localItemList.get(i).isChecked = true;
                                }
                            }
                        } else {
                            for (int i = 0; i < localItemList.size(); i++) {

                                LocalItem localItem = localItemList.get(i);

                                if (localItem.areaId == item.areaId) {
                                    localItemList.get(i).isChecked = false;
                                }
                            }

                        }
                    } else {

                        //일반 도시 클릭 시 , 지역에 관련된 도시들이  클릭 됬을 시  지역 자동 클릭/해제 기능 추가

                        if (!item.isChecked) {
                            for (int i = 0; i < localItemList.size(); i++) {

                                LocalItem localItem = localItemList.get(i);

                                if (localItem.areaId == item.areaId && localItem.isArea) {
                                    localItemList.get(i).isChecked = false;
                                }
                            }
                        } else {

                            int tempIndex = -1;
                            boolean isNeedCheck = true;
                            for (int i = 0; i < localItemList.size(); i++) {

                                LocalItem localItem = localItemList.get(i);

                                if (localItem.areaId == item.areaId && localItem.isArea) {
                                    tempIndex = i;
                                } else if (localItem.areaId == item.areaId) {
                                    if (!localItem.isChecked) {
                                        isNeedCheck = false;
                                        break;
                                    }
                                }
                            }

                            if (isNeedCheck && tempIndex != -1) {
                                localItemList.get(tempIndex).isChecked = true;
                            }

                        }

                    }


                    dataChange();
                }
            });

            v.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    v.findViewById(R.id.checkBox).performClick();

                }
            });

            viewHolder.text1.setText(item.koName);

        } else if (mType == TYPE_COMMON_TEXTVIEW2) {
            CustomViewHolder viewHolder = null;

            if (v == null) {

                v = _inflater.inflate(R.layout.common_list_item2, null);

                viewHolder = new CustomViewHolder();
                v.setTag(viewHolder);
            } else {
                viewHolder = (CustomViewHolder) v.getTag();
            }

            viewHolder.text1 = (TextView) v.findViewById(R.id.text1);
            viewHolder.text1.setText(listString.get(position));


        } else {
            CustomViewHolder viewHolder = null;

            if (v == null) {

                v = _inflater.inflate(R.layout.common_list_item, null);

                viewHolder = new CustomViewHolder();
                v.setTag(viewHolder);
            } else {
                viewHolder = (CustomViewHolder) v.getTag();
            }

            viewHolder.text1 = (TextView) v.findViewById(R.id.text1);
            viewHolder.text1.setText(listString.get(position));

        }

        return v;

    }


    public void dataChange() {
        this.notifyDataSetChanged();
        ;
    }

    class CustomViewHolder {
        TextView text1;
    }

    class CustomViewHolder2 {
        CheckBox checkBox;
        TextView text1;
    }


}
