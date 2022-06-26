package com.raina.PresentSir.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.raina.PresentSir.databinding.ActivityResetPasswordBinding;

public class ResetPasswordActivity extends AppCompatActivity {
    private ActivityResetPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.backBtn.setOnClickListener(view -> {

            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.resetPassBtn.setOnClickListener(view -> {
            String email = binding.emailEdtText.getText().toString();

            // Check if any field is empty
            if (TextUtils.isEmpty(email))
                Toast.makeText(ResetPasswordActivity.this, "Please Enter Email ID", Toast.LENGTH_LONG).show();
            else {
                // Firebase call to send password reset link using user's email id
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(ResetPasswordActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPasswordActivity.this, "Reset link sent to your email", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Unable to send reset mail", Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
        });

    }
}