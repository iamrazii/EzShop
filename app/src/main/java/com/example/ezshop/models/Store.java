package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class Store {

    @DocumentId
    private String storeId;
    private String storeName;
    private String location;
    private String status;
    private String owner_id;
    private double rating;

    public Store() {}

    public Store(String storeId, String storeName, String location, String status, double rating, String owner_id) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.location = location;
        this.status = status;
        this.rating = rating;
        this.owner_id = owner_id;
    }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOwner_id() { return owner_id; }
    public void setOwner_id(String owner_id) { this.owner_id = owner_id; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}