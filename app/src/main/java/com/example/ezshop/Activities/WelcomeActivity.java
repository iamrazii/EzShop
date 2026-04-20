package com.example.ezshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
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

        // 1. If they are already logged in, send them straight to their Home screen
        if (sessionManager.isLoggedIn()) {
            redirectLoggedInUser();
            return;
        }

        // 2. Check if this is the very first time opening the app
        SharedPreferences prefs = getSharedPreferences("EzShopPrefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTimeLaunch", true);

        if (!isFirstTime) {
            // Not their first time, and they aren't logged in. Send straight to Login.
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 3. If it IS their first time, flip the flag to false so they never see this again
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFirstTimeLaunch", false);
        editor.apply();

        // 4. Finally, show the Welcome layout since it passed all checks
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnGetStarted).setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
        findViewById(R.id.btnLogin).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class))
        );
    }

    private void redirectLoggedInUser() {
        String role = sessionManager.getUserRole();
        Intent intent;
        switch (role) {
            case "seller":
                intent = new Intent(this, SellerHomeActivity.class);
                break;
            default:
                intent = new Intent(this, UserHomeActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}