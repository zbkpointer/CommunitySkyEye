package com.live.communityskyeye;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryModelActivity extends AppCompatActivity  {

    private MapView mapView;
    private AMap aMap;

    private MySQLite mySQLite;
    private SQLiteDatabase mDbWriter;

    private long startTime = 0 ;
    private long endTime = 0;

    private List<LatLng> mList = new ArrayList<>();//编程必须加后边的实例化

    private List<Double> mLagData = new ArrayList<>();
    private List<Double> mLogData = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_model);

        mapView = (MapView) findViewById(R.id.Amap);
        mapView.onCreate(savedInstanceState);

        Intent intent = getIntent();
        startTime = intent.getLongExtra("startTime",0);
        endTime = intent.getLongExtra("endTime",0);
        initData();
        initMapView();


    }






    @Override
    protected void onResume(){
        super.onResume();
      //  mapView.onResume();
      //  initFlightController();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mySQLite.close();
    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
           // aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

        LatLng wuhan = new LatLng(30.518519,114.399421);
        aMap.addMarker(new MarkerOptions()
                .position(wuhan)
                .title("Hi>>>")
                .snippet("欢迎使用自由航点设置管理系统")).showInfoWindow();
        aMap.moveCamera(CameraUpdateFactory.newLatLng(wuhan));
    }



    private void initData(){
        mySQLite = new MySQLite(this);
        mDbWriter = mySQLite.getWritableDatabase();
      //  LatLng latLng = new LatLng(0,0);

        Cursor mCursor = mDbWriter.rawQuery("select * from Lag_Log where _Time>=? and _Time<=?",new String[]{String.valueOf(startTime),String.valueOf(endTime)});
     //   System.out.println(mCursor.getCount());
        if(mCursor.moveToFirst()) {
            mList.add(new LatLng(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))),
                   Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log"))) ));
        //   mLagData.add(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))));
        //    mLogData.add(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log"))));

          // System.out.println(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log"))));
        }

        while(mCursor.moveToNext()){
            mList.add(new LatLng(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))),
                   Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log")))));
          //    mLagData.add(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))));
          //   mLogData.add(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log"))));
            // System.out.println(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))));
           // System.out.println(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log"))));
        }
        for (LatLng latLng:mList
            ) {
           System.out.println(latLng.latitude+"-->"+latLng.longitude);
        }


    }



}
