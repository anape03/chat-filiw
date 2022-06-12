package com.example.filiw.backend;

import android.util.Log;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Node extends Thread {
    public final ArrayList<Address> brokerList = readAddresses();
    FileWriter logFile;
    BufferedWriter output;

    /**
     * Helper method to read IP addresses and port number for brokers
     * from configuration file.
     * @return Arraylist with Address Objects containing IP address 
     *         and port for every broker.
     */
    protected ArrayList<Address> readAddresses(){
        ArrayList<Address> addresses = new ArrayList<Address>();
        try {
            File confFile = new File("conf.txt");
            Log.e("CONFIG_FILE", "File exists: "+confFile.exists()); //-0
            Log.e("CONFIG_FILE","Can read file: "+confFile.canRead()); //-0
            Log.e("CONFIG_FILE", "File canonical path: "+ confFile.getCanonicalPath()); //-0
            Scanner confReader = new Scanner(confFile);
            String line = confReader.nextLine();
            while (!line.equals("%")) {
                Log.e("READING_CONFIGURATION_ADDRESSES",line); //-0
                String ipport[]= line.split(" ");
                Address address = new Address(ipport[0], Integer.parseInt(ipport[1]));
                addresses.add(address);
                line = confReader.nextLine();
            }
            confReader.close();
        }catch (FileNotFoundException e) {
            Log.e("CONFIG_FILE_EXC", "Configuration file wasn't found.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        addresses.add(new Address("192.168.2.5", 6000)); //-0
//        addresses.add(new Address("192.168.2.5", 7000)); //-0
//        addresses.add(new Address("192.168.2.5", 8000)); //-0
        return addresses;
    }

    /**
     * Create new log file (if needed)
     * @param name File name
     * @return FileWriter object
     */
    protected FileWriter createLogFile(String name){ //TODO: fix file things
//        try {
//            logFile = new FileWriter(name, false);
//            output = new BufferedWriter(logFile);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return logFile;
    }

    /**
     * Write info to log file
     * @param info Info to be added
     * @param print Whether to also print info on screen
     */
    protected void writeToFile(String info, boolean print){ //TODO: fix file things
//        try {
//            if (print){
//                System.out.println(info);
//            }
//            output.write(info + "\n");
//            output.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Close File Input
     */
    protected void closeFile(){
        try {
            output.close();
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

