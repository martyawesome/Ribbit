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
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class ViewProfileActivity extends Activity {
    public static final String TAG = ViewProfileActivity.class.getSimpleName();

    TextView mUsernameTextView;
    TextView mFirstNameTextView;
    TextView mLastNameTextView;
    TextView mEmailTextView;
    Button mEditButton;
    String mUsername;
    ImageView mImageView;
    Boolean mFriends;
    ParseRelation mFriendsRelation;
    ParseUser mCurrentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_profile);

        mUsernameTextView = (TextView) findViewById(R.id.usernameValue);
        mFirstNameTextView = (TextView) findViewById(R.id.firstName);
        mLastNameTextView = (TextView) findViewById(R.id.lastName);
        mEmailTextView = (TextView) findViewById(R.id.emailValue);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent myIntent = getIntent(); // gets the previously created intent
        mUsername = myIntent.getStringExtra(ParseConstants.KEY_USERNAME);
        Bundle bundle = getIntent().getExtras();
        mFriends = bundle.getBoolean("friends");

        mEditButton = (Button) findViewById(R.id.editProfileButton);

        if (isOnline()) {
            retrieveProfile();
        } else {
            Toast.makeText(ViewProfileActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
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
        setProgressBarIndeterminateVisibility(true);


        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_USER);
        query.whereEqualTo(ParseConstants.KEY_USERNAME, mUsername);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (e == null || parseObject != null) {
                    mUsernameTextView.setText(parseObject.getString(ParseConstants.KEY_USERNAME));
                    mFirstNameTextView.setText(parseObject.getString(ParseConstants.KEY_FIRST_NAME));
                    mLastNameTextView.setText(parseObject.getString(ParseConstants.KEY_LAST_NAME));
                    mEmailTextView.setText(parseObject.getString(ParseConstants.KEY_EMAIL));

                    mImageView = (ImageView) findViewById(R.id.imageView);
                    ParseFile imageFile = (ParseFile) parseObject.get(ParseConstants.KEY_PROFILE_PICTURE);
                    imageFile.getDataInBackground(new GetDataCallback() {
                        public void done(byte[] bytes, ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            if (e == null) {
                                BitmapFactory.Options options = new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                                options.inPurgeable = true; // inPurgeable is used to free up memory while required
                                mImageView.setImageBitmap(null);

                                mImageView.setImageDrawable(null);
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                                mImageView.setImageBitmap(bmp);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfileActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.signup_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });

                    if (!mFriends) {
                        mEditButton.setText(R.string.accept_request);
                        mEditButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mCurrentUser = ParseUser.getCurrentUser();
                                mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
                                mFriendsRelation.add(parseObject);

                                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
                                query.whereEqualTo(ParseConstants.KEY_REQUEST_FROM, parseObject.getString(ParseConstants.KEY_USERNAME));
                                query.whereEqualTo(ParseConstants.KEY_REQUEST_TO, ParseUser.getCurrentUser().getUsername());
                                query.getFirstInBackground(new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(ParseObject parseObject, ParseException e) {
                                        if(e==null) {
                                            parseObject.deleteInBackground();
                                            //finish();
                                            //Intent intent = new Intent(ViewProfileActivity.this, ViewProfileActivity.class);
                                            //startActivity(intent);
                                        }
                                        else{
                                            Toast.makeText(ViewProfileActivity.this, e.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                    else{
                        mEditButton.setText(R.string.view_profile);
                        mEditButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        });
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfileActivity.this);
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