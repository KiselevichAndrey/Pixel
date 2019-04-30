package com.pixelfunapp.pixelpart;

import android.util.Log;

public class MyLogger {
    private String mHeader;
    public MyLogger (String header) {
        mHeader = header;
    }
    public void log(String msg) {
        Log.i(mHeader, msg);
    }
}
