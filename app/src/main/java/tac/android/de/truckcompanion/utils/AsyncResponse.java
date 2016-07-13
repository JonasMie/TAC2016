package tac.android.de.truckcompanion.utils;

/**
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 17:24
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface AsyncResponse<T> {
    void processFinish(T output);
}
