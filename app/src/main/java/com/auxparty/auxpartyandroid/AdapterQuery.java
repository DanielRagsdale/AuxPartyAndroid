package com.auxparty.auxpartyandroid;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.auxparty.auxpartyandroid.utilities.NetworkUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AdapterQuery implements ListAdapter
{
    private List<SongObject> songs = new ArrayList<SongObject>();
    private List<DataSetObserver> observers = new ArrayList<DataSetObserver>();

    public void addSong(SongObject song)
    {
        songs.add(song);
        callObservers();
    }

    public void clearSongs()
    {
        songs.clear();
        callObservers();
    }

    //TODO
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SongObject song = songs.get(position);

        final LinearLayout container = new LinearLayout(parent.getContext());
        container.setGravity(Gravity.CENTER_VERTICAL);

        if(!song.requested)
        {
            container.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }
        else
        {
            container.setBackgroundColor(Color.parseColor("#CCEECC"));
        }

        final ImageView iv = new ImageView(parent.getContext());
        iv.setImageBitmap(song.art);

        final TextView tv = new TextView(parent.getContext());
        tv.setText(song.songTitle + "--" + song.artistName);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        tv.setSingleLine();
        tv.setTextColor(Color.parseColor("#444444"));

        container.addView(iv);
        container.addView(tv);

        container.setOnClickListener(new View.OnClickListener()
        {
           @Override
           public void onClick (View v)
           {
               container.setBackgroundColor(Color.parseColor("#CCEECC"));
               Log.d("auxparty", song.songTitle + "--" + song.artistName);


               //Send request to auxparty
               if(!song.requested)
               {
                   TaskSendRequest sendRequest = new TaskSendRequest();
                   sendRequest.execute(song.sessionIdentifier, song.appleID);
               }

               song.requested = true;
           }
        });

        return container;
    }

    //TODO
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(int pos)
    {
        return pos < songs.size();
    }

    //region trivial methods
    //MVP
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        observers.add(observer);
    }

    //MVP
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        observers.remove(observer);
    }

    //MVP
    @Override
    public int getCount() {
//        return songs.size();
        return songs.size();
    }

    //MVP
    @Override
    public Object getItem(int position) {
        return songs.get(position);
    }

    //MVP
    @Override
    public long getItemId(int position) {
        return position;
    }

    //MVP
    @Override
    public boolean hasStableIds() {
        return false;
    }
    //MVP
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    //MVP
    @Override
    public boolean isEmpty() {
        return songs.size() == 0;
    }
    //endregion

    private void callObservers()
    {
        for (DataSetObserver observer : observers)
        {
            observer.onChanged();
        }
    }

    class TaskSendRequest extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... params)
        {
            Log.d("auxparty", "Request submitted");

            String data = params[1];

            try
            {
                String response = NetworkUtils.postDataToHttpURL(new URL("http://auxparty.com/api/client/request/" + params[0]), data);

                Log.d("auxparty", response);
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }
}

