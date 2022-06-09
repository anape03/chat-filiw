package com.example.filiw.adapters;

public class ItemTopic {
    private String topicName;
    private String lastMessage;

    public ItemTopic(String topicName, String lastMessage) {
        this.topicName = topicName;
        this.lastMessage = lastMessage;
    }

    public String getTopicName() {
        return topicName;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
