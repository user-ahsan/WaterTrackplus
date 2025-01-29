package com.ahsan.watertrackplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahsan.watertrackplus.adapters.BlogAdapter;
import com.ahsan.watertrackplus.base.BaseFragment;
import com.ahsan.watertrackplus.models.BlogItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.os.Handler;
import android.os.Looper;

public class DiscoverFragment extends BaseFragment implements BlogAdapter.OnBlogItemClickListener {

    private RecyclerView rvArticles;
    private BlogAdapter blogAdapter;
    private TextView tvDailyTip;
    private boolean isLoading = false;
    private int currentPage = 1;
    private boolean hasMorePages = true;
    private static final int ITEMS_PER_PAGE = 10;

    private final String[] dailyTips = {
        "Stay hydrated throughout the day by setting reminders and tracking your water intake.",
        "Drink a glass of water first thing in the morning to kickstart your metabolism.",
        "Keep a water bottle with you at all times to make hydration convenient.",
        "Set reminders every 2 hours to take a water break.",
        "Replace sugary drinks with water to improve your health and hydration."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        rvArticles = view.findViewById(R.id.rvArticles);
        tvDailyTip = view.findViewById(R.id.tvDailyTip);
        
        // Setup RecyclerView
        setupRecyclerView();
        
        // Set random daily tip
        setRandomDailyTip();
        
        // Load initial blog items
        loadBlogItems(false);
    }

    private void setRandomDailyTip() {
        Random random = new Random();
        int tipIndex = random.nextInt(dailyTips.length);
        tvDailyTip.setText(dailyTips[tipIndex]);
    }

    private void setupRecyclerView() {
        blogAdapter = new BlogAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvArticles.setLayoutManager(layoutManager);
        rvArticles.setAdapter(blogAdapter);

        // Add scroll listener for pagination
        rvArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && hasMorePages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadBlogItems(true);
                    }
                }
            }
        });
    }

    private void loadBlogItems(boolean isLoadingMore) {
        if (isLoading) return;
        
        isLoading = true;
        
        // Simulate network delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            List<BlogItem> mockItems = createMockItems();
            
            if (!isLoadingMore) {
                blogAdapter.setBlogItems(mockItems);
            } else {
                blogAdapter.addItems(mockItems);
            }
            
            currentPage++;
            hasMorePages = currentPage <= 3; // Limit to 3 pages for mock data
            isLoading = false;
        }, 1000);
    }

    private List<BlogItem> createMockItems() {
        List<BlogItem> items = new ArrayList<>();
        String[] categories = {"Nutrition", "Hydration", "Wellness", "Health", "Fitness"};
        String[] titles = {
            "Benefits of Drinking Water",
            "Best Times to Hydrate",
            "Water and Weight Loss",
            "Hydration for Athletes",
            "Water Quality Guide"
        };
        
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int index = i % titles.length;
            items.add(new BlogItem(
                categories[index],
                titles[index] + " - Part " + currentPage + "." + (i + 1),
                "https://picsum.photos/seed/" + (currentPage * 10 + i) + "/500/300",
                50 + new Random().nextInt(100)
            ));
        }
        
        return items;
    }

    @Override
    public void onBlogItemClick(BlogItem blogItem) {
        // TODO: Handle blog item click, e.g., open detail view
        Toast.makeText(requireContext(), "Clicked: " + blogItem.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected boolean handleBackPress() {
        return false; // Let activity handle back press
    }
} 