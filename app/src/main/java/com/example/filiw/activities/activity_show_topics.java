package com.example.filiw.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterTopics;
import com.example.filiw.adapters.ItemTopic;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class activity_show_topics extends AppCompatActivity {
    private final static int TOTAL_TOPICS = 4;
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
        listItemTopic = getTopicData();

        ///////////// For testing purposes
//        String topicName = "Topic Name Number 0";
//        String topicLastMessage = "";
//        listItemTopic.add(new ItemTopic(topicName, topicLastMessage));
//        for (int i=1; i<=10; i++){
//            topicName = "Topic Name Number "+i;
//            topicLastMessage = "Test Message very very big let's seeeeeeeeeeee what it do be doing Number "+i;
//            listItemTopic.add(new ItemTopic(topicName, topicLastMessage));
//        }
//        listItemTopic.add(new ItemTopic("Topic Name Number 11", "Test Message Number 11"));
        /////////////

        CustomAdapterTopics customAdapterTopics = new CustomAdapterTopics(getApplicationContext(), listItemTopic/*topicImages, topicNames, topicLastMessage*/);
        topicList.setAdapter(customAdapterTopics);

        topicList.setOnItemClickListener((adapterView, view, position, id)
                -> onSelectedTopic(position));
    }

    /**
     * Get Values from previous activity
     */
    private void getData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle == null){
//            setUsername("witch"); //-0
            return;
        }
        String username = bundle.getString(USERNAME);

        setUsername(username);
    }

    /**
     * Helper method to topic data from configuration file.
     * @return Arraylist with topic names
     */
    private List<ItemTopic> getTopicData(){
        List<ItemTopic> topics = new ArrayList<>();

        Resources resources = this.getResources();
        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            String[] topicData;
            for (int i = 1; i<=TOTAL_TOPICS; i++) {
                topicData = properties.getProperty("topic_"+ i).split("%");
                ItemTopic topic = new ItemTopic(topicData[0],topicData[1]);
                topics.add(topic);
            }
        } catch (Resources.NotFoundException e) {
            Log.e("CONFIG_FILE", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("CONFIG_FILE", "Failed to open config file.");
        }

        return topics;
    }

    private void setUsername(String value){
        username = value;
    }

    /**
     * Send Topic Image and Name to next activity
     */
    private void onSelectedTopic(int position){
        String topicName = getTopicName(position);

        //TODO: handle stories

        Intent intent = new Intent(this, activity_show_chat.class);
        Bundle bundle = new Bundle();
        bundle.putString(TOPIC_NAME, topicName);
        bundle.putString(USERNAME, username);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        this.startActivity(intent);
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