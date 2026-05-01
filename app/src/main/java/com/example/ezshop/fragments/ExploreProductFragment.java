package com.example.ezshop.fragments;

import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExploreProductFragment extends Fragment {

    EditText Search;
    RecyclerView rvCategories;
    RecyclerView rvProducts;
    ChipGroup chipgrp;
    private DBManager dbManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbManager = new DBManager(getContext());
        dbManager.open();

        rvCategories = view.findViewById(R.id.explorervCategories);
        rvProducts = view.findViewById(R.id.explorervTrendingProducts);
        chipgrp = view.findViewById(R.id.explorechipGroupTrending);
        Search = view.findViewById(R.id.exploreetSearch);
        ImageView btnSearchIcon = view.findViewById(R.id.exploreBtnSearch);

        Search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        btnSearchIcon.setOnClickListener(v -> performSearch());

        rvCategories.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false));
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        loadTrendingSearches();
        loadCategories();
        loadTrendingProducts();
    }

    private void performSearch() {
        String query = Search.getText().toString().trim();
        if (!query.isEmpty()) {
            dbManager.searchlogDB.addSearch(query);
            Intent intent = new Intent(requireActivity(), CategoryResultsActivity.class);
            intent.putExtra("SEARCH_QUERY", query);
            startActivity(intent);
            Search.setText("");
            loadTrendingSearches();
        }
    }

    private void loadTrendingSearches() {
        dbManager.searchlogDB.getAllLogs().addOnSuccessListener(snap -> {
            if (!isAdded() || getContext() == null) return;
            chipgrp.removeAllViews();

            // Local tallying for Firebase
            Map<String, Integer> counts = new HashMap<>();
            for (DocumentSnapshot doc : snap) {
                String q = doc.getString("searchQuery");
                if (q != null && !q.isEmpty()) counts.put(q, counts.getOrDefault(q, 0) + 1);
            }

            List<Map.Entry<String, Integer>> sortedSearches = new ArrayList<>(counts.entrySet());
            sortedSearches.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            for (int i = 0; i < Math.min(5, sortedSearches.size()); i++) {
                String searchKeyword = sortedSearches.get(i).getKey();
                Chip chip = new Chip(getContext());
                chip.setText(searchKeyword);
                chip.setCheckable(false);

                chip.setBackgroundResource(R.drawable.bg_category);
                chip.setPadding(32, 16, 32, 16);
                chip.setTextSize(14f);
                chip.setTextColor(getResources().getColor(android.R.color.black));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 16, 0);
                chip.setLayoutParams(params);

                chip.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), CategoryResultsActivity.class);
                    intent.putExtra("SEARCH_QUERY", searchKeyword);
                    startActivity(intent);
                });

                chipgrp.addView(chip);
            }
        });
    }

    private void loadCategories() {
        dbManager.categoryDB.getAllCategories().addOnSuccessListener(snap -> {
            if (!isAdded() || getContext() == null) return;
            ArrayList<Category> categories = new ArrayList<>();
            for (DocumentSnapshot doc : snap) categories.add(doc.toObject(Category.class));

            CategoryAdapter adapter = new CategoryAdapter(getContext(), categories, category -> {
                Intent intent = new Intent(getActivity(), CategoryResultsActivity.class);
                intent.putExtra("CATEGORY_ID", category.getCategoryId());
                intent.putExtra("CATEGORY_NAME", category.getName());
                startActivity(intent);
            });
            rvCategories.setAdapter(adapter);
        });
    }

    private void loadTrendingProducts() {
        dbManager.productDB.getAllProducts().addOnSuccessListener(snap -> {
            if (!isAdded() || getContext() == null) return;
            ArrayList<Product> topProducts = new ArrayList<>();

            // Limit to top 5 visually
            for (int i = 0; i < Math.min(5, snap.size()); i++) {
                topProducts.add(snap.getDocuments().get(i).toObject(Product.class));
            }

            ExploreProductRVAdapter productAdapter = new ExploreProductRVAdapter(getContext(), topProducts, product -> {
                // Async Store Fetch on click
                dbManager.storeDB.getStoreById(product.getStoreId()).addOnSuccessListener(storeSnap -> {
                    if (!isAdded() || getContext() == null) return;
                    Store s = storeSnap.toObject(Store.class);
                    Intent intent = new Intent(requireActivity(), ProductDetailsActivity.class);
                    intent.putExtra("PRODUCT_ID", product.getProductId());
                    intent.putExtra("PRODUCT_NAME", product.getName());
                    intent.putExtra("PRODUCT_SOLD", product.getSoldCount());
                    intent.putExtra("PRODUCT_PRICE", product.getPrice());
                    intent.putExtra("PRODUCT_IMAGE", product.getProductimage());
                    intent.putExtra("PRODUCT_RATING", product.getRatingAverage());
                    intent.putExtra("PRODUCT_WEIGHT", product.getWeightGrams());
                    intent.putExtra("PRODUCT_CONDITION", product.getCondition());
                    intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
                    if (s != null) {
                        intent.putExtra("PRODUCT_LOCATION", s.getLocation());
                        intent.putExtra("STORE_NAME", s.getStoreName());
                    }
                    startActivity(intent);
                });
            });
            rvProducts.setAdapter(productAdapter);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dbManager != null) {
            loadTrendingSearches();
            loadTrendingProducts();
            loadCategories();
        }
    }
}