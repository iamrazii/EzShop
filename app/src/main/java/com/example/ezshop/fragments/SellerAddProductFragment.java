package com.example.ezshop.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.ezshop.R;
import com.example.ezshop.database.DBManager;
import com.example.ezshop.models.Category;
import com.example.ezshop.models.Product;
import com.example.ezshop.utilities.SessionManager;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SellerAddProductFragment extends Fragment {

    private DBManager dbManager;
    private SessionManager sessionManager;
    private EditText etProductName, etPrice, etDescription, etWeight;
    private Spinner spinnerCategory, spinnerCondition;
    private ImageView ivProductImage;
    private LinearLayout llImagePlaceholder;
    private ArrayList<Category> categories;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProductImage.setImageURI(uri);
                    ivProductImage.setVisibility(View.VISIBLE);
                    llImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && selectedImageUri != null) {
                    ivProductImage.setImageURI(selectedImageUri);
                    ivProductImage.setVisibility(View.VISIBLE);
                    llImagePlaceholder.setVisibility(View.GONE);
                }
            }
    );

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                boolean storageGranted = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    storageGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                } else {
                    storageGranted = result.getOrDefault(Manifest.permission.READ_EXTERNAL_STORAGE, false);
                }

                if (!cameraGranted || !storageGranted) {
                    Toast.makeText(requireContext(), "Permissions are required to add photos", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_add_product, container, false);

        sessionManager = new SessionManager(requireContext());
        dbManager = new DBManager(requireContext());
        dbManager.open();

        dbManager.categoryDB.seedCategories();

        etProductName = view.findViewById(R.id.etProductName);
        etPrice = view.findViewById(R.id.etPrice);
        etDescription = view.findViewById(R.id.etDescription);
        etWeight = view.findViewById(R.id.etWeight);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerCondition = view.findViewById(R.id.spinnerCondition);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        llImagePlaceholder = view.findViewById(R.id.llImagePlaceholder);

        setupSpinners();

        view.findViewById(R.id.flImagePicker).setOnClickListener(v -> showImagePickDialog());
        view.findViewById(R.id.btnSaveProduct).setOnClickListener(v -> saveProduct());

        return view;
    }

    private void setupSpinners() {
        categories = dbManager.categoryDB.getAllCategories();
        ArrayList<String> categoryNames = new ArrayList<>();

        if (categories != null && !categories.isEmpty()) {
            for (Category c : categories) {
                categoryNames.add(c.getName());
            }
        } else {
            categoryNames.add("Default");
        }

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categoryNames);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        String[] conditions = {"New", "Used - Like New", "Used - Good", "Used - Acceptable"};
        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, conditions);
        condAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(condAdapter);
    }

    private void showImagePickDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Product Image")
                .setItems(options, (dialog, which) -> {
                    checkPermissionsAndLaunch(which);
                })
                .show();
    }

    private void checkPermissionsAndLaunch(int choice) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            requestPermissionsLauncher.launch(permissions);
        } else {
            if (choice == 0) {
                selectedImageUri = createTempImageUri();
                cameraLauncher.launch(selectedImageUri);
            } else {
                galleryLauncher.launch("image/*");
            }
        }
    }

    private Uri createTempImageUri() {
        String fileName = "IMG_" + System.currentTimeMillis();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(requireContext(), "Product name and price are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        int weight = weightStr.isEmpty() ? 500 : Integer.parseInt(weightStr);
        int categoryId = 1;
        if (categories != null && !categories.isEmpty()) {
            categoryId = categories.get(spinnerCategory.getSelectedItemPosition()).getCategoryId();
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(desc);
        product.setWeightGrams(weight);
        product.setCategoryId(categoryId);
        product.setCondition(spinnerCondition.getSelectedItem().toString());
        product.setStoreId(sessionManager.getStoreId());
        product.setRatingAverage(0.0);
        product.setSoldCount(0);
        product.setProductimage(selectedImageUri != null ? selectedImageUri.toString() : "macbook");

        if (dbManager.productDB.addProduct(product) != -1) {
            Toast.makeText(requireContext(), "Product added!", Toast.LENGTH_SHORT).show();
            clearFields();
            ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
            if (viewPager != null) viewPager.setCurrentItem(1);
        } else {
            Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etProductName.setText("");
        etPrice.setText("");
        etDescription.setText("");
        etWeight.setText("");
        ivProductImage.setVisibility(View.GONE);
        llImagePlaceholder.setVisibility(View.VISIBLE);
        selectedImageUri = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbManager != null) dbManager.close();
    }
}