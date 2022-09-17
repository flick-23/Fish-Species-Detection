package com.example.fishapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MoreInfoActivity extends AppCompatActivity {

    private static String fishDesc, fishName, fishImgUrl;
    private FirebaseDatabase database;
    private TextView desc, fname;
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_more_info);


        desc = findViewById(R.id.description);
        fname = findViewById(R.id.fishNameView);
        imageView = findViewById(R.id.fishImgView);

        Intent intent = getIntent();
        fishDesc = intent.getStringExtra("desc");
        fishName = intent.getStringExtra("fishName");
        fishImgUrl = intent.getStringExtra("imgUrl");

        ImageLoadTask imgLoad = new ImageLoadTask(fishImgUrl, imageView);
        imgLoad.execute();

        desc.setText(fishDesc);
        fname.setText(fishName);


    }
}