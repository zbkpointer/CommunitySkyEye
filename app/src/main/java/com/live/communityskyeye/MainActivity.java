package com.live.communityskyeye;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import org.litepal.tablemanager.Connector;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.scene_mode).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //启动场景选择活动
                startActivity(new Intent(MainActivity.this,SceneActivity.class));

            }
        });
    }
}
