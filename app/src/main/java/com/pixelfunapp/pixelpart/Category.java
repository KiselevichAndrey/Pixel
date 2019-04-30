package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class Category extends View {
    private String Name;
    private boolean isTop;


    public Category(Context context) {
        super(context);
        init(context);
    }

    public Category(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Category(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        isTop = false;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getName() {
        return Name;
    }

    public void setTop(boolean top) {
        isTop = top;
        //create filter
    }

    public boolean isTop() {
        return isTop;
    }



}