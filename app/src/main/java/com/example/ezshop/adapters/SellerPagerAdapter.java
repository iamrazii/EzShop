package com.example.ezshop.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.ezshop.fragments.SellerDashboardFragment;
import com.example.ezshop.fragments.SellerProductsFragment;
import com.example.ezshop.fragments.SellerAddProductFragment;
import com.example.ezshop.fragments.SellerSettingsFragment;

public class SellerPagerAdapter extends FragmentStateAdapter {

    public SellerPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new SellerDashboardFragment();
            case 1: return new SellerProductsFragment();
            case 2: return new SellerAddProductFragment();
            case 3: return new SellerSettingsFragment();
            default: return new SellerDashboardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}