package com.auxparty.auxpartyandroid;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.auxparty.auxpartyandroid.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText mSearchBar;
    ListView mSearchPromptList;
    SquareImageView mAlbumArt;
    RelativeLayout mActivityMain;
    View mSearchTouchClose;

    AdapterQuery searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBar = (EditText) findViewById(R.id.et_search_bar);
        mSearchPromptList = (ListView) findViewById(R.id.lv_search_prompt);
        mActivityMain = (RelativeLayout) findViewById(R.id.activity_main);
        mAlbumArt = (SquareImageView) findViewById(R.id.iv_album_cover);
        mSearchTouchClose = (View) findViewById(R.id.v_search_touch_close);


        mAlbumArt.setImageResource(R.mipmap.album_test);

        searchAdapter = new AdapterQuery();
        mSearchPromptList.setAdapter(searchAdapter);

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
    }

    class QueryApple implements TextWatcher {
        SearchQueryTask mSearchTask = new SearchQueryTask();

        @Override
        public void afterTextChanged(Editable s){}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after){}

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after)
        {
            //Cancel the current task
            mSearchTask.cancel(true);

            mSearchTask = new SearchQueryTask();
            mSearchTask.execute(s);
        }
    }

    class SearchQueryTask extends AsyncTask<CharSequence, SongObject, Void>
    {
        @Override
        protected void onPreExecute()
        {
            //Clear the current song search results
            searchAdapter.clearSongs();
        }

        @Override
        protected Void doInBackground(CharSequence... params)
        {
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

            try {
                JSONObject jsonResults = new JSONObject(queryResults);
                JSONArray results = jsonResults.getJSONArray("results");

                for(int i = 0; i < results.length(); i++) {
                    JSONObject item = results.getJSONObject(i);

                    if (item.getBoolean("isStreamable"))
                    {
                        SongObject song = new SongObject();

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

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(SongObject... song)
        {
            searchAdapter.addSong(song[0]);
        }
    }
}




