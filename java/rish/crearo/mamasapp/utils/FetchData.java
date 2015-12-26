package rish.crearo.mamasapp.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by rish on 26/12/15.
 */
public class FetchData extends AsyncTask<Void, Void, Void> {

    DownloadManager.DownloadListener downloadListener;
    ArrayList<String> music;

    public FetchData(DownloadManager.DownloadListener downloadListener, ArrayList<String> music) {
        this.downloadListener = downloadListener;
        this.music = music;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        fetchData();
        return null;
    }

    private void fetchData() {
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet httppost = new HttpGet(Constants.BASE_URL_INFO);
        // Depends on your web service
        httppost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            result = sb.toString();

            Log.d("T", result);
            JSONObject jsonObject = new JSONObject(result);

            JSONArray jsonArray = jsonObject.getJSONArray("music");
            boolean downloadedNewSong = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject song = jsonArray.getJSONObject(i);
                String name = song.getString("name");
                Log.d("FD", "Fetched " + name);
                if (!music.contains(name)) {
                    new DownloadManager(downloadListener).execute(name);
                    Log.d("A", "Downloading this song");
                    downloadedNewSong = true;
                } else {
                    Log.d("A", "Already downloaded this song");
                }
            }
            if (!downloadedNewSong) {
                downloadListener.onDownloadMessage("There are no new songs to download");
            }
        } catch (Exception e) {
            downloadListener.onDownloadFailure();
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (Exception squish) {
            }
        }
    }
}