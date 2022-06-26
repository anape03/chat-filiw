package com.example.filiw.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterTopics;
import com.example.filiw.adapters.ItemTopic;
import com.example.filiw.backend.Address;
import com.example.filiw.backend.Client;
import com.example.filiw.backend.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class activity_show_topics extends AppCompatActivity {
    private final static int TOTAL_BROKERS = 3;
    private final static String TOPIC_NAME = "topic_name";
    private final static String USERNAME = "nameofperson";
    private static String username = "";

    ListView topicList;
    List<ItemTopic> listItemTopic = new ArrayList<>();
    Client client;
    CustomAdapterTopics customAdapterTopics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_topics);
        topicList = findViewById(R.id.show_topics_list);

        getData(); // Get necessary values from previous activity
        setUpClient();

        customAdapterTopics = new CustomAdapterTopics(getApplicationContext(), listItemTopic);
        topicList.setAdapter(customAdapterTopics);

        topicList.setOnItemClickListener((adapterView, view, position, id)
                -> onSelectedTopic(position));
    }

    /**
     * Set up client object, and connect to brokers
     */
    private void setUpClient(){
        client = new Client(username, "", this); // Generate client
        client.setBrokerAddresses(this.readAddresses()); // get all broker addresses
        TaskConnectToBroker getTaskConnectToBroker = new TaskConnectToBroker();
        getTaskConnectToBroker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"CONNECT_TO_BROKER");
    }

    private class TaskConnectToBroker extends AsyncTask<String, String, Client> {
        @Override
        protected Client doInBackground(String... strings) {
            if (strings[0].equals("CONNECT_TO_BROKER")) {
                Log.e("ASYNC_TASK","Executing connect to broker.");
                client.connectToBrokers();  // connect to broker
                return client;
            }
            Log.e("ASYNC_TASK","Invalid string given.");
            return null;
        }

        @Override
        protected void onPostExecute(Client user) {
            super.onPostExecute(user);
            if (user != null)
                client = user;
        }
    }

    /**
     * Consumer receives message from broker
     * @param message message received, in format:
     *                topic1&last_message_in_topic1%topic2&last_message_in_topic2....
     */
    public void receiveMessage(String message){
        String sep_topics = "%";        // separator for different topics
        String sep_topic_mes = "&";     // separator for topic and it's last message
        Log.e("MESSAGE","New message received.");
        String[] data = message.split(sep_topics);
        Log.e("TOPIC_INFO","Received topic info: "+ Arrays.toString(data));
        for (String item : data) {
            String[] topic_message = item.split(sep_topic_mes);
            Log.e("TOPIC_INFO","Topic and message items: "+ Arrays.toString(topic_message));
            String topic_name = topic_message[0];
            String last_message = topic_message.length > 1 ? topic_message[1] : "";

            int index = inListItemTopic(topic_name);
            if (index != -1){ // if topic already found in list
                if (listItemTopic.get(index).getLastMessage().equals("")) { // if previous entry had no messages
                    listItemTopic.remove(index);                            // remove it
                    listItemTopic.add(new ItemTopic(topic_name, last_message)); // and add the new one
                }
            }else{
                listItemTopic.add(new ItemTopic(topic_name, last_message)); // and add the new one
            }
        }
        runOnUiThread(() -> customAdapterTopics.notifyDataSetChanged());
    }

    /**
     * Get index of item in listItemTopic, by topic name.
     * @param topic_name the topic's name
     * @return index in list
     */
    private int inListItemTopic(String topic_name){
        int found_index = -1;
        for (ItemTopic item : listItemTopic){
            if (item.getTopicName().equals(topic_name)){
                found_index = listItemTopic.indexOf(item);
                break;
            }
        }
        return found_index;
    }

    /**
     * Get Values from previous activity
     */
    private void getData(){
        Bundle bundle = getIntent().getExtras();
        if (bundle == null){
            return;
        }
        String username = bundle.getString(USERNAME);

        setUsername(username);
    }

    /**
     * Helper method to read IP addresses and port number for brokers
     * from configuration file.
     * @return Arraylist with Address Objects containing IP address
     *         and port for every broker.
     */
    private ArrayList<Address> readAddresses(){
        ArrayList<Address> addresses = new ArrayList<Address>();

        Resources resources = this.getResources();
        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            String[] ipPort;
            for (int i = 1; i<=TOTAL_BROKERS; i++) {
                ipPort = properties.getProperty("broker_"+ i).split(" ");
                Address address = new Address(ipPort[0], Integer.parseInt(ipPort[1]));
                addresses.add(address);
            }
        } catch (Resources.NotFoundException e) {
            Log.e("CONFIG_FILE", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("CONFIG_FILE", "Failed to open config file.");
        }
        return addresses;
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