package teamdoppelganger.smarterbus;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthResponse;
/// 네이버 아이디로 로그인 샘플앱

/**
 * <br/> OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제,
 * <br/> 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 *
 * @author naver
 */
public class NaverLogin extends Activity {

    //naver
    private static String OAUTH_CLIENT_ID = "m_aL8ExzSUSUt1slNPkM";
    private static String OAUTH_CLIENT_SECRET = "qEOsgli16Z";
    private static String OAUTH_CLIENT_NAME = "전국 스마트 버스";
    private static String OAUTH_CALLBACK_URL = "teamdopelganger.smarterbus.action.OAUTH_LOGIN";

    private OAuthLogin mOAuthLoginInstance;
    private Context mContext;

    private TextView mHashedId;


    boolean mIsHashedId;

    SharedPreferences mPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.naver_login);

        mContext = this;
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        initData();
        initView();

        this.setTitle("OAuthLoginSample Ver." + OAuthLogin.getVersion());
    }

    private void initData() {
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_CALLBACK_URL);
    }

    private void initView() {

        mHashedId = (TextView) findViewById(R.id.account_hashed_id);

        Button logoutBtn = (Button) findViewById(R.id.naver_logout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteTokenTask().execute();
            }
        });

        mOAuthLoginInstance.startOauthLoginActivity(NaverLogin.this, mOAuthLoginHandler);


    }

    @Override
    protected void onResume() {

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();

    }

    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {

            if (success) {

                Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
                new RequestApiTask().execute();

            } else {

                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
                finish();

            }
        }

        ;
    };

    private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            OAuthResponse res = mOAuthLoginInstance.logoutAndDeleteToken(mContext);

            if (res != null && res.getResultValue().equals("success")) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putString(Constants.PREF_LOGIN, Constants.LOGIN_EMPTY);
                editor.commit();
            }

            return null;

        }

        protected void onPostExecute(Void v) {
            finish();
        }

    }

    private class RequestApiTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String url = "https://apis.naver.com/nidlogin/nid/getHashId_v2.xml";
            String at = mOAuthLoginInstance.getAccessToken(mContext);
            return mOAuthLoginInstance.requestApi(mContext, at, url);
        }

        protected void onPostExecute(String content) {
            readXML((String) content);
        }

    }

    private class RefreshTokenTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return mOAuthLoginInstance.refreshAccessToken(mContext);
        }

        protected void onPostExecute(String res) {
//			updateView();
        }

    }

    private void readXML(String xml) {

        try {

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            SaxHandler handler = new SaxHandler();
            reader.setContentHandler(handler);
            InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));
            reader.parse(new InputSource(is));

        } catch (UnsupportedEncodingException e) {
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }

    }

    private class SaxHandler extends DefaultHandler {

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (localName.equals("enc_id"))
                mIsHashedId = true;
            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            String characters = new String(ch, start, length);

            if (mIsHashedId) {

                mHashedId.setText(characters);
                mIsHashedId = false;
                SharedPreferences.Editor editor = mPref.edit();
                editor.putString(Constants.PREF_LOGIN, Constants.LOGIN_NAVER);
                editor.commit();
                SendData sendData = new SendData();
                sendData.setAuthId(characters);
                sendData.execute("");

            }

            super.characters(ch, start, length);

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

        }

        @Override
        public void endDocument() throws SAXException {

            super.endDocument();

        }

    }

    class SendData extends AsyncTask<String, String, Void> {

        String _authId;

        void setAuthId(String authId) {
            _authId = authId;
        }

        @Override
        protected Void doInBackground(String... params) {

            String url = "http://115.68.14.18/BusInfor/app/insertJoin.php";

            DefaultHttpClient client = new DefaultHttpClient();

            try {

                HttpPost post = new HttpPost(url);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("deviceId", Build.SERIAL));
                nameValuePairs.add(new BasicNameValuePair("email", ""));
                nameValuePairs.add(new BasicNameValuePair("password", ""));
                nameValuePairs.add(new BasicNameValuePair("authId", _authId));
                nameValuePairs.add(new BasicNameValuePair("how", "2"));

                post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

                HttpParams httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
                HttpConnectionParams.setSoTimeout(httpParams, 6000);

                HttpResponse response = client.execute(post);

                BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));

                String line = null;
                String result = "";
                StringBuilder sb = new StringBuilder();//추가

                while ((line = bufreader.readLine()) != null) {
                    sb.append(line.trim());
                }
                result = sb.toString();
                bufreader.close();


            } catch (Exception e) {
            }

            return null;
        }

    }

    private String SendByHttp(String authId) {

        String url = "http://115.68.14.18/BusInfor/app/insertJoin.php";

        DefaultHttpClient client = new DefaultHttpClient();

        try {

            HttpPost post = new HttpPost(url);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("deviceId", Build.SERIAL));
            nameValuePairs.add(new BasicNameValuePair("authId", authId));
            nameValuePairs.add(new BasicNameValuePair("how", "2"));


            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 6000);
            HttpConnectionParams.setSoTimeout(params, 6000);

            HttpResponse response = client.execute(post);

            BufferedReader bufreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));

            String line = null;
            String result = "";
            StringBuilder sb = new StringBuilder();//추가

            while ((line = bufreader.readLine()) != null) {
                sb.append(line.trim());
            }
            result = sb.toString();
            bufreader.close();//추

            return result;

        } catch (Exception e) {
        }

        return null;

    }

}