package com.example.android.popularmovies;

import android.content.ContentValues;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.tools.TMDBUtils;

/**
 * Adapters provide a binding from an app-specific data set to views that are displayed within a
 * RecyclerView.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private ContentValues[] mReviews;

    public ReviewAdapter() {
        super();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        public final TextView mAuthorName;
        public final TextView mReviewText;

        public ReviewViewHolder(View view) {
            super(view);
            mReviewText = (TextView) view.findViewById(R.id.tv_review_text);
            mAuthorName = (TextView) view.findViewById(R.id.tv_review_author);
        }
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) return 0;
        return mReviews.length;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_review, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        ContentValues review = mReviews[position];
        holder.mReviewText.setText(review.getAsString(TMDBUtils.REVIEW_TEXT));
        holder.mAuthorName.setText(review.getAsString(TMDBUtils.REVIEW_AUTHOR));
    }

    public void setMovieReviewsData(ContentValues[] reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }
}
