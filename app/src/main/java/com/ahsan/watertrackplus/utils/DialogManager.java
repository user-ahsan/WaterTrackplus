package com.ahsan.watertrackplus.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.ahsan.watertrackplus.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Centralized manager for dialog operations
 */
public class DialogManager {
    private final Context context;
    private Dialog currentDialog;
    private Dialog loadingDialog;

    public DialogManager(@NonNull Context context) {
        this.context = context;
    }

    public void showDialog(@NonNull View dialogView) {
        dismissCurrentDialog();

        currentDialog = new Dialog(context);
        currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentDialog.setContentView(dialogView);
        
        if (currentDialog.getWindow() != null) {
            currentDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            currentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        currentDialog.show();
    }

    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(context);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            
            ProgressBar progressBar = new ProgressBar(context);
            loadingDialog.setContentView(progressBar);
            
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            
            loadingDialog.setCancelable(false);
        }
        
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void showConfirmationDialog(String title, String message, 
                                     Runnable onConfirm, Runnable onCancel) {
        new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm", (dialog, which) -> {
                if (onConfirm != null) onConfirm.run();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                if (onCancel != null) onCancel.run();
            })
            .show();
    }

    public void showErrorDialog(String title, String message) {
        new MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }

    public void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public void dismissCurrentDialog() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }
    }

    public void cleanup() {
        dismissCurrentDialog();
        dismissLoadingDialog();
        currentDialog = null;
        loadingDialog = null;
    }
} 