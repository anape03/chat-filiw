package com.example.filiw.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterChat;
import com.example.filiw.backend.Client;
import com.example.filiw.backend.Value;

import java.util.ArrayList;
import java.util.List;

public class activity_show_chat extends AppCompatActivity {

    private final static String TOPIC_NAME = "topic_name";
    private final static String USERNAME = "nameofperson";
    private static String username = "";
    private static String topicName = "";

    ListView chatList;
    List<String> senderNames = new ArrayList<>();
    List<String> senderMessage = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);
        chatList = (ListView) findViewById(R.id.show_chat_list);

        setTopicValues(); // Set Topic Name and Image
        new Client(username, topicName, this).start(); // Generate client

        ///////////// For testing purposes
        for (int i=0; i<=10; i++){
            senderNames.add("Sender Name Number "+i);
            senderMessage.add("Test a big big very big message to see how it handles the thing Message Number "+i);
        }
        /////////////

        CustomAdapterChat customAdapterChat = new CustomAdapterChat(getApplicationContext(), senderNames, senderMessage);
        chatList.setAdapter(customAdapterChat);

        ImageButton homeButton = findViewById(R.id.show_chat_button_back);
        homeButton.setOnClickListener(v -> {
            finish();
        });

        ImageButton addFileButton = findViewById(R.id.show_chat_button_add_file);
        addFileButton.setOnClickListener(v -> {
            // TODO: go to add file
        });

        ImageButton sendMessageButton = findViewById(R.id.show_chat_button_send_message);
        sendMessageButton.setOnClickListener(v -> {
            // TODO: send message to broker
        });
    }

    /**
     * Consumer receives message from broker
     * @param messageValue message received
     */
    public void receiveMessage(Value messageValue){
        String sender = getSender(messageValue);
        String message = getMessage(messageValue);
        senderNames.add(sender);
        senderMessage.add(message);
    }

    /**
     * Extract sender value from message
     * @param message message Value object
     * @return sender name string
     */
    private String getSender(Value message){
        return message.getSenter();
    }

    /**
     * Extract message value from message
     * @param message message Value object
     * @return message string
     */
    private String getMessage(Value message){
        return message.getMessage();
    }

    /**
     * Get Topic name from previous activity
     * and set it to current one
     */
    private void setTopicValues(){
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        String topicName = bundle.getString(TOPIC_NAME);
        String username = bundle.getString(USERNAME);

        setTopicName(topicName);
        setUsername(username);
    }

    /**
     * Set topic name in variable, and show on screen
     * @param value topic name
     */
    public void setTopicName(String value) {
        topicName = value;
        ((TextView)findViewById(R.id.show_chat_title)).setText(value);
    }

    /**
     * set Username variable
     * @param value username
     */
    private void setUsername(String value){
        username = value;
    }
}