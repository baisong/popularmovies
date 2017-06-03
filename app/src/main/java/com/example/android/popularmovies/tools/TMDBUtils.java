package com.example.android.popularmovies.tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.popularmovies.data.Constants;
import com.example.android.popularmovies.data.FavoriteContract;
import com.example.android.popularmovies.data.MovieShelf;

import org.json.JSONArray;
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

    public static final int MODE_SORT_POPULAR = 0;
    public static final int MODE_SORT_TOP_RATED = 1;
    public static final int MODE_LIST_FAVORITES = 2;

    private static final String LOG_TAG = TMDBUtils.class.getSimpleName();

    public static final String MOVIE_ID = "id";
    public static final String MOVIE_TITLE = "original_title";
    public static final String MOVIE_POSTER = "poster_path";
    public static final String MOVIE_VOTE_AVG = "vote_average";
    public static final String MOVIE_SYNOPSIS = "overview";
    public static final String MOVIE_RELEASE_DATE = "release_date";

    public static final String REVIEW_ID = "id";
    public static final String REVIEW_AUTHOR = "author";
    public static final String REVIEW_TEXT = "content";

    public static final String TRAILER_YOUTUBE_NAME = "name";
    public static final String TRAILER_YOUTUBE_SIZE = "size";
    public static final String TRAILER_YOUTUBE_SOURCE = "source";
    public static final String TRAILER_YOUTUBE_TYPE = "type";
    public static final String FORMATTED_TRAILER_TITLE = "trailer_title";
    public static final String FORMATTED_TRAILER_SOURCE = "trailer_source";

    private static final int API_VERSION = 3;
    private static final String LANG_PARAM = "language";
    private static final String DEFAULT_LANG = "en-US";
    private static final String API_KEY_PARAM = "api_key";
    private static final String TMDB_RESULTS = "results";
    private static final String TMDB_STATUS_CODE = "status_code";

    private static final String API_BASE_URL = "https://api.themoviedb.org";
    private static final String API_POPULAR_PATH = "/movie/popular";
    private static final String API_TOP_RATED_PATH = "/movie/top_rated";
    private static final String API_MOVIE_DETAILS_PATH = "/movie/";
    private static final String API_MOVIE_REVIEWS_PATH = "/reviews";
    private static final String API_MOVIE_TRAILERS_PATH = "/trailers";
    private static final String URL_SEPARATOR = "/";

    private static final String YOUTUBE_BASE_URL = "http://www.youtube.com/watch?v=";
    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private static final String EMPTY_PATH_SEGMENT = "";
    private static final String REVIEWS_RESULTS_ARRAY_NAME = "results";
    private static final String TRAILERS_RESULTS_ARRAY_NAME = "youtube";
    private static final String TRAILER_TITLE_FORMAT = "%s: %s (%s)";
    private static final java.lang.String LIST_FAVORITES_SORT_ORDER =
            FavoriteContract.FavoriteEntry.COLUMN_CREATED_TIMESTAMP + " DESC";

    /**
     * Builds the URL for querying movies using the specified sort method.
     */
    public static URL buildUrl(int sortMethod) {
        // Default to popular, even if we have an invalid sortMethod
        // After all, the app is named "Popular Movies"
        String path = API_POPULAR_PATH;
        if (sortMethod == MODE_SORT_TOP_RATED) {
            path = API_TOP_RATED_PATH;
        }
        String baseUrl = API_BASE_URL
                        + URL_SEPARATOR
                        + String.valueOf(API_VERSION)
                        + path;
        String tmdbApiKey = Constants.TMDB_API_KEY;
        Uri builtUri = Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, tmdbApiKey)
                .appendQueryParameter(LANG_PARAM, DEFAULT_LANG)
                .build();
        return getUrl(builtUri);
    }

    /**
     * Converts a Uri to a URL object, encapsulates exception handling.
     */
    private static URL getUrl(Uri uri) {
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
     * Returns an "MovieShelf": an array of poster URL strings & the detail launch Intent extra.
     */
    public static MovieShelf getMovieTitlesFromJson(String jsonResponse) throws JSONException {
        JSONObject responseObj = new JSONObject(jsonResponse);
        if (responseObj.has(TMDB_STATUS_CODE)
                && (responseObj.getInt(TMDB_STATUS_CODE) != HttpURLConnection.HTTP_OK)) {
            return null;
        }
        return new MovieShelf(responseObj.getJSONArray(TMDB_RESULTS));
    }

    /**
     * Builds the URL to fetch a given movie's reviews.
     */
    public static URL buildReviewsUrl(int movieId) {
        return getUrl(buildDetailsUri(movieId, API_MOVIE_REVIEWS_PATH));
    }

    /**
     * Builds the URL to fetch a given movie's trailers.
     */
    public static URL buildTrailersUrl(int movieId) {
        return getUrl(buildDetailsUri(movieId, API_MOVIE_TRAILERS_PATH));
    }

    /**
     * Builds the URL to fetch a given movie.
     *
     * Currently used in the DetailActivity to refresh favorites after they are launched from the
     * favorites view, since the JSON string used to launch favorites is stored in the database,
     * and could be stale.
     */
    public static URL buildMovieUrl(int movieId) {
        return getUrl(buildDetailsUri(movieId, EMPTY_PATH_SEGMENT));
    }

    /**
     * Wrapper function for all movie data Uri builders.
     */
    private static Uri buildDetailsUri(int movieId, String detailsPath) {
        String tmdbApiKey = Constants.TMDB_API_KEY;
        String baseUrl = API_BASE_URL
                + URL_SEPARATOR
                + String.valueOf(API_VERSION)
                + API_MOVIE_DETAILS_PATH
                + String.valueOf(movieId)
                + detailsPath;
        return Uri.parse(baseUrl).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, tmdbApiKey)
                .appendQueryParameter(LANG_PARAM, DEFAULT_LANG)
                .build();
    }

    /**
     * Convert an individual movie JSON string into ContentValues for the provider.
     */
    @Nullable
    public static ContentValues getMovieItemData(String jsonString) {
        ContentValues item;
        try {
            JSONObject json = new JSONObject(jsonString);
            item = new ContentValues();
            item.put(TMDBUtils.MOVIE_ID, json.getInt(TMDBUtils.MOVIE_ID));
            item.put(TMDBUtils.MOVIE_TITLE, json.getString(TMDBUtils.MOVIE_TITLE));
            item.put(TMDBUtils.MOVIE_RELEASE_DATE, json.getString(TMDBUtils.MOVIE_RELEASE_DATE));
            item.put(TMDBUtils.MOVIE_POSTER, IMAGE_BASE_URL + json.getString(TMDBUtils.MOVIE_POSTER));
            item.put(TMDBUtils.MOVIE_VOTE_AVG, json.getString(TMDBUtils.MOVIE_VOTE_AVG));
            item.put(TMDBUtils.MOVIE_SYNOPSIS, json.getString(TMDBUtils.MOVIE_SYNOPSIS));
            return item;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a reviews URL response into an array of content values.
     */
    public static ContentValues[] getReviewsData(String jsonString) {
        ContentValues[] items;
        JSONArray results;
        try {
            JSONObject json = new JSONObject(jsonString);
            results = json.getJSONArray(REVIEWS_RESULTS_ARRAY_NAME);
            items = new ContentValues[results.length()];
            if (results.length() > 0) {
                for (int i = 0; i < results.length(); i++) {
                    items[i] = getReviewItemData(results.getJSONObject(i));
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return items;
    }

    /**
     * Parses an individual review item into a ContentValues instance.
     */
    private static ContentValues getReviewItemData(JSONObject json) {
        ContentValues item;
        try {
            item = new ContentValues();
            item.put(TMDBUtils.REVIEW_ID, json.getString(TMDBUtils.REVIEW_ID));
            item.put(TMDBUtils.REVIEW_AUTHOR, json.getString(TMDBUtils.REVIEW_AUTHOR));
            item.put(TMDBUtils.REVIEW_TEXT, json.getString(TMDBUtils.REVIEW_TEXT));
            return item;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parses a trailers URL response into an array of content values.
     */
    public static ContentValues[] getTrailersData(String jsonString) {
        ContentValues[] items;
        JSONArray results;
        try {
            JSONObject json = new JSONObject(jsonString);
            results = json.getJSONArray(TRAILERS_RESULTS_ARRAY_NAME);
            items = new ContentValues[results.length()];
            if (results.length() > 0) {
                for (int i = 0; i < results.length(); i++) {
                    items[i] = getTrailerItemData(results.getJSONObject(i));
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return items;
    }

    /**
     * Parses an individual trailer item into a ContentValues instance.
     */
    private static ContentValues getTrailerItemData(JSONObject json) {
        ContentValues item;
        item = new ContentValues();
        item.put(TMDBUtils.FORMATTED_TRAILER_TITLE, formatTrailerTitle(json));
        item.put(TMDBUtils.FORMATTED_TRAILER_SOURCE, formatTrailerContentUri(json));
        return item;

    }

    /**
     * Standardizes a pretty trailer title for display.
     */
    private static String formatTrailerTitle(JSONObject json) {
        try {
            String name = json.getString(TRAILER_YOUTUBE_NAME);
            String size = json.getString(TRAILER_YOUTUBE_SIZE);
            String type = json.getString(TRAILER_YOUTUBE_TYPE);
            return String.format(TRAILER_TITLE_FORMAT, type, name, size);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates an intent-ready content URI to launch a YouTube trailer
     */
    private static String formatTrailerContentUri(JSONObject json) {
        try {
            String source = json.getString(TRAILER_YOUTUBE_SOURCE);
            return YOUTUBE_BASE_URL + source;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Queries the FavoriteContentProvider and creates a MovieShelf for display in MainActivity.
     */
    @Nullable
    public static MovieShelf buildFavoritesShelf(Context context) {
        String[] projection = new String[]{
                FavoriteContract.FavoriteEntry.COLUMN_ID,
                FavoriteContract.FavoriteEntry.COLUMN_TMDB_POSTER_URL,
                FavoriteContract.FavoriteEntry.COLUMN_TMDB_CACHED_DATA,
        };
        Cursor cursor = context.getContentResolver().query(
                FavoriteContract.FavoriteEntry.CONTENT_URI,
                projection,
                null,
                null,
                LIST_FAVORITES_SORT_ORDER
        );
        if (cursor == null) {
            return null;
        }
        MovieShelf shelf = new MovieShelf(cursor);
        cursor.close();
        return shelf;
    }

    /**
     * Queries themoviedb.com API for the movies list of the specified sort.
     */
    @Nullable
    public static MovieShelf buildSortedShelf(int viewMode) {
        URL movieQueryUrl = buildUrl(viewMode);
        Log.d(LOG_TAG, movieQueryUrl.toString());
        try {
            String json = getResponseFromHttpUrl(movieQueryUrl);
            return getMovieTitlesFromJson(json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}