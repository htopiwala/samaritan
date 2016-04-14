package com.htdevs.samaritan;

import android.app.Activity;
import android.nfc.Tag;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by intel on 3/15/2016.
 */
public class MessageAdapter extends BaseAdapter {

    private static final String TAG = MessageAdapter.class.getSimpleName();

    //Integer values for Incoming and Outgoing function
    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
    public static final int DIRECTION_RESPONSE = 2;
    public static final int DIRECTION_MOVIE_RESPONSE = 3;

    private List<Pair<String, Integer>> messages;
    private LayoutInflater layoutInflater;
    public Activity activity;

    //Initializing constructor
    public MessageAdapter(Activity activity){
        this.activity = activity;
        Log.d(TAG, "MessageAdapter() constructor, initializing layoutInflater");
        layoutInflater = activity.getLayoutInflater();
        messages = new ArrayList<Pair<String, Integer>>();
    }

    //Function to add new message
    public void addMessage(String message, int direction) {
        Log.d(TAG, "addMessage(), adding message "+message+" with direction "+direction);
        messages.add(new Pair(message, direction));
        notifyDataSetChanged();
    }

    //Overriding default functions of BaseAdapter
    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).second;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int direction = getItemViewType(position);
        Log.d(TAG, "getView(), inside function");
        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            if (direction == DIRECTION_INCOMING) {
                res = R.layout.message_right;
            } else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.message_left;
            } else if(direction == DIRECTION_RESPONSE){
                res = R.layout.message_response;
            } else if(direction == DIRECTION_MOVIE_RESPONSE) {
                res = R.layout.message_movie_response;
            }
            convertView = layoutInflater.inflate(res, parent, false);
        }

        String message = messages.get(position).first;

        if(direction == DIRECTION_RESPONSE){
            Log.d(TAG,"getView(), return response if #test");
            return convertView;
        }
        Date date = new Date();
        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);
        txtDate.setText(date.toString());
        if(direction == DIRECTION_MOVIE_RESPONSE){
            String data = "";
            String posterPath = "";
            try{
                JSONObject jsonObject = new JSONObject(message);
                data = jsonObject.getString("data");
                posterPath = jsonObject.getString("posterPath");
            } catch (Exception e) {
                e.printStackTrace();
            }
            ImageView imageView = (ImageView) convertView.findViewById(R.id.txtMessageImage);
            Picasso.with(activity)
                    .load(posterPath)
                    .placeholder(R.drawable.placeholder)
                    .resize(220,280)
                    .into(imageView);

            //Code for justified alignment of text
            SpannableString spannableString = new SpannableString(Html.fromHtml(data));
            int leftMargin = 230;
            spannableString.setSpan(new MyLeadingMarginSpan2(10, leftMargin), 0, spannableString.length(), 0);

            txtMessage.setText(spannableString);

            return convertView;
        }
        txtMessage.setText(message);
        return convertView;
    }
}