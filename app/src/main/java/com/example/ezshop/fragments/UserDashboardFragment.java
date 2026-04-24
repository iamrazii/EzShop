package com.example.ezshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.ezshop.Activities.CategoryResultsActivity;
import com.example.ezshop.Activities.ProductDetailsActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.ProductCardAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;

public class UserDashboardFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private RecyclerView rvBestSellers, rvRecommendations;
    private LinearLayout llCategories;
    private TextView tvUserName, tvBalance;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        tvUserName = view.findViewById(R.id.tvUserName);
        tvBalance = view.findViewById(R.id.tvBalance);
        llCategories = view.findViewById(R.id.llCategories);
        rvBestSellers = view.findViewById(R.id.rvBestSellers);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);

        rvBestSellers.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Tap Profile Pic -> Jump to Settings/Profile Tab (Tab Index 3)
        view.findViewById(R.id.cvUserProfile).setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) viewPager.setCurrentItem(3);
        });

        // Tap Search Bar -> Jump to Search Tab
        view.findViewById(R.id.tvSearchBar).setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) viewPager.setCurrentItem(1);
        });

        // Tap Top Up Button -> Show Dialog
        view.findViewById(R.id.btnTopUp).setOnClickListener(v -> showTopUpDialog());

        loadUserData();
        loadCategories();
        loadProducts();

        return view;
    }

    private void showTopUpDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Top Up Wallet");
        builder.setMessage("Enter the amount you want to add to your balance.");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding / 2, padding, 0);

        TextInputLayout textInputLayout = new TextInputLayout(requireContext());
        textInputLayout.setHint("Amount ($)");
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText input = new TextInputEditText(textInputLayout.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        textInputLayout.addView(input);

        layout.addView(textInputLayout);
        builder.setView(layout);

        builder.setPositiveButton("Top Up", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                try {
                    double topUpAmount = Double.parseDouble(amountStr);
                    if (topUpAmount > 0) {
                        processTopUp(topUpAmount);
                    } else {
                        Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void processTopUp(double topUpAmount) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        User user = dbManager.userDB.getUserById(userId);
        if (user != null) {
            double newBalance = user.getWalletBalance() + topUpAmount;
            user.setWalletBalance(newBalance);

            int rowsUpdated = dbManager.userDB.updateUser(user);
            if (rowsUpdated > 0) {
                Toast.makeText(requireContext(), String.format("Successfully added $%.2f", topUpAmount), Toast.LENGTH_SHORT).show();
                loadUserData();
            } else {
                Toast.makeText(requireContext(), "Top up failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
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
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        llCategories.removeAllViews();
        for (Category cat : categories) {
            View chip = inflater.inflate(R.layout.item_category_chip, llCategories, false);
            TextView tvName = chip.findViewById(R.id.tvCategoryName);
            tvName.setText(cat.getName());
            chip.setOnClickListener(v -> {
                Intent intent = new Intent(requireActivity(), CategoryResultsActivity.class);
                intent.putExtra("CATEGORY_ID", cat.getCategoryId());
                intent.putExtra("CATEGORY_NAME", cat.getName());
                startActivity(intent);
            });
            llCategories.addView(chip);
        }
    }

    private void loadProducts() {
        ArrayList<Product> allProducts = dbManager.productDB.getAllProducts();
        ProductCardAdapter bestAdapter = new ProductCardAdapter(allProducts, this::launchDetail);
        rvBestSellers.setAdapter(bestAdapter);
        ProductCardAdapter recAdapter = new ProductCardAdapter(allProducts, this::launchDetail);
        rvRecommendations.setAdapter(recAdapter);
    }

    private void launchDetail(Product product) {
        Store store = dbManager.storeDB.getStoreById(product.getStoreId());
        Intent intent = new Intent(requireActivity(), ProductDetailsActivity.class);
        intent.putExtra("PRODUCT_ID", product.getProductId()); // Fixed to standard uppercase
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
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}