package com.auxparty.auxpartyandroid.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    final static String APPLE_PARAM_ENTITY = "entity";

    final static String APPLE_PARAM_ENTITY_VALUE = "song";

    public static URL buildUrl(String appleSearchQuery) {
        Uri builtUri = Uri.parse(APPLE_SEARCH_URL).buildUpon()
                .appendQueryParameter(APPLE_PARAM_QUERY, appleSearchQuery)
                .appendQueryParameter(APPLE_PARAM_ENTITY, APPLE_PARAM_ENTITY_VALUE)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
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

    public static String postDataToHttpURL(URL url, String data) throws IOException
    {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");

        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setRequestProperty("Accept", "application/json");

        JSONObject jsonData = new JSONObject();

        try
        {
            jsonData.put("apple_id",data);
            jsonData.put("hype_val","0.5");

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
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        finally
        {
            urlConnection.disconnect();
        }
        return null;
    }

    public static Bitmap getBitmapFromHttpURL(String url) throws IOException
    {
        Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
        return bitmap;
    }
}
