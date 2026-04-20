package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import java.util.ArrayList;

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
        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<User> users = dbManager.userDB.getAllUsers();
        User found = null;
        for (User u : users) {
            if (u.getEmail().equalsIgnoreCase(email) && u.getPasswordHash().equals(password)) {
                found = u;
                break;
            }
        }

        if (found != null) {
            // Auto-detect role based on store ownership!
            int storeId = dbManager.storeDB.getStoreIdByOwner(found.getUserId());
            if (storeId != -1) {
                sessionManager.createLoginSession(found.getUserId(), "seller");
                sessionManager.setStoreId(storeId);
                startActivity(new Intent(this, SellerHomeActivity.class));
            } else {
                sessionManager.createLoginSession(found.getUserId(), "user");
                startActivity(new Intent(this, UserHomeActivity.class));
            }
            finishAffinity();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}