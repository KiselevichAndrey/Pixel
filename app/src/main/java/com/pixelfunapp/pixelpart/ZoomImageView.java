package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.SeekBar;
import android.widget.VerticalSeekBar;

class ZoomImageView extends android.support.v7.widget.AppCompatImageView {
    public static final int DISTANCE_FOR_TAP = 20; //TODO: Need use DP instead PX
    public static final int MS_TO_DRAW = 300;
    public static final int MS_TO_DOUBLE_TAP = 300;

    private boolean isMagicOn;
    private boolean isDoubleTapOn;
    private VerticalSeekBar mSuperZoom;

    public boolean isSuperZoomOn() {
        return isSuperZoomOn;
    }

    private boolean isSuperZoomOn;

    private ScaleGestureDetector mScaleGestureDetector;
    private Matrix mImageMatrix;
    private boolean mIsLongPress;
    private boolean mIsFirstMoveEvent;
    private float mStartMoveX, mStartMoveY, mLastMoveX, mLastMoveY;
    private float mMinimumScale;

    public float getMinimumScale() {
        return mMinimumScale;
    }

    public float getMaximumScale() {
        return mMaximumScale;
    }

    private float mMaximumScale;
    private long doubleTapStartTime;
    private int mXBorder, mYBorder;
    //?
    private boolean mIsScrollOn;

    private int mPivotX, mPivotY;

    public ZoomImageView(Context context) {
        super(context);
        init(context);
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(context);
        isMagicOn = sharedPreferencesHelper.isMagicOn();
        isDoubleTapOn = sharedPreferencesHelper.isDoubleTapOn();
        isSuperZoomOn = sharedPreferencesHelper.isSuperZoomOn();
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(context);
        isMagicOn = sharedPreferencesHelper.isMagicOn();
        isDoubleTapOn = sharedPreferencesHelper.isDoubleTapOn();
        isSuperZoomOn = sharedPreferencesHelper.isSuperZoomOn();
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(context);
        isMagicOn = sharedPreferencesHelper.isMagicOn();
        isDoubleTapOn = sharedPreferencesHelper.isDoubleTapOn();
        isSuperZoomOn = sharedPreferencesHelper.isSuperZoomOn();
    }

