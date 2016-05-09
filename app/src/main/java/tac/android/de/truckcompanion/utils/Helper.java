package tac.android.de.truckcompanion.utils;

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
}
