package com.example.ezshop.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ezshop.Activities.WelcomeActivity;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Store;
import com.example.ezshop.utilities.SessionManager;

public class SellerSettingsFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private Store currentStore;

    private LinearLayout llViewMode, llEditMode;
    private TextView tvStoreNameDetail, tvLocationDetail, tvStatusDetail;
    private EditText etEditStoreName, etEditLocation;
    private Spinner spinnerEditStatus;
    private String[] statuses = {"Active", "On Vacation", "Closed"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_settings, container, false);

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        llViewMode = view.findViewById(R.id.llViewMode);
        tvStoreNameDetail = view.findViewById(R.id.tvStoreNameDetail);
        tvLocationDetail = view.findViewById(R.id.tvLocationDetail);
        tvStatusDetail = view.findViewById(R.id.tvStatusDetail);

        llEditMode = view.findViewById(R.id.llEditMode);
        etEditStoreName = view.findViewById(R.id.etEditStoreName);
        etEditLocation = view.findViewById(R.id.etEditLocation);
        spinnerEditStatus = view.findViewById(R.id.spinnerEditStatus);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditStatus.setAdapter(adapter);

        loadStoreData();

        view.findViewById(R.id.btnEditStore).setOnClickListener(v -> toggleEditMode(true));
        view.findViewById(R.id.btnCancelEdit).setOnClickListener(v -> toggleEditMode(false));
        view.findViewById(R.id.btnSaveEdit).setOnClickListener(v -> saveStoreDetails());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(requireContext(), WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
        return view;
    }

    private void loadStoreData() {
        String storeId = sessionManager.getStoreId();
        if (storeId == null) return;

        dbManager.storeDB.getStoreById(storeId).addOnSuccessListener(doc -> {
            if (!isAdded() || getContext() == null) return;
            if (doc.exists()) {
                currentStore = doc.toObject(Store.class);
                if (currentStore != null) {
                    tvStoreNameDetail.setText(currentStore.getStoreName());
                    tvLocationDetail.setText(currentStore.getLocation());
                    tvStatusDetail.setText(currentStore.getStatus());

                    etEditStoreName.setText(currentStore.getStoreName());
                    etEditLocation.setText(currentStore.getLocation());

                    for (int i = 0; i < statuses.length; i++) {
                        if (statuses[i].equalsIgnoreCase(currentStore.getStatus())) {
                            spinnerEditStatus.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void toggleEditMode(boolean showEdit) {
        if (showEdit) {
            llViewMode.setVisibility(View.GONE);
            llEditMode.setVisibility(View.VISIBLE);
        } else {
            llEditMode.setVisibility(View.GONE);
            llViewMode.setVisibility(View.VISIBLE);
            loadStoreData();
        }
    }

    private void saveStoreDetails() {
        if (currentStore == null) return;

        String newName = etEditStoreName.getText().toString().trim();
        String newLocation = etEditLocation.getText().toString().trim();
        String newStatus = spinnerEditStatus.getSelectedItem().toString();

        if (newName.isEmpty() || newLocation.isEmpty()) {
            Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentStore.setStoreName(newName);
        currentStore.setLocation(newLocation);
        currentStore.setStatus(newStatus);

        dbManager.storeDB.updateStore(currentStore).addOnSuccessListener(aVoid -> {
            if (!isAdded() || getContext() == null) return;
            Toast.makeText(requireContext(), "Store updated!", Toast.LENGTH_SHORT).show();
            loadStoreData();
            toggleEditMode(false);
        }).addOnFailureListener(e -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }
}