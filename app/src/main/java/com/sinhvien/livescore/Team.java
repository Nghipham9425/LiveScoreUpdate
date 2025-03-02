package com.sinhvien.livescore;

public class Team {
    private String name;
    private String crest;

    // Constructor
    public Team(String name, String crest) {
        this.name = name;
        this.crest = crest;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getCrest() {
        return crest;
    }

    // Optional: Setter methods nếu cần thay đổi các giá trị sau này
    public void setName(String name) {
        this.name = name;
    }

    public void setCrest(String crest) {
        this.crest = crest;
    }
}
