package com.live.communityskyeye;

import android.app.Application;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Created by Administrator on 2017/12/4.
 */

public class MyOwnApplication extends Application {
    public static final String FLAG_CONNECTION_CHANGE = "aircraft_connection_change";

    private static BaseProduct mProduct;
    private MySQLite mMySQLite;
    private SQLiteDatabase mDbWriter;
    private SQLiteDatabase mDbReader;
    public Handler mHandler;
    public MySQLite db = new MySQLite(this);

    /**
     * This function is used to get the instance of DJIBaseProduct.
     * If no product is connected, it returns null.
     */
    public static synchronized BaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getProduct();
        }
        return mProduct;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof HandHeld;
    }

    public static synchronized Camera getCameraInstance() {

        if (getProductInstance() == null) return null;

        Camera camera = null;

        if (getProductInstance() instanceof Aircraft){
            camera = ((Aircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof HandHeld) {
            camera = ((HandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);


        //判断程序是否是第一次执行
        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        //此处表示该应用程序专用
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        //此处表示如果key "isFirstRun"对应的value没有值则默认为true，
        //否则就把对应的value取出赋值给变量isFirstRun
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(isFirstRun) {
            editor.putBoolean("isFirstRun", false);
            //此处表示putBoolean(key, value)，
            //将value写入对应的key，而且是一一对应的
            editor.commit();
            //将isFirstRun写入editor中保存

            String[] scene_set = new String[]{"小区围墙","绿化","停车场","屋顶","行车道"};

            MySQLite db = new MySQLite(this);
            ContentValues  values= new ContentValues();


            for (String scene:scene_set) {
                values.put("placename", scene.trim());
                db.getWritableDatabase().insert("place_msg", null, values);
                values.clear();

            }
/*
            Connector.getDatabase();
            Place place1 = new Place();
            place1.setPlace_name("小区围墙");
            place1.save();


            Place place2 = new Place();
            place2.setPlace_name("绿化");
            place2.save();


            Place place3 = new Place();
            place3.setPlace_name("停车场");
            place3.save();

            Place place4 = new Place();
            place4.setPlace_name("屋顶");
            place4.save();

            Place place5 = new Place();
            place5.setPlace_name("行车道");
            place5.save();
*/
        }
        mHandler = new Handler(Looper.getMainLooper());

        //Check the permissions before registering the application for android system 6.0 above.
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (permissionCheck == 0 && permissionCheck2 == 0)) {
            //This is used to start SDK services and initiate SDK.
            DJISDKManager.getInstance().registerApp(this, mDJISDKManagerCallback);
        } else {
            Toast.makeText(getApplicationContext(), "Please check if the permission is granted.", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * When starting SDK services, an instance of interface DJISDKManager.DJISDKManagerCallback will be used to listen to
     * the SDK Registration result and the product changing.
     */
    private DJISDKManager.SDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.SDKManagerCallback() {

        //Listens to the SDK registration result
        @Override
        public void onRegister(DJIError error) {

            if(error == DJISDKError.REGISTRATION_SUCCESS) {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Register Success",
                                Toast.LENGTH_LONG).show();
                    }
                });

                DJISDKManager.getInstance().startConnectionToProduct();

            } else {

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Register sdk fails, check network is available",
                                Toast.LENGTH_LONG).show();
                    }
                });

            }
            Log.e("TAG", error.toString());
        }

        //Listens to the connected product changing, including two parts, component changing or product connection changing.
        @Override
        public void onProductChange(BaseProduct oldProduct, BaseProduct newProduct) {

            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setBaseProductListener(mDJIBaseProductListener);
            }

            notifyStatusChange();
        }
    };

    private BaseProduct.BaseProductListener mDJIBaseProductListener = new BaseProduct.BaseProductListener() {

        @Override
        public void onComponentChange(BaseProduct.ComponentKey key, BaseComponent oldComponent, BaseComponent newComponent) {

            if(newComponent != null) {
                newComponent.setComponentListener(mDJIComponentListener);
            }
            notifyStatusChange();
        }

        @Override
        public void onConnectivityChange(boolean isConnected) {

            notifyStatusChange();
        }

    };

    private BaseComponent.ComponentListener mDJIComponentListener = new BaseComponent.ComponentListener() {

        @Override
        public void onConnectivityChange(boolean isConnected) {
            notifyStatusChange();
        }

    };

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };



}
