package com.example.ezshop.database;

import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;

public class DBManager {

    public UserDB userDB;
    public CategoryDB categoryDB;
    public StoreDB storeDB;
    public PromoDB promoDB;
    public ProductDB productDB;
    public ReviewDB reviewDB;
    public SearchlogDB searchlogDB;
    public CartItemDB cartItemDB;
    public OrderDB orderDB;

    // We keep Context in the constructor so you don't have to rewrite your fragment code!
    public DBManager(Context context) { }

    public DBManager open() {
        FirebaseFirestore cloudDb = FirebaseFirestore.getInstance();
        // Note: Firebase enables offline persistence automatically on Android!

        userDB = new UserDB(cloudDb);
        categoryDB = new CategoryDB(cloudDb);
        storeDB = new StoreDB(cloudDb);
        promoDB = new PromoDB(cloudDb);
        productDB = new ProductDB(cloudDb);
        reviewDB = new ReviewDB(cloudDb);
        searchlogDB = new SearchlogDB(cloudDb);
        cartItemDB = new CartItemDB(cloudDb);
        orderDB = new OrderDB(cloudDb);

        return this;
    }

    public void close() {
        // Nothing to close! Firebase handles its own connections.
    }
}