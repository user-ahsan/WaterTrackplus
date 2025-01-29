package com.ahsan.watertrackplus.models;

public class BlogItem {
    private String category;
    private String title;
    private String imageUrl;
    private int likes;

    public BlogItem(String category, String title, String imageUrl, int likes) {
        this.category = category;
        this.title = title;
        this.imageUrl = imageUrl;
        this.likes = likes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
} 