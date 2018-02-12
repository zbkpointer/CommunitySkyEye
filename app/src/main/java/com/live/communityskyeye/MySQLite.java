package com.live.communityskyeye;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/1/29.
 */

class MySQLite extends SQLiteOpenHelper {
    private Context mContext;

    MySQLite(Context context) {
        super(context, "music_msg.db", null, 1);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table music_msg(_id integer primary key autoincrement, singer varchar, songname varchar, url varchar)");
        sqLiteDatabase.execSQL("create table sub_place(_id integer primary key autoincrement, _placeName varchar, _placeDate varchar,_startTime Integer,_endTime Integer)");
        sqLiteDatabase.execSQL("create table Lag_Log(_id integer primary key autoincrement, _placeName varchar, _Lag varchar,_Log varchar,_Time Integer)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Toast.makeText(mContext, "onUpgrade", Toast.LENGTH_SHORT).show();
    }
}