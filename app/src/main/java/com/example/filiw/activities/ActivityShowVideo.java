package com.example.filiw.activities;


import android.os.Bundle;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;

public class  ActivityShowVideo  extends AppCompatActivity {

    private VideoView videoView;
    private int position = 0;
    private MediaController mediaController;
    private Button buttonRaw;
    private Button buttonLocal;
    private Button buttonURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.videoView = findViewById(R.id.videoView);
        this.buttonRaw = findViewById(R.id.button_raw);
        this.buttonLocal = findViewById(R.id.button_local );
        this.buttonURL = findViewById(R.id.button_url);


        // Set the media controller buttons
        if (this.mediaController == null) {
            this.mediaController = new MediaController(ActivityShowVideo.this);

            // Set the videoView that acts as the anchor for the MediaController.
            this.mediaController.setAnchorView(videoView);

            // Set MediaController for VideoView
            this.videoView.setMediaController(mediaController);
        }


        // When the video file ready for playback.
        this.videoView.setOnPreparedListener(mediaPlayer -> {

            videoView.seekTo(position);
            if (position == 0) {
                videoView.start();
            }

            // When video Screen change size.
            mediaPlayer.setOnVideoSizeChangedListener((mp, width, height) -> {

                // Re-Set the videoView that acts as the anchor for the MediaController
                mediaController.setAnchorView(videoView);
            });
        });

        this.buttonRaw.setOnClickListener(v -> {
            // "myvideo.mp4" in directory "raw".
            String resName = VideoViewUtils.RAW_VIDEO_SAMPLE;
            VideoViewUtils.playRawVideo(ActivityShowVideo.this, videoView, resName);
        });

        this.buttonLocal.setOnClickListener(v -> {
            String localPath = VideoViewUtils.LOCAL_VIDEO_SAMPLE;
            VideoViewUtils.playLocalVideo(ActivityShowVideo.this, videoView, localPath);
        });

        this.buttonURL.setOnClickListener(v -> {
            String videoURL = VideoViewUtils.URL_VIDEO_SAMPLE;
            VideoViewUtils.playURLVideo(ActivityShowVideo.this, videoView, videoURL);
        });
    }

    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Store current position.
        savedInstanceState.putInt("CurrentPosition", videoView.getCurrentPosition());
        videoView.pause();
    }


    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved position.
        position = savedInstanceState.getInt("CurrentPosition");
        videoView.seekTo(position);
    }

}