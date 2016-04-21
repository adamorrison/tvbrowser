package com.test.surfer.tvappdatabase;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

/**
 * Created by surfer on 24/10/2015.
 */
public class Utility {
    public static String getPreferredTVCategory(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_list_view),
                context.getString(R.string.pref_list_view_most_rated));
    }

    static long getDate() {
        Time datetime = new Time();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), datetime.gmtoff);
        return datetime.setJulianDay(julianStartDay);
    }

    public static String formatRating(String rating) {
        return "Rating: " + rating;
    }
}
