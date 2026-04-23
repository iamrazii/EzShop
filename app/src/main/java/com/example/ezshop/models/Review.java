package com.example.ezshop.models;

import com.google.firebase.firestore.DocumentId;

public class Review {

    @DocumentId
    private String reviewId;
    private String productId;
    private String userId;
    private int rating;
    private String comment;
    private String reviewDate; // Kept as String to match "Feb 2022" format from Figma

    public Review() {}

    public Review(String reviewDate, String comment, int rating, String userId, String productId, String reviewId) {
        this.reviewDate = reviewDate;
        this.comment = comment;
        this.rating = rating;
        this.userId = userId;
        this.productId = productId;
        this.reviewId = reviewId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}