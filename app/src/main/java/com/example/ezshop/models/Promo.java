package com.example.ezshop.models;


public class Promo {
    private int promoId;
    private String code;
    private double discountPercentage;

    public Promo() {}

    public Promo(int promoId, String code, double discountPercentage) {
        this.promoId = promoId;
        this.code = code;
        this.discountPercentage = discountPercentage;
    }

    public int getPromoId() {
        return promoId;
    }

    public void setPromoId(int promoId) {
        this.promoId = promoId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}