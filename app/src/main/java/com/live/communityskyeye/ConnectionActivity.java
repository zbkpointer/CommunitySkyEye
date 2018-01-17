package com.live.communityskyeye;

import android.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.realname.AppActivationState;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;

public class ConnectionActivity extends Activity implements View.OnClickListener {
    private static final String TAG = ConnectionActivity.class.getName();

    private TextView mTextConnectionStatus;
    private TextView mTextProduct;

    private Button mBtnopen;


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
        setContentView(R.layout.activity_connection);
        initUI();
        //注册广播接收机接收飞行器接入状态的变化
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyOwnApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
    }
    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
    }
    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }
    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }
    private void initUI() {
        mTextConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
        mTextProduct = (TextView) findViewById(R.id.text_product_info);

        mBtnopen = (Button)findViewById(R.id.btn_open);
        mBtnopen.setOnClickListener(this);
        mBtnopen.setEnabled(false);
    }



    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshSDKRelativeUI();
        }
    };

    private void refreshSDKRelativeUI() {
        BaseProduct mProduct =MyOwnApplication.getProductInstance();

        if (null != mProduct && mProduct.isConnected()) {
            Log.v(TAG, "refreshSDK: True");
            mBtnopen.setEnabled(true);

            String str = mProduct instanceof Aircraft ? "飞机":"手持产品";
            mTextConnectionStatus.setText("状态: " + str + " 已连接");

            if (null != mProduct.getModel()) {
                mTextProduct.setText("飞机型号: " + mProduct.getModel().getDisplayName());
            } else {
                mTextProduct.setText(R.string.product_information);
            }

        } else {
            Log.v(TAG, "refreshSDK: False");
            mBtnopen.setEnabled(false);
            mTextProduct.setText(R.string.product_information);
            mTextConnectionStatus.setText(R.string.connection_loose);
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }


    }
}
