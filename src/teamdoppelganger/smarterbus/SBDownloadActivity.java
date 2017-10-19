package teamdoppelganger.smarterbus;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONObject;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.item.NotiItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.Download;
import teamdoppelganger.smarterbus.util.common.Download.PDownloadListener;
import teamdoppelganger.smarterbus.util.common.GoogleAppIdTask;
import teamdoppelganger.smarterbus.util.common.ImageStorage;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 다운로드 완료 메세지 후 activity 실행되도록 수정
 * 인트로 화면도 포함할 예정의 activity
 *
 * @author DOPPELSOFT4
 */
public class SBDownloadActivity extends SBBaseActivity implements PDownloadListener {

    NotiItem mNotiItem;
    SharedPreferences mPref;
    AlertDialog mDbInstallDialog;

    Handler mHandler = new Handler();
    Runnable mRunnable;

    SharedPreferences.Editor ed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);

        if (Constants.DEBUG_MODE)
            Toast.makeText(this, "DEBUG MODE", Toast.LENGTH_LONG).show();

        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean isLocationInfoAgree = mPref.getBoolean(Constants.PREF_LOCATION_INFO_AGREE, false);

        if (Constants.MARKET_TYPE == Constants.MARKET_TSTORE && !isLocationInfoAgree) {

            SBDialog dialog = new SBDialog(this, false);
            dialog.setTitleLayout("알림");
            dialog.setViewLayout("본 서비스는 고객님의 '위치 정보'를 사용합니다 \n사용에 동의하시겠습니까?");
            dialog.setCancelable(false);
            dialog.getPositiveButton("ok").setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putBoolean("gps_contents_agreed", true);
                    editor.putBoolean(Constants.PREF_LOCATION_INFO_AGREE, true);
                    editor.commit();

                    mNotiItem = new NotiItem();
                    new GetInforData().execute();
                }
            });
            dialog.getNegativeButton("cancel").setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            dialog.show();

            return;
        }

        ed = mPref.edit();
        ed.putString(Constants.PREF_EVENT_NAME, "");
        ed.commit();

        mNotiItem = new NotiItem();
        new GetInforData().execute();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDbInstallDialog != null)
            mDbInstallDialog.dismiss();
    }

    @Override
    public void downLoadFinish(int result) {

        if (result == Constants.RETURN_SUCCESS) {
            startMainActivity();

            String[] tmpSplit = mNotiItem.fileName.split("/");

            String fileName = tmpSplit[tmpSplit.length - 1].split("\\.")[0];

            fileName = fileName + ".sqlite";

            if (tmpSplit.length > 0 && !fileName.equals("null")) {
                SharedPreferences.Editor ed = mPref.edit();
                ed.putInt(Constants.PREF_DB_VERSION, mNotiItem.dbVersion);
                ed.putString(Constants.PREF_DB_NAME, fileName);
                ed.commit();
            }

        } else if (result == Constants.RETURN_FAIL) {

            useLocalDb();

        }

    }


    private void startMainActivity() {
        if (mNotiItem.strEventBanner.length() > 1) {
            imgDownLoad();
        } else {
            startIntentMainActivity();
        }
    }

    private void startIntentMainActivity() {

        mRunnable = new Runnable() {
            @Override
            public void run() {


                Intent intent = new Intent(getApplicationContext(), SBMainNewActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        };

        mHandler.postDelayed(mRunnable, 200);

    }

    private void useLocalDb() {
        if (mPref.getInt(Constants.PREF_DB_VERSION, 0) < Constants.DBVERSION) {
            initDB();
        }

        try {
            new GoogleAppIdTask(this).execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        startMainActivity();
    }

    @SuppressLint("NewApi")
    private void showDbDownloadDialog() {

        final SBDialog dialog = new SBDialog(this);

        TextView title = new TextView(this);
        TextView view = new TextView(this);

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
                finish();
            }
        });

        dialog.show();

    }

    private void startDbDownload(String downPath) {
        Download download = new Download(this);
        download.setDownloadListener(this);
        download.startDownload(downPath);
    }


    private void initDB() {

        File outfile = null;
        try {
            File folder = new File(Constants.LOCAL_PATH);

            folder.mkdirs();

            File[] fileList = folder.listFiles();

            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].getName().contains(".sqlite")) {

                    fileList[i].delete();
                }
            }


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

            byte[] tempdata = new byte[(int) filesize];
            is.read(tempdata);
            is.close();
            outfile.createNewFile();

            FileOutputStream fo = new FileOutputStream(outfile);
            fo.write(tempdata);
            fo.close();
            SharedPreferences.Editor ed = mPref.edit();
            ed.putInt(Constants.PREF_DB_VERSION, Constants.DBVERSION);
            ed.putString(Constants.PREF_DB_NAME, Constants.PREF_DEFAULT_DB_NAME);
            ed.commit();

        } catch (Exception e) {
        }
    }

    private void imgDownLoad() {

        try {
            String bannerStr = mNotiItem.strEventBanner;
            if (bannerStr.length() == 0)
                return;
            String[] split = bannerStr.split("\\|");
            Boolean loadingFlag = true;

            for (int i = 0; i < split.length; i += 2) {

                String imagename = split[i];
                String imgurl = "http://115.68.14.18/BusInfor/app/img/" + imagename + ".jpeg";


                if (!ImageStorage.checkifImageExists(imagename)) {
                    loadingFlag = false;
                    new GetImages(imgurl, imagename).execute();
                }

            }

            if (loadingFlag) {

                startIntentMainActivity();

            } else {

                mRunnable = new Runnable() {
                    @Override
                    public void run() {

                        imgDownLoad();
                        finish();

                    }
                };

                mHandler.postDelayed(mRunnable, 2000);
            }
        } catch (Exception e) {
            mNotiItem.strEventBanner = "";
            ed = mPref.edit();
            ed.putString(Constants.PREF_EVENT_NAME, "");
            ed.commit();
            startIntentMainActivity();
        }


    }

    private class GetImages extends AsyncTask<Object, Object, Object> {
        private String requestUrl, imagename_;
        private ImageView view;
        private Bitmap bitmap;
        private FileOutputStream fos;

        private GetImages(String requestUrl, String _imagename_) {
            this.requestUrl = requestUrl;
            this.imagename_ = _imagename_;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            try {
                URL url = new URL(requestUrl);
                URLConnection conn = url.openConnection();
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());
            } catch (Exception ex) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                ImageStorage.saveToSdCard(bitmap, imagename_);
            } catch (Exception e) {
            }
        }
    }

    class GetInforData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                int versionCode = getPackageManager().getPackageInfo(getApplicationInfo().packageName, 0).versionCode;
                String url = String.format(Constants.LOCAL_URL + Constants.PATH_NOTI_DB_VERSION + "?appV=%s&type=android", versionCode);

                String result = RequestCommonFuction.getSource(url, false, "", "utf-8");

                JSONObject object;

                object = new JSONObject(result);

                int appV = object.getInt("appV");
                int dbV = object.getInt("dbV");
                String fileDownPath = object.getString("fileRoute");
                boolean isDownNeed = object.getBoolean("downNeed");
                String strEventBanner = object.getString("eventBanner");

                mNotiItem.dbVersion = dbV;
                mNotiItem.fileName = fileDownPath;
                mNotiItem.isDownNeed = isDownNeed;
                mNotiItem.strEventBanner = strEventBanner;


                ed.putString(Constants.PREF_EVENT_NAME, strEventBanner);
                ed.commit();

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

                final SBDialog dialog = new SBDialog(SBDownloadActivity.this);

                dialog.setViewLayout("마켓에 새로운 버젼이 출시되었습니다. 업데이트 하시겠습니까?");
                dialog.getPositiveButton("업데이트").setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                        marketLaunch.setData(Uri.parse("market://search?q=teamdoppelganger.smarterbus"));
                        startActivity(marketLaunch);
                        finish();
                    }
                });

                dialog.getNegativeButton("취소").setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        useLocalDb();
                    }
                });

                dialog.show();
            }

            else if (mPref.getInt(Constants.PREF_DB_VERSION, 0) < mNotiItem.dbVersion) {

                showDbDownloadDialog();

            } else {
                if (mNotiItem.dbVersion == 0 || Constants.DBVERSION == mNotiItem.dbVersion) {

                    useLocalDb();

                }

            }
        }
    }
}
