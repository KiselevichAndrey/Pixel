package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by PC1 on 11.03.2018.
 */

public class IntroManager {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public IntroManager(Context context, String fileNamePreferences)
    {
        this.context = context;
        pref = context.getSharedPreferences(fileNamePreferences, Context.MODE_PRIVATE);
        // .getSharedPreferences( nameFile,
        editor = pref.edit();
    }

    public void setBoolean(String key, boolean value)
    {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void setString(String key, String value)
    {
        editor.putString(key, value);
        editor.commit();
    }
    public String getString(String key)
    {
        return pref.getString(key, "");
    }
    public boolean Check(String key)
    {
        return pref.getBoolean(key, true);
    }

}