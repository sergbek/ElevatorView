package ua.shubs.testelevator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
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

    public static final int FLOOR_COUNT = 50;
    public static final int FLOOR_PER_SCREEN = 7;

    public static final int SHAFT_PADDING_KOEFF = 9;
    public static final int SHAFT_WIDTH_KOEFF = 5;

    private Paint mFloorPaint;
    private Paint mTextPaint;
    private Paint mShaftPaint;
    private int mFloorColor;
    private int mShaftColor;
    private int mTextColor;
    private float mTextPadding;
    private float mTextSize;
    private float mFloorBackSidePadding;
    private float mShaftLineWidth;

    private float mFloorHeight;
    private float mCenterHeight;

    private OverScroller mScroller;
    private float mScrollPosition = 0.0f;
    private float mMaxScrollPosition;
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
            mShaftColor = typedArray.getColor(R.styleable.ElevatorView_shaftColor, Color.WHITE);
            mShaftLineWidth = typedArray.getDimensionPixelOffset(R.styleable.ElevatorView_shaftLineWidth, 3);
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

        mShaftPaint = new Paint();
        mShaftPaint.setAntiAlias(true);
        mShaftPaint.setStyle(Paint.Style.STROKE);
        mShaftPaint.setColor(mShaftColor);
        mShaftPaint.setStrokeWidth(mShaftLineWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mFloorHeight = h / FLOOR_PER_SCREEN;
        mCenterHeight = h / 2;
        mMaxScrollPosition = mFloorHeight * (FLOOR_COUNT - FLOOR_PER_SCREEN);
        if (mScrollPosition == 0.0f) {
            mScrollPosition = getTop();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float shaftWidth = getWidth() / SHAFT_WIDTH_KOEFF;
        final float xCenter = getWidth() / 2;

        /**
         * comment canvas.save, canvas.clipRect(),
         * canvas.restore() and drawShaft()
         * to turn off shaft drawing
         */
        canvas.save();
        canvas.clipRect(xCenter - shaftWidth / 2, getTop(), xCenter + shaftWidth / 2, getHeight(),
                Region.Op.DIFFERENCE);
        drawFloors(canvas);
        canvas.restore();
        drawShaft(canvas);
    }

    private void drawFloors(final Canvas _canvas) {
        final Path floorsPath = new Path();

        for (int i = 0; i <= FLOOR_COUNT; i++) {
            float y = mScrollPosition + getBottom() - mFloorHeight * i;
            final float backSideHeight = (mCenterHeight - y) / 15;
            if ((y + (backSideHeight + 20) <= 0) || y + backSideHeight - mFloorHeight - 20 >= getBottom()) continue;

            if (i == FLOOR_COUNT && mScrollPosition == mMaxScrollPosition) y = getTop();

            floorsPath.moveTo(getLeft(), y);
            floorsPath.lineTo(getRight(), y);
            floorsPath.lineTo(getRight() - mFloorBackSidePadding, y + backSideHeight);
            floorsPath.lineTo(getLeft() + mFloorBackSidePadding, y + backSideHeight);

            if (i < FLOOR_COUNT)
                _canvas.drawText(String.valueOf(i), getLeft() + mTextPadding,
                        y - mFloorHeight / 2 + mTextSize / 3, mTextPaint);
        }

        _canvas.drawPath(floorsPath, mFloorPaint);
    }

    private void drawShaft(final Canvas _canvas) {
        final Path elevatorShaft = new Path();

        final float shaftWidth = getWidth() / SHAFT_WIDTH_KOEFF;
        final float shaftHalfWidth = shaftWidth / 2;
        final float shaftPadding = shaftWidth / SHAFT_PADDING_KOEFF;
        final float xCenter = getWidth() / 2;

        for (int i = 0; i <= FLOOR_COUNT; i++) {
            float y = mScrollPosition + getBottom() - mFloorHeight * i;
            final float backSideHeight = (mCenterHeight - y) / 15;
            if ((y + (backSideHeight + 20) <= 0) || y + backSideHeight - mFloorHeight - 20 >= getBottom()) continue;

            if (i == FLOOR_COUNT && mScrollPosition == mMaxScrollPosition) y = getTop();

            elevatorShaft.moveTo(xCenter - shaftHalfWidth, y);
            elevatorShaft.lineTo(xCenter - shaftHalfWidth + shaftPadding, y + backSideHeight);
            elevatorShaft.lineTo(xCenter + shaftHalfWidth - shaftPadding, y + backSideHeight);
            elevatorShaft.lineTo(xCenter + shaftHalfWidth, y);

            if (i < FLOOR_COUNT)
                _canvas.drawText(String.valueOf(i), getLeft() + mTextPadding,
                        y - mFloorHeight / 2 + mTextSize / 3, mTextPaint);
        }

        elevatorShaft.moveTo(xCenter - shaftHalfWidth, getTop());
        elevatorShaft.lineTo(xCenter - shaftHalfWidth, getBottom());
        elevatorShaft.moveTo(xCenter + shaftHalfWidth, getTop());
        elevatorShaft.lineTo(xCenter + shaftHalfWidth, getBottom());

        _canvas.drawPath(elevatorShaft, mShaftPaint);
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
            if ((mScrollPosition == 0 && distanceY >= 0) ||
                    (mScrollPosition == mMaxScrollPosition &&  distanceY <= 0))
                return false;

            if (mScrollPosition - distanceY <= 0)
                mScrollPosition = 0;
            else if (mScrollPosition - distanceY >= mMaxScrollPosition)
                mScrollPosition = mMaxScrollPosition;
            else
                mScrollPosition -= distanceY;

            ViewCompat.postInvalidateOnAnimation(ElevatorView.this);
            return true;
        }
    };
}
