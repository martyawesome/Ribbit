package com.martyawesome.smarty.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.martyawesome.ribbit.app.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ForgotPasswordActivity extends Activity {
    public static final String TAG = ForgotPasswordActivity.class.getSimpleName();

    EditText mEmail;
    Button mButton;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ActionBar actionBar = getActionBar();
        actionBar.hide();

        mEmail = (EditText) findViewById(R.id.emailField);
        mButton = (Button) findViewById(R.id.password_button);

        Button mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
                progressDialog.setMessage(getString(R.string.getting_password));
                progressDialog.setCancelable(false);
                progressDialog.show();
                ParseUser.requestPasswordResetInBackground(email,
                        new RequestPasswordResetCallback() {
                            public void done(ParseException e) {
                                progressDialog.dismiss();
                                if (e == null) {
                                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.password_sent), Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordActivity.this);
                                    builder.setTitle(R.string.error_title).setMessage(R.string.change_password_error)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
                            }
                        }
                );

            }
        });
    }
}
