
// TO-DO: REVIEWING AND FIXING



package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import com.example.ezshop.models.CartItem;

public class CartItemDB {
    private SQLiteDatabase database;

    private static final String TABLE_CART = "cart_items";
    private static final String COLUMN_ID = "cart_item_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_QTY = "quantity";

    public CartItemDB(SQLiteDatabase database) {
        this.database = database;
    }

    // SPECIALIZED: "Smart Add" - Prevents duplicate rows in the cart
    public void addToCart(int userId, int productId, int variantId, int quantityToAdd) {
        // 1. Check if this exact item is already in the user's cart
        Cursor cursor = database.query(TABLE_CART, new String[]{COLUMN_ID, COLUMN_QTY},
                COLUMN_USER_ID + "=? AND " + COLUMN_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId), String.valueOf(variantId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Item exists! Just update the quantity.
            int existingId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QTY));

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_QTY, currentQty + quantityToAdd);
            database.update(TABLE_CART, cv, COLUMN_ID + "=?", new String[]{String.valueOf(existingId)});
            cursor.close();
        } else {
            // Item does not exist. Insert a brand new row.
            if (cursor != null) cursor.close();

            ContentValues cv = new ContentValues();
            cv.put(COLUMN_USER_ID, userId);
            cv.put(COLUMN_PRODUCT_ID, productId);
            cv.put(COLUMN_QTY, quantityToAdd);
            database.insert(TABLE_CART, null, cv);
        }
    }

    // SPECIALIZED: Load the "My Cart" screen for a specific user
    public ArrayList<CartItem> getCartForUser(int userId) {
        ArrayList<CartItem> list = new ArrayList<>();
        Cursor cursor = database.query(TABLE_CART, null, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                CartItem item = new CartItem();
                item.setCartItemId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                item.setProductId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PRODUCT_ID)));
                item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QTY)));
                list.add(item);
            }
            cursor.close();
        }
        return list;
    }

    // Update quantity when the user clicks "+" or "-" on the Cart Screen
    public void updateQuantity(int cartItemId, int newQuantity) {
        if (newQuantity <= 0) {
            deleteCartItem(cartItemId); // If they lower qty to 0, just delete it
        } else {
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_QTY, newQuantity);
            database.update(TABLE_CART, cv, COLUMN_ID + "=?", new String[]{String.valueOf(cartItemId)});
        }
    }

    // When the user clicks the little Trash Can icon
    public void deleteCartItem(int cartItemId) {
        database.delete(TABLE_CART, COLUMN_ID + "=?", new String[]{String.valueOf(cartItemId)});
    }

    // SPECIALIZED: Call this immediately after a successful checkout!
    public void clearCartForUser(int userId) {
        database.delete(TABLE_CART, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }
}