package com.example.ezshop.database;
import com.example.ezshop.models.Order;
import com.example.ezshop.models.OrderItem;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;

public class OrderDB {
    private FirebaseFirestore cloudDb;

    public OrderDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> placeOrder(Order order, ArrayList<OrderItem> orderItems) {
        WriteBatch batch = cloudDb.batch();

        String orderId = cloudDb.collection("orders").document().getId();
        order.setOrderId(orderId);
        order.setOrderStatus("Processing");
        order.setCreatedAt(System.currentTimeMillis());

        batch.set(cloudDb.collection("orders").document(orderId), order);

        for (OrderItem item : orderItems) {
            String itemId = cloudDb.collection("order_items").document().getId();
            item.setOrderItemId(itemId);
            item.setOrderId(orderId);
            batch.set(cloudDb.collection("order_items").document(itemId), item);
        }

        return batch.commit();
    }

    public Task<QuerySnapshot> getOrdersForUser(String userId) {
        return cloudDb.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getOrderItems(String orderId) {
        return cloudDb.collection("order_items").whereEqualTo("orderId", orderId).get();
    }
}