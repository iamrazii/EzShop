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
import com.example.ezshop.models.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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

        btnBack.setOnClickListener(v-> {
            finish();
        });
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        Intent intent = getIntent();
        String categoryId = intent.getStringExtra("CATEGORY_ID");
        String categoryName = intent.getStringExtra("CATEGORY_NAME");

        String searchQuery = intent.getStringExtra("SEARCH_QUERY");

        if (categoryName!= null) tvTitle.setText(categoryName);
        if (categoryId!= null){
           productlist =  dbManager.productDB.getProductsByCategory(categoryId);

           setupAdapter();
           setupFilters();

        }
        else if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            tvTitle.setText("Search: '" + searchQuery + "'");

            // Fetch the search results instead of category results
            productlist = dbManager.productDB.searchProducts(searchQuery);

            setupAdapter();
            setupFilters();

        }
        else {
            Toast.makeText(this, "Error loading items", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    private void setupFilters(){
        btnSortRating.setOnClickListener(v -> {
            Collections.sort(productlist, new Comparator<Product>() {
                @Override
                public int compare(Product p1, Product p2) {
                    // p2 first = Descending
                    return Double.compare(p2.getRatingAverage(), p1.getRatingAverage());
                }
            });
            adapter.notifyDataSetChanged();
        });

        btnSortPriceAsc.setOnClickListener(v -> {
            Collections.sort(productlist, new Comparator<Product>() {
                @Override
                public int compare(Product p1, Product p2) {
                    // p1 first = Ascending
                    return Double.compare(p1.getPrice(), p2.getPrice());
                }
            });
            adapter.notifyDataSetChanged();
        });

        btnSortPriceDesc.setOnClickListener(v -> {
            Collections.sort(productlist, new Comparator<Product>() {
                @Override
                public int compare(Product p1, Product p2) {
                    // p2 first = Descending
                    return Double.compare(p2.getPrice(), p1.getPrice());
                }
            });
            adapter.notifyDataSetChanged();
        });
    }
    private void setupAdapter(){
        adapter = new ExploreProductRVAdapter(this, productlist, new ExploreProductRVAdapter.OnProductClickListener(){
            @Override
            public void onProductClick(Product product) {
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

                Store s = dbManager.storeDB.getStoreById(product.getStoreId());
                String location = s.getLocation();
                String storeName = s.getStoreName();
                intent.putExtra("PRODUCT_LOCATION", location);
                intent.putExtra("STORE_NAME", storeName);
                startActivity(intent);
            }
        });
        rvProducts.setAdapter(adapter);
    }


    private void init(){
        dbManager = new DBManager(this);
        try { dbManager.open(); }
        catch (Exception e) { e.printStackTrace(); }

        btnBack = findViewById(R.id.catResBtnBack);
        tvTitle = findViewById(R.id.catResTvTitle);
        btnSortPriceAsc = findViewById(R.id.catResBtnSortPriceAsc);
        btnSortPriceDesc = findViewById(R.id.catResBtnSortPriceDesc);
        btnSortRating = findViewById(R.id.catResBtnSortRating);
        rvProducts = findViewById(R.id.catResRvProducts);
    }
}