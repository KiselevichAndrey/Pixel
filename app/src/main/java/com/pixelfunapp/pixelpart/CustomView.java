package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

public class CustomView extends View {
    private ScreenOne.PlaceholderFragment placeholderFragment;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public CustomView(Context context) {
        super(context);
    }
}
