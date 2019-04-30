package com.pixelfunapp.pixelpart;

import android.app.Activity;
import android.view.View;

public class MainImageGalleryClickListener implements View.OnClickListener {
    ImageAdapter mImageAdapter;
    ScreenOne mActivity;
    public MainImageGalleryClickListener(ImageAdapter imageAdapter, ScreenOne activity) {
        mImageAdapter = imageAdapter;
        mActivity = activity;
    }

    @Override
    public void onClick(View v) {
        ImageAdapter.currentColoringImageAdapter = mImageAdapter;
        mActivity.showPreview(ImageAdapter.currentColoringImageAdapter.getCategoryAdapterByIcon(v));
    }
}
