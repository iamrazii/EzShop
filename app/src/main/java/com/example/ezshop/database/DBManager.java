package com.example.ezshop.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DBManager {

    public static final String DATABASE_NAME = "EzShopDB";
    public static final int DATABASE_VERSION = 2; // Bumped to 3 to force table rebuild with TEXT IDs

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

    public DBManager(Context context) { this.context = context; }

    public DBManager open() {
        helper = new DBHelper(context);
        database = helper.getWritableDatabase();
        database.execSQL("PRAGMA foreign_keys = ON;");

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

    public void close() { if (helper != null) helper.close(); }

    private class DBHelper extends SQLiteOpenHelper {
        public DBHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE users (user_id TEXT PRIMARY KEY, name TEXT, email TEXT, password_hash TEXT, wallet_balance REAL, default_shipping_address TEXT)");
            db.execSQL("CREATE TABLE categories (category_id TEXT PRIMARY KEY, name TEXT, icon_name TEXT)");
            db.execSQL("CREATE TABLE stores (store_id TEXT PRIMARY KEY, store_name TEXT, owner_id TEXT, location TEXT, status TEXT, rating REAL)");
            db.execSQL("CREATE TABLE promos (promo_id TEXT PRIMARY KEY, code TEXT, discount_percentage REAL)");
            db.execSQL("CREATE TABLE products (product_id TEXT PRIMARY KEY, store_id TEXT, category_id TEXT, _name TEXT, _description TEXT, _price REAL, _condition TEXT, _image TEXT, weight_grams INTEGER, rating_average REAL, sold_count INTEGER, FOREIGN KEY(store_id) REFERENCES stores(store_id), FOREIGN KEY(category_id) REFERENCES categories(category_id))");
            db.execSQL("CREATE TABLE reviews (review_id TEXT PRIMARY KEY, product_id TEXT, user_id TEXT, rating INTEGER, comment TEXT, review_date TEXT, FOREIGN KEY(product_id) REFERENCES products(product_id), FOREIGN KEY(user_id) REFERENCES users(user_id))");
            db.execSQL("CREATE TABLE search_logs (log_id TEXT PRIMARY KEY, user_id TEXT, product_id TEXT, search_query TEXT, timestamp INTEGER, FOREIGN KEY(user_id) REFERENCES users(user_id), FOREIGN KEY(product_id) REFERENCES products(product_id))");
            db.execSQL("CREATE TABLE cart_items (cart_item_id TEXT PRIMARY KEY, user_id TEXT, product_id TEXT, variant_id TEXT, quantity INTEGER, FOREIGN KEY(user_id) REFERENCES users(user_id), FOREIGN KEY(product_id) REFERENCES products(product_id))");
            db.execSQL("CREATE TABLE orders (order_id TEXT PRIMARY KEY, user_id TEXT, promo_id TEXT, shipping_address TEXT, payment_method TEXT, total_price REAL, order_status TEXT, created_at INTEGER, FOREIGN KEY(user_id) REFERENCES users(user_id))");
            db.execSQL("CREATE TABLE order_items (order_item_id TEXT PRIMARY KEY, order_id TEXT, product_id TEXT, price_at_purchase REAL, quantity INTEGER, FOREIGN KEY(order_id) REFERENCES orders(order_id), FOREIGN KEY(product_id) REFERENCES products(product_id))");
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
            onCreate(db);
        }
    }
}