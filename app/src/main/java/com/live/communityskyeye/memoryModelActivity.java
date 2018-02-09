package com.live.communityskyeye;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.model.LatLng;

public class memoryModelActivity extends AppCompatActivity implements View.OnClickListener, AMap.OnMapClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_model);

    }



    @Override
    public void onMapClick(LatLng latLng) {

    }



    @Override
    public void onClick(View view) {

    }


}
