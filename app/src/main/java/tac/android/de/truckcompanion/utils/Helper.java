package tac.android.de.truckcompanion.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Jonas Miederer.
 * Date: 08.05.16
 * Time: 16:40
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Helper {
    /**
     * Clip an int value to a defined range.
     *
     * @param value the value
     * @param lower the lower
     * @param upper the upper
     * @return the int
     */
    public static int clip(int value, int lower, int upper) {
        return Math.max(lower, Math.min(upper, value));
    }

    /**
     * Gets json string from assets.
     *
     * @param context  the context
     * @param filename the filename
     * @return the json string from assets
     */
    @Nullable
    public static String getJsonStringFromAssets(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
