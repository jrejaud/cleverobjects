package com.crejaud.jrejaud.cleverobjects;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by jrejaud on 12/29/15.
 */
public class CleverObjectsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
