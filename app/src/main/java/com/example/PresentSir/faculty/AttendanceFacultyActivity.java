package com.raina.PresentSir.faculty;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.raina.PresentSir.databinding.ActivityAttendanceFacultyBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AttendanceFacultyActivity extends AppCompatActivity {
    private ActivityAttendanceFacultyBinding binding;
    private String date;
    private int currMonth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAttendanceFacultyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Month Attendance");

        email = getIntent().getStringExtra("email");

        SimpleDateFormat curFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        date = curFormatter.format(new Date());
        currMonth = Integer.parseInt(date.substring(3, 5));

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December"};

        // Starting activity with current month
        binding.month.setText(months[currMonth - 1]);
        binding.year.setText(date.substring(6));
        setSubjectAttendance(currMonth);

        binding.textSwitch.setInAnimation(AttendanceFacultyActivity.this, android.R.anim.slide_in_left);
        binding.textSwitch.setOutAnimation(AttendanceFacultyActivity.this, android.R.anim.slide_out_right);

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
        db.collection("Student").whereEqualTo("email", email).get().addOnCompleteListener(task -> {
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
                Toast.makeText(AttendanceFacultyActivity.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
        });
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
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