package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;

public class SharedPreferencesHelper {
    private static final String SHARED_PREF_USER_SETTINGS = "SHARED_PREF_USER_SETTINGS";
    private static final String APP_VERSION = "APP_VERSION";
    private static final String IS_MAGIC = "IS_MAGIC";
    private static final boolean DEFAULT_IS_MAGIC = true;
    private static final String IS_DOUBLE_TAP = "IS_DOUBLE_TAP";
    private static final boolean DEFAULT_IS_DOUBLE_TAP = true;
    private static final String IS_SUPER_ZOOM = "IS_SUPER_ZOOM";
    private static final boolean DEFAULT_IS_SUPER_ZOOM = true;
    private static final String COLORINGS_RESOURCES = "CLS_RES_";


    private SharedPreferences mSharedPreferences;

    SharedPreferencesHelper(Context context) {
        this.mSharedPreferences = context.getSharedPreferences(SHARED_PREF_USER_SETTINGS, Context.MODE_PRIVATE);
    }

    public boolean isMagicOn() {
        return mSharedPreferences.getBoolean(IS_MAGIC, DEFAULT_IS_MAGIC);
    }

    public boolean isDoubleTapOn() {
        return mSharedPreferences.getBoolean(IS_DOUBLE_TAP, DEFAULT_IS_DOUBLE_TAP);
    }

    public boolean isSuperZoomOn() {
        return mSharedPreferences.getBoolean(IS_SUPER_ZOOM, DEFAULT_IS_SUPER_ZOOM);
    }

    public boolean isColoringPrepared(String coloringName) {
        return mSharedPreferences.getBoolean(COLORINGS_RESOURCES + coloringName, false);
    }

    public int getPreferencesAppVersion() {
        return mSharedPreferences.getInt(APP_VERSION, 0);
    }

    public void setIsMagicOn(boolean checked) {
        mSharedPreferences.edit().putBoolean(IS_MAGIC, checked).apply();
    }

    public void setIsDoubleTapOn(boolean checked) {
        mSharedPreferences.edit().putBoolean(IS_DOUBLE_TAP, checked).apply();
    }

    public void setIsSuperZoomOn(boolean checked) {
        mSharedPreferences.edit().putBoolean(IS_SUPER_ZOOM, checked).apply();
    }

    public void setColoringIsPrepared(String coloringName) {
        mSharedPreferences.edit().putBoolean(COLORINGS_RESOURCES + coloringName, true).apply();
    }

    public void setPreferencesAppVersion(int version) {
        mSharedPreferences.edit().putInt(APP_VERSION, version).apply();
    }

}
