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

    private static final String COLUMN_QUERY = "search_query";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    public SearchlogDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addSearch(String query) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_QUERY, query);
        cv.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        return database.insert(TABLE_SEARCH, null, cv);
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