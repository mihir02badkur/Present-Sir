package com.raina.PresentSir.authentication;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.raina.PresentSir.faculty.Student;

import java.util.HashMap;

public class UserViewModel extends ViewModel {
    private final String collection = "Users";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference userRef = db.collection(collection);
    private final String TAG = "Present Sir";


    // Create new user document in firebase
    void createNewUser(String uid, String email, String role) {
        HashMap<String, String> user = new HashMap<>();

        user.put("Email", email);
        user.put("Role", role);

        if (uid != null) {
            userRef.document(uid).set(user).addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully written!")).addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }
    }

    // Create new Student document in firebase
    void createNewStudent(String uid, String email, String rollNumber, String name, String image) {
        Student student = new Student(email, rollNumber, name, image);

        if (uid != null) {
            db.collection("Student").document(uid).set(student).addOnSuccessListener(unused -> Log.d(TAG, "DocumentSnapshot successfully written!")).addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
        }
    }

}
