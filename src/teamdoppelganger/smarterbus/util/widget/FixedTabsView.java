/*
 * Copyright (C) 2011 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamdoppelganger.smarterbus.util.widget;

import teamdoppelganger.smarterbus.R;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FixedTabsView extends LinearLayout implements ViewPager.OnPageChangeListener {

    private Context mContext;

    private ViewPager mPager;

    private int mCurrentPosition = 0;

    private View mView;

    private Button mBtnFavo;
    private Button mBtnSearch;

    private boolean mIsPageSelected;

    public FixedTabsView(Context context) {
        this(context, null);
    }

    public FixedTabsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FixedTabsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);

        this.mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.timetable_tab_layout, null);

        LinearLayout layout = (LinearLayout) mView.findViewById(R.id.timetable_date_bar);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        addView(layout, params);

        mBtnFavo = (Button) mView.findViewById(R.id.widget_activity_btn_favo);
        mBtnSearch = (Button) mView.findViewById(R.id.widget_activity_btn_search);

        mBtnFavo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mBtnFavo.setSelected(true);
                mBtnSearch.setSelected(false);

                if (mIsPageSelected) {
                    mIsPageSelected = false;
                    return;
                }

                mPager.setCurrentItem(0);
            }
        });

        mBtnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                mBtnFavo.setSelected(false);
                mBtnSearch.setSelected(true);

                if (mIsPageSelected) {
                    mIsPageSelected = false;
                    return;
                }

                mPager.setCurrentItem(1);

            }
        });

    }


    public void setViewPager(ViewPager pager) {
        this.mPager = pager;
        mPager.setOnPageChangeListener(this);

        if (mPager != null)
            selectTab(mPager.getCurrentItem());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        selectTab(position);
        mCurrentPosition = position;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    private void selectTab(int position) {

        mIsPageSelected = true;

        switch (position) {
            case 0:
                mBtnFavo.performClick();
                break;
            case 1:
                mBtnSearch.performClick();
                break;
        }

    }

}
