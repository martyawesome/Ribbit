package com.martyawesome.smarty.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.utils.FileHelper;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditProfileActivity extends Activity {
    public static final String TAG = ProfileActivity.class.getSimpleName();
    Button mEditButton;
    String mUsername;
    String mFirstName;
    String mLastName;
    String mEmail;

    ParseUser mCurrentUser;

    EditText mUsernameEditText;
    EditText mFirstNameEditText;
    EditText mLastNameEditText;
    EditText mEmailEditText;
    ImageButton mImageButton;

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int PICK_PHOTO_REQUEST = 2;
    public static final int MEDIA_TYPE_IMAGE = 4;
    protected Uri mMediaUri;

    public DialogInterface.OnClickListener mDialogListener;

    {
        mDialogListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0: //Take Picture
                        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                        if (mMediaUri == null) {
                            //display error
                            Toast.makeText(EditProfileActivity.this, R.string.error_external_storage, Toast.LENGTH_LONG).show();
                        } else {
                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
                            startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                        }
                        break;
                    case 1: //Choose Picture
                        Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        choosePhotoIntent.setType("image/*");
                        Toast.makeText(EditProfileActivity.this, getString(R.string.video_file_size_warning), Toast.LENGTH_LONG).show();
                        startActivityForResult(choosePhotoIntent, PICK_PHOTO_REQUEST);
                        break;
                }
            }

            private Uri getOutputMediaFileUri(int mediaType) {
                //To be safe, you should check that the SDCard is mounted
                //using Environment.getExternalStorageState() before doing this
                if (isExternalStorageAvailable()) {
                    //get the Uri

                    //1. Get the external storage directory
                    String appName = EditProfileActivity.this.getString(R.string.app_name);
                    File mediaStorageDir = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            appName);

                    //2. Create our subdirectory
                    if (!mediaStorageDir.exists()) {
                        if (mediaStorageDir.mkdir()) {
                            Log.e(TAG, "Failed to create directory ");
                            return null;
                        }
                    }

                    //3. Create a file name
                    //4. Create the file
                    File mediaFile;
                    Date now = new Date();
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(now);

                   String path = mediaStorageDir.getPath() + File.separator + "IMG_" + timestamp + ".jpg";

                    if (mediaType == MEDIA_TYPE_IMAGE) {
                        mediaFile = new File(path);
                    } else {
                        return null;
                    }

                    Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
                    //5. Return the file uri
                    return Uri.fromFile(mediaFile);
                } else {
                    return null;
                }
            }

            private boolean isExternalStorageAvailable() {
                String state = Environment.getExternalStorageState();
                if (state.equals(Environment.MEDIA_MOUNTED)) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_edit_profile);

        initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_PHOTO_REQUEST) {
                if (data == null) {
                    Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_LONG).show();
                } else {
                    mMediaUri = data.getData();
                }
                Log.i(TAG, "Media URI: " + mMediaUri);
            } else {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            Picasso.with(this).load(mMediaUri.toString()).into(mImageButton);

        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.general_error, Toast.LENGTH_LONG).show();
        }
    }


    public void editProfile() {
        final ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setMessage(getString(R.string.editing_profile));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ParseUser editUser = ParseUser.getCurrentUser();
        editUser.put(ParseConstants.KEY_USERNAME, mUsernameEditText.getText().toString());
        editUser.put(ParseConstants.KEY_FIRST_NAME, mFirstNameEditText.getText().toString());
        editUser.put(ParseConstants.KEY_LAST_NAME, mLastNameEditText.getText().toString());
        editUser.put(ParseConstants.KEY_EMAIL, mEmailEditText.getText().toString());

        if(mMediaUri!=null) {
            byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);
            ParseFile file = new ParseFile(mUsernameEditText.getText().toString() + ".jpg", fileBytes);
            file.saveInBackground();
            editUser.put(ParseConstants.KEY_PROFILE_PICTURE, file);
        }

        editUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                progressDialog.dismiss();
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

    public void initialize(){
        mCurrentUser = ParseUser.getCurrentUser();
        mUsername = mCurrentUser.getUsername();
        mEmail = mCurrentUser.getEmail();
        mFirstName = mCurrentUser.getString(ParseConstants.KEY_FIRST_NAME);
        mLastName = mCurrentUser.getString(ParseConstants.KEY_LAST_NAME);

        mUsernameEditText = (EditText) findViewById(R.id.username);
        mFirstNameEditText = (EditText) findViewById(R.id.firstName);
        mLastNameEditText = (EditText) findViewById(R.id.lastName);
        mEmailEditText = (EditText) findViewById(R.id.email);

        mEditButton = (Button) findViewById(R.id.editProfileButton);

        mUsernameEditText.setText(mUsername);
        mFirstNameEditText.setText(mFirstName);
        mLastNameEditText.setText(mLastName);
        mEmailEditText.setText(mEmail);

        mImageButton = (ImageButton) findViewById(R.id.imageButton);

        ParseFile applicantResume = (ParseFile) mCurrentUser.get(ParseConstants.KEY_PROFILE_PICTURE);
        applicantResume.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, com.parse.ParseException e) {
                if (e == null) {
                    BitmapFactory.Options options=new BitmapFactory.Options();// Create object of bitmapfactory's option method for further option use
                    options.inPurgeable = true; // inPurgeable is used to free up memory while required
                    mImageButton.setImageBitmap(null);
                    mImageButton.setImageDrawable(null);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
                    mImageButton.setImageBitmap(bmp);
                    bmp=null;
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

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOnline()) {
                    editProfile();

                } else {
                    Toast.makeText(EditProfileActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
                }
            }
        });

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setItems(R.array.photo_choices, mDialogListener);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

}
