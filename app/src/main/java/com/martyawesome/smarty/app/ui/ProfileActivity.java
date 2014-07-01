package com.martyawesome.smarty.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
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
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_profile);

        mUsernameTextView = (TextView) findViewById(R.id.usernameValue);
        mFirstNameTextView = (TextView) findViewById(R.id.firstName);
        mLastNameTextView = (TextView) findViewById(R.id.lastName);
        mEmailTextView = (TextView) findViewById(R.id.emailValue);

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
        if (isOnline()) {
            retrieveProfile();
        } else {
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

    public void retrieveProfile() {
        mCurrentUser = ParseUser.getCurrentUser();
        mUsername = mCurrentUser.getUsername();
        mEmail = mCurrentUser.getEmail();
        mFirstName = mCurrentUser.getString(ParseConstants.KEY_FIRST_NAME);
        mLastName = mCurrentUser.getString(ParseConstants.KEY_LAST_NAME);

        mUsernameTextView.setText(mUsername);
        mFirstNameTextView.setText(mFirstName);
        mLastNameTextView.setText(mLastName);
        mEmailTextView.setText(mEmail);

        mImageView = (ImageView) findViewById(R.id.imageView);
        ParseFile applicantResume = (ParseFile) mCurrentUser.get(ParseConstants.KEY_PROFILE_PICTURE);
        applicantResume.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, com.parse.ParseException e) {
                if (e == null) {
                    BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                    options.inPurgeable = true; // inPurgeable is used to free up memory while required
                    mImageView.setImageBitmap(null);
                    mImageView.setImageDrawable(null);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    mImageView.setImageBitmap(bmp);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

        });

    }
}
