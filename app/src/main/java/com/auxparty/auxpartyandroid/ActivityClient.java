package com.auxparty.auxpartyandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.auxparty.auxpartyandroid.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ActivityClient extends AppCompatActivity {

    EditText mSearchBar;
    ListView mSearchPromptList;
    SquareImageView mAlbumArt;
    RelativeLayout mActivityMain;
    View mSearchTouchClose;
    LinearLayout mLayoutSearch;

    AdapterQuery searchAdapter;

    String identifier;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);


        mSearchBar = (EditText) findViewById(R.id.et_search_bar);
        mSearchPromptList = (ListView) findViewById(R.id.lv_search_prompt);
        mActivityMain = (RelativeLayout) findViewById(R.id.activity_client);
        mAlbumArt = (SquareImageView) findViewById(R.id.iv_album_cover);
        mSearchTouchClose = findViewById(R.id.v_search_touch_close);
        mLayoutSearch = (LinearLayout) findViewById(R.id.ll_search);

        Intent startingIntent = getIntent();
        identifier = startingIntent.getStringExtra("identifier");
        name = startingIntent.getStringExtra("name");

        getSupportActionBar().setTitle(name + " (" + identifier +")");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#333333")));

        searchAdapter = new AdapterQuery();
        mSearchPromptList.setAdapter(searchAdapter);

        mLayoutSearch.bringToFront();

        mSearchBar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                mSearchPromptList.setVisibility(View.VISIBLE);
                return false;
            }
        });

        mSearchTouchClose.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                mSearchPromptList.setVisibility(View.GONE);
                return true;
            }
        });

        mSearchBar.addTextChangedListener(new QueryApple());

        TaskGetArt getArt = new TaskGetArt();
        getArt.execute(identifier);
    }

    class QueryApple implements TextWatcher {
        TaskSearchQuery mSearchTask = new TaskSearchQuery();

        @Override
        public void afterTextChanged(Editable s){}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after)
        {
            //Cancel the current task
            mSearchTask.cancel(true);

            mSearchTask = new TaskSearchQuery();
            mSearchTask.execute(s);
        }
    }

    class TaskSearchQuery extends AsyncTask<CharSequence, SongObject, Void>
    {
        boolean needsClear;

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

            URL searchURL = NetworkUtils.buildUrl(params[0].toString());

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
                JSONObject jsonResults = new JSONObject(queryResults);
                JSONArray results = jsonResults.getJSONArray("results");

                for(int i = 0; i < results.length() && i < 15; i++)
                {
                    if(isCancelled())
                    {
                        return null;
                    }

                    JSONObject item = results.getJSONObject(i);

                    if (item.getBoolean("isStreamable") && !item.getString("collectionExplicitness").equals("cleaned"))
                    {
                        SongObject song = new SongObject();

                        song.sessionIdentifier = identifier;
                        song.songTitle = item.getString("trackName");
                        song.artistName = item.getString("artistName");
                        song.appleID = Integer.toString(item.getInt("trackId"));

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
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            return null;
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

    class TaskGetArt extends AsyncTask<CharSequence, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(CharSequence... params) {
            Bitmap art = null;

            try
            {
                String nowPlaying = NetworkUtils.getResponseFromHttpUrl(new URL("http://auxparty.com/api/neutral/nowplaying/" + params[0].toString()));

                JSONObject playingInfo = new JSONObject(nowPlaying);
                String playingID = playingInfo.getString("apple_id");


                String lookup = NetworkUtils.getResponseFromHttpUrl(new URL("https://itunes.apple.com/lookup?id=" + playingID));

                JSONObject songInfo = new JSONObject(lookup);
                JSONObject results = songInfo.getJSONArray("results").getJSONObject(0);

                String artLoc = results.getString("artworkUrl100");

                String largeArtLoc = artLoc.replace("100","512");

                art = NetworkUtils.getBitmapFromHttpURL(largeArtLoc);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            return art;
        }

        @Override
        protected void onPostExecute(Bitmap art)
        {
            if(art != null)
            {
                mAlbumArt.setImageBitmap(art);
            }
            else
            {
                //Set album not found art
            }
        }
    }
}




