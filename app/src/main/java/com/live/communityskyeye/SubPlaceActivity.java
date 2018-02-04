package com.live.communityskyeye;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubPlaceActivity extends AppCompatActivity implements View.OnClickListener {

    private Button fly_Bt;
    private TextView mTvSubPlaceName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_place);


        mTvSubPlaceName = (TextView)findViewById(R.id.sub_place_name);
        Intent intent = getIntent();
        mTvSubPlaceName.setText(intent.getStringExtra("placeName"));

        initView();


    }

    private void initView(){
        fly_Bt  = (Button)findViewById(R.id.fly_button);

        fly_Bt.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.fly_button: {
                Intent intent = new Intent(SubPlaceActivity.this, UIActivity.class);
                startActivity(intent);
                break;
            }
            default:
                break;

        }
    }
}

