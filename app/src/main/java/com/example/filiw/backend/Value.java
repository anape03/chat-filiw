package com.example.filiw.backend;

import java.io.Serializable;

public class Value implements Serializable, Comparable<Value>{
    MultimediaFile multimediaFile;
    String message = null;
    Boolean exit = null;
    Boolean fromBroker = false;
    Boolean hasMultimediaFile = false;
    String senter = null;
    boolean notification = false; // Message with info from broker

    /**
     * Constructor
    */
   
    public Value(String senter, String mes, Boolean mm, Boolean fromBroker){
        this.senter = senter;
        this.message = mes;
        this.fromBroker = fromBroker;
        this.exit = false;
        this.hasMultimediaFile = mm;

    }

    public Value(String senter){
        this.senter = senter;
        this.exit = true;
    }

    /**
     * Setters, Getters, toString, CompareTo methods
     */

    public void setMultimediaFile(MultimediaFile mf) { this.multimediaFile = mf;}
    public void setFromBroker(Boolean val) { this.fromBroker = val; }
    public void setNotification(Boolean val) { this.notification = val; }

    public Boolean getExit() { return exit; }
    public Boolean gethasMultimediaFile(){ return this.hasMultimediaFile; }
    public String getMessage() { return message; }
    public String getSenter() { return senter; }
    public Boolean getNotification() { return notification; }

    @Override
    public String toString(){
        return "["+senter+"]: "+message;
    }

    @Override
    public int compareTo(Value second) {
        if(this.multimediaFile.getChunkID() > second.multimediaFile.getChunkID())
            return 1;
        else
            return -1;
    }
}
