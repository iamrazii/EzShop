package com.example.ezshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.models.Product;
import java.util.ArrayList;

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.ViewHolder> {

    // Interfaces for the 3 actions a seller can take on a product
    public interface OnProductClickListener { void onProductClick(Product product); }
    public interface OnEditClickListener { void onEdit(Product product); }
    public interface OnDeleteClickListener { void onDelete(Product product); }

    private final Context context;
    private final ArrayList<Product> products;
    private final OnProductClickListener productClickListener;
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    public SellerProductAdapter(Context context, ArrayList<Product> products,
                                OnProductClickListener productClickListener,
                                OnEditClickListener editListener,
                                OnDeleteClickListener deleteListener) {
        this.context = context;
        this.products = products;
        this.productClickListener = productClickListener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seller_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("$%.2f", product.getPrice()));
        holder.tvSoldCount.setText(String.format("Sold %d", product.getSoldCount()));
        holder.tvRating.setText(String.format("⭐ %.1f", product.getRatingAverage()));

        // Set the image if it exists
        String imageName = product.getProductimage();
        if (imageName != null) {
            if (imageName.startsWith("content://") || imageName.startsWith("file://")) {
                // Load actual photo from gallery or camera
                holder.ivProductImage.setImageURI(android.net.Uri.parse(imageName));
            } else {
                // Fallback to the default drawables (like "macbook")
                int imgId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
                if (imgId != 0) holder.ivProductImage.setImageResource(imgId);
            }
        }

        // ADD LOGIC HERE TO HANDLE INVENTORY MODE vs DASHBOARD MODE
        if (productClickListener != null && editListener != null && deleteListener != null) {
            // --- INVENTORY MODE ---
            // Show buttons and make the card tappable
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.itemView.setClickable(true);

            // 1. Click the entire card to open ProductDetailsActivity
            holder.itemView.setOnClickListener(v -> productClickListener.onProductClick(product));

            // 2. Click the Edit Pencil icon
            holder.ivEdit.setOnClickListener(v -> editListener.onEdit(product));

            // 3. Click the Delete Trash icon
            holder.ivDelete.setOnClickListener(v ->
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Product")
                            .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                int pos = holder.getAdapterPosition();
                                if (pos != RecyclerView.NO_POSITION) {
                                    products.remove(pos);
                                    notifyItemRemoved(pos);
                                    notifyItemRangeChanged(pos, products.size());
                                }
                                deleteListener.onDelete(product);
                            })
                            .setNegativeButton("Cancel", null)
                            .show()
            );
        } else {
            // --- DASHBOARD MODE (Top 5) ---
            // Hide buttons and disable tapping completely
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setClickable(false);
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, ivEdit, ivDelete;
        TextView tvName, tvPrice, tvSoldCount, tvRating;

        ViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvSoldCount = itemView.findViewById(R.id.tvSoldCount);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}