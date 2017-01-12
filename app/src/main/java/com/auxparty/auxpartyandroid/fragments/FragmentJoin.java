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
import com.auxparty.auxpartyandroid.activities.ActivityClient;
import com.auxparty.auxpartyandroid.utilities.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Fragment that displays the tools for a user to join an existing session
 */
public class FragmentJoin extends Fragment
{
    View rootView;

    EditText mIdentifierEnter;
    Button mGoButton;
    Button mGPSFind;


    public FragmentJoin()
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
        rootView = inflater.inflate(R.layout.fragment_join, container, false);

        mIdentifierEnter = (EditText) rootView.findViewById(R.id.et_identifier_enter);
        mGoButton = (Button) rootView.findViewById(R.id.b_go);
        mGPSFind = (Button) rootView.findViewById(R.id.b_gps_find);

        mGoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String identifier = mIdentifierEnter.getText().toString();
                Log.d("auxparty", identifier);

                if(identifier.length() != 5)
                {
                    Toast wrongLength = Toast.makeText(getContext(), "Party ID must be 5 letters long", Toast.LENGTH_SHORT);
                    wrongLength.show();
                }
                else
                {
                    TaskGetSessionInfo state = new TaskGetSessionInfo();
                    state.execute(identifier);
                }
            }
        });

        return rootView;
    }

    class TaskGetSessionInfo extends AsyncTask<String, Void, ResultJoin>
    {
        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected ResultJoin doInBackground(String... params)
        {
            ResultJoin sess = new ResultJoin();
            sess.identifier = params[0];

            try
            {
                String sessionInfo = NetworkUtils.getResponseFromHttpUrl(new URL(getString(R.string.url_ap_info) + params[0]));
                JSONObject json = new JSONObject(sessionInfo);

                if(json.getBoolean("does_exist"))
                {
                    sess.user_name = json.getString(getString(R.string.jkey_ap_session_name));
                    sess.service_name = json.getString(getString(R.string.jkey_ap_service_type));
                    sess.state = states.SUCCESS;
                }
                else
                {
                    sess.state = states.NOT_FOUND;
                }
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
                sess.state = states.NOT_FOUND;
            }
            catch(IOException e)
            {
                sess.state = states.NO_CONNECTION;
                e.printStackTrace();
            }
            catch(JSONException e)
            {
                sess.state = states.NOT_FOUND;
                e.printStackTrace();
            }

            return sess;
        }

        @Override
        protected void onPostExecute(ResultJoin result)
        {
            if(result.state == states.NOT_FOUND)
            {
                Toast notFound = Toast.makeText(getContext(), "Could not find party " + result.identifier, Toast.LENGTH_LONG); notFound.show();
                return;
            }
            else if(result.state == states.NO_CONNECTION)
            {
                Toast noConnection = Toast.makeText(getContext(), "Could not connect to the auxparty servers", Toast.LENGTH_LONG);
                noConnection.show();
                return;
            }

            Intent startClientActivity = new Intent(getContext(), ActivityClient.class);
            startClientActivity.putExtra(getString(R.string.key_identifier), result.identifier);
            startClientActivity.putExtra(getString(R.string.key_session_name), result.user_name);

            startClientActivity.putExtra(getString(R.string.key_service_type), result.service_name);

            Toast success = Toast.makeText(getContext(), "Joining party " + result.identifier, Toast.LENGTH_SHORT);
            success.show();

            startActivity(startClientActivity);
        }
    }

    class ResultJoin
    {
        public String identifier;
        public String user_name;
        public String service_name;
        public states state;
    }

    enum states
    {
        SUCCESS, NO_CONNECTION, NOT_FOUND
    }
}
