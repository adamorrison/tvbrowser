package com.test.surfer.tvappdatabase.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by surfer on 12/09/2015.
 */
public class TestTVContract extends AndroidTestCase {

    public static final long TEST_ID = 12345;
    public static final String TEST_PROG = "/The Wire";

    public void testBuildTVUri() {

        Uri tvUri = TVContract.TVEntry.buildTvUri(TEST_ID);

        assertNotNull("Error: Null Uri returned.  You must fill-in buildTvUri in " +
                "TVContract.", tvUri);

        assertEquals("Error: Error: TV ID not properly appended to the end of the Uri",
                Long.toString(TEST_ID), tvUri.getLastPathSegment());

        assertEquals("Error: TV Uri doesn't match our expected result",
                tvUri.toString(),
                "content://com.test.surfer.tvappdatabase/tv/12345");
    }

    public void testGetIDFromUri() {
        Uri tvUri2 = TVContract.TVEntry.buildTvUri(TEST_ID);
        assertEquals("ERROR: URI mismatch", TVContract.TVEntry.getIDFromUri(tvUri2),Long.toString(TEST_ID));
    }
}
