package com.example.ezshop.database;
import com.example.ezshop.models.Store;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class StoreDB {
    private FirebaseFirestore cloudDb;

    public StoreDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> addStore(Store store) {
        if (store.getStoreId() == null) store.setStoreId(cloudDb.collection("stores").document().getId());
        return cloudDb.collection("stores").document(store.getStoreId()).set(store);
    }

    public Task<Void> updateStore(Store store) {
        return cloudDb.collection("stores").document(store.getStoreId()).set(store);
    }

    public Task<DocumentSnapshot> getStoreById(String storeId) {
        return cloudDb.collection("stores").document(storeId).get();
    }

    public Task<QuerySnapshot> getStoreIdByOwner(String userId) {
        return cloudDb.collection("stores").whereEqualTo("owner_id", userId).get();
    }
}