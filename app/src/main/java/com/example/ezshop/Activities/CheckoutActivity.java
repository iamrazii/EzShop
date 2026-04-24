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
import java.util.ArrayList;

public class CheckoutActivity extends AppCompatActivity {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private EditText etShippingAddress;
    private RadioButton rbEzShopBalance, rbCOD;
    private TextView tvTotal, tvEzpayBalance, tvPromoCode;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This prevents the UI from overlapping with the camera notch
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

        tvTotal.setText(String.format("$%.2f", totalPrice));

        // Make Radio Buttons Mutually Exclusive
        rbEzShopBalance.setOnClickListener(v -> {
            rbEzShopBalance.setChecked(true);
            rbCOD.setChecked(false);
        });

        rbCOD.setOnClickListener(v -> {
            rbCOD.setChecked(true);
            rbEzShopBalance.setChecked(false);
        });

        String userId = sessionManager.getUserId();
        User user = dbManager.userDB.getUserById(userId);
        if (user != null) {
            etShippingAddress.setText(user.getDefaultShippingAddress());
            tvEzpayBalance.setText(String.format("Balance $%.3f", user.getWalletBalance()));
        }

        RecyclerView rvItems = findViewById(R.id.rvCheckoutItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<CartItem> cartItems = dbManager.cartItemDB.getCartForUser(userId);
        rvItems.setAdapter(new CheckoutItemAdapter(this, cartItems, dbManager));

        tvPromoCode.setText("EZLHAPPYS (Discount 20%)");

        findViewById(R.id.ivBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnOrderNow).setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String address = etShippingAddress.getText().toString().trim();
        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter a shipping address", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod;
        if (rbEzShopBalance.isChecked()) paymentMethod = "EzShop Balance";
        else paymentMethod = "Cash On Delivery";

        String userId = sessionManager.getUserId();
        User user = dbManager.userDB.getUserById(userId);

        // CHECK BALANCE ONLY IF PAYING WITH EZSHOP BALANCE
        if (paymentMethod.equals("EzShop Balance") && user.getWalletBalance() < totalPrice) {
            Toast.makeText(this, "Insufficient EzShop balance! Please top up.", Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<CartItem> cartItems = dbManager.cartItemDB.getCartForUser(userId);
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Product> allProducts = dbManager.productDB.getAllProducts();
        ArrayList<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
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
        order.setUserId(userId);
        order.setShippingAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setTotalPrice(totalPrice);
        order.setPromoId(null);

        boolean success = dbManager.orderDB.placeOrder(order, orderItems);
        if (success) {
            // DEDUCT BALANCE AFTER SUCCESSFUL ORDER IF USING EZSHOP BALANCE
            if (paymentMethod.equals("EzShop Balance")) {
                user.setWalletBalance(user.getWalletBalance() - totalPrice);
                dbManager.userDB.updateUser(user);
            }

            Toast.makeText(this, "Order placed successfully! 🎉", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, UserHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}