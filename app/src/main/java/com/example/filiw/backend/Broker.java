package com.example.filiw.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

public class Broker extends Node {

    HashMap<Address,Integer> brokerHash;                // Broker Address - Broker Hash
    ArrayList<Integer> sortedBrokerHash;                // Sorted Hashes for Brokers
    HashMap<String,Integer> topicHash;                  // Topic Name - Topic Hash
    HashMap<Value,LocalDateTime> topicStories;          // Story (Value object) - Time uploaded
    HashMap<String,BrokerActionsForClient> activeClients;       // Username, connection with client 
    HashMap<String,ArrayList<String>> registerdTopicClients;    // Topic and registered Client 
    HashMap<String,ArrayList<Value>> topicHistory;              // Topic name - Topic history

    Address address;                // Broker Address
    ServerSocket brokerServerSocket;// Broker Server Socket
    int brokerNum;                  // Broker Number

    public Integer getBrokerHash(int num){
        return sortedBrokerHash.get(num);
    }



    /**
     * Constructor for Broker
     * @param num Broker number, in order to retrieve address
     */
    public Broker(int num){
        super();
        this.brokerNum = num-1;
        this.address = brokerList.get(this.brokerNum);
        this.activeClients          = new HashMap<>();
        this.registerdTopicClients  = new HashMap<>();
        this.topicHistory           = new HashMap<>();
        this.topicStories           = new HashMap<>();
        this.sortedBrokerHash	    = new ArrayList<>();

        this.createLogFile("Broker"+(num)+"-log.txt");
        System.out.println("[Broker]: Broker log file created.");
        this.writeToFile("[Broker]: Broker Initialized ("+address+")", true);
    }

    /**
     * Initialiser for brokers
     */
    public void init(){
        calculateKeys();
        openServer();
    }

    /**
     * Opens Broker Server and waits for requests
     */
    private void openServer(){
        try{
            brokerServerSocket = new ServerSocket(address.getPort());
            this.writeToFile("[Broker]: Ready to accept requests.", true);
            Socket clientSocket;
            while (true){
                clientSocket = brokerServerSocket.accept();
                Thread clientThread = new BrokerActionsForClient(this, clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                brokerServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculate keys(hashcode) for brokers, sort brokers,
     * and calculate hashes for pre-configed topics
     */
    private void calculateKeys(){
        // Get Broker addresses
        brokerHash = new HashMap<>();
        for (Address ad : brokerList){
            brokerHash.put(ad,(ad.getIp()+ad.getPort()).hashCode());
        }

        sortBrokerHashes();

        // Get topic names and hashcodes
        ArrayList<String> topics = readTopics();
        topicHash = new HashMap<>();
        for (String t : topics){
            topicHash.put(t, t.hashCode());
        }
    }

    private void sortBrokerHashes(){
        for (Address address : brokerHash.keySet()){
            sortedBrokerHash.add(brokerHash.get(address));
        }
        // Sort brokers by hash
        Collections.sort(sortedBrokerHash);
    }

    /**
     * Helper method to read topic names and previous messages from configuration file.
     * @return Arraylist with topic names.
     */
    private ArrayList<String> readTopics(){
        ArrayList<String> topics = new ArrayList<String>();
//        try {
//            File confFile = new File("conf.txt");
//            Scanner confReader = new Scanner(confFile);
//            String line = confReader.nextLine();
//            while (confReader.hasNextLine() && line != "%") {
//                line = confReader.nextLine();
//            }
//            ArrayList<Value> history = new ArrayList<>();
//            while (confReader.hasNextLine()){
//                line = confReader.nextLine();
//                String[] broker_topic_chat = line.split("#");
//                if (broker_topic_chat[0].equals(Integer.valueOf(brokerNum+1))){ // only read topics for specific broker
//                    String topic = broker_topic_chat[1];
//                    String[] whole_messages = broker_topic_chat[2].split("%"); // each item includes sender and message
//                    for (String item : whole_messages){
//                        String[] sender_message = item.split(":");
//                        String sender = sender_message[0];
//                        String message = sender_message[1];
//                        history.add(new Value(sender,message, false, false));
//                        topicHistory.put(topic, history);
//                    }
//                }
//            }
//            confReader.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }

        ArrayList<Value> history;
        String topic;
        String sender;
        String message;
        switch (brokerNum+1){
            case 1:

                this.writeToFile("[Broker]: Reading Topic info for Broker1...", true);

                history = new ArrayList<>();
                topic = "campfire";
                topicHistory.put(topic, history);
                topics.add(topic);

                break;
            case 2:

                this.writeToFile("[Broker]: Reading Topic info for Broker2...", true);

                history = new ArrayList<>();
                topic = "FIREE";
                sender = "ham";
                message = "this is so random tm";
                history.add(new Value(sender,message, false, false));
                sender = "blob";
                message = "you like trains?";
                history.add(new Value(sender,message, false, false));
                sender = "i like trains";
                message = "trains do be very cool actually, idk if you guys are aware";
                history.add(new Value(sender,message, false, false));
                topicHistory.put(topic, history);
                topics.add(topic);

                break;
            case 3:

                this.writeToFile("[Broker]: Reading Topic info for Broker3...", true);

                history = new ArrayList<>();
                topic = "The Umbrella Academy";
                topicHistory.put(topic, history);
                topics.add(topic);

                break;
        }

        return topics;
    }
}

