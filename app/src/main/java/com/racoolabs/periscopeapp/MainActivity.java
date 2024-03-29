package com.racoolabs.periscopeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;



public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {


    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA};
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT

    private SurfaceView surfaceView;
    private CameraPreview mCameraPreview;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)


    private View mImageButtonFlashOnOff;
    private View Button_zoomIn;
    private View Button_zoomOut;
    private ImageView ImageView_highlight;
    private boolean mFlashOn;
    public AdView mAdView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mLayout = findViewById(R.id.layout_main);
        surfaceView = findViewById(R.id.camera_preview_main);


        bindingAndsetButton();
        setAutoFocus();


        // 런타임 퍼미션 완료될때 까지 화면에서 보이지 않게 해야합니다.
        surfaceView.setVisibility(View.GONE);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                startCamera();

            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                    Snackbar.make(mLayout, "Camera permission are required to run this app.",
                            Snackbar.LENGTH_INDEFINITE).setAction("Yes", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                                    PERMISSIONS_REQUEST_CODE);

                        }

                    }).show();


                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE);
                }

            }

        } else {

            final Snackbar snackbar = Snackbar.make(mLayout, "The device does not support the camera.",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    finish();
                }
            });
            snackbar.show();
        }

    }

    void bindingAndsetButton(){

        mImageButtonFlashOnOff = findViewById(R.id.Button_flash);
        Button_zoomIn = findViewById(R.id.Button_zoomIn);
        Button_zoomOut = findViewById(R.id.Button_zoomOut);
        ImageView_highlight = findViewById(R.id.ImageView_highlight);



        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mImageButtonFlashOnOff.setClickable(false);
        } else {
            mImageButtonFlashOnOff.setClickable(true);

            mImageButtonFlashOnOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFlashOn = mCameraPreview.flashlight();
                    ImageView_highlight.setImageResource(mFlashOn ? R.drawable.round_highlighton_white_48dp: R.drawable.round_highlightoff_white_48dp);
                }
            });

        }

        Button_zoomIn.setClickable(true);
        Button_zoomOut.setClickable(true);


        Button_zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPreview.zoomIn();

            }
        });

        Button_zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraPreview.zoomOut();
            }
        });
    }

    void  setAutoFocus() {

        surfaceView.setOnClickListener(new View.OnClickListener() { // 레이아웃 클릭 이벤트
            @Override
            public void onClick(View v) {

                mCameraPreview.setAutoFocus();

            }
        });

    }



    private void delayedFinish() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }



    void startCamera(){

        // Create the Preview view and set it as the content of this Activity.
        mCameraPreview = new CameraPreview(this, this, CAMERA_FACING, surfaceView);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {

            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                startCamera();
            }

            else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                    Snackbar.make(mLayout, "Permission denied. Please run the app again to allow the performance. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("Yes", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }

                    }).show();

                }else {

                    Snackbar.make(mLayout, "The setting must allow the operation. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("Yes", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }


    }


}




