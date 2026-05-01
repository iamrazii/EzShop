package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;

import java.util.UUID;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etAddress, etStoreName, etStoreLocation;
    private RadioButton rbUser, rbSeller;
    private LinearLayout llStoreFields, llUserFields;
    private DBManager dbManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAddress = findViewById(R.id.etAddress);
        etStoreName = findViewById(R.id.etStoreName);
        etStoreLocation = findViewById(R.id.etStoreLocation);
        rbUser = findViewById(R.id.rbUser);
        rbSeller = findViewById(R.id.rbSeller);

        llStoreFields = findViewById(R.id.llStoreFields);
        llUserFields = findViewById(R.id.llUserFields);

        RadioGroup rgUserType = findViewById(R.id.rgUserType);
        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isSeller = checkedId == R.id.rbSeller;
            llStoreFields.setVisibility(isSeller ? View.VISIBLE : View.GONE);
            llUserFields.setVisibility(isSeller ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btnSignUp).setOnClickListener(v -> {
            attemptSignup();
        });
    }

    private void attemptSignup() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a definitive ID for the new user
        String userId = UUID.randomUUID().toString();

        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setWalletBalance(58.309); // Starting promo balance

        if (rbSeller.isChecked()) {
            String storeName = etStoreName.getText().toString().trim();
            String storeLocation = etStoreLocation.getText().toString().trim();

            if (storeName.isEmpty()) {
                Toast.makeText(this, "Please enter your store name", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setDefaultShippingAddress("N/A");

            // Save User to Firebase, THEN Save Store to Firebase
            dbManager.userDB.addUser(user).addOnSuccessListener(this, aVoid -> {

                String storeId = UUID.randomUUID().toString();
                Store store = new Store();
                store.setStoreId(storeId);
                store.setStoreName(storeName);
                store.setLocation(storeLocation.isEmpty() ? "Unknown" : storeLocation);
                store.setStatus("Active");
                store.setRating(0.0);
                store.setOwner_id(userId);

                dbManager.storeDB.addStore(store).addOnSuccessListener(this, aVoid2 -> {
                    sessionManager.createLoginSession(userId, "seller");
                    sessionManager.setStoreId(storeId);

                    Toast.makeText(this, "Store created! Welcome aboard.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, SellerHomeActivity.class));
                    finishAffinity();
                }).addOnFailureListener(this, e -> Toast.makeText(this, "Failed to create store data.", Toast.LENGTH_SHORT).show());

            }).addOnFailureListener(this, e -> Toast.makeText(this, "Registration failed. Try a different email.", Toast.LENGTH_SHORT).show());

        } else {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Please enter your shipping address", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setDefaultShippingAddress(address);

            dbManager.userDB.addUser(user).addOnSuccessListener(this, aVoid -> {
                sessionManager.createLoginSession(userId, "user");
                Toast.makeText(this, "Account created! Happy shopping.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, UserHomeActivity.class));
                finishAffinity();
            }).addOnFailureListener(this, e -> Toast.makeText(this, "Registration failed. Try a different email.", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}