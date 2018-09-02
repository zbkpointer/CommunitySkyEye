package com.live.communityskyeye;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import dji.common.realname.AircraftBindingState;
import dji.common.realname.AppActivationState;
import dji.sdk.realname.AppActivationManager;
import dji.sdk.sdkmanager.DJISDKManager;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button mediaPlay;
    private Button sceneMode;
    private Button optimizingMmode;
    private Button login;


    private AppActivationManager appActivationManager;
    private AircraftBindingState.AircraftBindingStateListener bindingStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initEvent();

    }


    private void initUI(){
        mediaPlay = (Button)findViewById(R.id.mediaPlay);
        sceneMode = (Button)findViewById(R.id.scene_mode);
        optimizingMmode = (Button)findViewById(R.id.optimizing_mode);
        login = (Button)findViewById(R.id.login);

    }

    private void initEvent(){
        mediaPlay.setOnClickListener(this);
        sceneMode.setOnClickListener(this);
        optimizingMmode.setOnClickListener(this);
        login.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.mediaPlay:{
                //启动媒体管理界面
                startActivity(new Intent(MainActivity.this,MediaManageActivity.class));
                break;
            }
            case R.id.scene_mode:
            {
                //启动场景模式
                startActivity(new Intent(MainActivity.this,PlaceActivity.class));
                break;
            }
            case R.id.optimizing_mode:
            {
                //启动自由模式
                startActivity(new Intent(MainActivity.this,WayPointActivity.class));
                break;
            }
            case R.id.login:{
                //启动自由模式
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                break;
            }
            default:
            {
                break;
            }

        }


    }
}
