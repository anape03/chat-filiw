package com.example.filiw.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;

public class  ActivityShowVideo  extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view);

        VideoView videoView = findViewById(R.id.videoView);

        Button backButton = this.findViewById(R.id.buttonbackfromvideo);

        backButton.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent != null) {
            Bundle vidF = intent.getExtras();
            if (vidF != null) {
                if (vidF.containsKey("videoPath")) {
                    String path = vidF.getString("videoPath");
                    if (path != null) {
                        videoView.setVideoPath(path);
                        videoView.start();
                    }
                }
            }
        }
    }
}