package com.example.filiw.backend;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Publisher Class sents messages for Client
 */
public class Publisher extends Node {
    Client client = null;
    ObjectOutputStream out = null;
    int sizeOfChunk = 1024 * 512;// 0.5MB = 512KB
    Value itemToSent = null;

    /**
     * Constructor for Publisher
     * @param client Client responsible
     */
    public Publisher(Client client){
        this.client = client;
        try {
            out = new ObjectOutputStream(client.getSocket().getOutputStream());
            Log.e("CONNECTION","Output Stream created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setItemToSent(Value value){
        itemToSent = value;
    }

    /**
     * Sent message
     * @param mes Message to be sent
     */
    public synchronized void push(Value mes){
        Log.e("PUSH","Attempting to send message \""+mes.getMessage()+"\" to broker from \""+mes.getSenter()+"\"...");
        try{
            if (Objects.isNull(mes)){ // empty message
                return;
            }
            if(mes.gethasMultimediaFile()){ // multimedia file
                Log.e("PUSH","Multimedia file item.");
                ArrayList<Value> chunks = chunkMultimediaFile(mes.getMessage());
                for (Value chunk : chunks) {
                    out.writeObject(chunk);
                    out.flush();
                }
            }
            else if (mes.getMessage() != null && mes.getMessage().getBytes().length > sizeOfChunk){ // big text
                ArrayList<Value> chunks = chunkString(mes.getMessage());
                for (Value chunk : chunks) {
                    out.writeObject(chunk);
                    out.flush();
                    Log.e("PUSH","Big text message sent to broker.");
                }
            }
            else{ // small text
                out.writeObject(mes);
                out.flush();
                Log.e("PUSH","Small text message sent to broker.");
            }
        } catch (UnknownHostException unknownHost) {
            Log.e("CONNECTION","You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Log.e("PUSH","Message sent to broker.");
    }

    /**
     * Handles Chunks for Text messages
     * @param message text message
     * @return arraylist of chunks
     */
    public ArrayList<Value> chunkString(String message) {
        ArrayList<Value> chunks = new ArrayList<>();
        byte[] buffer;
        int chunkID = 0;
        byte[] messBytes = message.getBytes();

        for (int i = 0; i < messBytes.length; i += sizeOfChunk) {
            buffer = new byte[sizeOfChunk];
            buffer = Arrays.copyOfRange(messBytes, i, i+sizeOfChunk);
            MultimediaFile chunk = new MultimediaFile("str.STRING", buffer, chunkID);
            Value chunkVal= new Value(client.getUsername(), ".STRING", true, false);
            chunkVal.setMultimediaFile(chunk);
            chunks.add(chunkVal);
            chunkID++;
        }
        Value chunkVal= new Value(client.getUsername(), Integer.toString(chunkID), true, false);
        chunks.add(chunkVal);

        return chunks;
	}	

    /**
     * Handles Chunks for Multimedia file messages
     * @param fileName File name for multimedia file
     * @return arraylist of chunks
     */
    public ArrayList<Value> chunkMultimediaFile(String fileName) {
        ArrayList<Value> chunks = new ArrayList<>();
        byte[] buffer;
        try {
            File myFile = new File(fileName);
            FileInputStream fis = new FileInputStream(fileName);
            int chunkID = 0;
            
            for (int i = 0; i < myFile.length(); i += sizeOfChunk) {
                buffer = new byte[sizeOfChunk];
                fis.read(buffer);
                MultimediaFile chunk = new MultimediaFile(fileName, buffer, chunkID);
                Value chunkVal= new Value(client.getUsername(), fileName, true, false);
                chunkVal.setMultimediaFile(chunk);
                chunks.add(chunkVal);
                chunkID++;
            }
            Value chunkVal= new Value(client.getUsername(), Integer.toString(chunkID), true, false);
            chunks.add(chunkVal);
            fis.close();
            return chunks;
        } catch ( IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
	}	
    
    @Override
    public void run(){
        Scanner sc = new Scanner(System.in);
        boolean exit = false;
        while (!exit){
            if(!client.getdesiredTopic().equals("STORIES")){
                System.out.println("Do you want to send a message? Press y for yes n for no");
                String answer= sc.nextLine().toUpperCase();
                if (answer.equals("Y")){
                    System.out.print("Enter message type: ");
                    String type = sc.nextLine().toUpperCase();
                    System.out.print("Enter message: ");
                    String message = sc.nextLine();

                    sendMessage(type, message);
                }
                else if(answer.equals("N")) {
                    sc.close();
                    exitRequest();
                    break;
                }
                else{
                    System.out.println("Invalid choice! Press y for yes n for no"); 
                }  
            }
            else{
                System.out.print("Enter action (upload | exit): ");
                String action = sc.nextLine().toUpperCase();
                String filename = null;
                if (action.equals("UPLOAD")) {
                    System.out.print("Enter filename: ");
                    filename = sc.nextLine();
                }
                exit = handleStories(action, filename);
            }
        }        
    }

    /**
     * Send message to broker
     * @param type Type of message (TEXT, MULTIMEDIA)
     * @return whether message value was successfully created
     *          exit code  0: successful
     *          exit code -1: invalid type input
     *          exit code -2: file not found
     */
    public int sendMessage(String type, String message){
        switch(type) {
            case "TEXT":
                Value mestext = new Value(client.getUsername(), message, false, false);
                push(mestext);
                return 0;
            case "MULTIMEDIA":
                if (!(new File(message)).exists()) {
                    Log.e("MEDIA_FILE","Media file does not exist.");
                    return -2;
                }
                Value mediaa = new Value(client.getUsername(), message, true, false);
                push(mediaa);
                return 0;
            default:
                return -1;
        }
    }

    /**
     * Handle stories page
     * @param action Action to be taken (UPLOAD, EXIT)
     * @param filename name of file to be uploaded
     * @return whether to exit or not
     */
    public boolean handleStories(String action, String filename){
        switch(action) {
            case "UPLOAD":
                if (!(new File(filename)).exists()) {
                    Log.e("MEDIA_FILE","Media file does not exist.");
                    return false;
                }
                Value m = new Value(client.getUsername(), filename, true, false);
                push(m);
                return true;
            case "EXIT":
                client.writeToFile("[Publisher]: Client wants to disconnect.", false);
                client.stopthreads = true;
                Value exitmes = new Value(client.getUsername());
                push(exitmes);
                try {
                    client.closeClient();
                } catch (SocketException a) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return false;
        }
    }

    /**
     * Client has requested to exit
     */
    public void exitRequest(){
        client.writeToFile("[Publisher]: Client wants to disconnect.", false);
        client.stopthreads = true;
        Value exitmes = new Value(client.getUsername());
        push(exitmes);
        try {
            client.closeClient();
        }catch (SocketException a) {
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close Output stream
     */
    public void closee(){
        try {
            if (!Objects.isNull(out)){
                out.close();
                out = null;
                client.writeToFile("[Publisher]: Output closed.", false);
            }
        }catch (SocketException a){}
         catch (IOException ioException) {
            ioException.printStackTrace();
        } 
    }
}
