package com.martyawesome.smarty.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditProfileActivity extends Activity {
    public static final String TAG = ProfileActivity.class.getSimpleName();
    Button mEditButton;
    String mUsername;
    String mFirstName;
    String mLastName;
    String mEmail;
    EditText mUsernameEditText;
    EditText mFirstNameEditText;
    EditText mLastNameEditText;
    EditText mEmailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_profile);

        Intent intent = getIntent();
        mUsername = intent.getStringExtra(ParseConstants.KEY_USERNAME);
        mFirstName = intent.getStringExtra(ParseConstants.KEY_FIRST_NAME);
        mLastName = intent.getStringExtra(ParseConstants.KEY_LAST_NAME);
        mEmail = intent.getStringExtra(ParseConstants.KEY_EMAIL);

        mUsernameEditText = (EditText) findViewById(R.id.username);
        mFirstNameEditText = (EditText) findViewById(R.id.firstName);
        mLastNameEditText = (EditText) findViewById(R.id.lastName);
        mEmailEditText = (EditText) findViewById(R.id.email);

        mEditButton = (Button) findViewById(R.id.editProfileButton);

        mUsernameEditText.setText(mUsername);
        mFirstNameEditText.setText(mFirstName);
        mLastNameEditText.setText(mLastName);
        mEmailEditText.setText(mEmail);

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProgressBarIndeterminateVisibility(true);
                if (isOnline()) {
                    editProfile();

                }
                else {
                    setProgressBarIndeterminateVisibility(false);
                    Toast.makeText(EditProfileActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void editProfile(){
        setProgressBarIndeterminateVisibility(true);;

        ParseUser editUser = ParseUser.getCurrentUser();
        editUser.put(ParseConstants.KEY_USERNAME, mUsernameEditText.getText().toString());
        editUser.put(ParseConstants.KEY_FIRST_NAME, mFirstNameEditText.getText().toString());
        editUser.put(ParseConstants.KEY_LAST_NAME, mLastNameEditText.getText().toString());
        editUser.put(ParseConstants.KEY_EMAIL, mEmailEditText.getText().toString());

        editUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setMessage(R.string.activity_edit_profile_success)
                            .setTitle(R.string.success)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setMessage(e.getMessage())
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
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
}
