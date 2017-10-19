package teamdoppelganger.smarterbus;

import teamdoppelganger.smarterbus.common.SBBaseActivity;
import teamdoppelganger.smarterbus.common.SBInforApplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * 인트로 클래스
 *
 * @author DOPPELSOFT4
 */
public class SBIntroActivity extends SBBaseActivity {


    Handler mHandler;
    Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro);
        mHandler = new Handler();


    }


}
