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
import com.example.ezshop.BuildConfig;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.utilities.Constants;
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private DBManager db;
    private SessionManager sessionManager;

    private HashMap<String, String> categoryMap = new HashMap<>();
    // Dictionary for auto-tagging
    private HashMap<String, String> autoTagRules;

    private String selectedLocalImageUri = "";

    private CardView cvImagePicker;
    private ImageView ivSelectedImage;
    private LinearLayout llImagePlaceholder;
    private MaterialAutoCompleteTextView actvCategory, actvCondition;
    private TextInputEditText etProductName, etProductPrice, etProductWeight, etProductDesc;

    private MaterialButton btnPublishProduct, btnMagicDescription;

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

        setupAutoTagging();

        btnPublishProduct.setOnClickListener(v -> startPublishingProcess());

        btnMagicDescription.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            String category = actvCategory.getText().toString().trim();
            String condition = actvCondition.getText().toString().trim();

            if (name.isEmpty()) {
                etProductName.setError("Enter a name first!");
                etProductName.requestFocus();
                return;
            }
            if (category.isEmpty() || condition.isEmpty()) {
                Toast.makeText(this, "Select a Category and Condition first!", Toast.LENGTH_SHORT).show();
                return;
            }

            generateMagicDescription(name, category, condition);
        });

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

    private void setupAutoTagging() {
        autoTagRules = new HashMap<>();

        // 1. Mobile Phones
        autoTagRules.put("phone", "Mobile Phones");
        autoTagRules.put("iphone", "Mobile Phones");
        autoTagRules.put("samsung", "Mobile Phones");
        autoTagRules.put("pixel", "Mobile Phones");
        autoTagRules.put("smartphone", "Mobile Phones");

        // 2. Laptops
        autoTagRules.put("laptop", "Laptops");
        autoTagRules.put("macbook", "Laptops");
        autoTagRules.put("thinkpad", "Laptops");
        autoTagRules.put("chromebook", "Laptops");

        // 3. Headphones
        autoTagRules.put("headphone", "Headphones");
        autoTagRules.put("earbud", "Headphones");
        autoTagRules.put("airpod", "Headphones");
        autoTagRules.put("headset", "Headphones");

        // 4. Microphone
        autoTagRules.put("mic", "Microphone");
        autoTagRules.put("microphone", "Microphone");

        // 5. Consoles
        autoTagRules.put("playstation", "Consoles");
        autoTagRules.put("ps4", "Consoles");
        autoTagRules.put("ps5", "Consoles");
        autoTagRules.put("xbox", "Consoles");
        autoTagRules.put("nintendo", "Consoles");
        autoTagRules.put("switch", "Consoles");
        autoTagRules.put("console", "Consoles");

        // 6. Electronics (Catch-all for general tech)
        autoTagRules.put("tv", "Electronics");
        autoTagRules.put("camera", "Electronics");
        autoTagRules.put("watch", "Electronics");
        autoTagRules.put("speaker", "Electronics");
        autoTagRules.put("charger", "Electronics");
        autoTagRules.put("mouse", "Electronics");
        autoTagRules.put("keyboard", "Electronics");

        etProductName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String inputName = s.toString().toLowerCase().trim();

                for (Map.Entry<String, String> entry : autoTagRules.entrySet()) {
                    if (inputName.contains(entry.getKey())) {
                        String predictedCategory = entry.getValue();

                        // Only update if it's not already selected (prevents UI flickering)
                        if (!actvCategory.getText().toString().equals(predictedCategory)) {
                            // false prevents the dropdown menu from popping open on the screen
                            actvCategory.setText(predictedCategory, false);
                            Toast.makeText(AddProductActivity.this, "Auto-tagged: " + predictedCategory + " ✨", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
            }
        });
    }

    private void startPublishingProcess() {
        String name = etProductName.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String priceStr = etProductPrice.getText().toString().trim();
        String weightStr = etProductWeight.getText().toString().trim();
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        if (selectedLocalImageUri.isEmpty() || category.isEmpty() || name.isEmpty() || priceStr.isEmpty() || weightStr.isEmpty() || desc.isEmpty() || condition.isEmpty()) {
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
        String categoryName = actvCategory.getText().toString().trim();
        String realCategoryId = categoryMap.get(categoryName);

        double price = Double.parseDouble(etProductPrice.getText().toString().trim());
        int weight = Integer.parseInt(etProductWeight.getText().toString().trim());
        String desc = etProductDesc.getText().toString().trim();
        String condition = actvCondition.getText().toString().trim();

        String storeId = sessionManager.getStoreId();

        if (storeId == null || realCategoryId == null) {
            Toast.makeText(this, "Error: Missing Store ID or Category data syncing.", Toast.LENGTH_SHORT).show();
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
        newProduct.setCategoryId(realCategoryId);
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
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Constants.CATEGORIES
        );
        actvCategory.setAdapter(categoryAdapter);

        FirebaseFirestore.getInstance().collection("categories").get().addOnSuccessListener(snapshots -> {
            for (DocumentSnapshot doc : snapshots) {
                Category c = doc.toObject(Category.class);
                if (c != null) {
                    categoryMap.put(c.getName(), c.getCategoryId());
                }
            }
        });

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                Constants.CONDITIONS
        );
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

        btnMagicDescription = findViewById(R.id.AddProdbtnMagicDescription);
    }

    private void generateMagicDescription(String productName, String category, String condition) {

        etProductDesc.setText(" AI is writing your description... please wait.");
        btnMagicDescription.setEnabled(false);

        String prompt = "You are an expert e-commerce copywriter. Write a compelling, " +
                "3-sentence product description for a " + condition +
                " item, in the " + category + " category. " +
                "Do not include any greetings, product naming, hashtags, or formatting. Just pure text.";

        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject parts = new JSONObject();
            parts.put("text", prompt);

            JSONObject message = new JSONObject();
            message.put("parts", new JSONArray().put(parts));

            contents.put(message);
            jsonBody.put("contents", contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String apiKey = BuildConfig.GEMINI_API_KEY; // API KEY
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + apiKey;
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        android.util.Log.d("AI_TEST", "Calling URL: " + url);
        Request request = new Request.Builder().url(url).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnMagicDescription.setEnabled(true);
                    etProductDesc.setText("");
                    Toast.makeText(AddProductActivity.this, "Network Error: Check your connection.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        String generatedText = jsonObject.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        runOnUiThread(() -> {
                            btnMagicDescription.setEnabled(true);
                            etProductDesc.setText(generatedText.trim());
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            btnMagicDescription.setEnabled(true);
                            Toast.makeText(AddProductActivity.this, "Error parsing AI response.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }  else {
                    String errorBody = "Unknown Error";
                    try {
                        if (response.body() != null) {
                            errorBody = response.body().string();
                        }
                    } catch (IOException ignored) {}

                    final String finalError = errorBody;
                    android.util.Log.e("AI_TEST", "Google API Error Body: " + finalError);

                    runOnUiThread(() -> {
                        btnMagicDescription.setEnabled(true);
                        Toast.makeText(AddProductActivity.this, "API Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }
}