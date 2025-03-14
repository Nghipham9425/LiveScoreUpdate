package com.sinhvien.livescore.Models;

public class Team {
    private String name;
    private String crestUrl;

    public Team() {
        // Required empty constructor for Firestore
    }

    public Team(String name, String crestUrl) {
        this.name = name;
        this.crestUrl = crestUrl;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCrest() {
        return crestUrl;
    }

    public void setCrest(String crestUrl) {
        this.crestUrl = crestUrl;
    }
}