package com.example.ezshop.database;

import com.example.ezshop.models.User;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

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

    public long addUser(User user) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, user.getName());
        cv.put(COLUMN_EMAIL, user.getEmail());
        cv.put(COLUMN_PASSWORD, user.getPasswordHash());
        cv.put(COLUMN_WALLET, user.getWalletBalance());
        cv.put(COLUMN_ADDRESS, user.getDefaultShippingAddress());

        return database.insert(TABLE_USERS, null, cv);
    }

    public User getUserById(int userId) {
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

    public int deleteUser(int userId) {
        // Returns the number of rows deleted
        return database.delete(TABLE_USERS, COLUMN_ID + "=?", new String[]{String.valueOf(userId)});
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        // using .getColumnIndexOrThrow() to get error message
        user.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
        user.setWalletBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WALLET)));
        user.setDefaultShippingAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
        return user;
    }
}