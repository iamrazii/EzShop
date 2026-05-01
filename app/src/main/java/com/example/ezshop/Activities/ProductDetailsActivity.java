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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.adapters.ReviewsAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.CartItem;
import com.example.ezshop.models.Review;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class ProductDetailsActivity extends AppCompatActivity {

    private ImageView btnBack, ivDetailImage;
    private TextView tvDetailName, tvDetailPrice, tvDetailRating, tvDetailSold;
    private TextView tvDetailCondition, tvDetailWeight, tvDetailStoreName, tvDetailDesc;
    private TextView tvStoreTitle, tvStoreLocation, btnSeeAllReviews;
    private Button btnAddToCart;
    private LinearLayout prodDetailBottomBar, layoutStoreInfo, rowStorefront;
    private RecyclerView rvReviews;
    private DBManager dbManager;
    private SessionManager sessionManager;

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

        if (productId != null) {
            dbManager.reviewDB.getReviewsByProductId(productId).addOnSuccessListener(this, snap -> {
                ArrayList<Pair<Review, String>> productReviews = new ArrayList<>();
                for (DocumentSnapshot doc : snap) {
                    // Temporarily mapping user name as "User" since full JOIN is async.
                    productReviews.add(new Pair<>(doc.toObject(Review.class), "User"));
                }

                ReviewsAdapter reviewAdapter = new ReviewsAdapter(this, productReviews, true);
                rvReviews.setLayoutManager(new LinearLayoutManager(this));
                rvReviews.setAdapter(reviewAdapter);

                if (productReviews.size() <= 3) {
                    btnSeeAllReviews.setVisibility(View.GONE);
                } else {
                    btnSeeAllReviews.setOnClickListener(v -> {
                        Intent reviewIntent = new Intent(ProductDetailsActivity.this, ReviewsActivity.class);
                        reviewIntent.putExtra("PRODUCT_ID", productId);
                        startActivity(reviewIntent);
                    });
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pure Firebase Cart Addition
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

        prodDetailBottomBar = findViewById(R.id.prodDetailBottomBar);
        layoutStoreInfo = findViewById(R.id.layoutStoreInfo);
        rowStorefront = findViewById(R.id.rowStorefront);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}