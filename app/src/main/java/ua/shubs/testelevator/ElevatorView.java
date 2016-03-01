package ua.shubs.testelevator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

/**
 * Created by Vitaliy on 01/03/2016.
 */
public class ElevatorView extends View {

    public static final int FLOOR_COUNT = 7;

    private Paint mFloorPaint;
    private Paint mTextPaint;
    private int mFloorColor;
    private int mTextColor;
    private float mTextPadding;
    private float mTextSize;
    private float mFloorBackSidePadding;

    private float mFloorHeight;
    private float mCenterHeight;

    private OverScroller mScroller;
    private float mScrollPosition = 0.0f;
    private GestureDetectorCompat mGestureDetector;

    public ElevatorView(final Context _context, AttributeSet _attributeSet) {
        super(_context, _attributeSet);
        initProps(_attributeSet);
        initPaints();
        mGestureDetector = new GestureDetectorCompat(_context, mGestureListener);
        mScroller = new OverScroller(_context);
    }

    private void initProps(final AttributeSet _attributeSet) {
        final TypedArray typedArray = getContext().getTheme()
                .obtainStyledAttributes(_attributeSet, R.styleable.ElevatorView, 0, 0);
        try {
            mFloorColor = typedArray.getColor(R.styleable.ElevatorView_floorColor, Color.GRAY);
            mTextColor = typedArray.getColor(R.styleable.ElevatorView_floorTextColor, Color.GRAY);
            mTextPadding = typedArray.getDimensionPixelOffset(R.styleable.ElevatorView_floorTextPadding, 5);
            mFloorBackSidePadding = typedArray.getDimensionPixelOffset(R.styleable.ElevatorView_floorBackPadding, 70);
            mTextSize = typedArray.getDimensionPixelOffset(R.styleable.ElevatorView_floorTextSize, 50);
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    private void initPaints() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        mFloorPaint = new Paint();
        mFloorPaint.setAntiAlias(true);
        mFloorPaint.setStyle(Paint.Style.FILL);
        mFloorPaint.setColor(mFloorColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mFloorHeight = h / FLOOR_COUNT;
        mCenterHeight = h / 2;
        if (mScrollPosition == 0.0f) {
            mScrollPosition = getTop();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Path floorsPath = new Path();

        for (int i = 0; i <= FLOOR_COUNT; i++) {
            final float y = mScrollPosition + mFloorHeight * i;
            final float backSideHeight = (mCenterHeight - y) / 15;
            floorsPath.moveTo(getLeft(), y);
            floorsPath.lineTo(getRight(), y);
            floorsPath.lineTo(getRight() - mFloorBackSidePadding, y + backSideHeight);
            floorsPath.lineTo(getLeft() + mFloorBackSidePadding, y + backSideHeight);

            canvas.drawText(String.valueOf(FLOOR_COUNT - (i + 1)), getLeft() + mTextPadding, y + mFloorHeight / 2 + mTextSize / 3, mTextPaint);
        }

        canvas.drawPath(floorsPath, mFloorPaint);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retVal = mGestureDetector.onTouchEvent(event);
        return retVal || super.onTouchEvent(event);
    }

    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(ElevatorView.this);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            mScroller.startScroll(0, mScroller.getCurrY(), 0, (int) distanceY);
            mScrollPosition -= distanceY;
            ViewCompat.postInvalidateOnAnimation(ElevatorView.this);
            return true;
        }
    };
}
