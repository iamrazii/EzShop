package com.example.ezshop.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Order;
import com.example.ezshop.models.OrderItem;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Review;
import com.example.ezshop.models.Store;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MyOrdersAdapter extends RecyclerView.Adapter<MyOrdersAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Order> orders;
    private DBManager dbManager;
    private String userId;
    private ArrayList<Product> allProducts;

    // HashSet to store product IDs the user has already reviewed
    private HashSet<String> reviewedProductIds;

    public MyOrdersAdapter(Context context, ArrayList<Order> orders, DBManager dbManager, String userId) {
        this.context = context;
        this.orders = orders;
        this.dbManager = dbManager;
        this.userId = userId;
        this.allProducts = new ArrayList<>();
        this.reviewedProductIds = new HashSet<>();

        // ASYNC FETCH: Grab products to match names and images
        dbManager.productDB.getAllProducts().addOnSuccessListener(snap -> {
            for (DocumentSnapshot doc : snap) {
                allProducts.add(doc.toObject(Product.class));
            }
            notifyDataSetChanged();
        });

        // ASYNC FETCH: Grab all reviews submitted by this specific user to hide buttons
        FirebaseFirestore.getInstance().collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    for (DocumentSnapshot doc : snap) {
                        String reviewedProductId = doc.getString("productId");
                        if (reviewedProductId != null) {
                            reviewedProductIds.add(reviewedProductId);
                        }
                    }
                    notifyDataSetChanged(); // Refresh the list to hide buttons
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        holder.tvOrderDate.setText("Ordered on: " + sdf.format(new Date(order.getCreatedAt())));
        holder.tvOrderTotal.setText(String.format(Locale.getDefault(), "$%.2f", order.getTotalPrice()));

        long now = System.currentTimeMillis();
        long diffMillis = now - order.getCreatedAt();
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

        String status;
        if (days == 0) status = "Confirmed";
        else if (days == 1) status = "Shipped";
        else if (days == 2) status = "In Transit";
        else status = "Delivered";

        holder.tvOrderStatus.setText(status);

        if (allProducts.isEmpty()) return; // Wait for products to load

        dbManager.orderDB.getOrderItems(order.getOrderId()).addOnSuccessListener(snap -> {

            holder.llOrderItems.removeAllViews();

            LayoutInflater inflater = LayoutInflater.from(context);

            for (DocumentSnapshot doc : snap) {
                OrderItem item = doc.toObject(OrderItem.class);
                if (item == null) continue;

                View productView = inflater.inflate(R.layout.item_my_order_product, holder.llOrderItems, false);

                ImageView ivProductImage = productView.findViewById(R.id.ivProductImage);
                TextView tvName = productView.findViewById(R.id.tvProductName);
                TextView tvPrice = productView.findViewById(R.id.tvProductPrice);
                TextView tvQty = productView.findViewById(R.id.tvProductQty);
                MaterialButton btnReview = productView.findViewById(R.id.btnAddReview);

                Product matchedProduct = null;
                for (Product p : allProducts) {
                    if (p.getProductId() != null && p.getProductId().equals(item.getProductId())) {
                        matchedProduct = p;
                        break;
                    }
                }

                if (matchedProduct != null) {
                    tvName.setText(matchedProduct.getName());
                    tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getPriceAtPurchase()));
                    tvQty.setText("x" + item.getQuantity());

                    String imgUrl = matchedProduct.getProductimage();

                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        // Let Glide handle URLs and URIs
                        com.bumptech.glide.Glide.with(context)
                                .load(imgUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(ivProductImage);
                    } else {
                        ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }

                    Product finalMatchedProduct = matchedProduct;

                    if (reviewedProductIds.contains(finalMatchedProduct.getProductId())) {
                        btnReview.setVisibility(View.GONE); // Hide the button
                    } else {
                        btnReview.setVisibility(View.VISIBLE); // Show the button
                        btnReview.setOnClickListener(v -> showReviewDialog(finalMatchedProduct, btnReview));
                    }
                }
                holder.llOrderItems.addView(productView);
            }
        });
    }

    private void showReviewDialog(Product product, MaterialButton btnReview) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("Write a Review");
        builder.setMessage("Rate your purchase of " + product.getName());

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 0);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        RatingBar ratingBar = new RatingBar(context);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5.0f);

        ratingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
        ratingBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        ratingBar.setSecondaryProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));

        LinearLayout.LayoutParams rbParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rbParams.setMargins(0, 0, 0, 30);
        ratingBar.setLayoutParams(rbParams);
        layout.addView(ratingBar);

        EditText etComment = new EditText(context);
        etComment.setHint("Share your experience...");
        etComment.setLines(3);
        etComment.setGravity(Gravity.TOP | Gravity.START);
        etComment.setBackgroundResource(android.R.drawable.edit_text);
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        etComment.setLayoutParams(etParams);
        layout.addView(etComment);

        builder.setView(layout);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            String comment = etComment.getText().toString().trim();
            int rating = (int) ratingBar.getRating();

            if (rating == 0) {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            Review review = new Review();
            review.setProductId(product.getProductId());
            review.setUserId(userId);
            review.setRating(rating);
            review.setComment(comment.isEmpty() ? "No comment" : comment);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            review.setReviewDate(sdf.format(new Date()));

            dbManager.reviewDB.addReview(review).addOnSuccessListener(aVoid -> {

                double currentProdAvg = product.getRatingAverage();
                int prodEstimatedN = 10; // Product weight factor
                double newProdAvg;

                if (currentProdAvg == 0.0) {
                    newProdAvg = rating;
                } else {
                    newProdAvg = ((currentProdAvg * prodEstimatedN) + rating) / (prodEstimatedN + 1);
                }

                product.setRatingAverage(newProdAvg);

                dbManager.productDB.updateProduct(product).addOnSuccessListener(aVoid2 -> {

                    reviewedProductIds.add(product.getProductId());
                    btnReview.setVisibility(View.GONE);

                    String storeId = product.getStoreId();

                    if (storeId != null && !storeId.isEmpty()) {
                        dbManager.storeDB.getStoreById(storeId).addOnSuccessListener(storeSnap -> {
                            Store store = storeSnap.toObject(Store.class);

                            if (store != null) {
                                double currentStoreAvg = store.getRating();
                                int storeEstimatedN = 50;
                                double newStoreAvg;

                                if (currentStoreAvg == 0.0) {
                                    newStoreAvg = rating;
                                } else {
                                    newStoreAvg = ((currentStoreAvg * storeEstimatedN) + rating) / (storeEstimatedN + 1);
                                }

                                store.setRating(newStoreAvg);

                                // Update Store in DB
                                dbManager.storeDB.updateStore(store).addOnSuccessListener(aVoid3 -> {
                                    // Success for everything!
                                    Toast.makeText(context, "Review submitted! ⭐", Toast.LENGTH_SHORT).show();
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(context, "Review saved! (Store sync delayed)", Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                Toast.makeText(context, "Review submitted! ⭐", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Review submitted! ⭐", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(context, "Review submitted! ⭐", Toast.LENGTH_SHORT).show();
                    }

                }).addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update product rating.", Toast.LENGTH_SHORT).show();
                });

            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to submit review.", Toast.LENGTH_SHORT).show();
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderDate, tvOrderStatus, tvOrderTotal;
        LinearLayout llOrderItems;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            llOrderItems = itemView.findViewById(R.id.llOrderItems);
        }
    }
}