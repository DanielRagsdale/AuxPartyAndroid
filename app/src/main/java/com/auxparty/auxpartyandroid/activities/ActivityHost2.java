package com.auxparty.auxpartyandroid.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import com.auxparty.auxpartyandroid.R;
import com.auxparty.auxpartyandroid.services.ServicePlayMusic;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Spotify;

/**
 * Activity displayed for a user when they are hosting a session.
 */
public class ActivityHost2 extends ActivityPlayer implements Player.NotificationCallback, ConnectionStateCallback, Player.OperationCallback
{
    ServicePlayMusic mServiceMusic;
    boolean mIsBound;

    public static final String CLIENT_ID = "ad5cd6a8c83843e19eb8cbf67663a3a3";
    public static final String REDIRECT_URI = "auxparty-spotify://callback";

    private static final int REQUEST_CODE = 4321;

    Player mPlayer;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE)
        {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN)
            {
                String token = response.getAccessToken();

                Config playerConfig = new Config(getApplicationContext(), token, ActivityHost.CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver()
                {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer)
                    {
                        Log.d("auxparty-spotify", "Player initialized");
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(ActivityHost2.this);
                        mPlayer.addNotificationCallback(ActivityHost2.this);
                    }

                    @Override
                    public void onError(Throwable throwable)
                    {
                        Log.e("auxparty-spotify", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent)
    {
        Log.d("auxparty-spotify", "Playback event received: " + playerEvent.name());

        switch (playerEvent)
        {
            // Handle event type as necessary
            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(com.spotify.sdk.android.player.Error error)
    {
        Log.d("auxparty-spotify", "Playback error!");
        Log.d("auxparty-spotify", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn()
    {
        Log.d("auxparty-spotify", "User logged in");
        mPlayer.playUri(this, "spotify:track:008GDaR710wy4nlvXh7Lwm", 0, 0);
//        mPlayer.playUri(null, "spotify:track:077OhUjy58qkjscrXjW696", 0, 0);
    }

    @Override
    public void onLoggedOut() {
        Log.d("auxparty-spotify", "User logged out");
    }

    @Override
    public void onLoginFailed(com.spotify.sdk.android.player.Error error)
    {
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
        Log.d("auxparty-spotify", "Error occured:   " + error.toString());
    }

    @Override
    public void onSuccess()
    {
        Log.d("auxparty-spotify", "playback success");
    }
    //endregion

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}
