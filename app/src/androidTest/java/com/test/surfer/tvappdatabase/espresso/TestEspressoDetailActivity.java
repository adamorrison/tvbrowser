package com.test.surfer.tvappdatabase.espresso;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.test.surfer.tvappdatabase.MainActivity;
import com.test.surfer.tvappdatabase.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

/**
 * Created by surfer on 31/01/2016.
 */
@RunWith(AndroidJUnit4.class)
public class TestEspressoDetailActivity {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    // DETAIL TEST: Tests that the menu action to share is usable as expected
    @Test
    public void testFragmentAction(){
        onData(anything()).atPosition(1).perform(click());
        // Click the action bar.
        onView(withContentDescription("Share with")).perform(click());
    }

    // DETAIL TEST: Tests that after clicking an item in the grid view, the specific item title is displayed
    @Test
    public void testFragmentClickAndTitleDisplay(){
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.detail_title)).check(matches(isDisplayed()));
    }

    // DETAIL TEST: Tests that after clicking an item in the grid view, the specific item title is displayed
    @Test
    public void testFragmentClickAndDescDisplay(){
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.description)).check(matches(isDisplayed()));
    }

    // DETAIL TEST: Tests that after clicking an item in the grid view, the specific item title is displayed
    @Test
    public void testFragmentClickAndRatingDisplay(){
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.rating)).check(matches(isDisplayed()));
    }

    // DETAIL TEST: Tests that after clicking an item in the grid view, the specific item title is displayed
    @Test
    public void testFragmentClickAndReleaseDisplay(){
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.releaseDate)).check(matches(isDisplayed()));
    }

    // DETAIL TEST: Tests that after clicking an item in the grid view, the specific item title is displayed
    @Test
    public void testFragmentClickAndImageDisplay(){
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.poster)).check(matches(isDisplayed()));
    }

    // SETTINGS TEST: Confirm the settings option is usable as expected
    @Test
    public void testSettingsClickFromDetail() {
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
