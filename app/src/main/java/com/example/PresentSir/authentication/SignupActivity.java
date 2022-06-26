package com.raina.PresentSir.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.raina.PresentSir.databinding.ActivitySignupBinding;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {
    private static final String MyPREFERENCES = "MyPrefs";
    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;
    private UserViewModel userviewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        userviewModel = new ViewModelProvider(SignupActivity.this).get(UserViewModel.class);


        binding.signupBtn.setOnClickListener(view -> {
            String email = binding.emailEdtText.getText().toString();
            String password = binding.passEdtText.getText().toString();
            String role = "Student";

            if (credentialCheck(email, password)) {
                // Firebase call to create account with given credentials
                if (!isNetworkConnected())
                    Toast.makeText(SignupActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                else
                {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                            // Creates document for user in user collection in firebase
                            userviewModel.createNewUser(Objects.requireNonNull(task.getResult().getUser()).getUid(), email, role);
                            SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("Role", role);
                            editor.apply();
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else if (!isNetworkConnected())
                            Toast.makeText(SignupActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(SignupActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        binding.loginBtn.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Email and Password validation
    boolean credentialCheck(String email, String password) {
        email = email.trim();
        if (password.length() < 6) {
            binding.passEdtText.setError("Password must contain atleast 6 Characters");
            binding.passEdtText.requestFocus();
        }
        if (password.equals("")) {
            binding.passEdtText.setError("Please enter Password");
            binding.passEdtText.requestFocus();
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEdtText.setError("Please enter valid Email address");
            binding.emailEdtText.requestFocus();
        }
        if (email.equals("")) {
            binding.emailEdtText.setError("Please enter Email address");
            binding.emailEdtText.requestFocus();
        }
        return !email.equals("") && password.length() >= 6 &&
                !password.trim().equals("") &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Function to check if phone is connected to a network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}