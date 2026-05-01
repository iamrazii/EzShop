package com.example.ezshop.database;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class CategoryDB {
    private FirebaseFirestore cloudDb;
    public CategoryDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> addCategory(Category category) {
        if (category.getCategoryId() == null) category.setCategoryId(cloudDb.collection("categories").document().getId());
        return cloudDb.collection("categories").document(category.getCategoryId()).set(category);
    }

    public Task<QuerySnapshot> getAllCategories() {
        return cloudDb.collection("categories").orderBy("name").get();
    }
}
