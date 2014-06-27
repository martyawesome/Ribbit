package com.martyawesome.smarty.app;

import android.app.Application;

import com.martyawesome.smarty.app.ui.MainActivity;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.PushService;

/**
 * Created by User on 6/18/2014.
 */
public class SmartyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "HAaoLx99dskeNiNF8XJwSNQgTk1roImA3dOeTSEZ", "BjuwGikCxpyYQPa8f37R4PubQUSQahrni4qviVYx");

        //PushService.setDefaultPushCallback(this, MainActivity.class);
        PushService.setDefaultPushCallback(this, MainActivity.class, R.drawable.ic_launcher);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    public static void updateParseInstallation (ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put(ParseConstants.KEY_USER_ID, user.getObjectId());
        installation.saveInBackground();
    }
}
