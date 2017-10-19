package teamdoppelganger.smarterbus.util.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import java.util.logging.Logger;

import teamdoppelganger.smarterbus.common.SBInforApplication;

/**
 * Created by Administrator on 2017-07-31.
 */

public class GoogleAppIdTask extends AsyncTask<Void, Void, String> {

    private Context context;


    public GoogleAppIdTask(Context context) { this.context = context; }

    protected String doInBackground(final Void... params) {
        String adId = null;
        try {
            adId = AdvertisingIdClient.getAdvertisingIdInfo(context).getId();
            ((SBInforApplication) context.getApplicationContext()).setADID(adId);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        } catch (GooglePlayServicesRepairableException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException ex) {
            ex.printStackTrace();
        }
        return adId;
    }

    protected void onPostExecute(String adId) {
        //TODO::Ad ID를 이용한 작업 수행
        super.onPostExecute(adId);
    }
}


