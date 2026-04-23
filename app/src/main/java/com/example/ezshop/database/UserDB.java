package com.example.ezshop.database;

import com.example.ezshop.models.User;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

import java.util.UUID; // Add this import at the top

public class UserDB {

    private SQLiteDatabase database; // db conn handed by db manager

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "user_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password_hash";
    private static final String COLUMN_WALLET = "wallet_balance";
    private static final String COLUMN_ADDRESS = "default_shipping_address";

    public UserDB(SQLiteDatabase database) {
        this.database = database;
    }

    // 1. Change the return type from 'long' to 'String'
    public String addUser(User user) {
        ContentValues cv = new ContentValues();

        // 2. Generate the unique String ID BEFORE inserting
        String newUserId = UUID.randomUUID().toString();
        cv.put(COLUMN_ID, newUserId); // Make sure you put the ID in the database!

        cv.put(COLUMN_NAME, user.getName());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPasswordHash());
        cv.put(COLUMN_WALLET, user.getWalletBalance());
        cv.put(COLUMN_ADDRESS, user.getDefaultShippingAddress());

        // 3. database.insert() still returns a long, but we just use it to check for errors
        long rowInserted = database.insert(TABLE_USERS, null, cv);

        // 4. If rowInserted is NOT -1, it means the save was successful
        if (rowInserted != -1) {
            user.setUserId(newUserId); // Attach it to the Java object

            // If you are syncing users to Firebase immediately, do it here:
            // cloudDb.collection("users").document(newUserId).set(user);

            return newUserId; // Return your new String ID!
        } else {
            return null; // Return null if SQLite failed to save it
        }
    }
    public User getUserById(String userId) {
        User user = null;
        Cursor cursor = database.query(
                TABLE_USERS,
                null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
            cursor.close(); // Always close cursors to prevent memory leaks!
        }
        return user;
    }

    public ArrayList<User> getAllUsers() {
        ArrayList<User> userList = new ArrayList<>();
        Cursor cursor = database.query(TABLE_USERS, null, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                User user = cursorToUser(cursor);
                userList.add(user);
            }
            cursor.close();
        }
        return userList;
    }

    public int updateUser(User user) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, user.getName());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPasswordHash());
        cv.put(COLUMN_WALLET, user.getWalletBalance());
        cv.put(COLUMN_ADDRESS, user.getDefaultShippingAddress());

        // Returns the number of rows affected (should be 1)
        return database.update(TABLE_USERS, cv, COLUMN_ID + "=?", new String[]{String.valueOf(user.getUserId())});
    }

    public int deleteUser(String userId) {
        // Returns the number of rows deleted
        return database.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
    }

    // --- DUMMY FUNCTION FOR TESTING ---
    public String getDummyUserId() {
        android.database.Cursor cursor = database.rawQuery("SELECT user_id FROM users LIMIT 1", null);
        String dummyId = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                dummyId = cursor.getString(0);
            }
            cursor.close();
        }
        return dummyId;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        // using .getColumnIndexOrThrow() to get error message
        user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
        user.setWalletBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WALLET)));
        user.setDefaultShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
        return user;
    }
}