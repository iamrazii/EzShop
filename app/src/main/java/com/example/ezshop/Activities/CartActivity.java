package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.adapters.CartAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.CartItem;
import com.example.ezshop.models.Product;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private RecyclerView rvCartItems;
    private TextView tvTotal;
    private ArrayList<CartItem> cartItems;
    private double totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        rvCartItems = findViewById(R.id.rvCartItems);
        tvTotal = findViewById(R.id.tvTotal);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));

        loadCart();

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CheckoutActivity.class);
            intent.putExtra("total_price", totalPrice);
            startActivity(intent);
        });
    }

    private void loadCart() {
        String userId = sessionManager.getUserId();

        dbManager.cartItemDB.getCartForUser(userId).addOnSuccessListener(this, cartSnap -> {
            cartItems = new ArrayList<>();
            for (DocumentSnapshot doc : cartSnap) cartItems.add(doc.toObject(CartItem.class));

            // Fetch products to calculate live total
            dbManager.productDB.getAllProducts().addOnSuccessListener(this, prodSnap -> {
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

                CartAdapter adapter = new CartAdapter(this, cartItems, dbManager, userId, this::loadCart);
                rvCartItems.setAdapter(adapter);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}