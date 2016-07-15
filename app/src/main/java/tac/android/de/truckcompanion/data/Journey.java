package tac.android.de.truckcompanion.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.routing.RouteElements;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.GeoHelper;
import tac.android.de.truckcompanion.geo.LatLng;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.simulator.SimulationEventListener;
import tac.android.de.truckcompanion.utils.AsyncResponse;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import static tac.android.de.truckcompanion.utils.Helper.getJsonStringFromAssets;

/**
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 15:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Journey implements SimulationEventListener {

    private int id;
    private int truck_id;
    private Driver driver;
    private DispoInformation.StartPoint startPoint;
    private ArrayList<DispoInformation.DestinationPoint> destinationPoints;
    private RouteWrapper routeWrapper;
    private int travelledDistance;
    private int travelledDuration;

    public int getId() {
        return id;
    }

    public Driver getDriver() {
        return driver;
    }

    public int getTruck_id() {
        return truck_id;
    }

    public DispoInformation.StartPoint getStartPoint() {
        return startPoint;
    }

    public ArrayList<DispoInformation.DestinationPoint> getDestinationPoints() {
        return destinationPoints;
    }

    public void setDestinationPoints(ArrayList<DispoInformation.DestinationPoint> destinationPoints) {
        this.destinationPoints = destinationPoints;
    }

    public void addDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoints.add(destinationPoint);
    }

    public void removeDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoints.remove(destinationPoint);
    }

    public RouteWrapper getRouteWrapper() {
        return routeWrapper;
    }

    public Journey(JSONObject journeyObj) throws JSONException, ParseException {
        this.id = journeyObj.getInt("id");
        this.driver = new Driver(journeyObj.getInt("driver_id"));
        this.truck_id = journeyObj.getInt("truck_id");
        this.startPoint = new DispoInformation.StartPoint(journeyObj.getJSONObject("start"));
        travelledDistance = 0;
        travelledDuration = 0;

        JSONArray stopsObjs = journeyObj.getJSONArray("stops");

        this.destinationPoints = new ArrayList<>();
        for (int i = 0; i < stopsObjs.length(); i++) {
            this.destinationPoints.add(new DispoInformation.DestinationPoint(stopsObjs.getJSONObject(i)));
        }
    }

    @Override
    public void onSimulationEvent(JSONObject event) {
        /**
         * When a new event is received, the journey-related data needs to be updated
         */
        try {
            JSONObject prevEvent = event.getJSONObject("prev");
            JSONObject newEvent = event.getJSONObject("new");
            travelledDistance += GeoHelper.getLocation("prev", prevEvent.getDouble("lat"), prevEvent.getDouble("lng")).distanceTo(GeoHelper.getLocation("new", newEvent.getDouble("lat"), newEvent.getDouble("lng")));
            //travelledDuration ++;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public RouteWrapper initRoute() {
        this.routeWrapper = new RouteWrapper();
        return routeWrapper;
    }

    public static class LoadJourneyData extends AsyncTask<Object, Void, Journey> {
        private Context context;
        public AsyncResponse<Journey> callback = null;

        public LoadJourneyData(Context context, AsyncResponse<Journey> callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected Journey doInBackground(Object... params) {
            JSONArray journeys = null;
            try {
                journeys = new JSONArray(getJsonStringFromAssets(context, "dispo.json"));
                return new Journey(journeys.getJSONObject((int) (params[0]) - 1));
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Journey journey) {
            callback.processFinish(journey);
        }
    }

    /**
     * Get travelled distance int.
     *
     * @return the int
     */
    public int getTravelledDistance() {
        return travelledDistance;
    }

    /**
     * Get travelled duration int.
     *
     * @return the int
     */
    public int getTravelledDuration() {
        return travelledDuration;
    }

    public LatLng getPositionOnRouteByDistance(int distance) {
        if (distance > routeWrapper.getDistance() - getTravelledDistance()) {
            // Chosen distance exceeds distance of remaining routeWrapper.
            // set it to the total routeWrapper distance
            distance = routeWrapper.getDistance() - getTravelledDistance();
        }
//        return routeWrapper.getWaypoints().get(Math.round(distance / RouteWrapper.DISTANCE_INTERVAL));
        // TODO
        return null;
    }

    public GeoCoordinate getPositionOnRouteByTime(int time) {
        RouteElements durationElements = routeWrapper.getRoute().getRouteElementsFromDuration(time);
        return durationElements.getElements().get(durationElements.getElements().size() - 1).getRoadElement().getGeometry().get(0);
    }
}
