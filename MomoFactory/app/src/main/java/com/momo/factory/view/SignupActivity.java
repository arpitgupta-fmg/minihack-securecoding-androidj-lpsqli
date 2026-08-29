package com.momo.factory.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.momo.factory.R;
import com.momo.factory.controller.AuthController;

public class SignupActivity extends AppCompatActivity {

    private EditText etUsername, etPassword, etConfirmPassword, etAddress;
    private Button btnSignup;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authController = new AuthController(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etAddress = findViewById(R.id.etAddress);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        btnSignup.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
    }

    private void setupListeners() {
        btnSignup.setOnClickListener(v -> attemptSignup());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptSignup() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username required");
            return;
        }
        if (username.length() < 3) {
            etUsername.setError("Min 3 characters");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password required");
            return;
        }
        if (password.length() < 4) {
            etPassword.setError("Min 4 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(false);

        btnSignup.postDelayed(() -> {
            boolean success = authController.registerUser(username, password, address);

            progressBar.setVisibility(View.GONE);
            btnSignup.setEnabled(true);

            if (success) {
                Toast.makeText(SignupActivity.this, "Account created! Please login.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(SignupActivity.this, "Signup failed. Username may exist.", Toast.LENGTH_LONG).show();
            }
        }, 1500);
    }
}