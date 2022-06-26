package com.raina.PresentSir.faculty;

public class Student {
    public String email;
    public String rollNumber;
    public String name;
    public String image;

    public Student() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Student(String email, String rollNumber, String name, String image) {
        this.email = email;
        this.rollNumber = rollNumber;
        this.name = name;
        this.image = image;
    }

    // Getter Functions
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRollNumber() {
        return rollNumber;
    }
}
