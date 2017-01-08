package com.auxparty.auxpartyandroid.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.auxparty.auxpartyandroid.TypeService;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by dan on 12/30/16.
 */

public class NetworkUtils {
    final static String APPLE_SEARCH_URL = "https://itunes.apple.com/search";
    final static String APPLE_PARAM_QUERY = "term";
    final static String APPLE_PARAM_TYPE = "entity";

    final static String APPLE_PARAM_TYPE_VALUE = "song";

    final static String SPOTIFY_SEARCH_URL = "https://api.spotify.com/v1/search";
    final static String SPOTIFY_PARAM_QUERY = "q";
    final static String SPOTIFY_PARAM_TYPE = "type";

    final static String SPOTIFY_PARAM_TYPE_VALUE = "track";

    public static URL buildSearchUrl(String searchQuery, TypeService service)
    {
        Uri builtUri = null;
        switch (service)
        {
            case APPLE_MUSIC:
                builtUri = Uri.parse(APPLE_SEARCH_URL).buildUpon()
                        .appendQueryParameter(APPLE_PARAM_QUERY, searchQuery)
                        .appendQueryParameter(APPLE_PARAM_TYPE, APPLE_PARAM_TYPE_VALUE)
                        .build();
                break;

            case SPOTIFY:
                builtUri = Uri.parse(SPOTIFY_SEARCH_URL).buildUpon()
                        .appendQueryParameter(SPOTIFY_PARAM_QUERY, searchQuery)
                        .appendQueryParameter(SPOTIFY_PARAM_TYPE, SPOTIFY_PARAM_TYPE_VALUE)
                        .build();
                break;
        }

        URL url = null;
        try
        {
            url = new URL(builtUri.toString());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try
        {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput)
            {
                return scanner.next();
            }
            else
            {
                return null;
            }
        }
        finally
        {
            urlConnection.disconnect();
        }
    }

    public static String postDataToHttpURL(URL url, JSONObject jsonData) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setRequestProperty("Accept", "application/json");

        try
        {
            OutputStreamWriter osw = new OutputStreamWriter(urlConnection.getOutputStream());
            osw.write(jsonData.toString());
            osw.flush();

            Log.d("auxparty", jsonData.toString());

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext())
            {
                return scanner.next();
            }
            else
            {
                return null;
            }

        }
        finally
        {
            urlConnection.disconnect();
        }
    }

    public static Bitmap getBitmapFromHttpURL(String url) throws IOException
    {
        Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
        return bitmap;
    }
}
