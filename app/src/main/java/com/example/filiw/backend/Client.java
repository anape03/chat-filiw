package com.example.filiw.backend;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filiw.activities.activity_show_topics;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Client extends Node {
    ArrayList<Address> brokerAddresses = null;
    Address address = null;
    String username = null;
    int id;
    Socket requestSocket = null;
    Thread consumer = null;
    Thread publisher = null;
    public boolean stopthreads = false;
    public boolean Alivesocket= false;
    String desiredTopic = null;
    AppCompatActivity activity;
    boolean changeBroker = false;

    /**
     * Constructor for Client
     */
    public Client() {
        address = getRandomBroker();
        id = new Random().nextInt();
        this.createLogFile("Client"+id+".txt");
    }

    /**
     * Constructor for Client
     */
    public Client(String username, String topicName, AppCompatActivity activity) {
        this.username = username;
        this.desiredTopic = topicName;
        this.activity = activity;
        id = new Random().nextInt();
        this.createLogFile("Client"+id+".txt");
    }

    /**
     * Getters
     */
    public String getUsername(){ return username; }
    public Socket getSocket(){ return requestSocket; }
    public String getdesiredTopic(){return desiredTopic; }
    public Socket getConnection(){ return requestSocket; }
    public AppCompatActivity getActivity(){ return activity; }
    public Consumer getConsumer() { return (Consumer)consumer; }
    public Publisher getPublisher() { return (Publisher)publisher; }
    public void setBrokerAddresses(ArrayList<Address> array) {
        brokerAddresses = array;
        Log.e("BROKER_ADDRESSES_SET", getBrokerAddresses().toString());
    }
    public ArrayList<Address> getBrokerAddresses() { return brokerAddresses; }
    public void setAddressBroker(){ address = getRandomBroker(); }

    /**
     * Get random broker address to connect to
     * @return Broker Address to connect to
     */
    public Address getRandomBroker(){
        int rnd = new Random().nextInt(brokerAddresses.size());
        Log.e("RANDOM_BROKER","Broker addresses found: "+brokerAddresses.get(rnd));
        return new Address(brokerAddresses.get(rnd).getIp(), brokerAddresses.get(rnd).getPort());
    }

    /**
     * Receive messages from broker
     */
    public void receiveMessages(){
        ((Consumer)consumer).showConversationData();
    }

    /**
     * Sent message to broker
     * @param type message type (MULTIMEDIA, TEXT)
     * @param message message contents
     */
    public void sendMessage(String type, String message){
        Log.e("CLIENT_SEND","Client wants to sent message.");
        Log.e("PUBLISHER IS NULL", String.valueOf(publisher==null));
        int status = ((Publisher)publisher).sendMessage(type,message);
        switch (status){
            case 0:
                Log.e("PUBLISHER_SEND","Message sent successfully.");
                break;
            case -2:
                Log.e("PUBLISHER_SEND","Multimedia file not found.");
                break;
            case -1:
                Log.e("PUBLISHER_SEND","Invalid message type given.");
                break;
        }
    }
            
    @Override
    public void run() {
        changeBroker = getTopicInfo();
        try {
            if (changeBroker){
                requestSocket = new Socket(address.getIp(), address.getPort());
                publisher = new Publisher(this);
                consumer = new Consumer(this);
                ((Publisher)publisher).push(new Value(this.getUsername(), desiredTopic, false, false)); 
            }
            Alivesocket = true;
            stopthreads = false;

            while(!stopthreads){
                consumer.start();

                ((Publisher)publisher).push(new Value(this.getUsername(), "", false, false)); 
                publisher.start();
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (IllegalThreadStateException ithe){
        }
    }

    /**
     * Connecto to assigned Broker for requested topic to receive and send messages
     * @param topicName topic requested
     * @return client object
     */
    public Client connectToBroker(String topicName){
        Log.e("BROKER_PING", "Is broker address reachable: "+sendPingRequest(address.getIp()));
        desiredTopic = topicName;
        Log.e("CONNECT_TOPIC","Trying to connect to topic: "+desiredTopic);
        boolean changeBroker = getTopicInfo();
        try {
            if (changeBroker){
                Log.e("CONNECT_TO_ADDRESS", "Must connect to broker address "+address);
                requestSocket = new Socket(address.getIp(), address.getPort());
                publisher = new Publisher(this);
                consumer = new Consumer(this);
                ((Publisher)publisher).push(new Value(this.getUsername(), desiredTopic, false, false));
            }
            Alivesocket = true;
            ((Publisher)publisher).push(new Value(this.getUsername(), "", false, false));
        } catch (UnknownHostException unknownHost) {
            Log.e("CONNECTION_FAIL","You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return this;
    }

    /**
     * Connect to broker for getting topic info
     * @return client object
     */
    public Client connectToBrokers(){
        Log.e("BROKER_ADDRESSES", String.valueOf(this.brokerAddresses));

        for (Address ad : this.brokerAddresses) {
            Log.e("BROKER_PING", "Is broker address reachable: " + sendPingRequest(ad.getIp()));
            try {
                Log.e("CONNECTION", "Creating socket to broker: " + ad);
                requestSocket = new Socket(ad.getIp(), ad.getPort());
                Log.e("CONNECTION", "Request socket: " + requestSocket);
                publisher = new Publisher(this);
                consumer = new Consumer(this);
                if (requestSocket != null) {
                    Value message = new Value(this.username, "INITIALISATION_MESSAGE", false, false);
                    ((Publisher)publisher).push(message);
                    message = new Value(this.username, "ALL_TOPIC_INFO", false, false);
                    ((Publisher)publisher).push(message);
                    Log.e("TOPICS_MESSAGE", "Client sent \"ALL_TOPIC_INFO\" message to broker.");
                    String reply = ((Consumer)consumer).register();
                    Log.e("TOPICS_MESSAGE", "Client received topic info from broker.");
                    ((activity_show_topics) (this.getActivity())).receiveMessage(reply);
                    Log.e("TOPICS_MESSAGE", "Client sent topic info to activity_show_topics.");
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            this.getPublisher().exitRequest(); // exit client
            Log.e("TOPICS_MESSAGE", "Client requested to exit broker.");
        }
        return this;
    }

    /**
     * Ping Broker to check if it's active
     * @param ipAddress broker's ip address
     * @return whether broker was reached
     */
    private static boolean sendPingRequest(String ipAddress) {
        InetAddress inet = null;
        try {
            inet = InetAddress.getByName(ipAddress);
            Log.e("PING_ADDRESS","Sending Ping Request to " + ipAddress);
            return inet.isReachable(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Learn from the randomly chosen broker whether to change 
     * broker in order to connect to topic
     * @return true if broker changed,
     *         false if not
     */
    private boolean getTopicInfo(){
        try {
            Log.e("CONNECTION", "Creating socket to broker: "+address);
            requestSocket = new Socket(address.getIp(), address.getPort());
            Log.e("CONNECTION","Request socket: "+requestSocket);
            publisher = new Publisher(this);
            consumer = new Consumer(this);
            if (requestSocket!=null){
                Value message = new Value(this.username, desiredTopic, false, false);
                ((Publisher)publisher).push(message);
                String[] data = ((Consumer)consumer).register().split(" ");
                Log.e("TOPIC_INFO","Received topic info: "+ Arrays.toString(data));
                if (data[0].equals("yes")){
                    String ip = getBrokerAddresses().get(Integer.parseInt(data[1])).getIp();
                    address.setIp(ip);
                    int port = getBrokerAddresses().get(Integer.parseInt(data[1])).getPort();
                    address.setPort(port);
                    
                    Value exitmes = new Value(this.username);
                    ((Publisher)publisher).push(exitmes);
                    closeClient();
                    changeBroker = true;
                    Log.e("CONNECTION","Must change broker.");
                    return true;
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        changeBroker = false;
        Log.e("CONNECTION","Must not change broker.");
        return false;
    }
    
    /**
     * Close client connection to server and streams
     * @throws IOException
     */
    public void closeClient() throws IOException {
        try {
            this.writeToFile("[Client]: Attempting to close client..", false);
            Log.e("CONNECTION","Attempting to close client.");
            Alivesocket= false;
            stopthreads=true;

            ((Publisher)publisher).closee();
            ((Consumer)consumer).closee();
            requestSocket.close();

            publisher = null;
            consumer = null;
            requestSocket = null;

            this.writeToFile("[Client]: Socket closed.", false);
            this.writeToFile("[Client]: Client closed connection to broker.", false);
            Log.e("CONNECTION","Client closed connection to broker.");
        } catch(SocketException e){
        } catch(IOException ioException) {
            ioException.printStackTrace();
        }
    }
}


