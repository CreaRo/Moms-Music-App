package rish.crearo.mamasapp.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by rish on 26/12/15.
 */
public class BasePath {
    public static String getBasePath() {

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/MaasMusic");

        if (!dir.exists())
            dir.mkdirs();

        return dir.getAbsolutePath();
    }

    public static File[] getSavedMusic() {
        File file = new File(getBasePath());
        return file.listFiles();
    }
}
