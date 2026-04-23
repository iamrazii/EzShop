package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.adapters.ProductCardAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import java.util.ArrayList;

public class UserHomeActivity extends AppCompatActivity {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private RecyclerView rvBestSellers, rvRecommendations;
    private LinearLayout llCategories;
    private TextView tvUserName, tvBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        tvUserName = findViewById(R.id.tvUserName);
        tvBalance = findViewById(R.id.tvBalance);
        llCategories = findViewById(R.id.llCategories);
        rvBestSellers = findViewById(R.id.rvBestSellers);
        rvRecommendations = findViewById(R.id.rvRecommendations);

        rvBestSellers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this));

        loadUserData();
        loadCategories();
        loadProducts();

        findViewById(R.id.ivCart).setOnClickListener(v ->
            startActivity(new Intent(this, CartActivity.class))
        );
        findViewById(R.id.tvSearchBar).setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryResultsActivity.class);
            intent.putExtra("SHOW_ALL", true);
            startActivity(intent);
        });

        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_explore) {
                Intent i = new Intent(this, CategoryResultsActivity.class);
                i.putExtra("SHOW_ALL", true);
                startActivity(i);
                return true;
            } else if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                sessionManager.logoutUser();
                Intent i = new Intent(this, WelcomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finishAffinity();
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;
        User user = dbManager.userDB.getUserById(userId);
        if (user != null) {
            tvUserName.setText(user.getName());
            tvBalance.setText(String.format("$%.3f", user.getWalletBalance()));
        }
    }

    private void loadCategories() {
        ArrayList<Category> categories = dbManager.categoryDB.getAllCategories();
        LayoutInflater inflater = LayoutInflater.from(this);
        llCategories.removeAllViews();
        for (Category cat : categories) {
            View chip = inflater.inflate(R.layout.item_category_chip, llCategories, false);
            TextView tvName = chip.findViewById(R.id.tvCategoryName);
            tvName.setText(cat.getName());
            String catId = cat.getCategoryId();
            String catName = cat.getName();
            chip.setOnClickListener(v -> {
                Intent intent = new Intent(this, CategoryResultsActivity.class);
                intent.putExtra("CATEGORY_ID", catId);
                intent.putExtra("CATEGORY_NAME", catName);
                startActivity(intent);
            });
            llCategories.addView(chip);
        }
    }

    private void loadProducts() {
        ArrayList<Product> allProducts = dbManager.productDB.getAllProducts();
        ProductCardAdapter bestAdapter = new ProductCardAdapter(allProducts, p -> launchDetail(p));
        rvBestSellers.setAdapter(bestAdapter);
        ProductCardAdapter recAdapter = new ProductCardAdapter(allProducts, p -> launchDetail(p));
        rvRecommendations.setAdapter(recAdapter);
    }

    private void launchDetail(Product product) {
        Store store = dbManager.storeDB.getStoreById(product.getStoreId());
        Intent intent = new Intent(this, ProductDetailsActivity.class);
        intent.putExtra("product_id", product.getProductId());
        intent.putExtra("PRODUCT_NAME", product.getName());
        intent.putExtra("PRODUCT_PRICE", product.getPrice());
        intent.putExtra("PRODUCT_RATING", product.getRatingAverage());
        intent.putExtra("PRODUCT_SOLD", product.getSoldCount());
        intent.putExtra("PRODUCT_CONDITION", product.getCondition());
        intent.putExtra("PRODUCT_WEIGHT", product.getWeightGrams());
        intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
        intent.putExtra("PRODUCT_IMAGE", product.getProductimage());
        if (store != null) {
            intent.putExtra("STORE_NAME", store.getStoreName());
            intent.putExtra("PRODUCT_LOCATION", store.getLocation());
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}
