package com.example.ezshop.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

import com.example.ezshop.models.Product;

public class ProductDB {

    // The open connection handed to us by DBManager
    private SQLiteDatabase database;

    // Database table and column names
    private static final String TABLE_PRODUCTS = "products";
    private static final String COLUMN_ID = "product_id";
    private static final String COLUMN_STORE_ID = "store_id";
    private static final String COLUMN_CATEGORY_ID = "category_id";
    private static final String COLUMN_NAME = "_name";
    private static final String COLUMN_DESC = "_description";
    private static final String COLUMN_PRICE = "_price";
    private static final String COLUMN_IMAGE = "_image";

    private static final String COLUMN_CONDITION = "_condition";
    private static final String COLUMN_WEIGHT = "weight_grams";
    private static final String COLUMN_RATING = "rating_average";
    private static final String COLUMN_SOLD = "sold_count";

    public ProductDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addProduct(Product product) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STORE_ID, product.getStoreId());
        cv.put(COLUMN_CATEGORY_ID, product.getCategoryId());
        cv.put(COLUMN_NAME, product.getName());
        cv.put(COLUMN_DESC, product.getDescription());
        cv.put(COLUMN_IMAGE,product.getProductimage());
        cv.put(COLUMN_PRICE, product.getPrice());
        cv.put(COLUMN_CONDITION, product.getCondition());
        cv.put(COLUMN_WEIGHT, product.getWeightGrams());
        cv.put(COLUMN_RATING, product.getRatingAverage());
        cv.put(COLUMN_SOLD, product.getSoldCount());

        return database.insert(TABLE_PRODUCTS, null, cv);
    }

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        // Orders by best seller by default
        Cursor cursor = database.query(TABLE_PRODUCTS, null, null, null, null, null, COLUMN_SOLD + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToProduct(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public ArrayList<Product> getProductsByCategory(int categoryId) {
        ArrayList<Product> list = new ArrayList<>();
        Cursor cursor = database.query(
                TABLE_PRODUCTS,
                null,
                COLUMN_CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)},
                null, null, null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToProduct(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public ArrayList<Product> getTrendingProductsWithStoreInfo() {
        ArrayList<Product> list = new ArrayList<>();

        // The JOIN Query: Gets product info PLUS the location from the stores table
        String rawSql = "SELECT p.*, s.location AS store_location " +
                "FROM products p " +
                "INNER JOIN stores s ON p.store_id = s.store_id " +
                "ORDER BY p.sold_count DESC LIMIT 10";

        Cursor cursor = database.rawQuery(rawSql, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product p = cursorToProduct(cursor);

                String joinedLocation = cursor.getString(cursor.getColumnIndexOrThrow("store_location"));
                list.add(p);
            }
            cursor.close();
        }
        return list;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product p = new Product();
        p.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        p.setStoreId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STORE_ID)));
        p.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)));
        p.setProductimage(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)));
        p.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        p.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC)));
        p.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)));
        p.setCondition(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONDITION)));
        p.setWeightGrams(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WEIGHT)));
        p.setRatingAverage(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)));
        p.setSoldCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOLD)));
        return p;
    }
}