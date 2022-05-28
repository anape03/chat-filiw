package com.example.filiw.backend;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.ArrayList;
import android.util.Pair;
import java.time.LocalDateTime;

public class BrokerActionsForClient extends Thread {
    ObjectInputStream in = null;    // Input Stream
    ObjectOutputStream out = null;  // Output Stream
    Socket connection = null;       // Socket responsible for connection
    Broker broker = null;           // Broker responsible for handling the requests
    String desiredTopic = "";       // Topic the client wants to access
   
    /**
     * Constructor for BrokerActionsForClient
     * @param broker Broker responsible for the requests
     * @param connection Socket handling the connection
     */
    public BrokerActionsForClient(Broker broker, Socket connection) {
        this.broker = broker;
        this.connection = connection;
        try {
            broker.writeToFile("[Broker]: Got a connection... Opening streams...", true);
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles first connection with the Client
     * @return true if to close connection with client after
     *         false if to keep connection with client after
     */
    private boolean firstConnect(){
        boolean topicalreadyin=false;
        try {
            Value receivedMes = (Value)in.readObject();
            desiredTopic = receivedMes.getMessage();
            int manager = managerBroker(desiredTopic);
            broker.activeClients.put(receivedMes.getSenter(),this);
            Value message = null;
            if (manager != broker.brokerNum){   // Client must change Broker
                broker.writeToFile("[Broker]: Client must change broker.", true);
                message = new Value("Broker"+broker.brokerNum, "yes "+manager, false, true);
                message.setNotification(true);
                out.writeObject(message);
                out.flush();
                return true;
            } else{                             // Client doesn't change Broker
                topicalreadyin = broker.registerdTopicClients.containsKey(desiredTopic);
                if (!topicalreadyin){
                    broker.registerdTopicClients.put(desiredTopic, new ArrayList<>());
                } else{
                    broker.writeToFile("[Broker]: Topic \"" + desiredTopic + "\" already exists.", true);
                }
                broker.registerdTopicClients.get(desiredTopic).add(receivedMes.getSenter());
            }
            message = new Value("Broker"+broker.brokerNum, "no", false, true);
            message.setNotification(true);
            out.writeObject(message);
            out.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return false;
    }

    /**
     * Sent message history to new client.
     */
    private void sentHistory(){
        // Stories
        if (desiredTopic.equals("STORIES")){
            LocalDateTime timenow= LocalDateTime.now();
            for (int i =0; i< broker.topicStories.size();i++){
                if(!timenow.isAfter(broker.topicStories.get(i).second.plusSeconds(60))){ // only show stories created in the last 60 seconds
                    try{
                        out.writeObject(broker.topicStories.get(i).first);
                        out.flush();
                    } catch (IOException ioe){
                        ioe.printStackTrace();
                    }
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return;
        }
        // Conversations
        if (!broker.topicHistory.containsKey(desiredTopic)){
            return;
        }
        for (Value message : broker.topicHistory.get(desiredTopic)){
            try{
                out.writeObject(message);
                out.flush();
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Keep conversation history. Add new message to history.
     * @param message Message to be added.
     */
    private void addToHistory( Value message){
        if (desiredTopic.equals("STORIES")){
            broker.topicStories.add(new Pair<>(message, LocalDateTime.now()));
            return;
        }
        // If new topic (aka no message history) create
        if (!broker.topicHistory.containsKey(desiredTopic)){
            broker.topicHistory.put(desiredTopic, new ArrayList<>());
        }
        // Add message to history
        broker.topicHistory.get(desiredTopic).add(message);
    }

    /**
     * Finds the appropriate broker to handle the topic
     * @param topic the topic name requested by the client
     * @return broker number responsible for topic
     */
    private int managerBroker(String topic){
        int topicHash = topic.hashCode();
        int brokerNum = -1;
        for (int i=0; i<broker.brokerHash.size(); i++){
            if (broker.brokerHash.get(i).second < topicHash){
                brokerNum = i;
            }
        }
        return brokerNum;
    }

    @Override
    public void run() {
        broker.writeToFile("[Broker]: Connection is made at port: " + connection.getPort(), true);
        try {
            firstConnect();
            sentHistory();
            while(true){
                Object mes = in.readObject();
                // If exit message
                if (((Value)mes).getExit()){
                    broker.writeToFile("[Broker]: Disconnecting Client...", true);
                    removeClient(((Value)mes).getSenter());
                    break;
                }
                // Add to log
                broker.writeToFile("[Broker]: Message Received: "+mes, true);
                
                if (((Value)mes).getMessage().equals("")){
                    continue;
                }
                
                // Add message to topic history
                addToHistory((Value)mes);
                // Send to registered clients
                for (int z=0; z<broker.registerdTopicClients.get(desiredTopic).size(); z++){
                    String username = broker.registerdTopicClients.get(desiredTopic).get(z);
                    if (!username.equals(((Value)mes).getSenter())){
                        broker.activeClients.get(username).push(mes);
                    }
                }
                
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException classNFException) {
            classNFException.printStackTrace();
        } 
    }

    /**
     * Remove client from Broker's lists and disconnect them.
     * @param name client name to be disconnected
     */
    private void removeClient(String name){
        broker.writeToFile("[Broker]: Disconnecting Client \""+name+"\"", true);
        broker.activeClients.remove(name);
        if (broker.registerdTopicClients.containsKey(desiredTopic)) {
            for (String c:broker.registerdTopicClients.get(desiredTopic)){
                if (c.equals(name)){
                    broker.writeToFile("[Broker]: Client "+name+" will be removed.", true);
                    broker.registerdTopicClients.get(desiredTopic).remove(c);
                    break;
                }
            }
        }
        this.closee();
    }

    /**
     * Close connection with client and close streams
     */
    public void closee(){
        try {
            if (!Objects.isNull(in)){
                in.close();
                in = null;
                broker.writeToFile("[Broker]: Input stream from client closed.", true);
            }
            if (!Objects.isNull(out)){
                out.close();
                out = null;
                broker.writeToFile("[Broker]: Output stream to client closed.", true);
            }
            connection.close();
            this.interrupt();
            broker.writeToFile("[Broker]: Client disconnected.", true);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        
    }

    /**
     * Sent message to client assigned
     * @param mes Message to be sent
     */
    public void push(Object mes){
        try {
            out.writeObject(mes);
            out.flush();
            broker.writeToFile("[Broker]: Message sent.", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
