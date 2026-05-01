package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.utilities.NetworkUtils;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private DBManager dbManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.tvSignUp).setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );

        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                attemptCloudLogin();
            } else {
                Toast.makeText(this, "Internet required to log in.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptCloudLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Authenticating with Cloud...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("passwordHash", password)
                .get()
                .addOnSuccessListener(this, snapshots -> {
                    if (!snapshots.isEmpty()) {
                        String userId = snapshots.getDocuments().get(0).getId();
                        checkStoreOwnershipAndLaunch(userId);
                    } else {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, e -> Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show());
    }

    private void checkStoreOwnershipAndLaunch(String userId) {
        FirebaseFirestore.getInstance().collection("stores")
                .whereEqualTo("owner_id", userId)
                .get()
                .addOnSuccessListener(this, storeSnap -> {
                    String role = "user";
                    if (!storeSnap.isEmpty()) {
                        role = "seller";
                        DocumentSnapshot storeDoc = storeSnap.getDocuments().get(0);
                        String storeId = storeDoc.contains("storeId") ? storeDoc.getString("storeId") :
                                storeDoc.contains("store_id") ? storeDoc.getString("store_id") : storeDoc.getId();
                        sessionManager.setStoreId(storeId);
                    }

                    sessionManager.createLoginSession(userId, role);
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, "seller".equals(role) ? SellerHomeActivity.class : UserHomeActivity.class));
                    finishAffinity();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}