package com.example.ezshop.database;
import com.example.ezshop.models.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class CartItemDB {
    private FirebaseFirestore cloudDb;
    public CartItemDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> saveCartItem(CartItem item) {
        if (item.getCartItemId() == null) item.setCartItemId(cloudDb.collection("cart_items").document().getId());
        return cloudDb.collection("cart_items").document(item.getCartItemId()).set(item);
    }

    public Task<Void> updateQuantity(String cartItemId, int newQty) {
        if (newQty <= 0) return cloudDb.collection("cart_items").document(cartItemId).delete();
        return cloudDb.collection("cart_items").document(cartItemId).update("quantity", newQty);
    }

    public Task<Void> deleteCartItem(String cartItemId) {
        return cloudDb.collection("cart_items").document(cartItemId).delete();
    }

    public Task<QuerySnapshot> getCartForUser(String userId) {
        return cloudDb.collection("cart_items").whereEqualTo("userId", userId).get();
    }
}




// SEARCH LOGS
