package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class Product {

    @DocumentId
    private String productId;
    private String storeId;
    private String categoryId;
    private String name;
    private String description;
    private String productimage;
    private double price;
    private String condition;
    private int weightGrams;
    private double ratingAverage;
    private int soldCount;

    public Product() {}

    public Product(String productId, int soldCount, int weightGrams, double ratingAverage, String condition, double price,
                   String description, String productimage, String name, String categoryId, String storeId) {
        this.productId = productId;
        this.soldCount = soldCount;
        this.weightGrams = weightGrams;
        this.ratingAverage = ratingAverage;
        this.condition = condition;
        this.price = price;
        this.description = description;
        this.name = name;
        this.categoryId = categoryId;
        this.storeId = storeId;
        this.productimage = productimage;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProductimage() { return productimage; }
    public void setProductimage(String productimage) { this.productimage = productimage; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public int getWeightGrams() { return weightGrams; }
    public void setWeightGrams(int weightGrams) { this.weightGrams = weightGrams; }

    public double getRatingAverage() { return ratingAverage; }
    public void setRatingAverage(double ratingAverage) {

        this.ratingAverage = Math.round(ratingAverage * 10.0) / 10.0;
    }

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }
}