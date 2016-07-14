package ua.shubs.testelevator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vitaliy on 01/03/2016.
 */
public class ElevatorView extends View {

    public static final int FLOOR_COUNT = 100;
    public static final int FLOOR_PER_SCREEN = 7;

    public static final int SHAFT_PADDING_KOEFF = 9;
    public static final int SHAFT_WIDTH_KOEFF = 5;

    private Paint mFloorPaint;
    private Paint mTextPaint;
    private Paint mShaftPaint;
    private Paint mActiveFloor;
    private int mFloorColor;
    private int mShaftColor;
    private int mTextColor;
    private float mTextPadding;
    private float mTextSize;
    private float mFloorBackSidePadding;
    private float mShaftLineWidth;

    private float mFloorHeight;
    private float mCenterHeight;

    private List<FloorModel> floorModelList;
    private int activeFloor;
    private int lastVisibleFloor = FLOOR_PER_SCREEN;

    private boolean isActive;
    private boolean showHeader;
    private boolean showMine = false;
    private Scroller mScroller;
    private float mScrollPosition = 0.0f;
    private float mMaxScrollPosition;
    private GestureDetectorCompat mGestureDetector;

    public ElevatorView(final Context _context, AttributeSet _attributeSet) {
        super(_context, _attributeSet);
        initProps(_attributeSet);
        initPaints();
        mGestureDetector = new GestureDetectorCompat(_context, mGestureListener);
        mScroller = new Scroller(_context, null, true);
        floorModelList = new ArrayList<>();
    }

