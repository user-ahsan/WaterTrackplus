package com.ahsan.watertrackplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.ahsan.watertrackplus.base.BaseFragment;

public class DiscoverFragment extends BaseFragment {

    private RecyclerView rvArticles;

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

        // Setup RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // TODO: Setup adapter and load articles
    }

    @Override
    protected boolean handleBackPress() {
        return false; // Let activity handle back press
    }
} 