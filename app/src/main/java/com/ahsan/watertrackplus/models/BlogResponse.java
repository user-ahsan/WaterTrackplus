package com.ahsan.watertrackplus.models;

import java.util.List;

public class BlogResponse {
    private List<BlogItem> data;
    private int currentPage;
    private int totalPages;
    private boolean hasMore;

    public List<BlogItem> getData() {
        return data;
    }

    public void setData(List<BlogItem> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
} 