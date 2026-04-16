package com.example.ezshop.database;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.ezshop.models.Promo;

public class PromoDB {
    private SQLiteDatabase database;

    private static final String TABLE_PROMOS = "promos";
    private static final String COLUMN_ID = "promo_id";
    private static final String COLUMN_CODE = "code";
    private static final String COLUMN_DISCOUNT = "discount_percentage";

    public PromoDB(SQLiteDatabase database) {
        this.database = database;
    }

    public long addPromo(Promo promo) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_CODE, promo.getCode().toUpperCase()); // Always save codes uppercase
        cv.put(COLUMN_DISCOUNT, promo.getDiscountPercentage());
        return database.insert(TABLE_PROMOS, null, cv);
    }

    public Promo getPromoByCode(String code) {
        Promo promo = null;
        Cursor cursor = database.query(
                TABLE_PROMOS, null,
                COLUMN_CODE + "=?",
                new String[]{code.toUpperCase()}, // Check against uppercase
                null, null, null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                promo = new Promo();
                promo.setPromoId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                promo.setCode(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CODE)));
                promo.setDiscountPercentage(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISCOUNT)));
            }
            cursor.close();
        }
        return promo; // return null if the code is invalid
    }
}