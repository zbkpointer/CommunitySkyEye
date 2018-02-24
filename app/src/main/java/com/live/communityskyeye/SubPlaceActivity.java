package com.live.communityskyeye;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SubPlaceActivity extends AppCompatActivity implements View.OnClickListener {

    private Button fly_Bt;
    private TextView mTvSubPlaceName;
    private ListView mListView;

    private MySQLite mySQLite;
    private SimpleCursorAdapter mSimpleCursorAdapter;
    private SQLiteDatabase mDbWriter;
    private SQLiteDatabase mDbReader;

    private String subPlaceName = null ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_place);


        mTvSubPlaceName = (TextView)findViewById(R.id.sub_place_name);
        Intent intent = getIntent();
        mTvSubPlaceName.setText(intent.getStringExtra("placeName"));
        subPlaceName = intent.getStringExtra("placeName");

        initView();
        initEvent();

        //单击修改item
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, final int position, long l) {

                //通过游标选择点击的内容
                Cursor mCursor = mSimpleCursorAdapter.getCursor();
                mCursor.moveToPosition(position);
                long startTime = mCursor.getLong(mCursor.getColumnIndex("_startTime"));
                System.out.println(startTime);
                long endTime = mCursor.getLong(mCursor.getColumnIndex("_endTime"));
                System.out.println(endTime);

                Intent intent = new Intent();
                //不同活动之间传递数值
                intent.putExtra("startTime",startTime);
                intent.putExtra("endTime",endTime);
                intent.setClass(SubPlaceActivity.this,MemoryModelActivity.class);
                startActivity(intent);

            }
        });

        //长按删除item
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                new AlertDialog.Builder(SubPlaceActivity.this)
                        .setTitle("提示")
                        .setMessage("是否删除该项")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteData(position);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
                return true;
            }
        });



    }

    @Override
    protected void onResume(){
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mySQLite.close();
    }

    private void initView(){
        fly_Bt  = (Button)findViewById(R.id.fly_button);
        mListView = (ListView)findViewById(R.id.subPlaceListview);


    }

    private void  initEvent(){
        fly_Bt.setOnClickListener(this);

        mySQLite = new MySQLite(this);

        mDbWriter = mySQLite.getWritableDatabase();
        mDbReader = mySQLite.getReadableDatabase();
     //   Cursor mCursor = mDbWriter.query("sub_place", null, null, null, null, null, null);
      //  System.out.println(mCursor.getLong(mCursor.getColumnIndex("_startTime")));

        mSimpleCursorAdapter = new SimpleCursorAdapter(SubPlaceActivity.this, R.layout.listview_sql_item, null,
                new String[]{"_placeName", "_placeDate"}, new int[]{R.id.placename, R.id.singer}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        mListView.setAdapter(mSimpleCursorAdapter);     //给ListView设置适配器
        refreshListview();      //自定义的方法，用于当数据列表改变时刷新ListView

    }




    //刷新数据列表
    public  void refreshListview() {

        Cursor mCursor = mDbWriter.query("sub_place", null, "_placeName=?", new String[]{subPlaceName}, null, null, null);
        mSimpleCursorAdapter.changeCursor(mCursor);
        }


    //删
    public void deleteData(int positon) {
        Cursor mCursor = mSimpleCursorAdapter.getCursor();
        mCursor.moveToPosition(positon);
        int itemId = mCursor.getInt(mCursor.getColumnIndex("_id"));
        mDbWriter.delete("sub_place", "_id=?", new String[]{itemId + ""});
        refreshListview();
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.fly_button: {

                Intent intent = new Intent(SubPlaceActivity.this, UIActivity.class);
                intent.putExtra("subPlaceName",mTvSubPlaceName.getText());
                startActivity(intent);
                break;
            }
            default:
                break;

        }
    }
}

