package com.ahsan.watertrackplus.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.models.Article;
import com.ahsan.watertrackplus.utils.MaterialToast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import com.google.android.material.button.MaterialButton;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {
    
    private List<Article> articles = new ArrayList<>();
    private boolean hasMoreItems = true;
    private final Context context;
    private final OnArticleClickListener listener;
    private long lastToastTime = 0;
    private static final long TOAST_DELAY = 3000; // 3 seconds between toasts

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
        void onLikeClick(Article article, int position);
        void onLoadMore();
    }

    public ArticleAdapter(Context context, OnArticleClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        
        holder.tvTitle.setText(article.getTitle());
        holder.tvLikes.setText(String.format("%d votes", article.getLikes()));
        
        // Load image with shimmer effect
        holder.imageShimmer.setVisibility(View.VISIBLE);
        holder.imageShimmer.startShimmer();
        holder.ivArticleImage.setVisibility(View.INVISIBLE);
        
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Glide.with(context)
                .load(article.getUrlToImage())
                .centerCrop()
                .timeout(15000) // 15 seconds timeout
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                              Target<Drawable> target, boolean isFirstResource) {
                        if (context instanceof AppCompatActivity && !((AppCompatActivity) context).isFinishing()) {
                            holder.imageShimmer.stopShimmer();
                            holder.imageShimmer.setVisibility(View.GONE);
                            holder.ivArticleImage.setVisibility(View.VISIBLE);
                            holder.ivArticleImage.setImageResource(R.drawable.placeholder_image);
                            
                            // Show error message with rate limiting
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastToastTime > TOAST_DELAY) {
                                if (e != null && e.getRootCauses().size() > 0) {
                                    Throwable rootCause = e.getRootCauses().get(0);
                                    if (rootCause instanceof SocketTimeoutException) {
                                        MaterialToast.showWarning(context, 
                                            "Slow connection detected. Some images may load slowly.");
                                    } else {
                                        MaterialToast.showError(context,
                                            "Check your connection. Some images may not load properly.");
                                    }
                                    lastToastTime = currentTime;
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                 Target<Drawable> target, DataSource dataSource,
                                                 boolean isFirstResource) {
                        holder.imageShimmer.stopShimmer();
                        holder.imageShimmer.setVisibility(View.GONE);
                        holder.ivArticleImage.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.placeholder_image)
                .into(holder.ivArticleImage);
        } else {
            holder.imageShimmer.stopShimmer();
            holder.imageShimmer.setVisibility(View.GONE);
            holder.ivArticleImage.setVisibility(View.VISIBLE);
            holder.ivArticleImage.setImageResource(R.drawable.placeholder_image);
        }
            
        // Handle click events
        holder.itemView.setOnClickListener(v -> listener.onArticleClick(article));
        holder.likeContainer.setOnClickListener(v -> listener.onLikeClick(article, position));
        holder.btnReadMore.setOnClickListener(v -> listener.onArticleClick(article));
        
        // Check if we need to load more items when reaching near the end
        if (position >= articles.size() - 3) {
            listener.onLoadMore();
        }
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<Article> newArticles, boolean isRefresh) {
        if (isRefresh) {
            articles.clear();
        }
        articles.addAll(newArticles);
        notifyDataSetChanged();
    }

    public void clearArticles() {
        articles.clear();
        notifyDataSetChanged();
    }

    public boolean getHasMoreItems() {
        return hasMoreItems;
    }

    public void setHasMoreItems(boolean hasMore) {
        this.hasMoreItems = hasMore;
    }

    public void updateLikes(int position, int newLikes) {
        if (position >= 0 && position < articles.size()) {
            articles.get(position).setLikes(newLikes);
            notifyItemChanged(position);
        }
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvLikes;
        ImageView ivArticleImage;
        View likeContainer;
        ShimmerFrameLayout imageShimmer;
        MaterialButton btnReadMore;

        ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            ivArticleImage = itemView.findViewById(R.id.ivArticleImage);
            likeContainer = itemView.findViewById(R.id.likeContainer);
            imageShimmer = itemView.findViewById(R.id.imageShimmer);
            btnReadMore = itemView.findViewById(R.id.tvReadMore);
        }
    }
} 