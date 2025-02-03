package com.ahsan.watertrackplus;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.ahsan.watertrackplus.adapters.ArticleAdapter;
import com.ahsan.watertrackplus.models.Article;
import com.ahsan.watertrackplus.utils.MaterialToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
import java.util.List;
import java.io.IOException;
import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;
import android.widget.TextView;
import java.util.Random;
import java.util.Calendar;
import android.widget.ProgressBar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class DiscoverFragment extends Fragment implements ArticleAdapter.OnArticleClickListener {

    private static final String NEWS_API_KEY = "5209e61af2f44af0802acf72d17caaea";
    private static final String BASE_URL = "https://newsapi.org/v2/";
    private static final int ARTICLES_PER_PAGE = 15;
    private int currentPage = 1;
    private boolean isLoading = false;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvArticles;
    private ArticleAdapter articleAdapter;
    private View errorView;
    private ShimmerFrameLayout shimmerLayout;
    private ProgressBar loadMoreProgress;
    private NewsApiService newsApiService;
    private SharedPreferences sharedPreferences;
    private View rootView;
    private ChipGroup chipGroupCategories;
    private String currentCategory = "All";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        
        swipeRefresh = rootView.findViewById(R.id.swipeRefresh);
        rvArticles = rootView.findViewById(R.id.rvArticles);
        errorView = rootView.findViewById(R.id.errorView);
        shimmerLayout = rootView.findViewById(R.id.shimmerLayout);
        loadMoreProgress = rootView.findViewById(R.id.loadMoreProgress);
        chipGroupCategories = rootView.findViewById(R.id.chipGroupCategories);

        setupRecyclerView();
        setupSwipeRefresh();
        setupChipGroup();
        loadContent(true);
        
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("article_likes", Context.MODE_PRIVATE);

        // Initialize Retrofit
        setupRetrofit();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true;
                }
                // Check for slow connection
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                    MaterialToast.showWarning(requireContext(), 
                        "You're on a metered connection. Images may load slowly.");
                }
            }
        }
        return false;
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvArticles.setLayoutManager(layoutManager);
        articleAdapter = new ArticleAdapter(requireContext(), this);
        rvArticles.setAdapter(articleAdapter);
        
        // Add scroll listener for infinite scrolling and pull-up refresh
        rvArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean isLoadingMore = false;
            private int previousTotal = 0;
            private static final int VISIBLE_THRESHOLD = 5;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                // Reset loading state if total items changed
                if (totalItemCount > previousTotal) {
                    isLoadingMore = false;
                    previousTotal = totalItemCount;
                }

                // Load more when reaching near the end
                if (!isLoadingMore && !isLoading && 
                    (totalItemCount - lastVisibleItem) <= VISIBLE_THRESHOLD) {
                    onLoadMore();
                    isLoadingMore = true;
                }

                // Pull-up refresh when at the bottom
                if (!isLoading && dy < 0 && lastVisibleItem == totalItemCount - 1) {
                    if (articleAdapter.getHasMoreItems()) {
                        loadMoreProgress.setVisibility(View.VISIBLE);
                        currentPage = 1;
                        articleAdapter.clearArticles();
                        articleAdapter.setHasMoreItems(true);
                        loadContent(true);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                
                // Show appropriate message when at bottom
                if (!isLoading && newState == RecyclerView.SCROLL_STATE_IDLE &&
                    layoutManager.findLastCompletelyVisibleItemPosition() == articleAdapter.getItemCount() - 1) {
                    if (articleAdapter.getHasMoreItems()) {
                        MaterialToast.showInfo(requireContext(), "Pull up to refresh");
                    } else {
                        MaterialToast.showInfo(requireContext(), "No more articles available");
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.benitoite_dark);
        swipeRefresh.setOnRefreshListener(() -> {
            // Reset everything for fresh load
            currentPage = 1;
            articleAdapter.clearArticles();
            articleAdapter.setHasMoreItems(true);
            loadContent(true);
        });
    }

    private void setupChipGroup() {
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                currentCategory = chip.getText().toString();
                loadContent(true);
            }
        });
    }

    private void loadContent(boolean isRefresh) {
        if (getContext() == null) return;  // Add early return if context is null

        if (!isNetworkAvailable()) {
            swipeRefresh.setRefreshing(false);
            loadMoreProgress.setVisibility(View.GONE);
            showError("No internet connection. Please check your network and try again.");
            MaterialToast.showError(requireContext(), "No internet connection");
            return;
        }

        try {
            if (isRefresh) {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmer();
                rvArticles.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
            } else {
                loadMoreProgress.setVisibility(View.VISIBLE);
            }

            isLoading = true;

            // Create OkHttpClient with logging and timeouts
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new HttpLoggingInterceptor()
                            .setLevel(BuildConfig.DEBUG ? 
                                    HttpLoggingInterceptor.Level.BODY : 
                                    HttpLoggingInterceptor.Level.NONE))
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            // Create Retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            // Create service
            NewsApiService service = retrofit.create(NewsApiService.class);

            // Make API call
            Call<NewsResponse> call = service.getArticles(
                    currentCategory.equals("All") ? null : currentCategory.toLowerCase(),
                    NEWS_API_KEY,
                    ARTICLES_PER_PAGE,
                    currentPage,
                    "publishedAt",
                    "en"
            );

            call.enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                    if (getContext() == null) return;

                    try {
                        isLoading = false;
                        swipeRefresh.setRefreshing(false);
                        shimmerLayout.stopShimmer();
                        shimmerLayout.setVisibility(View.GONE);
                        loadMoreProgress.setVisibility(View.GONE);
                        rvArticles.setVisibility(View.VISIBLE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null && !articles.isEmpty()) {
                                articleAdapter.setArticles(articles, currentPage == 1);
                                errorView.setVisibility(View.GONE);
                                // Check if we've reached the last page
                                articleAdapter.setHasMoreItems(articles.size() >= ARTICLES_PER_PAGE);
                                currentPage++; // Increment page number for next load
                            } else {
                                if (currentPage == 1) {
                                    showError("No articles found for this category");
                                }
                                articleAdapter.setHasMoreItems(false);
                            }
                        } else {
                            String errorMsg = "Error loading articles. Please try again.";
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg = response.errorBody().string();
                                }
                            } catch (IOException e) {
                                Log.e("DiscoverFragment", "Error reading error body", e);
                            }
                            showError(errorMsg);
                            MaterialToast.showError(requireContext(), errorMsg);
                        }
                    } catch (Exception e) {
                        Log.e("DiscoverFragment", "Error processing response", e);
                        showError("An unexpected error occurred. Please try again.");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NewsResponse> call, @NonNull Throwable t) {
                    if (getContext() == null) return;

                    isLoading = false;
                    swipeRefresh.setRefreshing(false);
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    loadMoreProgress.setVisibility(View.GONE);

                    String errorMessage = "Network error. Please check your connection and try again.";
                    if (t instanceof IOException) {
                        errorMessage = "Network error. Please check your connection.";
                    } else {
                        Log.e("DiscoverFragment", "Error loading articles", t);
                    }
                    showError(errorMessage);
                    MaterialToast.showError(requireContext(), errorMessage);
                }
            });
        } catch (Exception e) {
            Log.e("DiscoverFragment", "Error in loadContent", e);
            isLoading = false;
            swipeRefresh.setRefreshing(false);
            if (shimmerLayout != null) {
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
            }
            if (loadMoreProgress != null) {
                loadMoreProgress.setVisibility(View.GONE);
            }
            showError("An unexpected error occurred. Please try again.");
        }
    }

    private void showError(String message) {
        if (getContext() == null || errorView == null) return;
        
        TextView tvError = errorView.findViewById(R.id.tvError);
        if (tvError != null) {
            tvError.setText(message);
        }
        errorView.setVisibility(View.VISIBLE);
        rvArticles.setVisibility(View.GONE);
    }

    private void setupRetrofit() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        
        // Add logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        clientBuilder.addInterceptor(logging);

        // Add timeout
        clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        newsApiService = retrofit.create(NewsApiService.class);
    }

    @Override
    public void onArticleClick(Article article) {
        if (article.getUrl() != null && !article.getUrl().isEmpty()) {
            openArticleInBrowser(article.getUrl());
        }
    }

    @Override
    public void onLoadMore() {
        if (!isLoading && articleAdapter.getHasMoreItems()) {
            loadContent(false);
        }
    }

    private void handleArticleLike(Article article, int position) {
        int currentLikes = article.getLikes();
        int newLikes = currentLikes + 1;
        
        // Save likes to SharedPreferences
        sharedPreferences.edit()
            .putInt("article_" + article.getUrl(), newLikes)
            .apply();

        
    }

    private void openArticleInBrowser(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    }
} 