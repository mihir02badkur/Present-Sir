package com.raina.PresentSir.authentication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.raina.PresentSir.databinding.ActivityLoginBinding;
import com.raina.PresentSir.faculty.FacultyStudent;
import com.raina.PresentSir.studentAttendance.AttendanceActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String MyPREFERENCES = "MyPrefs";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Progress Dialog while logging in
        ProgressDialog mProgress = new ProgressDialog(LoginActivity.this);
        mProgress.setTitle("Logging in");
        mProgress.setMessage("Please wait...");
        mProgress.setCancelable(false);
        mProgress.setIndeterminate(true);

        binding.loginBtn.setOnClickListener(view -> {
            mProgress.show();
            String email = binding.emailEdtText.getText().toString();
            String password = binding.passEdtText.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                mProgress.dismiss();
                Toast.makeText(LoginActivity.this, "Please fill all the fields", Toast.LENGTH_LONG).show();
            } else {
                // Firebase call to log in
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, task -> {
                    mProgress.dismiss();
                    if (task.isSuccessful()) {
                        checkRoleIntent();
                    } else if (!isNetworkConnected())
                        Toast.makeText(LoginActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    else {
                        Toast.makeText(LoginActivity.this, "Incorrect Email ID/Password", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(e -> {
                    mProgress.dismiss();
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                });

            }
        });

        binding.signupBtn.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        binding.resetPassTv.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Function to send intent according to the user's Role -
    // Student - Attendance Activity
    // Faculty - Faculty Activity
    void checkRoleIntent() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firebase call to get user from Uer collection using email ID
        db.collection("Users").whereEqualTo("Email", Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        String role = (String) document.get("Role");
                        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("Role", role);
                        editor.apply();

                        // Sending intent accordingly
                        Intent intent;
                        if (Objects.equals(role, "Student")) {
                            addFaceToSP(mAuth.getCurrentUser().getEmail());
                            intent = new Intent(LoginActivity.this, AttendanceActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, FacultyStudent.class);
                        }
                        finish();
                        startActivity(intent);
                    }
                }
            }
        });
    }

    // Function that checks if face of student exists and store it in SharedPreferences
    void addFaceToSP(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Student").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                }
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        String image = document.getString("image");
                        if (image != null) {
                            SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("map", image);
                            editor.putString("name", document.getString("name"));
                            editor.apply();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
            }
        });
    }

    // Function to check if phone is connected to a network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}