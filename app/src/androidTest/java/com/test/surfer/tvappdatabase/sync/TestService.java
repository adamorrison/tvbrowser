package com.test.surfer.tvappdatabase.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.PeriodicSync;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.test.ServiceTestCase;

import com.test.surfer.tvappdatabase.R;
import com.test.surfer.tvappdatabase.data.TVContract;

import org.json.JSONException;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created by surfer on 09/11/2015.
 */
public class TestService extends ServiceTestCase<TVAppSyncService> {

    public TestService() {
        super(TVAppSyncService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // start the service/sync
        Intent startIntent = new Intent(getSystemContext(), TVAppSyncService.class);
        startIntent.setClass(getSystemContext(), TVAppSyncService.class);
        startService(startIntent);
        getContext().getContentResolver().setSyncAutomatically(getService().getTVAppSyncAdapter().getSyncAccount(getContext()),TVContract.CONTENT_AUTHORITY,true);
    }

    /**
     * Test basic startup of Service
     */
    public void testOnCreate() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TVAppSyncService.class);
        startService(startIntent);

        assertNotNull(getService().getTVAppSyncAdapter());
    }

    public void testOnBind() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TVAppSyncService.class);
        IBinder service = bindService(startIntent);

        assertNotNull(service);
    }

    public void testIsSyncable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TVAppSyncService.class);
        startService(startIntent);

        // confirm the content provider is syncable after the service/sync adapter setup
        assertTrue(getContext().getContentResolver().getIsSyncable(getService().getTVAppSyncAdapter().getSyncAccount(getContext()), TVContract.CONTENT_AUTHORITY) > 0);
    }

    public void testSyncAccount() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TVAppSyncService.class);
        startService(startIntent);

        // Get an instance of the Android account manager and create the account type and default account
        AccountManager accountManager =
                (AccountManager) getContext().getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(
                getContext().getString(R.string.app_name), getContext().getString(R.string.sync_account_type));
        accountManager.addAccountExplicitly(newAccount, "", null);

        // check the dummy account created above matches the account created automatically by the sync adapter at startup
        assertEquals("Error: accounts mistmatch", newAccount, getService().getTVAppSyncAdapter().getSyncAccount(getContext()));
    }

    public void testSyncProperties() {

        // start the service/sync
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), TVAppSyncService.class);
        startService(startIntent);

        // test interval
        long interval = Long.valueOf(getService().getTVAppSyncAdapter().SYNC_INTERVAL);

        // test authority
        String authority = TVContract.CONTENT_AUTHORITY;

        // confirm there is exactly 1 periodic sync configured after the sync adapter setup/request
        Account account = getService().getTVAppSyncAdapter().getSyncAccount(getContext());
        ArrayList<PeriodicSync> syncs = (ArrayList<PeriodicSync>) getContext().getContentResolver().getPeriodicSyncs(account,authority);
        assertEquals("The number of periodic syncs does not equal 1", 1, syncs.size());

        // confirm the values of the authority, account and interval of the configured sync are as expected
        for (PeriodicSync ps : syncs) {
            assertEquals("Authority mismatch", ps.authority, authority);
            assertEquals("Startime mismatch", ps.period, interval);
        }
    }

    public void testJSONNotNull() {
        TVAppSyncAdapter syncAdapter = new TVAppSyncAdapter(getContext(), true);

        Account account = syncAdapter.getSyncAccount(getContext());
        String authority = TVContract.CONTENT_AUTHORITY;
        syncAdapter.onPerformSync(account, new Bundle(), authority, getContext().getContentResolver().acquireContentProviderClient(authority), new SyncResult());

        String json = syncAdapter.getRawJSON();
        assertNotNull(json);

        /*TODO: Confirm a non-null json response and confirm the structure of the response is as expected, based on SyncAdapter parse method.
            May need to mock the relevant activity classes. At the moment, the json response object is null, even though the syncadapter is created
            in setup. The 'performSync' method never appears to be called.
         */
    }

    @Test(expected = JSONException.class)
    public void testJSONParsedOK() {
        TVAppSyncAdapter syncAdapter = new TVAppSyncAdapter(getContext(), true);

        Account account = syncAdapter.getSyncAccount(getContext());
        String authority = TVContract.CONTENT_AUTHORITY;
        syncAdapter.onPerformSync(account, new Bundle(), authority, getContext().getContentResolver().acquireContentProviderClient(authority), new SyncResult());

        assertTrue(syncAdapter.getNumOfDataValues()>0);
    }

}
