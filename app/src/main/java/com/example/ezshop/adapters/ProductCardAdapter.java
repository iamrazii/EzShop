package com.example.ezshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.models.Product;
import java.util.ArrayList;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onClick(Product product);
    }

    private final ArrayList<Product> products;
    private final OnProductClickListener listener;

    public ProductCardAdapter(ArrayList<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        android.content.Context ctx = holder.itemView.getContext();

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));
        holder.tvRating.setText(String.format("⭐ %.1f", product.getRatingAverage()));
        holder.tvSold.setText(String.format(" | Sold %d+", product.getSoldCount()));

        if (product.getProductimage() != null && holder.ivImage != null) {
            int imgId = ctx.getResources().getIdentifier(
                product.getProductimage(), "drawable", ctx.getPackageName());
            if (imgId != 0) holder.ivImage.setImageResource(imgId);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(product));
    }

    @Override
    public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvRating, tvSold, tvLocation;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProductImage);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvSold = itemView.findViewById(R.id.tvSold);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }
    }
}
