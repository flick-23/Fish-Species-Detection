package com.example.fishapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    public void goBtnClick(View view){
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
    }

    public void detailsBtn(View view){
        Intent intent = new Intent(this, ShowRegionActivity.class);
        startActivity(intent);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);



    }
}