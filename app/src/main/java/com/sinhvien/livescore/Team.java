package com.sinhvien.livescore;

public class Team {
    private String name;
    private String crestUrl;

    // Constructor không tham số (bắt buộc cho Firestore)
    public Team() {
        // Constructor trống
    }

    // Constructor với tham số (nếu cần)
    public Team(String name, String crestUrl) {
        this.name = name;
        this.crestUrl = crestUrl;
    }

    // Getters và Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCrestUrl() {
        return crestUrl;
    }

    public void setCrestUrl(String crestUrl) {
        this.crestUrl = crestUrl;
    }
}
