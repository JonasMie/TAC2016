package tac.android.de.truckcompanion.data;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 16:03
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface TruckStateEventListener {

    /**
     * On truck stationary state change (driving vs. pause)
     *
     * @param state the state
     */
    void onTruckStationaryStateChange(int state);

    /**
     * On truck moved.
     */
    void onTruckMoved();

    /**
     * On journey finished.
     */
    void onJourneyFinished();
}
