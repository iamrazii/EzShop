package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import com.example.ezshop.models.Store;

public class StoreDB {
    private SQLiteDatabase database;

    private static final String TABLE_STORES = "stores";
    private static final String COLUMN_ID = "store_id";
    private static final String COLUMN_NAME = "store_name";
    private static final String COLUMN_OWNERID = "owner_id";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_RATING = "rating";

    public StoreDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addStore(Store store) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, store.getStoreName());
        cv.put(COLUMN_OWNERID , store.getOwner_id());
        cv.put(COLUMN_LOCATION, store.getLocation());
        cv.put(COLUMN_STATUS, store.getStatus());
        cv.put(COLUMN_RATING, store.getRating());
        return database.insert(TABLE_STORES, null, cv);
    }

    public Store getStoreById(int storeId) {
        Store store = null;
        Cursor cursor = database.query(TABLE_STORES, null, COLUMN_ID + "=?", new String[]{String.valueOf(storeId)}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                store = new Store();
                store.setStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                store.setStoreName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                store.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                store.setOwner_id(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNERID)));
                store.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                store.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)));
            }
            cursor.close();
        }
        return store;
    }


    public int getStoreIdByOwner(int userId) {
        int storeId = -1;
        Cursor cursor = database.query("stores", new String[]{"store_id"},
                "owner_id = ?", new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            storeId = cursor.getInt(cursor.getColumnIndexOrThrow("store_id"));
            cursor.close();
        }
        return storeId;
    }

    public boolean updateStore(Store store) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, store.getStoreName());
        cv.put(COLUMN_LOCATION, store.getLocation());
        cv.put(COLUMN_STATUS, store.getStatus());

        int rowsAffected = database.update(TABLE_STORES, cv, COLUMN_ID + "=?", new String[]{String.valueOf(store.getStoreId())});
        return rowsAffected > 0;
    }

}