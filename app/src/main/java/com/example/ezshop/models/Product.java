package com.example.ezshop.models;

public class Product {
    private int productId;
    private int storeId;
    private int categoryId;
    private String name;
    private String description;
    private int productimage;
    private double price;
    private String condition;
    private int weightGrams;
    private double ratingAverage;
    private int soldCount;

    public Product() {}


    public Product(int productId, int soldCount, int weightGrams, double ratingAverage, String condition, double price,
                   String description,int image ,String name, int categoryId, int storeId) {
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
        this.productimage = image;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getWeightGrams() {
        return weightGrams;
    }

    public void setWeightGrams(int weightGrams) {
        this.weightGrams = weightGrams;
    }

    public double getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }
}
