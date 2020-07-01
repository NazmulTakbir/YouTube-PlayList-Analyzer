package com.example.youtubeplaylistanalyzer;

import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class getDuration {
    protected void duration(String playlistID) {
        DownloadPlayListInfo downloadTask = new DownloadPlayListInfo();
        String API_KEY = ( new YouTubeConfig() ).getApiKey();
        String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId="+playlistID+"&key="+API_KEY;
        downloadTask.execute(url);
    }
}

class DownloadPlayListInfo extends AsyncTask<String, Void, String> {

    String baseURL;
    static boolean changeButtonActive;
    @Override
    protected String doInBackground(String... urls) {
        changeButtonActive = false;
        baseURL = urls[0];
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            return responseStrBuilder.toString();
        } catch (UnknownHostException e) {
            MainActivity.mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.mainActivity, "No Internet Connection", Toast.LENGTH_LONG).show();
                    MainActivity.processing.setText("");
                    MainActivity.button.setEnabled(true);
                    MainActivity.clear.setEnabled(true);
                    MainActivity.button.setAlpha(1f);
                    MainActivity.clear.setAlpha(1f);
                }
            });
            changeButtonActive = true;
        } catch (Exception e) {
            MainActivity.mainActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.mainActivity, "Please Enter Valid URL", Toast.LENGTH_LONG).show();
                    MainActivity.processing.setText("");
                    MainActivity.button.setEnabled(true);
                    MainActivity.clear.setEnabled(true);
                    MainActivity.button.setAlpha(1f);
                    MainActivity.clear.setAlpha(1f);
                }
            });
            changeButtonActive = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if( result==null ) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            String items = jsonObject.getString("items");
            JSONArray itemsArray = new JSONArray(items);

            ArrayList<String> videoIDs = new ArrayList<String>();
            int itemPosition = 1000;
            for( int i=0; i<itemsArray.length(); i++ ) {
                JSONObject video = itemsArray.getJSONObject(i);
                String videoID = video.getJSONObject("snippet").getJSONObject("resourceId").getString("videoId");

                videoIDs.add(videoID);

                itemPosition = Integer.parseInt(video.getJSONObject("snippet").getString("position"));
            }

            int totalResults = Integer.parseInt(jsonObject.getJSONObject("pageInfo").getString("totalResults"));
            MainActivity.totalVideosCount = totalResults;

            GetVideoDuration videoDuration = new GetVideoDuration();
            videoDuration.duration(videoIDs);

            if( (itemPosition+1) < totalResults ) {
                String nextPage = jsonObject.getString("nextPageToken");
                String newUrl = baseURL + "&pageToken=" + nextPage;

                DownloadPlayListInfo downloadTask = new DownloadPlayListInfo();
                downloadTask.execute(newUrl);
            } else {
                changeButtonActive = true;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

class GetVideoDuration {
    protected void duration(ArrayList<String> videoIDs) {
        DownloadVideoInfo downloadTask = new DownloadVideoInfo();
        String API_KEY = ( new YouTubeConfig() ).getApiKey();
        String videoID = "";
        for( int i=0; i<videoIDs.size(); i++ ) {
            if( i==videoIDs.size()-1 ) {
                videoID += videoIDs.get(i);
            }
            else {
                videoID += videoIDs.get(i) + ",";
            }
        }
        String url = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id="+videoID+"&key="+API_KEY;
        downloadTask.execute(url);
    }
}

class DownloadVideoInfo extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... urls) {
        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {
            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            return responseStrBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if( result==null ) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(result);
            String items = jsonObject.getString("items");
            JSONArray itemsArray = new JSONArray(items);

            for( int j=0; j<itemsArray.length(); j++ ) {
                String duration = itemsArray.getJSONObject(j).getJSONObject("contentDetails").getString("duration");

                int T = -1, H = -1, M = -1, S = -1;
                for (int i = 0; i < duration.length(); i++) {
                    if (duration.charAt(i) == 'T') T = i;
                    else if (duration.charAt(i) == 'H') H = i;
                    else if (duration.charAt(i) == 'M') M = i;
                    else if (duration.charAt(i) == 'S') S = i;
                }

                String hours, minutes, seconds;
                hours = minutes = seconds = null;
                if (H != -1) {
                    hours = duration.substring(T + 1, H);
                }
                if (M != -1) {
                    if (H != -1) {
                        minutes = duration.substring(H + 1, M);
                    } else {
                        minutes = duration.substring(T + 1, M);
                    }
                }
                if (S != -1) {
                    if (M != -1) {
                        seconds = duration.substring(M + 1, S);
                    } else if (H != -1) {
                        seconds = duration.substring(H + 1, S);
                    } else {
                        seconds = duration.substring(T + 1, S);
                    }
                }

                int totalTime = 0;
                if (hours != null) totalTime += Integer.parseInt(hours) * 3600;
                if (minutes != null) totalTime += Integer.parseInt(minutes) * 60;
                if (seconds != null) totalTime += Integer.parseInt(seconds);

                MainActivity.totalTime += totalTime;

                int color = Color.rgb(14, 71, 161);

                String str = "Total length of playlist :\n\n\t\t\t" + formatTime(MainActivity.totalTime);
                SpannableStringBuilder displayTime = new SpannableStringBuilder(str);
                displayTime.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Total length of playlist :".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                displayTime.setSpan(new ForegroundColorSpan(color), "Total length of playlist :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.time.setText(displayTime);

                str = "No of videos :\n\n\t\t\t" + Integer.toString(MainActivity.totalVideosCount);
                SpannableStringBuilder totalVideos = new SpannableStringBuilder(str);
                totalVideos.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "No of videos : ".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                totalVideos.setSpan(new ForegroundColorSpan(color), "No of videos :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.totalVideos.setText(totalVideos);

                str = "Average length of video :\n\n\t\t\t" + formatTime(MainActivity.totalTime/MainActivity.totalVideosCount );
                SpannableStringBuilder averageLength = new SpannableStringBuilder(str);
                averageLength.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "Average length of video :".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                averageLength.setSpan(new ForegroundColorSpan(color), "Average length of video :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.averageLength.setText(averageLength);

                str = "At 1.25x :\n\n\t\t\t" + formatTime( Math.round(MainActivity.totalTime/1.25) );
                SpannableStringBuilder speed1 = new SpannableStringBuilder(str);
                speed1.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "At 1.25x :".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                speed1.setSpan(new ForegroundColorSpan(color), "At 1.25x :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.speed1.setText(speed1);

                str = "At 1.50x :\n\n\t\t\t" + formatTime( Math.round(MainActivity.totalTime/1.50) );
                SpannableStringBuilder speed2 = new SpannableStringBuilder(str);
                speed2.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "At 1.50x :".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                speed2.setSpan(new ForegroundColorSpan(color), "At 1.50x :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.speed2.setText(speed2);

                str = "At 1.75x :\n\n\t\t\t" + formatTime( Math.round(MainActivity.totalTime/1.75) );
                SpannableStringBuilder speed3 = new SpannableStringBuilder(str);
                speed3.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "At 1.75x :".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                speed3.setSpan(new ForegroundColorSpan(color), "At 1.75x :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.speed3.setText(speed3);

                str = "At 2.00x :\n\n\t\t\t" + formatTime( Math.round(MainActivity.totalTime/2.00) );
                SpannableStringBuilder speed4 = new SpannableStringBuilder(str);
                speed4.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, "At 2.00x :".length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                speed4.setSpan(new ForegroundColorSpan(color), "At 2.00x :\n\n\t\t\t".length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                MainActivity.speed4.setText(speed4);

                if( DownloadPlayListInfo.changeButtonActive && j==itemsArray.length()-1 ) {
                    MainActivity.mainActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.mainActivity, "DONE", Toast.LENGTH_LONG).show();
                            MainActivity.processing.setText("");
                            MainActivity.button.setEnabled(true);
                            MainActivity.clear.setEnabled(true);
                            MainActivity.button.setAlpha(1f);
                            MainActivity.clear.setAlpha(1f);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String formatTime(long timeInSeconds) {
        long h, m, s;
        h = timeInSeconds/3600;
        if( h==0 ) {
            m = timeInSeconds/60;
        }
        else {
            timeInSeconds -= h*3600;
            m = timeInSeconds/60;
        }
        if( m==0 ) {
            s = timeInSeconds;
        }
        else {
            timeInSeconds -= m*60;
            s = timeInSeconds;
        }
        String result;
        if( h!=0 ) {
            result = Long.toString(h) + " hours\n\t\t\t" + Long.toString(m) + " minutes\n\t\t\t" + Long.toString(s) + " seconds";
        }
        else if( m!=0 ) {
            result = Long.toString(m) + " minutes\n\t\t\t" + Long.toString(s) + " seconds";
        }
        else {
            result = Long.toString(s) + " seconds";
        }
        return result;
    }
}
