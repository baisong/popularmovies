package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.tools.TMDBUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerOnClickHandler {

    private String mDataJsonString;
    private ContentValues mMovieData;
    private boolean isFavorite;

    private RecyclerView mReviewRecyclerView;
    private RecyclerView mTrailerRecyclerView;
    private ReviewAdapter mReviewAdapter;
    private TrailerAdapter mTrailerAdapter;
    private Button mToggleFavorite;
    private Button mFavoriteBadge;

    private int mMovieId;
    private String mTitle;
    private String mReleaseDate;
    private String mMoviePoster;
    private String mVoteAverage;
    private String mPlotSynopsis;

    private TextView mTitleDisplay;
    private TextView mReleaseDisplay;
    private ImageView mPosterDisplay;
    private TextView mVoteDisplay;
    private TextView mPlotDisplay;

    /**
     * Initializes the activity and accesses data from the intent that launched this.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        isFavorite = false;
        mTitleDisplay = (TextView) findViewById(R.id.tv_display_title);
        mReleaseDisplay = (TextView) findViewById(R.id.tv_display_release_date);
        mPosterDisplay = (ImageView) findViewById(R.id.iv_display_movie_poster);
        mVoteDisplay = (TextView) findViewById(R.id.tv_display_vote_average);
        mPlotDisplay = (TextView) findViewById(R.id.tv_movie_synopsis);
        mToggleFavorite = (Button) findViewById(R.id.btn_add_favorite);
        mFavoriteBadge = (Button) findViewById(R.id.badge_favorite);
        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            if (launchIntent.hasExtra(Intent.EXTRA_TEXT)) {
                mDataJsonString = launchIntent.getStringExtra(Intent.EXTRA_TEXT);
                loadMovieDataFromIntent();
                mTitleDisplay.setText(mTitle);
                mReleaseDisplay.setText(this.formatReleaseDate(mReleaseDate));
                Picasso.with(getApplicationContext()).load(mMoviePoster).into(mPosterDisplay);
                mVoteDisplay.setText(getFormattedVote());
                mPlotDisplay.setText(mPlotSynopsis);
            }
        }

        mReviewRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mReviewRecyclerView.setNestedScrollingEnabled(false);
        mReviewAdapter = new ReviewAdapter();
        mReviewRecyclerView.setAdapter(mReviewAdapter);

        loadMovieReviewData();

        mTrailerRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);
        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mTrailerRecyclerView.setNestedScrollingEnabled(false);
        mTrailerAdapter = new TrailerAdapter(this);
        mTrailerRecyclerView.setAdapter(mTrailerAdapter);

        loadMovieTrailerData();
    }

    private void loadMovieDataFromIntent() {
        mMovieData = TMDBUtils.getMovieItemData(mDataJsonString);

        mMovieId = mMovieData.getAsInteger(TMDBUtils.MOVIE_ID);
        mTitle = mMovieData.getAsString(TMDBUtils.MOVIE_TITLE);
        mReleaseDate = mMovieData.getAsString(TMDBUtils.MOVIE_RELEASE_DATE);
        mMoviePoster = mMovieData.getAsString(TMDBUtils.MOVIE_POSTER);
        mVoteAverage = mMovieData.getAsString(TMDBUtils.MOVIE_VOTE_AVG);
        mPlotSynopsis = mMovieData.getAsString(TMDBUtils.MOVIE_SYNOPSIS);
    }

    /**
     * Builds an implicit intent to share the movie listing.
     */
    public void onClick(String trailerSource) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerSource)));
    }

    /**
     * Makes our vote average number look a little better.
     */
    private String getFormattedVote() {
        return mVoteAverage + "/10";
    }

    /**
     * Composes a brilliant social-media-ready post your friends will love.
     */
    private String getShareableText() {
        return mTitle + " (" + getFormattedVote() + "): " + mPlotSynopsis;
    }

    /**
     * Formats the release date like "Apr 2017"
     */
    private String formatReleaseDate(String dateString) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = fmt.parse(dateString);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM YYYY");
            return fmtOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown release date";
        }
    }

    /**
     * Inflates the detail menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * Executes the FetchMovieTask
     */
    private void loadMovieReviewData() {
        showMovieReviewsList();
        new FetchMovieReviewsTask().execute();
    }

    /**
     * Executes the FetchMovieTask
     */
    private void loadMovieTrailerData() {
        showMovieTrailersList();
        new FetchMovieTrailersTask().execute();
    }

    /**
     * Fetches a MovieShelf in the background.
     */
    private class FetchMovieDetailsTask extends AsyncTask<Integer, Void, String> {
        protected URL mDetailsUrl;
        protected ProgressBar mLoader;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoader.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... params) {
            if (mDetailsUrl == null) {
                return null;
            }
            //URL movieQueryUrl = TMDBUtils.buildUrl(params[0]);
            Log.d("popmovies_Details_URL", mDetailsUrl.toString());
            try {
                return TMDBUtils.getResponseFromHttpUrl(mDetailsUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mLoader.setVisibility(View.INVISIBLE);
        }
    }

    public class FetchMovieReviewsTask extends FetchMovieDetailsTask {
        @Override
        protected void onPreExecute() {
            mLoader = (ProgressBar) findViewById(R.id.pb_reviews_loader);
            mDetailsUrl = TMDBUtils.buildReviewsUrl(mMovieId);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            ContentValues[] data = TMDBUtils.getReviewsData(json);
            if (data != null) {
                if (data.length == 0) {
                    showReviewEmptyMessage();
                }
                else {
                    showMovieReviewsList();
                    mReviewAdapter.setMovieReviewsData(data);
                }
            } else {
                showReviewErrorMessage();
            }
        }
    }

    public class FetchMovieTrailersTask extends FetchMovieDetailsTask {
        @Override
        protected void onPreExecute() {
            mLoader = (ProgressBar) findViewById(R.id.pb_trailers_loader);
            mDetailsUrl = TMDBUtils.buildTrailersUrl(mMovieId);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            ContentValues[] data = TMDBUtils.getTrailersData(json);
            if (data != null) {
                if (data.length == 0) {
                    showTrailerEmptyMessage();
                }
                else {
                    showMovieTrailersList();
                    mTrailerAdapter.setMovieTrailersData(data);
                }
            } else {
                showTrailerErrorMessage();
            }
        }
    }

    private void showTrailerErrorMessage() {
        showDetailsErrorMessage(R.id.pb_trailers_loader, R.id.tv_trailers_error_message, R.string.error_message_trailers);
    }

    private void showReviewErrorMessage() {
        showDetailsErrorMessage(R.id.pb_reviews_loader, R.id.tv_reviews_error_message, R.string.error_message_reviews);
    }

    private void showTrailerEmptyMessage() {
        showDetailsErrorMessage(R.id.pb_trailers_loader, R.id.tv_trailers_error_message, R.string.empty_message_trailers);
    }

    private void showReviewEmptyMessage() {
        showDetailsErrorMessage(R.id.pb_reviews_loader, R.id.tv_reviews_error_message, R.string.empty_message_reviews);
    }

    private void showMovieTrailersList() {
        showDetailsRecyclerView(R.id.rv_trailers, R.id.tv_trailers_error_message);
    }

    private void showMovieReviewsList() {
        showDetailsRecyclerView(R.id.rv_reviews, R.id.tv_reviews_error_message);
    }

    private void showDetailsErrorMessage(int loaderId, int messageId, int messageStringId) {
        ProgressBar loader = (ProgressBar) findViewById(loaderId);
        TextView message = (TextView) findViewById(messageId);
        message.setText(getString(messageStringId));
        loader.setVisibility(View.INVISIBLE);
        message.setVisibility(View.VISIBLE);
    }


    /**
     *
     * @param recyclerViewId
     * @param messageId
     */
    private void showDetailsRecyclerView(int recyclerViewId, int messageId) {
        RecyclerView recyclerView = (RecyclerView) findViewById(recyclerViewId);
        TextView message = (TextView) findViewById(messageId);
        message.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void toggleFavorite(View view) {
        // Determine which is the new status
        String message = (isFavorite) ? "Removed from favorites" : "Added to favorites!";
        // Update the UI
        updateFavorite(!isFavorite);
        // Set the new value
        isFavorite = (!isFavorite);
        // Notify the user
        Toast toast = Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 16);
        toast.show();
    }

    private void updateFavorite(boolean becomingFavorite) {
        String btnText = "Add to favorites";
        int iconId = R.drawable.ic_not_favorite_24dp;
        int colorId = R.color.accent_body;
        int badgeVisibility = View.GONE;
        if (becomingFavorite) {
            btnText = "Remove";
            iconId = R.drawable.ic_remove_24dp;
            badgeVisibility = View.VISIBLE;
        }
        /**
         * android:drawableStart="@drawable/ic_not_favorite_24dp"
         android:drawableLeft="@drawable/ic_not_favorite_24dp"
         android:drawablePadding="12dip"
         */
        mToggleFavorite.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
        mToggleFavorite.setTextColor(ContextCompat.getColor(this, colorId));
        mToggleFavorite.setText(btnText);
        mFavoriteBadge.setVisibility(badgeVisibility);
    }
}