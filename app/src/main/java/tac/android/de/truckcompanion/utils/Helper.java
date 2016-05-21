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
    public static int clip(int value, int lower, int upper) {
        return Math.max(lower, Math.min(upper, value));
    }

    @Nullable
    public static String getJsonStringFromAssets(Context context, String filename){
        String json = null;
        try{
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
