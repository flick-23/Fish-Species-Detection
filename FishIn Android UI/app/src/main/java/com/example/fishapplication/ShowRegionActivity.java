package com.example.fishapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ShowRegionActivity extends AppCompatActivity {
    FirebaseDatabase database;
    ListView myList;
    ArrayList<String> values;
    ArrayAdapter<String> arrayAdp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_show_region);

        database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("restrictedDatabase");

        myList = findViewById(R.id.listview);
        myList.setBackgroundResource(R.drawable.newloader);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterable<DataSnapshot> dataSnapshots= snapshot.getChildren();
                values = new ArrayList<String>();
                for(DataSnapshot snap : dataSnapshots){
                    String value = snap.getValue(String.class);
                    //Change the Format
                    String[] valArr = value.split(",");
                    value = "";
                    value+="Specie : "+valArr[0]+"\n\nREGION\nLongitude : "+valArr[1]+"\nLattitude : "+valArr[2];

                    values.add(value);
                    arrayAdp = new ArrayAdapter<String>(ShowRegionActivity.this,
                            android.R.layout.simple_list_item_1, values);

                    myList.setAdapter(arrayAdp);
                    myList.setBackgroundColor(Color.BLACK);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}