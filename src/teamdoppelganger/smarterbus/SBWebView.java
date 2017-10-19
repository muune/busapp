package teamdoppelganger.smarterbus;

import java.util.ArrayList;

import org.apache.http.util.EncodingUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.util.common.Debug;
import teamdoppelganger.smarterbus.util.common.RequestCommonFuction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

public class SBWebView extends SBBaseActivity{
		
	
	WebView mWebView;
	ListView mListView;
	
	String mUrlString;
	
	String mUrlParam="";
	String mUrlSendType ="get";

	
	String mMode;
	
	GetHtmlData mGetHtmlData;
	
	ArrayList<TempItem> mTempItemAry;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.web);
		
		mUrlString = getIntent().getExtras().getString(Constants.INTENT_URL);
		
		//전달 값이 없을 수도 있음
		try{
			mUrlParam = getIntent().getExtras().getString(Constants.INTENT_URL_PARAM);
			mUrlSendType = getIntent().getExtras().getString(Constants.INTENT_URL_SNED_TYPE);
		}catch(Exception e){
			mUrlParam = "";
			mUrlSendType ="get";
			
		};
		
		mWebView = (WebView)findViewById(R.id.webView);
		mListView = (ListView)findViewById(R.id.timeTable);
		
		mGetHtmlData = new GetHtmlData();
		
		mTempItemAry = new ArrayList<TempItem>();
		
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setPluginState(PluginState.ON);
		
		mWebView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				
				//return super.shouldOverrideUrlLoading(view, url);
				return false;
			}
			
			
		});
		
		mWebView.setWebChromeClient(new WebChromeClient(){
			
			
		});
		

        if(mUrlString.contains(".pdf") || mUrlString.contains(".xls")){

			mWebView.loadUrl("http://docs.google.com/gview?embedded=true&url=" + mUrlString);

		}else{
			
			if(mUrlParam == null || mUrlParam.equals("")){
				mWebView.loadUrl(mUrlString);
			}else{
				
				if(mUrlSendType.equals("get")){
					mWebView.loadUrl(mUrlString + "?" + mUrlParam);
				}else{
					mWebView.postUrl(mUrlString,mUrlParam.getBytes());
				}
			}
		}
	}
	
	
	class GetHtmlData extends AsyncTask<String, String, String>{

		@Override
		protected String doInBackground(String... params) {			
			
			String url = params[0];
			
			
			String body = RequestCommonFuction.getSource(url, false, "", "euc-kr");
			
			Document doc = Jsoup.parse(body);
			if(mMode == "busanBIMS"){
				Elements ddTag = doc.select("bus");	
				
				for(int i=0;i<ddTag.size();i++){
					String start  = ddTag.get(i).select("text1").text();
					String startTime  = ddTag.get(i).select("text2").text();
					String endTime = ddTag.get(i).select("text3").text();
					
					TempItem tempItem = new TempItem();
					tempItem.start = start;
					tempItem.startTime = startTime;
					tempItem.endTime = endTime;					
					mTempItemAry.add(tempItem);
				//ddTag.get(i).select(cssQuery)
				}
			}else if(mMode == "cheonan"){
				Elements ddTag = doc.select("bus");	
				
				for(int i=0;i<ddTag.size();i++){
					String start  = ddTag.get(i).select("text1").text();
					String startTime  = ddTag.get(i).select("text2").text();
					String endTime = ddTag.get(i).select("text3").text();
					
					TempItem tempItem = new TempItem();
					tempItem.start = start;
					tempItem.startTime = startTime;
					tempItem.endTime = endTime;					
					mTempItemAry.add(tempItem);
				//ddTag.get(i).select(cssQuery)
				}
			}
			
			return null;

		}
		
	}
	
	class TempItem {
		String start;
		String startTime;
		String endTime;
	}
	
	@Override
	public void onBackPressed()
	{
	    if(mWebView.canGoBack())
	    	mWebView.goBack();
	    else
	        super.onBackPressed();
	}

}