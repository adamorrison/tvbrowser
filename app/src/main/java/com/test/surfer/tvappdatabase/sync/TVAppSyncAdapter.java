package com.test.surfer.tvappdatabase.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.test.surfer.tvappdatabase.R;
import com.test.surfer.tvappdatabase.Utility;
import com.test.surfer.tvappdatabase.data.TVContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by surfer on 25/09/2015.
 */
public class TVAppSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = TVAppSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the tv database, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    // needs an API key from moviedb
    private static final String API_KEY = "";

    // raw JSON response as a string.
    private String topRatedTVJsonStr = null;
    private int numOfDataValues = 0;

    public TVAppSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public String getRawJSON() {
        return topRatedTVJsonStr;
    }

    public int getNumOfDataValues() {
        return numOfDataValues;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).setExtras(new Bundle()).build();

            //TODO: Update, robolectric doesn't like 'ContentResolver.requestSync' for some reason, using 'ContentResolver.addPeriodicSync' instead
            //ContentResolver.requestSync(request);
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        TVAppSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            // Construct the URL for the Movie DB TV query
            final String categoryChoice = Utility.getPreferredTVCategory(getContext());
            final String TV_BASE_URL =
                    "http://api.themoviedb.org/3/tv/"+categoryChoice+"?";
            final String API_KEY_PARAM = "api_key";


            Uri builtUri = Uri.parse(TV_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to Movie DB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            topRatedTVJsonStr = buffer.toString();
            getTopRatedTVDataFromJson(topRatedTVJsonStr);
        } catch (IOException e) {
            // If the code didn't successfully get the tv data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private void getTopRatedTVDataFromJson(String topRatedTVJsonStr) throws JSONException {

        final String MD_RESULTS = "results";
        final String MD_TV_ID = "id";
        final String MD_TV_ORIG_NAME = "original_name";
        final String MD_TV_POSTER = "poster_path";
        final String MD_TV_SYNOPSIS = "overview";
        final String MD_TV_RATING = "vote_average";
        final String MD_TV_RELEASE_DATE = "first_air_date";

        try {
            JSONObject tvJson = new JSONObject(topRatedTVJsonStr);
            JSONArray tvArray = tvJson.getJSONArray(MD_RESULTS);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(tvArray.length());

            for(int i = 0; i < tvArray.length(); i++) {
                JSONObject obj = tvArray.getJSONObject(i);
                String id = obj.getString(MD_TV_ID);
                String orig_name = obj.getString(MD_TV_ORIG_NAME);
                String poster_path = obj.getString(MD_TV_POSTER);
                String synopsis = obj.getString(MD_TV_SYNOPSIS);
                String rating = obj.getString(MD_TV_RATING);
                String release_date = obj.getString(MD_TV_RELEASE_DATE);

                ContentValues tvValues = new ContentValues();

                tvValues.put(TVContract.TVEntry.COLUMN_NAME_ENTRY_ID, id);
                tvValues.put(TVContract.TVEntry.COLUMN_NAME_TITLE, orig_name);
                tvValues.put(TVContract.TVEntry.COLUMN_NAME_POSTER, poster_path);
                tvValues.put(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS, synopsis);
                tvValues.put(TVContract.TVEntry.COLUMN_NAME_RATING, rating);
                tvValues.put(TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE, release_date);

                String detailPoster = getDetailPoster(id);
                if(detailPoster == "") {detailPoster = poster_path;}
                tvValues.put(TVContract.TVEntry.COLUMN_NAME_DETAIL_POSTER, detailPoster);

                cVVector.add(tvValues);
            }
            insertData(cVVector);
            numOfDataValues = cVVector.size();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getDetailPoster(String id) {
        String detailPoster = "";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // raw JSON response as a string.
        String tvPostersJsonStr = null;

        try {
            // Construct the URL for the Movie DB TV query
            final String categoryChoice = Utility.getPreferredTVCategory(getContext());
            final String TV_BASE_URL =
                    "http://api.themoviedb.org/3/tv/" + id + "/" + "images?";
            final String API_KEY_PARAM = "api_key";


            Uri builtUri = Uri.parse(TV_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to Movie DB, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            tvPostersJsonStr = buffer.toString();

            final String MD_BACKDROPS = "backdrops";
            final String MD_FILEPATH = "file_path";
            final String MD_ASPECTRATIO = "aspect_ratio";

            JSONObject tvJson = new JSONObject(tvPostersJsonStr);
            JSONArray tvArray = tvJson.getJSONArray(MD_BACKDROPS);

            for (int i = 0; i < tvArray.length(); i++) {
                JSONObject obj = tvArray.getJSONObject(i);
                String ratio = obj.getString(MD_ASPECTRATIO);
                if (ratio.equals("1.77777777777778")) {
                    detailPoster = obj.getString(MD_FILEPATH);
                    return detailPoster;
                }
            }
        } catch (IOException e) {
            // If the code didn't successfully get the tv data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return detailPoster;
    }

    private int insertData(Vector<ContentValues> cVVector) {

        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            // delete old data so we don't build up an endless history
            getContext().getContentResolver().delete(TVContract.TVEntry.CONTENT_URI, null, null);

            inserted = getContext().getContentResolver().bulkInsert(TVContract.TVEntry.CONTENT_URI, cvArray);
        }
        return inserted;
    }
}
