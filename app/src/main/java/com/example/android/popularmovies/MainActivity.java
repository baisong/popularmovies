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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.MovieAdapter.MovieAdapterOnClickHandler;
import com.example.android.popularmovies.data.MovieShelf;
import com.example.android.popularmovies.tools.TMDBUtils;

import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler {

    private static final int MAIN_VIEW_GRID_COLUMNS = 2;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private TextView mActiveSort;

    /**
     * Initializes the main activity and loads movies.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mActiveSort = (TextView) findViewById(R.id.tv_active_sort);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_grid);

        GridLayoutManager layoutManager = new GridLayoutManager(this, MAIN_VIEW_GRID_COLUMNS);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        loadMovieData(getSort(this));
    }

    /**
     * Executes the FetchMovieTask
     */
    private void loadMovieData(int sortMethod) {
        showMovieGrid();
        new FetchMoviesTask().execute(sortMethod);
    }

    /**
     * Launches an explicit intent when a movie tile is clicked.
     */
    @Override
    public void onClick(JSONObject movieData) {
        Intent launchDetailIntent = new Intent(this, DetailActivity.class);
        launchDetailIntent.putExtra(Intent.EXTRA_TEXT, movieData.toString());
        startActivity(launchDetailIntent);
    }

    /**
     * Toggles the movie results on, and error message off.
     */
    private void showMovieGrid() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        updateSort();
        mActiveSort.setVisibility(View.VISIBLE);
    }

    /**
     * Toggles the error message on, and movie results off.
     */
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mActiveSort.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * Updates the TextView to display the active sort method.
     */
    private void updateSort() {
        String[] labels = {
                getString(R.string.active_popular),
                getString(R.string.active_top_rated)
        };
        int sort = getSort(this);
        if (sort == 0 || sort == 1) {
            mActiveSort.setText(labels[sort]);
        }
    }

    /**
     * Fetches a MovieShelf in the background.
     */
    public class FetchMoviesTask extends AsyncTask<Integer, Void, MovieShelf> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected MovieShelf doInBackground(Integer... params) {
            URL movieQueryUrl = TMDBUtils.buildUrl(params[0]);
            Log.d("Popular_Movies_URL", movieQueryUrl.toString());
            try {
                String json = TMDBUtils.getResponseFromHttpUrl(movieQueryUrl);
                MovieShelf shelf = TMDBUtils.getMovieTitlesFromJson(json);
                return shelf;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MovieShelf shelf) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (shelf != null) {
                showMovieGrid();
                mMovieAdapter.setMovieData(shelf);
            } else {
                showErrorMessage();
            }
        }
    }

    /**
     * Inflates the main menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Responds to menu item selection.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_popular) {
            setSort(TMDBUtils.SORT_POPULAR, this);
            loadMovieData(TMDBUtils.SORT_POPULAR);
            return true;
        }
        if (id == R.id.action_sort_top_rated) {
            setSort(TMDBUtils.SORT_TOP_RATED, this);
            loadMovieData(TMDBUtils.SORT_TOP_RATED);
            return true;
        }

        if (id == R.id.action_reload) {
            loadMovieData(getSort(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets the active sort method using SharedPreferences.
     */
    private void setSort(int newSort, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_active_sort), newSort);
        editor.commit();
    }

    /**
     * Gets the active sort method using SharedPreferences.
     */
    private int getSort(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        // After all, the app is named "Popular Movies"
        int defaultSort = TMDBUtils.SORT_POPULAR;
        return sharedPref.getInt(getString(R.string.pref_active_sort), defaultSort);
    }
}