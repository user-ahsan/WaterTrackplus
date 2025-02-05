package com.ahsan.watertrackplus.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.ahsan.watertrackplus.R;

public class MaterialToast {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showWarning(Context context, String message) {
        View rootView = ((android.app.Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(context.getResources().getColor(R.color.progress_medium, context.getTheme()));
        snackbar.show();
    }

    public static void showError(Context context, String message) {
        View rootView = ((android.app.Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(context.getResources().getColor(R.color.progress_low, context.getTheme()));
        snackbar.show();
    }
}