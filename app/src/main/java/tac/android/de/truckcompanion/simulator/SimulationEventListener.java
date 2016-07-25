package tac.android.de.truckcompanion.simulator;

import org.json.JSONObject;

/**
 * @deprecated
 *
 * Created by Jonas Miederer.
 * Date: 11.05.16
 * Time: 15:43
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public interface SimulationEventListener {
    void onSimulationEvent(JSONObject event);
}
