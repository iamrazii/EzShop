package com.example.ezshop.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.R;
import com.example.ezshop.adapters.ReviewsAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.CartItem;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Review;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// ✨ NEW GEMINI SDK IMPORTS ✨
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;

import java.util.ArrayList;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, ivDetailImage;
    private TextView tvDetailName, tvDetailPrice, tvDetailRating, tvDetailSold;
    private TextView tvDetailCondition, tvDetailWeight, tvDetailStoreName, tvDetailDesc;
    private TextView tvStoreTitle, tvStoreLocation, btnSeeAllReviews;
    private Button btnAddToCart, ChatwithSeller;
    private LinearLayout prodDetailBottomBar, layoutStoreInfo, rowStorefront;
    private RecyclerView rvReviews;

    // AI Summary Views
    private LinearLayout layoutAiSummary;
    private TextView tvAiSummaryText;

    private DBManager dbManager;
    private SessionManager sessionManager;

    // 🔥 NEW: Global variable to hold the store ID for the chat hop
    private String actualStoreId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        init();

        if (sessionManager.getUserType().equalsIgnoreCase("Seller")) {
            prodDetailBottomBar.setVisibility(View.GONE);
            layoutStoreInfo.setVisibility(View.GONE);
            rowStorefront.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent == null) return;

        final String productId = intent.hasExtra("PRODUCT_ID") ?
                intent.getStringExtra("PRODUCT_ID") : intent.getStringExtra("product_id");

        if (productId != null) {
            FirebaseFirestore.getInstance().collection("products").document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Product p = documentSnapshot.toObject(Product.class);
                            if (p != null && p.getStoreId() != null) {
                                actualStoreId = p.getStoreId();
                            }
                        }
                    });
        }

        tvDetailName.setText(intent.getStringExtra("PRODUCT_NAME"));
        tvDetailPrice.setText("$ " + String.format("%.2f", intent.getDoubleExtra("PRODUCT_PRICE", 0.0)));
        tvDetailRating.setText(String.valueOf(intent.getDoubleExtra("PRODUCT_RATING", 0.0)));
        tvDetailSold.setText("Sold " + intent.getIntExtra("PRODUCT_SOLD", 0) + "+");
        tvDetailCondition.setText(": " + intent.getStringExtra("PRODUCT_CONDITION"));
        tvDetailWeight.setText(": " + intent.getIntExtra("PRODUCT_WEIGHT", 0) + " Gram");

        String storeName = intent.getStringExtra("STORE_NAME");
        tvDetailStoreName.setText(": " + (storeName != null ? storeName : "Unknown"));
        tvStoreTitle.setText(storeName != null ? storeName : "Unknown");
        tvStoreLocation.setText("Online • " + intent.getStringExtra("PRODUCT_LOCATION"));
        tvDetailDesc.setText(intent.getStringExtra("PRODUCT_DESCRIPTION"));

        String imageUrl = intent.getStringExtra("PRODUCT_IMAGE");

        if (imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(ivDetailImage);
        } else {
            ivDetailImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        ChatwithSeller.setOnClickListener(v -> {
            if (actualStoreId == null) {
                Toast.makeText(ProductDetailsActivity.this, "Loading store info... please wait.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Temporarily disable button to prevent spam clicking
            ChatwithSeller.setEnabled(false);
            ChatwithSeller.setText("Connecting...");

            // Look up the store to find the owner's User ID
            FirebaseFirestore.getInstance().collection("stores").document(actualStoreId)
                    .get()
                    .addOnSuccessListener(storeSnap -> {
                        // Reset button
                        ChatwithSeller.setEnabled(true);
                        ChatwithSeller.setText("💬 Chat");

                        if (storeSnap.exists()) {
                            Store store = storeSnap.toObject(Store.class);
                            if (store != null && store.getOwner_id() != null) {
                                Intent i = new Intent(ProductDetailsActivity.this, ChatActivity.class);
                                i.putExtra("CHAT_PARTNER_ID", store.getOwner_id());
                                startActivity(i);
                            } else {
                                Toast.makeText(ProductDetailsActivity.this, "Could not find store owner.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        ChatwithSeller.setEnabled(true);
                        ChatwithSeller.setText("💬 Chat");
                        Toast.makeText(ProductDetailsActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                    });
        });

        if (productId != null) {
            dbManager.reviewDB.getReviewsByProductId(productId).addOnSuccessListener(this, snap -> {
                ArrayList<Pair<Review, String>> productReviews = new ArrayList<>();
                StringBuilder allCommentsForAI = new StringBuilder();

                ReviewsAdapter reviewAdapter = new ReviewsAdapter(this, productReviews, true);
                rvReviews.setLayoutManager(new LinearLayoutManager(this));
                rvReviews.setAdapter(reviewAdapter);

                int totalReviews = snap.size();

                // If there are no reviews, hide the AI and button, then stop.
                if (totalReviews == 0) {
                    layoutAiSummary.setVisibility(View.GONE);
                    btnSeeAllReviews.setVisibility(View.GONE);
                    return;
                }

                for (DocumentSnapshot doc : snap) {
                    Review r = doc.toObject(Review.class);
                    if (r != null) {

                        // Make a call to the UserDB to get the real name
                        dbManager.userDB.getUserById(r.getUserId()).addOnCompleteListener(task -> {
                            String realName = "Unknown User";

                            // If we successfully found the user, grab their name
                            if (task.isSuccessful() && task.getResult().exists()) {
                                User reviewer = task.getResult().toObject(User.class);
                                if (reviewer != null && reviewer.getName() != null) {
                                    realName = reviewer.getName();
                                }
                            }

                            // Add the review with the REAL name to the list
                            productReviews.add(new Pair<>(r, realName));
                            reviewAdapter.notifyDataSetChanged(); // Update the screen instantly

                            if (r.getComment() != null && !r.getComment().trim().isEmpty()) {
                                allCommentsForAI.append("- ").append(r.getComment()).append("\n");
                            }

                            if (productReviews.size() == totalReviews) {

                                // Now that everything is loaded, trigger the AI
                                if (productReviews.size() >= 3) {
                                    layoutAiSummary.setVisibility(View.VISIBLE);
                                    tvAiSummaryText.setText("Reading reviews...");
                                    generateAISummary(allCommentsForAI.toString());
                                } else {
                                    layoutAiSummary.setVisibility(View.GONE);
                                }

                                // Show or hide the "See All" button
                                if (productReviews.size() <= 3) {
                                    btnSeeAllReviews.setVisibility(View.GONE);
                                } else {
                                    btnSeeAllReviews.setVisibility(View.VISIBLE);
                                    btnSeeAllReviews.setOnClickListener(v -> {
                                        Intent reviewIntent = new Intent(ProductDetailsActivity.this, ReviewsActivity.class);
                                        reviewIntent.putExtra("PRODUCT_ID", productId);
                                        startActivity(reviewIntent);
                                    });
                                }
                            }
                        });
                    }
                }
            }).addOnFailureListener(e -> {
                android.util.Log.e("FirestoreError", "Failed to load reviews", e);
                Toast.makeText(this, "Error loading reviews: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

            btnAddToCart.setOnClickListener(v -> {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                    return;
                }

                CartItem newItem = new CartItem(null, sessionManager.getUserId(), productId, 1);
                dbManager.cartItemDB.saveCartItem(newItem).addOnSuccessListener(this, aVoid ->
                        Toast.makeText(this, "Added to cart! 🛒", Toast.LENGTH_SHORT).show()
                );
            });
        }
    }

    private void init() {
        dbManager = new DBManager(this);
        dbManager.open();

        btnBack = findViewById(R.id.prodDetailBtnBack);
        ivDetailImage = findViewById(R.id.prodDetailIvDetailImage);
        tvDetailName = findViewById(R.id.prodDetailTvDetailName);
        tvDetailPrice = findViewById(R.id.prodDetailTvDetailPrice);
        tvDetailRating = findViewById(R.id.prodDetailTvDetailRating);
        tvDetailSold = findViewById(R.id.prodDetailTvDetailSold);
        tvDetailCondition = findViewById(R.id.prodDetailTvDetailCondition);
        tvDetailWeight = findViewById(R.id.prodDetailTvDetailWeight);
        tvDetailStoreName = findViewById(R.id.prodDetailTvDetailStoreName);
        tvDetailDesc = findViewById(R.id.prodDetailTvDetailDesc);
        tvStoreTitle = findViewById(R.id.prodDetailTvStoreTitle);
        tvStoreLocation = findViewById(R.id.prodDetailTvStoreLocation);
        btnAddToCart = findViewById(R.id.prodDetailBtnAddToCart);
        btnSeeAllReviews = findViewById(R.id.prodDetailBtnSeeAllReviews);
        rvReviews = findViewById(R.id.prodDetailRvReviews);

        ChatwithSeller = findViewById(R.id.btnChatWithSeller);

        prodDetailBottomBar = findViewById(R.id.prodDetailBottomBar);
        layoutStoreInfo = findViewById(R.id.layoutStoreInfo);
        rowStorefront = findViewById(R.id.rowStorefront);

        // Initialize AI views
        layoutAiSummary = findViewById(R.id.layoutAiSummary);
        tvAiSummaryText = findViewById(R.id.tvAiSummaryText);
    }

    private void generateAISummary(String reviewsText) {
        // 1. Initialize Model
        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", "AIzaSyACbNXePoBtBZlhoA7wM9Bx3Q41mcdP3_g");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // 2. Build the System Prompt
        String promptText = "You are an AI summarizing product reviews for an e-commerce app. " +
                "Read the following reviews and provide a single summary paragraph in a maximum of 3 short sentences. " +
                "Focus purely on the main pros and cons. Do not use conversational filler.\n\nReviews:\n" + reviewsText;

        Content content = new Content.Builder().addText(promptText).build();

        // 3. Run the Request on a Background Thread using Guava Futures
        Executor executor = ContextCompat.getMainExecutor(this);
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                // Update UI safely on the main thread
                runOnUiThread(() -> {
                    if (result != null && result.getText() != null) {
                        tvAiSummaryText.setText(result.getText().trim());
                    } else {
                        tvAiSummaryText.setText("Summary unavailable.");
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                android.util.Log.e("GeminiError", "AI Summary Failed", t);
                runOnUiThread(() -> {
                    tvAiSummaryText.setText("Could not generate summary at this time.");
                });
            }
        }, executor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}