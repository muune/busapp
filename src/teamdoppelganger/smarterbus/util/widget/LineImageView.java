package teamdoppelganger.smarterbus.util.widget;

import org.apache.http.conn.scheme.PlainSocketFactory;

import teamdoppelganger.smarterbus.R;
import teamdoppelganger.smarterbus.common.Constants;
import teamdoppelganger.smarterbus.util.common.Debug;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class LineImageView extends ImageView {

    int mType;
    int mLevel, mPreLevel;
    String mPlainNum;
    String mColor = "000000";
    boolean mIsBus;
    String mSeat = null;


    public LineImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int startX, startY, stopX, stopY = 0;

        startX = (int) (getWidth() / 2 - getResources().getDimension(R.dimen.line_width) / 2) + (int) (getResources().getDimension(R.dimen.line_left_margin));
        stopX = (int) (getWidth() / 2 + getResources().getDimension(R.dimen.line_width) / 2) + (int) (getResources().getDimension(R.dimen.line_left_margin));

        if (mType == Constants.LINE_END) {
            startY = 0;
            stopY = getHeight() / 2;
        } else if (mType == Constants.LINE_START) {
            startY = getHeight() / 2;
            stopY = getHeight();
        } else {
            startY = 0;
            stopY = getHeight();
        }


        // -2라는 수치는 리스트 뷰의 라인을 덮기 위한 수치이다.
        Rect lineRect = new Rect(startX, startY - 2, stopX, stopY + 2);


        //라인 그리기(안쪽)
        Paint paint = new Paint();
        Paint linePaint = new Paint();
        int colorId;
        int lineColorId;
        int arrowId;

        if (mColor.equals("000000")) {
            colorId = R.color.bus_detail_in_color;
            lineColorId = R.color.bus_detail_border_color;
            arrowId = R.drawable.bus_line_arrow;
        } else if (mColor.equals("0000FF")) {
            colorId = R.color.color_1;
            lineColorId = R.color.color_sub_1;
            arrowId = R.drawable.color_0000ff;

        } else if (mColor.equals("00B050")) {
            colorId = R.color.color_2;
            lineColorId = R.color.color_sub_2;
            arrowId = R.drawable.color_00b050;

        } else if (mColor.equals("996600")) {

            colorId = R.color.color_3;
            lineColorId = R.color.color_sub_3;
            arrowId = R.drawable.color_996600;
        } else if (mColor.equals("FF0000")) {

            colorId = R.color.color_4;
            lineColorId = R.color.color_sub_4;
            arrowId = R.drawable.color_ff0000;

        } else if (mColor.equals("FF00FF")) {

            colorId = R.color.color_5;
            lineColorId = R.color.color_sub_5;
            arrowId = R.drawable.color_ff00ff;

        } else if (mColor.equals("FF6600")) {

            colorId = R.color.color_6;
            lineColorId = R.color.color_sub_6;
            arrowId = R.drawable.color_ff6600;

        } else if (mColor.equals("FFCC00")) {

            colorId = R.color.color_7;
            lineColorId = R.color.color_sub_7;
            arrowId = R.drawable.color_ffcc00;

        } else {
            colorId = R.color.bus_detail_in_color;
            lineColorId = R.color.bus_detail_border_color;
            arrowId = R.drawable.bus_line_arrow;
        }

        paint.setColor(getResources().getColor(colorId));//c4ccd2
        paint.setStyle(Paint.Style.FILL);

        if (mPreLevel == 1 && mIsPlainName) {
            lineRect.top = stopY / 2 - stopY / 2 / 2;
            canvas.drawRect(lineRect, paint);
        } else {
            canvas.drawRect(lineRect, paint);
        }


        //라인그리기(테두리)

        if (mPreLevel == 1 && mIsPlainName) {

            linePaint.setColor(getResources().getColor(lineColorId));
            canvas.drawLine(startX, stopY / 2 - stopY / 2 / 2, startX, stopY + 2, linePaint);
            canvas.drawLine(stopX, stopY / 2 - stopY / 2 / 2, stopX, stopY + 2, linePaint);

        } else {
            linePaint.setColor(getResources().getColor(lineColorId));
            canvas.drawLine(startX, startY - 2, startX, stopY + 2, linePaint);
            canvas.drawLine(stopX, startY - 2, stopX, stopY + 2, linePaint);
        }



        //화살표 이미지
        BitmapDrawable arrowdrawable = (BitmapDrawable) getResources().getDrawable(arrowId);
        Bitmap arrowBitmap = arrowdrawable.getBitmap();
        int arrowleft = (int) ((stopX - getResources().getDimension(R.dimen.line_width) / 2)) - arrowBitmap.getWidth() / 2;
        int arrowheight = getHeight() / 2 - arrowBitmap.getHeight() / 2;

        canvas.drawBitmap(arrowBitmap, arrowleft, arrowheight, null);


        if (mIsBus) {
            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.bus_ic);
            Bitmap bitmap = drawable.getBitmap();
            int left = (int) (getResources().getDimension(R.dimen.bus_left_margin));
            int height;


            if (mType == Constants.LINE_END) {
                height = getHeight() / 2 - bitmap.getHeight() / 2;
            } else if (mType == Constants.LINE_START) {
                height = getHeight() / 2 - bitmap.getHeight() / 2;
            } else {
                height = getHeight() / mLevel - bitmap.getHeight() / 2;
            }


            canvas.drawBitmap(bitmap, left, height, null);

            if (mPlainNum != null && mPlainNum.length() > 0) {

                String[] contents = mPlainNum.split(" ");

                int fontSize = (int) getResources().getDimension(R.dimen.ballon_font_size);  //28;
                int margin = (int) getResources().getDimension(R.dimen.ballon_margin);    //10;
                int leftBollon = (int) (getResources().getDimension(R.dimen.bus_left_margin)) + bitmap.getWidth() + margin;
                int spareSize = (int) (getResources().getDimension(R.dimen.ballon_spare_size));
                int spareSize2 = 15;//(int) (getResources().getDimension(R.dimen.ballon_spare_size2)) ;
                int topMargin = (int) (getResources().getDimension(R.dimen.txt_top_margin));

                Paint fontPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
                fontPaint.setAntiAlias(true);
                fontPaint.setColor(getResources().getColor(R.color.bus_detail_text_color));
                fontPaint.setTextSize(fontSize);

                NinePatchDrawable bollonDrawable = (NinePatchDrawable) getResources().getDrawable(R.drawable.bus_text_bg);
                int fontWidth = 0;
                Rect npdBounds;


                if (contents != null && contents.length > 1) {
                    fontWidth = (int) fontPaint.measureText("ddddddd");

                    mPlainNum = "";

                    int oldHeight = 0;
                    npdBounds = new Rect(leftBollon, height - (contents.length - 1) * fontSize / 2, leftBollon + fontWidth + margin * 2, height + fontSize + spareSize + (contents.length - 1) * fontSize / 2);
                    bollonDrawable.setBounds(npdBounds);
                    bollonDrawable.draw(canvas);


                    for (int i = 0; i < contents.length; i++) {

                        contents[i] = contents[i].replace("(저상)", "");

                        if (mPlainNum.equals("")) {
                            mPlainNum = contents[i];
                        } else {

                            String tmp;
                            if (contents[1].contains(";")) {
                                tmp = contents[1].split(";")[0];
                                mSeat = contents[1].split(";")[1];
                                mPlainNum = mPlainNum + "\n" + tmp;
                            } else {
                                mPlainNum = mPlainNum + "\n" + contents[i];
                            }

                        }

                        if (i == 0) {
                            oldHeight = height + bitmap.getHeight() / 2 + (fontSize * (i)) / 2 + i * (spareSize * 2) - topMargin;
                            canvas.drawText(contents[i].split(";")[0], leftBollon + margin, oldHeight, fontPaint);

                            if (mSeat != null) {
                                canvas.drawText(mSeat + "석", left, height - 4, fontPaint);
                            } else {

                            }
                        } else {
                            canvas.drawText(contents[i].split(";")[0], leftBollon + margin, oldHeight + fontPaint.getTextSize(), fontPaint);
                            if (mSeat != null) {
                                canvas.drawText(mSeat + "석", left, height - 4, fontPaint);

                            }
                        }
                    }

                } else {
                    npdBounds = new Rect(leftBollon, height, leftBollon + fontWidth + margin * 6, height + fontSize + 20);
                    bollonDrawable.setBounds(npdBounds);
                    bollonDrawable.draw(canvas);
                    canvas.drawText(mPlainNum, leftBollon + margin, height + bitmap.getHeight() / 2 + fontSize / 2, fontPaint);
                }


            }


        }
    }

    public void setType(int type) {
        mType = type;
    }

    boolean mIsPlainName = false;

    public void setBus(boolean isBus, int level, String text, String color, int preLevel, boolean isPlainNum) {
        mIsBus = isBus;
        mLevel = level;
        mPreLevel = preLevel;
        mIsPlainName = isPlainNum;
        mPlainNum = text;
        mColor = color;
        mSeat = null;
        invalidate();
        requestLayout();
    }

}
