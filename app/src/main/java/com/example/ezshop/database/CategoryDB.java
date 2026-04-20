package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import com.example.ezshop.models.Category;

public class CategoryDB {
    private SQLiteDatabase database;

    private static final String TABLE_CATEGORIES = "categories";
    private static final String COLUMN_ID = "category_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ICON = "icon_name";

    public CategoryDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, category.getName());
        cv.put(COLUMN_ICON, category.getIconName());
        return database.insert(TABLE_CATEGORIES, null, cv);
    }

    public ArrayList<Category> getAllCategories() {
        ArrayList<Category> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_CATEGORIES, null, null, null, null, null, COLUMN_NAME + " ASC"); // Alphabetical order
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Category c = new Category();
                c.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                c.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                c.setIconName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON)));
                list.add(c);
            }
            cursor.close();
        }
        return list;
    }

    public void seedCategories() {
        try {
            android.database.Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM categories", null);
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();

            if (count < 16) {
                database.delete("categories", null, null);

                String[] initialCategories = {
                        "Electronics",
                        "Laptops & PCs",
                        "Mobile Phones",
                        "Fashion & Apparel",
                        "Footwear",
                        "Home & Kitchen",
                        "Beauty & Personal Care",
                        "Health & Fitness",
                        "Sports & Outdoors",
                        "Toys & Games",
                        "Automotive Parts",
                        "Books & Stationery",
                        "Groceries",
                        "Pet Supplies",
                        "Office Supplies",
                        "Handmade Crafts"
                };

                for (String catName : initialCategories) {
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put("name", catName);
                    database.insert("categories", null, values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}