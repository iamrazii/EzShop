package com.example.ezshop.database;
import com.example.ezshop.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserDB {
    private FirebaseFirestore cloudDb;

    public UserDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> addUser(User user) {
        if (user.getUserId() == null) user.setUserId(cloudDb.collection("users").document().getId());
        return cloudDb.collection("users").document(user.getUserId()).set(user);
    }

    public Task<Void> updateUser(User user) {
        return cloudDb.collection("users").document(user.getUserId()).set(user);
    }

    public Task<Void> deleteUser(String userId) {
        return cloudDb.collection("users").document(userId).delete();
    }

    public Task<DocumentSnapshot> getUserById(String userId) {
        return cloudDb.collection("users").document(userId).get();
    }

    public Task<QuerySnapshot> getAllUsers() {
        return cloudDb.collection("users").get();
    }
}