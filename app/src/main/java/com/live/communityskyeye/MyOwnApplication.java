package com.live.communityskyeye;

import android.app.Application;
import android.content.SharedPreferences;

import org.litepal.LitePal;
import org.litepal.tablemanager.Connector;

/**
 * Created by Administrator on 2017/12/4.
 */

public class MyOwnApplication extends Application {
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

        }


    }
}
