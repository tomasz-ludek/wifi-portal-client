package com.dashngo.android;

import android.app.Application;

import com.activeandroid.ActiveAndroid;
import com.crashlytics.android.Crashlytics;
import com.dashngo.android.tools.Configuration;

import io.fabric.sdk.android.Fabric;

public class DashNGoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        Configuration.init(getApplicationContext());
        ActiveAndroid.initialize(this, true);
    }
}
