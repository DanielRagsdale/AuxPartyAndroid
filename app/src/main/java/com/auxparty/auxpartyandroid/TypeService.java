package com.auxparty.auxpartyandroid;

/**
 * Created by dan on 1/8/17.
 */

public enum TypeService
{
    SPOTIFY, APPLE_MUSIC, NONE;

    public static TypeService parseServiceString (String serviceString)
    {
       switch (serviceString)
       {
           case "spotify":
               return SPOTIFY;
           case "apple_music":
               return APPLE_MUSIC;
           default:
               return NONE;
       }
    }
}
