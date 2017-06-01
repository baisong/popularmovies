package com.example.android.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Adapters provide a binding from an app-specific data set to views that are displayed within a
 * RecyclerView.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        public final TextView mReviewText;

        public ReviewViewHolder(View view) {
            super(view);
            mReviewText= (TextView) view.findViewById(R.id.tv_review_text);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {

    }
}
