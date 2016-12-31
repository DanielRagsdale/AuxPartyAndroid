package com.auxparty.auxpartyandroid.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static Bitmap getBitmapFromHttpURL(String url) throws IOException, MalformedURLException
    {
        Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
        return bitmap;
    }
}
