package com.pragyamutluru.uberdemo;
import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

/**
 * Created by Pragya on 09-10-2017.
 */

public class StarterActivity extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("2b4d51787ec29b7b7b3169dbd51ef8f404139104")
                .clientKey("f66883ca74d8da9e2f0195f7788a213e6b495592")
                .server("http://52.66.173.223:80/parse/")
                .build()
        );


       // ParseUser.enableAutomaticUser();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
