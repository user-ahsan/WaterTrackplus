package com.ahsan.watertrackplus.models;

public class Article {
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String category;
    private int likes;
    private String publishedAt;

    public Article(String title, String description, String url, String urlToImage, String category) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.category = category;
        this.likes = 0;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getUrlToImage() { return urlToImage; }
    public void setUrlToImage(String urlToImage) { this.urlToImage = urlToImage; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
} 