package com.ahsan.watertrackplus.base;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

public abstract class BaseFragment extends Fragment {
    
    protected boolean handleBackPress() {
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Setup back press handling
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
            new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (!handleBackPress()) {
                        setEnabled(false);
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
    }

    protected void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, 
                android.widget.Toast.LENGTH_SHORT).show();
        }
    }
} 