package com.example.ezshop.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private DBManager db;
    private ArrayList<Category> categoryList;
    private int selectedCategoryId = -1;
    private String selectedLocalImageUri = "";

    private CardView cvImagePicker;
    private ImageView ivSelectedImage;
    private LinearLayout llImagePlaceholder;
    private MaterialAutoCompleteTextView actvCategory, actvCondition;
    private TextInputEditText etProductName, etProductPrice, etProductWeight, etProductDesc;
    private MaterialButton btnPublishProduct;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    llImagePlaceholder.setVisibility(View.GONE);
                    ivSelectedImage.setImageURI(uri);
                    selectedLocalImageUri = uri.toString();
                } else {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        setupDropdowns();

        btnPublishProduct.setOnClickListener(v -> startPublishingProcess());

        cvImagePicker.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showQuitWarningDialog();
            }
        });

        findViewById(R.id.AddProdbtnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void startPublishingProcess() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String weightStr = etProductWeight.getText().toString().trim();
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        // 1. Validations
        if (selectedLocalImageUri.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.isEmpty() || priceStr.isEmpty() || weightStr.isEmpty() || desc.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Start Cloudinary Upload
        uploadToCloudinary(Uri.parse(selectedLocalImageUri));
    }

    private void uploadToCloudinary(Uri uri) {
        btnPublishProduct.setEnabled(false);
        btnPublishProduct.setText("Uploading...");
        Toast.makeText(this, "Uploading image to cloud...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(uri)
                .unsigned("ezshop_products") // 🔥 Ensure this matches your Cloudinary Preset
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        saveProductToSqlite(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            btnPublishProduct.setEnabled(true);
                            btnPublishProduct.setText("Publish Product");
                            Toast.makeText(AddProductActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void saveProductToSqlite(String remoteUrl) {
        String name = etProductName.getText().toString().trim();
        double price = Double.parseDouble(etProductPrice.getText().toString().trim());
        int weight = Integer.parseInt(etProductWeight.getText().toString().trim());
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        User u = new User();
        Store s = db.storeDB.getStoreById(1);
        int storeId = s.getStoreId();
//        SessionManager sm = new SessionManager(this);
//        int storeId = db.storeDB.getStoreIdByOwner(1);
//


        if (storeId == -1) {
            Toast.makeText(this, "Error: Store not found!", Toast.LENGTH_SHORT).show();
            btnPublishProduct.setEnabled(true);
            return;
        }

        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setPrice(price);
        newProduct.setWeightGrams(weight);
        newProduct.setDescription(desc);
        newProduct.setCondition(condition);
        newProduct.setProductimage(remoteUrl); // Saving the HTTPS URL
        newProduct.setCategoryId(selectedCategoryId);
        newProduct.setStoreId(storeId);
        newProduct.setSoldCount(0);
        newProduct.setRatingAverage(0.0f);

        long result = db.productDB.addProduct(newProduct);

        if (result != -1) {
            Toast.makeText(this, "Product Published Successfully!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            btnPublishProduct.setEnabled(true);
            btnPublishProduct.setText("Publish Product");
            Toast.makeText(this, "Database Insert Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDropdowns() {
        categoryList = db.categoryDB.getAllCategories();
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) categoryNames.add(c.getName());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);
        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = categoryList.get(position).getCategoryId();
        });

        String[] conditions = new String[]{"New", "Used - Like New", "Used - Fair"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, conditions);
        actvCondition.setAdapter(conditionAdapter);
    }

    private void showQuitWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Discard changes?")
                .setMessage("All entered information will be lost.")
                .setPositiveButton("Quit", (dialog, which) -> finish())
                .setNegativeButton("Stay", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void init() {
        db = new DBManager(this);
        try { db.open(); } catch (Exception e) { e.printStackTrace(); }

        // Cloudinary Init
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "da3jabd2w"); // 🔥 Verify your cloud name
            MediaManager.init(this, config);
        } catch (Exception ignored) {}

        cvImagePicker = findViewById(R.id.AddProdcvImagePicker);
        ivSelectedImage = findViewById(R.id.AddProdivSelectedImage);
        llImagePlaceholder = findViewById(R.id.AddProdllImagePlaceholder);
        actvCategory = findViewById(R.id.AddProdactvCategory);
        actvCondition = findViewById(R.id.AddProdactvCondition);
        etProductName = findViewById(R.id.AddProdetProductName);
        etProductPrice = findViewById(R.id.AddProdetProductPrice);
        etProductWeight = findViewById(R.id.AddProdetProductWeight);
        etProductDesc = findViewById(R.id.AddProdetProductDesc);
        btnPublishProduct = findViewById(R.id.AddProdbtnPublishProduct);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}