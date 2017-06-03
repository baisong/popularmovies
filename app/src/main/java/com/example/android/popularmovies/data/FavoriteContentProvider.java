package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.android.popularmovies.data.FavoriteContract.FavoriteEntry.TABLE_NAME;

public class FavoriteContentProvider extends ContentProvider {

    public static final int FAVORITES = 100;
    public static final int FAVORITE_WITH_ID = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private FavoriteDbHelper mFavoriteDbHelper;

    private static final String INSERT_FAIL_MESSAGE = "Failed to insert: ";
    private static final String INSERT_EXCEPTION_MESSAGE = "Unknown uri: ";

    private static final String UPDATE_UNSUPPORTED_MESSAGE = "This provider does not provide update operations.";
    private static final String TYPE_UNSUPPORTED_MESSAGE = "This provider does not provide a type.";

    private static final String SINGLE_DELETE_WHERECLAUSE = "_id=?";
    private static final String PATH_SEGMENT_FAVORITE_WITH_ID = "/#";

    /**
     * Matches accepted content URIs
     */
    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                FavoriteContract.AUTHORITY,
                FavoriteContract.PATH_FAVORITES,
                FAVORITES
        );
        uriMatcher.addURI(
                FavoriteContract.AUTHORITY,
                FavoriteContract.PATH_FAVORITES + PATH_SEGMENT_FAVORITE_WITH_ID,
                FAVORITE_WITH_ID);
        return uriMatcher;
    }

    /**
     * Sets up the custom content provider's database helper.
     */
    @Override
    public boolean onCreate() {
        mFavoriteDbHelper = new FavoriteDbHelper(getContext());
        return true;
    }

    /**
     * Standard insert method, only accepts single inserts.
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVORITES:
                long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(FavoriteContract.FavoriteEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException(INSERT_FAIL_MESSAGE + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException(INSERT_EXCEPTION_MESSAGE + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    /**
     * Standard query method, only accepts bulk queries.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mFavoriteDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case FAVORITES:
                cursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException(INSERT_EXCEPTION_MESSAGE + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Standard delete method, only accepts single favorite record with ID.
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mFavoriteDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int favoritesDeleted;

        switch (match) {
            case FAVORITE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                favoritesDeleted = db.delete(TABLE_NAME, SINGLE_DELETE_WHERECLAUSE, new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException(INSERT_EXCEPTION_MESSAGE + uri);
        }

        if (favoritesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return favoritesDeleted;
    }

    /**
     * Standard update method, unimplemented.
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException(UPDATE_UNSUPPORTED_MESSAGE);
    }

    /**
     * Standard getType method, unimplemented.
     */
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException(TYPE_UNSUPPORTED_MESSAGE);
    }

}
