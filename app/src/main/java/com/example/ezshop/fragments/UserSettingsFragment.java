package com.example.ezshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ezshop.Activities.EditUserDetailsActivity;
import com.example.ezshop.Activities.WelcomeActivity;
import com.example.ezshop.R;
import com.example.ezshop.adapters.MyOrdersAdapter;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Order;
import com.example.ezshop.models.User;
import com.example.ezshop.utilities.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;

public class UserSettingsFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private TextView tvName, tvEmail, tvAddress;
    private RecyclerView rvMyOrders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        tvName = view.findViewById(R.id.tvSettingsName);
        tvEmail = view.findViewById(R.id.tvSettingsEmail);
        tvAddress = view.findViewById(R.id.tvSettingsAddress);
        rvMyOrders = view.findViewById(R.id.rvMyOrders);

        // Edit Details Button
        view.findViewById(R.id.btnEditDetails).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditUserDetailsActivity.class));
        });

        // Logout Button
        view.findViewById(R.id.btnLogout).setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void loadMyOrders() {
        String userId = sessionManager.getUserId();
        if (userId != null) {
            ArrayList<Order> orders = dbManager.orderDB.getOrdersForUser(userId);
            MyOrdersAdapter adapter = new MyOrdersAdapter(requireContext(), orders, dbManager, userId);
            rvMyOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvMyOrders.setAdapter(adapter);
        }
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out of your account?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent i = new Intent(requireActivity(), WelcomeActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    requireActivity().finishAffinity();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUserData() {
        String userId = sessionManager.getUserId();
        User user = dbManager.userDB.getUserById(userId);
        if (user != null) {
            tvName.setText(user.getName());
            tvEmail.setText(user.getEmail());

            String address = user.getDefaultShippingAddress();
            if (address == null || address.isEmpty()) {
                tvAddress.setText("No address provided");
            } else {
                tvAddress.setText(address);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
        loadMyOrders();
    }
}