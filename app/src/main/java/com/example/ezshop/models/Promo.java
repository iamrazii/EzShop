package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class Promo {

    @DocumentId
    private String promoId;
    private String code;
    private double discountPercentage;

    public Promo() {}

    public Promo(String promoId, String code, double discountPercentage) {
        this.promoId = promoId;
        this.code = code;
        this.discountPercentage = discountPercentage;
    }

    public String getPromoId() { return promoId; }
    public void setPromoId(String promoId) { this.promoId = promoId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
}