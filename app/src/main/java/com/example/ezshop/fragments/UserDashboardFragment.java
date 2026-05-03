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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class UserDashboardFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private RecyclerView rvBestSellers, rvRecommendations;
    private LinearLayout llCategories;
    private TextView tvUserName, tvBalance, tvSeeAllBest, tvSeeAllRecommended;

    // State Tracking
    private boolean isBestExpanded = false;
    private boolean isRecExpanded = false;
    private ArrayList<Product> cachedProducts = new ArrayList<>();
    private ArrayList<Product> cachedRecommendations = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_dashboard, container, false);

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        tvUserName = view.findViewById(R.id.tvUserName);
        tvBalance = view.findViewById(R.id.tvBalance);
        tvSeeAllBest = view.findViewById(R.id.tvSeeAllBest);
        tvSeeAllRecommended = view.findViewById(R.id.tvSeeAllRec);
        llCategories = view.findViewById(R.id.llCategories);
        rvBestSellers = view.findViewById(R.id.rvBestSellers);
        rvRecommendations = view.findViewById(R.id.rvRecommendations);

        rvBestSellers.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (tvSeeAllRecommended != null) {
            tvSeeAllRecommended.setOnClickListener(v -> {
                isRecExpanded = !isRecExpanded;
                tvSeeAllRecommended.setText(isRecExpanded ? "See Less" : "See All");
                updateRecommendationsUI();
            });
        }

        if (tvSeeAllBest != null) {
            tvSeeAllBest.setOnClickListener(v -> {
                isBestExpanded = !isBestExpanded;
                tvSeeAllBest.setText(isBestExpanded ? "See Less" : "See All");
                updateBestSellersUI();
            });
        }

        view.findViewById(R.id.cvUserProfile).setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) viewPager.setCurrentItem(3);
        });

        view.findViewById(R.id.tvSearchBar).setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) viewPager.setCurrentItem(1);
        });

        view.findViewById(R.id.btnTopUp).setOnClickListener(v -> showTopUpDialog());

        loadUserData();
        loadCategories();
        loadProducts();

        return view;
    }

    private void updateBestSellersUI() {
        int limit = isBestExpanded ? 6 : 3;
        ProductCardAdapter bestAdapter = new ProductCardAdapter(cachedProducts, limit, this::launchDetail);
        rvBestSellers.setAdapter(bestAdapter);
    }

    private void updateRecommendationsUI() {
        int limit = isRecExpanded ? 6 : 3;
        ProductCardAdapter recAdapter = new ProductCardAdapter(cachedRecommendations, limit, this::launchDetail);
        rvRecommendations.setAdapter(recAdapter);
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
                    if (topUpAmount > 0) processTopUp(topUpAmount);
                    else Toast.makeText(requireContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
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

        dbManager.userDB.getUserById(userId).addOnSuccessListener(doc -> {
            if (!isAdded() || getContext() == null) return;
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    user.setWalletBalance(user.getWalletBalance() + topUpAmount);
                    dbManager.userDB.updateUser(user).addOnSuccessListener(aVoid -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(requireContext(), String.format("Successfully added $%.2f", topUpAmount), Toast.LENGTH_SHORT).show();
                        loadUserData();
                    });
                }
            }
        });
    }

    private void loadUserData() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        dbManager.userDB.getUserById(userId).addOnSuccessListener(doc -> {
            if (!isAdded() || getContext() == null) return;
            if (doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    tvUserName.setText(user.getName());
                    tvBalance.setText(String.format("$%.3f", user.getWalletBalance()));
                }
            }
        });
    }
    private void loadCategories() {
        dbManager.categoryDB.getAllCategories().addOnSuccessListener(snap -> {
            if (!isAdded() || getContext() == null) return;
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            llCategories.removeAllViews();

            for (DocumentSnapshot doc : snap) {
                Category cat = doc.toObject(Category.class);
                if (cat == null) continue;

                View categoryView = inflater.inflate(R.layout.item_category_chip, llCategories, false);

                TextView tvName = categoryView.findViewById(R.id.tvCategoryName);

                if (tvName != null) {
                    tvName.setText(cat.getName());
                }

                categoryView.setOnClickListener(v -> {
                    Intent intent = new Intent(requireActivity(), CategoryResultsActivity.class);
                    intent.putExtra("CATEGORY_ID", cat.getCategoryId());
                    intent.putExtra("CATEGORY_NAME", cat.getName());
                    startActivity(intent);
                });

                llCategories.addView(categoryView);
            }
        });
    }
    private void loadProducts() {
        dbManager.productDB.getAllProducts().addOnSuccessListener(snap -> {
            if (!isAdded() || getContext() == null) return;

            cachedProducts.clear();
            cachedRecommendations.clear();

            for (DocumentSnapshot doc : snap) {
                Product p = doc.toObject(Product.class);
                cachedProducts.add(p);
                cachedRecommendations.add(p);
            }

            Collections.shuffle(cachedRecommendations);

            updateBestSellersUI();
            updateRecommendationsUI();
        });
    }

    private void launchDetail(Product product) {
        dbManager.storeDB.getStoreById(product.getStoreId()).addOnSuccessListener(storeSnap -> {
            if (!isAdded() || getContext() == null) return;
            Store store = storeSnap.toObject(Store.class);

            Intent intent = new Intent(requireActivity(), ProductDetailsActivity.class);
            intent.putExtra("PRODUCT_ID", product.getProductId());
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
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadProducts();
    }
}