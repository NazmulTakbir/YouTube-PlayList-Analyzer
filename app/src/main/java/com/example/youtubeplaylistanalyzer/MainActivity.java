package com.example.youtubeplaylistanalyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    static EditText urlField;
    static Button button;
    static Button clear;
    static TextView time;
    static TextView totalVideos;
    static TextView averageLength;
    static TextView speed1;
    static TextView speed2;
    static TextView speed3;
    static TextView speed4;
    static TextView processing;
    static int totalTime;
    static int totalVideosCount;
    static MainActivity mainActivity;

    public void getTime(View view) {
        processing.setText("Processing ...");
        button.setEnabled(false);
        clear.setEnabled(false);
        button.setAlpha(0.5f);
        clear.setAlpha(0.5f);

        totalTime = 0;
        String url = urlField.getText().toString();

        if( url.equals("") || url == null ) {

            try {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Toast.makeText(MainActivity.this, "Please Enter Valid URL", Toast.LENGTH_LONG).show();

            processing.setText("");
            button.setEnabled(true);
            clear.setEnabled(true);
            button.setAlpha(1f);
            clear.setAlpha(1f);
            return;
        }

        Pattern pattern = Pattern.compile("[&?]list=([^&]+)");
        Matcher m = pattern.matcher(url);

        String playListID = null;
        if( m.find() ) {
            playListID = url.substring(m.start(), m.end()).substring(6);
        }
        else {
            playListID = urlField.getText().toString();
        }

        getDuration playListDuration = new getDuration();
        playListDuration.duration(playListID);

        try {
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlField = (EditText) findViewById(R.id.URL);
        button = (Button) findViewById(R.id.button);
        clear = (Button) findViewById(R.id.clear);
        time = (TextView) findViewById(R.id.time);
        totalVideos = (TextView) findViewById(R.id.totalVideos);
        averageLength = (TextView) findViewById(R.id.averageLength);
        speed1 = (TextView) findViewById(R.id.speed1);
        speed2 = (TextView) findViewById(R.id.speed2);
        speed3 = (TextView) findViewById(R.id.speed3);
        speed4 = (TextView) findViewById(R.id.speed4);
        processing = (TextView) findViewById(R.id.processing);
        mainActivity = this;

        urlField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    button.performClick();
                }
                return false;
            }
        });
    }

    public void clearEntry(View view) {
        urlField.setText("");
        try {
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}