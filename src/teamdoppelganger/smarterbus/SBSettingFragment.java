package teamdoppelganger.smarterbus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseFragment;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.item.LocalInforItem;
import teamdoppelganger.smarterbus.item.NotiItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.Download;
import teamdoppelganger.smarterbus.util.common.Download.PDownloadListener;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import teamdoppelganger.smarterbus.util.db.LocalDBHelper;
import teamdoppelganger.smarterbus.util.widget.SlideButton;

/**
 * 셋팅 설정 관련
 *
 * @author DOPPELSOFT4
 */
@SuppressLint("ValidFragment")
public class SBSettingFragment extends SBBaseFragment implements PDownloadListener {

    ListView mListView;

    CustomListAdapter mCustomListAdapter;

    ArrayList<String> mListArray;

    SharedPreferences mPref;

    private LocalInforItem mLocalSaveInfor;

    public SBSettingFragment(int id, SQLiteDatabase db,
                             LocalDBHelper localDBHelper) {
        super(id, db, localDBHelper);
    }

    public SBSettingFragment() {

    }

    @Override
    public void onLayoutFinish(View view) {
        super.onLayoutFinish(view);

        mLocalSaveInfor = ((SBInforApplication) getActivity()
                .getApplicationContext()).mLocalSaveInfor;

        mListArray = new ArrayList<String>();

        mPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        initView(view);

    }

    private void initView(View view) {


        mListView = (ListView) view.findViewById(R.id.listView);

        String[] settingList = getResources().getStringArray(R.array.setting);
        mListArray = new ArrayList<String>();

        for (int i = 0; i < settingList.length; i++) {
            mListArray.add(settingList[i]);
        }

        mCustomListAdapter = new CustomListAdapter();
        mListView.setAdapter(mCustomListAdapter);


        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                switch (position) {
                    case 0:

                        ((SBInforApplication) getActivity().getApplicationContext())
                                .showLocationDialog(getLocalDBHelper(), getBusDbSqlite(), getActivity());
                        break;

                    case 1:

                        Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + getResources().getString(R.string.mail)));

