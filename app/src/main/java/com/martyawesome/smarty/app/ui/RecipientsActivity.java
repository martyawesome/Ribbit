package com.martyawesome.smarty.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.adapters.UserAdapter;
import com.martyawesome.smarty.app.utils.FileHelper;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends Activity {

    public static final String TAG = RecipientsActivity.class.getSimpleName();

    protected ParseRelation<ParseUser> mFriendsRelation;
    protected List<ParseUser> mFriends;
    protected ParseUser mCurrentUser;
    protected Uri mMediaUri;
    protected String mFileType;
    protected GridView mGridView;
    //Handler mUpdateBarHandler;


    protected MenuItem mSendMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);

        mGridView  = (GridView) findViewById(R.id.friendsGrid);
        //mUpdateBarHandler = new Handler();
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);

        mMediaUri = getIntent().getData();
        mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isOnline()) {
            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);
            setProgressBarIndeterminateVisibility(true);
            ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
            query.addAscendingOrder(ParseConstants.KEY_USERNAME);
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> friends, ParseException e) {
                    setProgressBarIndeterminateVisibility(false);

                    if (e == null) {
                        mFriends = friends;

                        String[] usernames = new String[mFriends.size()];
                        int i = 0;
                        for (ParseUser user : mFriends) {
                            usernames[i] = user.getUsername();
                            i++;
                        }

                        if (mGridView.getAdapter() == null) {
                            UserAdapter adapter = new UserAdapter(RecipientsActivity.this, mFriends);
                            mGridView.setAdapter(adapter);
                        } else {
                            ((UserAdapter) mGridView.getAdapter()).refill(mFriends);
                        }
                    } else {
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipients, menu);

        mSendMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_send:
                ParseObject message = createMessage();
                if (message == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.error_selecting_file)
                            .setTitle(R.string.error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    if(isOnline()) {
                        new RecipientsTask(message).execute();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected ParseObject createMessage() {
        ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
        message.put(ParseConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(ParseConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(ParseConstants.KEY_FILE_TYPE, mFileType);

        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaUri);

        if (fileBytes == null) {
            return null;
        } else {
            if (mFileType.equals(ParseConstants.TYPE_IMAGE)) {
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(ParseConstants.KEY_FILE, file);
            return message;
        }
    }

    protected ArrayList<String> getRecipientIds() {
        ArrayList<String> recipientIds = new ArrayList<String>();
        for (int i = 0; i < mGridView.getCount(); i++) {
            if (mGridView.isItemChecked(i)) {
                recipientIds.add(mFriends.get(i).getObjectId());
            }
        }

        return recipientIds;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class RecipientsTask extends AsyncTask<Void, Integer, Boolean> {
        private ProgressDialog mProgressDialog;
        private final ParseObject mMessage;
        private volatile boolean running = true;
        private Boolean file_sent = false;

        RecipientsTask(ParseObject message) {
            mMessage = message;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(RecipientsActivity.this);
            mProgressDialog.setMessage(getString(R.string.sending_file));
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return file_sent;
            }

            while (running) {
                mMessage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            file_sent = true;
                        } else {
                            file_sent = false;
                        }
                        running = false;
                    }
                });
            }
            return file_sent;
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mProgressDialog.dismiss();

            if (file_sent) {
                Toast.makeText(RecipientsActivity.this,getString(R.string.success_message),
                        Toast.LENGTH_LONG).show();
                sendPushNotifications();
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                builder.setMessage(R.string.error_sending_message)
                        .setTitle(R.string.error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }

        @Override
        protected void onCancelled() {
            running = false;
        }

        protected void sendPushNotifications(){
            ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
            query.whereContainedIn(ParseConstants.KEY_USER_ID, getRecipientIds());

            ParsePush push = new ParsePush();
            push.setQuery(query);
            push.setMessage(getString(R.string.push_message,ParseUser.getCurrentUser().getUsername()));
            push.sendInBackground();
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

    protected AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (mGridView.getCheckedItemCount() > 0) {
                mSendMenuItem.setVisible(true);
            } else {
                mSendMenuItem.setVisible(false);
            }

            ImageView checkImageView = (ImageView) view.findViewById(R.id.checkImageView);

            if (mGridView.isItemChecked(position)) {
                checkImageView.setVisibility(View.VISIBLE);
            } else {
                checkImageView.setVisibility(View.INVISIBLE);
            }

        }
    };


}
