package com.example.android.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.popularmovies.data.FavoriteContract.FavoriteEntry;
import com.example.android.popularmovies.tools.TMDBUtils;


public class FavoriteDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favorites.db";
    private static final int VERSION = 1;

    FavoriteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE "    + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID                      + " INTEGER PRIMARY KEY, " +
                FavoriteEntry.COLUMN_TITLE             + " TEXT NOT NULL, "       +
                FavoriteEntry.COLUMN_TMDB_CACHED_DATA  + " TEXT NOT NULL, "       +
                FavoriteEntry.COLUMN_TMDB_POSTER_URL   + " TEXT NOT NULL, "       +
                FavoriteEntry.COLUMN_TMDB_ID           + " INTEGER NOT NULL, "    +
                FavoriteEntry.COLUMN_CREATED_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                " UNIQUE (" + FavoriteEntry.COLUMN_TMDB_ID + ") ON CONFLICT REPLACE);";
        db.execSQL(CREATE_TABLE);
    }

    public static ContentValues prepareFavoriteFromJson(String jsonString) {
        ContentValues parsedJson = TMDBUtils.getMovieItemData(jsonString);
        ContentValues favorite = new ContentValues();
        favorite.put(FavoriteEntry.COLUMN_TITLE, parsedJson.getAsString(TMDBUtils.MOVIE_TITLE));
        favorite.put(FavoriteEntry.COLUMN_TMDB_CACHED_DATA, jsonString);
        favorite.put(FavoriteEntry.COLUMN_TMDB_POSTER_URL, parsedJson.getAsString(TMDBUtils.MOVIE_POSTER));
        favorite.put(FavoriteEntry.COLUMN_TMDB_ID, parsedJson.getAsInteger(TMDBUtils.MOVIE_ID));
        return favorite;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        onCreate(db);
    }
}
