package rish.crearo.mamasapp.utils;

/**
 * Created by rish on 26/12/15.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Background Async Task to download file
 */
public class DownloadManager extends AsyncTask<String, String, String> {

    DownloadListener downloadListener;

    public DownloadManager(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.downloadListener.onDownloadStart();
    }

    @Override
    protected String doInBackground(String... f_url) {
        Log.d("DM", "Downloading " + f_url[0]);
        int count;
        try {
            URL url = new URL(Constants.BASE_FOLDER + "/" + f_url[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // this will be useful so that you can show a tipical 0-100% progress bar
            int lengthOfFile = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 5 * 1024);

            // Output stream
            OutputStream output = new FileOutputStream(BasePath.getBasePath() + "/" + f_url[0]);
            Log.d("DM", "Output stream saved to " + BasePath.getBasePath() + "/" + f_url[0]);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress("" + (int) ((total * 100) / lengthOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
            downloadListener.onDownloadFailure();
        }
        return null;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        downloadListener.onDownloadProgress(Integer.parseInt(progress[0]));
    }

    /**
     * After completing background task
     * Dismiss the progress dialog
     **/
    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded
        downloadListener.onDownloadSuccess();
    }

    public interface DownloadListener {
        void onDownloadStart();

        void onDownloadProgress(int x);

        void onDownloadSuccess();

        void onDownloadFailure();

        void onDownloadMessage(String message);
    }
}