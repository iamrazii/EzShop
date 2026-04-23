package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.UUID;
import com.example.ezshop.models.CartItem;

public class CartItemDB {
    private SQLiteDatabase database;
    private static final String TABLE_CART = "cart_items";
    private static final String COLUMN_ID = "cart_item_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_VARIANT_ID = "variant_id"; // Added to schema mentally
    private static final String COLUMN_QTY = "quantity";

    public CartItemDB(SQLiteDatabase database) { this.database = database; }

    public void addToCart(String userId, String productId, String variantId, int quantityToAdd) {
        Cursor cursor = database.query(TABLE_CART, new String[]{COLUMN_ID, COLUMN_QTY},
                COLUMN_USER_ID + "=? AND " + COLUMN_PRODUCT_ID + "=? AND " + COLUMN_VARIANT_ID + "=?",
                new String[]{userId, productId, variantId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String existingId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QTY));
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_QTY, currentQty + quantityToAdd);
            database.update(TABLE_CART, cv, COLUMN_ID + "=?", new String[]{existingId});
            cursor.close();
        } else {
            if (cursor != null) cursor.close();
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_ID, UUID.randomUUID().toString());
            cv.put(COLUMN_USER_ID, userId);
            cv.put(COLUMN_PRODUCT_ID, productId);
            cv.put(COLUMN_VARIANT_ID, variantId);
            cv.put(COLUMN_QTY, quantityToAdd);
            database.insert(TABLE_CART, null, cv);
        }
    }

    public ArrayList<CartItem> getCartForUser(String userId) {
        ArrayList<CartItem> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_CART, null, COLUMN_USER_ID + "=?", new String[]{userId}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                CartItem item = new CartItem();
                item.setCartItemId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                item.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QTY)));
                list.add(item);
            }
            cursor.close();
        }
        return list;
    }

    public void updateQuantity(String cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            deleteCartItem(cartItemId);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_QTY, newQuantity);
            database.update(TABLE_CART, cv, COLUMN_ID + "=?", new String[]{cartItemId});
        }
    }

    public void deleteCartItem(String cartItemId) {
        database.delete(TABLE_CART, COLUMN_ID + "=?", new String[]{cartItemId});
    }

    public void clearCartForUser(String userId) {
        database.delete(TABLE_CART, COLUMN_USER_ID + "=?", new String[]{userId});
    }
}