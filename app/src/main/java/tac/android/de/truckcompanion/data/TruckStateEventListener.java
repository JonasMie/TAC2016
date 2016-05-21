package tac.android.de.truckcompanion.data;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 16:03
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface TruckStateEventListener {

    void onTruckStationaryStateChange(int state);
}
