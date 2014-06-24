package com.martyawesome.ribbit.app;

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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;


public class SignUpActivity extends Activity {

    protected EditText mUsername;
    protected EditText mPassword;
    protected EditText mEmail;
    protected EditText mFirstName;
    protected EditText mLastName;
    protected EditText mPhoneNumber;
    protected Button mSignUpButton;

    public static Boolean created = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_sign_up);

        mUsername = (EditText) findViewById(R.id.usernameField);
        mPassword = (EditText) findViewById(R.id.passwordField);
        mEmail = (EditText) findViewById(R.id.emailField);
        mFirstName = (EditText) findViewById(R.id.firstName);
        mLastName = (EditText) findViewById(R.id.lastName);
        mPhoneNumber = (EditText) findViewById(R.id.phoneNumber);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);


        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String firstNamePiece = mFirstName.getText().toString();
                final String firstName = firstNamePiece.substring(0,1).toUpperCase() + firstNamePiece.substring(1);
                String lastNamePiece = mLastName.getText().toString();
                final String lastName = lastNamePiece.substring(0,1).toUpperCase() + lastNamePiece.substring(1);
                final String phoneNumber = mPhoneNumber.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()
                        || firstName.isEmpty() || lastName.isEmpty() || phoneNumber.isEmpty()) {
                    if (username.isEmpty()) {
                        mUsername.setError(getString(R.string.error_field_required));
                    }
                    if (password.isEmpty()) {
                        mPassword.setError(getString(R.string.error_field_required));
                    }
                    if (email.isEmpty()) {
                        mEmail.setError(getString(R.string.error_field_required));
                    }
                    if (firstName.isEmpty()) {
                        mFirstName.setError(getString(R.string.error_field_required));
                    }
                    if (lastName.isEmpty()) {
                        mLastName.setError(getString(R.string.error_field_required));
                    }
                    if (phoneNumber.isEmpty()) {
                        mPhoneNumber.setError(getString(R.string.error_field_required));
                    }
                } else {
                    if (isOnline()) {
                        ParseUser newUser = new ParseUser();
                        newUser.setUsername(username);
                        newUser.setPassword(password);
                        newUser.setEmail(email);
                        final String finalUsername = username;
                        newUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    ParseObject newUserInfo = new ParseObject(ParseConstants.CLASS_USER_INFO);
                                    newUserInfo.put(ParseConstants.KEY_USERNAME, finalUsername);
                                    newUserInfo.put(ParseConstants.KEY_FIRST_NAME, firstName);
                                    newUserInfo.put(ParseConstants.KEY_LAST_NAME, lastName);
                                    newUserInfo.put(ParseConstants.KEY_PHONE_NUMBER, phoneNumber);
                                    newUserInfo.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            } else {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                                builder.setMessage(R.string.signup_error_message)
                                                        .setTitle(R.string.signup_error_title)
                                                        .setPositiveButton(android.R.string.ok, null);
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                            }
                                        }
                                    });
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                    builder.setMessage(R.string.signup_error_message)
                                            .setTitle(R.string.signup_error_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }

                            }
                        });


                        /**/


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
