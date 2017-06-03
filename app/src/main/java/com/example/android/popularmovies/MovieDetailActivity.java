package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.data.FavoriteContract;
import com.example.android.popularmovies.data.FavoriteDbHelper;
import com.example.android.popularmovies.tools.TMDBUtils;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailActivity extends AppCompatActivity implements TrailerAdapter.TrailerOnClickHandler {

    private static final String LOG_TAG = "PopularMovies Detail";
    private static final String ACTION_ADD_FAVORITE = "add_favorite";
    private static final String ACTION_REMOVE_FAVORITE = "remove_favorite";
    private static final String MIME_TEXT = "text/plain";
    private static final String DATE_FORMAT_PARSE_RAW = "yyyy-MM-dd";
    private static final String DATE_FORMAT_DISPLAY = "MMM YYYY";
    private static final String SHARE_MOVIE_FORMAT = "%s (%s) %s";
    private static final String TOAST_MESSAGE_EMPTY = "";
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
    private int mFavoriteId;
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
     * Sets up the view, handles incoming intent data, and fetches fresh movie trailers & reviews.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitleDisplay = (TextView) findViewById(R.id.tv_display_title);
        mReleaseDisplay = (TextView) findViewById(R.id.tv_display_release_date);
        mPosterDisplay = (ImageView) findViewById(R.id.iv_display_movie_poster);
        mVoteDisplay = (TextView) findViewById(R.id.tv_display_vote_average);
        mPlotDisplay = (TextView) findViewById(R.id.tv_movie_synopsis);
        mToggleFavorite = (Button) findViewById(R.id.btn_add_favorite);
        mFavoriteBadge = (Button) findViewById(R.id.badge_favorite);

        Intent launchIntent = getIntent();
        if ((launchIntent != null) && (launchIntent.hasExtra(Intent.EXTRA_TEXT))) {
            mDataJsonString = launchIntent.getStringExtra(Intent.EXTRA_TEXT);
            loadMovieData(TMDBUtils.getMovieItemData(mDataJsonString));
            if (launchIntent.hasExtra(MainActivity.EXTRA_VIEW_MODE)) {
                int viewMode = launchIntent.getIntExtra(MainActivity.EXTRA_VIEW_MODE, -1);
                if (viewMode == TMDBUtils.MODE_LIST_FAVORITES) {
                    refreshCachedData();
                }
            }
            loadFavoriteInfo();
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

    /**
     * Used when activity is launched from cached data.
     *
     * Currently, this only occurs when the user clicks a movie tile from the "Favorites" list.
     */
    private void refreshCachedData() {
        new FetchMovieTask().execute();
    }

    /**
     * Queries the content provider for this movie's favorite status and updates the UI.
     */
    private void loadFavoriteInfo() {
        isFavorite = false;
        mFavoriteId = 0;
        Cursor cursor = getContentResolver().query(
                FavoriteContract.FavoriteEntry.CONTENT_URI,
                new String[]{FavoriteContract.FavoriteEntry.COLUMN_ID},
                FavoriteContract.FavoriteEntry.COLUMN_TMDB_ID + "=?",
                new String[]{String.valueOf(mMovieId)},
                FavoriteContract.FavoriteEntry.COLUMN_CREATED_TIMESTAMP + " DESC"
        );
        if (cursor != null) {
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                isFavorite = true;
                mFavoriteId = cursor.getInt(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_ID));
            }
            cursor.close();
        }
        refreshFavoriteActions();
    }

    /**
     * Updates the movie info views from a new ContentValues instance.
     *
     * This method is usually called with data retrieved from a launch intent, but can also come
     * from a background FetchMovieTask when we want to refresh possibly-stale content.
     */
    private void loadMovieData(ContentValues movieData) {
        mMovieData = movieData;

        mMovieId = mMovieData.getAsInteger(TMDBUtils.MOVIE_ID);
        mTitle = mMovieData.getAsString(TMDBUtils.MOVIE_TITLE);
        mReleaseDate = mMovieData.getAsString(TMDBUtils.MOVIE_RELEASE_DATE);
        mMoviePoster = mMovieData.getAsString(TMDBUtils.MOVIE_POSTER);
        mVoteAverage = mMovieData.getAsString(TMDBUtils.MOVIE_VOTE_AVG);
        mPlotSynopsis = mMovieData.getAsString(TMDBUtils.MOVIE_SYNOPSIS);

        mTitleDisplay.setText(mTitle);
        mReleaseDisplay.setText(this.formatReleaseDate(mReleaseDate));
        Picasso.with(getApplicationContext()).load(mMoviePoster).into(mPosterDisplay);
        mVoteDisplay.setText(getFormattedVote());
        mPlotDisplay.setText(mPlotSynopsis);
    }

    /**
     * Launches a FetchMovieTrailersTask in the background.
     */
    private void loadMovieTrailerData() {
        showMovieTrailersList();
        new FetchMovieTrailersTask().execute();
    }

    /**
     * Launches a FetchMovieReviewsTask in the background.
     */
    private void loadMovieReviewData() {
        showMovieReviewsList();
        new FetchMovieReviewsTask().execute();
    }

    /**
     * Handles trailer item click events to launch view intents.
     */
    public void onClick(String trailerSource) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerSource)));
    }

    /**
     * Inflates the menu to provide the share action.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    /**
     * Launches the share intent action when the item is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            String title = getString(R.string.share_movie_chooser_title);
            String message = formatShareMovieMessage();
            Intent intent = ShareCompat.IntentBuilder.from(this).setChooserTitle(title).setType(MIME_TEXT).setText(message).createChooserIntent();
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper class to define all themoviedb.com API background tasks for this activity.
     */
    private class FetchMovieDetailsTask extends AsyncTask<Integer, Void, String> {
        protected URL mApiEndpoint;
        protected ProgressBar mLoader;

        /**
         * Shows a ProgressBar.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoader.setVisibility(View.VISIBLE);
        }

        /**
         * Attempts to retrieve the payload from themoviedb.com.
         */
        @Override
        protected String doInBackground(Integer... params) {
            if (mApiEndpoint == null) {
                return null;
            }
            try {
                return TMDBUtils.getResponseFromHttpUrl(mApiEndpoint);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Hides a ProgressBar.
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mLoader.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Provides a background task to fetch reviews from themoviedb.com for the ReviewAdapter.
     */
    public class FetchMovieReviewsTask extends FetchMovieDetailsTask {
        /**
         * Sets up the reviews ProgressBar and API endpoint.
         */
        @Override
        protected void onPreExecute() {
            mLoader = (ProgressBar) findViewById(R.id.pb_reviews_loader);
            mApiEndpoint = TMDBUtils.buildReviewsUrl(mMovieId);
            super.onPreExecute();
        }

        /**
         * Handles the UI changes for the new data. Shows a list, empty message, or error message.
         */
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

    /**
     * Provides a background task to fetch trailers from themoviedb.com for the ReviewAdapter.
     */
    public class FetchMovieTrailersTask extends FetchMovieDetailsTask {
        /**
         * Sets up the trailers ProgressBar and API endpoint.
         */
        @Override
        protected void onPreExecute() {
            mLoader = (ProgressBar) findViewById(R.id.pb_trailers_loader);
            mApiEndpoint = TMDBUtils.buildTrailersUrl(mMovieId);
            super.onPreExecute();
        }

        /**
         * Handles the UI changes for the new data. Shows a list, empty message, or error message.
         */
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

    /**
     * Provides a background task to fetch the active movie's latest info from themoviedb.com.
     */
    public class FetchMovieTask extends FetchMovieDetailsTask {
        /**
         * Sets up the active movie's cache refresh ProgressBar and API endpoint.
         */
        @Override
        protected void onPreExecute() {
            mLoader = (ProgressBar) findViewById(R.id.pb_movie_loader);
            mApiEndpoint = TMDBUtils.buildMovieUrl(mMovieId);
            super.onPreExecute();
        }

        /**
         * Updates UI and notifies user only if the fresh data is different from the cached data.
         */
        @Override
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            ContentValues[] data = new ContentValues[]{TMDBUtils.getMovieItemData(json)};
            ContentValues movieData = data[0];
            if (data.length == 0) {
                displayToast(getString(R.string.fetch_movie_error_message));
            }
            else {
                if (!mMovieData.equals(movieData)) {
                    displayToast(getString(R.string.fetch_movie_success_new));
                    loadMovieData(movieData);
                }
            }
        }
    }

    /**
     * Handles button click action to add or remove the current movie as a favorite.
     */
    public void toggleFavorite(View view) {
        String action = (isFavorite) ? ACTION_REMOVE_FAVORITE : ACTION_ADD_FAVORITE;
        String toastMessage = TOAST_MESSAGE_EMPTY;
        if (action.equals(ACTION_ADD_FAVORITE)) {
            toastMessage = getString(R.string.toast_message_add_favorite_success);
            Uri uri = getContentResolver().insert(
                    FavoriteContract.FavoriteEntry.CONTENT_URI,
                    FavoriteDbHelper.prepareFavoriteFromJson(mDataJsonString));

            isFavorite = true;
            mFavoriteId = Integer.valueOf(uri.getPathSegments().get(1));
        }
        else if (action.equals(ACTION_REMOVE_FAVORITE)) {
            toastMessage = getString(R.string.toast_toggle_favorite_fail);
            Uri contentUri = FavoriteContract.FavoriteEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mFavoriteId))
                    .build();
            int itemsDeleted = getContentResolver().delete(contentUri, null, null);
            if (itemsDeleted == 1) {
                isFavorite = false;
                toastMessage = getString(R.string.toast_message_remove_favorite_success);
            }
        }

        refreshFavoriteActions();
        displayToast(toastMessage);
    }

    /**
     * Updates the buttons to either "☆ Add to favorites" or "✕ Remove - ★ Added to favorites".
     */
    private void refreshFavoriteActions() {
        String btnText = getString(R.string.action_label_add_favorite);
        int iconId = R.drawable.ic_not_favorite_24dp;
        int colorId = R.color.accent_body;
        int badgeVisibility = View.GONE;
        if (isFavorite) {
            btnText = getString(R.string.action_label_remove_favorite);
            iconId = R.drawable.ic_remove_24dp;
            badgeVisibility = View.VISIBLE;
        }
        mToggleFavorite.setCompoundDrawablesWithIntrinsicBounds(iconId, 0, 0, 0);
        mToggleFavorite.setTextColor(ContextCompat.getColor(this, colorId));
        mToggleFavorite.setText(btnText);
        mFavoriteBadge.setVisibility(badgeVisibility);
    }

    /**
     * Helper method to show a short toast at the top of the screen.
     */
    private void displayToast(String toastMessage) {
        Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.CENTER, 0, 16);
        toast.show();
    }

    /**
     * Shows the trailer load failure error message.
     */
    private void showTrailerErrorMessage() {
        showDetailsErrorMessage(R.id.pb_trailers_loader, R.id.tv_trailers_error_message, R.string.error_message_trailers);
    }

    /**
     * Shows the review load failure error message.
     */
    private void showReviewErrorMessage() {
        showDetailsErrorMessage(R.id.pb_reviews_loader, R.id.tv_reviews_error_message, R.string.error_message_reviews);
    }

    /**
     * Shows the empty trailer results message.
     */
    private void showTrailerEmptyMessage() {
        showDetailsErrorMessage(R.id.pb_trailers_loader, R.id.tv_trailers_error_message, R.string.empty_message_trailers);
    }
    /**
     * Shows the empty review results message.
     */
    private void showReviewEmptyMessage() {
        showDetailsErrorMessage(R.id.pb_reviews_loader, R.id.tv_reviews_error_message, R.string.empty_message_reviews);
    }

    /**
     * Shows the list of trailers.
     */
    private void showMovieTrailersList() {
        showDetailsRecyclerView(R.id.rv_trailers, R.id.tv_trailers_error_message);
    }

    /**
     * Shows the list of reviews.
     */
    private void showMovieReviewsList() {
        showDetailsRecyclerView(R.id.rv_reviews, R.id.tv_reviews_error_message);
    }

    /**
     * Helper method to enacpsulate similar error message methods.
     */
    private void showDetailsErrorMessage(int loaderId, int messageId, int messageStringId) {
        ProgressBar loader = (ProgressBar) findViewById(loaderId);
        TextView message = (TextView) findViewById(messageId);
        message.setText(getString(messageStringId));
        loader.setVisibility(View.INVISIBLE);
        message.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to enacpsulate similar RecyclerView display methods.
     */
    private void showDetailsRecyclerView(int recyclerViewId, int messageId) {
        RecyclerView recyclerView = (RecyclerView) findViewById(recyclerViewId);
        TextView message = (TextView) findViewById(messageId);
        message.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Helper method to format the vote average integer like "9/10".
     */
    private String getFormattedVote() {
        return mVoteAverage + getString(R.string.vote_average_suffix);
    }

    /**
     * Helper method to format the movie release date like "May 2017"
     */
    private String formatReleaseDate(String dateString) {
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT_PARSE_RAW);
        try {
            Date date = fmt.parse(dateString);
            SimpleDateFormat fmtOut = new SimpleDateFormat(DATE_FORMAT_DISPLAY);
            return fmtOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return getString(R.string.movie_release_error_value);
        }
    }

    /**
     * Helper method to format a shareable movie description.
     * @return
     */
    private String formatShareMovieMessage() {
        return String.format(SHARE_MOVIE_FORMAT, mTitle, getFormattedVote(), mPlotSynopsis);
    }
}