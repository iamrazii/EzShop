package com.example.ezshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.Activities.CheckoutActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.CartAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.CartItem;
import com.example.ezshop.models.Product;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class CartFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private RecyclerView rvCartItems;
    private TextView tvTotal;
    private ArrayList<CartItem> cartItems;
    private double totalPrice = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvTotal = view.findViewById(R.id.tvTotal);
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadCart();

        view.findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireActivity(), CheckoutActivity.class);
            intent.putExtra("total_price", totalPrice);
            startActivity(intent);
        });

        return view;
    }

    private void loadCart() {
        String userId = sessionManager.getUserId();

        dbManager.cartItemDB.getCartForUser(userId).addOnSuccessListener(cartSnap -> {
            if (!isAdded() || getContext() == null) return;
            cartItems = new ArrayList<>();
            for (DocumentSnapshot doc : cartSnap) cartItems.add(doc.toObject(CartItem.class));

            dbManager.productDB.getAllProducts().addOnSuccessListener(prodSnap -> {
                if (!isAdded() || getContext() == null) return;

                ArrayList<Product> allProducts = new ArrayList<>();
                for (DocumentSnapshot doc : prodSnap) allProducts.add(doc.toObject(Product.class));

                totalPrice = 0;
                for (CartItem item : cartItems) {
                    for (Product p : allProducts) {
                        if (p.getProductId() != null && p.getProductId().equals(item.getProductId())) {
                            totalPrice += p.getPrice() * item.getQuantity();
                            break;
                        }
                    }
                }
                tvTotal.setText(String.format("$%.2f", totalPrice));

                CartAdapter adapter = new CartAdapter(requireContext(), cartItems, dbManager, userId, this::loadCart);
                rvCartItems.setAdapter(adapter);
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }
}