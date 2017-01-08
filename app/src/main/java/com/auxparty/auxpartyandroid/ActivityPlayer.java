package com.auxparty.auxpartyandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.auxparty.auxpartyandroid.utilities.TaskSearchQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

/**
 * Created by dan on 1/4/17.
 */

public class ActivityPlayer extends AppCompatActivity
{
    String identifier;
    String name;
    String key;
    TypeService service;

    AdapterQuery searchAdapter;

    RelativeLayout mActivityMain;
    SquareImageView mAlbumArt;
    View mSearchTouchClose;

    LinearLayout mSearchLayout;

    EditText mSearchBar;
    ListView mSearchPromptList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent startingIntent = getIntent();

        identifier = startingIntent.getStringExtra("identifier");
        name = startingIntent.getStringExtra("user_name");
        key = startingIntent.getStringExtra("key");

        service = TypeService.parseServiceString(startingIntent.getStringExtra("service"));

        getSupportActionBar().setTitle("(" + identifier.toUpperCase() +") " + name);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#333333")));
    }

    protected void initializeViews()
    {
        mActivityMain = (RelativeLayout) findViewById(R.id.activity_player);
        mAlbumArt = (SquareImageView) findViewById(R.id.comp_album_art);
        mSearchTouchClose = findViewById(R.id.v_search_touch_close);

        mSearchLayout = (LinearLayout) findViewById(R.id.comp_song_search);

        Log.d("auxparty", mSearchLayout.toString());

        //mLayoutSearch = (LinearLayout) mSearchLayout.findViewById(R.id.ll_search);
        mSearchBar = (EditText) mSearchLayout.findViewById(R.id.et_search_bar);
        mSearchPromptList = (ListView) mSearchLayout.findViewById(R.id.lv_search_prompt);

        searchAdapter = new AdapterQuery();
        mSearchPromptList.setAdapter(searchAdapter);

        mSearchLayout.bringToFront();

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

        mSearchBar.addTextChangedListener(new QueryService(service));

        TaskGetArt getArt = new TaskGetArt();
        getArt.execute(identifier);
    }

    class QueryService implements TextWatcher
    {
        TaskSearchQuery mSearchTask;
        TypeService service;

        public QueryService(TypeService service)
        {
            this.service = service;
            mSearchTask = new TaskSearchQuery(identifier, searchAdapter, service);
        }

        @Override
        public void afterTextChanged(Editable s){}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after)
        {
            //Cancel the current task
            mSearchTask.cancel(true);
            mSearchTask = new TaskSearchQuery(identifier, searchAdapter, service);

            mSearchTask.execute(s);
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
                //TODO Set album not found art
            }
        }
    }
}
