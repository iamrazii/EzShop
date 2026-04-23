package com.example.ezshop.database;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;
import java.util.ArrayList;
import java.util.UUID;
import com.example.ezshop.models.Review;

public class ReviewDB {
    private SQLiteDatabase database;
    public ReviewDB(SQLiteDatabase database) { this.database = database; }

    public String addReview(Review review) {
        ContentValues cv = new ContentValues();
        String newId = UUID.randomUUID().toString();
        cv.put("review_id", newId);
        cv.put("product_id", review.getProductId());
        cv.put("user_id", review.getUserId());
        cv.put("rating", review.getRating());
        cv.put("comment", review.getComment());
        cv.put("review_date", review.getReviewDate());
        database.insert("reviews", null, cv);
        return newId;
    }

    public ArrayList<Pair<Review,String>> getReviewsByProductId(String productId) {
        ArrayList<Pair<Review, String>> list = new ArrayList<>();
        String query = "SELECT reviews.*, users.name AS user_name FROM reviews INNER JOIN users ON reviews.user_id = users.user_id WHERE reviews.product_id = ? ORDER BY reviews.review_id DESC";
        Cursor cursor = database.rawQuery(query, new String[]{productId});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Review r = new Review();
                r.setReviewId(cursor.getString(cursor.getColumnIndexOrThrow("review_id")));
                r.setProductId(cursor.getString(cursor.getColumnIndexOrThrow("product_id")));
                r.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
                r.setRating(cursor.getInt(cursor.getColumnIndexOrThrow("rating")));
                r.setComment(cursor.getString(cursor.getColumnIndexOrThrow("comment")));
                r.setReviewDate(cursor.getString(cursor.getColumnIndexOrThrow("review_date")));
                list.add(new Pair<>(r, cursor.getString(cursor.getColumnIndexOrThrow("user_name"))));
            }
            cursor.close();
        }
        return list;
    }
}