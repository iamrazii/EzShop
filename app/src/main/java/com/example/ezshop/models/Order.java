package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class Order {

    @DocumentId
    private String orderId;
    private String userId;
    private String promoId;
    private String shippingAddress;
    private String paymentMethod;
    private double totalPrice;
    private String orderStatus;
    private long createdAt;

    public Order() {}

    public Order(String orderId, long createdAt, String orderStatus, double totalPrice,
                 String paymentMethod, String shippingAddress, String promoId, String userId) {
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
        this.promoId = promoId;
        this.userId = userId;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPromoId() { return promoId; }
    public void setPromoId(String promoId) { this.promoId = promoId; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}