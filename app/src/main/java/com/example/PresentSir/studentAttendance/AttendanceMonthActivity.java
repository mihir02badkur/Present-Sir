package com.raina.PresentSir.studentAttendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.raina.PresentSir.R;
import com.raina.PresentSir.authentication.LoginActivity;
import com.raina.PresentSir.databinding.ActivityAttendanceMonthBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AttendanceMonthActivity extends AppCompatActivity {
    private static final String MyPREFERENCES = "MyPrefs";
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private ActivityAttendanceMonthBinding binding;
    private String date;
    private int currMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttendanceMonthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Month Attendance");
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

            if (id == R.id.nav_attendance) {
                Intent intent = new Intent(AttendanceMonthActivity.this, AttendanceActivity.class);
                startActivity(intent);
                binding.drawerLayout.closeDrawers();
            } else if (id == R.id.nav_logout) {
                // Sign out alert
                new AlertDialog.Builder(AttendanceMonthActivity.this, R.style.AlertDialogTheme)
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to Sign Out?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // Continue with sign out operation
                            FirebaseAuth.getInstance().signOut();
                            // Clear data from SharedPreferences
                            sharedPreferences.edit().clear().apply();
                            Intent intent = new Intent(AttendanceMonthActivity.this, LoginActivity.class);
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

        SimpleDateFormat curFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        date = curFormatter.format(new Date());
        currMonth = Integer.parseInt(date.substring(3, 5));

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"};

        // Starting activity with current month
        binding.month.setText(months[currMonth - 1]);
        binding.year.setText(date.substring(6));
        setSubjectAttendance(currMonth);

        binding.textSwitch.setInAnimation(AttendanceMonthActivity.this, android.R.anim.slide_in_left);
        binding.textSwitch.setOutAnimation(AttendanceMonthActivity.this, android.R.anim.slide_out_right);

        //Incrementing current month
        binding.nextMonth.setOnClickListener(view -> {
            currMonth++;
            // If index reaches maximum reset it
            if (currMonth > 12) {
                currMonth = 1;
            }

            // Updating month and attendance accordingly
            binding.textSwitch.setCurrentText(months[currMonth - 1]);
            setSubjectAttendance(currMonth);
        });

        //Decrementing current month
        binding.prevMonth.setOnClickListener(view -> {
            currMonth--;
            // If index reaches minimum reset it
            if (currMonth < 1) {
                currMonth = 12;
            }

            // Updating month and attendance accordingly
            binding.textSwitch.setCurrentText(months[currMonth - 1]);
            setSubjectAttendance(currMonth);
        });

    }

    // Update attendance of month displayed
    void setSubjectAttendance(int currMonth) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String CurrDate = "";

        if (currMonth < 10)
            CurrDate += "0";

        CurrDate += String.valueOf(currMonth);
        CurrDate += date.substring(5, 10);

        TextView[] subjects = {binding.subject1, binding.subject2, binding.subject3,
                binding.subject4, binding.subject5, binding.subject6};
        for (int i = 0; i < 6; i++)
            subjects[i].setText("0");

        // Firebase call to get count of student's attendance for a month
        String finalCurrDate = CurrDate;
        db.collection("Student").whereEqualTo("email", Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    String id = documentSnapshot.getId();
                    for (int i = 1; i < 7; i++) {
                        int finalI = i;
                        // Query to get subject having attendance marked for a day and add it to the result
                        db.collection("Student").document(id).collection("Attendance").whereEqualTo("subject" + i, 1).get().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                for (DocumentSnapshot document : task1.getResult()) {
                                    String docId = document.getId();
                                    String month = docId.substring(3, 10);

                                    if (month.equals(finalCurrDate)) {
                                        int val = Integer.parseInt(subjects[finalI - 1].getText().toString());
                                        val++;
                                        subjects[finalI - 1].setText(String.valueOf(val));
                                    }
                                }
                            }
                        });
                    }
                }
            } else if (!isNetworkConnected())
                Toast.makeText(AttendanceMonthActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
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