package com.ahsan.watertrackplus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ahsan.watertrackplus.base.BaseFragment;

public class HomeFragment extends BaseFragment {

    private TextView tvHealthScore;
    private TextView tvWaterIntake;
    private TextView tvGoal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvHealthScore = view.findViewById(R.id.tvHealthScore);
        tvWaterIntake = view.findViewById(R.id.tvWaterIntake);
        tvGoal = view.findViewById(R.id.tvGoal);

        // Load data
        loadData();
    }

    private void loadData() {
        // TODO: Load actual data from database/preferences
        tvHealthScore.setText("78");
        tvWaterIntake.setText("1.5L");
        tvGoal.setText("2.5L");
    }

    @Override
    protected boolean handleBackPress() {
        return false; // Let activity handle back press
    }
} 