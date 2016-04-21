package com.test.surfer.tvappdatabase.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;
import android.text.format.Time;
import android.util.Log;

import com.test.surfer.tvappdatabase.utils.PollingCheck;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * Created by surfer on 19/09/2015.
 */
public class TestUtilities extends AndroidTestCase {

    static private final String LOG_TAG = TestUtilities.class.getSimpleName();

    public static long getDate() {
        Time datetime = new Time();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), datetime.gmtoff);
        return datetime.setJulianDay(julianStartDay);
    }

    public static ContentValues createValue() {

        Time datetime = new Time();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), datetime.gmtoff);
        long dateTime = datetime.setJulianDay(julianStartDay);

        ContentValues values = new ContentValues();
        values.put(TVContract.TVEntry.COLUMN_NAME_ENTRY_ID, 123);
        values.put(TVContract.TVEntry.COLUMN_NAME_TITLE, "Deadwood");
        values.put(TVContract.TVEntry.COLUMN_NAME_POSTER, "poster");
        values.put(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS, "synopsis");
        values.put(TVContract.TVEntry.COLUMN_NAME_RATING, 8.9);
        values.put(TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE, dateTime);
        values.put(TVContract.TVEntry.COLUMN_NAME_DETAIL_POSTER, "detailPoster");

        return values;
    }

    public static ArrayList createValues(int count) {
        ArrayList values = new ArrayList();
        for (int i=0; i<count; i++) {
            Time datetime = new Time();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), datetime.gmtoff);
            long dateTime = datetime.setJulianDay(julianStartDay);

            ContentValues value = new ContentValues();
            value.put(TVContract.TVEntry.COLUMN_NAME_ENTRY_ID, i);
            value.put(TVContract.TVEntry.COLUMN_NAME_TITLE, "Deadwood"+i);
            value.put(TVContract.TVEntry.COLUMN_NAME_POSTER, "poster"+i);
            value.put(TVContract.TVEntry.COLUMN_NAME_SYNOPSIS, "synopsis"+i);
            value.put(TVContract.TVEntry.COLUMN_NAME_RATING, 8.9);
            value.put(TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE, dateTime);
            value.put(TVContract.TVEntry.COLUMN_NAME_DETAIL_POSTER, "detailPoster"+i);

            values.add(value);
        }
        return values;
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);

            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();

            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.
     */
    public static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        public static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(20000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    public static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
