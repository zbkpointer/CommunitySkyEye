package com.live.communityskyeye;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.sql.SQLOutput;
import java.util.List;

import static android.R.id.list;

public class  SceneActivity extends AppCompatActivity {



    int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
       // System.out.println(DataSupport.count(Place.class));
        Connector.getDatabase();
        List<Place>  places = DataSupport.findAll(Place.class);
        String[] save_place= new String[places.size()];

        for (Place place : places){
            save_place[counter] = place.getPlace_name();
            counter++;
        }



        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                SceneActivity.this, android.R.layout.simple_list_item_1,save_place);
        ListView listview = (ListView) findViewById(R.id.secene_list) ;
        listview.setAdapter(adapter);

        findViewById(R.id.add_scene).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SceneActivity.this,add_surface.class));

            }
        });




        //设置长按监听器
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                //定义对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(SceneActivity.this);
                builder.setMessage("确定删除?");
                builder.setTitle("提示");
             //   System.out.println(DataSupport.count(Place.class));
                System.out.println("position is"+position);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("position is"+position);
                        Connector.getDatabase();
                        DataSupport.delete(Place.class,position);
                        System.out.println(DataSupport.count(Place.class));

                        //adapter.notifyDataSetChanged();
                        Toast.makeText(getBaseContext(),"已删除",Toast.LENGTH_SHORT).show();
                    }

                });

                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.create().show();
               // startActivity(new Intent(SceneActivity.this,SceneActivity.class));

                return false;
            }
        });



    }











}
