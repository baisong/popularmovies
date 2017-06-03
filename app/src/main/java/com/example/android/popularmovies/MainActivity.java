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
    private TextView mActiveSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mEmptyFavorites = (LinearLayout) findViewById(R.id.ll_empty_favorites);
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

    private void loadMovieData(int sortMethod) {
        showMovieGrid();
        new FetchMoviesTask().execute(sortMethod);
    }

    @Override
    public void onClick(JSONObject movieData) {
        Intent launchDetailIntent = new Intent(this, MovieDetailActivity.class);
        launchDetailIntent.putExtra(Intent.EXTRA_TEXT, movieData.toString());
        launchDetailIntent.putExtra(EXTRA_VIEW_MODE, getSort(this));
        startActivity(launchDetailIntent);
    }

    private void showMovieGrid() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        updateSort();
        mActiveSort.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mActiveSort.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void updateSort() {
        String[] labels = {
                getString(R.string.active_popular),
                getString(R.string.active_top_rated),
                getString(R.string.active_favorites)
        };
        int sort = getSort(this);
        if (sort == 0 || sort == 1 || sort == 2) {
            mActiveSort.setText(labels[sort]);
        }
    }

    public class FetchMoviesTask extends AsyncTask<Integer, Void, MovieShelf> {

        private int mViewMode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

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
            loadMovieData(getSort(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void viewMostPopular(View view) {
        updateViewMode(TMDBUtils.MODE_SORT_POPULAR);
    }

    public void viewTopRated(View view) {
        updateViewMode(TMDBUtils.MODE_SORT_TOP_RATED);
    }

    private void updateViewMode(int viewMode) {
        setSort(viewMode, this);
        loadMovieData(viewMode);
    }

    private void setSort(int newSort, Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.pref_active_sort), newSort);
        editor.commit();
    }

    private int getSort(Activity activity) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        // After all, the app is named "Popular Movies"
        int defaultSort = TMDBUtils.MODE_SORT_POPULAR;
        return sharedPref.getInt(getString(R.string.pref_active_sort), defaultSort);
    }
}