package com.auxparty.auxpartyandroid;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.auxparty.auxpartyandroid.utilities.NetworkUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dan on 1/4/17.
 */

public class ActivityHost extends ActivityPlayer implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback
{
    private static final String CLIENT_ID = "ad5cd6a8c83843e19eb8cbf67663a3a3";
    private static final String REDIRECT_URI = "auxparty-spotify://callback";

    private static final int REQUEST_CODE = 1114;

    private Player mPlayer;

    SongObject nowPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

        initializeViews();
    }

    //region spotify funcs
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(ActivityHost.this);
                        mPlayer.addNotificationCallback(ActivityHost.this);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    class TaskPlayFirstSong extends AsyncTask<Void, Void, SongObject>
    {
        @Override
        protected SongObject doInBackground(Void... params)
        {
            Log.d("auxparty", "waiting for first song");

            SongObject toPlay = null;

            try
            {
                String request = NetworkUtils.getResponseFromHttpUrl(new URL("http://auxparty.com/api/host/data/" + identifier + "/?count=5"));

                JSONObject jsonRequest = new JSONObject(request);
                JSONArray tracks = jsonRequest.getJSONArray("tracks");

                if(tracks.length() > 0)
                {
                    toPlay = new SongObject();

                    JSONObject jsonSong = tracks.getJSONObject(0);

                    toPlay.servicePlayID = tracks.getJSONObject(0).getJSONObject("song").getString("play_id");

                    Log.d("auxparty", toPlay.servicePlayID + " song set up");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(JSONException e)
            {
                e.printStackTrace();

                Log.d("auxparty", "JSON Execption");
            }
            return toPlay;
        }

        @Override
        protected void onPostExecute(SongObject toPlay)
        {
            if(toPlay == null)
            {
                Log.d("auxparty", "toPlay is null");
            }
            else
            {
                Log.d("auxparty", toPlay.servicePlayID);
            }
            if(toPlay != null && toPlay.servicePlayID != null)
            {
                playSong(toPlay);
                updatePlaying.cancel();
            }
        }
    }

    private void playSong(SongObject song)
    {
        Log.d("auxparty", song.servicePlayID + " played");

        mPlayer.playUri(null, "spotify:track:" + song.servicePlayID, 0, 0);
        nowPlaying = song;
    }

    private Timer updatePlaying;

    @Override
    public void onResume()
    {
        super.onResume();

        if(nowPlaying == null)
        {
            updatePlaying = new Timer();
            updatePlaying.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            new TaskPlayFirstSong().execute();
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    @Override
    public void onPause()
    {
        updatePlaying.cancel();
        super.onPause();
    }

    //region Spotify functions

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(com.spotify.sdk.android.player.Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(com.spotify.sdk.android.player.Error error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }
    //endregion
}
