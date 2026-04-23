package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.google.firebase.firestore.FirebaseFirestore;

public class SearchlogDB {
    private SQLiteDatabase database;
    private FirebaseFirestore cloudDb;

    private static final String TABLE_SEARCH = "search_logs";
    private static final String COLUMN_QUERY = "search_query";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public SearchlogDB(SQLiteDatabase database) {
        this.database = database;
        this.cloudDb = FirebaseFirestore.getInstance();
    }

    public String addSearch(String query) {
        ContentValues cv = new ContentValues();
        String newId = UUID.randomUUID().toString();
        long time = System.currentTimeMillis();

        cv.put("log_id", newId);
        cv.put(COLUMN_QUERY, query);
        cv.put(COLUMN_TIMESTAMP, time);

        if(database.insert(TABLE_SEARCH, null, cv) != -1) {
            // FIREBASE SYNC: Using a Map since Searchlog is minimal here
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("logId", newId);
            logMap.put("searchQuery", query);
            logMap.put("timestamp", time);

            cloudDb.collection("search_logs").document(newId).set(logMap);
            return newId;
        }
        return null;
    }

    public ArrayList<String> getTrendingSearches() {
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
}