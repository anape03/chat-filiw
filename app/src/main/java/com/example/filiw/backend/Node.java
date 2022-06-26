package com.example.filiw.backend;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Node extends Thread {
    public ArrayList<Address> brokerList;
    FileWriter logFile;
    BufferedWriter output;

    public Node(){
        brokerList = readAddresses();
    }

    /**
     * Helper method to read IP addresses and port number for brokers
     * from configuration file.
     * @return Arraylist with Address Objects containing IP address 
     *         and port for every broker.
     */
    protected ArrayList<Address> readAddresses(){
        ArrayList<Address> addresses = new ArrayList<Address>();
//        try {
//            File confFile = new File("conf.txt");
//            System.out.println("File exists: "+confFile.exists()); //-0
//            System.out.println("Can read file: "+confFile.canRead()); //-0
//            System.out.println("File canonical path: "+ confFile.getCanonicalPath()); //-0
//            Scanner confReader = new Scanner(confFile);
//            String line = confReader.nextLine();
//            while (!line.equals("%")) {
//                System.out.println("READING_CONFIGURATION_ADDRESSES - "+line); //-0
//                String ipport[]= line.split(" ");
//                Address address = new Address(ipport[0], Integer.parseInt(ipport[1]));
//                addresses.add(address);
//                line = confReader.nextLine();
//            }
//            confReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        addresses.add(new Address("192.168.1.11", 6000)); //-0
        addresses.add(new Address("192.168.1.11", 7000)); //-0
        addresses.add(new Address("192.168.1.11", 8000)); //-0
        return addresses;
    }

    /**
     * Create new log file (if needed)
     * @param name File name
     * @return FileWriter object
     */
    protected FileWriter createLogFile(String name){ 
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
    protected void writeToFile(String info, boolean print){ 
//        try {
            if (print){
                System.out.println(info);
            }
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

