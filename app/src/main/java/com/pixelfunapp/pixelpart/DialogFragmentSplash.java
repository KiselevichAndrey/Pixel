package com.pixelfunapp.pixelpart;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class DialogFragmentSplash extends DialogFragment implements View.OnClickListener, DialogInterface.OnShowListener{
    private View dialogView;
    private ScreenOne.PlaceholderFragment placeholderFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dialogView = inflater.inflate(R.layout.dialog_splash, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //FULL_SCREEN
        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialogView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        placeholderFragment.loadGallery();
//        dismiss();
//    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
    }

    public void setPlaceholderFragment(ScreenOne.PlaceholderFragment placeholderFragment) {
        this.placeholderFragment = placeholderFragment;

    }

    @Override
    public void onShow(DialogInterface dialog) {
        Log.i("s", "S");
    }
}
