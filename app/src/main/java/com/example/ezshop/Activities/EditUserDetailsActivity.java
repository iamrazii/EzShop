package com.example.ezshop.Activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class EditUserDetailsActivity extends AppCompatActivity {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private TextInputEditText etName, etEmail, etAddress;
    private MaterialButton btnSaveDetails;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        etName = findViewById(R.id.etEditName);
        etEmail = findViewById(R.id.etEditEmail);
        etAddress = findViewById(R.id.etEditAddress);
        btnSaveDetails = findViewById(R.id.btnSaveDetails);

        loadUserData();

        btnSaveDetails.setOnClickListener(v -> saveChanges());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() { showQuitWarningDialog(); }
        });

        findViewById(R.id.btnBackEdit).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void loadUserData() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        dbManager.userDB.getUserById(userId).addOnSuccessListener(this, documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    etName.setText(currentUser.getName());
                    etEmail.setText(currentUser.getEmail());
                    etAddress.setText(currentUser.getDefaultShippingAddress());
                }
            }
        });
    }

    private void saveChanges() {
        if (currentUser == null) return;

        String newName = etName.getText().toString().trim();
        String newAddress = etAddress.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(newName);
        currentUser.setDefaultShippingAddress(newAddress);

        dbManager.userDB.updateUser(currentUser)
                .addOnSuccessListener(this, aVoid -> {
                    Toast.makeText(this, "Details updated successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(this, e -> Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show());
    }

    private void showQuitWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Discard changes?")
                .setMessage("All entered information will be lost.")
                .setPositiveButton("Quit", (dialog, which) -> finish())
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}