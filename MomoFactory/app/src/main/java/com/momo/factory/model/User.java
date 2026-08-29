package com.momo.factory.model;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String password;
    private String address;
    private boolean isProUser;
    private double walletBalance;

    public User() {}

    public User(String username, String password, String address) {
        this.username = username;
        this.password = password;
        this.address = address;
        this.isProUser = false;
        this.walletBalance = 160;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public boolean isProUser() { return isProUser; }
    public void setProUser(boolean proUser) { isProUser = proUser; }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }
}