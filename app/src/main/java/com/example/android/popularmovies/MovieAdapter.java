/**
 * Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
 */
package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.data.MovieShelf;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private MovieShelf mShelf;
    private final MovieAdapterOnClickHandler mClickHandler;

    /**
     * Provides an interface for onClickHandlers to pass along a JSONobject.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(JSONObject movieData);
    }

    /**
     * Requires that MovieAdapter instances provide a clickHandler.
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     *  Describes a movie item view and it's metadata.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public final ImageView mPosterImageView;

        /**
         * Sets up the poster ImageView and onClickHandler for launching the DetailActivity intent.
         */
        public MovieAdapterViewHolder(View view) {
            super(view);
            mPosterImageView = (ImageView) view.findViewById(R.id.iv_tile_poster);
            view.setOnClickListener(this);
        }

        /**
         * Passes the specified movie's JSONobject of metadata to the onClickHandler.
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            JSONObject movieData = mShelf.movieData[adapterPosition];
            mClickHandler.onClick(movieData);
        }
    }

    /**
     * Inflates the movie_tile view for each item.
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_tile;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);

        return new MovieAdapterViewHolder(view);
    }

    /**
     * Uses Picasso to load the poster into the item view.
     */
    @Override
    public void onBindViewHolder(MovieAdapterViewHolder movieAdapterViewHolder, int position) {
        String posterImageUrl = mShelf.moviePosters[position];
        Picasso.with(movieAdapterViewHolder.mPosterImageView.getContext()).load(posterImageUrl).into(movieAdapterViewHolder.mPosterImageView);
    }

    /**
     * Returns the current item count.
     */
    @Override
    public int getItemCount() {
        if (mShelf == null) return 0;
        return mShelf.getCount();
    }

    /**
     * Allows for new data to be loaded into the RecyclerView.
     */
    public void setMovieData(MovieShelf shelf) {
        mShelf = shelf;
        notifyDataSetChanged();
    }
}