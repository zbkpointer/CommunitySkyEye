package com.live.communityskyeye;

import android.animation.TimeAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class UIActivity extends AppCompatActivity implements  View.OnClickListener{
    private Button startRecord;
    private Button load;
    private boolean isRecord = false;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private double droneLocationLat1 = 0, droneLocationLng1 = 0;
    private FlightController mFlightController;

    private List<LatLng> latLngs = new ArrayList<>();

    private Handler mhandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /*
         当SDK版本超过22时，需要用一下代表确保大疆SDK运行良好
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.VIBRATE,
                            android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_WIFI_STATE,
                            android.Manifest.permission.WAKE_LOCK, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.CHANGE_WIFI_STATE, android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.SYSTEM_ALERT_WINDOW,
                            android.Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
     //   this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_ui);


        IntentFilter filter = new IntentFilter();
        filter.addAction(MyOwnApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        initView();

        //进行判断选择按钮
        if(isRecord ==true) {

            mhandler = new Handler();
            mhandler.post(new Runnable() {
                @Override
                public void run() {

                    mhandler.postDelayed(this, 1000);//延时1分钟进行取点

                }
            });
        }
    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
    }

    private void initFlightController() {

        BaseProduct product = MyOwnApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {

            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(FlightControllerState
                                                     djiFlightControllerCurrentState) {
                            droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            //转换坐标系
                            //因为高德使用的是火星坐标系，
                            //所以需要进行原始坐标转换火星坐标，才能对应
                            SimpleCoodinates pos1 = WgsGcjConverter.wgs84ToGcj02(droneLocationLat, droneLocationLng);
                            droneLocationLat1 = pos1.getLat();
                            droneLocationLng1 = pos1.getLon();
                            //     LatLngs_drone.add(pos1);

                            /*
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    aMap.addPolyline(new PolylineOptions().addAll(LatLngs_drone).width(10).color(Color.argb(255,0,0,255)));
                                }
                            });
                            */
                        }
                    });

        }
    }

    protected void onResume(){
        super.onResume();
        initFlightController();
        /*
        onProductChange();
        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
        */

    }

    private void initView(){
        startRecord = (Button)findViewById(R.id.start_record_button);
        load = (Button)findViewById(R.id.load_button);

        startRecord.setOnClickListener(this);
        load.setOnClickListener(this);
    }

    private void enableDisableRecord(){
        if (isRecord == false) {
            isRecord = true;
            startRecord.setText("正在记录");

            startRecord.setBackgroundColor(Color.rgb(0,128,0));
        }else{
            isRecord = false;
            startRecord.setText("停止记录");
           startRecord.setBackgroundColor(Color.rgb(220,20,60));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.start_record_button:{
                enableDisableRecord();
                break;
            }



            default:
                break;
        }

    }

}
