package com.example.filiw.activities;

import android.os.Bundle;

import com.example.filiw.adapters.CustomAdapterChat;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ListView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.filiw.R;

import java.util.ArrayList;
import java.util.List;

public class activity_show_chat extends AppCompatActivity {

//    private AppBarConfiguration appBarConfiguration;
//    private ActivityShowChatBinding binding;
    ListView chatList;
    List<String> topicNames = new ArrayList<>();
    List<String> topicLastMessage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);
        chatList = (ListView) findViewById(R.id.show_chat_list);

        ///////////// For testing purposes
        for (int i=0; i<=10; i++){
            topicNames.add("Sender Name Number "+i);
            topicLastMessage.add("Test a big big very big message to see how it handles the thing Message Number "+i);
        }
        /////////////

        CustomAdapterChat customAdapterChat = new CustomAdapterChat(getApplicationContext(), topicNames, topicLastMessage);
        chatList.setAdapter(customAdapterChat);


//        binding = ActivityShowChatBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.toolbar);
//
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_activity_show_chat);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

//        binding.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_activity_show_chat);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}