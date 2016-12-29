package com.auxparty.auxpartyandroid;

import android.database.DataSetObserver;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AdapterQuery implements ListAdapter
{
    private List<SongObject> songs = new ArrayList<SongObject>();
    private List<DataSetObserver> observers = new ArrayList<DataSetObserver>();

    //TODO
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView tv = new TextView(parent.getContext());

        tv.setText("My name is Daniel Ragsdale");
        return tv;
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
        return pos <= 3;
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
        return 3;
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
}

class SongObject
{
    public String artistName;
    public String songTitle;
    public String appleID;
    public String artURL;
}
