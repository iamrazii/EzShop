package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import com.example.ezshop.models.Review;

public class ReviewDB {
    private SQLiteDatabase database;

    private static final String TABLE_REVIEWS = "reviews";
    private static final String COLUMN_ID = "review_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_RATING = "rating";
    private static final String COLUMN_COMMENT = "comment";
    private static final String COLUMN_DATE = "review_date";

    public ReviewDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addReview(Review review) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PRODUCT_ID, review.getProductId());
        cv.put(COLUMN_USER_ID, review.getUserId());
        cv.put(COLUMN_RATING, review.getRating());
        cv.put(COLUMN_COMMENT, review.getComment());
        cv.put(COLUMN_DATE, review.getReviewDate());
        return database.insert(TABLE_REVIEWS, null, cv);
    }

    public ArrayList<Review> getReviewsByProductId(int productId) {
        ArrayList<Review> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_REVIEWS, null, COLUMN_PRODUCT_ID + "=?", new String[]{String.valueOf(productId)}, null, null, COLUMN_ID + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Review r = new Review();
                r.setReviewId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                r.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                r.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                r.setRating(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RATING)));
                r.setComment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT)));
                r.setReviewDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                list.add(r);
            }
            cursor.close();
        }
        return list;
    }
}