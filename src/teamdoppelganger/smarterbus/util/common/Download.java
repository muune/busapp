package teamdoppelganger.smarterbus.util.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import teamdoppelganger.smarterbus.common.Constants;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;


public class Download {

    Context mContext;
    String mTopicText;

    ProgressDialog mProgressDialog;

    PDownloadListener mDownloadListener;

    public interface PDownloadListener {
        public void downLoadFinish(int result);
    }

    public Download(Context context) {
        mContext = context;

    }

    public void setDownloadListener(PDownloadListener l) {
        mDownloadListener = l;
    }

    public void startDownload(String... url) {

        new DownloadFileAsync().execute(url);

    }

    private void DownLoadDialog(Context context, int type) {

        if (mProgressDialog != null) {
            mProgressDialog.cancel();
        }

        mProgressDialog = new ProgressDialog(context);
        if (type == 1) {
            mProgressDialog.setMessage(" 파일 다운로드 중");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } else {
            mProgressDialog.setMessage("파일 압축 해제중");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressDialog.setCancelable(false);

    }

    private String makeFileName(String url) {
        String[] fileSplit = url.split("/");
        return fileSplit[fileSplit.length - 1];
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        ArrayList<String> fileNameArray = new ArrayList<String>();
        String _deleteFileName;
        boolean isError;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isError = false;
            DownLoadDialog(mContext, 1);
            mProgressDialog.show();

        }

        @Override
        protected String doInBackground(String... aurl) {
            fileNameArray.clear();

            int count;


            for (int i = 0; i < aurl.length; i++) {
                try {

                    String downloadUrl = aurl[i];

                    URL url = new URL(downloadUrl);
                    URLConnection conexion = url.openConnection();
                    conexion.connect();

                    int lenghtOfFile = conexion.getContentLength();

                    InputStream input = new BufferedInputStream(
                            url.openStream());
                    String fileName = makeFileName(aurl[i]);


                    if (fileName.contains("/")) {
                        fileName = fileName.split("/")[fileName.split("/").length - 1];
                    }

                    _deleteFileName = fileName;

                    OutputStream output = null;


                    File fileDir = new File(Constants.DOWNLOAD_PATH);
                    if (!fileDir.isDirectory()) {
                        fileDir.mkdirs();
                    }

                    output = new FileOutputStream(
                            Constants.DOWNLOAD_PATH + File.separator
                                    + fileName);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress(""
                                + (int) ((total * 100) / lenghtOfFile));
                        output.write(data, 0, count);
                    }

                    fileNameArray.add(fileName);
                    output.flush();
                    output.close();
                    input.close();

                } catch (Exception e) {
                    isError = true;
                    deleteFile(_deleteFileName);
                    if (mDownloadListener != null) {
                        mDownloadListener.downLoadFinish(Constants.RETURN_FAIL);
                    }
                    e.printStackTrace();
                }

            }

            return null;

        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
            if (isError)
                return;

            if (fileNameArray.get(0).contains(".zip")) {
                new UnZipFileSync().execute(fileNameArray.get(0));
            } else {
                if (mDownloadListener != null) {
                    mDownloadListener
                            .downLoadFinish(Constants.RETURN_SUCCESS);
                }
            }

        }
    }

    class UnZipFileSync extends AsyncTask<String, String, String> {

        ArrayList<String> fileNameArray = new ArrayList<String>();

        private int per = 0;
        String fileName;
        boolean isError;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isError = false;
            DownLoadDialog(mContext, 2);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... aurl) {

            fileNameArray.clear();

            fileName = aurl[0];

            for (int i = 0; i < aurl.length; i++) {
                try {

                    ZipFile zip = new ZipFile(Constants.DOWNLOAD_PATH
                            + File.separator + aurl[i]);
                    mProgressDialog.setMax(zip.size());
                    FileInputStream fin = new FileInputStream(
                            Constants.DOWNLOAD_PATH + File.separator
                                    + aurl[i]);
                    ZipInputStream zin = new ZipInputStream(fin);
                    ZipEntry ze = null;

                    File fileDir = new File(Constants.DOWNLOAD_PATH);
                    if (!fileDir.isDirectory()) {
                        fileDir.mkdirs();
                    }

                    while ((ze = zin.getNextEntry()) != null) {
                        per++;

                        FileOutputStream fout = new FileOutputStream(
                                Constants.DOWNLOAD_PATH
                                        + File.separator + ze.getName());

                        long total = 0;

                        byte[] buffer = new byte[4096];
                        int len = 0;
                        while ((len = zin.read(buffer)) != -1) {

                            total = total + len;
                            publishProgress(""
                                    + (int) ((total * 100) / zip.size()));
                            fout.write(buffer, 0, len);
                        }
                        fout.close();
                    }

                    zin.close();

                } catch (Exception e) {

                    e.printStackTrace();

                    isError = true;

                    SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    String previousFileName = mPref.getString(Constants.PREF_DB_NAME, "empty");
                    if (!previousFileName.equals(fileName.split("\\.")[0] + ".sqlite")) {
                        deleteFile(fileName.split("\\.")[0] + ".sqlite");
                    }


                    if (mDownloadListener != null) {
                        mDownloadListener.downLoadFinish(Constants.RETURN_FAIL);
                    }
                    // TODO: handle exception
                } finally {
                    mProgressDialog.dismiss();
                }
            }

            return null;

        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {

            mProgressDialog.dismiss();

            deleteFile(fileName);

            if (!isError && mDownloadListener != null) {

                SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                String previousFileName = mPref.getString(Constants.PREF_DB_NAME, "empty");

                if (!previousFileName.equals(fileName.split("\\.")[0] + ".sqlite")) {
                    deleteFile(previousFileName);
                }


                mDownloadListener.downLoadFinish(Constants.RETURN_SUCCESS);
            }

        }
    }

    private void deleteFile(String fileName) {

        File file = new File(Constants.DOWNLOAD_PATH + File.separator + fileName);
        if (file.exists())
            file.delete();

    }

    public void setTopicText(String topic) {
        mTopicText = topic;
    }

}
