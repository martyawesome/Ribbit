package com.martyawesome.smarty.app.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.adapters.UserAdapter;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class EditFriendsActivity extends Activity {

    public static final String TAG = EditFriendsActivity.class.getSimpleName();

    protected List<ParseUser> mUsers;
    protected ParseRelation<ParseUser> mFriendsRelation;
    protected ParseRelation<ParseObject> mAccept;
    protected ParseRelation<ParseObject> mDelete;
    protected ParseUser mCurrentUser;
    protected GridView mGridView;
    public ImageView checkImageView;
    public int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.user_grid);
        setupActionBar();

        mGridView = (GridView) findViewById(R.id.friendsGrid);
        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);
        mGridView.setOnItemClickListener(mOnItemClickListener);

        TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
        mGridView.setEmptyView(emptyTextView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isOnline()) {
            mCurrentUser = ParseUser.getCurrentUser();
            mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_FRIENDS_RELATION);

            setProgressBarIndeterminateVisibility(true);

            String currentUser = ParseUser.getCurrentUser().getUsername();

            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.orderByAscending(ParseConstants.KEY_USERNAME);
            query.whereNotEqualTo(ParseConstants.KEY_USERNAME, currentUser);
            query.setLimit(1000);
            query.findInBackground(new FindCallback<ParseUser>() {

                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    setProgressBarIndeterminateVisibility(false);
                    if (e == null) {
                        mUsers = users;
                        String[] usernames = new String[mUsers.size()];
                        int i = 0;
                        for (ParseUser user : mUsers) {
                            usernames[i] = user.getUsername();
                            i++;
                        }

                        if (mGridView.getAdapter() == null) {
                            UserAdapter adapter = new UserAdapter(EditFriendsActivity.this, mUsers);
                            mGridView.setAdapter(adapter);
                        } else {
                            ((UserAdapter) mGridView.getAdapter()).refill(mUsers);
                        }

                        addFriendCheckmarks();

                    } else {
                        Log.e(TAG, e.getMessage());
                        AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
                        builder.setMessage(e.getMessage())
                                .setTitle(R.string.error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            });
        } else {
            Toast.makeText(EditFriendsActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
        }
    }

    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addFriendCheckmarks() {
        mFriendsRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                if (e == null) {
                    //list returned - look for a match
                    for (int i = 0; i < mUsers.size(); i++) {
                        ParseUser user = mUsers.get(i);

                        for (ParseUser friend : friends) {
                            if (friend.getObjectId().equals(user.getObjectId())) {
                                mGridView.setItemChecked(i, true);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            checkImageView = (ImageView) view.findViewById(R.id.checkImageView);
            mPosition = position;
            if (checkImageView.getVisibility() == View.INVISIBLE) {
                //add friend
                 sendFriendRequest();
            } else {
                //remove friend
                unFriend();
            }
        }
    };

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void sendFriendRequest(){
        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
        query.whereEqualTo(ParseConstants.KEY_REQUEST_FROM, mCurrentUser.getUsername());
        query.whereEqualTo(ParseConstants.KEY_REQUEST_TO, mUsers.get(mPosition).getUsername());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (e != null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditFriendsActivity.this);
                    alertDialogBuilder.setTitle(getString(R.string.add_friend_title))
                            .setMessage(getString(R.string.add_friend_body))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ParseObject object = new ParseObject(ParseConstants.CLASS_REQUESTS);
                                    object.put(ParseConstants.KEY_REQUEST_FROM, mCurrentUser.getUsername());
                                    object.put(ParseConstants.KEY_REQUEST_TO, mUsers.get(mPosition).getUsername());
                                    object.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            setProgressBarIndeterminateVisibility(false);
                                            if (e == null) {
                                                Toast.makeText(EditFriendsActivity.this, getString(R.string.add_successful), Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EditFriendsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                } else {
                    Toast.makeText(EditFriendsActivity.this, getString(R.string.add_exist), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void unFriend(){
        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
        query.whereEqualTo(ParseConstants.KEY_REQUEST_FROM, mCurrentUser.getUsername());
        query.whereEqualTo(ParseConstants.KEY_REQUEST_TO, mUsers.get(mPosition).getUsername());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (e == null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditFriendsActivity.this);
                    alertDialogBuilder.setTitle(getString(R.string.delete_friend_title))
                            .setMessage(getString(R.string.delete_friend_body))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    ParseObject object = new ParseObject(ParseConstants.CLASS_REQUESTS);
                                    object.createWithoutData(ParseConstants.CLASS_REQUESTS, mUsers.get(mPosition).getUsername()).deleteEventually();
                                    Toast.makeText(EditFriendsActivity.this, getString(R.string.delete_friend_success), Toast.LENGTH_SHORT).show();
                                    checkImageView.setVisibility(View.INVISIBLE);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    Toast.makeText(EditFriendsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
