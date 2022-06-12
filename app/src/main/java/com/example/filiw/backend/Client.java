package com.example.filiw.backend;

import android.util.Log;

import com.example.filiw.activities.activity_show_chat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
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
    activity_show_chat activity;
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
    public Client(String username, String topicName, activity_show_chat activity) {
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
    public activity_show_chat getActivity(){ return activity; }
    public Consumer getConsumer() { return (Consumer)consumer; }
    public Publisher getPublisher() { return (Publisher)publisher; }
    public void setBrokerAddresses(ArrayList<Address> array) { brokerAddresses = array; }
    public void setAddressBroker(){ address = getRandomBroker(); }

    /**
     * Get random broekr address to connect to
     * @return Broker Address to connect to
     */
    public Address getRandomBroker(){
        Log.e("BROKER_ADDRESS","Broker addresses found: "+brokerAddresses); //-0
        int rnd = new Random().nextInt(brokerAddresses.size());
        return new Address(brokerAddresses.get(rnd).getIp(), brokerAddresses.get(rnd).getPort());
    }
            
    @Override
    public void run() {
//        Scanner sc = new Scanner(System.in);
//        System.out.print("Choose a username: ");
//        username = sc.nextLine();
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

    public Client connectToBroker(String topicName){
        Log.e("BROKER_ADDRESS", "Is broker address reachable: "+sendPingRequest(address.getIp()));
        desiredTopic = topicName;
        Log.e("CONNECTION","Trying to connect to topic: "+desiredTopic);
        boolean changeBroker = getTopicInfo();
        try {
            if (changeBroker){
                Log.e("CONNECTION", "Must connect to broker address "+address); //-0
                requestSocket = new Socket(address.getIp(), address.getPort());
                publisher = new Publisher(this);
                consumer = new Consumer(this);
                ((Publisher)publisher).push(new Value(this.getUsername(), desiredTopic, false, false));
            }
            ((Publisher)publisher).push(new Value(this.getUsername(), "", false, false));
        } catch (UnknownHostException unknownHost) {
            Log.e("CONNECTION","You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
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
//        Scanner sc = new Scanner(System.in);
//        System.out.print("What topic would you like to access? Type Stories to see stories: ");
//        desiredTopic = sc.nextLine().toUpperCase();
        
        try {
            Log.e("CONNECTION", "Creating socket to broker: "+address);
            requestSocket = new Socket(address.getIp(), address.getPort()); // TODO: it gets stuck? somehow
            Log.e("CONNECTION","Request socket: "+requestSocket);
            publisher = new Publisher(this);
            consumer = new Consumer(this);
            if (requestSocket!=null){
                Value message = new Value(this.username, desiredTopic, false, false);
                ((Publisher)publisher).push(message);
                String[] data = ((Consumer)consumer).register().split(" ");
                Log.e("TOPIC_INFO","Received topic info: "+data);
                if (data[0].equals("yes")){
                    String ip = brokerAddresses.get(Integer.parseInt(data[1])).getIp();
                    address.setIp(ip);
                    int port = brokerAddresses.get(Integer.parseInt(data[1])).getPort();
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


