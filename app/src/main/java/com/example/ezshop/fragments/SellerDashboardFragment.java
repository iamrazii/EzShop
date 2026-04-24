package com.example.ezshop.fragments;

import android.content.Intent;
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
import com.example.ezshop.Activities.AddProductActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.SellerProductAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Collections;

public class SellerDashboardFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private FirebaseFirestore cloudDb;
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
        rvTopSellers = view.findViewById(R.id.rvTopSellers);

        view.findViewById(R.id.btnDashAddProduct).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddProductActivity.class));
        });

        sessionManager = new SessionManager(requireContext());
        cloudDb = FirebaseFirestore.getInstance();

        // Keep local DB open for offline fallback if needed
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
        String storeId = sessionManager.getStoreId();
        String userId = sessionManager.getUserId();

        if (storeId == null || userId == null) return;

        // 1. Fetch User Data from Firebase
        cloudDb.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcomeName.setText("Welcome, " + (name != null ? name : "Seller") + " 👋");
                    }
                });

        // 2. Fetch Store Data from Firebase
        cloudDb.collection("stores").document(storeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvStoreName.setText(documentSnapshot.getString("storeName"));
                        Double rating = documentSnapshot.getDouble("rating");
                        tvStoreRating.setText(String.format("%.1f", rating != null ? rating : 0.0));
                    }
                });

        // 3. Fetch Products for this store to calculate stats
        cloudDb.collection("products")
                .whereEqualTo("storeId", storeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Product> products = new ArrayList<>();
                    int totalSold = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            // Ensure the ID from the document is set in the object
                            p.setProductId(doc.getId());
                            totalSold += p.getSoldCount();
                            products.add(p);
                        }
                    }

                    tvTotalProducts.setText(String.valueOf(products.size()));
                    tvTotalSold.setText(String.valueOf(totalSold));

                    // 4. Update the Top Sellers List
                    setupTopSellers(products);
                });
    }

    private void setupTopSellers(ArrayList<Product> products) {
        if (products.isEmpty()) return;

        // Sort by sold count descending
        Collections.sort(products, (p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()));

        // Take top 5
        ArrayList<Product> top5 = new ArrayList<>();
        for (int i = 0; i < Math.min(5, products.size()); i++) {
            top5.add(products.get(i));
        }

        SellerProductAdapter topSellerAdapter = new SellerProductAdapter(requireContext(), top5, null, null, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvTopSellers.setLayoutManager(layoutManager);
        rvTopSellers.setAdapter(topSellerAdapter);
    }
}