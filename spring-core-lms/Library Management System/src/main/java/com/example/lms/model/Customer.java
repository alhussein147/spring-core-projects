package com.example.lms.model;

import jakarta.persistence.Entity;

@Entity
public class Customer extends LibraryUser {

    private String membershipNumber;

    protected Customer() {
    }

    public Customer(String fullName, String email, String membershipNumber) {
        super(fullName, email);
        this.membershipNumber = membershipNumber;
    }

    public String getMembershipNumber() {
        return membershipNumber;
    }
}
