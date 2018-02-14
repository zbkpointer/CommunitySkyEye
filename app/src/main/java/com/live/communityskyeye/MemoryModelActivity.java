package com.live.communityskyeye;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;

public class MemoryModelActivity extends AppCompatActivity  {

    private MapView mapView;
    private AMap aMap;
    private Marker droneMarker = null;
    private FlightController mFlightController;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private double droneLocationLat1 = 0, droneLocationLng1 = 0;

    private MySQLite mySQLite;
    private SQLiteDatabase mDbWriter;

    private long startTime = 0 ;
    private long endTime = 0;

    private List<LatLng> mList = new ArrayList<>();//编程必须加后边的实例化
    private List<Float> mAltList = new ArrayList<>();

  //  private List<Double> mLagData = new ArrayList<>();
 //   private List<Double> mLogData = new ArrayList<>();



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
        //画出已经飞过的航点
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                aMap.addPolyline(new PolylineOptions().addAll(mList).width(12).color(Color.argb(255,0,255,0)));

            }
        });


    }






    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
        initFlightController();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mySQLite.close();
        aMap.clear();
    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
           // aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

        cameraUpdate();

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
                            updateDroneLocation();

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


    private void initData(){
        mySQLite = new MySQLite(this);
        mDbWriter = mySQLite.getWritableDatabase();

        Cursor mCursor = mDbWriter.rawQuery("select * from Lag_Log where _Time>=? and _Time<=?",new String[]{String.valueOf(startTime),String.valueOf(endTime)});

        if(mCursor.moveToFirst()) {
            mList.add(new LatLng(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))),
                   Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log"))) ));
            mAltList.add(Float.valueOf(mCursor.getString(mCursor.getColumnIndex("_Alt"))));
        }

        while(mCursor.moveToNext()){
            mList.add(new LatLng(Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Lag"))),
                   Double.valueOf(mCursor.getString(mCursor.getColumnIndex("_Log")))));
            mAltList.add(Float.valueOf(mCursor.getString(mCursor.getColumnIndex("_Alt"))));
        }
        for (LatLng latLng:mList
            ) {
           System.out.println(latLng.latitude+"-->"+latLng.longitude);
        }
        for (Float altitude:mAltList
             ) {
            System.out.println(altitude);
        }


    }
    private void cameraUpdate(){

        LatLng pos = new LatLng(mList.get(0).latitude, mList.get(0).longitude);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        aMap.moveCamera(cu);

    }

    private void updateDroneLocation(){

        LatLng pos = new LatLng(droneLocationLat1,droneLocationLng1);
        //   LatLngs_drone.add(pos);
        // LatLngs.add(pos);  !!!坐标更新频次为10Hz,所以第一个点标记不能放在这
        //创建飞机标记点
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat1, droneLocationLng1)) {
                    droneMarker = aMap.addMarker(markerOptions);
                }
            }
        });
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }







}
