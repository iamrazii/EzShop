package com.example.ezshop.Activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.example.ezshop.fragments.ExploreProductFragment;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.models.Store;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DBManager dbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "da3jabd2w"); // Replace with your actual cloud name!
        MediaManager.init(this, config);

        // 1. Open the database
        dbManager = new DBManager(this);
        try {
            dbManager.open();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Seed the database with dummy data (Only runs if empty!)
        seedDatabaseIfNeeded();

        // 3. Load the ExploreFragment into the FrameLayout
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ExploreProductFragment())
                    .commit();
        }
    }

    private void seedDatabaseIfNeeded() {
        // We check if categories are empty. If they are, it's a fresh install!
        if (dbManager.categoryDB.getAllCategories().isEmpty()) {

            // --- 1. CREATE A DUMMY STORE ---
            Store store = new Store();
            store.setStoreName("AlfinasStore");
            store.setLocation("South Jakarta");
            store.setRating(4.8);
            store.setStatus("Active");
            // Save it and get its ID so we can assign products to it
            String storeId =  dbManager.storeDB.addStore(store);

            // --- 2. CREATE DUMMY CATEGORIES ---
            Category catLaptop = new Category();
            catLaptop.setName("Laptop");
            catLaptop.setIconName("laptop_icon"); // Make sure this XML file exists in your drawable folder!
            String laptopCatId =  dbManager.categoryDB.addCategory(catLaptop);

            Category catPhone = new Category();
            catPhone.setName("Smartphone");
            catPhone.setIconName("mobile_icon");
            String phoneCatId =  dbManager.categoryDB.addCategory(catPhone);

            Category catMonitor = new Category();
            catMonitor.setName("Mouse");
            catMonitor.setIconName("mouse_icon");
            String monitorCatId = dbManager.categoryDB.addCategory(catMonitor);

            // --- 3. CREATE DUMMY PRODUCTS ---
            Product p1 = new Product();
            p1.setStoreId(storeId);
            p1.setCategoryId(laptopCatId);
            p1.setName("Asus ROG Zephyrus");
            p1.setPrice(1499.99);
            p1.setRatingAverage(4.9);
            p1.setSoldCount(150);
            p1.setProductimage("macbook");
            dbManager.productDB.addProduct(p1);

            Product p2 = new Product();
            p2.setStoreId(storeId);
            p2.setCategoryId(phoneCatId);
            p2.setName("Redmi 9A");
            p2.setPrice(348.00);
            p2.setRatingAverage(4.8);
            p2.setSoldCount(250);
            p2.setProductimage("macbook");
            dbManager.productDB.addProduct(p2);

            Product p3 = new Product();
            p3.setStoreId(storeId);
            p3.setCategoryId(laptopCatId);
            p3.setName("Macbook Air M2");
            p3.setPrice(1199.00);
            p3.setRatingAverage(5.0);
            p3.setSoldCount(890);
            p3.setProductimage("macbook");
            dbManager.productDB.addProduct(p3);

            // --- 4. CREATE DUMMY TRENDING SEARCHES ---
            // Null user ID because they are global searches
//            dbManager.searchlogDB.addSearch("Macbook Air M2", 1,1);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Always close the master vault when the app is completely closed!
        if (dbManager != null) {
            dbManager.close();
        }
    }
}