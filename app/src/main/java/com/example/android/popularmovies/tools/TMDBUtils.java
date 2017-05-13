package com.example.android.popularmovies.tools;

import android.net.Uri;
import android.util.Log;

import com.example.android.popularmovies.data.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the weather servers.
 */
public final class TMDBUtils {

    public static final int SORT_POPULAR = 0;
    public static final int SORT_TOP_RATED = 1;
    private static final String LOG_TAG = TMDBUtils.class.getSimpleName();

    public static final String TITLE = "original_title";
    public static final String POSTER = "poster_path";
    public static final String VOTE_AVG = "vote_average";
    public static final String SYNOPSIS = "overview";
    public static final String RELEASE_DATE = "release_date";

    private static final String API_BASE_URL = "https://api.themoviedb.org";
    private static final String API_POPULAR_PATH = "/movie/popular";
    private static final String API_TOP_RATED_PATH = "/movie/top_rated";
    private static final String LANG_PARAM = "language";
    private static final String defaultLang = "en-US";
    private static final String API_KEY_PARAM = "api_key";
    private static final String TMDB_RESULTS = "results";
    private static final String TMDB_STATUS_CODE = "status_code";
    private static final int API_VERSION = 3;

    /**
     * Builds the URL for querying movies using the specified sort method.
     */
    public static URL buildUrl(int sortMethod) {
        Log.d("Popular_Movies_URL:", String.valueOf(sortMethod));
        // Default to popular, even if we have an invalid sortMethod
        // After all, the app is named "Popular Movies"
        String path = API_POPULAR_PATH;
        if (sortMethod == SORT_TOP_RATED) {
            path = API_TOP_RATED_PATH;
        }
        String baseUrl = API_BASE_URL
                        + "/"
                        + String.valueOf(API_VERSION)
                        + path;
        String tmdbApiKey = Constants.TMDB_API_KEY;
        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, tmdbApiKey)
                .appendQueryParameter(LANG_PARAM, defaultLang)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(LOG_TAG, "Built URI " + url);

        return url;
    }

    /**
     * Fetches an HTTP response from a URL.
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Returns an "MovieShelf": an array of poster URL strings, from a TMDB JSON response.
     */
    public static MovieShelf getMovieTitlesFromJson(String jsonResponse) throws JSONException {
        JSONObject responseObj = new JSONObject(jsonResponse);
        if (responseObj.has(TMDB_STATUS_CODE)
                && (responseObj.getInt(TMDB_STATUS_CODE) != HttpURLConnection.HTTP_OK)) {
            return null;
        }
        return new MovieShelf(responseObj.getJSONArray(TMDB_RESULTS));
    }
}