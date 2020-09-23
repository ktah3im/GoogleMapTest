package com.example.googlemaptest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MarkerType extends AppCompatActivity {
    private RadioGroup radioGroup;
    private ImageView imageView;
    private Integer []photos = {R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_type);

        this.imageView = (ImageView) findViewById(R.id.imageView);
        this.radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        this.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int i) {
                RadioButton radioButton = (RadioButton) radioGroup.findViewById(i);
                int index = radioGroup.indexOfChild(radioButton);
                imageView.setImageResource(photos[index]);
            }
        });
    }
}