package com.live.communityskyeye;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

public class SubScene extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    private static final String TAG = SubScene.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // 实时视频流的编解码
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    private Button mCaptureBtn, mShootPhotoModeBtn, mRecordVideoModeBtn;
    private ToggleButton mRecordBtn;
    private TextView recordingTime;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sub_scene);

        Intent intent = getIntent();
        setTitle(intent.getStringExtra("sceneName"));

        handler = new Handler();

        initUI();
        //用于接收相机实时取景的原始H264视频数据的回调
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };

        Camera camera = MyOwnApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemStste) {
                    if (cameraSystemStste != null) {
                        //获取相机记录时间
                        int recordTime = cameraSystemStste.getCurrentVideoRecordingTimeInSeconds();
                        int hours = recordTime / 3600;
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;
                        final String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                        final boolean isVideoRecord = cameraSystemStste.isRecording();

                        SubScene.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recordingTime.setText(timeString);
                            }
                        });
                        /*
                        更新时间记录框显示状态，如果开始记录就显示时间进度，否则不显示
                         */
                        if (isVideoRecord) {
                            recordingTime.setVisibility(View.VISIBLE);
                        } else {
                            recordingTime.setVisibility(View.INVISIBLE);
                        }
                    }

                }
            });

        }

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();
        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }


    private void initUI() {
        //初始化视频流界面
        mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);

        recordingTime = (TextView) findViewById(R.id.timer);
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mShootPhotoModeBtn = (Button) findViewById(R.id.btn_shoot_photo_mode);
        mRecordVideoModeBtn = (Button) findViewById(R.id.btn_record_video_mode);

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }

        mCaptureBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
        mShootPhotoModeBtn.setOnClickListener(this);
        mRecordVideoModeBtn.setOnClickListener(this);

        recordingTime.setVisibility(View.INVISIBLE);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recordingTime.setVisibility(View.VISIBLE);
                    startRecord();
                } else {
                    recordingTime.setVisibility(View.INVISIBLE);
                    stopRecord();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
               // switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                captureAction();
                break;
            case R.id.btn_shoot_photo_mode:
                switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                break;
            case R.id.btn_record_video_mode:
                switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
                break;
            default:
                break;
        }
    }








    protected void onProductChange() {
        initPreviewer();
    }


    public void initPreviewer() {
        BaseProduct product = MyOwnApplication.getProductInstance();
        if (product == null || product.isConnected()) {
            Toast.makeText(SubScene.this, "无连接", Toast.LENGTH_SHORT).show();
        } else {
            if (mVideoSurface != null) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(mReceivedVideoDataCallBack);
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = MyOwnApplication.getCameraInstance();
        if (camera != null) {
            // 重置回调
            VideoFeeder.getInstance().getPrimaryVideoFeed().setCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode){
        Camera camera =MyOwnApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Toast.makeText(SubScene.this, "转换相机模式成功",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SubScene.this, djiError.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    // 开始记录
    private void startRecord() {

        final Camera camera = MyOwnApplication.getCameraInstance();
        if (camera != null) {
            camera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Toast.makeText(SubScene.this, "记录视频：成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SubScene.this, djiError.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord() {

        Camera camera = MyOwnApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {

                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        Toast.makeText(SubScene.this, "停止记录：成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SubScene.this, djiError.getDescription(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }); // Execute the stopRecordVideo API
        }

    }


    // 捕获照片方法
    private void captureAction(){
        final Camera camera = MyOwnApplication.getCameraInstance();
        if (camera != null) {
            //设置相机捕获照片模式为单张拍照模式
            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE;
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            Toast.makeText(SubScene.this, "获取照片：成功",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SubScene.this, djiError.getDescription(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }, 800);
                    }
                }
            });
        }
    }






}
