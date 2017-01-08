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

public class ActivityStart extends AppCompatActivity
{

    EditText mStartName;
    Button mStartGo;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);



        mStartName = (EditText) findViewById(R.id.et_start_name);
        mStartGo = (Button) findViewById(R.id.b_start_go);

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

    }

    class TaskCreateSession extends AsyncTask<String, Void, ResultCreate>
    {
        @Override
        protected ResultCreate doInBackground(String... params)
        {
            ResultCreate result = new ResultCreate();
            result.user_name = params[0];
            result.service = "spotify";

            try
            {

                JSONObject jsonData = new JSONObject();

                jsonData.put("user_name", params[0]);
                jsonData.put("service", "spotify");

                String response = NetworkUtils.postDataToHttpURL(new URL("http://auxparty.com/api/host/create"), jsonData);

                JSONObject jsonResult = new JSONObject(response);

                result.identifier = jsonResult.getString("identifier");
                result.key = jsonResult.getString("key");

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

        @Override
        protected void onPostExecute(ResultCreate result)
        {
            if(result == null)
            {
                Toast failure = Toast.makeText(ActivityStart.this, "Could not connect to the auxparty servers", Toast.LENGTH_LONG);
                failure.show();
                return;
            }

            Intent startHostActivity = new Intent(ActivityStart.this, ActivityHost.class);
            startHostActivity.putExtra("identifier", result.identifier);
            startHostActivity.putExtra("user_name", result.user_name);
            startHostActivity.putExtra("key", result.key);
            startHostActivity.putExtra("service", result.service);

            startActivity(startHostActivity);
        }
    }

    class ResultCreate
    {
        String identifier;
        String user_name;
        String key;
        String service;
    }
}
