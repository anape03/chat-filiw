package com.example.filiw.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;

public class ActivityShowImage extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        this.imageView = this.findViewById(R.id.imageViewDownPicture);
        Button backButton = this.findViewById(R.id.buttonBackChact);

        Intent intent = getIntent();
        if (intent != null){
            Bundle imageF = intent.getExtras();
            if (imageF != null) {
                if (imageF.containsKey("imagePath")) {
                    String imP = imageF.getString("imagePath");
                    if (imP != null){
                        imageView.setImageBitmap(BitmapFactory.decodeFile(imP));
                    }
                }
            }
        }

        backButton.setOnClickListener(v -> {finish();});
    }

}
