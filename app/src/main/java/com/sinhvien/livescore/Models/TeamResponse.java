package com.sinhvien.livescore.Models;

import com.google.gson.annotations.SerializedName;

public class TeamResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("crest")
    private String crestUrl; // URL logo đội bóng

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCrestUrl() {
        return crestUrl;
    }
}
