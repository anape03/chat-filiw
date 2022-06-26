package com.example.filiw.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.core.app.ActivityCompat;

import com.example.filiw.R;

public class ActivityCaptureVideo extends Activity
{
    static final int  REQUEST_TAKE_GALLERY_VIDEO = 1;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private String videoPath = "";
    private static final int LOAD_IMAGE_RESULTS = 1;
    private final String[] PERMISSIONS = {  Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};


    @SuppressLint("QueryPermissionsNeeded")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_video);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Button videoButton = this.findViewById(R.id.buttonCapture);
        Button SendButton = this.findViewById(R.id.buttonSendVideo);
        Button storageButton = findViewById(R.id.buttonfromStorage);
        Button cancel= findViewById(R.id.buttoncancelvideo);

        if (!hasPermissions(this, PERMISSIONS)) {
            int PERMISSION_ALL = 1;
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        videoButton.setOnClickListener(v -> {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
        });

        storageButton.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQUEST_TAKE_GALLERY_VIDEO);
        });


        SendButton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityCaptureVideo.this, activity_show_chat.class);
            intent.putExtra("mediafilepath", videoPath);
            startActivity(intent);
        });

        cancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            videoPath = videoUri.toString();
            Log.e("VIDEO PATH CAMERA", videoPath);
        }


        //  ------------CHOSE VIDEO FROM  gallery------------
        if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
//            // OI FILE Manager
//            String filemanagerstring = selectedImageUri.getPath();
            // MEDIA GALLERY
            String selectedImagePath = getPath(selectedImageUri);
            Log.e("VIDEO PATH GALLERY", selectedImagePath);
            videoPath = selectedImagePath;
        }
    }



    private static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
}
