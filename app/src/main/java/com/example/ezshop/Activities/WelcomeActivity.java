package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezshop.R;
import com.example.ezshop.utilities.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        // PHASE 1: The Bootup Check
        // If a session exists, go straight to the app. No syncing here!
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