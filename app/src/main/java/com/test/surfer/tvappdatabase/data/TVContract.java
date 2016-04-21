package com.test.surfer.tvappdatabase.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by surfer on 29/08/2015.
 */
public class TVContract {

    public TVContract() {}

    // name for the content provider
    public static final String CONTENT_AUTHORITY = "com.test.surfer.tvappdatabase";

    // the base of all uris which apps will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // path defined for accessing the content provider
    public static final String PATH_TV = "tv";

    /* Inner class defining the table contents for the tv table */
    public static final class TVEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TV).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TV;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TV;

        // table name
        public static final String TABLE_NAME = "tv";

        public static final String COLUMN_NAME_ENTRY_ID = "entryid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_SYNOPSIS = "synopsis";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_RELEASE_DATE = "releasedate";
        public static final String COLUMN_NAME_DETAIL_POSTER = "detailposter";

        public static Uri buildTvUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIDFromUri(Uri uri) {
            return Long.toString(ContentUris.parseId(uri));
        }
    }



}
