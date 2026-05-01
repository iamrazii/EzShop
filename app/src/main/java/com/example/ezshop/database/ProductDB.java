package com.example.ezshop.database;
import com.example.ezshop.models.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class ProductDB {
    private FirebaseFirestore cloudDb;

    public ProductDB(FirebaseFirestore cloudDb) { this.cloudDb = cloudDb; }

    public Task<Void> addProduct(Product product) {
        if (product.getProductId() == null) product.setProductId(cloudDb.collection("products").document().getId());
        return cloudDb.collection("products").document(product.getProductId()).set(product);
    }

    public Task<Void> updateProduct(Product product) {
        return cloudDb.collection("products").document(product.getProductId()).set(product);
    }

    public Task<Void> deleteProduct(String productId) {
        return cloudDb.collection("products").document(productId).delete();
    }

    public Task<QuerySnapshot> getAllProducts() {
        return cloudDb.collection("products").orderBy("soldCount", Query.Direction.DESCENDING).get();
    }

    public Task<QuerySnapshot> getProductsByCategory(String categoryId) {
        return cloudDb.collection("products").whereEqualTo("categoryId", categoryId).get();
    }

    public Task<QuerySnapshot> getProductsForSeller(String storeId) {
        return cloudDb.collection("products").whereEqualTo("storeId", storeId).get();
    }

    public Task<Void> incrementSoldCount(String productId, int quantitySold) {
        return cloudDb.collection("products").document(productId)
                .update("soldCount", FieldValue.increment(quantitySold));
    }

    public Task<QuerySnapshot> searchProducts(String query) {
        // Firebase Prefix Search Hack
        return cloudDb.collection("products")
                .whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + "\uf8ff")
                .get();
    }
}