package com.example.ezshop.database;
import com.example.ezshop.models.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;

public class PromoDB {
    private FirebaseFirestore cloudDb;
    public PromoDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<QuerySnapshot> getPromoByCode(String code) {
        return cloudDb.collection("promos").whereEqualTo("code", code.toUpperCase()).get();
    }
}