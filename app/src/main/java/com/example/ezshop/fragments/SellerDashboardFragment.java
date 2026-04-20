package com.example.ezshop.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.adapters.SellerProductAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import java.util.ArrayList;

public class SellerDashboardFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private TextView tvWelcomeName, tvStoreName, tvTotalProducts, tvTotalSold, tvStoreRating;
    private RecyclerView rvTopSellers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_dashboard, container, false);

        tvWelcomeName = view.findViewById(R.id.tvWelcomeName);
        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvTotalSold = view.findViewById(R.id.tvTotalSold);
        tvStoreRating = view.findViewById(R.id.tvStoreRating);
        rvTopSellers = view.findViewById(R.id.rvTopSellers); // Initialize the new RecyclerView

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        loadDashboardData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        int storeId = sessionManager.getStoreId();
        int userId = sessionManager.getUserId();
        if (storeId == -1 || userId == -1) return;

        // Fetch User to say hello!
        User user = dbManager.userDB.getUserById(userId);
        if (user != null) {
            tvWelcomeName.setText("Welcome, " + user.getName() + " 👋");
        }

        // Fetch Store
        Store store = dbManager.storeDB.getStoreById(storeId);
        if (store != null) {
            tvStoreName.setText(store.getStoreName());
            tvStoreRating.setText(String.format("%.1f", store.getRating()));
        }

        // Fetch Inventory Stats
        ArrayList<Product> products = dbManager.productDB.getProductsForSeller(userId);
        tvTotalProducts.setText(String.valueOf(products.size()));

        int totalSold = 0;
        for (Product p : products) totalSold += p.getSoldCount();
        tvTotalSold.setText(String.valueOf(totalSold));

        // Load the Top Sellers
        setupTopSellers(storeId);
    }

    private void setupTopSellers(int storeId) {
        // Get the top 5 products specifically for this store
        ArrayList<Product> topProducts = dbManager.productDB.getTopSellingProducts(storeId);

        // Pass null for the listener so the cards are display-only
// Pass null for the click, edit, and delete listeners so it stays read-only
        SellerProductAdapter topSellerAdapter = new SellerProductAdapter(requireContext(), topProducts, null, null, null);
        // Make it scroll horizontally
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvTopSellers.setLayoutManager(layoutManager);
        rvTopSellers.setAdapter(topSellerAdapter);
    }
}