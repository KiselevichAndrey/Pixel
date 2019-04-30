package com.pixelfunapp.pixelpart;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.VerticalSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements Palette.PaletteAsyncListener {

    public static final int MAX_DIFF_BETWEEN_COLORS = 32;
    public static final int SCALE_MULTIPLIER = 1000;

    public static ZoomImageView mImageWorkspaceView;
    public static Bitmap imageWorkspace;
    public static String fullPathWRK, fullPathFIN, fullPathToColoring;
    public static PixelPalette mPixelPalette;
    public static boolean hasNewColoredPixels;
    public static boolean isColoringFinished;
    private static ArrayList<Integer> mColoredPixelsPairs;
    private VerticalSeekBar mSuperZoom;
    private VerticalSeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float minimumScale = mImageWorkspaceView.getMinimumScale();
            float maximumScale = mImageWorkspaceView.getMaximumScale();
            float newScale = minimumScale + (float)progress/SCALE_MULTIPLIER;
            //todo: fix scale!
            if (newScale < minimumScale) newScale = minimumScale;
            if (newScale > maximumScale) newScale = maximumScale;
            mImageWorkspaceView.setScale(newScale);
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) Log.i("savedInstanceState", "is NULL");
        else Log.i("savedInstanceState", "is NOT_NULL");
        setContentView(R.layout.activity_main);

        //FULL_SCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent i = getIntent();
        //get .png full path and .wrk full path
        fullPathToColoring = i.getStringExtra("fullPathToColoring");
        fullPathWRK = i.getStringExtra("fullPathWRK");
        fullPathFIN = i.getStringExtra("fullPathFIN");
        //get colored pixel pairs
        mColoredPixelsPairs = i.getIntegerArrayListExtra("coloredPixels");

        //get image raw from file
        Bitmap imageRaw = BitmapFactory.decodeFile(fullPathToColoring);
        imageWorkspace = imageRaw;

        mImageWorkspaceView = new ZoomImageView(this);
        mImageWorkspaceView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        RelativeLayout imageViewContainer = findViewById(R.id.image_view_container);
        imageViewContainer.addView(mImageWorkspaceView);
        imageViewContainer.findViewById(R.id.back_btn).bringToFront();



        hasNewColoredPixels = false;
        mPixelPalette = new PixelPalette(this);
        isColoringFinished = new File(fullPathFIN).exists();

        //generate Palette colors
        if (imageRaw != null && !imageRaw.isRecycled()) {
            Palette.from(imageRaw).generate(this);
        }
    }

    public static boolean colorIsNearByColors(int color, LinkedList<Integer> colors, int maxDiff) {
        int cR, cG, cB;
        cR = Color.red(color);
        cG = Color.green(color);
        cB = Color.blue(color);
        int R, G, B;
        for (int col : colors) {
            R = Color.red(col);
            G = Color.green(col);
            B = Color.blue(col);
            int difference = 0;
            difference += Math.abs(R - cR);
            difference += Math.abs(G - cG);
            difference += Math.abs(B - cB);
            if (maxDiff > difference) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onGenerated(@NonNull Palette palette) {
        LinkedList<Integer> colors = new LinkedList<>();
        for (Palette.Swatch swatch:palette.getSwatches()
                ) {
            int color = swatch.getRgb();
            //exclude near color
            if (!colorIsNearByColors(color, colors, MAX_DIFF_BETWEEN_COLORS)) colors.add(color);
        }
        mPixelPalette.initPallete(colors);
        // Longtime operations
        Pixel.initPXsByBitmap(imageWorkspace, convertPairsToStrings(mColoredPixelsPairs));
        imageWorkspace = Pixel.generateBitmapWorked(imageWorkspace);

        mImageWorkspaceView.setImageBitmap(imageWorkspace);
        mImageWorkspaceView.initScrollAndScale();
        mSuperZoom = findViewById(R.id.super_zoom);
        if (!mImageWorkspaceView.isSuperZoomOn()) { mSuperZoom.setVisibility(View.GONE); }
        else {
            mImageWorkspaceView.setSuperZoom(mSuperZoom);
            mSuperZoom.setMax((int)((mImageWorkspaceView.getMaximumScale() - mImageWorkspaceView.getMinimumScale())*SCALE_MULTIPLIER));
            mSuperZoom.setupOnSeekBarChangeListener(mSeekBarChangeListener);
        }
        PixelPalette.checkAllColored();
        PixelPalette.mSelectedColorNumber = Pixel.getNotFinishedColorNumberNearNumber(1);
        PixelPalette.setNewSelectedColorNumber(PixelPalette.mSelectedColorNumber);
    }


    public static LinkedList<String> convertPairsToStrings(ArrayList<Integer> pairs) {
        LinkedList<String> res = new LinkedList<>();
        int x = 0, y = 0;
        boolean isY = false;
        for (Integer p : pairs) {
            if (!isY) {
                x = p;
                isY = true;
            } else {
                y = p;
                res.add(String.valueOf(x) + "," + String.valueOf(y));
                isY = false;
            }
        }
        return res;
    }

    public void backClick(View v) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (hasNewColoredPixels) {
            Intent intent = getIntent();
            intent.putExtra("hasNewColoredPixels", true);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }

    public static void touchFileByPathIfNotExist(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                if(!file.createNewFile()) {
                    Log.e("touchFile", "Can't create file");
                }
            } catch (IOException e) {
                Log.e("touchFile", "IOException! Can't create file");
            }
        }
    }


    public static void showDialogFinished() {
        //!
        // close existing dialog fragments
        android.app.FragmentManager manager = getFragmentManagerStatic();
        android.app.Fragment frag = manager.findFragmentByTag("finished_fragment");
        if (frag != null) {
            manager.beginTransaction().remove(frag).commit();
        }
        DialogFragmentFinished previewDialog = new DialogFragmentFinished();
        previewDialog.show(manager, "finished_fragment");
    }

    private static android.app.FragmentManager getFragmentManagerStatic() {
        Context context = MainActivity.mImageWorkspaceView.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return ((Activity)context).getFragmentManager();
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    public static Activity getActivityStatic() {
        Context context = MainActivity.mImageWorkspaceView.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}
