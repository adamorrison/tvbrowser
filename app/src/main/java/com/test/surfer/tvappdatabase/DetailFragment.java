package com.test.surfer.tvappdatabase;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by surfer on 13/10/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private ShareActionProvider mShareActionProvider;
    private static final String TV_SHARE_HASHTAG = " #TVApp";

    static final String DETAIL_URI = "URI";
    private Uri mUri;
    private static final int DETAIL_LOADER = 0;

    static final String POSTER_SIZE = "w780";

    private TextView mTitleView;
    private TextView mRatingView;
    private TextView mReleaseDateView;
    private ImageView mPosterView;
    private TextView mDescView;

    private boolean onLoadFinished;

    static final int COL_ID = 0;
    static final int COL_ENTRY_ID = 1;
    static final int COL_TITLE = 2;
    static final int COL_POSTER = 3;
    static final int COL_SYNOPSIS = 4;
    static final int COL_RATING = 5;
    static final int COL_RELEASE_DATE = 6;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
        mDescView = (TextView) rootView.findViewById(R.id.description);
        mRatingView = (TextView) rootView.findViewById(R.id.rating);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.releaseDate);
        mPosterView = (ImageView) rootView.findViewById(R.id.poster);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (onLoadFinished != false) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(takeScreenshot()));
        return shareIntent;
    }

    private File takeScreenshot() {
        String mPath = Environment.getExternalStorageDirectory().toString() + "/" + Utility.getDate() + ".jpg";
        File imageFile = new File(mPath);
        try {
            // image naming and path  to include sd card  appending name you choose for file
            // create bitmap screen capture
            View v1 = getActivity().getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            return imageFile;

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
        return imageFile;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.

            return new CursorLoader(
                    getActivity(),
                    mUri, // contains an appended _id which gets parsed and used for the selection/args in the provider itself
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            String title = data.getString(COL_TITLE);
            mTitleView.setText(title);

            String desc = data.getString(COL_SYNOPSIS);
            mDescView.setText(desc);

            //String rating = data.getString(COL_RATING);
            mRatingView.setText(Utility.formatRating(data.getString(COL_RATING)));

            String release = data.getString(COL_RELEASE_DATE);
            mReleaseDateView.setText(release);

            //TODO: Update this and remove picasso from project, using new image loader
            Picasso.with(getActivity()).load(getImageURL(data)).into(mPosterView);

            onLoadFinished = true;
        }

        // A delay of 1s before creating the share intent in order to fully load the display before the screenshot is taken
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                }
            }
        }, 1000);
    }

    private String getImageURL(Cursor cursor) {
        return "http://image.tmdb.org/t/p/"+POSTER_SIZE+"/" + cursor.getString(TVFragment.COL_DETAIL_POSTER);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
