package com.auxparty.auxpartyandroid;

/**
 * Enum representing the different types of music playback services
 */
public enum TypeService
{
    SPOTIFY("spotify"), APPLE_MUSIC("apple_music"), NONE("");

    public String name;

    private TypeService(String name)
    {
        this.name = name;
    }

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
