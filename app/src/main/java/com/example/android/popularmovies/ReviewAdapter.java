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

    /**
     * Creates a ReviewAdapter.
     */
    public ReviewAdapter() {
        super();
    }

    /**
     * Cache of the children views for a item_review view.
     */
    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        public final TextView mAuthorName;
        public final TextView mReviewText;

        /**
         * Sets up the item view.
         */
        public ReviewViewHolder(View view) {
            super(view);
            mReviewText = (TextView) view.findViewById(R.id.tv_review_text);
            mAuthorName = (TextView) view.findViewById(R.id.tv_review_author);
        }
    }

    /**
     * Returns the number of items.
     */
    @Override
    public int getItemCount() {
        if (mReviews == null) return 0;
        return mReviews.length;
    }

    /**
     * Initializes each visible view holder on the screen.
     */
    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_review, parent, false);

        return new ReviewViewHolder(view);
    }

    /**
     * Displays the layout bound to each visible view.
     */
    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        ContentValues review = mReviews[position];
        holder.mReviewText.setText(review.getAsString(TMDBUtils.REVIEW_TEXT));
        holder.mAuthorName.setText(review.getAsString(TMDBUtils.REVIEW_AUTHOR));
    }

    /**
     * Refreshes the data held in the adapter.
     */
    public void setMovieReviewsData(ContentValues[] reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }
}
