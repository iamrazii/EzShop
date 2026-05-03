package com.example.ezshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ezshop.R;
import com.example.ezshop.adapters.ReviewsAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Review;
import com.example.ezshop.models.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView rvAllReviews;
    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reviews);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnBackReviews = findViewById(R.id.btnBackReviews);
        rvAllReviews = findViewById(R.id.rvAllReviews);
        btnBackReviews.setOnClickListener(v -> finish());

        dbManager = new DBManager(this);
        dbManager.open();

        rvAllReviews.setLayoutManager(new LinearLayoutManager(this));
        Intent intent = getIntent();
        String productId = intent.getStringExtra("PRODUCT_ID");

        if (productId != null) {
            dbManager.reviewDB.getReviewsByProductId(productId).addOnSuccessListener(this, snap -> {
                ArrayList<Pair<Review, String>> allReviews = new ArrayList<>();

                ReviewsAdapter adapter = new ReviewsAdapter(this, allReviews, false);
                rvAllReviews.setAdapter(adapter);

                if (snap.isEmpty()) {
                    return;
                }

                for (DocumentSnapshot doc : snap) {
                    Review r = doc.toObject(Review.class);
                    if (r != null) {

                        dbManager.userDB.getUserById(r.getUserId()).addOnCompleteListener(task -> {
                            String realName = "Unknown User";

                            if (task.isSuccessful() && task.getResult().exists()) {
                                User reviewer = task.getResult().toObject(User.class);
                                if (reviewer != null && reviewer.getName() != null) {
                                    realName = reviewer.getName();
                                }
                            }

                            allReviews.add(new Pair<>(r, realName));

                            adapter.notifyDataSetChanged();
                        });
                    }
                }
            }).addOnFailureListener(e -> {
                android.util.Log.e("ReviewsActivity", "Failed to load reviews", e);
                Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}