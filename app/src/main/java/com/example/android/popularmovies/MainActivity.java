package com.example.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.MovieAdapter.MovieAdapterOnClickHandler;
import com.example.android.popularmovies.data.MovieShelf;
import com.example.android.popularmovies.tools.TMDBUtils;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private static final int MAIN_VIEW_GRID_COLUMNS = 2;
    private static final String LOG_TAG = "PopularMovies Main";
    public  static final String EXTRA_VIEW_MODE = "viewmode";
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private LinearLayout mEmptyFavorites;
    private ProgressBar mLoadingIndicator;
    private TextView mActiveViewModeHeading;

    /**
     * Sets up the main RecyclerView and loads the proper movie data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mEmptyFavorites = (LinearLayout) findViewById(R.id.ll_empty_favorites);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mActiveViewModeHeading = (TextView) findViewById(R.id.tv_active_view_mode);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_grid);

        GridLayoutManager layoutManager = new GridLayoutManager(this, MAIN_VIEW_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        loadMovieData(getViewMode(this));
    }

    /**
     * Updates the RecyclerView to show the specified list of movies (remote sorted or favorites).
     */
    private void loadMovieData(int viewMode) {
        showMovieGrid();
        new FetchMoviesTask().execute(viewMode);
    }

    /**
     * Launches a detail intent, passing along item info as a JSON string.
     *
     * This intent also takes the view mode, since currently we launch favorites with data stored
     * at the time the movie was added to favorites. So, when the DetailActivity sees we came from
     * the favorites list, we'll run a background refresh of the data in case things have changed,
     * like the vote average, for example.
     */
    @Override
    public void onClick(JSONObject movieData) {
        Intent launchDetailIntent = new Intent(this, MovieDetailActivity.class);
        launchDetailIntent.putExtra(Intent.EXTRA_TEXT, movieData.toString());
        launchDetailIntent.putExtra(EXTRA_VIEW_MODE, getViewMode(this));
        startActivity(launchDetailIntent);
    }

    /**
     * Updates both the list and heading label, and hides any previous error message.
     */
    private void showMovieGrid() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        updateViewModeHeading();
        mActiveViewModeHeading.setVisibility(View.VISIBLE);
    }

    /**
     * Displays the error message and hides the movie list.
     */
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mActiveViewModeHeading.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Updates the heading label which shows the active view mode.
     */
    private void updateViewModeHeading() {
        String[] labels = {
                getString(R.string.active_popular),
                getString(R.string.active_top_rated),
                getString(R.string.active_favorites)
        };
        int viewMode = getViewMode(this);
        if (viewMode == 0 || viewMode == 1 || viewMode == 2) {
            mActiveViewModeHeading.setText(labels[viewMode]);
        }
    }

    /**
     * Loads the correct list of movies, given the view mode (popular, top rated, or favorites).
     */
    public class FetchMoviesTask extends AsyncTask<Integer, Void, MovieShelf> {

        private int mViewMode;

        /**
         * Show the loader before the task starts.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        /**
         * Retrieve the proper MovieShelf (a POJO containing an array of display and intent values).
         */
        @Override
        protected MovieShelf doInBackground(Integer... params) {
            mViewMode = params[0];
            switch (mViewMode) {
                case TMDBUtils.MODE_LIST_FAVORITES:
                    return TMDBUtils.buildFavoritesShelf(getApplicationContext());
                case TMDBUtils.MODE_SORT_POPULAR:
                case TMDBUtils.MODE_SORT_TOP_RATED:
                    return TMDBUtils.buildSortedShelf(mViewMode);
                default:
                    throw new UnsupportedOperationException();
            }
        }

        /**
         * Handles updating the UI depending on the result of the background task.
         */
        @Override
        protected void onPostExecute(MovieShelf shelf) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (shelf != null) {
                if (mViewMode == TMDBUtils.MODE_LIST_FAVORITES && shelf.getCount() == 0) {
                    mEmptyFavorites.setVisibility(View.VISIBLE);
                }
                else {
                    mEmptyFavorites.setVisibility(View.GONE);
                }
                showMovieGrid();
                mMovieAdapter.setMovieData(shelf);
            } else {
                showErrorMessage();
            }
        }
    }

    /**
     * Loads the menu containing the available view mode options.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles menu item selection by updating the data inside the RecyclerView.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_popular) {
            updateViewMode(TMDBUtils.MODE_SORT_POPULAR);
            return true;
        }
        if (id == R.id.action_sort_top_rated) {
            updateViewMode(TMDBUtils.MODE_SORT_TOP_RATED);
            return true;
        }
        if (id == R.id.action_list_favorites) {
            updateViewMode(TMDBUtils.MODE_LIST_FAVORITES);
            return true;
        }
        if (id == R.id.action_reload) {
            loadMovieData(getViewMode(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Provides an onClick method to activate "Most Popular" view mode.
     */
    public void viewMostPopular(View view) {
        updateViewMode(TMDBUtils.MODE_SORT_POPULAR);
    }

    /**
     * Provides an onClick method to activate "Top Rated" view mode.
     */
    public void viewTopRated(View view) {
        updateViewMode(TMDBUtils.MODE_SORT_TOP_RATED);
    }

    /**
     * Helper function for settings menu actions and button actions relating to view mode changes.
     */
    private void updateViewMode(int viewMode) {
        setViewMode(viewMode, this);
        loadMovieData(viewMode);
    }

    /**
     * Sets the active view mode as a SharedPreferences key-value to retain state.
     */
    private void setViewMode(int newViewMode, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_active_view_mode), newViewMode);
        editor.commit();
    }

    /**
     * Gets the active view mode from SharedPreferences.
     */
    private int getViewMode(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        // After all, the app is named "Popular Movies"
        int defaultViewMode = TMDBUtils.MODE_SORT_POPULAR;
        return sharedPref.getInt(getString(R.string.pref_active_view_mode), defaultViewMode);
    }
}