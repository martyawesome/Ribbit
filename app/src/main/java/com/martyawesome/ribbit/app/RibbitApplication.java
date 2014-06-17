package com.martyawesome.ribbit.app;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by User on 6/18/2014.
 */
public class RibbitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "HAaoLx99dskeNiNF8XJwSNQgTk1roImA3dOeTSEZ", "BjuwGikCxpyYQPa8f37R4PubQUSQahrni4qviVYx");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
    }
}
