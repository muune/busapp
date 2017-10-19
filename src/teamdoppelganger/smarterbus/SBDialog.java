package teamdoppelganger.smarterbus;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SBDialog extends AlertDialog {
	
	LinearLayout mCustomLayout;
	RelativeLayout mCustomLayout2;
	LinearLayout mTitleLayout;
	LinearLayout mViewLayout;
	LinearLayout mBtnLayout;
	
	Button mPositiveBtn;
	Button mNegativeBtn;
	
	TextView mDefaultTitle;
	TextView mDefaultView;
	
	ImageView mBuleLine;
	
	boolean mIsFromSetting;
	
	public SBDialog(Context context) {
		this(context, false);
	}
	
	public SBDialog(Context context, boolean isFromSetting) {
		super(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
		getWindow().setBackgroundDrawableResource(android.R.color.transparent);

		
		mIsFromSetting = isFromSetting;
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.custom_dialog, null);

		if(mIsFromSetting){
			v = inflater.inflate(R.layout.custom_dialog2, null);
			mCustomLayout2 = (RelativeLayout)v.findViewById(R.id.cutom_dialog_parent);
		}else {
			mCustomLayout = (LinearLayout) v.findViewById(R.id.cutom_dialog_parent);
		}
		
		mTitleLayout = (LinearLayout)v.findViewById(R.id.custom_dialog_title);
		mViewLayout = (LinearLayout)v.findViewById(R.id.custom_dialog_view);
		mBtnLayout = (LinearLayout)v.findViewById(R.id.custom_dialog_btn_parent);
		
		mPositiveBtn = (Button)v.findViewById(R.id.custom_dialog_btn_positive);
		mNegativeBtn = (Button)v.findViewById(R.id.custom_dialog_btn_negative);

		mDefaultTitle = (TextView)v.findViewById(R.id.custom_dialog_title_txt);
		mDefaultView = (TextView)v.findViewById(R.id.custom_dialog_view_txt);
		
		mBuleLine = (ImageView)v.findViewById(R.id.custom_dialog_line);
		
		setCancelable(false);

	}
	public void setTitleLayout(String string){
		setTitleLayout(string, 0xFF0998FF);
	}
	public void setTitleLayout(String string, int color){
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
	public void setViewLayout(String msg){
		mViewLayout.setVisibility(View.VISIBLE);
		mDefaultView.setVisibility(View.VISIBLE);
		mDefaultView.setText(msg);
	}
	public void setViewLayout(View v){
		mViewLayout.setVisibility(View.VISIBLE);
		mViewLayout.addView(v);
	}
	@Override
	public void show() {
		if(mIsFromSetting) {
			setView(mCustomLayout2);
		}else {
			setView(mCustomLayout);
		}
		super.show();

	}

	public Button getPositiveButton(String text){
		mPositiveBtn.setVisibility(View.VISIBLE);
		mBtnLayout.setVisibility(View.VISIBLE);
		if(text != null)
			mPositiveBtn.setText(text);
		return mPositiveBtn;
	}
	public Button getNegativeButton(String text){
		mNegativeBtn.setVisibility(View.VISIBLE);
		mBtnLayout.setVisibility(View.VISIBLE);
		if(text != null)
			mNegativeBtn.setText(text);
		return mNegativeBtn;
	}

}
