package com.example.filiw.backend;

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

    /**
     * Constructor for Publisher
     * @param client Client responsible
     */
    public Publisher(Client client){
        this.client = client;
        try {
            out = new ObjectOutputStream(client.getSocket().getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sent message
     * @param mes Message to be sent
     */
    public synchronized void push(Value mes){
        try{
            if (Objects.isNull(mes)){
                return;
            }
            if(mes.gethasMultimediaFile()){
                ArrayList<Value> chunks = chunkMultimediaFile(mes.getMessage());
                for (Value chunk : chunks) {
                    out.writeObject(chunk);
                    out.flush();
                }
            }
            else if (mes.getMessage() != null && mes.getMessage().getBytes().length > sizeOfChunk){
                ArrayList<Value> chunks = chunkString(mes.getMessage());
                for (Value chunk : chunks) {
                    out.writeObject(chunk);
                    out.flush();
                }
            }
            else{
                out.writeObject(mes);
                out.flush();
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
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
        while (true){
            if(!client.getdesiredTopic().equals("STORIES")){
                System.out.println("Do you want to send a message? Press y for yes n for no"); 
                String answer= sc.nextLine().toUpperCase();
                if (answer.equals("Y")){
                    while (true){
                        System.out.println("Press 't' to send a text ,'m' for a mediafile (photo or video) and 'b' to go back if u changed your mind");   
                        String answer2= sc.nextLine().toUpperCase();
                        if (answer2.equals("T")){
                            System.out.println("Please type your text"); 
                            String mytext =  sc.nextLine();
                            Value mestext = new Value(client.getUsername(),mytext,false,false);
                            push(mestext);
                            break;
                        }   
                        else if (answer2.equals("M")){
                            System.out.println("Please type your name of the media file with its extension"); 
                            String mytext = sc.nextLine();
                            if (!(new File(mytext)).exists()){
                                System.out.println("Your media file does not exist."); 
                                continue;
                            }
                            Value mediaa = new Value(client.getUsername(),mytext,true,false);
                            push(mediaa);
                            break;
                        }
                        else if (answer2.equals("B")){
                            break;
                        }
                        else{
                            System.out.println("Invalid choice!");  
                        }
                        continue;
                    }     
                }
                else if(answer.equals("N")) {
                    client.writeToFile("[Publisher]: Client wants to disconnect.", false);
                    sc.close();
                    client.stopthreads=true;
                    Value exitmes = new Value(client.getUsername());
                    push(exitmes);
                    try {
                        client.closeClient();
                    }catch (SocketException a){}
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                else{
                    System.out.println("Invalid choice! Press y for yes n for no"); 
                }  
            }
            else{
                System.out.println("Type upload to upload a story type exit to exit from stories");
                String answer3= sc.nextLine().toUpperCase();
                if (answer3.equals("UPLOAD")){
                    System.out.println("Please type your name of photo or video with its extension"); 
                    String mytext =  sc.nextLine();
                    if (!(new File(mytext)).exists()){
                        System.out.println("Your media file does not exist."); 
                        continue;
                    }
                    Value m = new Value(client.getUsername(),mytext,true,false);
                    push(m);
                    break;
                }
                else if (answer3.equals("EXIT")){
                    client.writeToFile("[Publisher]: Client wants to disconnect.", false);
                    sc.close();
                    client.stopthreads=true;
                    Value exitmes = new Value(client.getUsername());
                    push(exitmes);
                    try {
                        client.closeClient();
                    }catch (SocketException a){}
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                else{
                    System.out.println("Invalid choice!");
                }
            }
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
