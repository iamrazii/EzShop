package com.example.ezshop.database;
import com.example.ezshop.models.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class SearchlogDB {
    private FirebaseFirestore cloudDb;

    public SearchlogDB(FirebaseFirestore cloudDb) {
        this.cloudDb = cloudDb;
    }

    public Task<Void> addSearch(String query) {
        String logId = cloudDb.collection("search_logs").document().getId();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("logId", logId);
        logMap.put("searchQuery", query);
        logMap.put("timestamp", System.currentTimeMillis());
        return cloudDb.collection("search_logs").document(logId).set(logMap);
    }

    public Task<QuerySnapshot> getAllLogs() {

        return cloudDb.collection("search_logs")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get();
    }
}