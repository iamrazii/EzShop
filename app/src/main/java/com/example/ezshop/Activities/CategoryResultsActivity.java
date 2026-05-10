package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.R;
import com.example.ezshop.adapters.ExploreProductRVAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class CategoryResultsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvTitle, btnSortPriceAsc, btnSortPriceDesc, btnSortRating;
    private RecyclerView rvProducts;
    private DBManager dbManager;
    private ExploreProductRVAdapter adapter;
    private ArrayList<Product> productlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_results);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
        btnBack.setOnClickListener(v -> finish());
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));

        Intent intent = getIntent();
        String categoryId = intent.getStringExtra("CATEGORY_ID");
        String categoryName = intent.getStringExtra("CATEGORY_NAME");
        String searchQuery = intent.getStringExtra("SEARCH_QUERY");

        if (categoryName != null) tvTitle.setText(categoryName);

        if (categoryId != null) {
            dbManager.productDB.getProductsByCategory(categoryId)
                    .addOnSuccessListener(this, queryDocumentSnapshots -> {
                        productlist = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            productlist.add(doc.toObject(Product.class));
                        }
                        setupAdapter();
                        setupFilters();
                    });
        } else if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            tvTitle.setText("Search: '" + searchQuery + "'");


            dbManager.productDB.getAllProducts()
                    .addOnSuccessListener(this, queryDocumentSnapshots -> {
                        productlist = new ArrayList<>();
                        String lowerCaseQuery = searchQuery.toLowerCase();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Product product = doc.toObject(Product.class);

                            if (product != null && product.getName() != null) {
                                String productNameLower = product.getName().toLowerCase();

                                if (productNameLower.contains(lowerCaseQuery)) {
                                    productlist.add(product);
                                }
                            }
                        }
                        if (productlist.isEmpty()) {
                            Toast.makeText(this, "No products found for '" + searchQuery + "'", Toast.LENGTH_SHORT).show();
                        }

                        setupAdapter();
                        setupFilters();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            Toast.makeText(this, "Error loading items", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupFilters() {
        btnSortRating.setOnClickListener(v -> {
            Collections.sort(productlist, (p1, p2) -> Double.compare(p2.getRatingAverage(), p1.getRatingAverage()));
            adapter.notifyDataSetChanged();
        });

        btnSortPriceAsc.setOnClickListener(v -> {
            Collections.sort(productlist, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            adapter.notifyDataSetChanged();
        });

        btnSortPriceDesc.setOnClickListener(v -> {
            Collections.sort(productlist, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            adapter.notifyDataSetChanged();
        });
    }

    private void setupAdapter() {
        adapter = new ExploreProductRVAdapter(this, productlist, product -> {
            Intent intent = new Intent(CategoryResultsActivity.this, ProductDetailsActivity.class);
            intent.putExtra("PRODUCT_ID", product.getProductId());
            intent.putExtra("PRODUCT_NAME", product.getName());
            intent.putExtra("PRODUCT_PRICE", product.getPrice());
            intent.putExtra("PRODUCT_IMAGE", product.getProductimage());
            intent.putExtra("PRODUCT_RATING", product.getRatingAverage());
            intent.putExtra("PRODUCT_SOLD", product.getSoldCount());
            intent.putExtra("PRODUCT_CONDITION", product.getCondition());
            intent.putExtra("PRODUCT_WEIGHT", product.getWeightGrams());
            intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());

            // Fire and forget store info request
            dbManager.storeDB.getStoreById(product.getStoreId()).addOnSuccessListener(doc -> {
                if(doc.exists()) {
                    intent.putExtra("PRODUCT_LOCATION", doc.getString("location"));
                    intent.putExtra("STORE_NAME", doc.getString("storeName"));
                }
                startActivity(intent);
            });
        });
        rvProducts.setAdapter(adapter);
    }

    private void init() {
        dbManager = new DBManager(this);
        dbManager.open();

        btnBack = findViewById(R.id.catResBtnBack);
        tvTitle = findViewById(R.id.catResTvTitle);
        btnSortPriceAsc = findViewById(R.id.catResBtnSortPriceAsc);
        btnSortPriceDesc = findViewById(R.id.catResBtnSortPriceDesc);
        btnSortRating = findViewById(R.id.catResBtnSortRating);
        rvProducts = findViewById(R.id.catResRvProducts);
    }
}