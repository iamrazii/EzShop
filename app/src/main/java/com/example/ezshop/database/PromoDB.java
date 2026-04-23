package com.example.ezshop.database;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.UUID;
import com.example.ezshop.models.Promo;

public class PromoDB {
    private SQLiteDatabase database;
    public PromoDB(SQLiteDatabase database) { this.database = database; }

    public String addPromo(Promo promo) {
        ContentValues cv = new ContentValues();
        String newId = UUID.randomUUID().toString();
        cv.put("promo_id", newId);
        cv.put("code", promo.getCode().toUpperCase());
        cv.put("discount_percentage", promo.getDiscountPercentage());
        if(database.insert("promos", null, cv) != -1) return newId;
        return null;
    }

    public Promo getPromoByCode(String code) {
        Promo promo = null;
        Cursor cursor = database.query("promos", null, "code=?", new String[]{code.toUpperCase()}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            promo = new Promo();
            promo.setPromoId(cursor.getString(cursor.getColumnIndexOrThrow("promo_id")));
            promo.setCode(cursor.getString(cursor.getColumnIndexOrThrow("code")));
            promo.setDiscountPercentage(cursor.getDouble(cursor.getColumnIndexOrThrow("discount_percentage")));
            cursor.close();
        }
        return promo;
    }
}