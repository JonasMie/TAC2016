package tac.android.de.truckcompanion.simulator;

import android.content.Context;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static tac.android.de.truckcompanion.utils.Helper.getJsonStringFromAssets;

/**
 * Created by Jonas Miederer.
 * Date: 11.05.16
 * Time: 10:47
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class JourneySimulation {
    public static final int STATE_DRIVING = 0;
    public static final int STATE_PAUSE = 0;
    public static final int AUTO_PAUSE_TIME = 3;

    private static JourneySimulation simulation;
    private static Context context;
    private static JSONArray liveData;

    private static boolean autoPause = false;
    private List<SimulationEventListener> listeners = new ArrayList<>();
    private final Handler handler = new Handler();
    private boolean isSimulationRunning = false;

    private int currentState = STATE_DRIVING;

    private JourneySimulation() {

    }

    public static JourneySimulation Builder(Context context) throws JSONException {
        if (simulation == null) {
            JourneySimulation.context = context;
            simulation = new JourneySimulation();
            liveData = (new JSONArray(getJsonStringFromAssets(context, "live.json")));
        }
        return simulation;
    }

    public static JourneySimulation Builder(Context context, boolean autoPause) throws JSONException {
        if (simulation == null) {
            JourneySimulation.context = context;
            JourneySimulation.autoPause = autoPause;
            simulation = new JourneySimulation();
            liveData = (new JSONArray(getJsonStringFromAssets(context, "live.json")));
        }
        return simulation;
    }


    /**
     * Add SimulationEventListener.
     *
     * @param listener the listener
     */
    public void addOnSimulationEventListener(SimulationEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Start simulation.
     *
     * The live.json-File (assets-folder) is being parsed and returns the next json-object in an interval of 1 second.
     * Notice, that the simulator also returns the previous live-object, so the structure of the returned object looks
     * like this:
     *      {
     *          "new": {
     *              "lat":              double  (latitude of current position)
     *              "lng":              double  (longitude of current position)
     *              "speed":            int     (current speed)
     *              "embarkation":      boolean (indicates the embarkation status)
     *              "stationarySince":  int     (minutes since vehicle is stationary)
     *              "heading":          int     (heading of the vehicle)
     *              "driveDirection":   int     (driving direction of vehicle)
     *              "motorOn":          boolean (indicates the motor status)
     *          },
     *          "prev": {
     *              // same as above
     *          }
     *      }
     */
    public void startSimulation() {
        // just one simulation can be run at the same time
        if (!isSimulationRunning) {
            isSimulationRunning = true;
            // emit events in a 1 sec interval
            handler.postDelayed(new Runnable() {
                int count = 0;
                int current_pause_time=0;
                JSONObject liveObj;
                JSONObject prevObj;

                @Override
                public void run() {
                    // emit event
                    try {
                        prevObj = liveObj;
                        liveObj = (JSONObject) liveData.get(count);
                        // If user selected auto pause, then the simulation will simulate a pause at the following coordinate
                        if (autoPause && liveObj.getDouble("lat") == 51.6624 && liveObj.getDouble("lng") == 12.203862) {
                            setCurrentState(STATE_PAUSE);
                            current_pause_time++;
                        }
                        // after AUTO_PAUSE_TIME loops, the driver finishes his pause and continues his journey
                        if(current_pause_time == AUTO_PAUSE_TIME){
                            setCurrentState(STATE_DRIVING);
                            current_pause_time=0;
                        }
                        for (SimulationEventListener listener : listeners) {
                            JSONObject event = new JSONObject();
                            event.put("new", liveObj);
                            event.put("prev", prevObj);
                            listener.onSimulationEvent(event);
                        }
                        if (currentState == STATE_DRIVING) {
                            count += 1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    handler.postDelayed(this, 1000);
                }
            }, 1000);

        }
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public static void setAutoPause(boolean autoPause) {
        JourneySimulation.autoPause = autoPause;
    }
}
