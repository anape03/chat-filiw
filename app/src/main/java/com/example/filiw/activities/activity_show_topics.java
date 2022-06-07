package com.example.filiw.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.ListView;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterTopics;

import java.util.ArrayList;
import java.util.List;

public class activity_show_topics extends AppCompatActivity {

    ListView topicList;
    List<Integer> topicImages = new ArrayList<>();
    List<String> topicNames = new ArrayList<>();
    List<String> topicLastMessage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_topics);
        topicList = (ListView) findViewById(R.id.show_topics_list);

        ///////////// For testing purposes
        for (int i=0; i<=10; i++){
            topicImages.add(R.drawable.filiw_pinei_kafe);
            topicNames.add("Topic Name Number "+i);
            topicLastMessage.add("Test Message Number "+i);
        }
        /////////////

        CustomAdapterTopics customAdapterTopics = new CustomAdapterTopics(getApplicationContext(), topicImages, topicNames, topicLastMessage);
        topicList.setAdapter(customAdapterTopics);
    }

//    private ActivityShowTopicsBinding binding;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        binding = ActivityShowTopicsBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
//        ViewPager viewPager = binding.viewPager;
//        viewPager.setAdapter(sectionsPagerAdapter);
//        TabLayout tabs = binding.tabs;
//        tabs.setupWithViewPager(viewPager);
//        FloatingActionButton fab = binding.fab;
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//    }
}