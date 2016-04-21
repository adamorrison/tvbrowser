import android.content.ContentResolver;

import com.test.surfer.tvappdatabase.BuildConfig;
import com.test.surfer.tvappdatabase.MainActivity;
import com.test.surfer.tvappdatabase.data.TVContract;
import com.test.surfer.tvappdatabase.data.TVProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.shadows.ShadowLog;

import static org.junit.Assert.assertTrue;

/**
 * Created by surfer on 12/11/2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    private ContentResolver mContentResolver;
    private ShadowContentResolver mShadowContentResolver;
    private TVProvider mProvider;

    @Before
    public void setup() throws Exception {
        ShadowLog.stream = System.out;

        mProvider = new TVProvider();
        //mContentResolver = Robolectric.application.getContentResolver();
        //mShadowContentResolver = Robolectric.shadowOf(mContentResolver);
        mProvider.onCreate();
        ShadowContentResolver.registerProvider(TVContract.CONTENT_AUTHORITY, mProvider);
    }

    @Test
    public void testSomething() throws Exception {
        //assertTrue(Robolectric.buildActivity(MainActivity.class) != null);
        MainActivity main = Robolectric.buildActivity(MainActivity.class).create().get();
        assertTrue(main != null);
    }
}
