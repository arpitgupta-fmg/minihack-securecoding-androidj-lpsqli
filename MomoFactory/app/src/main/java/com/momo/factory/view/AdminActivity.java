package com.momo.factory.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.momo.factory.R;

public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminTitle, tvAdminMessage, tvStats;
    private Button btnRefresh, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        setupAdminPanel();
        setupListeners();
    }

    private void initViews() {
        tvAdminTitle = findViewById(R.id.tvAdminTitle);
        tvAdminMessage = findViewById(R.id.tvAdminMessage);
        tvStats = findViewById(R.id.tvStats);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnClose = findViewById(R.id.btnClose);

        tvAdminTitle.setTextColor(ContextCompat.getColor(this, R.color.primary));
    }

    private void setupAdminPanel() {
        tvAdminMessage.setText(
                "⚡ ADMIN PANEL\n\n" +
                        "⚠️ SECURITY WARNING:\n" +
                        "This activity is exported!\n" +
                        "Any app can launch this!\n\n" +
                        "🔑 Secret Key: MOMO_FACTORY_2024"
        );

        updateStats();
    }

    private void updateStats() {
        tvStats.setText(
                "📊 STATISTICS\n\n" +
                        "👥 Total Users: 42\n" +
                        "📦 Total Orders: 156\n" +
                        "💰 Revenue: ₹12,500.00\n" +
                        "⭐ Pro Users: 12"
        );
    }

    private void setupListeners() {
        btnRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
            updateStats();
        });

        btnClose.setOnClickListener(v -> finish());
    }
}