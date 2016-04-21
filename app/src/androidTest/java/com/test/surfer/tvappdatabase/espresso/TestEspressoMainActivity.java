package com.test.surfer.tvappdatabase.espresso;

/**
 * Created by surfer on 12/11/2015.
 */

import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.test.surfer.tvappdatabase.MainActivity;
import com.test.surfer.tvappdatabase.R;
import com.test.surfer.tvappdatabase.data.TVContract;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static android.support.test.espresso.action.ViewActions.*;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;


@RunWith(AndroidJUnit4.class)
public class TestEspressoMainActivity {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    // Tests the initial gridView is displayed
    @Test
    public void testGridViewIsDisplayed() {
        onView(ViewMatchers.withId(R.id.gridView)).check(matches(isDisplayed()));
    }

    // Test the swipe on gridView
    @Test
    public void testGridViewSwipe() {
        onView(withId(R.id.gridView)).perform(swipeUp());
    }

    // A test to confirm that images are displayed in the grid on startup
    @Test
    public void testGridViewImages() {
        /* use of the content desc from the cursor data gets us the specific imageview.
        Use of the 'anyOf' is an or statement for matching multiple content descriptions if necessary
         */
        final int COL_TITLE = 2;
        Cursor cursor = mActivityRule.getActivity().getContentResolver().query(
                TVContract.TVEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        cursor.moveToFirst();
        onView(anyOf(withContentDescription(cursor.getString(COL_TITLE)))).check(matches(isDisplayed()));
    }

    // Performs a click on the 20th item in the grid view, confirming that the correct # of items have been pulled in
    @Test
    public void testGridTotalSize() {
        //performs a click action on the 20th item in the grid view
        onData(anything()).inAdapterView(withId(R.id.gridView)).atPosition(19).perform(click());
    }

    // SETTINGS TEST: Confirm the settings option is usable as expected
    @Test
    public void testSettingsClickFromMain() {
        // Click the settings.
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText("Settings")).perform(click());

        // click the display category, followed by the top rated option
        onView(withText("TV Series Category")).perform(click());
        onView(withText("Top Rated")).perform(click());

        // check the preference value for the display category matches our selection above
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        assertThat("pref: category mismatch1", prefs.getString("viewType", "fail"), is("top_rated"));

        // click the display category, followed by the most popular option
        onView(withText("TV Series Category")).perform(click());
        onView(withText("Most Popular")).perform(click());

        // check the preference value for the display category matches our selection above
        prefs.getString("pref_list_view", "top_rated");
        assertThat("pref: category mismatch2", prefs.getString("viewType", "fail"), is("popular"));
    }
}
