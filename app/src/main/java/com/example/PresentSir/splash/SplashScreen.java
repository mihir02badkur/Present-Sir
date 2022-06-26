package com.raina.PresentSir.splash;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.raina.PresentSir.R;
import com.raina.PresentSir.authentication.LoginActivity;
import com.raina.PresentSir.authentication.MainActivity;
import com.raina.PresentSir.faculty.FacultyStudent;
import com.raina.PresentSir.studentAttendance.AttendanceActivity;

import java.util.Objects;

public class SplashScreen extends AppCompatActivity {
    private static final String MyPREFERENCES = "MyPrefs";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mAuth = FirebaseAuth.getInstance();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // if user is logged out, send intent to login screen
            if (mAuth.getCurrentUser() == null) {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                finish();
                startActivity(intent);
            } else  // Else check and send intent accordingly
                checkRole();
        }, 1500);
    }

    // If student - attendance screen
    // If faculty - faculty screen
    void checkRole() {
        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        String defValue = "Student";
        String role = sharedPreferences.getString("Role", defValue);

        Intent intent;
        if (Objects.equals(role, "Student")) {
            addFaceToSP(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
            intent = new Intent(SplashScreen.this, AttendanceActivity.class);
        } else {
            intent = new Intent(SplashScreen.this, FacultyStudent.class);
        }
        finish();
        startActivity(intent);
    }

    // Function that checks if face of student exists and store it in SharedPreferences
    void addFaceToSP(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Student").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
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
                            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                            finish();
                            startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                        finish();
                        startActivity(intent);
                    }
                }
            }
        });
    }
}