package com.example.android.popularmovies.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieShelf {

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    public String[] moviePosters;
    public JSONObject[] movieData;
    public MovieShelf(JSONArray movieListings) throws JSONException {
        this.moviePosters = new String[movieListings.length()];
        this.movieData = new JSONObject[movieListings.length()];
        try {
            for (int i = 0; i < movieListings.length(); i++) {
                JSONObject obj = movieListings.getJSONObject(i);
                this.movieData[i] = obj;
                this.moviePosters[i] = buildPosterUrl(obj.getString(TMDBUtils.POSTER));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String buildPosterUrl(String posterPath) {
        return IMAGE_BASE_URL + posterPath;
    }
}
