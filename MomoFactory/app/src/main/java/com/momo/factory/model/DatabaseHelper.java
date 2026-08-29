package com.momo.factory.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "momo_factory.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_IS_PRO = "isPro";
    private static final String COLUMN_WALLET = "wallet";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_USERNAME + " TEXT UNIQUE,"
                    + COLUMN_PASSWORD + " TEXT,"
                    + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_IS_PRO + " INTEGER DEFAULT 0,"
                    + COLUMN_WALLET + " REAL DEFAULT 0.0"
                    + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);

        // Default admin user
        String defaultAdmin = "INSERT INTO " + TABLE_USERS + "("
                + COLUMN_USERNAME + "," + COLUMN_PASSWORD + "," + COLUMN_ADDRESS + "," + COLUMN_IS_PRO
                + ") VALUES ('admin', '" + Base64.encodeToString("admin123".getBytes(), Base64.DEFAULT) + "', 'Admin Address', 1)";
        db.execSQL(defaultAdmin);

        // Sample user
        String sampleUser = "INSERT INTO " + TABLE_USERS + "("
                + COLUMN_USERNAME + "," + COLUMN_PASSWORD + "," + COLUMN_ADDRESS + "," + COLUMN_IS_PRO
                + ") VALUES ('john_doe', '" + Base64.encodeToString("password123".getBytes(), Base64.DEFAULT) + "', '123 Main St', 0)";
        db.execSQL(sampleUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    public User checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;
        try {
            String sql = "SELECT * FROM " + TABLE_USERS + " WHERE "
                    + COLUMN_USERNAME + " = '" + username + "' AND "
                    + COLUMN_PASSWORD + " = '" + password + "'";

            Log.d("DatabaseHelper", "Login SQL: " + sql);
            Cursor cursor = db.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                user.setProUser(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PRO)) == 1);
                user.setWalletBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WALLET)));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Login error: " + e.getMessage());
        } finally {
            db.close();
        }
        return user;
    }

    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String sql = "INSERT INTO " + TABLE_USERS + "("
                    + COLUMN_USERNAME + "," + COLUMN_PASSWORD + "," + COLUMN_ADDRESS
                    + ") VALUES ('" + user.getUsername() + "', '" + user.getPassword() + "', '" + user.getAddress() + "')";

            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
}