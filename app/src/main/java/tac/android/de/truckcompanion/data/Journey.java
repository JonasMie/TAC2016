package tac.android.de.truckcompanion.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.Route;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import java.text.ParseException;
import java.util.ArrayList;

import static tac.android.de.truckcompanion.utils.Helper.getJsonStringFromAssets;

/**
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 15:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Journey {

    private static DataCollector dataCollector;

    private int id;
    private int driver_id;
    private int truck_id;
    private DispoInformation.StartPoint startPoint;
    private ArrayList<DispoInformation.DestinationPoint> destinationPoints;

    public Route getRoute() {
        return route;
    }

    public static DataCollector getDataCollector() {
        return dataCollector;
    }

    public int getId() {
        return id;
    }

    public int getDriver_id() {
        return driver_id;
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

    private Route route;


    public Journey(JSONObject journeyObj, DataCollector dataCollector, ProgressDialog mProgressDialog) throws JSONException, ParseException {
        this.id = journeyObj.getInt("id");
        this.driver_id = journeyObj.getInt("driver_id");
        this.truck_id = journeyObj.getInt("truck_id");
        this.startPoint = new DispoInformation.StartPoint(journeyObj.getJSONObject("start"));

        JSONArray stopsObjs = journeyObj.getJSONArray("stops");

        this.destinationPoints = new ArrayList<>();
        for (int i = 0; i < stopsObjs.length(); i++) {
            this.destinationPoints.add(new DispoInformation.DestinationPoint(stopsObjs.getJSONObject(i)));
        }

        this.route = new Route();
    }


    public static class LoadJourneyData extends AsyncTask<Integer, Void, Journey> {

        private final DataCollector dataCollector;
        private Context context;
        private ProgressDialog mProgressDialog;
        public AsyncResponse<Journey> callback = null;

        public LoadJourneyData(Context context, ProgressDialog mProgressDialog, AsyncResponse<Journey> callback, DataCollector dataCollector) {
            this.context = context;
            this.mProgressDialog = mProgressDialog;
            this.callback = callback;
            this.dataCollector = dataCollector;
        }

        @Override
        protected Journey doInBackground(Integer... params) {
            JSONArray journeys = null;
            try {
                journeys = new JSONArray(getJsonStringFromAssets(context, "dispo.json"));
                return new Journey(journeys.getJSONObject(params[0] - 1), dataCollector, mProgressDialog);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setTitle(R.string.loading_journey_data_title);
            mProgressDialog.setMessage(context.getString(R.string.loading_journey_data_msg));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Journey journey) {
            mProgressDialog.dismiss();
            callback.processFinish(journey);
        }
    }

    /**
     * Get travelled distance int.
     *
     * @return the int
     */
    public int getTravelledDistance() {
        // TODO implement getTravelledDistance
        return 0;
    }

    /**
     * Get travelled duration int.
     *
     * @return the int
     */
    public int getTravelledDuration() {
        // TODO implement getTravelledDuration
        return 0;
    }

    public LatLng getPositionOnRouteByDistance(int distance) {
        if (distance > route.getDistance() - getTravelledDistance()) {
            // Chosen distance exceeds distance of remaining route.
            // set it to the total route distance
            distance = route.getDistance() - getTravelledDistance();
        }
        return route.getWaypoints().get(distance / Route.DISTANCE_INTERVAL);
    }

    public LatLng getPositionOnRouteByTime(int time) {
        // TODO implement getPositionOnRouteByTime
        return null;
    }
}
