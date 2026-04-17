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

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ezshop.Activities.CategoryResultsActivity;
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

        db = new DBManager(getContext());
        try {
            db.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        rvCategories = view.findViewById(R.id.explorervCategories);
        rvProducts = view.findViewById(R.id.explorervTrendingProducts);
        chipgrp = view.findViewById(R.id.explorechipGroupTrending);
         Search = view.findViewById(R.id.exploreetSearch);
        ImageView btnSearchIcon = view.findViewById(R.id.exploreBtnSearch);


        Search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                    String query = Search.getText().toString().trim();
                    Toast.makeText(getContext(),"Hello",Toast.LENGTH_SHORT).show();
                    if (!query.isEmpty()) {
                        db.searchlogDB.addSearch(query);
                        Intent intent = new Intent(requireActivity(), CategoryResultsActivity.class);
                        intent.putExtra("SEARCH_QUERY", query);
                        startActivity(intent);
                        Search.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        btnSearchIcon.setOnClickListener(v -> {
            String query = Search.getText().toString().trim();

            if (!query.isEmpty()) {
                db.searchlogDB.addSearch(query);
                Intent intent = new Intent(requireActivity(), CategoryResultsActivity.class);
                intent.putExtra("SEARCH_QUERY", query);
                startActivity(intent);
                Search.setText("");
                loadTrendingSearches();
            }
        });

        GridLayoutManager categoryLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        rvCategories.setLayoutManager(categoryLayoutManager);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTrendingSearches();
        loadCategories();
        loadTrendingProducts();
    }

    private void loadTrendingSearches() {

        chipgrp.removeAllViews();
        ArrayList<String> trendingSearches = db.searchlogDB.getTrendingSearches();

        for (String searchKeyword : trendingSearches) {
            Chip chip = new Chip(getContext());
            chip.setText(searchKeyword);
            chip.setCheckable(false);
            chip.setBackgroundResource(R.drawable.bg_category);
            chip.setPadding(32, 16, 32, 16);
            chip.setTextSize(14f);
            chip.setTextColor(getResources().getColor(android.R.color.black));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CategoryResultsActivity.class);
                intent.putExtra("SEARCH_QUERY", searchKeyword);
                startActivity(intent);
            });

            chipgrp.addView(chip);
        }
    }

    private void loadCategories() {
        ArrayList<Category> categories = db.categoryDB.getAllCategories();
        CategoryAdapter adapter = new CategoryAdapter(getContext(), categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                Intent intent = new Intent(getActivity(), CategoryResultsActivity.class);
                intent.putExtra("CATEGORY_ID", category.getCategoryId());
                intent.putExtra("CATEGORY_NAME", category.getName());
                startActivity(intent);
            }
        });

        // Attach the Adapter to your RecyclerView
        rvCategories.setAdapter(adapter);
    }

    private void loadTrendingProducts() {

        ArrayList<Product> topProducts = db.productDB.getTrendingProductsWithStoreInfo();
        ExploreProductRVAdapter productAdapter = new ExploreProductRVAdapter(getContext(), topProducts, new ExploreProductRVAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Store s = db.storeDB.getStoreById(product.getStoreId());
                Intent intent = new Intent(requireActivity(), ProductDetailsActivity.class);
                intent.putExtra("PRODUCT_ID", product.getProductId());
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_Sold", product.getSoldCount());
                intent.putExtra("PRODUCT_PRICE", product.getPrice());
                intent.putExtra("PRODUCT_IMAGE", product.getProductimage()); // e.g., "macbook"
                intent.putExtra("PRODUCT_LOCATION", s.getLocation());
                intent.putExtra("PRODUCT_RATING", product.getRatingAverage());
                intent.putExtra("PRODUCT_WEIGHT", product.getWeightGrams());
                intent.putExtra("PRODUCT_CONDITION", product.getCondition());
                intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
                intent.putExtra("STORE_NAME", s.getStoreName());

                startActivity(intent);
            }
        });

        rvProducts.setAdapter(productAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // refreshing on resume
        if (db != null) {
            loadTrendingSearches();
            loadTrendingProducts();
            loadCategories();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (db != null) {
            db.close();
        }
    }

}