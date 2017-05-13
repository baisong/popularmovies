package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.tools.TMDBUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185/";

    private String mDataJsonString;
    private JSONObject mData;
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
     * Initializes the activity and accesses data from the intent that launched this.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mTitleDisplay = (TextView) findViewById(R.id.tv_display_title);
        mReleaseDisplay = (TextView) findViewById(R.id.tv_display_release_date);
        mPosterDisplay = (ImageView) findViewById(R.id.iv_display_movie_poster);
        mVoteDisplay = (TextView) findViewById(R.id.tv_display_vote_average);
        mPlotDisplay = (TextView) findViewById(R.id.tv_display_plot_synopsis);
        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            if (launchIntent.hasExtra(Intent.EXTRA_TEXT)) {
                mDataJsonString = launchIntent.getStringExtra(Intent.EXTRA_TEXT);
                try {
                    mData = new JSONObject(mDataJsonString);
                    mTitle = mData.getString(TMDBUtils.TITLE);
                    mReleaseDate = mData.getString(TMDBUtils.RELEASE_DATE);
                    mMoviePoster = IMAGE_BASE_URL + mData.getString(TMDBUtils.POSTER);
                    mVoteAverage = mData.getString(TMDBUtils.VOTE_AVG);
                    mPlotSynopsis = mData.getString(TMDBUtils.SYNOPSIS);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                mTitleDisplay.setText(mTitle);
                mReleaseDisplay.setText(this.formatReleaseDate(mReleaseDate));
                Picasso.with(getApplicationContext()).load(mMoviePoster).into(mPosterDisplay);
                mVoteDisplay.setText(getFormattedVote());
                mPlotDisplay.setText(mPlotSynopsis);
            }
        }
    }

    /**
     * Builds an implicit intent to share the movie listing.
     */
    private Intent createShareMovieIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(getShareableText())
                .getIntent();
        return null;
    }

    /**
     * Makes our vote average number look a little better.
     */
    private String getFormattedVote() {
        return mVoteAverage + "/10";
    }

    /**
     * Composes a brilliant social-media-ready post your friends will love.
     */
    private String getShareableText() {
        return mTitle + " (" + getFormattedVote() + "): " + mPlotSynopsis;
    }

    /**
     * Formats the release date like "Apr 2017"
     */
    private String formatReleaseDate(String dateString) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = fmt.parse(dateString);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM YYYY");
            return fmtOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown release date";
        }
    }

    /**
     * Inflates the detail menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareMovieIntent());
        return true;
    }
}