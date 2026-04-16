package com.example.ezshop.database;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

import com.example.ezshop.models.Order;
import com.example.ezshop.models.OrderItem;

public class OrderDB {
    private SQLiteDatabase database;

    // Table 1: The Master Order
    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PROMO_ID = "promo_id";
    private static final String COLUMN_ADDRESS = "shipping_address";
    private static final String COLUMN_PAYMENT = "payment_method";
    private static final String COLUMN_TOTAL = "total_price";
    private static final String COLUMN_STATUS = "order_status";
    private static final String COLUMN_DATE = "created_at";

    // Table 2: The Individual Items
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String COLUMN_ITEM_ID = "order_item_id";
    private static final String COLUMN_ITEM_ORDER_ID = "order_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_PRICE_PURCHASE = "price_at_purchase";
    private static final String COLUMN_QTY = "quantity";

    public OrderDB(SQLiteDatabase database) {
        this.database = database;
    }

    public boolean placeOrder(Order order, ArrayList<OrderItem> orderItems) {
        database.beginTransaction();
        try {
            // Step 1: Create the Master Order
            ContentValues orderValues = new ContentValues();
            orderValues.put(COLUMN_USER_ID, order.getUserId());

            // Handle the nullable Promo ID safely
            if (order.getPromoId() != null) {
                orderValues.put(COLUMN_PROMO_ID, order.getPromoId());
            } else {
                orderValues.putNull(COLUMN_PROMO_ID);
            }

            orderValues.put(COLUMN_ADDRESS, order.getShippingAddress());
            orderValues.put(COLUMN_PAYMENT, order.getPaymentMethod());
            orderValues.put(COLUMN_TOTAL, order.getTotalPrice());
            orderValues.put(COLUMN_STATUS, "Processing"); // Default status for new orders
            orderValues.put(COLUMN_DATE, System.currentTimeMillis());

            long newOrderId = database.insert(TABLE_ORDERS, null, orderValues);

            if (newOrderId == -1) {
                return false;
            }

            // Step 2: Loop through the items and freeze their purchase price
            for (OrderItem item : orderItems) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(COLUMN_ITEM_ORDER_ID, newOrderId); // Link it to the receipt!
                itemValues.put(COLUMN_PRODUCT_ID, item.getProductId());
                itemValues.put(COLUMN_PRICE_PURCHASE, item.getPriceAtPurchase());
                itemValues.put(COLUMN_QTY, item.getQuantity());

                database.insert(TABLE_ORDER_ITEMS, null, itemValues);
            }

            // Step 3: Empty the user's shopping cart
            database.delete("cart_items", "user_id=?", new String[]{String.valueOf(order.getUserId())});

            database.setTransactionSuccessful();
            return true;

        } finally {
            database.endTransaction();
        }
    }
}