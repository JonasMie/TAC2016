package tac.android.de.truckcompanion.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.Point;
import tac.android.de.truckcompanion.utils.AsyncResponse;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 15:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Journey {

    private int id;
    private int driver_id;
    private int truck_id;
    private DispoInformation.StartPoint startPoint;
    private ArrayList<DispoInformation.DestinationPoint> destinationPoints;

    public Journey() {

    }

    public Journey(JSONObject journeyObj) throws JSONException, ParseException {
        this.id = journeyObj.getInt("id");
        this.driver_id = journeyObj.getInt("driver_id");
        this.truck_id= journeyObj.getInt("truck_id");
        this.startPoint = new DispoInformation.StartPoint(journeyObj.getJSONObject("start"));

        JSONArray stopsObjs = journeyObj.getJSONArray("stops");

        this.destinationPoints = new ArrayList<>();
        for(int i = 0; i<stopsObjs.length(); i++){
            this.destinationPoints.add(new DispoInformation.DestinationPoint(stopsObjs.getJSONObject(i)));
        }
    }

    public static String getJourneys(Context context){
        String json = null;
        try{
            InputStream is = context.getAssets().open("dispo.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }


    public static class LoadJourneyData extends AsyncTask<Integer, Void, Journey> {

        private Context context;
        private ProgressDialog mProgressDialog;
        public AsyncResponse<Journey> callback = null;

        public LoadJourneyData(Context context, ProgressDialog mProgressDialog, AsyncResponse<Journey> callback) {
            this.context = context;
            this.mProgressDialog = mProgressDialog;
            this.callback = callback;
        }

        @Override
        protected Journey doInBackground(Integer... params) {
            JSONArray journeys= null;
            try {
                journeys = new JSONArray(getJourneys(context));
                return new Journey(journeys.getJSONObject(params[0]-1));
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
}
