package com.example.ezshop.models;


import com.google.firebase.firestore.DocumentId;

public class Category {

    @DocumentId
    private String categoryId;
    private String name;
    private String iconName;

    public Category() {}

    public Category(String categoryId, String name, String iconName) {
        this.categoryId = categoryId;
        this.name = name;
        this.iconName = iconName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}