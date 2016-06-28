package tac.android.de.truckcompanion.geo;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.DataCollector;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.utils.Helper;
import tac.android.de.truckcompanion.utils.ResponseCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jonas Miederer.
 * Date: 11.05.16
 * Time: 16:17
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Route {

    public static final int DISTANCE_INTERVAL = 10;
    private JSONObject googleRoute;
    private ArrayList<LatLng> waypoints;
    private int duration;
    private int distance;

    private ArrayList<ArrayList> legs = new ArrayList<>();

    public Route() {

    }

    public void init(Context context){

    }

    public void requestRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, DataCollector dataCollector, ResponseCallback callback) {
        dataCollector.getRoute(startPoint, destinationPoints, callback);
    }

    public void setup(JSONObject result) throws JSONException {
        googleRoute = result.getJSONArray("routes").getJSONObject(0);
        waypoints = getWaypoints(new JSONArray(Helper.getJsonStringFromAssets(MainActivity.context, "live.json")));
        fillLegs(googleRoute);
    }

    private void fillLegs(JSONObject googleRoute) throws JSONException {
        JSONArray legs = googleRoute.getJSONArray("legs");

        for (int i = 0; i < legs.length(); i++) {
            ArrayList<HashMap<String, Integer>> stepsCont = new ArrayList<>();
            JSONObject leg = legs.getJSONObject(i);
            JSONArray steps = leg.getJSONArray("steps");

            for (int j = 0; j < steps.length(); j++) {
                HashMap<String, Integer> stepsMap = new HashMap<>();
                JSONObject step = steps.getJSONObject(j);
                int duration = step.getJSONObject("duration").getInt("value");
                int distance = step.getJSONObject("distance").getInt("value");
                stepsMap.put("duration", duration);
                stepsMap.put("distance", distance);
                this.duration += duration;
                this.distance += distance;
                stepsCont.add(stepsMap);
            }
            this.legs.add(stepsCont);
        }
    }
    private static ArrayList<LatLng> getWaypoints(JSONArray json) {
        ArrayList<LatLng> arrayList = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                JSONObject obj = json.getJSONObject(i);
                arrayList.add(new LatLng(obj.getDouble("lat"), obj.getDouble("lng")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    public JSONObject getGoogleRoute() {
        return googleRoute;
    }

    public ArrayList<LatLng> getWaypoints() {
        return waypoints;
    }

    public int getDuration() {
        return duration;
    }

    public int getDistance() {
        return distance;
    }

    public ArrayList<ArrayList> getLegs() {
        return legs;
    }
}
