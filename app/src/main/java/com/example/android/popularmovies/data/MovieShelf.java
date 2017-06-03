package com.example.android.popularmovies.data;

import android.database.Cursor;

import com.example.android.popularmovies.tools.TMDBUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple POJO to hold an array of poster URLs and json data, for the MainActivity.
 */
public class MovieShelf {

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public String[] moviePosters;
    public JSONObject[] movieData;

    /**
     * Returns the minimum length of both the poster and movieData arrays.
     *
     * Since we always use both pieces of data, we wouldn't want to return a length that creates
     * an out of bounds error.
     */
    public int getCount() {
        return moviePosters.length;
    }

    /**
     * Creates a shelf based on a JSONArray, currently invoked for "Most Popular" and "Top Rated".
     */
    public MovieShelf(JSONArray movieListings) throws JSONException {
        int count = movieListings.length();
        this.moviePosters = new String[count];
        this.movieData = new JSONObject[count];
        try {
            for (int i = 0; i < count; i++) {
                JSONObject obj = movieListings.getJSONObject(i);
                this.movieData[i] = obj;
                this.moviePosters[i] = buildPosterUrl(obj.getString(TMDBUtils.MOVIE_POSTER));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a shelf based on a Cursor from FavoriteContentProvider, for "Favorites" view mode.
     */
    public MovieShelf(Cursor cursor) {
        int count = cursor.getCount();
        this.moviePosters = new String[count];
        this.movieData = new JSONObject[count];
        try {
            String jsonCacheColumn = FavoriteContract.FavoriteEntry.COLUMN_TMDB_CACHED_DATA;
            String posterUrlColumn = FavoriteContract.FavoriteEntry.COLUMN_TMDB_POSTER_URL;
            for (int i = 0; i < count; i++) {
                cursor.moveToPosition(i);
                String jsonSting = cursor.getString(cursor.getColumnIndex(jsonCacheColumn));
                this.movieData[i] = new JSONObject(jsonSting);
                this.moviePosters[i] = cursor.getString(cursor.getColumnIndex(posterUrlColumn));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a fully-qualified URL string for a poster image.
     */
    private String buildPosterUrl(String posterPath) {
        return IMAGE_BASE_URL + posterPath;
    }
}
