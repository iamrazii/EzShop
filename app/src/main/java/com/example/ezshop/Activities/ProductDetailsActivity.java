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
import com.example.ezshop.models.Review;
import com.example.ezshop.utilities.SessionManager;
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

        // Hide buyer elements if the user is a Seller
        if (sessionManager.getUserType().equalsIgnoreCase("Seller")) {
            prodDetailBottomBar.setVisibility(View.GONE);
            layoutStoreInfo.setVisibility(View.GONE);
            rowStorefront.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent == null) return;

        // FIXED: Effectively final declaration to avoid local variable error in lambda
        final String productId = intent.hasExtra("PRODUCT_ID") ?
                intent.getStringExtra("PRODUCT_ID") : intent.getStringExtra("product_id");

        // Set Basic Text Data
        tvDetailName.setText(intent.getStringExtra("PRODUCT_NAME"));
        tvDetailPrice.setText("$ " + String.format("%.2f", intent.getDoubleExtra("PRODUCT_PRICE", 0.0)));
        tvDetailRating.setText(String.valueOf(intent.getDoubleExtra("PRODUCT_RATING", 0.0)));
        tvDetailSold.setText("Sold " + intent.getIntExtra("PRODUCT_SOLD", 0) + "+");
        tvDetailCondition.setText(": " + intent.getStringExtra("PRODUCT_CONDITION"));
        tvDetailWeight.setText(": " + intent.getIntExtra("PRODUCT_WEIGHT", 0) + " Gram");

        // Set Store Data
        String storeName = intent.getStringExtra("STORE_NAME");
        tvDetailStoreName.setText(": " + (storeName != null ? storeName : "Unknown"));
        tvStoreTitle.setText(storeName != null ? storeName : "Unknown");
        tvStoreLocation.setText("Online • " + intent.getStringExtra("PRODUCT_LOCATION"));
        tvDetailDesc.setText(intent.getStringExtra("PRODUCT_DESCRIPTION"));

        // Set Image Data (Handles both local URI and Drawable strings)
        String imageName = intent.getStringExtra("PRODUCT_IMAGE");
        if (imageName != null) {
            if (imageName.startsWith("content://") || imageName.startsWith("file://")) {
                ivDetailImage.setImageURI(Uri.parse(imageName));
            } else {
                int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                if (imageId != 0) ivDetailImage.setImageResource(imageId);
            }
        }

        if (productId != null) {
            // Load Reviews
            ArrayList<Pair<Review, String>> productReviews = dbManager.reviewDB.getReviewsByProductId(productId);
            ReviewsAdapter reviewAdapter = new ReviewsAdapter(this, productReviews, true);
            rvReviews.setLayoutManager(new LinearLayoutManager(this));
            rvReviews.setAdapter(reviewAdapter);

            // Handle "See All Reviews" visibility
            if (productReviews.size() <= 3) {
                btnSeeAllReviews.setVisibility(View.GONE);
            } else {
                btnSeeAllReviews.setOnClickListener(v -> {
                    Intent reviewIntent = new Intent(ProductDetailsActivity.this, ReviewsActivity.class);
                    reviewIntent.putExtra("PRODUCT_ID", productId);
                    startActivity(reviewIntent);
                });
            }

            // Handle Add to Cart
            btnAddToCart.setOnClickListener(v -> {
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Adds item to database cart
                dbManager.cartItemDB.addToCart(sessionManager.getUserId(), productId, "", 1);
                Toast.makeText(this, "Added to cart! 🛒", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void init() {
        dbManager = new DBManager(this);
        dbManager.open();

        // Bind Views
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

        // Layout Containers
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