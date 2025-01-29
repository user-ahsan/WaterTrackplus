package com.ahsan.watertrackplus.api;

import com.ahsan.watertrackplus.models.BlogResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BlogService {
    @GET("blogs")
    Call<BlogResponse> getBlogs(
        @Query("page") int page,
        @Query("per_page") int perPage
    );
} 