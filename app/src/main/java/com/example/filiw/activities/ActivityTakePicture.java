package com.example.filiw.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import androidx.core.app.ActivityCompat;
import com.example.filiw.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ActivityTakePicture extends Activity
{
    private String imagePath = "";
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int LOAD_IMAGE_RESULTS = 1;
    private final String[] PERMISSIONS = {  Manifest.permission.CAMERA,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        this.imageView = (ImageView)this.findViewById(R.id.imageViewTakenPicture);
        Button photoButton = (Button)this.findViewById(R.id.buttonTakePhoto);
        Button backButton = (Button)this.findViewById(R.id.buttonBack);

        //  ------------Gia epilogh eikonas apo gallery------------
        Button storageButton = (Button)findViewById(R.id.buttonStorage);

        if (!hasPermissions(this, PERMISSIONS)) {
            int PERMISSION_ALL = 1;
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        photoButton.setOnClickListener(v -> {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        });

        //  ------------Gia epilogh eikonas apo gallery------------
        storageButton.setOnClickListener(v -> {
            // Create the Intent for Image Gallery.
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
            startActivityForResult(i, LOAD_IMAGE_RESULTS);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra("mediafilepath", imagePath);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            saveImage(photo);
        }

        //  ------------Gia epilogh eikonas apo gallery------------
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            @SuppressLint("Range") String imP = cursor.getString(cursor.getColumnIndex(filePath[0]));
            imagePath = imP;
            // Now we need to set the GUI ImageView data with data read from the picked file.
            imageView.setImageBitmap(BitmapFactory.decodeFile(imP));

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
    }

    private void saveImage(Bitmap image) {
        try {
            int quality = 100;
            String videoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String dateTime = simpleDateFormat.format(calendar.getTime());
            new File(videoPath+"/filiw/data").mkdirs();
            FileOutputStream fos = new FileOutputStream(videoPath + "/filiw/data/" + dateTime + ".jpg");
            image.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//  ------------Isws ta permissions na prepei na pane sthn arxikh othonh------------
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
}
