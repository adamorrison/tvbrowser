package com.test.surfer.tvappdatabase.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by surfer on 29/08/2015.
 */
public class TVProvider extends ContentProvider {

    private final String LOG_TAG = TVProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private TVDbHelper mOpenHelper;

    static final int TV = 100;
    static final int TV_WITH_ID = 101;

    private static final SQLiteQueryBuilder sTVQueryBuilder;

    static {sTVQueryBuilder = new SQLiteQueryBuilder();sTVQueryBuilder.setTables(TVContract.TVEntry.TABLE_NAME);}

    //tv._id = ?
    private static final String s_IDSelection =
            TVContract.TVEntry.TABLE_NAME+
                    "." + TVContract.TVEntry._ID + " = ? ";

    private Cursor getTV(Uri uri, String[] projection, String selection, String sortOrder) {

        return sTVQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTVBy_ID(Uri uri, String[] projection, String sortOrder) {
        String iD = TVContract.TVEntry.getIDFromUri(uri);

        String[] selectionArgs = new String[]{iD};

        return sTVQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                s_IDSelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TVContract.CONTENT_AUTHORITY;

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // TVContract to help define the types to the UriMatcher.
        sURIMatcher.addURI(authority,TVContract.PATH_TV,TV);
        sURIMatcher.addURI(authority,TVContract.PATH_TV+"/*", TV_WITH_ID);

        // 3) Return the new matcher!
        return sURIMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TVDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TV_WITH_ID:
                return TVContract.TVEntry.CONTENT_ITEM_TYPE;
            case TV:
                return TVContract.TVEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "tv/*"
            case TV_WITH_ID:
            {
                retCursor = getTVBy_ID(uri, projection, sortOrder);
                break;
            }
            // "tv/"
            case TV: {
                retCursor = getTV(uri, projection, selection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TV: {
                long _id = db.insert(TVContract.TVEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = TVContract.TVEntry.buildTvUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);

        if (null == selection) selection = "1";
        switch (match) {
            case TV: {
                rowsDeleted =  db.delete(TVContract.TVEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case TV: {
                rowsUpdated =  db.update(TVContract.TVEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TV:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TVContract.TVEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
