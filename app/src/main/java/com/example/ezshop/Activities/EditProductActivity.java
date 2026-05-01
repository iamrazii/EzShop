package com.example.ezshop.Activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;

public class EditProductActivity extends AppCompatActivity {

    private DBManager dbManager;
    private EditText etName, etPrice, etDesc;
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

        productId = getIntent().getStringExtra("PRODUCT_ID");
        etName.setText(getIntent().getStringExtra("PRODUCT_NAME"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("PRODUCT_PRICE", 0.0)));
        etDesc.setText(getIntent().getStringExtra("PRODUCT_DESC"));

        findViewById(R.id.ivEditBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnFinalUpdate).setOnClickListener(v -> updateInDB());
    }

    private void updateInDB() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in the name and price", Toast.LENGTH_SHORT).show();
            return;
        }

        Product p = new Product();
        p.setProductId(this.productId);
        p.setName(name);
        p.setPrice(Double.parseDouble(priceStr));
        p.setDescription(desc);

        dbManager.productDB.updateProduct(p)
                .addOnSuccessListener(this, aVoid -> {
                    Toast.makeText(this, "Product Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(this, e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}