    private void initProps(final AttributeSet _attributeSet) {
        final TypedArray typedArray = getContext().getTheme()
                .obtainStyledAttributes(_attributeSet, R.styleable.ElevatorView, 0, 0);
        try {
            mFloorColor = typedArray.getColor(R.styleable.ElevatorView_floorColor, Color.GRAY);
            mTextColor = typedArray.getColor(R.styleable.ElevatorView_floorTextColor, Color.GRAY);
            mTextPadding = typedArray.getDimensionPixelOffset(R.styleable.ElevatorView_floorTextPadding, 5);
            mFloorBackSidePadding = typedArray.getDimensionPixelOffset(R.styleable.ElevatorView_floorBackPadding, 100);
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

        mActiveFloor = new Paint();
        mActiveFloor.setAntiAlias(true);
        mActiveFloor.setColor(mShaftColor);
        mActiveFloor.setAlpha(100);
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
        fillFloorList();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float shaftWidth = getWidth() / SHAFT_WIDTH_KOEFF;
        final float xCenter = getWidth() / 2;


        canvas.save();
        if (showMine)
            canvas.clipRect(xCenter - shaftWidth / 2, getTop(), xCenter + shaftWidth / 2, getHeight(),
                    Region.Op.DIFFERENCE);
        drawFloors(canvas);

        canvas.restore();
        if (showMine) drawShaft(canvas);

//        if (showHeader) drawHeader(canvas);
    }

    private void drawHeader(Canvas _canvas) {
        RectF rectF = new RectF(getLeft(), getBottom() - mFloorHeight, getWidth(), getBottom());
        _canvas.drawRect(rectF, mActiveFloor);
    }

    private void drawFloors(final Canvas _canvas) {
        final Path floorsPath = new Path();

        for (int i = 0; i <= FLOOR_COUNT; i++) {
            float y = mScrollPosition + getBottom() - mFloorHeight * i;
            final float backSideHeight = (mCenterHeight - y) / 15;
            if ((y + (backSideHeight + 20) <= 0) || y + backSideHeight - mFloorHeight - 20 >= getBottom())
                continue;
            if (i == FLOOR_COUNT && mScrollPosition == mMaxScrollPosition) y = getTop();
            if (isActive && activeFloor - 1 == i && floorModelList.get(activeFloor - 1).begin > getBottom())
                continue;

            floorsPath.moveTo(getLeft(), y);
            floorsPath.lineTo(getRight(), y);
            floorsPath.lineTo(getRight() - mFloorBackSidePadding, y + backSideHeight);
            floorsPath.lineTo(getLeft() + mFloorBackSidePadding, y + backSideHeight);

            if (i < FLOOR_COUNT)
                _canvas.drawText(String.valueOf(i), getLeft() + mTextPadding,
                        y - mFloorHeight / 2 + mTextSize / 3, mTextPaint);
        }

        _canvas.drawPath(floorsPath, mFloorPaint);

        if (isActive) {
            float y = mScrollPosition + getBottom() - mFloorHeight * activeFloor;
            RectF rect = new RectF(getLeft(), y, getWidth(), y + mFloorHeight);
            if (showHeader && floorModelList.get(activeFloor - 1).begin > getBottom()) {
                floorsPath.reset();
                float backSideHeight = (mCenterHeight - getBottom()) / 15;
                floorsPath.moveTo(getLeft(), getBottom());
                floorsPath.lineTo(getRight(), getBottom());
                floorsPath.lineTo(getRight() - mFloorBackSidePadding, getBottom() + backSideHeight);
                floorsPath.lineTo(getLeft() + mFloorBackSidePadding, getBottom() + backSideHeight);
                _canvas.drawRect(getLeft(), getBottom() - mFloorHeight, getWidth(), getBottom(), mActiveFloor);
                _canvas.drawPath(floorsPath, mActiveFloor);
                _canvas.drawText(String.valueOf(floorModelList.get(activeFloor - 1).number), getLeft() + mTextPadding,
                        getBottom() - mFloorHeight / 2 + mTextSize / 3, mTextPaint);
            } else
                _canvas.drawRect(rect, mActiveFloor);
        }

    }

    strictfp private void fillFloorList() {
        if (floorModelList.size() == 0) {
            for (int i = 0; i < FLOOR_COUNT; i++) {
                floorModelList.add(new FloorModel(i, getBottom() - (mFloorHeight * i), getBottom() - mFloorHeight * (i + 1)));
            }
        }
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
            if ((y + (backSideHeight + 20) <= 0) || y + backSideHeight - mFloorHeight - 20 >= getBottom())
                continue;

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

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {


        @Override
        public boolean onDown(MotionEvent e) {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            mScroller.startScroll(0, mScroller.getCurrY(), 0, (int) distanceY);
            if ((mScrollPosition == 0 && distanceY >= 0) ||
                    (mScrollPosition == mMaxScrollPosition && distanceY <= 0))
                return false;

            if (mScrollPosition - distanceY <= 0)
                mScrollPosition = 0;
            else if (mScrollPosition - distanceY >= mMaxScrollPosition)
                mScrollPosition = mMaxScrollPosition;
            else
                mScrollPosition -= distanceY;

//            if (testa(distanceY)) {
                if (e2.getY() < getBottom() && e1.getY() < getBottom()) {
                    for (int i = 0; i < floorModelList.size(); i++) {
                        floorModelList.get(i).change(distanceY);
                        if (isActive && activeFloor == i && floorModelList.get(i).begin > getBottom() - mFloorHeight)
                            showHeader = true;
                    }
                }
//            }

//            if (distanceY < 0) {
//                for (int i = 0; i < (test(-distanceY / mFloorHeight)); i++) {
//                    if (!tset2()){
//                        floorModelList.add(new FloorModel(lastVisibleFloor + i, -distanceY + (mFloorHeight * i), mFloorHeight * (i + 1)));
//                        lastVisibleFloor += test(-distanceY / mFloorHeight);
//                    }
////                    if (i != test(-distanceY / mFloorHeight))
////                        floorModelList.remove(i);
//                }
//            }
            invalidate();
            return true;
        }

        private float last;
//        strictfp private boolean testa(float _distanceY) {
//            if (mScrollPosition - _distanceY > 0)
//                last =
//        }

        private int test(float _f) {
            return (int) Math.floor(_f + 0.99f);
        }

        private boolean tset2() {
            for (FloorModel floorModel : floorModelList) {
                if (floorModel.number == lastVisibleFloor)
                    return true;
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < floorModelList.size(); i++) {
                FloorModel floorModel = floorModelList.get(i);
                if (e.getY() < floorModel.begin && e.getY() > floorModel.end) {
                    Log.e(ElevatorView.class.getSimpleName(), String.valueOf(floorModel.number));
                    isActive = true;
                    activeFloor = i + 1;
                    ViewCompat.postInvalidateOnAnimation(ElevatorView.this);
                    return true;
                }
            }
            return false;
        }


    };
}
