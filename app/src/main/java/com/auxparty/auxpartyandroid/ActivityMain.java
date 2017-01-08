package com.auxparty.auxpartyandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.spotify.sdk.android.player.SpotifyPlayer;

public class ActivityMain extends AppCompatActivity
{
    Button mHostButton;
    Button mJoinButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHostButton = (Button) findViewById(R.id.b_begin_host);
        mJoinButton = (Button) findViewById(R.id.b_begin_join);

        mHostButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent startStartView = new Intent(ActivityMain.this, ActivityStart.class);
                startActivity(startStartView);
            }
        });

        mJoinButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent startJoinView = new Intent(ActivityMain.this, ActivityJoin.class);

                startActivity(startJoinView);
            }
        });
    }
}
