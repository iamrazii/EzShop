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

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<CartItem> cartItems;
    private final ArrayList<Product> allProducts;

    public CheckoutItemAdapter(Context context, ArrayList<CartItem> cartItems, DBManager dbManager) {
        this.context = context;
        this.cartItems = cartItems;
        this.allProducts = dbManager.productDB.getAllProducts();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        for (Product p : allProducts) {
            if (p.getProductId() == item.getProductId()) {
                holder.tvName.setText(p.getName());
                holder.tvPrice.setText(String.format("$%.2f", p.getPrice()));
                String imageName = p.getProductimage();
                if (imageName != null) {
                    int imgId = context.getResources().getIdentifier(
                        imageName, "drawable", context.getPackageName());
                    if (imgId != 0) holder.ivImage.setImageResource(imgId);
                }
                break;
            }
        }
        holder.tvQty.setText("x" + item.getQuantity());
    }

    @Override
    public int getItemCount() { return cartItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvQty;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProductImage);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQty = itemView.findViewById(R.id.tvQty);
        }
    }
}
