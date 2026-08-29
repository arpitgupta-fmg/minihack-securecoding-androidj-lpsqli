package com.momo.factory.controller;

import android.content.Context;

import com.momo.factory.model.DatabaseHelper;
import com.momo.factory.model.User;

public class AuthController {

    private DatabaseHelper dbHelper;

    public AuthController(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public boolean registerUser(String username, String password, String address) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                address == null || address.trim().isEmpty()) {
            return false;
        }

        User user = new User(username, password, address);
        return dbHelper.addUser(user);
    }

    public User loginUser(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }
        return dbHelper.checkLogin(username, password); // VULNERABLE
    }
}