package com.example.ezshop.models;

public class Review {
    private int reviewId;
    private int productId;
    private int userId;
    private int rating;
    private String comment;
    private String reviewDate; // Kept as String to match "Feb 2022" format from Figma

    public Review() {}

    public Review(String reviewDate, String comment, int rating, int userId, int productId, int reviewId) {
        this.reviewDate = reviewDate;
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.productId = productId;
        this.reviewId = reviewId;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}