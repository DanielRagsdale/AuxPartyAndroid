package com.auxparty.auxpartyandroid;

import android.database.DataSetObserver;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.lang.reflect.Type;
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
        SongObject song = songs.get(position);

        LinearLayout container = new LinearLayout(parent.getContext());
        container.setGravity(Gravity.CENTER_VERTICAL);

        ImageView iv = new ImageView(parent.getContext());
        iv.setImageBitmap(song.art);

        TextView tv = new TextView(parent.getContext());
        tv.setText(song.songTitle + "--" + song.artistName);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        tv.setMaxLines(1);

        container.addView(iv);
        container.addView(tv);
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
//        return songs.size() == 0;
        return false;
    }
    //endregion

    private void callObservers()
    {
        for (DataSetObserver observer : observers)
        {
            observer.onChanged();
        }
    }
}

