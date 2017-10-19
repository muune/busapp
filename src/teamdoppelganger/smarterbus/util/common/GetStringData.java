package teamdoppelganger.smarterbus.util.common;

import android.os.AsyncTask;

public class GetStringData extends AsyncTask<String, String, String> {

    String mResult = null;

    GetStringDataListenr mGetStringDataListener;

    public interface GetStringDataListenr {
        public void onGetStringDataResult(String resultStr);
    }


    public void setGetStringDataListener(GetStringDataListenr l) {
        mGetStringDataListener = l;
    }


    @Override
    protected String doInBackground(String... params) {

        String urlString = params[0];
        boolean isPost = Boolean.parseBoolean(params[1]);
        String valuePair = params[2];
        String encodingStyle = params[3];

        if (!isCancelled()) {
            mResult = RequestCommonFuction.getSource(urlString, isPost, valuePair, encodingStyle);
        }


        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (mResult == null || mGetStringDataListener == null) return;
        if (!isCancelled()) {
            mGetStringDataListener.onGetStringDataResult(mResult);
        }

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }


}
