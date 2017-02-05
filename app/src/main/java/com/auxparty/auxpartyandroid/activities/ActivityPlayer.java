package com.auxparty.auxpartyandroid.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.auxparty.auxpartyandroid.AdapterQuery;
import com.auxparty.auxpartyandroid.R;
import com.auxparty.auxpartyandroid.SquareImageView;
import com.auxparty.auxpartyandroid.TypeService;
import com.auxparty.auxpartyandroid.utilities.NetworkUtils;
import com.auxparty.auxpartyandroid.utilities.TaskSearchQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import static com.auxparty.auxpartyandroid.R.id.toolbar;

/**
 * Super class for ActivityHost and ActivityClient
 */
public class ActivityPlayer extends AppCompatActivity
{
    /**
     * The session identifier
     */
    String identifier;
    /**
     * The session name
     */
    String name;
    /**
     * The music service the session is using
     */
    TypeService service;

    RelativeLayout mActivityMain;
    SquareImageView mAlbumArt;
    View mSearchTouchClose;

    LinearLayout mSearchLayout;

    EditText mSearchBar;
    ListView mSearchPromptList;

    /**
     * The adapter used to provide views to the search box
     */
    AdapterQuery searchAdapter;

    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Intent startingIntent = getIntent();

        identifier = startingIntent.getStringExtra(getString(R.string.key_identifier));
        name = startingIntent.getStringExtra(getString(R.string.key_session_name));

        service = TypeService.parseServiceString(startingIntent.getStringExtra("service_name"));

        Log.d("auxparty", "Player identifier" + identifier);
        Log.d("auxparty", "Player name" + name);
    }

    protected void initializeViews()
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("(" + identifier.toUpperCase() +") " + name);


        mActivityMain = (RelativeLayout) findViewById(R.id.activity_player);
        mAlbumArt = (SquareImageView) findViewById(R.id.comp_album_art);
        mSearchTouchClose = findViewById(R.id.v_search_touch_close);

        mSearchLayout = (LinearLayout) findViewById(R.id.comp_song_search);

        Log.d("auxparty", mSearchLayout.toString());

        mSearchBar = (EditText) mSearchLayout.findViewById(R.id.et_search_bar);
        mSearchPromptList = (ListView) mSearchLayout.findViewById(R.id.lv_search_prompt);

        searchAdapter = new AdapterQuery(service);
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

        pollAlbumArt();
    }

    //TODO add more sensible system for updating the art if your are the host
    Timer updateArt;
    /**
     * Poll the auxparty track listing until there is a valid next song.
     * Ends when nowPlaying is not null
     */
    public void pollAlbumArt()
    {
        updateArt = new Timer();
        updateArt.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                TaskGetArt getArt = new TaskGetArt();
                getArt.execute(identifier);
            }
            //TODO poll art less frequently in production
        }, 0, 1500);
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
        protected Bitmap doInBackground(CharSequence... params)
        {
            Bitmap art = null;

            try
            {
                String nowPlaying = NetworkUtils.getResponseFromHttpUrl(new URL(getString(R.string.url_ap_nowplaying) + params[0].toString()));

                JSONObject playingInfo = new JSONObject(nowPlaying);
                String playingID = playingInfo.getString(getString(R.string.key_track_id));

                String artLoc = "";

                switch(service)
                {
                    case APPLE_MUSIC:
                    {
                        String lookup = NetworkUtils.getResponseFromHttpUrl(new URL("https://itunes.apple.com/lookup?id=" + playingID));

                        JSONObject songInfo = new JSONObject(lookup);
                        JSONObject results = songInfo.getJSONArray(getString(R.string.jkey_pl_results)).getJSONObject(0);

                        String smallArtLoc = results.getString(getString(R.string.jkey_pl_art_loc));

                        artLoc = smallArtLoc.replace("100", "512");
                        break;
                    }

                    case SPOTIFY:
                    {
                        String lookup = NetworkUtils.getResponseFromHttpUrl(new URL(getString(R.string.url_sp_lookup) + playingID));
                        JSONObject songInfo = new JSONObject(lookup);
                        JSONArray images = songInfo.getJSONObject("album").getJSONArray("images");

                        artLoc = images.getJSONObject(0).getString("url");

                        break;
                    }
                }

                art = NetworkUtils.getBitmapFromHttpURL(artLoc);
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
