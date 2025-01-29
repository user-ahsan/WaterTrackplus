package com.ahsan.watertrackplus;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ahsan.watertrackplus.base.BaseFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class HomeFragment extends BaseFragment {
    
    private TextView tvHealthScore;
    private ChipGroup chipGroup;
    private Dialog customDialog;

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
        setupChips(view);

        // Load data
        loadData();
    }

    private void setupChips(View view) {
        Chip chip150ml = view.findViewById(R.id.chip150ml);
        Chip chip250ml = view.findViewById(R.id.chip250ml);
        Chip chip500ml = view.findViewById(R.id.chip500ml);
        Chip chipCustom = view.findViewById(R.id.chipCustom);

        // Set click listeners
        chip150ml.setOnClickListener(v -> handleAmountSelection(150));
        chip250ml.setOnClickListener(v -> handleAmountSelection(250));
        chip500ml.setOnClickListener(v -> handleAmountSelection(500));
        chipCustom.setOnClickListener(v -> showCustomAmountDialog());
    }

    private void handleAmountSelection(int amount) {
        // TODO: Handle the amount selection
    }

    private void showCustomAmountDialog() {
        if (getContext() == null) return;

        customDialog = new Dialog(getContext());
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        customDialog.setContentView(R.layout.dialog_custom_amount);
        
        if (customDialog.getWindow() != null) {
            customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            customDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Initialize dialog views
        TextInputEditText etAmount = customDialog.findViewById(R.id.etAmount);
        ChipGroup chipGroupSuggestions = customDialog.findViewById(R.id.chipGroupSuggestions);
        MaterialButton btnCancel = customDialog.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = customDialog.findViewById(R.id.btnConfirm);

        // Set click listeners
        btnCancel.setOnClickListener(v -> customDialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String amount = etAmount.getText().toString();
            if (!amount.isEmpty()) {
                handleAmountSelection(Integer.parseInt(amount));
                customDialog.dismiss();
            }
        });

        // Handle chip suggestions
        chipGroupSuggestions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip400) {
                etAmount.setText("400");
            } else if (checkedId == R.id.chip750) {
                etAmount.setText("750");
            } else if (checkedId == R.id.chip1000) {
                etAmount.setText("1000");
            }
        });

        customDialog.show();
    }

    private void loadData() {
        // TODO: Load actual data from database/preferences
        if (tvHealthScore != null) {
            tvHealthScore.setText("78");
        }
    }

    @Override
    protected boolean handleBackPress() {
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
            return true;
        }
        return false;
    }
} 