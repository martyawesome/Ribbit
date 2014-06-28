package com.martyawesome.smarty.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.ParseUser;

public class ProfileActivity extends Activity {
    public static final String TAG = ProfileActivity.class.getSimpleName();
    ParseUser mCurrentUser;
    TextView mUsernameTextView;
    TextView mFirstNameTextView;
    TextView mLastNameTextView;
    TextView mEmailTextView;
    Button mEditButton;
    String mUsername;
    String mFirstName;
    String mLastName;
    String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_profile);

        mUsernameTextView = (TextView) findViewById(R.id.username);
        mFirstNameTextView = (TextView) findViewById(R.id.firstName);
        mLastNameTextView = (TextView) findViewById(R.id.lastName);
        mEmailTextView = (TextView) findViewById(R.id.email);

        mEditButton = (Button) findViewById(R.id.editProfileButton);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra(ParseConstants.KEY_USERNAME, mUsername);
                intent.putExtra(ParseConstants.KEY_FIRST_NAME, mFirstName);
                intent.putExtra(ParseConstants.KEY_LAST_NAME, mLastName);
                intent.putExtra(ParseConstants.KEY_EMAIL, mEmail);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOnline()) {
            retrieveProfile();

        }else {
            Toast.makeText(ProfileActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
        }


    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void retrieveProfile (){
        mCurrentUser = ParseUser.getCurrentUser();
        mUsername = mCurrentUser.getUsername();
        mEmail = mCurrentUser.getEmail();
        mFirstName = mCurrentUser.getString(ParseConstants.KEY_FIRST_NAME);
        mLastName = mCurrentUser.getString(ParseConstants.KEY_LAST_NAME);

        mUsernameTextView.setText(mUsername);
        mFirstNameTextView.setText(mFirstName);
        mLastNameTextView.setText(mLastName);
        mEmailTextView.setText(mEmail);
    }
}
