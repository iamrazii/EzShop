package com.example.ezshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.CartItem;
import com.example.ezshop.models.Product;
import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartChangedListener { void onChanged(); }

    private final Context context;
    private final ArrayList<CartItem> cartItems;
    private final DBManager dbManager;
    private final String userId;
    private final OnCartChangedListener listener;
    private final ArrayList<Product> allProducts;

    public CartAdapter(Context context, ArrayList<CartItem> cartItems,
                       DBManager dbManager, String userId, OnCartChangedListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.dbManager = dbManager;
        this.userId = userId;
        this.listener = listener;
        this.allProducts = dbManager.productDB.getAllProducts();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        Product product = null;
        for (Product p : allProducts) {
            // FIXED: Used .equals() instead of == for String comparison
            if (p.getProductId().equals(item.getProductId())) { product = p; break; }
        }

        if (product != null) {
            holder.tvName.setText(product.getName());
            holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));

            // FIXED: Handle both local camera photos and default drawables
            String imageName = product.getProductimage();
            if (imageName != null) {
                if (imageName.startsWith("content://") || imageName.startsWith("file://")) {
                    holder.ivProductImage.setImageURI(android.net.Uri.parse(imageName));
                } else {
                    int imgId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                    if (imgId != 0) holder.ivProductImage.setImageResource(imgId);
                }
            }
        }

        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        holder.tvPlus.setOnClickListener(v -> {
            dbManager.cartItemDB.updateQuantity(item.getCartItemId(), item.getQuantity() + 1);
            listener.onChanged();
        });
        holder.tvMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                dbManager.cartItemDB.updateQuantity(item.getCartItemId(), item.getQuantity() - 1);
            } else {
                dbManager.cartItemDB.deleteCartItem(item.getCartItemId());
            }
            listener.onChanged();
        });
        holder.ivDelete.setOnClickListener(v -> {
            dbManager.cartItemDB.deleteCartItem(item.getCartItemId());
            listener.onChanged();
        });
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivDelete;
        // FIXED: Changed tvPlus and tvMinus to TextView to match item_cart.xml
        TextView tvName, tvProductVariant, tvPrice, tvQty, tvPlus, tvMinus;

        ViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvPlus = itemView.findViewById(R.id.ivPlus);
            tvMinus = itemView.findViewById(R.id.ivMinus);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvProductVariant = itemView.findViewById(R.id.tvProductVariant);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQty = itemView.findViewById(R.id.tvQty);
        }
    }
}