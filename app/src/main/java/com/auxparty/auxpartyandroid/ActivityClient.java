package com.auxparty.auxpartyandroid;

import android.os.Bundle;

public class ActivityClient extends ActivityPlayer
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        initializeViews();
    }
}