    private void init(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleListener);
        setScaleType(ScaleType.MATRIX);
        mImageMatrix = new Matrix();
    }

    public void initScrollAndScale() {
        int w = getWidth();
        int h = getHeight();
        mIsScrollOn = false;
        // Border 5%
        int imageBorder = Math.min(w, h)/100 * 5;
        int imageWidth = getDrawable().getIntrinsicWidth();
        int imageHeight = getDrawable().getIntrinsicHeight();
        int newW = w - imageBorder * 2;
        int newH = h - imageBorder * 2;
        float scale;
        int newImageWidth;
        int newImageHeight;
        if (newW < newH) {
            //width
            scale = (float)newW / imageWidth;
            newImageWidth = (int)(imageWidth * scale);
            newImageHeight = (int)(imageHeight * scale);
            if (newImageHeight > newH) {
                scale = newH / newImageHeight;
                newImageWidth = (int)(newImageHeight * scale);
                newImageHeight = (int)(newImageHeight * scale);
            }
        } else {
            //height
            scale = (float)newH / imageHeight;
            newImageWidth = (int)(imageWidth * scale);
            newImageHeight = (int)(imageHeight * scale);
            if (newImageWidth > newW) {
                scale = newW / newImageWidth;
                newImageWidth = (int) (newImageHeight * scale);
                newImageHeight = (int) (newImageHeight * scale);
            }
        }
        mMinimumScale = scale;
        mMaximumScale = scale * 10;
        mImageMatrix.setScale(scale, scale);
        //Shift the image to the center of the view
        int translateX = Math.abs(w - newImageWidth) / 2;
        mXBorder = translateX;
        int translateY = Math.abs(h - newImageHeight) / 2;
        mYBorder = translateY;
        mImageMatrix.postTranslate(translateX, translateY);

        setImageMatrix(mImageMatrix);
        //Get the center point for future scale and rotate transforms
        mPivotX = w / 2;
        mPivotY = h / 2;
    }

    private SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mImageMatrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(mImageMatrix);
            return true;
        }
    };

    private SimpleOnScaleGestureListener mScaleListener = new SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //scale
            float scaleFactor = detector.getScaleFactor();
            if (scaleFactor == 1.0f) return true;
            postScale(scaleFactor);
            float[] martixValues = new float[9];
            mImageMatrix.getValues(martixValues);
            float newScale = martixValues[0];
            if (mSuperZoom != null) {
                mSuperZoom.onSeekBarChangeListenerOff();
                mSuperZoom.setMax(mSuperZoom.getMax());
                mSuperZoom.setProgress((int) (newScale - mMinimumScale) * MainActivity.SCALE_MULTIPLIER);
                mSuperZoom.updateThumb();
                mSuperZoom.onSeekBarChangeListenerOn();
            }
            return true;
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            // We don't care about this event directly, but we declare
//            // interest so we can get later multi-touch events.
//            return true;
//        }
        switch (event.getPointerCount()) {
            case 2:
                // With two fingers down, rotate the image
                // following the fingers
                // return doRotationEvent(event);
                return mScaleGestureDetector.onTouchEvent(event);
            case 1:
                //Not gesture, manual process
                //printSamples(event);
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsLongPress = false;
                    mIsFirstMoveEvent = true;
                    mStartMoveX = event.getX();
                    mStartMoveY = event.getY();
                    mLastMoveX = event.getX();
                    mLastMoveY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsFirstMoveEvent) {
                        float distanceX = mStartMoveX - event.getX();
                        float distanceY = mStartMoveY - event.getY();
                        float distance = (float) Math.sqrt(distanceX*distanceX + distanceY*distanceY);
                        Log.i("event", "distance: " + String.valueOf(distance));
                        if (distance > DISTANCE_FOR_TAP) {
                            mIsFirstMoveEvent = false;
                        }
                        if (event.getEventTime() - event.getDownTime() > MS_TO_DRAW) {
                            mIsLongPress = true;
                            mIsFirstMoveEvent = false;
                        }
                        mLastMoveX = event.getX();
                        mLastMoveY = event.getY();
                    } else {
                        if (mIsLongPress && isMagicOn) {
                            //Draw
                            if (PixelPalette.mSelectedColorNumber != 0) {
                                float inViewX = event.getX();
                                float inViewY = event.getY();
                                float[] martixValues = new float[9];
                                mImageMatrix.getValues(martixValues);
                                float scale = martixValues[0];
                                float imageX = martixValues[2];
                                float imageY = martixValues[5];
                                float inImageX = (inViewX - imageX) / scale;
                                float inImageY = (inViewY - imageY) / scale;
                                Log.i("XY", "X : " + String.valueOf(inImageX) + " Y : " + String.valueOf(inImageY));
                                Pixel px = Pixel.getPixelByFloatCoordinate(inImageX, inImageY);
                                if (px != null) {
                                    px.coloringByNumber(PixelPalette.mSelectedColorNumber);
                                }
                            }
                        } else {
                            //Scroll
                            float distanceX = event.getX() - mStartMoveX;
                            float distanceY = event.getY() - mStartMoveY;
                            float[] martixValues = new float[9];
                            mImageMatrix.getValues(martixValues);
                            float scale = martixValues[0];
                            float imageXabsolute = martixValues[2];
                            float imageYabsolute = martixValues[5];
                            Bitmap image = ((BitmapDrawable)getDrawable()).getBitmap();
                            float imageWidth = image.getWidth()*scale;
                            float imageHeight = image.getHeight()*scale;
                            Log.i("distance", String.valueOf(distanceX) + "," + String.valueOf(distanceY));
                            if (distanceX > 0) {
                                if ((imageXabsolute + distanceX) > mXBorder) distanceX = (mXBorder - imageXabsolute);
                            }
                            else {
                                if ((getWidth() - imageXabsolute - imageWidth - distanceX) > mXBorder) distanceX = (getWidth() - imageXabsolute - imageWidth - mXBorder);
                            }
                            if (distanceY > 0) {
                                if ((imageYabsolute + distanceY) > mYBorder) distanceY = (mYBorder - imageYabsolute);
                            } else {
                                if ((getHeight() - imageYabsolute - imageHeight - distanceY) > mYBorder) distanceY = (getHeight() - imageYabsolute - imageHeight - mYBorder);
                            }
                            if (mIsScrollOn) {
                                setImageMatrix(mImageMatrix);
                                mImageMatrix.postTranslate(distanceX, distanceY);
                            }
                            mStartMoveX = event.getX();
                            mStartMoveY = event.getY();
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    float distanceX = mLastMoveX - event.getX();
                    float distanceY = mLastMoveY - event.getY();
                    float distance = (float) Math.sqrt(distanceX*distanceX + distanceY*distanceY);
                    if (distance < DISTANCE_FOR_TAP) {
                        if (mIsFirstMoveEvent) {
                            //Tap Draw one
                            float viewX = event.getX();
                            float viewY = event.getY();
                            float[] matrixValues = new float[9];
                            mImageMatrix.getValues(matrixValues);
                            float scale = matrixValues[0];
                            float imageX = matrixValues[2];
                            float imageY = matrixValues[5];
                            float inImageX = (viewX - imageX) / scale;
                            float inImageY = (viewY - imageY) / scale;
                            Log.i("XY", "X : " + String.valueOf(inImageX) + " Y : " + String.valueOf(inImageY));
                            Pixel px = Pixel.getPixelByFloatCoordinate(inImageX, inImageY);
                            if (px != null) {
                                px.coloringByNumber(PixelPalette.mSelectedColorNumber);
                            }
                            //Check double tap
                            if (event.getDownTime() - doubleTapStartTime < MS_TO_DOUBLE_TAP) {
                                //Double tap
                                if (px != null && isDoubleTapOn) {
                                    px.coloringByNumberDoubleTap(PixelPalette.mSelectedColorNumber);
                                }
                            }
                            //Save time for register double tap
                            doubleTapStartTime = event.getDownTime();
                        }
                    }
                    break;
                }
            default:
                //Ignore this event
                return true;
        }
    }

    public void postScale(float scaleFactor) {
        float[] martixValues = new float[9];
        mImageMatrix.getValues(martixValues);
        float oldScale = martixValues[0];
        if (oldScale * scaleFactor < mMinimumScale) {
            scaleFactor = mMinimumScale / oldScale;
            mIsScrollOn = false;
        } else {
            mIsScrollOn = true;
        }
        if (oldScale * scaleFactor > mMaximumScale) scaleFactor = mMaximumScale / oldScale;

        mImageMatrix.postScale(scaleFactor, scaleFactor, mPivotX, mPivotY);

        //move after scale
        float oldX = martixValues[2];
        float oldY = martixValues[5];
        mImageMatrix.getValues(martixValues);
        float imageXabsolute = martixValues[2];
        float imageYabsolute = martixValues[5];
        float distanceX = imageXabsolute - oldX;
        float distanceY = imageYabsolute - oldY;
        float scale = martixValues[0];
        Bitmap image = ((BitmapDrawable)getDrawable()).getBitmap();
        float imageWidth = image.getWidth()*scale;
        float imageHeight = image.getHeight()*scale;

        float leftBorder = distanceX + oldX;
        float rightBorder = getWidth() - imageWidth - distanceX - oldX;
        if (leftBorder > mXBorder) distanceX = mXBorder - oldX;
        else if (rightBorder > mXBorder) distanceX = getWidth() - imageWidth - oldX - mXBorder;
        else distanceX = 0;
        float topBorder = distanceY + oldY;
        float bottomBorder = getHeight() - imageHeight - distanceY - oldY;
        if (topBorder > mYBorder) distanceY = mYBorder - oldY;
        else if (bottomBorder > mYBorder) distanceY = getHeight() - imageHeight - oldY - mYBorder;
        else distanceY = 0;

        mImageMatrix.postTranslate(distanceX, distanceY);

        setImageMatrix(mImageMatrix);
    }

    public void setScale(float scaleFactor) {
        float[] martixValues = new float[9];
        mImageMatrix.getValues(martixValues);
        float oldScale = martixValues[0];
        scaleFactor = scaleFactor/oldScale;
        if (oldScale * scaleFactor < mMinimumScale) {
            scaleFactor = mMinimumScale / oldScale;
            mIsScrollOn = false;
        } else {
            mIsScrollOn = true;
        }
        if (oldScale * scaleFactor > mMaximumScale) scaleFactor = mMaximumScale / oldScale;

        mImageMatrix.postScale(scaleFactor, scaleFactor, mPivotX, mPivotY);

        //move after scale
        float oldX = martixValues[2];
        float oldY = martixValues[5];
        mImageMatrix.getValues(martixValues);
        float imageXabsolute = martixValues[2];
        float imageYabsolute = martixValues[5];
        float distanceX = imageXabsolute - oldX;
        float distanceY = imageYabsolute - oldY;
        float scale = martixValues[0];
        //todo: check asyncronic change width and height in setImageMatrix
        Bitmap image = ((BitmapDrawable)getDrawable()).getBitmap();
        float imageWidth = image.getWidth()*scale;
        float imageHeight = image.getHeight()*scale;

        float leftBorder = distanceX + oldX;
        float rightBorder = getWidth() - imageWidth - distanceX - oldX;
        if (leftBorder > mXBorder) distanceX = mXBorder - oldX;
        else if (rightBorder > mXBorder) distanceX = getWidth() - imageWidth - oldX - mXBorder;
        else distanceX = 0;
        float topBorder = distanceY + oldY;
        float bottomBorder = getHeight() - imageHeight - distanceY - oldY;
        if (topBorder > mYBorder) distanceY = mYBorder - oldY;
        else if (bottomBorder > mYBorder) distanceY = getHeight() - imageHeight - oldY - mYBorder;
        else distanceY = 0;

        mImageMatrix.postTranslate(distanceX, distanceY);

        setImageMatrix(mImageMatrix);
    }

    public void setSuperZoom(VerticalSeekBar superZoom) {
        this.mSuperZoom = superZoom;
    }
}