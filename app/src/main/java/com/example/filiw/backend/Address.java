package com.example.filiw.backend;

import java.io.Serializable;

/**
 * Address represents an address (ip, port)
 */
public class Address implements Serializable {

    private int port;
    private String ip;

    /**
     * Constructor
     * @param ip
     * @param port
     */
    public Address(String ip,int port) {
        this.port = port;
        this.ip = ip;
    }

    /**
     * Getters 
     */
    public int getPort() { return port; }

    public String getIp() { return ip; }

    public void setIp(String ip){ this.ip = ip; }

    public void setPort(int port){ this.port = port; }

    /**
     * Used to check if 2 Address obj are the same (same port and ip)
     * @param address the Address obj that we are comparing this obj with
     * @return boolean true if addresses are the same, false if they are not
     */
    public boolean compare(Address address){
        return (this.getIp().equals(address.getIp()) && this.getPort()==address.getPort()) || (this == address);
    }

    /**
     * @Override of toString() method
     * @return Readable Address string
     */
    @Override
    public String toString() {
        return "Port: " + this.port + " IP: " + this.ip;
    }
}

