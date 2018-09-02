package com.live.communityskyeye;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.concurrent.ConcurrentHashMap;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;

public class MemoryModelActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MemoryModelActivity";
    private MapView mapView;
    private AMap aMap;
    private Marker droneMarker = null;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();

    private Button config, upload, start, stop;

    //航点规划
    public static WaypointMission.Builder waypointMissionBuilder;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;

    private List<Waypoint> waypointList = new ArrayList<>();
    private float mSpeed = 10.0f;

    private FlightController mFlightController;
    private double droneLocationLat = 181, droneLocationLng = 181;
    private double droneLocationLat1 = 0, droneLocationLng1 = 0;

    private MySQLite mySQLite;
    private SQLiteDatabase mDbWriter;

    private long startTime = 0 ;
    private long endTime = 0;

    private List<LatLng> mList = new ArrayList<>();//编程必须加后边的初始化
    private List<Float> mAltList = new ArrayList<>();
    private List<LatLng> mMapList = new ArrayList<>();//转换后的火星坐标用于绘制航线



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_model);

        mapView = (MapView) findViewById(R.id.Amap);
        mapView.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MyOwnApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        Intent intent = getIntent();
        startTime = intent.getLongExtra("startTime",0);
        endTime = intent.getLongExtra("endTime",0);
        initData();
        initUI();
        initMapView();




        addListener();
        initWayPointMission();

        //画出已经飞过的航点
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                aMap.addPolyline(new PolylineOptions().addAll(mMapList).width(12).color(Color.argb(255,0,255,0)));

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
        mapView.onDestroy();
        mList.clear();
        mAltList.clear();
        waypointList.clear();
        mySQLite.close();
        waypointMissionBuilder.waypointList(waypointList);
        unregisterReceiver(mReceiver);
        removeListener();
    }

    private void initUI(){
        config = (Button) findViewById(R.id._config);
        upload = (Button) findViewById(R.id._upload);
        start = (Button) findViewById(R.id._start);
        stop = (Button)findViewById(R.id._stop);

        config.setOnClickListener(this);
        upload.setOnClickListener(this);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
           // aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }

        cameraUpdate();

        int counter = 1;
        for (LatLng latLng:mList) {
            SimpleCoodinates pos = WgsGcjConverter.wgs84ToGcj02(latLng.latitude, latLng.longitude);
            LatLng latLng1 = new LatLng(pos.getLat(),pos.getLon());
            mMapList.add(latLng1);
            markWaypoint(latLng1,Integer.toString(counter));
            counter+=1;
        }




    }

    private void markWaypoint(LatLng point,String counter){
        //创建标记对象
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.title(counter);
        markerOptions.snippet("标记");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = aMap.addMarker(markerOptions);
        marker.showInfoWindow();
        mMarkers.put(mMarkers.size(), marker);
    }

    private void initWayPointMission(){

        for (int i = 0; i <mList.size() ; i++) {
            Waypoint mWaypoint = new Waypoint(mList.get(i).latitude, mList.get(i).longitude, mAltList.get(i));
            //添加航点到航点列表内;
            if (waypointMissionBuilder != null) {
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            } else {
                waypointMissionBuilder = new WaypointMission.Builder();
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            }

            }

            setResultToToast("添加航点任务成功");


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

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("执行结束: " + (error == null ? "成功!" : error.getDescription()));
        }
    };


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
        SimpleCoodinates pos1 = WgsGcjConverter.wgs84ToGcj02(mList.get(0).latitude, mList.get(0).longitude);
        LatLng pos = new LatLng(pos1.getLat(),pos1.getLon());
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

    private void setResultToToast(final String string){
        MemoryModelActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MemoryModelActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSettingDialog(){
        LinearLayout wayPointSettings = (LinearLayout)getLayoutInflater().inflate(R.layout.dialog_config, null);

        RadioGroup speed_RG = (RadioGroup) wayPointSettings.findViewById(R.id.speedRG);
        RadioGroup actionAfterFinished_RG = (RadioGroup) wayPointSettings.findViewById(R.id.actionFinished);
        RadioGroup heading_RG = (RadioGroup) wayPointSettings.findViewById(R.id.head);

        speed_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.lowSpeedSelec){
                    mSpeed = 3.0f;
                } else if (checkedId == R.id.midSpeedSelec){
                    mSpeed = 5.0f;
                } else if (checkedId == R.id.highSpeedSelec){
                    mSpeed = 10.0f;
                }
            }

        });

        actionAfterFinished_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select finish action");
                if (checkedId == R.id.finishNoneSelec){
                    mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
                } else if (checkedId == R.id.finishGoHomeSelec){
                    mFinishedAction = WaypointMissionFinishedAction.GO_HOME;
                } else if (checkedId == R.id.finishAutoLandingSelec){
                    mFinishedAction = WaypointMissionFinishedAction.AUTO_LAND;
                } else if (checkedId == R.id.finishToFirstSelec){
                    mFinishedAction = WaypointMissionFinishedAction.GO_FIRST_WAYPOINT;
                }
            }
        });

        heading_RG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d(TAG, "Select heading");

                if (checkedId == R.id.headingNextSelec) {
                    mHeadingMode = WaypointMissionHeadingMode.AUTO;
                } else if (checkedId == R.id.headingInitDirecSelec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_INITIAL_DIRECTION;
                } else if (checkedId == R.id.headingRCSelec) {
                    mHeadingMode = WaypointMissionHeadingMode.CONTROL_BY_REMOTE_CONTROLLER;
                } else if (checkedId == R.id.headingWPSelec) {
                    mHeadingMode = WaypointMissionHeadingMode.USING_WAYPOINT_HEADING;
                }
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("")
                .setView(wayPointSettings)
                .setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                        Log.e(TAG,"speed "+mSpeed);
                        Log.e(TAG, "mFinishedAction "+mFinishedAction);
                        Log.e(TAG, "mHeadingMode "+mHeadingMode);
                        configWayPointMission();
                    }

                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }

                })
                .setCancelable(false)
                .create()
                .show();
    }


    private void configWayPointMission(){

        if (waypointMissionBuilder == null){

            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }else
        {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("加载航点任务成功");
        } else {
            setResultToToast("加载航点任务失败 " + error.getDescription());
        }


    }

    private void uploadWayPointMission(){

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("任务上传成功!");
                } else {
                    setResultToToast("任务上传失败, 错误: " + error.getDescription() + " 重试...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });

    }

    private void startWaypointMission(){

        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("任务开始: " + (error == null ? "成功" : error.getDescription()));
            }
        });

    }

    private void stopWaypointMission(){

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("任务停止: " + (error == null ? "成功" : error.getDescription()));
            }
        });

    }


    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id._config:{
                showSettingDialog();
                break;
            }
            case R.id._upload:{
                uploadWayPointMission();
                break;
            }
            case R.id._start:{
                startWaypointMission();
                break;
            }
            case R.id._stop:{
                stopWaypointMission();
                break;
            }
            default:{
                break;
            }

        }
    }
}
