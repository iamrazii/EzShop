package com.example.ezshop.models;

public class Store {
    private int storeId;
    private String storeName;
    private String location;
    private String status;
    private double rating;

    public Store() {}

    public Store(int storeId, String storeName, String location, String status, double rating) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.location = location;
        this.status = status;
        this.rating = rating;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}