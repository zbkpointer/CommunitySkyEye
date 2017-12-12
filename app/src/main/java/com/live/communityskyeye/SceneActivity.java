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

    List<Place>  places = DataSupport.findAll(Place.class);
    private ListView listview = null;
   // private  List<Place> places = new ArrayList<>();
    int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
       // System.out.println(DataSupport.count(Place.class));
       // initScene();
        //Connector.getDatabase();
        String[] save_place= new String[places.size()];
       // System.out.println(DataSupport.count(Place.class));
        for (Place place : places){
           save_place[counter] = place.getPlace_name();
         counter++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                SceneActivity.this, android.R.layout.simple_list_item_1,save_place);
        listview = (ListView) findViewById(R.id.secene_list) ;
        listview.setAdapter(adapter);

        findViewById(R.id.add_scene).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SceneActivity.this,add_surface.class));

            }
        });

       listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Intent intent = new Intent();
               intent.putExtra("sceneName", (String) listview.getItemAtPosition(position));
               intent.setClass(SceneActivity.this,SubScene.class);
              // System.out.println((String)listview.getItemAtPosition(position));
               startActivity(intent);
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
                System.out.println(DataSupport.count(Place.class));
             //   System.out.println(DataSupport.count(Place.class));
                System.out.println("position is"+position);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("position is"+position);
                        Connector.getDatabase();
                        DataSupport.delete(Place.class,position+1);
                       // DataSupport.update(Place.class);
                       // System.out.println(DataSupport.count(Place.class));

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
                //长按与短按的区别
                return true;
            }
        });



    }

    public void initScene(){
        Connector.getDatabase();
        List<Place> places = DataSupport.findAll(Place.class);
        //List<Place> places = new ArrayList<>();
        String[] save_place= new String[places.size()];
        for (Place place : places){
            save_place[counter] = place.getPlace_name();
            counter++;
        }

    }









}
