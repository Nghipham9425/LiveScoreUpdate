package com.sinhvien.livescore.Models;

import com.google.firebase.firestore.PropertyName;

public class Team {
    private int id;
    private String name;
    private String shortName;
    private String tla;

    @PropertyName("crestUrl")
    private String crest; // Firebase sẽ ánh xạ "crestUrl" vào biến "crest"

    public Team() {
    }

    public Team(int id, String name, String shortName, String tla, String crest) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.tla = tla;
        this.crest = crest;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getTla() {
        return tla;
    }

    @PropertyName("crestUrl")
    public String getCrest() {
        return crest;
    }
}

