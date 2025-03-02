package com.sinhvien.livescore;

public class Competition {
    private String id;
    private String name;

    // Constructor không tham số (mặc định)
    public Competition() {
        // Constructor mặc định, có thể để trống
    }

    // Constructor có tham số
    public Competition(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
