package com.sinhvien.livescore.Models;

public class Competition {
    private int id;
    private String name;
    private String code;
    private String emblem;

    // Constructor không tham số (mặc định)
    public Competition() {
        // Constructor mặc định, có thể để trống
    }

    // Constructor có tham số
    public Competition(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getEmblem() { return emblem; }
    public void setEmblem(String emblem) { this.emblem = emblem; }
}
