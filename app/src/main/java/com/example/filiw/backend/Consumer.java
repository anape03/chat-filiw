package com.example.filiw.backend;

import java.net.UnknownHostException;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.net.SocketException;

/**
 * Consumer Class receives messages for Client
 */
public class Consumer extends Node {
    Client client = null;
    ObjectInputStream in = null;

    /**
     * Constructor for Consumer
     * @param client Client responsible
     */
    public Consumer(Client client){
        this.client = client;
        try {
            in = new ObjectInputStream(this.client.getSocket().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive info for new connection from the randomly chosen broker
     * @return message received
     */
    public String register(){
        try{
            Value response = (Value)in.readObject();
            return response.getMessage(); // message is in form "<change broker: yes/no> <brokernum if needed>"
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException cnfException){
            cnfException.printStackTrace();
        }
        return "";
    }

    /**
     * Receive messages and show conversation on screen
     */
    public synchronized void showConversationData(){
        try{
            ArrayList<Value> chunksOfMess = new ArrayList<>();
            while(true){
                if (Objects.isNull(in) || Objects.isNull(client.getConnection())){
                    break;
                }
                
                if (client.Alivesocket){
                    Value mess = (Value)in.readObject();
                    if(mess.multimediaFile!= null){
                        chunksOfMess.add(mess);
                        Collections.sort(chunksOfMess);
                        continue;
                    }
                    if(mess.hasMultimediaFile && Integer.parseInt(mess.message) == chunksOfMess.size()){
                        if(chunksOfMess.get(0).message.equals(".STRING")){
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            for (Value chunk : chunksOfMess) {
                                baos.write(chunk.multimediaFile.getChunkData());
                            }
                            byte[] strByteArray = baos.toByteArray();
                            String allMessage = new String(strByteArray);
                            System.out.println(allMessage);
                            baos.close();
                        }
                        else{
                            FileOutputStream fos = new FileOutputStream(chunksOfMess.get(0).message);
                            for (Value chunk : chunksOfMess) {
                                fos.write(chunk.multimediaFile.getChunkData());
                            }
                            fos.close();
                            System.out.println(chunksOfMess.get(0));
                        }
                        chunksOfMess.clear();
                        continue;
                    }
                    if (!mess.getNotification()){
                        client.writeToFile("[Consumer]: Message received: "+mess, false);
                        System.out.println(mess);
                        client.getActivity().receiveMessage(mess); // send message to activity
                    }
                }
            }

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (SocketException se){
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (ClassNotFoundException classNotFoundException){
            classNotFoundException.printStackTrace();
        } 
    }

    @Override
    public void run(){
        if (!Objects.isNull(in)){
            showConversationData();
        }
    }
    
    /**
     * Close Input Stream
     */
    public void closee(){
        try {
            if (!Objects.isNull(in)){
                in.close();
                in = null;
                client.writeToFile("[Consumer]: Input closed.", false);
            }
        }catch (SocketException a){}
         catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
