package com.example.ezshop.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.ezshop.Activities.ProductDetailsActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.CategoryAdapter;
import com.example.ezshop.adapters.ExploreProductRVAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class ExploreProductFragment extends Fragment {

    EditText Search;
    RecyclerView rvCategories;
    RecyclerView rvProducts;
    ChipGroup chipgrp;
    private DBManager db;

    public ExploreProductFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_explore_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Initialize Database Manager
        db = new DBManager(getContext());
        try {
            db.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Bind Views
        rvCategories = view.findViewById(R.id.explorervCategories);
        rvProducts = view.findViewById(R.id.explorervTrendingProducts);
        chipgrp = view.findViewById(R.id.explorechipGroupTrending);
        // Search = view.findViewById(R.id.your_search_edit_text_id); // Uncomment and add ID if you are using it!

        // 3. Setup Categories Layout (Horizontal Grid, 2 rows)
        GridLayoutManager categoryLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(categoryLayoutManager);

        // 4. Setup Trending Products Layout (Standard Vertical List)
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        // 5. LOAD ALL DATA
        loadTrendingSearches();
        loadCategories();
        loadTrendingProducts();
    }

    private void loadTrendingSearches() {
        // Fetch the top searches from the database
        ArrayList<String> trendingSearches = db.searchlogDB.getTrendingSearches();

        // Create a visual "Chip" for every string in our list
        for (String searchKeyword : trendingSearches) {
            Chip chip = new Chip(getContext());
            chip.setText(searchKeyword);
            chip.setCheckable(false);
            // Optional styling:
            // chip.setChipBackgroundColorResource(R.color.white);
            // chip.setStrokeColorResource(R.color.light_gray);
            // chip.setStrokeWidth(1f);

            // Handle clicks on the chip!
            chip.setOnClickListener(v -> {
                // Future feature: populate the search bar with this keyword
            });

            chipgrp.addView(chip);
        }
    }

    private void loadCategories() {
        // Fetch categories from the database
        ArrayList<Category> categories = db.categoryDB.getAllCategories();

        // Hand them to the Adapter
        CategoryAdapter adapter = new CategoryAdapter(getContext(), categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                // Future feature: Open Category Search Results
            }
        });

        // Attach the Adapter to your RecyclerView
        rvCategories.setAdapter(adapter);
    }

    private void loadTrendingProducts() {
        // Fetch products using our awesome SQL JOIN
        ArrayList<Product> topProducts = db.productDB.getTrendingProductsWithStoreInfo();

        // Hand them to the Adapter
        ExploreProductRVAdapter productAdapter = new ExploreProductRVAdapter(getContext(), topProducts, new ExploreProductRVAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Store s = db.storeDB.getStoreById(product.getStoreId());

                Intent intent = new Intent(requireActivity(), ProductDetailsActivity.class);
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_PRICE", product.getPrice());
                intent.putExtra("PRODUCT_IMAGE", product.getProductimage()); // e.g., "macbook"
                intent.putExtra("PRODUCT_LOCATION", s.getLocation());
                intent.putExtra("PRODUCT_RATING", product.getRatingAverage());

                startActivity(intent);
            }
        });

        // Attach the Adapter to your RecyclerView
        rvProducts.setAdapter(productAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Always close the database when the fragment is destroyed to prevent memory leaks!
        if (db != null) {
            db.close();
        }
    }

}