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
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private DBManager db;
    private SessionManager sessionManager;
    private ArrayList<Category> categoryList;
    private String selectedCategoryId = null;
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
            public void handleOnBackPressed() { showQuitWarningDialog(); }
        });

        findViewById(R.id.AddProdbtnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void startPublishingProcess() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String weightStr = etProductWeight.getText().toString().trim();
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        if (selectedLocalImageUri.isEmpty() || selectedCategoryId == null || name.isEmpty() || priceStr.isEmpty() || weightStr.isEmpty() || desc.isEmpty() || condition.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields and select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadToCloudinary(Uri.parse(selectedLocalImageUri));
    }

    private void uploadToCloudinary(Uri uri) {
        btnPublishProduct.setEnabled(false);
        btnPublishProduct.setText("Uploading...");

        MediaManager.get().upload(uri)
                .unsigned("ezshop_products")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        saveProductToFirebase(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        runOnUiThread(() -> {
                            btnPublishProduct.setEnabled(true);
                            btnPublishProduct.setText("Publish Product");
                            Toast.makeText(AddProductActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                        });
                    }
                }).dispatch();
    }

    private void saveProductToFirebase(String remoteUrl) {
        String name = etProductName.getText().toString().trim();
        double price = Double.parseDouble(etProductPrice.getText().toString().trim());
        int weight = Integer.parseInt(etProductWeight.getText().toString().trim());
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        String storeId = sessionManager.getStoreId();

        if (storeId == null) {
            Toast.makeText(this, "Error: Store ID missing!", Toast.LENGTH_SHORT).show();
            runOnUiThread(() -> {
                btnPublishProduct.setEnabled(true);
                btnPublishProduct.setText("Publish Product");
            });
            return;
        }

        Product newProduct = new Product();
        newProduct.setName(name);
        newProduct.setPrice(price);
        newProduct.setWeightGrams(weight);
        newProduct.setDescription(desc);
        newProduct.setCondition(condition);
        newProduct.setProductimage(remoteUrl);
        newProduct.setCategoryId(selectedCategoryId);
        newProduct.setStoreId(storeId);
        newProduct.setSoldCount(0);
        newProduct.setRatingAverage(0.0f);

        db.productDB.addProduct(newProduct).addOnSuccessListener(this, aVoid -> {
            Toast.makeText(this, "Product Published Successfully!", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(this, e -> {
            btnPublishProduct.setEnabled(true);
            btnPublishProduct.setText("Publish Product");
            Toast.makeText(this, "Database Insert Failed", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDropdowns() {
        db.categoryDB.getAllCategories().addOnSuccessListener(this, queryDocumentSnapshots -> {
            categoryList = new ArrayList<>();
            ArrayList<String> categoryNames = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Category c = doc.toObject(Category.class);
                if (c != null) {
                    categoryList.add(c);
                    categoryNames.add(c.getName());
                }
            }

            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
            actvCategory.setAdapter(categoryAdapter);
            actvCategory.setOnItemClickListener((parent, view, position, id) -> {
                selectedCategoryId = categoryList.get(position).getCategoryId();
            });
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
        db.open();
        sessionManager = new SessionManager(this);

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "da3jabd2w");
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