package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class User {

    @DocumentId
    private String userId;
    private String name;
    private String email;
    private String passwordHash;
    private double walletBalance;
    private String defaultShippingAddress;

    public User() {}

    public User(String userId, String name, String email, String passwordHash, double walletBalance, String defaultShippingAddress) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.walletBalance = walletBalance;
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }

    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String defaultShippingAddress) { this.defaultShippingAddress = defaultShippingAddress; }
}