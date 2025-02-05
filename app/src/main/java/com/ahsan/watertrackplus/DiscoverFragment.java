package com.ahsan.watertrackplus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ahsan.watertrackplus.adapters.ArticleAdapter;
import com.ahsan.watertrackplus.base.BaseFragment;
import com.ahsan.watertrackplus.models.Article;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends BaseFragment {
    private View rootView;
    private RecyclerView rvArticles;
    private SwipeRefreshLayout swipeRefresh;
    private ArticleAdapter articleAdapter;
    private ShimmerFrameLayout shimmerLayout;
    private LinearLayout errorView;
    private TextView tvError;
    private ProgressBar loadMoreProgress;
    private ChipGroup chipGroupCategories;
    private String currentCategory = "all";
    private boolean isLoading = false;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupChipGroup();
        loadArticles(true);
    }

    private void initViews(View view) {
        rvArticles = view.findViewById(R.id.rvArticles);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        errorView = view.findViewById(R.id.errorView);
        tvError = view.findViewById(R.id.tvError);
        loadMoreProgress = view.findViewById(R.id.loadMoreProgress);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
    }

    private void setupRecyclerView() {
        articleAdapter = new ArticleAdapter(requireContext(), new ArticleAdapter.OnArticleClickListener() {
            @Override
            public void onArticleClick(Article article) {
                // Handle article click
                // TODO: Implement article detail view
            }

            @Override
            public void onLoadMore() {
                loadMoreArticles();
            }
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvArticles.setLayoutManager(layoutManager);
        rvArticles.setAdapter(articleAdapter);
        
        // Add scroll listener for pagination
        rvArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (!isLoading) {
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreArticles();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            loadArticles(false);
        });
    }

    private void setupChipGroup() {
        if (chipGroupCategories != null) {
            chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    currentCategory = chip.getText().toString().toLowerCase();
                    currentPage = 1;
                    loadArticles(true);
                }
            });
        }
    }

    private void loadArticles(boolean showShimmer) {
        if (!isAdded()) return;

        if (showShimmer) {
                shimmerLayout.setVisibility(View.VISIBLE);
                shimmerLayout.startShimmer();
            }

            isLoading = true;
        errorView.setVisibility(View.GONE);
        
        // Simulate network call
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            List<Article> articles = generateDummyArticles();
            updateUI(articles, true);
        }, 1500);
    }

    private void loadMoreArticles() {
        if (!isAdded() || isLoading) return;
        
        isLoading = true;
        loadMoreProgress.setVisibility(View.VISIBLE);
        
        // Simulate network call
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            List<Article> moreArticles = generateDummyArticles();
            updateUI(moreArticles, false);
        }, 1500);
    }

    private void updateUI(List<Article> articles, boolean isFirstPage) {
        if (!isAdded()) return;

        if (isFirstPage) {
            articleAdapter.setArticles(articles, true);
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    } else {
            articleAdapter.setArticles(articles, false);
                loadMoreProgress.setVisibility(View.GONE);
        }
        
        swipeRefresh.setRefreshing(false);
        isLoading = false;
        currentPage++;
    }

    private List<Article> generateDummyArticles() {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            String title = "Stay Hydrated: The Importance of Water";
            String description = "Learn why staying hydrated is crucial for your health and how much water you should drink daily.";
            String url = "https://example.com/article" + i;
            String imageUrl = "https://example.com/image.jpg";
            
            Article article = new Article(
                title,
                description,
                url,
                imageUrl,
                currentCategory
            );
            articles.add(article);
        }
        return articles;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (shimmerLayout != null) {
            shimmerLayout.stopShimmer();
        }
        handler.removeCallbacksAndMessages(null);
    }
} 