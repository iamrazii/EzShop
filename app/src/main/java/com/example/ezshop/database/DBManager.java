package com.example.ezshop.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBManager {

    public static final String DATABASE_NAME = "EzShopDB";
    // BUMPED VERSION TO 2: This forces the app to rebuild the fixed tables!
    public static final int DATABASE_VERSION = 2;

    private Context context;
    private DBHelper helper;
    private SQLiteDatabase database;

    public UserDB userDB;
    public CategoryDB categoryDB;
    public StoreDB storeDB;
    public PromoDB promoDB;
    public ProductDB productDB;
    public ReviewDB reviewDB;
    public SearchlogDB searchlogDB;
    public CartItemDB cartItemDB;
    public OrderDB orderDB;

    public DBManager(Context context) {
        this.context = context;
    }

    public DBManager open() {
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();

        // Turn on Foreign Keys so our tables can strictly link to each other
        database.execSQL("PRAGMA foreign_keys = ON;");

        // hiring the sub workers
        userDB = new UserDB(database);
        categoryDB = new CategoryDB(database);
        storeDB = new StoreDB(database);
        promoDB = new PromoDB(database);
        productDB = new ProductDB(database);
        reviewDB = new ReviewDB(database);
        searchlogDB = new SearchlogDB(database);
        cartItemDB = new CartItemDB(database);
        orderDB = new OrderDB(database);

        return this;
    }

    public void close() {
        if (helper != null) helper.close();
    }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            // Users Table
            db.execSQL("CREATE TABLE users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " + "email TEXT, " + "password_hash TEXT, " +
                    "wallet_balance REAL, " + "default_shipping_address TEXT)");

            // Category Table
            db.execSQL("CREATE TABLE categories (" +
                    "category_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " + "icon_name TEXT)");

            // Stores Table (FIXED: Added owner_id)
            db.execSQL("CREATE TABLE stores (" +
                    "store_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "store_name TEXT, " + "owner_id INTEGER, " + "location TEXT, " + "status TEXT, " + "rating REAL)");

            // Promo Table
            db.execSQL("CREATE TABLE promos (" +
                    "promo_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT, " +
                    "discount_percentage REAL)");

            // Products Table
            db.execSQL("CREATE TABLE products (" +
                    "product_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "store_id INTEGER, " + "category_id INTEGER, " + "_name TEXT, " +
                    "_description TEXT, " + "_price REAL, " + "_condition TEXT, " +
                    "_image TEXT, " + "weight_grams INTEGER, " +
                    "rating_average REAL, " + "sold_count INTEGER, " +
                    "FOREIGN KEY(store_id) REFERENCES stores(store_id), " +
                    "FOREIGN KEY(category_id) REFERENCES categories(category_id))");

            // Reviews Table
            db.execSQL("CREATE TABLE reviews (" +
                    "review_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "product_id INTEGER, " +
                    "user_id INTEGER, " +
                    "rating INTEGER, " +
                    "comment TEXT, " +
                    "review_date TEXT, " +
                    "FOREIGN KEY(product_id) REFERENCES products(product_id), " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id))");

            // Searchlogs Table
            db.execSQL("CREATE TABLE search_logs (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "product_id INTEGER, " +
                    "search_query TEXT, " +
                    "timestamp INTEGER, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY(product_id) REFERENCES products(product_id))");

            // Cart_items Table
            db.execSQL("CREATE TABLE cart_items (" +
                    "cart_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "product_id INTEGER, " +
                    "variant_id INTEGER, " +
                    "quantity INTEGER, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY(product_id) REFERENCES products(product_id))");

            // Orders Table (FIXED: Matched exactly to OrderDB schema)
            db.execSQL("CREATE TABLE orders (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "promo_id INTEGER, " +
                    "shipping_address TEXT, " +
                    "payment_method TEXT, " +
                    "total_price REAL, " +
                    "order_status TEXT, " +
                    "created_at INTEGER, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id))");

            // OrderItems Table (FIXED: Added price_at_purchase)
            db.execSQL("CREATE TABLE order_items (" +
                    "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "product_id INTEGER, " +
                    "price_at_purchase REAL, " +
                    "quantity INTEGER, " +
                    "FOREIGN KEY(order_id) REFERENCES orders(order_id), " +
                    "FOREIGN KEY(product_id) REFERENCES products(product_id))");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS order_items");
            db.execSQL("DROP TABLE IF EXISTS orders");
            db.execSQL("DROP TABLE IF EXISTS cart_items");
            db.execSQL("DROP TABLE IF EXISTS search_logs");
            db.execSQL("DROP TABLE IF EXISTS reviews");
            db.execSQL("DROP TABLE IF EXISTS products");
            db.execSQL("DROP TABLE IF EXISTS promos");
            db.execSQL("DROP TABLE IF EXISTS stores");
            db.execSQL("DROP TABLE IF EXISTS categories");
            db.execSQL("DROP TABLE IF EXISTS users");

            // Create everything again cleanly
            onCreate(db);
        }

    }
}