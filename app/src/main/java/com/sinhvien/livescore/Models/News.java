package com.sinhvien.livescore.Models;

public class News {
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String publishedAt;
    private String source;

    public News(String title, String description, String url, String imageUrl, String publishedAt, String source) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedAt = publishedAt;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public String getSource() {
        return source;
    }
}