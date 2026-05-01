package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class OrderItem {
    @DocumentId
    private String orderItemId;
    private String orderId;
    private String productId;
    private double priceAtPurchase;
    private int quantity;

    public OrderItem() {}

    public OrderItem(String orderItemId, String orderId, String productId, double priceAtPurchase, int quantity) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.priceAtPurchase = priceAtPurchase;
        this.quantity = quantity;
    }

    public String getOrderItemId() { return orderItemId; }
    public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public double getPriceAtPurchase() { return priceAtPurchase; }
    public void setPriceAtPurchase(double priceAtPurchase) { this.priceAtPurchase = priceAtPurchase; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}