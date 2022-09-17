package com.example.fishapplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fishapplication.ml.FishModel;
import com.example.fishapplication.ml.FishModell;
import com.example.fishapplication.ml.FishnewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Arrays;

public class UploadActivity extends AppCompatActivity {

    private ImageView imgView;
    private Button select, predict, gallery, camera;
    private Bitmap img;
    private TextView tv;
    private String[] classification;
    private DecimalFormat f = new DecimalFormat("##.00");
    private LinearLayout layout;
    private static int cameraStatus=0, hide1=0;
    private FirebaseDatabase database;

    private static final int REQUEST_LOCATION = 1;


    LocationManager locationManager;
    String latitude, longitude;

    private static String fishName;
    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                UploadActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                UploadActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);
                Toast.makeText(UploadActivity.this, "Location is been recorded", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getLatLong(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
    }

    public void infoClick(View view){
        Intent intent = new Intent(UploadActivity.this, MoreInfoActivity.class);

        DatabaseReference myRef = database.getReference("database/"+fishName);


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot descSnap = snapshot.child("Description");
                DataSnapshot imgSnap = snapshot.child("Img");
                String descValue = descSnap.getValue(String.class);
                String imgValue = imgSnap.getValue(String.class);
                intent.putExtra("desc",descValue);
                intent.putExtra("fishName", fishName);
                intent.putExtra("imgUrl", imgValue);

                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_upload);

        database = FirebaseDatabase.getInstance();


        imgView = (ImageView) findViewById(R.id.imageView);
        tv = (TextView) findViewById(R.id.resultView);
        select = (Button) findViewById(R.id.selectBtn);
        predict = (Button) findViewById(R.id.predictBtn);
        gallery = (Button) findViewById(R.id.galleryBtn);
        camera = (Button) findViewById(R.id.cameraBtn);
        layout = (LinearLayout) findViewById(R.id.hideLayout2);
        classification = new String[]{"Arowana","Black Sea Sprat", "Gilt Head Bream", "Horse Mackerel", "Red Mullet", "Red Sea Bream", "Sea Bass", "Shrimp", "Spanish Dancer Jellyfish","Stripped Red Mullet", "Trout"};

        //Location
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);




        //Location


        imgView.setImageResource(R.drawable.icon1);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(layout.getVisibility() == LinearLayout.VISIBLE)
                    layout.setVisibility(View.INVISIBLE);
                else
                    layout.setVisibility(LinearLayout.VISIBLE);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 100);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    startActivityForResult(cameraIntent, 100);
                    if(cameraStatus == 0)
                        cameraStatus++;

                }else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);


                }
            }
        });

        predict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ((LinearLayout)findViewById(R.id.hideLayout)).setVisibility(View.VISIBLE);
                //classifyImage(img);
                classifyImage2(img);

//                try {
//                    FishModel model = FishModel.newInstance(getApplicationContext());
//
//                    // Creates inputs for reference.
//                    TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
//
//                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
//                    tensorImage.load(img);
//                    ByteBuffer byteBuffer = tensorImage.getBuffer();
//
//                    inputFeature0.loadBuffer(byteBuffer);
//
//                    // Runs model inference and gets result.
//                    FishModel.Outputs outputs = model.process(inputFeature0);
//                    TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//
//                    // Releases model resources if no longer used.
//                    model.close();
//
//
//                    tv.setText(outputFeature0.getFloatArray()[6]*100+"\n");
//
//
//                } catch (IOException e) {
//                    // TODO Handle the exception
//                }
            }
        });

        //Action for back button
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent1 = new Intent(UploadActivity.this, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent1);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public void classifyImage(Bitmap image){
        try {
            FishModel model = FishModel.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*224*224*3);
            byteBuffer.order(ByteOrder.nativeOrder());


            int[] intValues = new int[224*224];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0;
            for(int i=0; i<224; i++){
                for(int j=0; j<224 ; j++){
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 225));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 225));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 225));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            FishModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            float[] sortConfidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0, secondPos=0, thirdPos=0;
            int endpos = confidences.length-1;
            float maxConfidence = 0;
            Arrays.sort(sortConfidences);
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }

                if(sortConfidences[endpos-1] == confidences[i])
                    secondPos = i;
                if(sortConfidences[endpos-2] == confidences[i])
                    thirdPos = i;
            }



            //Setting up the top match
            float confidence = sortConfidences[endpos]*100;
            tv.setText("Specie : "+classification[maxPos]+"  => "+f.format(confidence)+"%");
            fishName = classification[maxPos];



            //Setting up 2nd and 3rd match
            String res = new String("");
            confidence = sortConfidences[--endpos]*100;
            res+="Specie : "+classification[secondPos]+"  => "+f.format(confidence)+"%";
            confidence = sortConfidences[--endpos]*100;
            res+="\nSpecie : "+classification[thirdPos]+"  => "+f.format(confidence)+"%";
            ((TextView)findViewById(R.id.resultView2)).setText(res);



            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    public void classifyImage2(Bitmap image){
        try {
            FishnewModel model = FishnewModel.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*224*224*3);
            byteBuffer.order(ByteOrder.nativeOrder());


            int[] intValues = new int[224*224];
            image.getPixels(intValues,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());
            int pixel = 0;
            for(int i=0; i<224; i++){
                for(int j=0; j<224 ; j++){
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 225));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 225));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 225));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            FishnewModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            float[] sortConfidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0, secondPos=0, thirdPos=0;
            int endpos = confidences.length-1;
            float maxConfidence = 0;
            Arrays.sort(sortConfidences);
            for (int i = 0; i < confidences.length; i++) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i];
                    maxPos = i;
                }

                if(sortConfidences[endpos-1] == confidences[i])
                    secondPos = i;
                if(sortConfidences[endpos-2] == confidences[i])
                    thirdPos = i;
            }



            //Setting up the top match
            float confidence = sortConfidences[endpos]*100;

            tv.setTextColor(Color.WHITE);
            tv.setText("Specie : "+classification[maxPos]+"  => "+f.format(confidence)+"%");
            fishName = classification[maxPos];



            //Setting up 2nd and 3rd match
            String res = new String("");
            confidence = sortConfidences[--endpos]*100;
            res+="Specie : "+classification[secondPos]+"  => "+f.format(confidence)+"%";
            confidence = sortConfidences[--endpos]*100;
            res+="\nSpecie : "+classification[thirdPos]+"  => "+f.format(confidence)+"%";
            ((TextView)findViewById(R.id.resultView2)).setText(res);

            //Checking for Endangered
            if(classification[maxPos].equals("Spanish Dancer Jellyfish")){
                tv.setText("Specie : "+classification[maxPos]+" => "+f.format(maxConfidence*100)+"%"+"(Endangered)");
                tv.setTextColor(Color.RED);
                getLatLong();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("locDatabase");
                myRef.child("UI3").setValue(classification[maxPos]+","+latitude+","+longitude);


            }


            //Checking for Endangered

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && cameraStatus !=1)
        {


            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            } catch (IOException e) {
                e.printStackTrace();
            }

            imgView.setImageBitmap(img);
            img = Bitmap.createScaledBitmap(img, 224, 224, true);
        }else{

            img = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(img.getWidth(), img.getHeight());
            img = ThumbnailUtils.extractThumbnail(img, dimension, dimension);
            imgView.setImageBitmap(img);
            img = Bitmap.createScaledBitmap(img, 224, 224, true);
            if(cameraStatus == 1)
                cameraStatus--;
        }


    }
}