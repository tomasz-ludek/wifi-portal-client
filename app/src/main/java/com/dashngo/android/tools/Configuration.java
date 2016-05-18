package com.dashngo.android.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Configuration {

    private static final String PREF_KEY_FIRST_RUN = "pref_key_first_run";

    private static SharedPreferences preferences;

    private static Configuration instance;

    public static void init(Context context) {
        if (instance == null) {
            instance = new Configuration(context);
        } else {
            throw new IllegalStateException("Multiple initialisation not supported");
        }
    }

    public static Configuration getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Use init(Context) method first");
        }
        return instance;
    }

    private Configuration(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isFirstRun() {
        return preferences.getBoolean(PREF_KEY_FIRST_RUN, true);
    }

    public void setIsFirstRun(boolean isFirstRun) {
        preferences.edit().putBoolean(PREF_KEY_FIRST_RUN, isFirstRun).apply();
    }
}
