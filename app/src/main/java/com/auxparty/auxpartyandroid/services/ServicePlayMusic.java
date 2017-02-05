package com.auxparty.auxpartyandroid.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.auxparty.auxpartyandroid.R;
import com.auxparty.auxpartyandroid.SongObject;
import com.auxparty.auxpartyandroid.activities.ActivityHost;
import com.auxparty.auxpartyandroid.utilities.NetworkUtils;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ReflectPermission;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Service that runs in the background and handles playing of music
 */
public class ServicePlayMusic extends Service implements Player.NotificationCallback, ConnectionStateCallback, Player.OperationCallback
{
    private final IBinder musicBinder = new MusicBinder();

    SongObject nowPlaying;
    int songNumber = -1;
    boolean paused = true;

    Player mPlayer;

    String identifier;
    String key;

    private Timer updatePlaying;
    final Handler handler = new Handler();

    private BroadcastReceiver mNetworkStateReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        identifier = intent.getStringExtra(getString(R.string.key_identifier));

        key = intent.getStringExtra(getString(R.string.key_key));

        String token = intent.getStringExtra(getString(R.string.key_spotify_token));

        Config playerConfig = new Config(getApplicationContext(), token, ActivityHost.CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver()
        {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer)
            {
                Log.d("auxparty-spotify", "Player initialized");
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(ServicePlayMusic.this);
                mPlayer.addNotificationCallback(ServicePlayMusic.this);
            }

            @Override
            public void onError(Throwable throwable)
            {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });

        pollTracks();

