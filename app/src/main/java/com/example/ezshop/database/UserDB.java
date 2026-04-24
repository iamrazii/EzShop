package com.example.ezshop.database;

import com.example.ezshop.models.User;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.UUID;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDB {

    private SQLiteDatabase database;
    private FirebaseFirestore cloudDb;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "user_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password_hash";
    private static final String COLUMN_WALLET = "wallet_balance";
    private static final String COLUMN_ADDRESS = "default_shipping_address";

    public UserDB(SQLiteDatabase database) {
        this.database = database;
        this.cloudDb = FirebaseFirestore.getInstance();
    }

    public String addUser(User user) {
        ContentValues cv = new ContentValues();
        String newUserId = UUID.randomUUID().toString();
        cv.put(COLUMN_ID, newUserId);
        cv.put(COLUMN_NAME, user.getName());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPasswordHash());
        cv.put(COLUMN_WALLET, user.getWalletBalance());
        cv.put(COLUMN_ADDRESS, user.getDefaultShippingAddress());

        long rowInserted = database.insert(TABLE_USERS, null, cv);

        if (rowInserted != -1) {
            user.setUserId(newUserId);
            // FIREBASE SYNC
            cloudDb.collection("users").document(newUserId).set(user);
            return newUserId;
        }
        return null;
    }

    public int updateUser(User user) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, user.getName());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPasswordHash());
        cv.put(COLUMN_WALLET, user.getWalletBalance());
        cv.put(COLUMN_ADDRESS, user.getDefaultShippingAddress());

        int rows = database.update(TABLE_USERS, cv, COLUMN_ID + "=?", new String[]{user.getUserId()});

        // FIREBASE SYNC
        if (rows > 0) {
            cloudDb.collection("users").document(user.getUserId()).set(user);
        }
        return rows;
    }

    public int deleteUser(String userId) {
        int rows = database.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{userId});

        // FIREBASE SYNC
        if (rows > 0) {
            cloudDb.collection("users").document(userId).delete();
        }
        return rows;
    }

    public User getUserById(String userId) {
        User user = null;
        Cursor cursor = database.query(TABLE_USERS, null, COLUMN_ID + "=?", new String[]{userId}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) { user = cursorToUser(cursor); }
            cursor.close();
        }
        return user;
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> userList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_USERS, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) { userList.add(cursorToUser(cursor)); }
            cursor.close();
        }
        return userList;
    }

    public String getDummyUserId() {
        android.database.Cursor cursor = database.rawQuery("SELECT user_id FROM users LIMIT 1", null);
        String dummyId = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) { dummyId = cursor.getString(0); }
            cursor.close();
        }
        return dummyId;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
        user.setWalletBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WALLET)));
        user.setDefaultShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
        return user;
    }

}