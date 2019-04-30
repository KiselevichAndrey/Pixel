package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

public class IconGrid extends GridLayout {
    public IconGrid(Context context) {
        super(context);
        setColumnCount(3);
    }
//    //Calculate columns
//    int parentWidth = ((LinearLayout)WorkshopIconsGrid.getParent()).getWidth();
//            if (parentWidth != 0) {
//        int imageAdapterWidthInPixels = (int) (ImageAdapter.scaleDPMultiply * (ImageAdapter.mImageDP + ImageAdapter.mMarginDP * 2));
//        WorkshopIconsGrid.setColumnCount(parentWidth / imageAdapterWidthInPixels);
//    }
}
