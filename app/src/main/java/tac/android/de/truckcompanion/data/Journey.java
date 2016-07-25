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
public class Journey {

    // Members
    private int id;
    private int truck_id;
    private DispoInformation.StartPoint startPoint;
    private ArrayList<DispoInformation.DestinationPoint> destinationPoints;
    private RouteWrapper routeWrapper;

    /**
     * Instantiates a new Journey.
     *
     * @param journeyObj the journey obj
     * @throws JSONException  the json exception
     * @throws ParseException the parse exception
     */
    public Journey(JSONObject journeyObj) throws JSONException, ParseException {
        this.id = journeyObj.getInt("id");
        this.truck_id = journeyObj.getInt("truck_id");
        this.startPoint = new DispoInformation.StartPoint(journeyObj.getJSONObject("start"));

        JSONArray stopsObjs = journeyObj.getJSONArray("stops");

        this.destinationPoints = new ArrayList<>();
        for (int i = 0; i < stopsObjs.length(); i++) {
            this.destinationPoints.add(new DispoInformation.DestinationPoint(stopsObjs.getJSONObject(i)));
        }
    }


    /**
     * Gets id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Gets truck id.
     *
     * @return the truck id
     */
    public int getTruck_id() {
        return truck_id;
    }

    /**
     * Gets start point.
     *
     * @return the start point
     */
    public DispoInformation.StartPoint getStartPoint() {
        return startPoint;
    }

    /**
     * Gets destination points.
     *
     * @return the destination points
     */
    public ArrayList<DispoInformation.DestinationPoint> getDestinationPoints() {
        return destinationPoints;
    }

    /**
     * Sets destination points.
     *
     * @param destinationPoints the destination points
     */
    public void setDestinationPoints(ArrayList<DispoInformation.DestinationPoint> destinationPoints) {
        this.destinationPoints = destinationPoints;
    }

    /**
     * Add destination point.
     *
     * @param destinationPoint the destination point
     */
    public void addDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoints.add(destinationPoint);
    }

    /**
     * Remove destination point.
     *
     * @param destinationPoint the destination point
     */
    public void removeDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoints.remove(destinationPoint);
    }

    /**
     * Gets route wrapper.
     *
     * @return the route wrapper
     */
    public RouteWrapper getRouteWrapper() {
        return routeWrapper;
    }


    /**
     * Initialize the route wrapper.
     *
     * @return the route wrapper
     */
    public RouteWrapper initRoute() {
        this.routeWrapper = new RouteWrapper();
        return routeWrapper;
    }

    /**
     * Load the journey data from disposition (or json file respectively)
     */
    public static class LoadJourneyData extends AsyncTask<Object, Void, Journey> {
        private Context context;
        /**
         * The Callback.
         */
        public AsyncResponse<Journey> callback = null;

        /**
         * Instantiates a new Load journey data.
         *
         * @param context  the context
         * @param callback the callback
         */
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
     * Gets position on route by time.
     *
     * @param time the time
     * @return the position on route by time
     */
    public GeoCoordinate getPositionOnRouteByTime(int time) {
        RouteElements durationElements = routeWrapper.getRoute().getRouteElementsFromDuration(time);
        return durationElements.getElements().get(durationElements.getElements().size() - 1).getRoadElement().getGeometry().get(0);
    }
}