        return musicBinder;
    }

    public class MusicBinder extends Binder
    {
        public ServicePlayMusic getService ()
        {
            return ServicePlayMusic.this;
        }
    }

    /**
     * Poll the auxparty track listing until there is a valid next song.
     * Ends when nowPlaying is not null
     */
    public void pollTracks()
    {
        Log.d("auxparty", "poll tracks init");
        if(nowPlaying == null)
        {
            updatePlaying = new Timer();
            updatePlaying.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    Log.d("auxparty", "searching for track");
                    if(nowPlaying == null)
                    {
                        new TaskPlaySong().execute();
                    }
                    else
                    {
                        Log.d("auxparty", "playing: " + nowPlaying.songTitle);
                        updatePlaying.cancel();
                    }
                }
            }, 0, 1000);
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("ServicePlayMusic", "Playback event received: " + playerEvent.name());

        switch (playerEvent) {
            /**
             * Step 0: Increment the current song number
             * Step 1: Send a POST request to auxparty.com to update the current song
             * Step 2: Update the displayed album artwork
             * Step 3: Queue the next song 15 seconds before the current track ends
             */
            case kSpPlaybackNotifyTrackChanged:
                Metadata.Track currentTrack = mPlayer.getMetadata().currentTrack;

                songNumber++;

                if(!paused)
                {
                    SongObject song = new SongObject();
                    song.servicePlayID = currentTrack.uri.replaceAll("spotify:track:", "");
                    song.duration = currentTrack.durationMs;
                    song.songTitle = currentTrack.name;

                    nowPlaying = song;

                    //TODO eliminate race condition
                    TaskSetPlaying setPlaying = new TaskSetPlaying();
                    setPlaying.execute(identifier, nowPlaying.servicePlayID);
                }

                break;

            case kSpPlaybackNotifyPause:
                songNumber--;
                paused = true;
                if(nowPlaying != null)
                {
                    nowPlaying = null;
                    pollTracks();
                }
                break;

            case kSpPlaybackNotifyMetadataChanged:
                PlaybackState state = mPlayer.getPlaybackState();
//                Log.d("auxparty", "State is:   " + state.toString());
                break;

            // Handle event type as necessary
            default:
                break;
        }
    }

    /**
     * POST the currently playing song to auxparty.com
     */
    class TaskSetPlaying extends AsyncTask<String, Void, Void>
    {
        @Override
        public Void doInBackground(String... params)
        {
            try
            {
                JSONObject jsonData = new JSONObject();
                jsonData.put("key", key);
                jsonData.put("play_id", params[1]);
                jsonData.put("song_number", 1);

                NetworkUtils.postDataToHttpURL(new URL("http://auxparty.com/api/neutral/nowplaying/" + params[0]), jsonData);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
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

        @Override
        protected void onPostExecute(Void v)
        {
            long pos = mPlayer.getPlaybackState().positionMs;

            long delay = Math.max(0, nowPlaying.duration - pos - 15000);

            Log.d("auxparty", "Track length is " + nowPlaying.duration);

            //Find next song
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    TaskQueueSong loadNext = new TaskQueueSong();
                    loadNext.execute();
                }
            }, delay);

        }
    }

    /**
     * Get the music playlist from auxparty.com
     */
    abstract class TaskLoadSong extends AsyncTask<Void, Void, SongObject>
    {
        @Override
        protected SongObject doInBackground(Void... params)
        {
            Log.d("auxparty", "waiting for song");

            SongObject toPlay = null;

            try
            {
                //TODO make this not shitty
                NetworkUtils.getResponseFromHttpUrl(new URL("http://auxparty.com/api/host/data/" + identifier + "/?count=" + (songNumber + 3)));
                String request = NetworkUtils.getResponseFromHttpUrl(new URL("http://auxparty.com/api/host/data/" + identifier + "/?count=" + (songNumber + 3)));

                JSONObject jsonRequest = new JSONObject(request);

//                Log.d("auxparty", jsonRequest.toString());

                JSONArray tracks = jsonRequest.getJSONArray("tracks");

                if(tracks.length() > songNumber + 1)
                {
                    toPlay = new SongObject();

                    JSONObject jsonSong = tracks.getJSONObject(songNumber + 1);

                    toPlay.servicePlayID = jsonSong.getJSONObject("song").getString("play_id");
                }
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
            return toPlay;
        }

        @Override
        protected void onPostExecute(SongObject toPlay)
        {
            if(toPlay != null && toPlay.servicePlayID != null)
            {
                //TODO ensure that mPlayer is not null
                if(mPlayer != null)
                {
                    playAction(toPlay);

                    Log.d("auxparty", "Song played / Queued");
                }
                else
                {
                    Log.d("auxparty", "mPlayer is null");
                }

            }
        }

        protected abstract void playAction(SongObject toPlay);
    }

    /**
     * Play the next song in the playlist
     */
    class TaskPlaySong extends TaskLoadSong
    {
        @Override
        protected void playAction(SongObject toPlay)
        {
            playSong(toPlay);
        }
    }

    /**
     * Add the next song in the playlist to the queue
     */
    class TaskQueueSong extends TaskLoadSong
    {
        @Override
        protected void playAction(SongObject toPlay)
        {
            Log.d("auxparty", "song queued");

            queueSong(toPlay);
        }
    }

    /**
     * Play the given song with spotify
     *
     * @param song
     */
    private void playSong(SongObject song)
    {
        Log.d("auxparty", song.servicePlayID + " played");

        Metadata m = mPlayer.getMetadata();

//        Log.d("auxparty", " " + m.toString());

        paused = false;

        mPlayer.playUri(ServicePlayMusic.this, "spotify:track:" + song.servicePlayID, 0, 0);
//        mPlayer.queue(ServicePlayMusic.this, "spotify:track:6KywfgRqvgvfJc3JRwaZdZ");

//        Log.d("auxparty-spotify", "playback data: " + mPlayer.getPlaybackState().toString());
//        Log.d("auxparty-spotify", "metadata: " + mPlayer.getMetadata().toString());
    }

    /**
     * Queue the given song with spotify
     *
     * @param song
     */
    private void queueSong(SongObject song)
    {
        Log.d("auxparty", song.servicePlayID + " queued");

        Metadata m = mPlayer.getMetadata();

        Log.d("auxparty", " " + m.toString());


        mPlayer.queue(this, "spotify:track:" + song.servicePlayID);
        nowPlaying = song;
    }

    //region Spotify functions
    @Override
    public void onPlaybackError(com.spotify.sdk.android.player.Error error) {
        Log.d("auxparty-service", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("auxparty-spotify", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("auxparty-spotify", "User logged out");
    }

    @Override
    public void onLoginFailed(com.spotify.sdk.android.player.Error error) {
        Log.d("auxparty-spotify", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("auxparty-spotify", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("auxparty-spotify", "Received connection message: " + message);
    }

    @Override
    public void onError(Error error)
    {
        Log.d("auxparty", error.toString());
    }

    @Override
    public void onSuccess()
    {
        Log.d("auxparty", "playback success");
    }
    //endregion
}
