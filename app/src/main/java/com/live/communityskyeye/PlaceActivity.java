package com.live.communityskyeye;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceActivity extends AppCompatActivity implements View.OnClickListener {

    private List<Map<String, String>> mStringList = new ArrayList<>();
    private ListView mListView;
    private Button mBtn_insert;
    private Button mBtn_query;
    private EditText mEt_placeName;
    private EditText mEt_singer;
    private EditText mEt_input;
  //  private EditText mEt_dialog_songName;
  //  private EditText mEt_dialog_singer;
  //  private Toolbar mToolbar;
    private SimpleCursorAdapter mSimpleCursorAdapter;
    private SQLiteDatabase mDbWriter;
    private SQLiteDatabase mDbReader;
    private MySQLite mMySQLite;
    private String TAG = "TAG";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
      //  initData();
        initView();
        initEvent();

        //单击修改item
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int position, long l) {

                //通过游标选择点击的内容
                Cursor mCursor = mSimpleCursorAdapter.getCursor();
                mCursor.moveToPosition(position);
                String placeName = mCursor.getString(mCursor.getColumnIndex("placename"));

                Intent intent = new Intent();

                intent.putExtra("placeName",placeName);
                intent.setClass(PlaceActivity.this,SubPlaceActivity.class);
                startActivity(intent);

            }
        });


        //长按删除item
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                new AlertDialog.Builder(PlaceActivity.this)
                        .setTitle("提示")
                        .setMessage("是否删除该项")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteData(position);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
                return true;
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMySQLite.close();
    }

    private void initView() {
     //   mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mListView = (ListView) findViewById(R.id.myListview);
        mBtn_insert = (Button) findViewById(R.id.btn_insert);
        mBtn_query = (Button) findViewById(R.id.btn_query);
        mEt_placeName = (EditText) findViewById(R.id.et_placename);
        mEt_singer = (EditText) findViewById(R.id.et_singer);
        mEt_input = (EditText) findViewById(R.id.et_query);

    }

    private void initEvent() {
      //  setSupportActionBar(mToolbar);
        mBtn_insert.setOnClickListener(this);
        mBtn_query.setOnClickListener(this);

        mMySQLite = new MySQLite(this);
        mDbWriter = mMySQLite.getWritableDatabase();
        mDbReader = mMySQLite.getReadableDatabase();

        mSimpleCursorAdapter = new SimpleCursorAdapter(PlaceActivity.this, R.layout.listview_sql_item, null,
                new String[]{"placename"}, new int[]{R.id.placename}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mListView.setAdapter(mSimpleCursorAdapter);     //给ListView设置适配器
        refreshListview();      //自定义的方法，用于当数据列表改变时刷新ListView

    }

    //刷新数据列表
    public  void refreshListview() {
        Cursor mCursor = mDbWriter.query("place_msg", null, null, null, null, null, null);
        mSimpleCursorAdapter.changeCursor(mCursor);
    }

    //增
    public void insertData() {
        ContentValues mContentValues = new ContentValues();
        mContentValues.put("placename", mEt_placeName.getText().toString().trim());
        //  mContentValues.put("singer", this.getTime().trim());
        //   mContentValues.put("singer", mEt_singer.getText().toString().trim());
        mDbWriter.insert("place_msg", null, mContentValues);
        refreshListview();
    }

    //删
    public void deleteData(int positon) {
        Cursor mCursor = mSimpleCursorAdapter.getCursor();
        mCursor.moveToPosition(positon);
        int itemId = mCursor.getInt(mCursor.getColumnIndex("_id"));
        mDbWriter.delete("place_msg", "_id=?", new String[]{itemId + ""});
        refreshListview();
    }
/*
    //改
    public void updateData(int positon) {
        Cursor mCursor = mSimpleCursorAdapter.getCursor();
        mCursor.moveToPosition(positon);
        int itemId = mCursor.getInt(mCursor.getColumnIndex("_id"));
        ContentValues mContentValues = new ContentValues();
        mContentValues.put("placename", mEt_dialog_songName.getText().toString().trim());
        mContentValues.put("singer", mEt_dialog_singer.getText().toString().trim());
        mDbWriter.update("place_msg", mContentValues, "_id=?", new String[]{itemId + ""});
        refreshListview();
    }
*/
    //查
    public void queryData() {
        String mInput = mEt_input.getText().toString().trim();
        //第二个参数是你需要查找的列
        //第三和第四个参数确定是从哪些行去查找第二个参数的列
        Cursor mCursor1 = mDbReader.query("place_msg", new String[]{"singer"}, "placename=?", new String[]{mInput}, null, null, null);
        Cursor mCursor2 = mDbReader.query("place_msg", new String[]{"placename"}, "singer=?", new String[]{mInput}, null, null, null);
        if (mCursor1.getCount() > 0) {
            mStringList.clear();        //清空List
            while (mCursor1.moveToNext()) {     //游标总是在查询到的上一行
                Map<String, String> mMap = new HashMap<>();
                String output_singer = mCursor1.getString(mCursor1.getColumnIndex("singer"));
                mMap.put("tv1", mInput);
                mMap.put("tv2", output_singer);
                mStringList.add(mMap);
            }
            mCursor1.close();
        } else if (mCursor2.getCount() > 0) {
            mStringList.clear();        //清空List
            while (mCursor2.moveToNext()) {     //游标总是在查询到的上一行
                Map<String, String> mMap = new HashMap<>();
                String output_songname = mCursor2.getString(mCursor2.getColumnIndex("placename"));
                mMap.put("tv1", output_songname);
                mMap.put("tv2", mInput);
                mStringList.add(mMap);
            }
            mCursor2.close();
        } else {
            mStringList.clear();        //清空List
            Map<String, String> mMap = new HashMap<>();
            mMap.put("tv1", "未能查询到结果");
            mStringList.add(mMap);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //点击添加按钮
            case R.id.btn_insert:
                insertData();
                mEt_placeName.setText("");
                mEt_singer.setText("");
                break;
            //点击查询按钮
            case R.id.btn_query:
                queryData();
                mEt_input.setText("");      //清空输入框
                new AlertDialog.Builder(PlaceActivity.this)
                        .setTitle("查询结果")
                        .setAdapter(new SimpleAdapter(PlaceActivity.this, mStringList, R.layout.dialog_tv_layout, new String[]{"tv1", "tv2"}, new int[]{R.id.tv1_dialog, R.id.tv2_dialog}), null)
                        .setPositiveButton("确定", null)
                        .setCancelable(false)
                        .show();

                break;

        }
    }





}
