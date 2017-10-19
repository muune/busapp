package teamdoppelganger.smarterbus.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

import teamdoppelganger.smarterbus.util.common.Debug;

public class KeyboardCheckLayout extends RelativeLayout {

    private boolean isKeyboardShown;


    public KeyboardCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    private OnSoftKeyboardListener onSoftKeyboardListener;

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
        final int actualHeight = getHeight();

        if (actualHeight > proposedheight) {
            isKeyboardShown = true;
            onSoftKeyboardListener.onShown();
        } else {

        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (isKeyboardShown) {
                isKeyboardShown = false;
                onSoftKeyboardListener.onHidden();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

    public final void setOnSoftKeyboardListener(
            final OnSoftKeyboardListener listener) {
        this.onSoftKeyboardListener = listener;
    }

    public interface OnSoftKeyboardListener {

        public void onShown();

        public void onHidden();

    }
}