/**
 * Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
 */
package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.tools.TMDBUtils;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    private ContentValues[] mTrailers;
    private final TrailerOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface TrailerOnClickHandler {
        void onClick(String trailerSource);
    }

    /**
     * Creates a MovieAdapter.
     */
    public TrailerAdapter(TrailerOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a menu_main list item.
     */
    public class TrailerViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public final TextView mTrailerTitle;

        public TrailerViewHolder(View view) {
            super(view);
            mTrailerTitle = (TextView) view.findViewById(R.id.tv_trailer_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ContentValues trailer = mTrailers[getAdapterPosition()];
            String trailerSource = trailer.getAsString(TMDBUtils.FORMATTED_TRAILER_SOURCE);
            mClickHandler.onClick(trailerSource);
        }
    }

    /**
     * Initializes each visible view holder on the screen.
     */
    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.item_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);

        return new TrailerViewHolder(view);
    }

    /**
     * Displays the layout bound to each visible view.
     */
    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        ContentValues review = mTrailers[position];
        holder.mTrailerTitle.setText(review.getAsString(TMDBUtils.FORMATTED_TRAILER_TITLE));
    }

    /**
     * Returns the number of items.
     */
    @Override
    public int getItemCount() {
        if (mTrailers == null) return 0;
        return mTrailers.length;
    }

    /**
     * Refreshes the data held in the adapter.
     */
    public void setMovieTrailersData(ContentValues[] trailers) {
        mTrailers = trailers;
        notifyDataSetChanged();
    }
}