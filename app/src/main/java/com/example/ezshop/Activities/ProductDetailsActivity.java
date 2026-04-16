package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.R;
import com.example.ezshop.adapters.ReviewsAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Review;

import java.util.ArrayList;

public class ProductDetailsActivity extends AppCompatActivity {
    private ImageView btnBack, ivDetailImage;
    private TextView tvDetailName, tvDetailPrice, tvDetailRating, tvDetailSold;
    private TextView tvDetailCondition, tvDetailWeight, tvDetailStoreName, tvDetailDesc;
    private TextView tvStoreTitle, tvStoreLocation, btnSeeAllReviews;
    private Button btnAddToCart;
    private RecyclerView rvReviews;

    // Database
    private DBManager dbManager;

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

        init();

        btnBack.setOnClickListener( v-> { finish(); });

        Intent intent = getIntent();
         if (intent!=null){
                // setting up basic views
             int productId = intent.getIntExtra("PRODUCT_ID" , -1);
             tvDetailName.setText(intent.getStringExtra("PRODUCT_NAME"));
             tvDetailPrice.setText("$ " + intent.getDoubleExtra("PRODUCT_PRICE", 0.0));
             tvDetailRating.setText(String.valueOf(intent.getDoubleExtra("PRODUCT_RATING", 0.0)));
             tvDetailSold.setText("Sold " + intent.getIntExtra("PRODUCT_SOLD", 0) + "+");

             tvDetailCondition.setText(": " + intent.getStringExtra("PRODUCT_CONDITION"));
             tvDetailWeight.setText(": " + intent.getIntExtra("PRODUCT_WEIGHT", 0) + " Gram");

             String storeName = intent.getStringExtra("STORE_NAME");
             tvDetailStoreName.setText(": " + storeName);
             tvStoreTitle.setText(storeName);

             tvStoreLocation.setText("Online • " + intent.getStringExtra("PRODUCT_LOCATION"));

             String desc = intent.getStringExtra("PRODUCT_DESC");
             tvDetailDesc.setText(desc != null ? desc : "No description available.");

             String imageName = intent.getStringExtra("PRODUCT_IMAGE");
             if (imageName != null) {
                 int imageId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                 if (imageId != 0)  ivDetailImage.setImageResource(imageId);
                    }

             // setting up review  recycler view

             if (productId != -1)
             {

                 ArrayList<Review> productReviews = dbManager.reviewDB.getReviewsByProductId(productId);

                 ReviewsAdapter reviewAdapter = new ReviewsAdapter(this, productReviews, true);
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
             }


         }

    }

    void init(){
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

    }


}