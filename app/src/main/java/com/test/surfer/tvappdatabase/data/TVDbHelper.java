package com.test.surfer.tvappdatabase.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by surfer on 29/08/2015.
 */
public class TVDbHelper extends SQLiteOpenHelper {

    private final String LOG_TAG = TVDbHelper.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "tv.db";

    public TVDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TV_TABLE = "CREATE TABLE " + TVContract.TVEntry.TABLE_NAME + " (" +
                TVContract.TVEntry._ID + " INTEGER PRIMARY KEY, " +
                TVContract.TVEntry.COLUMN_NAME_ENTRY_ID + " TEXT UNIQUE NOT NULL, " +
                TVContract.TVEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL, " +
                TVContract.TVEntry.COLUMN_NAME_POSTER + " TEXT NOT NULL, " +
                TVContract.TVEntry.COLUMN_NAME_SYNOPSIS + " TEXT NOT NULL, " +
                TVContract.TVEntry.COLUMN_NAME_RATING + " TEXT NOT NULL, " +
                TVContract.TVEntry.COLUMN_NAME_RELEASE_DATE + " TEXT NOT NULL, " +
                TVContract.TVEntry.COLUMN_NAME_DETAIL_POSTER + " TEXT NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_TV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        /* This database is only a cache for online data, so its upgrade policy is
        to simply to discard the data and start over

        Note that this only fires if you change the version number for your database.
        It does NOT depend on the version number for your application.

        If you want to update the schema without wiping data, commenting out the next 2 lines
        should be your top priority before modifying this method.
         */
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TVContract.TVEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
