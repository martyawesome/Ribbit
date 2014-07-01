package com.martyawesome.smarty.app.ui;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.adapters.RequestsAdapter;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class PendingRequestsActivity extends ListActivity {

    protected List<ParseObject> mRequests;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    String[] usernames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_pending_requests);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorScheme(R.color.swipeRefresh1, R.color.swipeRefresh2, R.color.swipeRefresh3, R.color.swipeRefresh4);

    }

    @Override
    public void onResume() {
        super.onResume();
        setProgressBarIndeterminateVisibility(true);

        if (isOnline()) {
           retrieveRequests();

        } else {
            setProgressBarIndeterminateVisibility(false);
            Toast.makeText(PendingRequestsActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent myIntent = new Intent(PendingRequestsActivity.this, ViewProfileActivity.class);
        myIntent.putExtra(ParseConstants.KEY_USERNAME, usernames[position]);
        myIntent.putExtra("friends", false);
        startActivity(myIntent);
    }

    protected SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {

        @Override
        public void onRefresh() {
            if (isOnline()) {
                retrieveRequests();
            } else {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                Toast.makeText(PendingRequestsActivity.this, getString(R.string.internet_error), Toast.LENGTH_LONG).show();
            }
        }
    };

    public void retrieveRequests() {

        setProgressBarIndeterminateVisibility(true);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_REQUESTS);
        query.whereEqualTo(ParseConstants.KEY_REQUEST_TO, ParseUser.getCurrentUser().getUsername());
        query.addDescendingOrder(ParseConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> requests, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                if (e == null) {
                    // we found friend requests
                    mRequests = requests;

                    usernames = new String[mRequests.size()];
                    int i = 0;
                    for (ParseObject request : mRequests) {
                        usernames[i] = request.getString(ParseConstants.KEY_REQUEST_FROM);
                        i++;
                    }

                    if (getListView().getAdapter() == null) {
                        RequestsAdapter adapter = new RequestsAdapter(getListView().getContext(), mRequests);
                        setListAdapter(adapter);
                    } else {
                        ((RequestsAdapter) getListView().getAdapter()).refill(mRequests);
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
