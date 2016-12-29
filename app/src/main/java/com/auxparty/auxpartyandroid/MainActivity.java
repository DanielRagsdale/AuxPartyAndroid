package com.auxparty.auxpartyandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {

    EditText mSearchBar;
    ListView mSearchPromptList;
    RelativeLayout mActivityMain;

    AdapterQuery testAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBar = (EditText) findViewById(R.id.et_search_bar);
        mSearchPromptList = (ListView) findViewById(R.id.lv_search_prompt);
        mActivityMain = (RelativeLayout) findViewById(R.id.activity_main);

        testAdapter = new AdapterQuery();

        mSearchPromptList.setAdapter(testAdapter);

        mSearchBar.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                mSearchPromptList.setVisibility(View.VISIBLE);
                return false;
            }
        });

        mActivityMain.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch (View v, MotionEvent event)
            {
                mSearchPromptList.setVisibility(View.GONE);
                return true;
            }
        });

        mSearchBar.addTextChangedListener(new QueryApple());
    }
}

class QueryApple implements TextWatcher {
    @Override
    public void afterTextChanged(Editable s)
    {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after)
    {
        // Update the search string


        Log.d("auxparty", s.toString());
    }
}
