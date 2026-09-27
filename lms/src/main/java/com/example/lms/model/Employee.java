package com.example.lms.model;

import jakarta.persistence.Entity;

@Entity
public class Employee extends LibraryUser {

    private String jobTitle;

    protected Employee() {
    }

    public Employee(String fullName, String email, String jobTitle) {
        super(fullName, email);
        this.jobTitle = jobTitle;
    }

    public String getJobTitle() {
        return jobTitle;
    }
}
