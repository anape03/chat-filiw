package com.example.filiw.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
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
                    Uri path = Uri.parse(vidF.getString("videoPath"));
                    if (path != null) {
                        videoView.setVideoURI(path);
//                        MediaController mediaController = new MediaController(this);
//                        mediaController.setAnchorView(videoView);
//                        mediaController.setMediaPlayer(videoView);
//                        videoView.setMediaController(mediaController);
//                        videoView.setVisibility(View.VISIBLE);
//                        videoView.bringToFront();
//                        videoView.requestFocus();
                        videoView.start();
                    }
                }
            }
        }
    }
}