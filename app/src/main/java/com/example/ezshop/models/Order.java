package com.example.ezshop.models;

public class Order {
    private int orderId;
    private int userId;
    private Integer promoId; // Using Integer instead of int so it can be 'null' if no promo is used
    private String shippingAddress;
    private String paymentMethod;
    private double totalPrice;
    private String orderStatus;
    private long createdAt;

    public Order(int orderId, long createdAt, String orderStatus, double totalPrice,
                 String paymentMethod, String shippingAddress, Integer promoId, int userId) {
        this.orderId = orderId;
        this.createdAt = createdAt;
        this.orderStatus = orderStatus;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.shippingAddress = shippingAddress;
        this.promoId = promoId;
        this.userId = userId;
    }

    public Order() {}

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Integer getPromoId() {
        return promoId;
    }

    public void setPromoId(Integer promoId) {
        this.promoId = promoId;
    }
}