package com.example.basvuru_sistemi_yazilim_gelistirme;

import java.math.BigInteger;

public class User {
    public String email, name, surname, university, faculty, major, address, phoneNumber, dateOfBirth, password;
    public Integer year, studentNumber;
    public Long registryNumber;

    public User(String email, String name, String surname, String password, String university, String faculty, String major, String address, String phoneNumber, String dateOfBirth, Integer year, Long registryNumber, Integer studentNumber) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.university = university;
        this.faculty = faculty;
        this.major = major;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.year = year;
        this.registryNumber = registryNumber;
        this.studentNumber = studentNumber;
    }

    public User() {

    }
}
