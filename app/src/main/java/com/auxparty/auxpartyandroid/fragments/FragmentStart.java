package com.auxparty.auxpartyandroid.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.auxparty.auxpartyandroid.R;
import com.auxparty.auxpartyandroid.TypeService;
import com.auxparty.auxpartyandroid.activities.ActivityHost;
import com.auxparty.auxpartyandroid.utilities.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dan on 1/11/17.
 */

public class FragmentStart extends Fragment
{
    View rootView;

    EditText mStartName;
    Button mStartGo;

    public FragmentStart()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_start, container, false);

        mStartName = (EditText) rootView.findViewById(R.id.et_start_name);
        mStartGo = (Button) rootView.findViewById(R.id.b_start_go);

        mStartGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TaskCreateSession task = new TaskCreateSession();
                String name = mStartName.getText().toString();

                Log.d("auxparty", name);

                task.execute(name);
            }
        });


        return rootView;
    }

    /**
     * Send a post request to auxparty.com to create a session.
     * when a response is received it will use that information to
     * create the host activity
     */
    class TaskCreateSession extends AsyncTask<String, Void, ResultCreate>
    {
        @Override
        protected ResultCreate doInBackground(String... params)
        {
            ResultCreate result = new ResultCreate();
            result.user_name = params[0];
            result.service = TypeService.SPOTIFY.name;

            try
            {

                JSONObject jsonData = new JSONObject();

                jsonData.put("user_name", params[0]);
                jsonData.put("service", "spotify");

                String response = NetworkUtils.postDataToHttpURL(new URL("http://auxparty.com/api/host/create"), jsonData);

                JSONObject jsonResult = new JSONObject(response);

                result.identifier = jsonResult.getString("identifier");
                result.key = jsonResult.getString("key");

                Log.d("auxparty", jsonResult.toString());

                return result;
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch(IOException e )
            {
                e.printStackTrace();
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Start host activity with the returned data
         */
        @Override
        protected void onPostExecute(ResultCreate result)
        {
            if(result == null)
            {
                Toast failure = Toast.makeText(getContext(), "Could not connect to the auxparty servers", Toast.LENGTH_LONG);
                failure.show();
                return;
            }

            Intent startHostActivity = new Intent(getContext(), ActivityHost.class);
            startHostActivity.putExtra("identifier", result.identifier);
            startHostActivity.putExtra("user_name", result.user_name);
            startHostActivity.putExtra("key", result.key);
            startHostActivity.putExtra("service", result.service);

            getActivity().startActivity(startHostActivity);
        }
    }

    /**
     * Holds the information needed to create the host activity
     */
    class ResultCreate
    {
        String identifier;
        String user_name;
        String key;
        String service;
    }
}
