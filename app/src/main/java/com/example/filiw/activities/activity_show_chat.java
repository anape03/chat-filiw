package com.example.filiw.activities;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterChat;
import com.example.filiw.backend.Address;
import com.example.filiw.backend.Client;
import com.example.filiw.backend.Consumer;
import com.example.filiw.backend.Publisher;
import com.example.filiw.backend.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class activity_show_chat extends AppCompatActivity {
    private final static int TOTAL_BROKERS = 3;

    private final static String TOPIC_NAME = "topic_name";
    private final static String USERNAME = "nameofperson";
    private final static String FILENAME = "filename";
    private static String username = "";
    private static String topicName = "";
    private static String filename = "";

    ListView chatList;
    List<String> senderNames = new ArrayList<>();
    List<String> senderMessage = new ArrayList<>();

    Client client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);
        chatList = (ListView) findViewById(R.id.show_chat_list);

        setTopicValues(); // Set Topic Name and Image
        setUpClient();

        GetTask getTask = new GetTask();
        getTask.execute("CONNECT_TO_BROKER");

        ///////////// For testing purposes
//        for (int i=0; i<=10; i++){
//            senderNames.add("Sender Name Number "+i);
//            senderMessage.add("Test a big big very big message to see how it handles the thing Message Number "+i);
//        }
        /////////////

        CustomAdapterChat customAdapterChat = new CustomAdapterChat(getApplicationContext(), senderNames, senderMessage);
        chatList.setAdapter(customAdapterChat);

        // Handle buttons =============

        ImageButton homeButton = findViewById(R.id.show_chat_button_back);
        homeButton.setOnClickListener(v -> {
            getTask.execute("EXIT_CLIENT");
            finish();
        });

        ImageButton addFileButton = findViewById(R.id.show_chat_button_add_file);
        addFileButton.setOnClickListener(v -> {
            // TODO: go to add file
            getFilename(); // Get filename from other activity
            getTask.execute("SEND_MESSAGE", "MULTIMEDIA", filename);
        });

        ImageButton sendMessageButton = findViewById(R.id.show_chat_button_send_message);
        sendMessageButton.setOnClickListener(v -> {
            getTask.execute("SEND_MESSAGE", "TEXT", this.getTextWritten());
        });
    }

    /**
     * Set up client object
     */
    private void setUpClient(){
        client = new Client(username, topicName, this); // Generate client
        client.setBrokerAddresses(readAddresses()); // get all broker addresses
        client.setAddressBroker();  //set random broker
    }

    private class GetTask extends AsyncTask<String, String, Client> {
        @Override
        protected Client doInBackground(String... strings) {
            switch (strings[0]) {
                case "CONNECT_TO_BROKER":
                    return client.connectToBroker(topicName);
                case "RECEIVE_MESSAGES":
                    client.getConsumer().showConversationData();
                    return client;
                case "SEND_MESSAGE":
                    client.getPublisher().sendMessage(strings[1], strings[2]);
                    return client;
                case "EXIT_CLIENT":
                    client.getPublisher().exitRequest();
                    return client;
            }
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
     * Get text written on screen by user
     * @return text message written on screen
     */
    public String getTextWritten(){
        return ((EditText)findViewById(R.id.show_chat_write_message)).getText().toString().trim();
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
        String file_name = bundle.getString(FILENAME);

        setTopicName(topicName);
        setUsername(username);
        setFilename(file_name);
    }

    /**
     * Get filename (to be uploaded) from other activity
     * and set it to filename variable
     */
    private void getFilename(){
        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;
        String file_name = bundle.getString(FILENAME);
        setFilename(file_name);
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
     * Set Username variable
     * @param value username
     */
    private void setUsername(String value){
        username = value;
    }

    /**
     * Set filename variable
     * @param value username
     */
    private void setFilename(String value){
        filename = value;
    }
}