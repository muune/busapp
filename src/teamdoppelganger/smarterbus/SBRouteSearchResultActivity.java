package teamdoppelganger.smarterbus;


import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.item.SearchItem;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.GeoPoint;
import teamdoppelganger.smarterbus.util.common.GeoTrans;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;

import android.os.AsyncTask;
import android.os.Bundle;

public class SBRouteSearchResultActivity extends SBBaseActivity {


    SearchItem mStartSearchItem, mEndSearchItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStartSearchItem = getIntent().getExtras().getParcelable("startLocation");
        mEndSearchItem = getIntent().getExtras().getParcelable("endLocation");


        new SearchData().execute();
    }


    class SearchData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            GeoPoint in_pt = new GeoPoint(mStartSearchItem.positionX, mStartSearchItem.positionY);//
            GeoPoint end_pt = new GeoPoint(mEndSearchItem.positionX, mEndSearchItem.positionY);//

            GeoPoint utmKS_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, in_pt);
            GeoPoint utmKE_pt = GeoTrans.convert(GeoTrans.GEO, GeoTrans.UTMK, end_pt);

            String param = String.format("searchType=0&SX=%s&SY=%s&EX=%s&EY=%s&resultCount=5&radius=500:1000&sortOpt=0&inType=2&specialOption=undefined,N,0,N",
                    utmKS_pt.getX(), utmKS_pt.getY(), utmKE_pt.getX(), utmKE_pt.getY());

            String htmlResult = RequestCommonFuction.getSource("http://tago.go.kr/transportation/publicRoutingSearchResultWeb_return.service", true, param, "utf-8");

            return null;
        }

    }

}
