package com.test.surfer.tvappdatabase.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by surfer on 23/09/2015.
 */
public class TestTVProvider extends AndroidTestCase {
    public static final String LOG_TAG = TestTVProvider.class.getSimpleName();

    /* Call delete on TV provider where selection (2nd parameter) is null this deletes all rows */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                TVContract.TVEntry.CONTENT_URI,
                null,
                null
        );

        /* Now grab a cursor containing the rows found in the tv table - this should be empty following the delete */
        Cursor cursor = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals("Error: Records not deleted from TV table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // TVProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                TVProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: TVProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + TVContract.CONTENT_AUTHORITY,
                    providerInfo.authority, TVContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: TVProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
        This test doesn't touch the database.  It verifies that the ContentProvider returns
        the correct type for each type of URI that it can handle.
    */
    public void testGetType() {
        // content://com.test.surfer.tvappdatabase/tv/
        String type = mContext.getContentResolver().getType(TVContract.TVEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.test.surfer.tvappdatabase/tv
        assertEquals("Error: the TVEntry CONTENT_URI should return TVEntry.CONTENT_TYPE",
                TVContract.TVEntry.CONTENT_TYPE, type);

        long testID = 1;
        // content://com.test.surfer.tvappdatabase/tv/Buffy
        type = mContext.getContentResolver().getType(
                TVContract.TVEntry.buildTvUri(testID));

        // vnd.android.cursor.dir/com.test.surfer.tvappdatabase/tv
        assertEquals("Error: the TVEntry CONTENT_URI with tv programme should return TVEntry.CONTENT_ITEM_TYPE",
                TVContract.TVEntry.CONTENT_ITEM_TYPE, type);
    }

    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.
     */
    public void testTVQuery() {
        // insert our test records into the database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createValue();

        long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to Insert TVEntry into the Database", tvRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor tvCursor = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testTVQuery", tvCursor, testValues);
    }

    /*
     This test uses the database directly to insert and then uses the ContentProvider to
     read out the data.
    */
    public void testTVIDQuery() {
        // insert our test records into the database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createValue();

        long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to Insert TVEntry into the Database", tvRowId != -1);

        String clause = TVContract.TVEntry._ID + " = " + "1";

        // Test the named content provider query
        Cursor tvCursor = mContext.getContentResolver().query(
                TVContract.TVEntry.buildTvUri(tvRowId),
                null,
                null,
                null,
                null
                //TVContract.TVEntry.COLUMN_NAME_ENTRY_ID + " 123"
        );

        TestUtilities.validateCursor("testNamedTVQuery", tvCursor, testValues);

        db.close();
    }

    public void testInsert() {
        // insert our test records into the database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Query the database, expecting no records initially
        Cursor tvCursor = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(tvCursor.getCount(), 0);

        // Insert data, confirm row ID returned as expected
        ContentValues testValues = TestUtilities.createValue();
        long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to Insert TVEntry into the Database", tvRowId != -1);

        // Query the database, now expecting 1 row and confirm contents below
        Cursor tvCursor2 = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(tvCursor2.getCount(), 1);
        TestUtilities.validateCursor("testInsert", tvCursor2, testValues);

        db.close();
        tvCursor.close();
        tvCursor2.close();
    }

    /*
    This test uses the provider to insert and then update the data. Uncomment this test to
    see if your update location is functioning correctly.
 */
    public void testUpdateTV() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createValue();

        Uri tvUri = mContext.getContentResolver().
                insert(TVContract.TVEntry.CONTENT_URI, values);
        long tvRowId = ContentUris.parseId(tvUri);

        // Verify we got a row back.
        assertTrue(tvRowId != -1);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(TVContract.TVEntry._ID, tvRowId);
        updatedValues.put(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS, "Bingo");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor tvCursor = mContext.getContentResolver().query(TVContract.TVEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        tvCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                TVContract.TVEntry.CONTENT_URI, updatedValues, TVContract.TVEntry._ID + "= ?",
                new String[] { Long.toString(tvRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called
        tco.waitForNotificationOrFail();

        tvCursor.unregisterContentObserver(tco);
        tvCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null,   // projection
                TVContract.TVEntry._ID + " = " + tvRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateTV.  Error validating tv entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    /* This test performs an insertion and query to confirm successful retrieval of inserted data */
    public void testContentObserver() {
        ContentValues testValues = TestUtilities.createValue();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TVContract.TVEntry.CONTENT_URI, true, tco);
        Uri tvUri = mContext.getContentResolver().insert(TVContract.TVEntry.CONTENT_URI, testValues);

        // Did our content observer get called?
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long locationRowId = ContentUris.parseId(tvUri);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);

        // Pull some out to stare at it and verify it made the round trip.
        Cursor cursor = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testContentObserver. Error validating TVEntry.",
                cursor, testValues);
    }

    /* A test to confirm we can successfully delete data through the content provider interface */
    public void testDelete() {
        testContentObserver();

        // Register a content observer for our tv delete.
        TestUtilities.TestContentObserver tvObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TVContract.TVEntry.CONTENT_URI, true, tvObserver);

        deleteAllRecordsFromProvider();

        tvObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tvObserver);
    }

    /* Create a contentvalue array full of data to insert into the content provider */
    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertTVValues() {
        long currentTestDate = TestUtilities.getDate();
        long millisecondsInADay = 1000*60*60*24;
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate+= millisecondsInADay ) {
            ContentValues values = new ContentValues();
            values.put(TVContract.TVEntry.COLUMN_NAME_ENTRY_ID, i);
            values.put(TVContract.TVEntry.COLUMN_NAME_TITLE, "title");
            values.put(TVContract.TVEntry.COLUMN_NAME_POSTER, "poster");
            values.put(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS, "synopsis");
            values.put(TVContract.TVEntry.COLUMN_NAME_RATING, 8.9);
            values.put(TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE, currentTestDate);
            returnContentValues[i] = values;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        ArrayList<ContentValues> aLContent = TestUtilities.createValues(10);
        ContentValues[] bulkInsertValues = new ContentValues[aLContent.size()];
        aLContent.toArray(bulkInsertValues);

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver tvObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(TVContract.TVEntry.CONTENT_URI, true, tvObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(TVContract.TVEntry.CONTENT_URI, bulkInsertValues);

        tvObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tvObserver);

        assertEquals(BULK_INSERT_RECORDS_TO_INSERT, insertCount);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE + " ASC"  // sort order == by DATE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating TVEntry " + i,
                    cursor, bulkInsertValues[i]);
        }
        cursor.close();
    }
}
