package com.live.communityskyeye;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.litepal.tablemanager.Connector;

public class add_surface extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_surface);
        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) findViewById(R.id.add_place);
                //Log.d("你好",et.getText().toString());
               // Connector.getDatabase();
               // Place place = new Place();
               // place.setPlace_name(et.getText().toString());
               // place.save();
               // finish();
                Intent intent = new Intent();
                intent.setClass(add_surface.this,SceneActivity.class);
                intent.putExtra("et",et.getText().toString());
                setResult(2,intent);
                finish();
                //startActivity(new Intent(add_surface.this,SceneActivity.class));
                //System.exit(0);

            }
        });


    }

}
