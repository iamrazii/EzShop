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
import java.util.ArrayList;
import java.util.Collections;

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
        rvTopSellers = view.findViewById(R.id.rvTopSellers);

        // Add Product Button
        view.findViewById(R.id.btnDashAddProduct).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddProductActivity.class));
        });

        view.findViewById(R.id.btnSellerDashInbox).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(((ViewGroup) requireView().getParent()).getId(), new InboxFragment())
                    .addToBackStack(null)
                    .commit();
        });

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
        String storeId = sessionManager.getStoreId();
        String userId = sessionManager.getUserId();

        if (storeId == null || userId == null) return;

        dbManager.userDB.getUserById(userId).addOnSuccessListener(documentSnapshot -> {
            if (!isAdded() || getContext() == null) return;
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                tvWelcomeName.setText("Welcome, " + (user != null ? user.getName() : "Seller") + " 👋");
            }
        });

        dbManager.storeDB.getStoreById(storeId).addOnSuccessListener(documentSnapshot -> {
            if (!isAdded() || getContext() == null) return;
            if (documentSnapshot.exists()) {
                Store store = documentSnapshot.toObject(Store.class);
                if (store != null) {
                    tvStoreName.setText(store.getStoreName());
                    tvStoreRating.setText(String.format("%.1f", store.getRating()));
                }
            }
        });

        dbManager.productDB.getProductsForSeller(storeId).addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded() || getContext() == null) return;
            ArrayList<Product> products = new ArrayList<>();
            int totalSold = 0;

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                if (p != null) {
                    totalSold += p.getSoldCount();
                    products.add(p);
                }
            }

            tvTotalProducts.setText(String.valueOf(products.size()));
            tvTotalSold.setText(String.valueOf(totalSold));
            setupTopSellers(products);
        });
    }

    private void setupTopSellers(ArrayList<Product> products) {
        if (products.isEmpty() || getContext() == null) return;

        Collections.sort(products, (p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()));

        ArrayList<Product> top5 = new ArrayList<>();
        for (int i = 0; i < Math.min(5, products.size()); i++) {
            top5.add(products.get(i));
        }

        SellerProductAdapter topSellerAdapter = new SellerProductAdapter(requireContext(), top5, null, null, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rvTopSellers.setLayoutManager(layoutManager);
        rvTopSellers.setAdapter(topSellerAdapter);
    }
}