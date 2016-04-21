package com.test.surfer.tvappdatabase.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by surfer on 19/09/2015.
 */
public class TestDb extends AndroidTestCase {

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    void deleteTheDatabase() {mContext.deleteDatabase(TVDbHelper.DATABASE_NAME);}

    // clear the existing database of items before new suite of tests
    public void setUp() {
        deleteTheDatabase();
    }


    /*
        Test the database creation by checking column names returned match as expected
     */
    public void testCreateDb() throws Throwable {
        // vector of the table names - just tv for now
        final Vector<String> tableNames = new Vector<String>();
        tableNames.add(TVContract.TVEntry.TABLE_NAME);

        SQLiteDatabase db = new TVDbHelper(
                this.mContext).getWritableDatabase();

        // is the db created and writeable OK?
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        // false if nothing returned
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNames.remove(c.getString(0));
        } while( c.moveToNext() );

        // confirm tv table was removed as expected above
        assertTrue("Error: Your database was created without the tv table",
                tableNames.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + TVContract.TVEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: We were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final Vector<String> columnHashSet = new Vector<>();
        columnHashSet.add(TVContract.TVEntry._ID);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_ENTRY_ID);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_POSTER);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_RATING);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_TITLE);
        columnHashSet.add(TVContract.TVEntry.COLUMN_NAME_DETAIL_POSTER);

        // The number of rows returned by the cursor, should match the number of columns defined in the hash set
        assertTrue("Error: The cursor and hash set sizes don't match up",
                columnHashSet.size() == c.getCount());

        // remove column names from our vector as we find them in the database
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            columnHashSet.remove(columnName);
        } while(c.moveToNext());

        // should be an empty vector or our database is incomplete
        assertTrue("Error: The database doesn't contain all of the required entry columns",
                columnHashSet.isEmpty());
        db.close();

        // the db should no longer be open
        assertEquals(false, db.isOpen());
    }

    public void testInsertItem() {
        // grab writeable database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create some test values from utility class
        ContentValues testValues = TestUtilities.createValue();

        // insert the test values into the database
        long tvRowId;
        tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValues);

        // confirm that we got an actual tv row back, indicating successful insertion
        assertTrue(tvRowId != -1);

        // query the tv table for its entire contents
        Cursor cursor = db.query(
                TVContract.TVEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // meaning that we returned an item, otherwise the table is empty!
        assertTrue("Error: No Records returned from query", cursor.moveToFirst());

        // for each column, confirm it exists and that it compares as expected with the equivalent test value
        TestUtilities.validateCurrentRecord("Error: Query Validation Failed",
                cursor, testValues);

        // only one insertion above, so there shouldn't be anything else to look at
        assertFalse("Error: More than one record returned from query",
                cursor.moveToNext());

        cursor.close();
        db.close();
    }

    public void testRemoveItem() {

        // grab writeable database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValue = TestUtilities.createValue();
        long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValue);

        // query the tv table for its entire contents
        Cursor cursor = db.query(
                TVContract.TVEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // meaning that we returned an item, otherwise the table is empty!
        assertTrue("Error: No Records returned from query", cursor.moveToFirst());

        int deleteReturn = db.delete(TVContract.TVEntry.TABLE_NAME, TVContract.TVEntry._ID + "=" + tvRowId, null);
        // we should return a 1, representing the single row we have inserted and removed
        assertEquals("Error: Unexpected number of records removed", 1, deleteReturn);

        // query the tv table for its entire contents
        Cursor cursor2 = db.query(
                TVContract.TVEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // only one insertion/deletion above, so there shouldn't be anything else to look at
        assertFalse("Error: More than one record returned from query", cursor.moveToNext());

        cursor.close();
        cursor2.close();
        db.close();
    }

    // test to see if we can successfully query the database with no clauses
    public void testBasicQueryDB() {

        // grab writeable database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // insert a bunch of items in the database
        ArrayList<ContentValues> values = TestUtilities.createValues(BULK_INSERT_RECORDS_TO_INSERT);
        for (ContentValues value : values) {
            // insert the test values into the database
            long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, value);
            assertTrue(tvRowId != -1);
        }

        // query the tv table for its entire contents
        Cursor cursor = db.query(
                TVContract.TVEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // meaning that we returned an item, otherwise the table is empty!
        assertEquals("Error: Incorrect number of records returned from query", BULK_INSERT_RECORDS_TO_INSERT, cursor.getCount());

        cursor.close();
        db.close();
    }

    // test to see if we can successfully query the database with an ID clause
    public void testIDQueryDB() {
        // grab writeable database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClauseColumn = "_id=?";

        // create some test values from utility class
        ContentValues testValue = TestUtilities.createValue();
        // insert the test values into the database
        long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValue);

        // query the tv table for the specific ID corresponding to the value we just inserted
        Cursor cursor = db.query(
                TVContract.TVEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                TVContract.TVEntry._ID + " = " + tvRowId, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        TestUtilities.validateCursor("Error: The query results don't match the values inserted", cursor, testValue);

        cursor.close();
        db.close();
    }

    // test to see if we can successfully update the database with new values
    public void testUpdateDB() {

        // grab writeable database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // create some test values from utility class
        ContentValues testValue = TestUtilities.createValue();
        // insert the test values into the database
        long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, testValue);

        // new values to update with
        ContentValues updatedValues = new ContentValues(testValue);
        updatedValues.put(TVContract.TVEntry._ID, tvRowId);
        updatedValues.put(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS, "New SYNOPSIS");

        // update the database with the new values from above, where the _id matches
        int count = db.update(
                TVContract.TVEntry.TABLE_NAME, updatedValues, TVContract.TVEntry._ID + "= ?",
                new String[]{Long.toString(tvRowId)});
        assertEquals(count, 1);

        // query the database against the matching _id for the entry we inserted and updated above
        Cursor cursor = db.query(
                TVContract.TVEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                TVContract.TVEntry._ID + " = " + tvRowId, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        /* the returned cursor values should match the manually updated content values from above,
        indiciating that the changes were successfully updated in the DB
         */
        TestUtilities.validateCursor("testUpdateDB.  Error validating tv entry update.",
                cursor, updatedValues);

        cursor.close();
        db.close();
    }

    // test to see if we can successfully insert many values at once
    public void testBulkInsertDB() {
        // grab writeable database
        TVDbHelper dbHelper = new TVDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ArrayList<ContentValues> values = TestUtilities.createValues(BULK_INSERT_RECORDS_TO_INSERT);
        try
        {
            db.beginTransaction();
            for (ContentValues value : values)
            {
                long tvRowId = db.insert(TVContract.TVEntry.TABLE_NAME, null, value);
                assertTrue(tvRowId != -1);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {}
        finally
        {
            db.endTransaction();
        }

        // Query for everything in the TV table
        Cursor cursor = db.query(TVContract.TVEntry.TABLE_NAME, null, null, null, null, null, null);

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        //make sure they match the ones we created
        cursor.moveToFirst();
        for (ContentValues value : values)
        {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating TVEntry ", cursor, value);
            cursor.moveToNext();
        }

        cursor.close();
        db.close();
    }
}
