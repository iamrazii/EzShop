package com.example.ezshop.Activities;

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

import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class AddProductActivity extends AppCompatActivity {

    private DBManager db;
    private ArrayList<Category> categoryList;
    private int selectedCategoryId = -1;
    private String selectedImageString = "";

    private CardView cvImagePicker;
    private ImageView ivSelectedImage;
    private LinearLayout llImagePlaceholder;
    private MaterialAutoCompleteTextView actvCategory, actvCondition;
    private TextInputEditText etProductName, etProductPrice, etProductWeight, etProductDesc;
    private MaterialButton btnPublishProduct;


    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    llImagePlaceholder.setVisibility(View.GONE);
                    ivSelectedImage.setImageURI(uri);
                    selectedImageString = uri.toString();
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

        setupDropdowns(); // creating dropdowns for category and condition
        btnPublishProduct.setOnClickListener(v -> publishProduct());

        cvImagePicker.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });


        //  System Back Swipe / Back Button
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showQuitWarningDialog(); // show warning
            }
        });

        ImageView btnBack = findViewById(R.id.AddProdbtnBack);
        btnBack.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
    }



    private void showQuitWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Discard changes?")
                .setMessage("If you go back now, all the product information you entered will be lost.")
                .setPositiveButton("Quit", (dialog, which) -> {
                    // User clicked Quit. Close the activity!
                    finish();
                })
                .setNegativeButton("Stay", (dialog, which) -> {
                    // User clicked Stay. Just dismiss the dialog and do nothing.
                    dialog.dismiss();
                })
                .show();
    }


    private void publishProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String weightStr = etProductWeight.getText().toString().trim();
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        // Validations
        if (selectedImageString.isEmpty()) {
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

        double price = Double.parseDouble(priceStr);
        int weight = Integer.parseInt(weightStr); // Parse the new weight field

        SessionManager sm = new SessionManager(this);
        int currentUserId = sm.getUserId();
        int storeId = db.storeDB.getStoreIdByOwner(currentUserId);

        if (storeId == -1) {
            Toast.makeText(this, "Error: Store not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // SAVE TO DB
        Product newProduct = new Product();
        newProduct.setName(name); newProduct.setPrice(price);
        newProduct.setWeightGrams(weight); newProduct.setDescription(desc);
        newProduct.setCondition(condition); newProduct.setProductimage(selectedImageString);
        newProduct.setCategoryId(selectedCategoryId); newProduct.setStoreId(storeId);
        newProduct.setSoldCount(0); newProduct.setRatingAverage(0.0f);

        db.productDB.addProduct(newProduct);
        Toast.makeText(this, "Product Published Successfully! ", Toast.LENGTH_LONG).show();
        finish();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
    private void setupDropdowns() {
        categoryList = db.categoryDB.getAllCategories();
        ArrayList<String> categoryNames = new ArrayList<>();
        for (Category c : categoryList) {
            categoryNames.add(c.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);

        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = categoryList.get(position).getCategoryId();
        });

        String[] conditions = new String[]{"New", "Used - Like New", "Used - Fair"};
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, conditions);
        actvCondition.setAdapter(conditionAdapter);
    }
    void init(){

        db = new DBManager(this);
        try { db.open(); } catch (Exception e) { e.printStackTrace(); }

        // Bind UI
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
}