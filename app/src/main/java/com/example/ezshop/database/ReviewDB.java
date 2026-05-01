package com.example.ezshop.database;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Review;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


// REVIEW
public class ReviewDB {
    private FirebaseFirestore cloudDb;
    public ReviewDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> addReview(Review review) {
        if (review.getReviewId() == null) review.setReviewId(cloudDb.collection("reviews").document().getId());
        return cloudDb.collection("reviews").document(review.getReviewId()).set(review);
    }

    public Task<QuerySnapshot> getReviewsByProductId(String productId) {
        return cloudDb.collection("reviews").whereEqualTo("productId", productId)
                .orderBy("reviewDate", Query.Direction.DESCENDING).get();
    }
}