package tac.android.de.truckcompanion.geo;

import android.app.ProgressDialog;
import android.util.Log;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.DataCollector;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.utils.Helper;
import tac.android.de.truckcompanion.utils.ResponseCallback;

import java.util.ArrayList;

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

    public Route() {

    }

    public void requestRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, DataCollector dataCollector, ResponseCallback callback){
        dataCollector.getRoute(startPoint, destinationPoints, callback);
    }
    public void setup(JSONObject result) throws JSONException {
        googleRoute = result.getJSONArray("routes").getJSONObject(0);
        waypoints = getWaypoints(new JSONArray(Helper.getJsonStringFromAssets(MainActivity.context, "live.json")));
        duration = getRouteDuration(googleRoute);
        distance = getRouteDistance(googleRoute);
    }
    private int getRouteDuration(JSONObject googleRoute) throws JSONException {
        int duration = 0;
        JSONArray legs = googleRoute.getJSONArray("legs");
        for (int i = 0; i < legs.length(); i++) {
            JSONObject leg = legs.getJSONObject(i);
            duration += leg.getJSONObject("duration").getInt("value");
        }
        return duration;
    }

    private int getRouteDistance(JSONObject googleRoute) throws JSONException {
        int distance = 0;
        JSONArray legs = googleRoute.getJSONArray("legs");
        for (int i = 0; i < legs.length(); i++) {
            JSONObject leg = legs.getJSONObject(i);
            distance += leg.getJSONObject("distance").getInt("value");
        }
        return distance;
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
}
