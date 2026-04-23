package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.UUID;
import com.example.ezshop.models.Product;
import com.google.firebase.firestore.FirebaseFirestore; // Add this import

public class ProductDB {
    private FirebaseFirestore cloudDb;
    private SQLiteDatabase database;
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
        this.cloudDb = FirebaseFirestore.getInstance();
    }

    public String addProduct(Product product) {
        ContentValues cv = new ContentValues();
        String newId = UUID.randomUUID().toString();
        cv.put(COLUMN_ID, newId);
        cv.put(COLUMN_STORE_ID, product.getStoreId());
        cv.put(COLUMN_CATEGORY_ID, product.getCategoryId());
        cv.put(COLUMN_NAME, product.getName());
        cv.put(COLUMN_DESC, product.getDescription());
        cv.put(COLUMN_IMAGE, product.getProductimage());
        cv.put(COLUMN_PRICE, product.getPrice());
        cv.put(COLUMN_CONDITION, product.getCondition());
        cv.put(COLUMN_WEIGHT, product.getWeightGrams());
        cv.put(COLUMN_RATING, product.getRatingAverage());
        cv.put(COLUMN_SOLD, product.getSoldCount());

        if (database.insert(TABLE_PRODUCTS, null, cv) != -1) {
            product.setProductId(newId);
            cloudDb.collection("products").document(newId).set(product);
            return newId;
        }
        return null;
    }

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_PRODUCTS, null, null, null, null, null, COLUMN_SOLD + " DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) { list.add(cursorToProduct(cursor)); }
            cursor.close();
        }
        return list;
    }

    public ArrayList<Product> searchProducts(String query) {
        ArrayList<Product> list = new ArrayList<>();
        String sqlQuery = "SELECT * FROM products WHERE _name LIKE ?";
        Cursor cursor = database.rawQuery(sqlQuery, new String[]{"%" + query + "%"});
        if (cursor != null) {
            while (cursor.moveToNext()) { list.add(cursorToProduct(cursor)); }
            cursor.close();
        }
        return list;
    }

    public ArrayList<Product> getProductsByCategory(String categoryId) {
        ArrayList<Product> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_PRODUCTS, null, COLUMN_CATEGORY_ID + "=?", new String[]{categoryId}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) { list.add(cursorToProduct(cursor)); }
            cursor.close();
        }
        return list;
    }

    public ArrayList<Product> getTrendingProductsWithStoreInfo() {
        ArrayList<Product> list = new ArrayList<>();
        String rawSql = "SELECT p.*, s.location AS store_location FROM products p INNER JOIN stores s ON p.store_id = s.store_id ORDER BY p.sold_count DESC LIMIT 5";
        Cursor cursor = database.rawQuery(rawSql, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product p = cursorToProduct(cursor);
                list.add(p);
            }
            cursor.close();
        }
        return list;
    }

    public ArrayList<Product> getProductsForSeller(String currentUserId) {
        ArrayList<Product> myProducts = new ArrayList<>();
        String query = "SELECT p.* FROM products p INNER JOIN stores s ON p.store_id = s.store_id WHERE s.owner_id = ?";
        Cursor cursor = database.rawQuery(query, new String[]{currentUserId});
        if (cursor != null) {
            while (cursor.moveToNext()) { myProducts.add(cursorToProduct(cursor)); }
            cursor.close();
        }
        return myProducts;
    }

    public boolean updateProduct(Product product) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, product.getName());
        cv.put(COLUMN_PRICE, product.getPrice());
        cv.put(COLUMN_DESC, product.getDescription());
        boolean isUpdated = database.update(TABLE_PRODUCTS, cv, COLUMN_ID + "=?", new String[]{product.getProductId()}) > 0;

        if (isUpdated) {
            cloudDb.collection("products").document(product.getProductId()).set(product);
        }

        return isUpdated;
    }

    public boolean deleteProduct(String productId) {
        boolean isDeleted = database.delete("products", "product_id=?", new String[]{productId}) > 0;

        if (isDeleted) {
            // FIREBASE SYNC: Remove the document entirely from the cloud
            cloudDb.collection("products").document(productId).delete();
        }

        return isDeleted;
    }

    public ArrayList<Product> getTopSellingProducts(String storeId) {
        ArrayList<Product> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_PRODUCTS, null, COLUMN_STORE_ID + "=?", new String[]{storeId}, null, null, COLUMN_SOLD + " DESC", "5");
        if (cursor != null) {
            while (cursor.moveToNext()) { list.add(cursorToProduct(cursor)); }
            cursor.close();
        }
        return list;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product p = new Product();
        p.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        p.setStoreId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STORE_ID)));
        p.setCategoryId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)));
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