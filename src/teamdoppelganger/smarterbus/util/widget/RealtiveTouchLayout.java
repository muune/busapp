package teamdoppelganger.smarterbus.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class RealtiveTouchLayout extends RelativeLayout {

    boolean mIsTouchAction = false;

    public RealtiveTouchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {


        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mIsTouchAction = true;
        }


        return super.dispatchTouchEvent(ev);
    }

    public boolean getTouchAction() {
        return mIsTouchAction;
    }

    public void resetTouchAction() {
        mIsTouchAction = false;
    }

}
