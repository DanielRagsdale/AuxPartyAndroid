package com.auxparty.auxpartyandroid.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.auxparty.auxpartyandroid.R;
import com.auxparty.auxpartyandroid.services.ServicePlayMusic;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Spotify;

/**
 * Activity displayed for a user when they are hosting a session.
 */
public class ActivityHost extends ActivityPlayer
{
    ServicePlayMusic mServiceMusic;
    boolean mIsBound;

    /**
     * The private key needed to perform some actions on the server
     */
    String key;

    public static final String CLIENT_ID = "ad5cd6a8c83843e19eb8cbf67663a3a3";
    public static final String REDIRECT_URI = "auxparty-spotify://callback";

    private static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        Intent startingIntent = getIntent();
        key = startingIntent.getStringExtra("key");

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

                Intent startingIntent = new Intent(ActivityHost.this, ServicePlayMusic.class);
                startingIntent.putExtra(getString(R.string.key_identifier), identifier);
                startingIntent.putExtra(getString(R.string.key_key), key);
                startingIntent.putExtra(getString(R.string.key_spotify_token), token);

                bindService(startingIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);

            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
        doUnbindService();
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(musicServiceConnection);
            mIsBound = false;
        }
    }

    private ServiceConnection musicServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            ServicePlayMusic.MusicBinder binder = (ServicePlayMusic.MusicBinder) service;
            mServiceMusic = binder.getService();
            mIsBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mIsBound = false;
        }
    };
}
