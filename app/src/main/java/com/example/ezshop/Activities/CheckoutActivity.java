package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.adapters.CheckoutItemAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.CartItem;
import com.example.ezshop.models.Order;
import com.example.ezshop.models.OrderItem;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private EditText etShippingAddress;
    private RadioButton rbEzShopBalance, rbCOD;
    private TextView tvTotal, tvEzpayBalance, tvPromoCode;
    private double totalPrice;
    private User currentUser;
    private ArrayList<CartItem> currentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        totalPrice = getIntent().getDoubleExtra("total_price", 0);

        etShippingAddress = findViewById(R.id.etShippingAddress);
        rbEzShopBalance = findViewById(R.id.rbEzShopBalance);
        rbCOD = findViewById(R.id.rbCOD);
        tvTotal = findViewById(R.id.tvTotal);
        tvEzpayBalance = findViewById(R.id.tvEzpayBalance);
        tvPromoCode = findViewById(R.id.tvPromoCode);
        RecyclerView rvItems = findViewById(R.id.rvCheckoutItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        tvTotal.setText(String.format("$%.2f", totalPrice));

        rbEzShopBalance.setOnClickListener(v -> {
            rbEzShopBalance.setChecked(true);
            rbCOD.setChecked(false);
        });

        rbCOD.setOnClickListener(v -> {
            rbCOD.setChecked(true);
            rbEzShopBalance.setChecked(false);
        });

        String userId = sessionManager.getUserId();

        // Async Loading
        dbManager.userDB.getUserById(userId).addOnSuccessListener(this, doc -> {
            if (doc.exists()) {
                currentUser = doc.toObject(User.class);
                etShippingAddress.setText(currentUser.getDefaultShippingAddress());
                tvEzpayBalance.setText(String.format("Balance $%.3f", currentUser.getWalletBalance()));
            }
        });

        dbManager.cartItemDB.getCartForUser(userId).addOnSuccessListener(this, snap -> {
            currentCart = new ArrayList<>();
            for (DocumentSnapshot doc : snap) currentCart.add(doc.toObject(CartItem.class));
            rvItems.setAdapter(new CheckoutItemAdapter(this, currentCart, dbManager));
        });

        tvPromoCode.setText("EZLHAPPYS (Discount 20%)");

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnOrderNow).setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        if (currentUser == null || currentCart == null || currentCart.isEmpty()) {
            Toast.makeText(this, "Data is still loading or cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = etShippingAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter a shipping address", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = rbEzShopBalance.isChecked() ? "EzShop Balance" : "Cash On Delivery";

        if (paymentMethod.equals("EzShop Balance") && currentUser.getWalletBalance() < totalPrice) {
            Toast.makeText(this, "Insufficient EzShop balance! Please top up.", Toast.LENGTH_LONG).show();
            return;
        }

        // We must fetch products one last time to capture the exact price at checkout
        dbManager.productDB.getAllProducts().addOnSuccessListener(this, prodSnap -> {
            ArrayList<Product> allProducts = new ArrayList<>();
            for (DocumentSnapshot doc : prodSnap) allProducts.add(doc.toObject(Product.class));

            ArrayList<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : currentCart) {
                for (Product p : allProducts) {
                    if (p.getProductId().equals(item.getProductId())) {
                        OrderItem oi = new OrderItem();
                        oi.setProductId(item.getProductId());
                        oi.setQuantity(item.getQuantity());
                        oi.setPriceAtPurchase(p.getPrice());
                        orderItems.add(oi);
                        break;
                    }
                }
            }

            Order order = new Order();
            order.setUserId(currentUser.getUserId());
            order.setShippingAddress(address);
            order.setPaymentMethod(paymentMethod);
            order.setTotalPrice(totalPrice);
            order.setPromoId(null);

            // Using the massive Firebase Batch Writer we built
            dbManager.orderDB.placeOrder(order, orderItems).addOnSuccessListener(this, aVoid -> {

                if (paymentMethod.equals("EzShop Balance")) {
                    currentUser.setWalletBalance(currentUser.getWalletBalance() - totalPrice);
                    dbManager.userDB.updateUser(currentUser);
                }

                // Increment sold counts
                for (OrderItem item : orderItems) {
                    dbManager.productDB.incrementSoldCount(item.getProductId(), item.getQuantity());
                }

                // Clear the Cart Items from Firebase!
                for (CartItem cItem : currentCart) {
                    dbManager.cartItemDB.deleteCartItem(cItem.getCartItemId());
                }

                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, UserHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }).addOnFailureListener(this, e -> Toast.makeText(this, "Failed to place order.", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}