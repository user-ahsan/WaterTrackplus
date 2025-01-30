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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        rvArticles = view.findViewById(R.id.rvArticles);
        errorView = view.findViewById(R.id.errorView);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        loadMoreProgress = view.findViewById(R.id.loadMoreProgress);

        setupRecyclerView();
        setupSwipeRefresh();
        loadContent(true);
        
        return view;
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
                    loadMoreProgress.setVisibility(View.VISIBLE);
                    currentPage = 1;
                    articleAdapter.clearArticles();
                    articleAdapter.setHasMoreItems(true);
                    loadContent(true);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                
                // Show pull-up refresh hint when at bottom
                if (!isLoading && newState == RecyclerView.SCROLL_STATE_IDLE &&
                    layoutManager.findLastCompletelyVisibleItemPosition() == articleAdapter.getItemCount() - 1) {
                    MaterialToast.showInfo(requireContext(), "Pull up to refresh");
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

    private void loadContent(boolean isRefresh) {
        if (!isNetworkAvailable()) {
            swipeRefresh.setRefreshing(false);
            loadMoreProgress.setVisibility(View.GONE);
            showError("No internet connection. Please check your network and try again.");
            MaterialToast.showError(requireContext(), "No internet connection");
            return;
        }

        if (isRefresh) {
            shimmerLayout.setVisibility(View.VISIBLE);
            shimmerLayout.startShimmer();
            rvArticles.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        } else {
            loadMoreProgress.setVisibility(View.VISIBLE);
        }

        isLoading = true;
        long startTime = System.currentTimeMillis();

        // Create OkHttpClient with logging and timeouts
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
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
        service.getArticles(
                "water health hydration wellness drinking-water",
                NEWS_API_KEY,
                ARTICLES_PER_PAGE,
                currentPage,
                "publishedAt",
                "en"
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call, @NonNull Response<NewsResponse> response) {
                if (getContext() == null) return;

                isLoading = false;
                swipeRefresh.setRefreshing(false);
                shimmerLayout.stopShimmer();
                shimmerLayout.setVisibility(View.GONE);
                loadMoreProgress.setVisibility(View.GONE);

                // Check loading time
                long loadTime = System.currentTimeMillis() - startTime;
                if (loadTime > 5000) { // If loading took more than 5 seconds
                    MaterialToast.showWarning(requireContext(), 
                        "Slow internet connection detected. Some images may take longer to load.");
                }

                if (response.isSuccessful() && response.body() != null && response.body().getArticles() != null) {
                    List<Article> articles = response.body().getArticles();
                    if (!articles.isEmpty()) {
                        rvArticles.setVisibility(View.VISIBLE);
                        errorView.setVisibility(View.GONE);
                        articleAdapter.setArticles(articles, isRefresh);
                        currentPage++;
                    } else {
                        articleAdapter.setHasMoreItems(false);
                        if (isRefresh) {
                            showError("No articles found. Please try again later.");
                        } else {
                            MaterialToast.showInfo(requireContext(), "No more articles to load");
                        }
                    }
                } else {
                    if (isRefresh) {
                        showError("Failed to load articles. Please check your connection and try again.");
                    } else {
                        MaterialToast.showError(requireContext(), "Failed to load more articles");
                    }
                    articleAdapter.setHasMoreItems(false);
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

                String errorMessage = "Network error: ";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage += "Connection timed out. Please check your internet speed.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMessage += "No internet connection.";
                } else {
                    errorMessage += "Please check your connection and try again.";
                }

                if (isRefresh) {
                    showError(errorMessage);
                } else {
                    MaterialToast.showError(requireContext(), errorMessage);
                }
                articleAdapter.setHasMoreItems(false);
            }
        });
    }

    private void showError(String message) {
        rvArticles.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
        TextView tvError = errorView.findViewById(R.id.tvError);
        tvError.setText(message);
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

    private void openArticleInBrowser(String url) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
    }

    @Override
    public void onArticleClick(Article article) {
        openArticleInBrowser(article.getUrl());
    }

    @Override
    public void onLikeClick(Article article, int position) {
        handleArticleLike(article, position);
    }

    @Override
    public void onLoadMore() {
        if (!isLoading) {
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

        // Update UI
        articleAdapter.updateLikes(position, newLikes);
    }
} 