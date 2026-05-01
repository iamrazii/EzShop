package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezshop.R;
import com.example.ezshop.utilities.SessionManager;
import android.util.Log;
import com.example.ezshop.models.Category;
import com.example.ezshop.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        seedCategoriesIfNeeded();
        if (sessionManager.isLoggedIn()) {
            launchRoleDashboard(sessionManager.getUserRole());
            return;
        }

        // Otherwise, show the Welcome UI
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnGetStarted).setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }



    private void seedCategoriesIfNeeded() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String [] list = {"electronics_icon" , "mobile_icon", "laptop_icon", "headphones_icon", "mic_icon", "console_icon"};
        db.collection("categories").limit(1).get().addOnSuccessListener(snapshots -> {

            if (snapshots.isEmpty()) {
                Log.d("FirebaseSeed", "No categories found. Uploading from Constants...");
                int i = 0;
                for (String catName : Constants.CATEGORIES) {

                    String newId = db.collection("categories").document().getId();

                    Category category = new Category();
                    category.setCategoryId(newId);
                    category.setName(catName);
                    category.setIconName(list[i++]);

                    db.collection("categories").document(newId).set(category)
                            .addOnSuccessListener(aVoid -> Log.d("FirebaseSeed", "Uploaded: " + catName))
                            .addOnFailureListener(e -> Log.e("FirebaseSeed", "Failed to upload: " + catName, e));
                }
            } else {
                Log.d("FirebaseSeed", "Categories already exist. Skipping upload.");
            }
        }).addOnFailureListener(e -> {
            Log.e("FirebaseSeed", "Failed to check categories.", e);
        });
    }
    private void launchRoleDashboard(String role) {
        if (role == null || role.equalsIgnoreCase("Guest")) {
            sessionManager.logoutUser();
            return;
        }

        Intent intent;
        if ("seller".equalsIgnoreCase(role.trim())) {
            intent = new Intent(this, SellerHomeActivity.class);
        } else {
            intent = new Intent(this, UserHomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}