                        try {
                            mailIntent.putExtra(Intent.EXTRA_TEXT, "(오류신고 하실때 스크린샷 또는 지역 및 정류장번호 등 자세히 알려주시면 문제해결에 큰 도움이 됩니다^^)\n\n\n\n\n" + "기기명 : " + Build.MODEL
                                    + "\n앱버전 : " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionCode
                                    + "\nOS버전 : " + android.os.Build.VERSION.SDK_INT + "\n");
                        } catch (NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        startActivity(mailIntent);
                        break;

                    case 2:

                        Intent intent = new Intent(getActivity(), SBWebView.class);
                        intent.putExtra(Constants.INTENT_URL, "http://115.68.14.18/gnuboard4/bbs/board.php?bo_table=notice");
                        getActivity().startActivity(intent);
                        break;

                    case 3:

                        mNotiItem = new NotiItem();
                        new GetInforData().execute();
                        break;

                    case 4:

                        ((SBInforApplication) getActivity().getApplicationContext())
                                .showLocationDialog(getLocalDBHelper(), getBusDbSqlite(), getActivity());
                        break;

                    case 6:

                        Intent intent1 = new Intent(getActivity(), SBWebView.class);
                        intent1.putExtra(Constants.INTENT_URL, "http://133.186.135.161:3000/static/privacy-terms.html");
                        getActivity().startActivity(intent1);
                        break;

                }


            }
        });

    }

    @Override
    public void selectedPage() {
        super.selectedPage();

        Tracker t = ((SBInforApplication) getActivity().getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_setting));
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    class CustomListAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            return mListArray.size();
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

            View view = convertView;
            ViewHolder viewHolder = null;

            if (view == null) {
                viewHolder = new ViewHolder();
                view = getActivity().getLayoutInflater().inflate(R.layout.setting_row,
                        parent, false);

                viewHolder.title = (TextView) view.findViewById(R.id.name);

                if (position == 4) {
                    final RelativeLayout slideLayout = (RelativeLayout) View.inflate(getActivity(), R.layout.slide_button, null);
                    LinearLayout slideLayoutParent = (LinearLayout) view.findViewById(R.id.setting_row_slide_layout);
                    slideLayoutParent.addView(slideLayout);

                    final SlideButton slideBtn = (SlideButton) slideLayout.findViewById(R.id.slideButton);

                    boolean isAlarmSoundOn = mPref.getBoolean(Constants.SETTING_ALARM, Constants.SETTING_ALARM_MODE_SOUND);

                    final TextView sound = (TextView) slideLayout.findViewById(R.id.slide_button_txt_sound);
                    final TextView vibe = (TextView) slideLayout.findViewById(R.id.slide_button_txt_vibe);

                    if (isAlarmSoundOn) {
                        slideBtn.open();
                        sound.setEnabled(true);
                        vibe.setEnabled(false);
                    } else {
                        slideBtn.close();
                        sound.setEnabled(false);
                        vibe.setEnabled(true);
                    }

                    slideBtn.setOnCheckChangedListner(new SlideButton.OnCheckChangedListner() {
                        @Override
                        public void onCheckChanged(View v, boolean isChecked) {
                            SharedPreferences.Editor ed = mPref.edit();
                            ed.putBoolean(Constants.SETTING_ALARM, !isChecked);
                            ed.commit();

                            if (isChecked) {
                                sound.setEnabled(false);
                                vibe.setEnabled(true);
                            } else {
                                sound.setEnabled(true);
                                vibe.setEnabled(false);
                            }
                        }
                    });

                    slideBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SharedPreferences.Editor ed = mPref.edit();
                            ed.putBoolean(Constants.SETTING_ALARM, !slideBtn.isChecked());
                            ed.commit();


                            boolean isChecked = slideBtn.isChecked();

                            if (isChecked) {
                                slideBtn.setChecked(false);
                                sound.setEnabled(true);
                                vibe.setEnabled(false);
                            } else {
                                slideBtn.setChecked(true);
                                sound.setEnabled(false);
                                vibe.setEnabled(true);
                            }


                        }
                    });


                } else if (position == 5) {

                    final RelativeLayout slideLayout = (RelativeLayout) View.inflate(getActivity(), R.layout.slide_button2, null);
                    LinearLayout slideLayoutParent = (LinearLayout) view.findViewById(R.id.setting_row_slide_layout);
                    slideLayoutParent.addView(slideLayout);


                    boolean isVibrate = mPref.getBoolean(Constants.SETTING_VIBRATE, Constants.SETTING_VIBRATE_ON);

                    final SlideButton slideBtn = (SlideButton) slideLayout.findViewById(R.id.slideButton);
                    final TextView sound = (TextView) slideLayout.findViewById(R.id.slide_button_txt_sound);
                    final TextView vibe = (TextView) slideLayout.findViewById(R.id.slide_button_txt_vibe);


                    if (isVibrate) {
                        slideBtn.open();
                        sound.setEnabled(true);
                        vibe.setEnabled(false);
                    } else {
                        slideBtn.close();
                        sound.setEnabled(false);
                        vibe.setEnabled(true);
                    }


                    slideBtn.setOnCheckChangedListner(new SlideButton.OnCheckChangedListner() {
                        @Override
                        public void onCheckChanged(View v, boolean isChecked) {
                            SharedPreferences.Editor ed = mPref.edit();
                            ed.putBoolean(Constants.SETTING_VIBRATE, !isChecked);
                            ed.commit();

                            if (isChecked) {
                                sound.setEnabled(false);
                                vibe.setEnabled(true);
                            } else {
                                sound.setEnabled(true);
                                vibe.setEnabled(false);
                            }
                        }
                    });

                    slideBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SharedPreferences.Editor ed = mPref.edit();
                            ed.putBoolean(Constants.SETTING_VIBRATE, !slideBtn.isChecked());
                            ed.commit();


                            boolean isChecked = slideBtn.isChecked();

                            if (isChecked) {
                                slideBtn.setChecked(false);
                                sound.setEnabled(true);
                                vibe.setEnabled(false);
                            } else {
                                slideBtn.setChecked(true);
                                sound.setEnabled(false);
                                vibe.setEnabled(true);
                            }


                        }
                    });

                }

                view.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }


            if (position == 0) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_setting_list_1), null, null, null);
                viewHolder.title.setText("  지역 설정");

            } else if (position == 1) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_setting_list_3), null, null, null);
                viewHolder.title.setText(" 어플리케이션 문의 및 오류 보고");

            } else if (position == 2) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds
                        (getResources().getDrawable(R.drawable.ic_setting_list_4), null, null, null);
                viewHolder.title.setText("  FAQ");

            } else if (position == 3) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_setting_list_5), null, null, null);
                viewHolder.title.setText(" 데이터베이스 업데이트");

            } else if (position == 4) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_setting_list_6), null, null, null);
                viewHolder.title.setText(" 알람");

            } else if (position == 5) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.ic_setting_list_7), null, null, null);
                viewHolder.title.setText("진동설정");

            } else if (position == 6) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        getResources().getDrawable(R.drawable.btn_ic_info_normal), null, null, null);
                viewHolder.title.setText("개인정보처리방침");

            } else if (position == 7) {
                viewHolder.title.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null);
                viewHolder.title.setText("위치기반서비스 이용약관");

            }

            return view;
        }


        class ViewHolder {
            TextView title;
            SlideButton slide;
        }

    }

    NotiItem mNotiItem;

    class GetInforData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            try {

                int versionCode = getActivity().getPackageManager().getPackageInfo(
                        getActivity().getApplicationInfo().packageName, 0).versionCode;
                String url = String.format(Constants.LOCAL_URL + Constants.PATH_NOTI_DB_VERSION + "?appV=%s&type=android", versionCode);

                String result = RequestCommonFuction.getSource(url, false, "", "utf-8");

                JSONObject object;

                object = new JSONObject(result);

                int appV = object.getInt("appV");
                int dbV = object.getInt("dbV");
                String fileDownPath = object.getString("fileRoute");
                boolean isDownNeed = object.getBoolean("downNeed");

                mNotiItem.dbVersion = dbV;
                mNotiItem.fileName = fileDownPath;
                mNotiItem.isDownNeed = isDownNeed;


            } catch (NameNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (mNotiItem.isDownNeed) {
                //다운로드 화면으로 이동
            }

            if (mNotiItem.dbVersion == 0
                    || Constants.DBVERSION == mNotiItem.dbVersion) {

                try {
                    String[] tmpSplit = mNotiItem.fileName.split("/");
                    String fileName = tmpSplit[tmpSplit.length - 1].split("\\.")[0];
                    fileName = fileName + ".sqlite";
                    if (!fileName.contains("null")) {
                        SharedPreferences.Editor ed = mPref.edit();
                        ed.putInt(Constants.PREF_DB_VERSION, mNotiItem.dbVersion);
                        ed.putString(Constants.PREF_DB_NAME, fileName);
                        ed.commit();
                    }
                } catch (Exception e) {
                }

                useLocalDb();

            } else {

                int oldVersion = mPref.getInt(Constants.PREF_DB_VERSION, 0);

                if (mNotiItem.dbVersion > oldVersion) {

                    gpsAgreeDialog();

                } else {

                    useLocalDb();

                }
            }
        }
    }

    private void useLocalDb() {
        if (mPref.getString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME).equals(Constants.PREF_DEFAULT_DB_NAME)) {
            installDBAndStart();
        } else {
            Toast.makeText(getActivity(), "Database가 최신 버전입니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void installDBAndStart() {
        initDB();
        Toast.makeText(getActivity(), "DB 업데이트가 완료되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void initDB() {

        File outfile = null;
        try {
            File folder = new File(Constants.LOCAL_PATH);

            folder.mkdirs();

            outfile = new File(Constants.LOCAL_PATH
                    + Constants.PREF_DEFAULT_DB_NAME);

            if (!outfile.exists()) {
                outfile.createNewFile();
            }

            AssetManager assetManager = getResources().getAssets();
            InputStream is;
            is = assetManager.open(Constants.PREF_DEFAULT_DB_NAME,
                    AssetManager.ACCESS_BUFFER);

            long filesize = is.available();

            if (outfile.length() < filesize) {
                byte[] tempdata = new byte[(int) filesize];
                is.read(tempdata);
                is.close();
                outfile.createNewFile();

                FileOutputStream fo = new FileOutputStream(outfile);
                fo.write(tempdata);
                fo.close();
            }
        } catch (Exception e) {
        }
    }

    private void gpsAgreeDialog() {

        final SBDialog dialog = new SBDialog(getActivity());

        TextView title = new TextView(getActivity());
        TextView view = new TextView(getActivity());

        title.setText("DB 업데이트");
        title.setTextSize(25);
        view.setText("새로운 DB가 있습니다. 다운로드 하시겠습니까?\nWi-Fi가 연결되지 않았을때는 요금이 부과 될 수 있습니다.");
        view.setTextSize(18);

        dialog.setTitleLayout(title);
        dialog.setViewLayout(view);

        dialog.getPositiveButton("확인").setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startDbDownload(mNotiItem.fileName);
                dialog.dismiss();
            }
        });
        dialog.getNegativeButton("취소").setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                useLocalDb();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void startDbDownload(String downPath) {
        Download download = new Download(getActivity());
        download.setDownloadListener(this);
        download.startDownload(downPath);
    }

    @Override
    public void downLoadFinish(int result) {
        if (result == Constants.RETURN_SUCCESS) {
            Toast.makeText(getActivity(), "DB 업데이트가 완료되었습니다.", Toast.LENGTH_SHORT).show();

            String[] tmpSplit = mNotiItem.fileName.split("/");

            String fileName = tmpSplit[tmpSplit.length - 1].split("\\.")[0];

            fileName = fileName + ".sqlite";

            if (tmpSplit.length > 0) {
                SharedPreferences.Editor ed = mPref.edit();
                ed.putInt(Constants.PREF_DB_VERSION, mNotiItem.dbVersion);
                ed.putString(Constants.PREF_DB_NAME, fileName);
                ed.commit();
            }

        } else if (result == Constants.RETURN_FAIL) {

            useLocalDb();

        }

    }

}
