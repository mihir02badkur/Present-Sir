package com.raina.PresentSir.studentAttendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.raina.PresentSir.R;
import com.raina.PresentSir.authentication.LoginActivity;
import com.raina.PresentSir.databinding.ActivityAttendanceBinding;
import com.raina.PresentSir.faceRecognition.RecognizeFaceActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AttendanceActivity extends AppCompatActivity {
    private static final String MyPREFERENCES = "MyPrefs";
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private ActivityAttendanceBinding binding;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttendanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        setSupportActionBar(binding.toolbar);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                // Update navigation header text
                View navHeader = binding.navigationView.getHeaderView(0);
                TextView headerName = navHeader.findViewById(R.id.headerText);
                String nameText = "Hello " + sharedPreferences.getString("name", "User") + " !";
                headerName.setText(nameText);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }
        };

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        actionBarDrawerToggle.setDrawerSlideAnimationEnabled(true);

        // On selecting navigation drawer item
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_month_attendance) {
                Intent intent = new Intent(AttendanceActivity.this, AttendanceMonthActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
            } else if (id == R.id.nav_logout) {
                // Sign out alert
                new AlertDialog.Builder(AttendanceActivity.this, R.style.AlertDialogTheme)
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to Sign Out?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // Continue with sign out operation
                            FirebaseAuth.getInstance().signOut();
                            // Clear data from SharedPreferences
                            sharedPreferences.edit().clear().apply();
                            Intent intent = new Intent(AttendanceActivity.this, LoginActivity.class);
                            finishAffinity();
                            startActivity(intent);
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                binding.drawerLayout.closeDrawers();
            }
            return true;
        });

        ImageView[] subjects = {binding.subjectName, binding.subjectName1, binding.subjectName2,
                binding.subjectName3, binding.subjectName4, binding.subjectName5};

        for (int i = 0; i < 6; i++) {
            subjects[i].setClipToOutline(true);
        }

        for (int i = 0; i < 6; i++) {
            int finalI = i;
            subjects[i].setOnClickListener(view -> checkAttendance(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail(), "subject" + (finalI + 1)));
        }

        if (!isNetworkConnected())
            Toast.makeText(AttendanceActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
    }

    // Check if attendance for selected subject is already marked for the day
    void checkAttendance(String email, String subject) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firebase call to get student's attendance details
        db.collection("Student").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.exists()) {
                        Log.d("Document: ", document.getId() + " " + document.getData());
                        SimpleDateFormat curFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String date = curFormatter.format(new Date());
                        String id = document.getId();

                        // Firebase call to get document of current date
                        db.collection("Student").document(id).collection("Attendance").document(date).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                if (task1.getResult().exists()) {
                                    Long attendance = (Long) task1.getResult().get(subject);
//                                  // Send intent only if attendance is not marked
                                    if (attendance != null && attendance == 0) {
                                        Intent intent = new Intent(AttendanceActivity.this, RecognizeFaceActivity.class);
                                        intent.putExtra("Subject", subject);
                                        startActivity(intent);
                                    } else
                                        Snackbar.make(binding.getRoot(), "Attendance already marked", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Intent intent = new Intent(AttendanceActivity.this, RecognizeFaceActivity.class);
                                    intent.putExtra("Subject", subject);
                                    startActivity(intent);
                                }

                            }
                        });

                    }
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //  Function to check if phone is connected to a network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}