package com.test.surfer.tvappdatabase.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by surfer on 25/09/2015.
 */
public class TVAppAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private TVAppAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new TVAppAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
