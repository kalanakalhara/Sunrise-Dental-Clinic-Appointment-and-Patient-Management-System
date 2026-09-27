package com.mycompany.sunrisedentalclinic.model;

import java.time.LocalTime;

public record Dentist(int id, String fullName, String specialization, String phone, String email,
        LocalTime startTime, LocalTime endTime, boolean available) {

    public Dentist(int id, String fullName, String specialization, String phone, String email,
            LocalTime startTime, LocalTime endTime) {
        this(id, fullName, specialization, phone, email, startTime, endTime, true);
    }

    @Override
    public String toString() {
        return fullName + (available ? "" : " (Unavailable)");
    }

    public String availability() {
        return available ? startTime + " - " + endTime : "Unavailable";
    }
}
