package com.example.ezshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.Activities.AddProductActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.SellerProductAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SellerDashboardFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private TextView tvWelcomeName, tvStoreName, tvTotalProducts, tvTotalSold, tvStoreRating;
    private TextView tvAiForecast; // ✨ NEW
    private RecyclerView rvTopSellers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_dashboard, container, false);

        tvWelcomeName = view.findViewById(R.id.tvWelcomeName);
        tvStoreName = view.findViewById(R.id.tvStoreName);
        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvTotalSold = view.findViewById(R.id.tvTotalSold);
        tvStoreRating = view.findViewById(R.id.tvStoreRating);
        tvAiForecast = view.findViewById(R.id.tvAiForecast);
        rvTopSellers = view.findViewById(R.id.rvTopSellers);

        view.findViewById(R.id.btnDashAddProduct).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AddProductActivity.class));
        });

        view.findViewById(R.id.btnSellerDashInbox).setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(((ViewGroup) requireView().getParent()).getId(), new InboxFragment())
                    .addToBackStack(null)
                    .commit();
        });

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        loadDashboardData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        String storeId = sessionManager.getStoreId();
        String userId = sessionManager.getUserId();

        if (storeId == null || userId == null) return;

        dbManager.userDB.getUserById(userId).addOnSuccessListener(documentSnapshot -> {
            if (!isAdded() || getContext() == null) return;
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                tvWelcomeName.setText("Welcome, " + (user != null ? user.getName() : "Seller") + " 👋");
            }
        });

        dbManager.storeDB.getStoreById(storeId).addOnSuccessListener(documentSnapshot -> {
            if (!isAdded() || getContext() == null) return;
            if (documentSnapshot.exists()) {
                Store store = documentSnapshot.toObject(Store.class);
                if (store != null) {
                    tvStoreName.setText(store.getStoreName());
                }
            }
        });

        dbManager.productDB.getProductsForSeller(storeId).addOnSuccessListener(queryDocumentSnapshots -> {
            if (!isAdded() || getContext() == null) return;

            ArrayList<Product> products = new ArrayList<>();
            int totalSold = 0;
            double sumRatings = 0.0;
            int ratedProductsCount = 0;

            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                if (p != null) {
                    totalSold += p.getSoldCount();
                    products.add(p);

                    if (p.getRatingAverage() > 0) {
                        sumRatings += p.getRatingAverage();
                        ratedProductsCount++;
                    }
                }
            }

            tvTotalProducts.setText(String.valueOf(products.size()));
            tvTotalSold.setText(String.valueOf(totalSold));

            double finalStoreRating = 0.0;
            if (ratedProductsCount > 0) {
                finalStoreRating = sumRatings / ratedProductsCount;
            }
            tvStoreRating.setText(String.format("%.1f", finalStoreRating));

            setupTopSellers(products);

            // ✨ AI FORECAST TRIGGER
            if (!products.isEmpty()) {
                generateForecast(products.size(), totalSold, finalStoreRating, products);
            } else {
                tvAiForecast.setText("Add some products to get your first AI store forecast!");
            }
        });
    }

    private void setupTopSellers(ArrayList<Product> products) {
        if (products.isEmpty() || getContext() == null) return;

        Collections.sort(products, (p1, p2) -> Integer.compare(p2.getSoldCount(), p1.getSoldCount()));

        ArrayList<Product> top5 = new ArrayList<>();
        for (int i = 0; i < Math.min(5, products.size()); i++) {
            top5.add(products.get(i));
        }

        SellerProductAdapter topSellerAdapter = new SellerProductAdapter(requireContext(), top5, null, null, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        rvTopSellers.setLayoutManager(layoutManager);
        rvTopSellers.setAdapter(topSellerAdapter);
    }

    // ✨ NEW AI METHOD
    private void generateForecast(int totalItems, int totalSold, double avgRating, ArrayList<Product> allProducts) {
        tvAiForecast.setText("Analyzing store performance...");

        // Create a string of their top items so the AI knows what they sell
        StringBuilder inventoryInfo = new StringBuilder();
        for (int i = 0; i < Math.min(allProducts.size(), 5); i++) {
            inventoryInfo.append(allProducts.get(i).getName())
                    .append(" (Sold: ").append(allProducts.get(i).getSoldCount()).append("), ");
        }

        String prompt = "You are an expert e-commerce store analyst. " +
                "This store has " + totalItems + " total items, " + totalSold + " total sales, and a " + String.format("%.1f", avgRating) + " star average rating. " +
                "Their inventory includes: " + inventoryInfo.toString() + " " +
                "Based on this, forecast which product category they need to improve, and which category they should add more of to increase sales. " +
                "Keep your response strictly under 4 lines. Do not use markdown formatting. Be direct and helpful.";

        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject parts = new JSONObject();
            parts.put("text", prompt);

            JSONObject message = new JSONObject();
            message.put("parts", new JSONArray().put(parts));

            contents.put(message);
            jsonBody.put("contents", contents);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // REPLACE WITH YOUR GEMINI API KEY
        String apiKey = "AIzaSyACbNXePoBtBZlhoA7wM9Bx3Q41mcdP3_g";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder().url(url).post(body).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                android.util.Log.e("GeminiError", "Network request failed entirely", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvAiForecast.setText("Network error. Check your connection."));
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() == null) return;

                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);

                        String generatedText = jsonObject.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        getActivity().runOnUiThread(() -> tvAiForecast.setText(generatedText.trim()));

                    } catch (Exception e) {
                        android.util.Log.e("GeminiError", "Failed to parse JSON response", e);
                        getActivity().runOnUiThread(() -> tvAiForecast.setText("Received an unexpected response from AI."));
                    }
                } else {
                    // ✨ THIS WAS MISSING ✨
                    // Catch 400 Bad Request, 403 Forbidden, etc.
                    String errorBody = "Unknown error";
                    try {
                        if (response.body() != null) {
                            errorBody = response.body().string();
                        }
                    } catch (IOException ignored) {}

                    final String finalError = errorBody;
                    android.util.Log.e("GeminiError", "API Error " + response.code() + ": " + finalError);

                    getActivity().runOnUiThread(() ->
                            tvAiForecast.setText("API Error " + response.code() + " (Check Logcat for details).")
                    );
                }
            }
        });
    }
}