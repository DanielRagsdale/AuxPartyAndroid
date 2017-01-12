package com.auxparty.auxpartyandroid;

import android.graphics.Bitmap;

import static android.R.attr.duration;

/**
 * Object representing a song that can be played
 */
public class SongObject
{
    public String sessionIdentifier;
    public String artistName;
    public String songTitle;
    public Bitmap art;
    public long duration;

    public String servicePlayID;

    public boolean requested;
}
