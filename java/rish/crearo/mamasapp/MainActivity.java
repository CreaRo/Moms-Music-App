package rish.crearo.mamasapp;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;
import rish.crearo.mamasapp.utils.BasePath;
import rish.crearo.mamasapp.utils.DownloadManager;
import rish.crearo.mamasapp.utils.FetchData;

public class MainActivity extends AppCompatActivity implements DownloadManager.DownloadListener {

    MediaPlayer mediaPlayer;

    @Bind(R.id.main_listview)
    ListView listView;

    @Bind(R.id.tray_play_pause)
    LinearLayout btnPlayPause;

    @Bind(R.id.tray_random)
    LinearLayout btnRandomPlay;

    @Bind(R.id.tray_text_play_pause)
    TextView trayTextPlayPause;

    @Bind(R.id.tray_text)
    TextView trayText;

    @Bind(R.id.trayImage)
    ImageView trayImage;

    ArrayAdapter<String> listAdapter;

    ProgressDialog progressDialog;

    MaterialDialog mMaterialDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        listAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getDataSet());
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("MA", "Playing " + i);
                playMusic(listAdapter.getItem(i));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showDeleteDialog(listAdapter.getItem(i));
                return true;
            }
        });

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying())
                        setPaused();
                    else
                        setPlaying();
                } else {
                    btnRandomPlay.performClick();
                }
                setTrayText();
            }
        });

        btnRandomPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList songs = getDataSet();
                playMusic((String) songs.get(new Random().nextInt(songs.size())));
            }
        });
    }

    private void showDeleteDialog(final String name) {
        mMaterialDialog = new MaterialDialog(this)
                .setTitle("Delete this song?")
                .setMessage("")
                .setPositiveButton("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new File(BasePath.getBasePath() + "/" + name).delete();
                        refreshListView();
                        mMaterialDialog.dismiss();
                    }
                })
                .setNegativeButton("CANCEL", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMaterialDialog.dismiss();
                    }
                });
        mMaterialDialog.show();
    }

    private void setupProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog.cancel();
            progressDialog = null;
        }
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Downloading Music");
        progressDialog.setCancelable(false);
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
    }

    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_bookmark:
                new FetchData(MainActivity.this, getDataSet()).execute();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDownloadStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupProgressDialog();
                progressDialog.show();
            }
        });
    }

    @Override
    public void onDownloadProgress(final int x) {
        if (x % 5 == 0)
            Log.d("Downloading ", x + " %");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null) {
                    progressDialog.show();
                    progressDialog.setProgress(x);
                } else {
                    setupProgressDialog();
                }
            }
        });
        if (x == 100) {
            if (progressDialog != null)
                progressDialog.dismiss();
        }
    }

    @Override
    public void onDownloadSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshListView();
                progressDialog.dismiss();
                Snackbar.make(listView, "Successfully downloaded 1 song.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDownloadFailure() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshListView();
                progressDialog.dismiss();
                Snackbar.make(listView, "Failed to downloaded 1 song.", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDownloadMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(listView, message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public ArrayList<String> getDataSet() {
        ArrayList<String> dataSet = new ArrayList<>();
        if (BasePath.getSavedMusic() != null)
            for (File f : BasePath.getSavedMusic()) {
                dataSet.add(f.getName());
            }
        Collections.sort(dataSet);
        return dataSet;
    }

    private void refreshListView() {
        listAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, getDataSet());
        listView.setAdapter(listAdapter);
    }

    private void playMusic(String nameOfSong) {
        try {
            if (mediaPlayer == null)
                mediaPlayer = new MediaPlayer();
            else
                mediaPlayer.reset();

            mediaPlayer.setDataSource(BasePath.getBasePath() + "/" + nameOfSong);
            mediaPlayer.prepare();
            mediaPlayer.start();
            setTrayText();
            trayText.setText("Playing " + nameOfSong.substring(0, nameOfSong.length() - 4));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTrayText() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                trayTextPlayPause.setText("PAUSE");
                trayImage.setImageResource(R.drawable.pause);
            } else {
                trayTextPlayPause.setText("PLAY");
                trayImage.setImageResource(R.drawable.play);
            }
        }
    }

    private void setPlaying() {
        mediaPlayer.start();
        setTrayText();
    }

    private void setPaused() {
        mediaPlayer.pause();
        setTrayText();
    }
}