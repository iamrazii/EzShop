package com.example.ezshop.models;


public class Category {
    private int categoryId;
    private String name;
    private String iconName;

    public Category() {}

    public Category(int categoryId, String name, String iconName) {
        this.categoryId = categoryId;
        this.name = name;
        this.iconName = iconName;
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

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}