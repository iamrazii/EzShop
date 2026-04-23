package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class Searchlog {

    @DocumentId
    private String logId;
    private String userId; // Integer allows this to be 'null' for guest searches
    private String searchQuery;

    private long timestamp;

    public Searchlog() {} // Required empty constructor

    public Searchlog(String logId, String userId,String searchQuery, long timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.searchQuery = searchQuery;
        this.timestamp = timestamp;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}