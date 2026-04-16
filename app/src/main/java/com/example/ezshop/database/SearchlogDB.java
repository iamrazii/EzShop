package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import com.example.ezshop.models.Searchlog;

public class SearchlogDB {
    private SQLiteDatabase database;

    private static final String TABLE_SEARCH = "search_logs";
    private static final String COLUMN_USER_ID = "user_id";

    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_QUERY = "search_query";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public SearchlogDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addSearch(String query,Integer productId ,Integer userId) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_QUERY, query);
        cv.put(COLUMN_USER_ID, userId);
        cv.put(COLUMN_PRODUCT_ID,productId);
        cv.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        return database.insert(TABLE_SEARCH, null, cv);
    }

    public ArrayList<String> getTrendingSearches() { // top 5 searches
        ArrayList<String> trending = new ArrayList<>();
        String rawSql = "SELECT " + COLUMN_QUERY + ", COUNT(" + COLUMN_QUERY + ") as hit_count " +
                "FROM " + TABLE_SEARCH + " " +
                "GROUP BY " + COLUMN_QUERY + " " +
                "ORDER BY hit_count DESC LIMIT 5";

        Cursor cursor = database.rawQuery(rawSql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                trending.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUERY)));
            }
            cursor.close();
        }
        return trending;
    }



    public ArrayList<String> getMostSearchedProducts() { // most searched products
        ArrayList<String> topProducts = new ArrayList<>();

        // The SQL JOIN query
        String Sqlquery = "SELECT products.name, COUNT(search_logs.product_id) as click_count " +
                "FROM search_logs " +
                "INNER JOIN products ON search_logs.product_id = products.product_id " +
                "WHERE search_logs.product_id IS NOT NULL " + // Only count successful searches
                "GROUP BY search_logs.product_id " +
                "ORDER BY click_count DESC " +
                "LIMIT 5";

        Cursor cursor = database.rawQuery(Sqlquery, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                // We are extracting the 'name' column from the products table
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                topProducts.add(productName);
            }
            cursor.close();
        }

        return topProducts;
    }
}