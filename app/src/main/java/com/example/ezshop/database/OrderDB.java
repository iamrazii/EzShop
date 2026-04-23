package com.example.ezshop.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.UUID;
import com.example.ezshop.models.Order;
import com.example.ezshop.models.OrderItem;
import com.google.firebase.firestore.FirebaseFirestore;

public class OrderDB {
    private SQLiteDatabase database;
    private FirebaseFirestore cloudDb;

    private static final String TABLE_ORDERS = "orders";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_PROMO_ID = "promo_id";
    private static final String COLUMN_ADDRESS = "shipping_address";
    private static final String COLUMN_PAYMENT = "payment_method";
    private static final String COLUMN_TOTAL = "total_price";
    private static final String COLUMN_STATUS = "order_status";
    private static final String COLUMN_DATE = "created_at";

    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String COLUMN_ITEM_ID = "order_item_id";
    private static final String COLUMN_ITEM_ORDER_ID = "order_id";
    private static final String COLUMN_PRODUCT_ID = "product_id";
    private static final String COLUMN_PRICE_PURCHASE = "price_at_purchase";
    private static final String COLUMN_QTY = "quantity";

    public OrderDB(SQLiteDatabase database) {
        this.database = database;
        this.cloudDb = FirebaseFirestore.getInstance();
    }

    public boolean placeOrder(Order order, ArrayList<OrderItem> orderItems) {
        database.beginTransaction();
        try {
            ContentValues orderValues = new ContentValues();
            String newOrderId = UUID.randomUUID().toString();

            // Attach generated data to the object for Firebase later
            order.setOrderId(newOrderId);
            order.setOrderStatus("Processing");
            order.setCreatedAt(System.currentTimeMillis());

            orderValues.put(COLUMN_ORDER_ID, newOrderId);
            orderValues.put(COLUMN_USER_ID, order.getUserId());
            if (order.getPromoId() != null) {
                orderValues.put(COLUMN_PROMO_ID, order.getPromoId());
            } else {
                orderValues.putNull(COLUMN_PROMO_ID);
            }
            orderValues.put(COLUMN_ADDRESS, order.getShippingAddress());
            orderValues.put(COLUMN_PAYMENT, order.getPaymentMethod());
            orderValues.put(COLUMN_TOTAL, order.getTotalPrice());
            orderValues.put(COLUMN_STATUS, order.getOrderStatus());
            orderValues.put(COLUMN_DATE, order.getCreatedAt());

            if (database.insert(TABLE_ORDERS, null, orderValues) == -1) { return false; }

            for (OrderItem item : orderItems) {
                String newItemId = UUID.randomUUID().toString();
                item.setOrderItemId(newItemId);
                item.setOrderId(newOrderId); // Link it to master order

                ContentValues itemValues = new ContentValues();
                itemValues.put(COLUMN_ITEM_ID, newItemId);
                itemValues.put(COLUMN_ITEM_ORDER_ID, newOrderId);
                itemValues.put(COLUMN_PRODUCT_ID, item.getProductId());
                itemValues.put(COLUMN_PRICE_PURCHASE, item.getPriceAtPurchase());
                itemValues.put(COLUMN_QTY, item.getQuantity());
                database.insert(TABLE_ORDER_ITEMS, null, itemValues);
            }

            database.delete("cart_items", "user_id=?", new String[]{order.getUserId()});
            database.setTransactionSuccessful();

            // FIREBASE SYNC: Transaction was successful, push everything!
            cloudDb.collection("orders").document(newOrderId).set(order);
            for (OrderItem item : orderItems) {
                cloudDb.collection("order_items").document(item.getOrderItemId()).set(item);
            }

            // Also clear the user's cart in Firebase
            cloudDb.collection("cart_items").whereEqualTo("userId", order.getUserId()).get()
                    .addOnSuccessListener(snap -> {
                        for(com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                            doc.getReference().delete();
                        }
                    });

            return true;

        } finally {
            database.endTransaction();
        }
    }
}