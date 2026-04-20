package com.example.ezshop.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.ezshop.R;
import com.example.ezshop.adapters.SellerPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.view.WindowInsetsControllerCompat;

public class SellerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(android.graphics.Color.parseColor("#121212"));

        new WindowInsetsControllerCompat(window, window.getDecorView()).setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_seller_home);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);


        SellerPagerAdapter adapter = new SellerPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setIcon(R.drawable.home_icon); break;
                case 1: tab.setIcon(R.drawable.inventory); break;
                case 2: tab.setIcon(R.drawable.add_box); break;
                case 3: tab.setIcon(R.drawable.settings); break;
            }
        }).attach();
    }
}