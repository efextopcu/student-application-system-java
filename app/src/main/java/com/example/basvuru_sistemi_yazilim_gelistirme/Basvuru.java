package com.example.basvuru_sistemi_yazilim_gelistirme;

public class Basvuru {
    public String applicationType, applicationDate, applicant, state;

    public Basvuru(String applicationType, String applicationDate, String applicant, String state) {
        this.applicationType = applicationType;
        this.applicationDate = applicationDate;
        this.applicant = applicant;
        this.state = state;
    }
    public Basvuru () {

    }
}
