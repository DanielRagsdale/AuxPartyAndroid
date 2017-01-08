package com.auxparty.auxpartyandroid.utilities;

import android.os.AsyncTask;
import android.util.Log;

import com.auxparty.auxpartyandroid.AdapterQuery;
import com.auxparty.auxpartyandroid.SongObject;
import com.auxparty.auxpartyandroid.TypeService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dan on 1/4/17.
 */

public class TaskSearchQuery extends AsyncTask<CharSequence, SongObject, Void>
{
    boolean needsClear;
    String identifier;
    AdapterQuery searchAdapter;
    TypeService service;

    public TaskSearchQuery(String identifier, AdapterQuery searchAdapter, TypeService service)
    {
        this.identifier = identifier;
        this.searchAdapter = searchAdapter;
        this.service = service;
    }

    @Override
    protected void onPreExecute()
    {
        //Clear the current song search results
        needsClear = true;
    }

    @Override
    protected Void doInBackground(CharSequence... params)
    {
        Log.d("auxparty", "Loading search results");

        URL searchURL = NetworkUtils.buildSearchUrl(params[0].toString(), service);

        //Get the list from apple
        String queryResults = null;
        try
        {
            queryResults = NetworkUtils.getResponseFromHttpUrl(searchURL);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }

        try
        {
            Log.d("auxparty", queryResults);

            JSONObject jsonResults = new JSONObject(queryResults);

            switch(service)
            {
                case APPLE_MUSIC:
                    parseApple(jsonResults);
                    break;

                case SPOTIFY:
                    parseSpotify(jsonResults);
                    break;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected void parseApple(JSONObject jsonResults) throws JSONException
    {
        JSONArray results = jsonResults.getJSONArray("results");

        for(int i = 0; i < results.length() && i < 15; i++)
        {
            if(isCancelled())
            {
                return;
            }

            JSONObject item = results.getJSONObject(i);

            if (item.getBoolean("isStreamable") && !item.getString("collectionExplicitness").equals("cleaned"))
            {
                SongObject song = new SongObject();

                song.sessionIdentifier = identifier;
                song.songTitle = item.getString("trackName");
                song.artistName = item.getString("artistName");
                song.servicePlayID = Integer.toString(item.getInt("trackId"));

                String artLocation = item.getString("artworkUrl100");

                try
                {
                    song.art = NetworkUtils.getBitmapFromHttpURL(artLocation);
                }
                catch(MalformedURLException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }

                publishProgress(song);
            }
        }
    }

    protected void parseSpotify(JSONObject jsonResults) throws JSONException
    {
        JSONArray items = jsonResults.getJSONObject("tracks").getJSONArray("items");

        for(int i = 0; i < items.length() && i < 15; i++)
        {
            if(isCancelled())
            {
                return;
            }

            JSONObject item = items.getJSONObject(i);

            if (true)
            {
                SongObject song = new SongObject();

                song.sessionIdentifier = identifier;
                song.songTitle = item.getString("name");
                song.artistName = item.getJSONArray("artists").getJSONObject(0).getString("name");
                song.servicePlayID = item.getString("id");

                String artLocation = item.getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");

                try
                {
                    song.art = NetworkUtils.getBitmapFromHttpURL(artLocation);
                }
                catch(MalformedURLException e)
                {
                    e.printStackTrace();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                }

                publishProgress(song);
            }
        }
    }

    @Override
    protected void onProgressUpdate(SongObject... song)
    {
        if(needsClear)
        {
            searchAdapter.clearSongs();
            needsClear = false;
        }

        searchAdapter.addSong(song[0]);
    }
}
