package com.test.surfer.tvappdatabase.sync;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by surfer on 25/09/2015.
 */
public class TVAppSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static TVAppSyncAdapter sTVAppSyncAdapter = null;
    private static TVAppSyncService self = null;

    public final String LOG_TAG = TVAppSyncService.class.getSimpleName();

    public static TVAppSyncService getServiceObject(){
        return self;
    }

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sTVAppSyncAdapter == null) {
                sTVAppSyncAdapter = new TVAppSyncAdapter(getApplicationContext(), true);
            }
        }
        self = this;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sTVAppSyncAdapter.getSyncAdapterBinder();
    }

    public TVAppSyncAdapter getTVAppSyncAdapter() {
        return sTVAppSyncAdapter;
    }

    public class LocalBinder extends Binder {
        TVAppSyncService getService() {
            return TVAppSyncService.this;
        }
    }
}
