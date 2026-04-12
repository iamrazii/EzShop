package com.example.ezshop.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBManager {

    public static final String DATABASE_NAME = "EzShopDB";
    public static final int DATABASE_VERSION = 1;

    private Context context;
    private DBHelper helper;
    private SQLiteDatabase database;


    public DBManager(Context context)
    {
        this.context = context;
    }

    public void open()
    {
        helper = new DBHelper(context);
    }

    public void close()
    {
        helper.close();
    }

    private class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            // code
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            // code
        }
    }
}
