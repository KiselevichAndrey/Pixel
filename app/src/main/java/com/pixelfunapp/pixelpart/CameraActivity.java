package com.pixelfunapp.pixelpart;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener{
    private android.hardware.Camera backCamera, frontCamera;
    private android.hardware.Camera.Parameters cameraParameters;
    private View changeCameraButton;
    private CameraPreview backCameraPreview, frontCameraPreview;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private Button shotBtn;
    private SeekBar pixelizationControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FULL_SCREEN
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        changeCameraButton = findViewById(R.id.camera_change_btn);
        changeCameraButton.setOnClickListener(this);
        //SEEK_BAR_GRADIENT
//        pixelizationControl = findViewById(R.id.add_seekbar);
//        LinearGradient test = new LinearGradient(0.f, 0.f, 300.f, 0.0f,
//                Color.BLACK, Color.RED, Shader.TileMode.CLAMP);
//        ShapeDrawable shape = new ShapeDrawable(new RectShape());
//        shape.getPaint().setShader(test);
//        pixelizationControl.setProgressDrawable(shape);
        //CAMERA
        Log.i("C", String.valueOf(checkCameraHardware(this)));
        android.hardware.Camera.CameraInfo cameraInfo1 = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.CameraInfo cameraInfo2 = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, cameraInfo1);
        android.hardware.Camera.getCameraInfo(1, cameraInfo2);
        backCamera = getCameraInstanceById(0);
        cameraParameters = backCamera.getParameters();



        // Create our Preview view and set it as the content of our activity.
        backCameraPreview = new CameraPreview(this, backCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(backCameraPreview);
    }
    /** Check if this device has a backCamera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a backCamera
            return true;
        } else {
            // no backCamera on this device
            return false;
        }
    }
    /** A safe way to get an instance of the Camera object. */
    public static android.hardware.Camera getCameraInstance(){
        android.hardware.Camera c = null;
        try {
            c = android.hardware.Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if backCamera is unavailable
    }
    public static android.hardware.Camera getCameraInstanceById(int id){
        android.hardware.Camera c = null;
        try {
            c = android.hardware.Camera.open(id); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if backCamera is unavailable
    }
    public void closeClick(View v) {
        onBackPressed();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (backCamera != null) {
            backCamera.release();
            backCamera = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.camera_change_btn:
                changeCamera();
                break;
        }
    }

    private void changeCamera() {
        FrameLayout preview = findViewById(R.id.camera_preview);
        if (frontCamera == null) {
            backCamera.stopPreview();
            backCamera.release();
            preview.removeAllViews();
            backCamera = null;
            backCameraPreview = null;
            frontCamera = getCameraInstanceById(1);
            frontCameraPreview = new CameraPreview(this, frontCamera);
            preview.addView(frontCameraPreview);
        } else {
            if (backCamera == null) {
                frontCamera.stopPreview();
                frontCamera.release();
                preview.removeAllViews();
                frontCamera = null;
                frontCameraPreview = null;
                backCamera = getCameraInstanceById(0);
                backCameraPreview = new CameraPreview(this, backCamera);
                preview.addView(backCameraPreview);
            }
        }
    }
}
