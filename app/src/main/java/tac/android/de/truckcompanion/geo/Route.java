package tac.android.de.truckcompanion.geo;

import android.util.Log;
import com.android.volley.VolleyError;
import org.json.JSONObject;
import tac.android.de.truckcompanion.data.DataCollector;
import tac.android.de.truckcompanion.dispo.DispoInformation;
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
    public Route(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, DataCollector dataCollector) {
        dataCollector.getRoute(startPoint, destinationPoints, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.d("TAC","got result");
            }

            @Override
            public void onError(VolleyError error) {
                Log.e("TAC",error.getMessage());
            }
        });
    }
}
