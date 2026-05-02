package com.example.ezshop.models;


public class InboxItem {
    public String partnerId; // The ID of the person they are talking to
    public String lastMessage;
    public long timestamp;

    public InboxItem() {}

    public InboxItem(String partnerId, String lastMessage, long timestamp) {
        this.partnerId = partnerId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }
}