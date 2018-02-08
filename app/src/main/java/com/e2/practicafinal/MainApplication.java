package com.e2.practicafinal;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by mcvasquez on 2/8/18.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
