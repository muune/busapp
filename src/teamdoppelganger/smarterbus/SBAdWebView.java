package teamdoppelganger.smarterbus;


import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;
import teamdoppelganger.smarterbus.util.common.Debug;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.mocoplex.adlib.AdlibManager;


public class SBAdWebView extends SBBaseActivity {

    String mParam;
    String mUrl;

    boolean mIsPageFinish = false;
    WebView mWebview;

    Context context;
    AdlibManager mAdlibManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.adweb);
        TextView title = (TextView) findViewById(R.id.webview_title);
        title.setHeight((int) (25 * getResources().getDisplayMetrics().density));

        Tracker t = ((SBInforApplication) getApplication()).getTracker(
                SBInforApplication.TrackerName.APP_TRACKER);
        t.enableAdvertisingIdCollection(true);
        t.setScreenName(getString(R.string.analytics_screen_adwebview));
        t.send(new HitBuilders.AppViewBuilder().build());

        LinearLayout launch = (LinearLayout) findViewById(R.id.right_view);
        launch.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        mParam = getIntent().getExtras().getString("param");

        context = getApplicationContext();

        initView();
        initData();

        setupView();

    }

    private void initView() {

        mWebview = (WebView) (findViewById(R.id.webview));

    }

    // coupon type 에 따라 url 주소 변경
    private void initData() {

        mUrl = getIntent().getExtras().getString(
                Constants.INTENT_URL);

        TextView title = (TextView) findViewById(R.id.webview_title);

    }

    // 연동 화면에 따라서 파라미터가 다름
    private void setupView() {

        mWebview.getSettings().setDomStorageEnabled(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.setVerticalScrollBarEnabled(false);
        mWebview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        mWebview.loadUrl(mUrl);


        mWebview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        mWebview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri
                            .parse(url));
                    startActivity(intent);
                } else if (url != null && (url.startsWith("https://play.google.com"))) {
                    try {
                        String tempURL = url.split("id=")[1];
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=" + tempURL));
                        startActivity(marketIntent);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    mIsPageFinish = false;
                    view.loadUrl(url);
                } else if (url != null && url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (url != null && (url.startsWith("market://") || url.startsWith("intent:kakaolink:"))) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
                        if (existPackage != null) {
                            startActivity(intent);
                        } else {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + intent.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }

            //앱 처음 접속 시 접속한다는 메시지 띄울 필요
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!mIsPageFinish) {
                    mIsPageFinish = true;
                }

            }


            //coupon error 날 시 처리 멘트
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

            }

        });

        mWebview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

        });

        mAdlibManager = new AdlibManager(Constants.ADLIB_WEBVIEW_API_KEY);
        mAdlibManager.onCreate(this);
        mAdlibManager.setBannerFailDelayTime(5);
        mAdlibManager.setAdsHandler(new Handler() {
            public void handleMessage(Message message) {
                try {
                    switch (message.what) {
                        case AdlibManager.DID_SUCCEED:
                            if (findViewById(R.id.adlib).getVisibility() == View.GONE) {
                                findViewById(R.id.adlib).setVisibility(View.VISIBLE);
                                Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up);
                                findViewById(R.id.admixer_layout).startAnimation(slide_up);
                            }
                            break;
                        case AdlibManager.DID_ERROR:
                            break;
                    }
                } catch (Exception e) {

                }
            }
        });
        mAdlibManager.setAdsContainer(R.id.adlib);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            //전에 히스토리를 다 지움
            mWebview.goBack();
            return true;

        } else {

            if (mWebview != null) {
                mWebview.stopLoading();
            }

        }

        return super.onKeyDown(keyCode, event);

    }

    @Override
    public void onResume() {

        super.onResume();
        mAdlibManager.onResume(this);

    }

    @Override
    public void onPause() {

        super.onPause();
        mAdlibManager.onPause(this);

    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mAdlibManager.onDestroy(this);

    }

}

