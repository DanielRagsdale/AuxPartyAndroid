package com.auxparty.auxpartyandroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.auxparty.auxpartyandroid.utilities.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ActivityMain extends AppCompatActivity
{
    EditText mIdentifierEnter;
    Button mGoButton;
    Button mGPSFind;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIdentifierEnter = (EditText) findViewById(R.id.et_identifier_enter);
        mGoButton = (Button) findViewById(R.id.b_go);
        mGPSFind = (Button) findViewById(R.id.b_gps_find);

        mGoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String identifier = mIdentifierEnter.getText().toString();
                Log.d("auxparty", identifier);

                if(identifier.length() != 5)
                {
                    Toast wrongLength = Toast.makeText(ActivityMain.this, "Party ID must be 5 letters long", Toast.LENGTH_SHORT);
                    wrongLength.show();
                }
                else
                {
                    TaskGetSessionInfo state = new TaskGetSessionInfo();
                    state.execute(identifier);
                }
            }
        });
    }

    class TaskGetSessionInfo extends AsyncTask<String, Void, SessionResult>
    {
        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected SessionResult doInBackground(String... params)
        {
            SessionResult sess = new SessionResult();
            sess.identifier = params[0];

            try
            {
                String sessionInfo = NetworkUtils.getResponseFromHttpUrl(new URL("http://auxparty.com/api/client/name/" + params[0]));
                JSONObject json = new JSONObject(sessionInfo);

                if(json.getBoolean("does_exist"))
                {
                    sess.user_name = json.getString("user_name");
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
        protected void onPostExecute(SessionResult result)
        {
            if(result.state == states.NOT_FOUND)
            {
                Toast notFound = Toast.makeText(ActivityMain.this, "Could not find party " + result.identifier, Toast.LENGTH_LONG);
                notFound.show();
                return;
            }
            else if(result.state == states.NO_CONNECTION)
            {
                Toast noConnection = Toast.makeText(ActivityMain.this, "Could not connect to the auxparty servers", Toast.LENGTH_LONG);
                noConnection.show();
                return;
            }

            Intent startClientActivity = new Intent(ActivityMain.this, ActivityClient.class);
            startClientActivity.putExtra("identifier", result.identifier);
            startClientActivity.putExtra("name", result.user_name);

            Toast success = Toast.makeText(ActivityMain.this, "Joining party " + result.identifier, Toast.LENGTH_SHORT);
            success.show();

            startActivity(startClientActivity);
        }
    }

    class SessionResult
    {
        public String identifier;
        public String user_name;
        public states state;
    }

    enum states
    {
        SUCCESS, NO_CONNECTION, NOT_FOUND
    }
}
