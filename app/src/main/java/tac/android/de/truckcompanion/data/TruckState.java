package tac.android.de.truckcompanion.data;

import android.content.Context;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.simulator.JourneySimulation;
import tac.android.de.truckcompanion.simulator.SimulationEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 16:12
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class TruckState implements SimulationEventListener {

    public static final int JOURNEY_STATE_DRIVING = 0;
    public static final int TRUCK_STATE_STANDING = 1;

    private static JourneySimulation simulation;
    private Context context;
    private int currentState;
    private List<TruckStateEventListener> listeners = new ArrayList<>();

    private boolean motorOnPrev=false;
    private long lastMove = -System.currentTimeMillis() / 1000L;

    /**
     * Instantiates a new Journey state.
     *
     * @param context the context
     * @throws JSONException the json exception
     */
    public TruckState(Context context) throws JSONException {
        this.context = context;
        if (simulation == null) {
            simulation = JourneySimulation.Builder(context);
            simulation.addOnSimulationEventListener(this);
            simulation.startSimulation();
        }
    }

    /**
     * Add truck event listener.
     *
     * @param listener the listener
     */
    public void addTruckStateEventListener(TruckStateEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Gets current state.
     *
     * @return the current state
     */
    public int getCurrentState() {
        return currentState;
    }


    @Override
    public void onSimulationEvent(JSONObject event) {
        /*
        Here we get the (simulated) live truck data.
        However, we don't need the raw data, but the state-changes in order to compute the remaining driving times etc.

         This works as follows:
         If the motor is shut off, the truck does not move anymore, so we can assume the driver parked anywhere (or gets stuck in a traffic jam,...)
         Furthermore, if the motor is running, but the vehicle doesn't move for at least 2 min, the state changes (traffic jam, unloading cargo,...)
         */

        try {
            Log.d("TAC", "New event: " + event.getDouble("lat") + ", " + event.getDouble("lng") + ", Speed: " + event.getInt("speed"));
            if(!event.getBoolean("motorOn")){
                // motor is not running
                if(motorOnPrev && currentState!= TRUCK_STATE_STANDING ){
                    // if motor is shut off, change state
                    changeState(TRUCK_STATE_STANDING );
                }
            } else {
                // motor is running
                if(event.getInt("speed")==0){
                    // vehicle is not moving
                    if((System.currentTimeMillis() / 1000L - lastMove) > 2*60 && currentState!= TRUCK_STATE_STANDING ){
                        changeState(TRUCK_STATE_STANDING );
                    }
                } else {
                    lastMove = System.currentTimeMillis() / 1000L;
                    // motor is running, speed is > 0 (truck is driving). if the truck wasn't moving before, change state
                    if(currentState != JOURNEY_STATE_DRIVING){
                        changeState(JOURNEY_STATE_DRIVING);
                    }
                }
            }
            motorOnPrev = event.getBoolean("motorOn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changeState(int state) {
        currentState = state;
        for(TruckStateEventListener listener: listeners){
            listener.onTruckStationaryStateChange(state);
        }
    }
}
