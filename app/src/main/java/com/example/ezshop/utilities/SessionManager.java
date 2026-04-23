package com.example.ezshop.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "EzShopSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_ROLE = "userRole";
    private static final String KEY_STORE_ID = "storeId";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(String userId, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public void setStoreId(String storeId) {
        editor.putString(KEY_STORE_ID, storeId);
        editor.apply();
    }

    public String getStoreId() {
        return prefs.getString(KEY_STORE_ID, null);
    }

    public  String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserRole() {
        return prefs.getString(KEY_ROLE, "Guest");
    }

    public String getUserType() {
        return getUserRole();
    }
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
