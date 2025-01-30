package com.ahsan.watertrackplus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahsan.watertrackplus.R;
import com.ahsan.watertrackplus.models.BlogItem;

import java.util.ArrayList;
import java.util.List;

public class BlogAdapter extends RecyclerView.Adapter<BlogAdapter.BlogViewHolder> {

    private List<BlogItem> blogItems;
    private OnBlogItemClickListener listener;

    public interface OnBlogItemClickListener {
        void onBlogItemClick(BlogItem blogItem);
    }

    public BlogAdapter(OnBlogItemClickListener listener) {
        this.blogItems = new ArrayList<>();
        this.listener = listener;
    }

    public void setBlogItems(List<BlogItem> blogItems) {
        this.blogItems = blogItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blog, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogItem item = blogItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return blogItems.size();
    }

    class BlogViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBlogImage;
        private TextView tvCategory;
        private TextView tvTitle;
        private TextView tvLikes;
        private TextView tvReadMore;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBlogImage = itemView.findViewById(R.id.ivBlogImage);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvReadMore = itemView.findViewById(R.id.tvReadMore);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBlogItemClick(blogItems.get(position));
                }
            });
        }

        public void bind(BlogItem item) {
            tvCategory.setText(item.getCategory());
            tvTitle.setText(item.getTitle());
            tvLikes.setText(item.getLikes() + " votes");
            // Set placeholder image
            ivBlogImage.setImageResource(R.drawable.placeholder_image);
            // TODO: When you implement image loading library:
            // Glide.with(itemView.getContext())
            //     .load(item.getImageUrl())
            //     .placeholder(R.drawable.placeholder_image)
            //     .into(ivBlogImage);
        }
    }
} 