package com.example.ezshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.Activities.EditProductActivity;
import com.example.ezshop.Activities.ProductDetailsActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.SellerProductAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class SellerProductsFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private RecyclerView rvSellerProducts;
    private SellerProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_products, container, false);

        rvSellerProducts = view.findViewById(R.id.rvSellerProducts);
        rvSellerProducts.setLayoutManager(new LinearLayoutManager(requireContext()));

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        loadSellerProducts();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSellerProducts();
    }

    private void loadSellerProducts() {
        String storeId = sessionManager.getStoreId();
        if (storeId == null) return;

        dbManager.storeDB.getStoreById(storeId).addOnSuccessListener(storeSnap -> {
            if (!isAdded() || getContext() == null) return;
            Store store = storeSnap.toObject(Store.class);

            dbManager.productDB.getProductsForSeller(storeId).addOnSuccessListener(prodSnap -> {
                if (!isAdded() || getContext() == null) return;

                ArrayList<Product> products = new ArrayList<>();
                for (DocumentSnapshot doc : prodSnap) products.add(doc.toObject(Product.class));

                adapter = new SellerProductAdapter(requireContext(), products,
                        product -> {
                            Intent intent = new Intent(requireContext(), ProductDetailsActivity.class);
                            intent.putExtra("product_id", product.getProductId());
                            intent.putExtra("PRODUCT_NAME", product.getName());
                            intent.putExtra("PRODUCT_PRICE", product.getPrice());
                            intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
                            intent.putExtra("PRODUCT_CONDITION", product.getCondition());
                            intent.putExtra("PRODUCT_WEIGHT", product.getWeightGrams());
                            intent.putExtra("PRODUCT_IMAGE", product.getProductimage());
                            intent.putExtra("PRODUCT_SOLD", product.getSoldCount());
                            intent.putExtra("PRODUCT_RATING", product.getRatingAverage());
                            if (store != null) {
                                intent.putExtra("STORE_NAME", store.getStoreName());
                                intent.putExtra("PRODUCT_LOCATION", store.getLocation());
                            }
                            startActivity(intent);
                        },
                        product -> {
                            Intent intent = new Intent(requireContext(), EditProductActivity.class);
                            intent.putExtra("PRODUCT_ID", product.getProductId());
                            intent.putExtra("PRODUCT_NAME", product.getName());
                            intent.putExtra("PRODUCT_PRICE", product.getPrice());
                            intent.putExtra("PRODUCT_DESC", product.getDescription());
                            startActivity(intent);
                        },
                        product -> {
                            dbManager.productDB.deleteProduct(product.getProductId()).addOnSuccessListener(aVoid -> {
                                if (!isAdded() || getContext() == null) return;
                                Toast.makeText(requireContext(), "Product removed", Toast.LENGTH_SHORT).show();
                                loadSellerProducts();
                            }).addOnFailureListener(e -> {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                );
                rvSellerProducts.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}