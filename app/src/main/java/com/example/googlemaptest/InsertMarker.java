package com.example.googlemaptest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InsertMarker extends AppCompatActivity implements View.OnClickListener {
    Button btnBack, btnType, btnSwitch, btnData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_marker);

        btnBack=(Button)findViewById(R.id.btnBack);
        btnType=(Button)findViewById(R.id.btnType);
        btnSwitch=(Button)findViewById(R.id.btnSwitch);
        btnData=(Button)findViewById(R.id.btnData);

        btnBack.setOnClickListener(this);
        btnType.setOnClickListener(this);
        btnSwitch.setOnClickListener(this);
        btnData.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btnBack:
                finish();
                break;
            case R.id.btnType:
                Intent it = new Intent()    ;
                it.setClass(this, MarkerType.class);
                startActivity(it);
                break;
            case R.id.btnSwitch:

                break;
            case R.id.btnData:
                Intent it3 = new Intent()    ;
                it3.setClass(this, MarkerList.class);
                startActivity(it3);
                break;
        }

    }
}