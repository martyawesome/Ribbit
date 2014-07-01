package com.martyawesome.smarty.app.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.martyawesome.smarty.app.R;
import com.martyawesome.smarty.app.utils.ParseConstants;
import com.parse.ParseObject;

import java.util.Date;
import java.util.List;

/**
 * Created by User on 6/23/2014.
 */
public class RequestsAdapter extends ArrayAdapter<ParseObject> {

    protected Context mContext;
    protected List<ParseObject> mRequests;

    public RequestsAdapter(Context context, List<ParseObject> messages) {
        super(context, R.layout.request_item, messages);
        mContext = context;
        mRequests = messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.request_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.nameLabel = (TextView) convertView.findViewById(R.id.senderLabel);
            holder.timeLabel = (TextView) convertView.findViewById(R.id.timeLabel);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ParseObject request = mRequests.get(position);
        Date createdAt = request.getCreatedAt();
        long now = new Date().getTime();
        String convertedDate =
                DateUtils.getRelativeTimeSpanString(createdAt.getTime(),now, DateUtils.SECOND_IN_MILLIS).toString();
        holder.timeLabel.setText(convertedDate);

        holder.nameLabel.setText(request.getString(ParseConstants.KEY_REQUEST_FROM));

        return convertView;
    }

    private static class ViewHolder {
        TextView nameLabel;
        TextView timeLabel;
    }

    public void refill(List<ParseObject> messages){
        mRequests.clear();
        mRequests.addAll(messages);
        notifyDataSetChanged();
    }

}
