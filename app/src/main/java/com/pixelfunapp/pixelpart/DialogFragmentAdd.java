package com.pixelfunapp.pixelpart;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

public class DialogFragmentAdd extends DialogFragment implements View.OnClickListener{
    private View dialogView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dialogView = inflater.inflate(R.layout.dialog_add, container);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //FULL_SCREEN
        getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialogView.findViewById(R.id.add_close_btn).setOnClickListener(this);
        dialogView.findViewById(R.id.camera_btn).setOnClickListener(this);
        dialogView.findViewById(R.id.load_gallery_btn).setOnClickListener(this);
        return dialogView;
    }

    private void setOnClickListener(View v) {
        v.setOnClickListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case(R.id.add_close_btn):
                dismiss();
                break;
            case(R.id.camera_btn):
                dismiss();
                ((ScreenOne)getActivity()).cameraClick();
                break;
            case(R.id.load_gallery_btn):
                dismiss();
                ((ScreenOne)getActivity()).loadFromGalleryClick();
                break;
        }

    }
}
