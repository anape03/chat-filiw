package com.example.filiw.backend;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

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
     * @return type of connection "ALL_TOPICS_INFO", "TOPIC_CONNECT"
     */
    private String firstConnect(){
        try {
            Value receivedMes = (Value)in.readObject();
            String clientName = receivedMes.getSenter();
            String messageFromClient = receivedMes.getMessage();
            if (messageFromClient.equals("INITIALISATION_MESSAGE")){
                broker.writeToFile("[Broker]: Client \""+clientName+"\" sent initialisation message.",true);
                receivedMes = (Value)in.readObject();
                clientName = receivedMes.getSenter();
                messageFromClient = receivedMes.getMessage();
            }
            broker.writeToFile("[Broker]: Received message from \""+clientName+"\": "+messageFromClient,true);
            if (messageFromClient.equals("ALL_TOPIC_INFO")){
                broker.writeToFile("[Broker]: Client \""+clientName+"\" requests all topic info.",true);
                String all_topic_info = getAllTopicInfo();
                sendToClient(all_topic_info);
                broker.writeToFile("[Broker]: Topic info sent to \""+clientName+"\".",true);
                return "ALL_TOPICS_INFO";
            }
            desiredTopic = messageFromClient;
            broker.writeToFile("[Broker]: Client requests to connect to topic \""+desiredTopic+"\"", true);
            int manager = managerBroker(desiredTopic);
            broker.activeClients.put(receivedMes.getSenter(),this);

            if (manager != broker.brokerNum){   // Client must change Broker
                broker.writeToFile("[Broker]: Client must change broker.", true);
                sendToClient("yes "+manager);
                return "TOPIC_CONNECT";
            } else{                             // Client doesn't change Broker
                boolean topicalreadyin = broker.registerdTopicClients.containsKey(desiredTopic);
                if (!topicalreadyin){
                    broker.registerdTopicClients.put(desiredTopic, new ArrayList<>());
                } else{
                    broker.writeToFile("[Broker]: Topic \"" + desiredTopic + "\" already exists.", true);
                }
                broker.registerdTopicClients.get(desiredTopic).add(receivedMes.getSenter());
            }
            sendToClient("no");
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return "TOPIC_CONNECT";
    }

    /**
     * Get all topic info from broker.
     * (topic names and last message for each topic)
     * @return all info in format:
     * topic1&last_message_in_topic1%topic2&last_message_in_topic2....
     */
    private String getAllTopicInfo(){
        String all_topic_info = "";
        String sep_topics = "%";        // separator for different topics
        String sep_topic_mes = "&";     // separator for topic and it's last message
        for (String topic : broker.topicHistory.keySet()){
            ArrayList<Value> message_list = broker.topicHistory.get(topic);
            all_topic_info += topic + sep_topic_mes;
            if (message_list.size() != 0) {
                String sender = message_list.get(message_list.size() - 1).getSenter();
                String last_message = message_list.get(message_list.size() - 1).getMessage();
                all_topic_info += sender + ": " + last_message + sep_topics;
            }
        }
        return all_topic_info;
    }

    /**
     * Send message history to new client.
     */
    private void sendHistory(){
        // Stories
        if (desiredTopic.equals("STORIES")){
            LocalDateTime timenow= LocalDateTime.now();
            for (Value value : broker.topicStories.keySet()){
                if(!timenow.isAfter(broker.topicStories.get(value).plusSeconds(60))){ // only show stories created in the last 60 seconds
                    try{
                        out.writeObject(value);
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
            broker.topicStories.put(message, LocalDateTime.now());
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
        broker.writeToFile("[Broker]: Looking for broker responsible for topic with hash: "+topicHash, true);
        int brokerNum = 1;
        for (int i=0; i<broker.sortedBrokerHash.size(); i++){
            if (broker.getBrokerHash(i) < topicHash){
                brokerNum = i;
            }
        }
        broker.writeToFile("[Broker]: Broker responsible for topic \""+topic+"\" is Broker"+(brokerNum+1), true);
        return brokerNum;
    }

    @Override
    public void run() {
        broker.writeToFile("[Broker]: Connection is made at port: " + connection.getPort(), true);
        try {
            String connectionType = firstConnect();
            broker.writeToFile("[Broker]: Connection type: " + connectionType, true);
            if (!connectionType.equals("ALL_TOPICS_INFO")) {
                broker.writeToFile("[Broker]: Sending message history to client...", true);
                sendHistory();
            }
            while (true) {
//                Object mes = in.readObject();
                Object mes = readMessages();
                // If exit message
                if (((Value) mes).getExit()) {
                    broker.writeToFile("[Broker]: Disconnecting Client...", true);
                    removeClient(((Value) mes).getSenter());
                    break;
                }
                // Add to log
                broker.writeToFile("[Broker]: Message Received: " + mes, true);

                if (((Value) mes).getMessage().equals("")) {
                    continue;
                }

                // Add message to topic history
                addToHistory((Value) mes);
                // Send to registered clients
                for (int z = 0; z < broker.registerdTopicClients.get(desiredTopic).size(); z++) {
                    String username = broker.registerdTopicClients.get(desiredTopic).get(z);
//                    if (!username.equals(((Value)mes).getSenter())){
                    broker.activeClients.get(username).push(mes);
//                    }
                }

            }

        } catch (SocketException socketException){
        }catch (ClassNotFoundException classNFException) {
            classNFException.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized Object readMessages() throws IOException, ClassNotFoundException {
        return in.readObject();
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
        broker.writeToFile("[Broker]: Sending message to client...", true);
        try {
            out.writeObject(mes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        broker.writeToFile("[Broker]: Message sent.", true);
    }

    /**
     * Send string message to client and log action to broker
     * @param text string message to be sent
     */
    private void sendToClient(String text){
        broker.writeToFile("[Broker]: Sending message to client...", true);
        Value message = new Value("Broker"+(broker.brokerNum+1), text, false, true);
        message.setNotification(true);
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        broker.writeToFile("[Broker]: Sent message to client: "+message, true);
    }
}
