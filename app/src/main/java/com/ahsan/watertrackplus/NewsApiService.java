package com.ahsan.watertrackplus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {
    @GET("everything")
    Call<NewsResponse> getArticles(
        @Query("q") String query,
        @Query("apiKey") String apiKey,
        @Query("pageSize") int pageSize,
        @Query("page") int page,
        @Query("sortBy") String sortBy,
        @Query("language") String language
    );
} 