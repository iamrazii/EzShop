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
import com.example.ezshop.models.Product;
import java.util.ArrayList;

public class ExploreProductRVAdapter extends RecyclerView.Adapter<ExploreProductRVAdapter.ProductViewHolder> {


    private Context context;
    private ArrayList<Product> productList;
    private OnProductClickListener listener;


    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ExploreProductRVAdapter(Context context, ArrayList<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your custom single-item layout (item_product.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.trending_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product currentProduct = productList.get(position);

        holder.tvName.setText(currentProduct.getName());
        holder.tvPrice.setText("$ " + currentProduct.getPrice());
        holder.tvRating.setText(String.valueOf(currentProduct.getRatingAverage()));
        holder.tvSold.setText("Sold " + currentProduct.getSoldCount() + "+");


        String iconName = currentProduct.getProductimage();

        int drawbableid = context.getResources().getIdentifier(
                iconName,
                "drawable",
                context.getPackageName()
        );

        holder.ivProduct.setImageResource(drawbableid);
        // 3. TRIGGER THE CLICK ACTION
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(currentProduct);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvRating, tvSold;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Link these IDs to your item_product.xml
            ivProduct = itemView.findViewById(R.id.trndivProductImage);
            tvName = itemView.findViewById(R.id.trndtvProductName);
            tvPrice = itemView.findViewById(R.id.trndtvProductPrice);
            tvRating = itemView.findViewById(R.id.trndtvProductRating);
            tvSold = itemView.findViewById(R.id.trndtvProductSold);
        }
    }
}
