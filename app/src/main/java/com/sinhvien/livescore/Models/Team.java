package com.sinhvien.livescore.Models;

public class Team {
    private int id;
    private String name;
    private String shortName;
    private String tla;
    private String crestUrl;

    public Team() {
        // Required empty constructor for Firestore
    }

    public Team(String name, String shortName, String tla, String crestUrl) {
        this.name = name;
        this.shortName = shortName;
        this.tla = tla;
        this.crestUrl = crestUrl;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getTla() {
        return tla;
    }

    public void setTla(String tla) {
        this.tla = tla;
    }

    public String getCrest() {
        return crestUrl;
    }

    public void setCrest(String crestUrl) {
        this.crestUrl = crestUrl;
    }
}