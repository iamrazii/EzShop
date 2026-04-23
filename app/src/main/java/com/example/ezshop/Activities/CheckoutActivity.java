package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
    private RadioButton rbEzpay, rbBca, rbPaypal;
    private TextView tvTotal, tvEzpayBalance, tvPromoCode;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        sessionManager = new SessionManager(this);
        dbManager = new DBManager(this);
        dbManager.open();

        totalPrice = getIntent().getDoubleExtra("total_price", 0);

        etShippingAddress = findViewById(R.id.etShippingAddress);
        rbEzpay = findViewById(R.id.rbEzpay);
        rbBca = findViewById(R.id.rbBca);
        rbPaypal = findViewById(R.id.rbPaypal);
        tvTotal = findViewById(R.id.tvTotal);
        tvEzpayBalance = findViewById(R.id.tvEzpayBalance);
        tvPromoCode = findViewById(R.id.tvPromoCode);

        tvTotal.setText(String.format("$%.2f", totalPrice));

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
        if (rbEzpay.isChecked()) paymentMethod = "Ezpay";
        else if (rbBca.isChecked()) paymentMethod = "BCA";
        else paymentMethod = "Paypal";

        String userId = sessionManager.getUserId();
        ArrayList<CartItem> cartItems = dbManager.cartItemDB.getCartForUser(userId);
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Product> allProducts = dbManager.productDB.getAllProducts();
        ArrayList<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            for (Product p : allProducts) {
                if (p.getProductId() == item.getProductId()) {
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
