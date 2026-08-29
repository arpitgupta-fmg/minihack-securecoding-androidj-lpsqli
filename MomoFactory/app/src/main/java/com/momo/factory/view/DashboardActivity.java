package com.momo.factory.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.momo.factory.R;
import com.momo.factory.model.User;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvUserType, tvWalletBalance, tvMemberSince;
    private Button btnOrder, btnLogout;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        currentUser = (User) getIntent().getSerializableExtra("user");

        if (currentUser == null) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupDashboard();
        setupListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvUserType = findViewById(R.id.tvUserType);
        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        btnOrder = findViewById(R.id.btnOrder);
        btnLogout = findViewById(R.id.btnLogout);

        btnOrder.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
    }

    private void setupDashboard() {
        tvWelcome.setText("Welcome, " + currentUser.getUsername() + "!");

        if (currentUser.isProUser()) {
            tvUserType.setText("⭐ PRO MEMBER");
            tvUserType.setTextColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            tvUserType.setText("👤 Regular Member");
            tvUserType.setTextColor(ContextCompat.getColor(this, R.color.dark));
        }

        tvWalletBalance.setText("₹ " + String.format("%.2f", currentUser.getWalletBalance()));
        tvMemberSince.setText("Member since: Jan 2025");
    }

    private void setupListeners() {
        btnOrder.setOnClickListener(v -> {
            Toast.makeText(this, "Order screen is under testing", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Hidden: Tap user type to open admin
        tvUserType.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}