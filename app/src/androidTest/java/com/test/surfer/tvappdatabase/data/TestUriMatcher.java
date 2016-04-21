package com.test.surfer.tvappdatabase.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by surfer on 25/09/2015.
 */
public class TestUriMatcher extends AndroidTestCase {
    private static final long TV_QUERY = 1;

    // content://com.test.surfer.tvappdatabase/tv"
    private static final Uri TEST_TV_DIR = TVContract.TVEntry.CONTENT_URI;
    private static final Uri TEST_TV_WITH_ID = TVContract.TVEntry.buildTvUri(TV_QUERY);

    /*
        This function tests that UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.  Uncomment this when you are
        ready to test your UriMatcher.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = TVProvider.buildUriMatcher();

        assertEquals("Error: The TV URI was matched incorrectly.",
                testMatcher.match(TEST_TV_DIR), TVProvider.TV);
        assertEquals("Error: The TEST_TV_WITH_ID URI was matched incorrectly.",
                testMatcher.match(TEST_TV_WITH_ID), TVProvider.TV_WITH_ID);
    }
}
