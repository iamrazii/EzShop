package com.example.ezshop.models;

public class Searchlog {
    private int logId;
    private Integer userId; // Integer allows this to be 'null' for guest searches
    private String searchQuery;

    private long timestamp;

    public Searchlog() {} // Required empty constructor

    public Searchlog(int logId, Integer userId,String searchQuery, long timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.searchQuery = searchQuery;
        this.timestamp = timestamp;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
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