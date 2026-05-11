package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
    private EditText etShippingAddress, etPromoCode;
    private RadioButton rbEzShopBalance, rbCOD;
    private TextView tvTotal, tvEzpayBalance;
    private Button btnApplyPromo;

    // Tracking Prices and Promos
    private double originalPrice;
    private double totalPrice;
    private double discountAmount = 0;
    private String appliedPromoCode = null;

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

        // Save original price and initialize total price
        originalPrice = getIntent().getDoubleExtra("total_price", 0);
        totalPrice = originalPrice;

        etShippingAddress = findViewById(R.id.etShippingAddress);
        rbEzShopBalance = findViewById(R.id.rbEzShopBalance);
        rbCOD = findViewById(R.id.rbCOD);
        tvTotal = findViewById(R.id.tvTotal);
        tvEzpayBalance = findViewById(R.id.tvEzpayBalance);
        etPromoCode = findViewById(R.id.etPromoCode);
        btnApplyPromo = findViewById(R.id.btnApplyPromo);

        RecyclerView rvItems = findViewById(R.id.rvCheckoutItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        updateTotalDisplay();

        // Promo Button Listener
        btnApplyPromo.setOnClickListener(v -> applyPromoLogic());

        rbEzShopBalance.setOnClickListener(v -> {
            rbEzShopBalance.setChecked(true);
            rbCOD.setChecked(false);
        });

        rbCOD.setOnClickListener(v -> {
            rbCOD.setChecked(true);
            rbEzShopBalance.setChecked(false);
        });

        String userId = sessionManager.getUserId();

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

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnOrderNow).setOnClickListener(v -> placeOrder());
    }

    private void applyPromoLogic() {
        String code = etPromoCode.getText().toString().trim().toLowerCase();

        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter a promo code", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset previous discount before applying new one
        discountAmount = 0;
        appliedPromoCode = null;

        // Hardcoded Promos with Minimum Spend Rules
        switch (code) {
            case "ezshop10":
                if (originalPrice >= 50) applyDiscount(10, code);
                else showMinSpendError(50);
                break;
            case "ezshop20":
                if (originalPrice >= 100) applyDiscount(20, code);
                else showMinSpendError(100);
                break;
            case "ezshop50":
                if (originalPrice >= 200) applyDiscount(50, code);
                else showMinSpendError(200);
                break;
            case "ezshop100":
                if (originalPrice >= 400) applyDiscount(100, code);
                else showMinSpendError(400);
                break;
            case "ezshop200":
                if (originalPrice >= 800) applyDiscount(200, code);
                else showMinSpendError(800);
                break;
            default:
                Toast.makeText(this, "Invalid Promo Code", Toast.LENGTH_SHORT).show();
                updateTotalDisplay(); // Resets back to original if they typed a bad code
                break;
        }
    }

    private void applyDiscount(double discount, String code) {
        discountAmount = discount;
        appliedPromoCode = code;
        Toast.makeText(this, "Promo Applied! You saved $" + discount, Toast.LENGTH_SHORT).show();
        updateTotalDisplay();
    }

    private void showMinSpendError(double minSpend) {
        Toast.makeText(this, "Cart must be over $" + minSpend + " to use this promo", Toast.LENGTH_LONG).show();
        updateTotalDisplay();
    }

    private void updateTotalDisplay() {
        totalPrice = originalPrice - discountAmount;
        // Ensure total price doesn't drop below zero
        if (totalPrice < 0) totalPrice = 0;

        if (discountAmount > 0) {
            tvTotal.setText(String.format("$%.2f (Saved $%.2f)", totalPrice, discountAmount));
        } else {
            tvTotal.setText(String.format("$%.2f", totalPrice));
        }
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
            order.setPromoId(appliedPromoCode);

            dbManager.orderDB.placeOrder(order, orderItems).addOnSuccessListener(this, aVoid -> {

                if (paymentMethod.equals("EzShop Balance")) {
                    currentUser.setWalletBalance(currentUser.getWalletBalance() - totalPrice);
                    dbManager.userDB.updateUser(currentUser);
                }

                for (OrderItem item : orderItems) {
                    dbManager.productDB.incrementSoldCount(item.getProductId(), item.getQuantity());
                }

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