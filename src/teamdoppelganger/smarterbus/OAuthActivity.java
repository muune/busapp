package teamdoppelganger.smarterbus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthResponse;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
/// 네이버 아이디로 로그인 샘플앱

/**
 * <br/> OAuth2.0 인증을 통해 Access Token을 발급받는 예제, 연동해제하는 예제,
 * <br/> 발급된 Token을 활용하여 Get 등의 명령을 수행하는 예제, 네아로 커스터마이징 버튼을 사용하는 예제 등이 포함되어 있다.
 *
 * @author naver
 */
public class OAuthActivity extends Activity {
    /**
     * client 정보를 넣어준다.
     */
    private static String OAUTH_CLIENT_ID = "jyvqXeaVOVmV";
    private static String OAUTH_CLIENT_SECRET = "527300A0_COq1_XV33cf";
    private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인";
    private static String OAUTH_CALLBACK_URL = "http://static.nid.naver.com/oauth/naverOAuthExp.nhn";

    private OAuthLogin mOAuthLoginInstance;
    private Context mContext;

    /**
     * UI 요소들
     */
//	private TextView mApiResultText;
//	private TextView mOauthAT, mOauthRT, mOauthExpires, mOauthTokenType, mOAuthState;
    private TextView mHashedId;
    private OAuthLoginButton mOAuthLoginButton;

    boolean mIsHashedId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts_layout);

        mContext = this;

        initData();
        initView();

        this.setTitle("OAuthLoginSample Ver." + OAuthLogin.getVersion());
    }

    private void initData() {
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME, OAUTH_CALLBACK_URL);
    }

    private void initView() {
//		mApiResultText = (TextView) findViewById(R.id.api_result_text);
//		
//		mOauthAT = (TextView) findViewById(R.id.oauth_access_token);
//		mOauthRT = (TextView) findViewById(R.id.oauth_refresh_token);
//		mOauthExpires = (TextView) findViewById(R.id.oauth_expires);
//		mOauthTokenType = (TextView) findViewById(R.id.oauth_type);
//		mOAuthState = (TextView) findViewById(R.id.oauth_state);

        mHashedId = (TextView) findViewById(R.id.account_hashed_id);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
        mOAuthLoginButton.setBgType("green", "long");

//		updateView();
    }


//	private void updateView() {
//		mOauthAT.setText(mOAuthLoginInstance.getAccessToken(mContext));
//		mOauthRT.setText(mOAuthLoginInstance.getRefreshToken(mContext));
//		mOauthExpires.setText(String.valueOf(mOAuthLoginInstance.getExpiresAt(mContext)));
//		mOauthTokenType.setText(mOAuthLoginInstance.getTokenType(mContext));
//		mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
//	}

    @Override
    protected void onResume() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume();
    }

    /**
     * startOAuthLoginActivity() 호출시 인자로 넘기거나, OAuthLoginButton 에 등록해주면 인증이 종료되는 걸 알 수 있다.
     */
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);

                Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();

                new RequestApiTask().execute();

//				mOauthAT.setText(accessToken);
//				mOauthRT.setText(refreshToken);
//				mOauthExpires.setText(String.valueOf(expiresAt));
//				mOauthTokenType.setText(tokenType);
//				mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }

        ;
    };

//	public void onButtonClick(View v) throws Throwable {
//
//		switch (v.getId()) {
//		case R.id.buttonOAuth: {
//			mOAuthLoginInstance.startOauthLoginActivity(OAuthActivity.this, mOAuthLoginHandler);
//			break;
//		}
//		case R.id.buttonVerifier: {
//			new RequestApiTask().execute();
//			break;
//		}
//		case R.id.buttonRefresh: {
//			new RefreshTokenTask().execute();
//			break;
//		}
//		case R.id.buttonOAuthLogout: {
//			mOAuthLoginInstance.logout(mContext);
//			updateView();
//			break;
//		}
//		case R.id.buttonOAuthDeleteToken: {
//			new DeleteTokenTask().execute();
//			break;
//		}
//		default:
//			break;
//		}
//	}


    private class DeleteTokenTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // 서버에 삭제 요청한 결과를 알고 싶다면 res.getResultValue() 로 확인한다.
            OAuthResponse res = mOAuthLoginInstance.logoutAndDeleteToken(mContext);
            return null;
        }

        protected void onPostExecute(Void v) {
//			updateView();
        }
    }

    private class RequestApiTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
//			mApiResultText.setText((String) "");
        }

        @Override
        protected String doInBackground(Void... params) {
//			String url = "https://apis.naver.com/nidlogin/nid/getUserProfile.xml";
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
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            String characters = new String(ch, start, length);
            if (mIsHashedId) {
                mHashedId.setText(characters);
                mIsHashedId = false;
            }
            super.characters(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }
    }
}