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
        void onProductClick(Product product);
    }

    private ArrayList<Product> productList;
    private OnProductClickListener listener;
    private int displayLimit = 0; // 0 means show all

    // Constructor 1: Show all items
    public ProductCardAdapter(ArrayList<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
        this.displayLimit = 0;
    }

    // Constructor 2: Show limited items
    public ProductCardAdapter(ArrayList<Product> productList, int displayLimit, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
        this.displayLimit = displayLimit;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));

        String imageName = product.getProductimage();
        if (imageName != null) {
            if (imageName.startsWith("content://") || imageName.startsWith("file://") || imageName.startsWith("http")) {
                holder.ivImage.setImageURI(android.net.Uri.parse(imageName));
            } else {
                int imgId = holder.itemView.getContext().getResources().getIdentifier(imageName, "drawable", holder.itemView.getContext().getPackageName());
                if (imgId != 0) holder.ivImage.setImageResource(imgId);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    // THE MAGIC: Restrict the item count if a limit is set!
    @Override
    public int getItemCount() {
        if (displayLimit > 0) {
            return Math.min(productList.size(), displayLimit);
        }
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivProductImage);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}