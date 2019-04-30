package com.pixelfunapp.pixelpart;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class DialogFragmentMenu extends DialogFragment implements View.OnClickListener{
    private SwitchCompat doubleTapSwitch;
    private SwitchCompat magicSwitch;
    private SwitchCompat superZoomSwitch;
    private Button mButtonSubscribeOneMonth;
    private SharedPreferencesHelper mSharedPreferencesHelper;
    private TextView mMainText;
    private TextView mFirstSubText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View dialogView = inflater.inflate(R.layout.dialog_menu, container);

        Window window = getDialog().getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            //FULL_SCREEN
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //window.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_menu));
        }

        mSharedPreferencesHelper = new SharedPreferencesHelper(getActivity());

        View menuCloseButton = dialogView.findViewById(R.id.menu_close_btn);
        menuCloseButton.setOnClickListener(this);

        mMainText = dialogView.findViewById(R.id.menu_main_text);
        mFirstSubText = dialogView.findViewById(R.id.menu_first_sub_text);
        mFirstSubText.setVisibility(View.GONE);

        doubleTapSwitch = dialogView.findViewById(R.id.switch_double_tap);
        doubleTapSwitch.setOnClickListener(this);
        doubleTapSwitch.setChecked(mSharedPreferencesHelper.isDoubleTapOn());

        magicSwitch = dialogView.findViewById(R.id.switch_magic);
        magicSwitch.setOnClickListener(this);
        magicSwitch.setChecked(mSharedPreferencesHelper.isMagicOn());

        superZoomSwitch = dialogView.findViewById(R.id.switch_super_zoom);
        superZoomSwitch.setOnClickListener(this);
        superZoomSwitch.setChecked(mSharedPreferencesHelper.isSuperZoomOn());

        mButtonSubscribeOneMonth = dialogView.findViewById(R.id.button_tariff1);
        mButtonSubscribeOneMonth.setOnClickListener(this);

        if (((ScreenOne) getActivity()).adIsOff) onAdIsOff();
        return dialogView;
    }

    private void onAdIsOff() {
        mMainText.setText(R.string.menu_main_text_subscribed);
        String newFisrtLineText = getResources().getString(R.string.menu_subtitle_subscribed);
        mFirstSubText.setText(newFisrtLineText);
        mFirstSubText.setVisibility(View.VISIBLE);
        mButtonSubscribeOneMonth.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.MyDialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case(R.id.menu_close_btn):
                dismiss();
                break;
            case R.id.button_tariff1:
                dismiss();
                ((ScreenOne) getActivity()).subscribeOneMonth();
                break;
            case(R.id.switch_double_tap):
                mSharedPreferencesHelper.setIsDoubleTapOn(doubleTapSwitch.isChecked());
                break;
            case(R.id.switch_magic):
                mSharedPreferencesHelper.setIsMagicOn(magicSwitch.isChecked());
                break;
            case (R.id.switch_super_zoom):
                mSharedPreferencesHelper.setIsSuperZoomOn(superZoomSwitch.isChecked());
        }

    }
}
