package com.martyawesome.smarty.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import com.martyawesome.smarty.app.SmartyApplication;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends Activity {

    protected EditText mUsernameEditText;
    protected EditText mPasswordEditText;
    protected EditText mEmailEditText;
    protected EditText mFirstNameEditText;
    protected EditText mLastNameEditText;
    protected Button mSignUpButton;
    protected Button mCancelButton;

    public String mUsername;
    public String mPassword;
    public String mEmail;
    public String mFirstName;
    public String mLastName;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_sign_up);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        mUsernameEditText = (EditText) findViewById(R.id.usernameField);
        mPasswordEditText = (EditText) findViewById(R.id.passwordField);
        mEmailEditText = (EditText) findViewById(R.id.emailField);
        mFirstNameEditText = (EditText) findViewById(R.id.firstName);
        mLastNameEditText = (EditText) findViewById(R.id.lastName);

        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsername = mUsernameEditText.getText().toString();
                mPassword = mPasswordEditText.getText().toString();
                mEmail = mEmailEditText.getText().toString();
                String firstNamePiece = mFirstNameEditText.getText().toString();
                mFirstName = firstNamePiece.substring(0, 1).toUpperCase() + firstNamePiece.substring(1);
                String lastNamePiece = mLastNameEditText.getText().toString();
                mLastName = lastNamePiece.substring(0, 1).toUpperCase() + lastNamePiece.substring(1);
                mUsername = mUsername.trim();
                mPassword = mPassword.trim();
                mEmail = mEmail.trim();

                if (mUsername.isEmpty() || mPassword.isEmpty() || mEmail.isEmpty()
                        || mFirstName.isEmpty() || mLastName.isEmpty()) {
                    if (mUsername.isEmpty()) {
                        mUsernameEditText.setError(getString(R.string.error_field_required));
                    }
                    if (mPassword.isEmpty()) {
                        mPasswordEditText.setError(getString(R.string.error_field_required));
                    }
                    if (mEmail.isEmpty()) {
                        mEmailEditText.setError(getString(R.string.error_field_required));
                    }
                    if (mFirstName.isEmpty()) {
                        mFirstNameEditText.setError(getString(R.string.error_field_required));
                    }
                    if (mLastName.isEmpty()) {
                        mLastNameEditText.setError(getString(R.string.error_field_required));
                    }
                } else {
                    if (isOnline()) {

                        mProgressDialog = new ProgressDialog(SignUpActivity.this);
                        mProgressDialog.setMessage(getString(R.string.creating_account));
                        mProgressDialog.setCancelable(false);
                        mProgressDialog.show();

                        ParseQuery<ParseObject> queryCount = new ParseQuery<ParseObject>(ParseConstants.CLASS_USER);
                        queryCount.countInBackground(new CountCallback() {
                            @Override
                            public void done(int count, ParseException e) {
                                if (e == null) {
                                    ParseUser newUser = new ParseUser();
                                    newUser.setUsername(mUsername);
                                    newUser.setPassword(mPassword);
                                    newUser.setEmail(mEmail);
                                    newUser.put(ParseConstants.KEY_FIRST_NAME, mFirstName);
                                    newUser.put(ParseConstants.KEY_LAST_NAME, mLastName);
                                    newUser.put(ParseConstants.KEY_NUMBER_ID, count+1);

                                    newUser.signUpInBackground(new SignUpCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mProgressDialog.dismiss();
                                            if (e == null) {
                                                SmartyApplication.updateParseInstallation(ParseUser.getCurrentUser());
                                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                Toast.makeText(SignUpActivity.this,getString(R.string.signup_success_message),Toast.LENGTH_LONG).show();
                                            } else {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                                builder.setMessage(e.getMessage())
                                                        .setTitle(R.string.signup_error_title)
                                                        .setPositiveButton(android.R.string.ok, null);
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }

                                        }
                                    });
                                } else {

                                }
                            }
                        });


                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                        builder.setMessage(R.string.internet_error)
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
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
