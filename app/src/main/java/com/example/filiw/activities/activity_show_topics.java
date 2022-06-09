package com.example.filiw.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterTopics;
import com.example.filiw.adapters.ItemTopic;

import java.util.ArrayList;
import java.util.List;

public class activity_show_topics extends AppCompatActivity {
    private final static String TOPIC_NAME = "topic_name";
    private final static String USERNAME = "nameofperson";
    private static String username = "";

    ListView topicList;
    List<ItemTopic> listItemTopic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_topics);
        topicList = (ListView) findViewById(R.id.show_topics_list);

        getData(); // Get necessary values from previous activity

        ///////////// For testing purposes
        for (int i=0; i<=10; i++){
            String topicName = "Topic Name Number "+i;
            String topicLastMessage = "Test Message very very big let's seeeeeeeeeeee what it do be doing Number "+i;
            listItemTopic.add(new ItemTopic(topicName, topicLastMessage));
        }
        listItemTopic.add(new ItemTopic("Topic Name Number 11", "Test Message Number 11"));
        /////////////

        CustomAdapterTopics customAdapterTopics = new CustomAdapterTopics(getApplicationContext(), listItemTopic/*topicImages, topicNames, topicLastMessage*/);
        topicList.setAdapter(customAdapterTopics);

        topicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
                onSelectedTopic(position);
            }
        });
    }

    /**
     * Get Values from previous activity
     */
    private void getData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        String username = bundle.getString(USERNAME);

        setUsername(username);
    }

    private void setUsername(String value){
        username = value;
    }

    /**
     * Send Topic Image and Name to next activity
     */
    private void onSelectedTopic(int position){
        String topicName = getTopicName(position);

        Intent intent = new Intent(this, activity_show_chat.class);
        Bundle bundle = new Bundle();
        bundle.putString(TOPIC_NAME, topicName);
        bundle.putString(USERNAME, username);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    /**
     * Get Topic Name from screen
     * @param i for item in position i
     * @return topic name
     */
    public String getTopicName(int i) {
        return listItemTopic.get(i).getTopicName();
    }
}