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
import com.google.firebase.firestore.DocumentSnapshot;

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
        this.allProducts = new ArrayList<>();

        // ASYNC FETCH: Get all products, then tell the adapter to draw the UI
        dbManager.productDB.getAllProducts().addOnSuccessListener(snap -> {
            for (DocumentSnapshot doc : snap) {
                allProducts.add(doc.toObject(Product.class));
            }
            notifyDataSetChanged();
        });
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

        // Wait until allProducts is populated by Firebase
        if (allProducts.isEmpty()) return;

        Product product = null;
        for (Product p : allProducts) {
            if (p.getProductId() != null && p.getProductId().equals(item.getProductId())) {
                product = p;
                break;
            }
        }

        if (product != null) {
            holder.tvName.setText(product.getName());
            holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));

            String imageName = product.getProductimage();
            if (imageName != null) {
                if (imageName.startsWith("content://") || imageName.startsWith("file://") || imageName.startsWith("http")) {
                    holder.ivProductImage.setImageURI(android.net.Uri.parse(imageName));
                } else {
                    int imgId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                    if (imgId != 0) holder.ivProductImage.setImageResource(imgId);
                }
            }

            if (imageName != null && !imageName.isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(imageName)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

        }

        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        // ASYNC UPDATES: Only trigger the listener refresh once Firebase confirms the update!
        holder.tvPlus.setOnClickListener(v -> {
            dbManager.cartItemDB.updateQuantity(item.getCartItemId(), item.getQuantity() + 1)
                    .addOnSuccessListener(aVoid -> listener.onChanged());
        });

        holder.tvMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                dbManager.cartItemDB.updateQuantity(item.getCartItemId(), item.getQuantity() - 1)
                        .addOnSuccessListener(aVoid -> listener.onChanged());
            } else {
                dbManager.cartItemDB.deleteCartItem(item.getCartItemId())
                        .addOnSuccessListener(aVoid -> listener.onChanged());
            }
        });

        holder.ivDelete.setOnClickListener(v -> {
            dbManager.cartItemDB.deleteCartItem(item.getCartItemId())
                    .addOnSuccessListener(aVoid -> listener.onChanged());
        });
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivDelete;
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