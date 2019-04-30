package com.pixelfunapp.pixelpart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class PixelPalette {
    private static PixelPalette mPixelPalette;
    private static Activity mActivity;
    private static Context mContext;
    private static final int MAX_COLUMN_COUNT = 5;
    private static final int MAX_ROW_COUNT = 2;
    private int rowCount, pageCount, currentPage;
    public static int mSelectedColorNumber;
    public static Bitmap mEmptyPixelBitmap;
    public static SparseIntArray mPalleteColorsMap;
    public static HashMap<Integer, Bitmap> mBitmapNumbersMap;
    public static HashMap<Integer, Bitmap> mFullColoredBitmap;
    public static HashMap<Integer, Bitmap> mStartedNotColoredBitmap;
    public static HashMap<Integer, Bitmap> mDarkedNotColoredBitmap;
    public static HashMap<Integer, LinkedHashMap<Integer, Bitmap>> mWrongColoredBitmapNoNumber;

    PixelPalette(Activity activity) {
        mContext = activity.getBaseContext();
        mActivity = activity;
        mPixelPalette = this;
        rowCount = 0;
        pageCount = 0;
        currentPage = 1;
    }

    public static void setNewSelectedColorNumber(int newSelectedColorNumber) {
        TextView tmpView;
        //remove old - set unselected
        if (mSelectedColorNumber != 0) {
            tmpView = findPalleteButtonViewByNumber(mSelectedColorNumber);
            int buttonColor = mPalleteColorsMap.get(mSelectedColorNumber);
            tmpView.setBackgroundResource(R.drawable.pallete_button_unselected);
            Pixel.setFillColorForDrawable(tmpView.getBackground(), buttonColor);
            Pixel.returnStartedPixelsByNumber(mSelectedColorNumber);
        }
        if (newSelectedColorNumber != 0) {
            //set new - set selected
            tmpView = findPalleteButtonViewByNumber(newSelectedColorNumber);
            int buttonColor = mPalleteColorsMap.get(newSelectedColorNumber);
            tmpView.setBackgroundResource(R.drawable.pallete_button_selected);
            Pixel.setFillColorForDrawable(tmpView.getBackground(), buttonColor);
            Pixel.setDarkBackgroundForPixelsByNumber(newSelectedColorNumber);
            mSelectedColorNumber = newSelectedColorNumber;
        }
    }

    private static TextView findPalleteButtonViewByNumber(int number) {
        for (View button : getAllButtons()) {
            if (((TextView)button.findViewById(R.id.pallete_button_number)).getText().toString().equals(String.valueOf(number)))
                return button.findViewById(R.id.pallete_button_number);
        }
        return null;
    }

    private ViewGroup getView() {
        return mActivity.findViewById(R.id.pallete);
    }

    private static View getLeftButton() {
        return mActivity.findViewById(R.id.palette_left_btn);
    }

    private static View getRightButton() {
        return mActivity.findViewById(R.id.palette_right_btn);
    }

    private static ViewGroup getRowsContainer() {
        return mActivity.findViewById(R.id.pallete_buttons_rows_container);
    }

    public void initPallete(LinkedList<Integer> colors) {
        mPalleteColorsMap = new SparseIntArray();
        mBitmapNumbersMap = new HashMap<>();
        mFullColoredBitmap = new HashMap<>();
        mStartedNotColoredBitmap = new HashMap<>();
        mDarkedNotColoredBitmap = new HashMap<>();
        mWrongColoredBitmapNoNumber = new HashMap<>();
        mEmptyPixelBitmap = Pixel.createEmptyPixelBitmap();
        getRowsContainer().removeAllViews();
        int i = 1;
        for (int color: colors
                ) {
            //Collect color by number
            mPalleteColorsMap.put(i,color);
            //Collect Pixel by number for Pallete button
            Pixel px = new Pixel(color, i, mActivity.getBaseContext());
            //Create and collect Pallete button view
            mPixelPalette.addButton(px);
            //Create and collect BitmapNumber
            mBitmapNumbersMap.put(i, Pixel.createBitmapNumber(i));
            //Create and collect FullColoredBitmap
            mFullColoredBitmap.put(i, Pixel.createFullColoredBitmapByNumber(i));
            //Create and collect StartedNotColoredBitmap
            mStartedNotColoredBitmap.put(i, Pixel.createBitmapFullByNumber(i));
            //Create and collect DarkedNotColoredBitmap
            mDarkedNotColoredBitmap.put(i, Pixel.createDarkedBitmapByNumber(i));
            //Create WrongColoredBitmapNoNumber
            mWrongColoredBitmapNoNumber.put(i, null);
            i++;
        }
        for (int j = 1; j <= mWrongColoredBitmapNoNumber.size(); j++) {
            mWrongColoredBitmapNoNumber.put(j,Pixel.createWrongColoredCollectionByNumber(j));
        }
        mPixelPalette.initPager();
    }

    private void initPager() {
        if (pageCount > 1) {
            //set listeners
            getLeftButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickLeft();
                    LinkedList<Pixel> allPXs = new LinkedList<>();
                    for (LinkedList<Pixel> linkedList: Pixel.pxsByNumberNotColoredMap.values()) {
                        for (Pixel px: linkedList) {
                            allPXs.add(px);
                        }
                    }
                    for (Pixel px: allPXs) {
                        px.coloringByHisNumber();
                    }
                }
            });
            getRightButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickRight();
                }
            });
            checkPagerBtnStatus();
        }
    }

    private void checkPagerBtnStatus() {
        //left_btn
        if (currentPage == 1) {
            //deactivate left_btn
            getLeftButton().setBackgroundResource(R.drawable.palette_left_btn_inactive);
        }
        if (currentPage == 2) {
            //activate left_btn
            getLeftButton().setBackgroundResource(R.drawable.palette_left_btn_active);
        }
        //right_btn
        if (currentPage == pageCount) {
            //deactivate right_btn
            getRightButton().setBackgroundResource(R.drawable.palette_right_btn_inactive);
        }
        if (currentPage == (pageCount - 1)) {
            //activate right_btn
            getRightButton().setBackgroundResource(R.drawable.palette_right_btn_active);
        }
    }

    private void clickLeft() {
        if (currentPage != 1) {
            for (View v : getRowsOnPageByNumber(currentPage)) {v.setVisibility(View.GONE);}
            currentPage--;
            for (View v : getRowsOnPageByNumber(currentPage)) {v.setVisibility(View.VISIBLE);}
        }
        checkPagerBtnStatus();
    }

    private void clickRight() {
        if (currentPage != pageCount) {
            for (View v : getRowsOnPageByNumber(currentPage)) {v.setVisibility(View.GONE);}
            currentPage++;
            for (View v : getRowsOnPageByNumber(currentPage)) {v.setVisibility(View.VISIBLE);}
        }
        checkPagerBtnStatus();
    }

    private LinkedList<View> getRowsOnPageByNumber(int pageNumber) {
        LinkedList<View> ll = new LinkedList<>();
        for (int i = 0; i < MAX_ROW_COUNT; i++) {
            int rowNumber = (pageNumber) * MAX_ROW_COUNT - 1 + i;
            //rowNumber - 1 // numeric start 0
            View row = getRowByNumber(rowNumber - 1);
            if (row != null) ll.add(row);
        }
        return ll;
    }

    private View getRowByNumber(int rowNumber) {
        ViewGroup rows_cointainer = getRowsContainer();
        return rows_cointainer.getChildAt(rowNumber);
    }

    private void addButton(Pixel pixel) {
        int number = pixel.getNumber();
        int rowNumber = (number - 1) / MAX_COLUMN_COUNT;
        View row = getOrCreateRowByNumber(rowNumber);
        ((ViewGroup)row).addView(pixel.generatePalleteButton());
    }

    private View getOrCreateRowByNumber(int rowNumber) {
        ViewGroup rows_cointainer = getRowsContainer();
        int rows_count = rows_cointainer.getChildCount();
        if (rows_count > rowNumber) {
            return rows_cointainer.getChildAt(rowNumber);
        } else {
            View res = null;
            for (int i = 0; i < MAX_ROW_COUNT; i++) {
                View v = createNewRowAndAppend(rows_cointainer);
                if (res == null) res = v;
            }
            return res;
        }
    }

    private static LinkedList<ViewGroup> getAllRows() {
        LinkedList<ViewGroup> ll = new LinkedList<>();
        ViewGroup rows_cointainer = getRowsContainer();
        for (int i = 0; i < rows_cointainer.getChildCount(); i++) {
            ll.add((ViewGroup) rows_cointainer.getChildAt(i));
        }
        return ll;
    }

    private static LinkedList<View> getAllButtonsInRow(ViewGroup row) {
        LinkedList<View> ll = new LinkedList<>();
        for (int i = 0; i < row.getChildCount(); i++) {
            ll.add(row.getChildAt(i));
        }
        return ll;
    }

    private static LinkedList<View> getAllButtons() {
        LinkedList<View> buttons = new LinkedList<>();
        LinkedList<ViewGroup> rows = getAllRows();
        for (ViewGroup row : rows) {
            buttons.addAll(getAllButtonsInRow(row));
        }
        return buttons;
    }

    private View createNewRowAndAppend(ViewGroup container) {
        LinearLayout linearLayout = new LinearLayout(container.getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(llParams);
        container.addView(linearLayout);
        rowCount++;
        if (rowCount % MAX_ROW_COUNT == 1) pageCount++;
        return linearLayout;
    }

    public static Bitmap getBackgroundView(View v) {
        Drawable d = v.getBackground();
        return convertDrawableToBitmap(d);
    }

    private static Bitmap convertDrawableToBitmap(Drawable d) {
        if (d instanceof GradientDrawable) {
            GradientDrawable gd = (GradientDrawable) d;
            int[] fillColors = new int[60*60];
            Arrays.fill(fillColors, Color.BLUE);
            Bitmap immutableBitmap = Bitmap.createBitmap(fillColors, 60, 60, Bitmap.Config.ARGB_4444);
            Bitmap muttableBitmap = immutableBitmap.copy(Bitmap.Config.ARGB_4444, true);
            Canvas c = new Canvas(muttableBitmap);
            d.setBounds(0,0, muttableBitmap.getWidth(), muttableBitmap.getHeight());
            d.draw(c);
            return muttableBitmap;
        }
        return null;
    }

    public Context getContext() {return mContext;}

    private static void setButtonFinishedByNumber(int number) {
        TextView palleteButton = findPalleteButtonViewByNumber(number);
        palleteButton.setText(null);
        palleteButton.setBackgroundResource(R.drawable.pallete_button_finished);
        //coloredBackgroundLayerByColor(palleteButton, mContext.getResources().getColor(R.color.colorFinishedButton));
        palleteButton.setOnClickListener(null);
        mSelectedColorNumber = Pixel.getNotFinishedColorNumberNearNumber(mSelectedColorNumber);
        setNewSelectedColorNumber(mSelectedColorNumber);
        if (mSelectedColorNumber == 0) {
            if (!MainActivity.isColoringFinished) {
                MainActivity.isColoringFinished = true;
                MainActivity.touchFileByPathIfNotExist(MainActivity.fullPathFIN);
                MainActivity.showDialogFinished();
            }

        }
    }



    public static void RemoveFromNotColoredMap(Pixel pixel) {
        LinkedList<Pixel> pixelsList = Pixel.pxsByNumberNotColoredMap.get(pixel.getNumber());
        pixelsList.remove(pixel);
    }

    public static void checkColoredByNumber(int number) {
        LinkedList<Pixel> pixelsList = Pixel.pxsByNumberNotColoredMap.get(number);
        if (pixelsList == null) setButtonFinishedByNumber(number);
        else if (pixelsList.size() == 0) setButtonFinishedByNumber(number);
    }

    public static void checkAllColored() {
        for (Integer number: Pixel.pxsByNumberMap.keySet()) {
            checkColoredByNumber(number);
        }
    }

    private static void coloredBackgroundLayerByColor(TextView palleteButton, int color) {
        LayerDrawable ld = (LayerDrawable) palleteButton.getBackground();
        Drawable d = ld.findDrawableByLayerId(R.id.pallete_button_finished);
        Pixel.setFillColorForDrawable(d, color);
        ld.setDrawableByLayerId(R.id.pallete_button_finished ,d);
        palleteButton.setBackground(ld);
    }


}
