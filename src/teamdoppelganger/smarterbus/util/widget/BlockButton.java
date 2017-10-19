package teamdoppelganger.smarterbus.util.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class BlockButton extends Button {

    public BlockButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        return true;
    }


}
