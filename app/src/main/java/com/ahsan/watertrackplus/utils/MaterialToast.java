package com.ahsan.watertrackplus.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import com.ahsan.watertrackplus.R;

public class MaterialToast {
    private static Toast currentToast;

    public static void show(Context context, String message, int iconRes) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        View toastView = LayoutInflater.from(context).inflate(R.layout.custom_toast, null);
        ImageView iconView = toastView.findViewById(R.id.toastIcon);
        TextView textView = toastView.findViewById(R.id.toastText);

        iconView.setImageResource(iconRes);
        textView.setText(message);

        currentToast = new Toast(context);
        currentToast.setDuration(Toast.LENGTH_SHORT);
        currentToast.setView(toastView);
        currentToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 64);
        currentToast.show();
    }

    public static void showError(Context context, String message) {
        show(context, message, R.drawable.ic_error);
    }

    public static void showInfo(Context context, String message) {
        show(context, message, R.drawable.ic_info);
    }

    public static void showSuccess(Context context, String message) {
        show(context, message, R.drawable.ic_success);
    }

    public static void showWarning(Context context, String message) {
        show(context, message, R.drawable.ic_warning);
    }
} 