package com.raina.PresentSir.faculty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.raina.PresentSir.R;
import com.raina.PresentSir.authentication.LoginActivity;
import com.raina.PresentSir.databinding.ActivityFacultyStudentBinding;

public class FacultyStudent extends AppCompatActivity {
    private static final String MyPREFERENCES = "MyPrefs";
    StudentAdapter adapter; // Create Object of the Adapter class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFacultyStudentBinding binding = ActivityFacultyStudentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        // To display the Recycler view linearly
        binding.recyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        // query in the database to fetch appropriate data
        Query query = firebaseFirestore.collection("Student");

        FirestoreRecyclerOptions<Student> options
                = new FirestoreRecyclerOptions.Builder<Student>()
                .setQuery(query, Student.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new StudentAdapter(options, FacultyStudent.this);
        // Connecting Adapter class with the Recycler view*/
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setItemAnimator(null);
        adapter.notifyDataSetChanged();

        if (!isNetworkConnected())
            Toast.makeText(FacultyStudent.this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
    }

    // Function to tell the app to start getting
    // data from database on starting of the activity
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_faculty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Sign out alert
        if (id == R.id.action_signOut) {
            new AlertDialog.Builder(FacultyStudent.this, R.style.AlertDialogTheme)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to Sign Out?")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        // Continue with sign out operation
                        FirebaseAuth.getInstance().signOut();
                        // Clear SharedPreferences before sign out
                        deleteSharedPreference();
                        Intent intent = new Intent(FacultyStudent.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //  Function to check if phone is connected to a network
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    // Clear all data from SharedPreference
    void deleteSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}