package com.example.filiw.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.filiw.R;
import com.example.filiw.adapters.CustomAdapterChat;
import com.example.filiw.backend.Address;
import com.example.filiw.backend.Client;
import com.example.filiw.backend.Value;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class activity_show_chat extends AppCompatActivity {
    private static final int VIDEO_CAPTURE = 101;
    private final static int TOTAL_BROKERS = 3;
    private final static String TOPIC_NAME = "topic_name";
    private final static String USERNAME = "nameofperson";
    private final static String FILENAME = "filename";
    private static String username = "";
    private static String topicName = "";
    private static String filename = "";

    ListView chatList;
    private static List<String> senderNames = new ArrayList<>();
    private static List<String> senderMessage = new ArrayList<>();

    Client client;
    CustomAdapterChat customAdapterChat;

    private boolean clicked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);
        chatList = (ListView) findViewById(R.id.show_chat_list);

        setTopicValues(); // Set Topic Name and Image
        setUpClient();

        customAdapterChat = new CustomAdapterChat(getApplicationContext(), senderNames, senderMessage);
        chatList.setAdapter(customAdapterChat);

        TaskConnectToBroker getTaskConnectToBroker = new TaskConnectToBroker();
        getTaskConnectToBroker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"CONNECT_TO_BROKER");

        ///////////// For testing purposes
//        for (int i=0; i<=10; i++){
//            senderNames.add("Sender Name Number "+i);
//            senderMessage.add("Test a big big very big message to see how it handles the thing Message Number "+i);
//        }
        /////////////

        //Create buttons

        FloatingActionButton openchoices=  findViewById(R.id.floatingActionButtonOpenChoices );
        FloatingActionButton addPictureButton = findViewById(R.id.addPicture);
        FloatingActionButton addVideoButton = findViewById(R.id.addVideo);
        ImageButton homeButton = findViewById(R.id.show_chat_button_back);
        ImageButton sendMessageButton = findViewById(R.id.show_chat_button_send_message);

        /*animations*/
        Animation rotateOpen= AnimationUtils.loadAnimation
                (this,R.anim.rotate_open);
        Animation rotateClose=AnimationUtils.loadAnimation
                (this,R.anim.rotate_close);
        Animation fromBottom= AnimationUtils.loadAnimation
                (this,R.anim.from_bottom);
        Animation toBottom=AnimationUtils.loadAnimation
                (this,R.anim.to_bottom);


        // Handle buttons =============

        homeButton.setOnClickListener(v -> {
            exitClient();
        });


        if (clicked){
            addPictureButton.setClickable(false);
            addVideoButton.setClickable(false);
        }else{
            addPictureButton.setClickable(true);
            addVideoButton.setClickable(true);
        }


        addVideoButton.setOnClickListener(v -> {

        });


        addPictureButton.setOnClickListener(v -> {
            Intent temp=new Intent(activity_show_chat.this,ActivityTakePicture.class);
            startActivity(temp);
                });



        openchoices.setOnClickListener(v -> {
            clicked=!clicked;
            if (clicked){
                addPictureButton.show();
                addVideoButton.show();
                addPictureButton.startAnimation(fromBottom);
                addVideoButton.startAnimation(fromBottom);
                openchoices.startAnimation(rotateOpen);
            }else{
                addPictureButton.hide();
                addVideoButton.hide();
                addPictureButton.startAnimation(toBottom);
                addVideoButton.startAnimation(toBottom);
                openchoices.startAnimation(rotateClose);



            }
        });
        sendMessageButton.setOnClickListener(v -> {
            Log.e("SEND_MESSAGE_BUTTON","Send message button pressed.");
            String text = this.getTextWritten();
            if (!text.equals("")) {
                Log.e("SEND_MESSAGE_BUTTON","Message box not empty: "+text);
                TaskSendMessage getTaskSendMessage = new TaskSendMessage();
                getTaskSendMessage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"SEND_MESSAGE", "TEXT", text);
                this.setTextWritten("");
            }
        });
    }

    /**
     * Set up client object
     */
    private void setUpClient(){
        client = new Client(username, topicName, this); // Generate client
        client.setBrokerAddresses(this.readAddresses()); // get all broker addresses
        client.setAddressBroker();  //set random broker
    }

    /**
     * Client wants to exit
     */
    private void exitClient(){ // TODO also exit client when back button pressed
        TaskExitClient getTaskExitClient = new TaskExitClient();
        getTaskExitClient.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"EXIT_CLIENT");
        senderNames.clear();
        senderMessage.clear();
        finish();
    }

    private class TaskConnectToBroker extends AsyncTask<String, String, Client> {
        @Override
        protected Client doInBackground(String... strings) {
            if (strings[0].equals("CONNECT_TO_BROKER")) {
                Log.e("ASYNC_TASK","Executing connect to broker.");
                client.connectToBroker(topicName);  // connect to broker
                client.receiveMessages();           // receive conversation history
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

    private class TaskSendMessage extends AsyncTask<String, String, Client> {
        @Override
        protected Client doInBackground(String... strings) {
            if (strings[0].equals("SEND_MESSAGE")) {
                Log.e("ASYNC_TASK","Executing send message.");
                client.sendMessage(strings[1], strings[2]);
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

    private class TaskExitClient extends AsyncTask<String, String, Client> {
        @Override
        protected Client doInBackground(String... strings) {
            if (strings[0].equals("EXIT_CLIENT")) {
                Log.e("ASYNC_TASK","Executing client exit.");
                client.getPublisher().exitRequest();
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
     * Consumer receives message from broker
     * @param messageValue message received
     */
    public void receiveMessage(Value messageValue){
        Log.e("MESSAGE","New message received.");
        String sender = messageValue.getSenter();
        String message = messageValue.getMessage();
        senderNames.add(sender);
        senderMessage.add(message);
        runOnUiThread(() -> customAdapterChat.notifyDataSetChanged());
    }

    /**
     * Get text written on screen by user
     * @return text message written on screen
     */
    public String getTextWritten(){
        return ((EditText)findViewById(R.id.show_chat_write_message)).getText().toString().trim();
    }


    private static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

//    private void CaptureVideo(){
//        if (!hasPermissions(this, PERMISSIONS)) {
//            int PERMISSION_ALL = 1;
//            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//        }
//        else {
//            File mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
//            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//            Uri videoUri = Uri.fromFile(mediaFile);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
//            startActivityForResult(intent, VIDEO_CAPTURE);
//        }
//    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_LONG).show();
            }
        }
    }



    /**
     * Sets text written on screen
     * @param value text
     */
    public void setTextWritten(String value) {
        ((EditText)findViewById(R.id.show_chat_write_message)).setText(value);
    }

    /**
     * Get Topic name from previous activity0
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