package com.ahsan.watertrackplus;

import java.util.List;
import com.google.gson.annotations.SerializedName;
import com.ahsan.watertrackplus.models.Article;

public class NewsResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("totalResults")
    private int totalResults;

    @SerializedName("articles")
    private List<Article> articles;

    public String getStatus() {
        return status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<Article> getArticles() {
        return articles;
    }
} 