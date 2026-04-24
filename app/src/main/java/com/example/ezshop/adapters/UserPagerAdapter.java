package com.example.ezshop.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.ezshop.fragments.CartFragment;
import com.example.ezshop.fragments.ExploreProductFragment;
import com.example.ezshop.fragments.UserDashboardFragment;
import com.example.ezshop.fragments.UserSettingsFragment;

public class UserPagerAdapter extends FragmentStateAdapter {

    public UserPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new UserDashboardFragment();
            case 1: return new ExploreProductFragment();
            case 2: return new CartFragment();
            case 3: return new UserSettingsFragment();
            default: return new UserDashboardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}