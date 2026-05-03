package com.example.ezshop.Activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProductActivity extends AppCompatActivity {

    private DBManager dbManager;
    private EditText etName, etPrice, etDesc;
    private Button btnFinalUpdate;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        dbManager = new DBManager(this);
        dbManager.open();

        etName = findViewById(R.id.etUpdateName);
        etPrice = findViewById(R.id.etUpdatePrice);
        etDesc = findViewById(R.id.etUpdateDesc);
        btnFinalUpdate = findViewById(R.id.btnFinalUpdate);

        productId = getIntent().getStringExtra("PRODUCT_ID");
        etName.setText(getIntent().getStringExtra("PRODUCT_NAME"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("PRODUCT_PRICE", 0.0)));
        etDesc.setText(getIntent().getStringExtra("PRODUCT_DESC"));

        findViewById(R.id.ivEditBack).setOnClickListener(v -> finish());
        btnFinalUpdate.setOnClickListener(v -> updateInDB());
    }

    private void updateInDB() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in the name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent accidental double-clicks while loading
        btnFinalUpdate.setEnabled(false);
        btnFinalUpdate.setText("Updating...");

        FirebaseFirestore.getInstance().collection("products").document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        Product existingProduct = documentSnapshot.toObject(Product.class);

                        if (existingProduct != null) {
                            // Update ONLY the fields the user changed
                            existingProduct.setName(name);
                            existingProduct.setPrice(Double.parseDouble(priceStr));
                            existingProduct.setDescription(desc);

                            // Now save the fully populated object back to the database
                            dbManager.productDB.updateProduct(existingProduct)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Product Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnFinalUpdate.setEnabled(true);
                                        btnFinalUpdate.setText("Update Product");
                                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Product not found in database", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    btnFinalUpdate.setEnabled(true);
                    btnFinalUpdate.setText("Update Product");
                    Toast.makeText(this, "Failed to connect to database", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}