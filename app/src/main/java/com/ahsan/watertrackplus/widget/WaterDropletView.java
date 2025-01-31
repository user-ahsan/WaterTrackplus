package com.ahsan.watertrackplus.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class WaterDropletView extends View {
    private Paint dropletPaint;
    private Paint waterPaint;
    private Path dropletPath;
    private float progress = 0f;
    private float targetProgress = 0f;
    private ValueAnimator progressAnimator;
    private boolean isDarkMode = false;

    public WaterDropletView(Context context) {
        super(context);
        init();
    }

    public WaterDropletView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        dropletPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dropletPaint.setStyle(Paint.Style.STROKE);
        dropletPaint.setStrokeWidth(4f);
        
        waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaint.setStyle(Paint.Style.FILL);
        
        dropletPath = new Path();
        
        progressAnimator = ValueAnimator.ofFloat(0f, 0f);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    public void setDarkMode(boolean darkMode) {
        isDarkMode = darkMode;
        updateColors();
        invalidate();
    }

    private void updateColors() {
        if (isDarkMode) {
            dropletPaint.setColor(Color.argb(180, 255, 255, 255));
        } else {
            dropletPaint.setColor(Color.argb(180, 0, 0, 0));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createDropletPath(w, h);
        updateWaterGradient(w, h);
    }

    private void createDropletPath(int width, int height) {
        dropletPath.reset();
        float centerX = width / 2f;
        float dropletHeight = height * 0.9f;
        float dropletWidth = width * 0.8f;
        
        dropletPath.moveTo(centerX, height * 0.1f);
        dropletPath.cubicTo(
            centerX - dropletWidth / 2, height * 0.3f,
            centerX - dropletWidth / 2, height * 0.7f,
            centerX, dropletHeight
        );
        dropletPath.cubicTo(
            centerX + dropletWidth / 2, height * 0.7f,
            centerX + dropletWidth / 2, height * 0.3f,
            centerX, height * 0.1f
        );
    }

    private void updateWaterGradient(int width, int height) {
        int startColor = isDarkMode ? Color.argb(200, 33, 150, 243) : Color.argb(200, 3, 169, 244);
        int endColor = isDarkMode ? Color.argb(200, 13, 71, 161) : Color.argb(200, 2, 119, 189);
        
        waterPaint.setShader(new LinearGradient(
            width / 2f, height,
            width / 2f, height * (1 - progress),
            startColor, endColor,
            Shader.TileMode.CLAMP
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw water fill
        canvas.save();
        canvas.clipPath(dropletPath);
        float waterHeight = getHeight() * progress;
        canvas.drawRect(0, getHeight() - waterHeight, getWidth(), getHeight(), waterPaint);
        canvas.restore();
        
        // Draw droplet outline
        canvas.drawPath(dropletPath, dropletPaint);
    }

    public void setProgress(float newProgress) {
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        
        targetProgress = Math.min(1f, Math.max(0f, newProgress));
        progressAnimator.setFloatValues(progress, targetProgress);
        progressAnimator.start();
        
        updateWaterGradient(getWidth(), getHeight());
    }

    public float getProgress() {
        return progress;
    }
} 