package com.live.communityskyeye;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

public class  SceneActivity extends AppCompatActivity {

    private  List<Place> places = DataSupport.findAll(Place.class);
    private  ListView listview = null;
    private  ArrayAdapter<String> adapter = null;
    // private  List<Place> places = new ArrayList<>();
    int counter = 0;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);

        String[] save_place = new String[places.size()];
        for (Place place : places) {
            save_place[counter] = place.getPlace_name();
            counter++;
        }
        adapter = new ArrayAdapter<>(
                SceneActivity.this, android.R.layout.simple_list_item_1, save_place);
        listview = (ListView) findViewById(R.id.secene_list);
        listview.setAdapter(adapter);


        findViewById(R.id.add_scene).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // startActivity(new Intent(SceneActivity.this,add_surface.class));
                Intent intent = new Intent(SceneActivity.this, add_surface.class);
                startActivityForResult(intent,1);


            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("sceneName", (String) listview.getItemAtPosition(position));
                intent.setClass(SceneActivity.this, UIActivity.class);
                startActivity(intent);
            }
        });

        //已经发现问题，数据库中的id对应不了列表中的id
        //设置长按监听器
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                          final int position, long id) {
                //定义对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(SceneActivity.this);
                builder.setMessage("确定删除?");
                builder.setTitle("提示");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {

                        //解决问题
                        DataSupport.deleteAll(Place.class,"place_name = ?",
                                (String) listview.getItemAtPosition(position));
                       // Connector.getDatabase();
                        adapter.notifyDataSetChanged();

                        Toast.makeText(getBaseContext(), "已删除", Toast.LENGTH_SHORT).show();
                    }

                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
                // startActivity(new Intent(SceneActivity.this,SceneActivity.class));
                //长按与短按的区别
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==1){
            if(resultCode==2){
                String et = data.getStringExtra("et");
                // Connector.getDatabase();
                Place place = new Place();
                place.setPlace_name(et);
                place.save();
                System.out.println("获得内容是:"+et);
               // notify();
            }
        }
    }


}