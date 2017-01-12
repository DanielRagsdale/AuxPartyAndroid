package com.auxparty.auxpartyandroid.activities;

import android.os.Bundle;

import com.auxparty.auxpartyandroid.R;

/**
 * Activity displayed when a user joins a session as a client.
 */
public class ActivityClient extends ActivityPlayer
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        initializeViews();
    }
}
