package tac.android.de.truckcompanion.data;

import android.util.Log;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.ResponseCallback;

import java.util.ArrayList;

/**
 * Created by Jonas Miederer.
 * Date: 15.06.2016
 * Time: 17:15
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */

public class Break {

    private static final int MAX_ALTERNATIVES = 4;
    private static final int MAX_SEARCHRADIUS = 50;

    private Roadhouse mainRoadhouse;
    private ArrayList<Roadhouse> alternativeRoadhouses;
    private DataCollector dc;

    public Break() {
        dc = new DataCollector(MainActivity.context);
    }

    public Break(LatLng loc) {
        dc = new DataCollector(MainActivity.context);
        calculateRoadhouses(loc, null);
    }

    public Break(int elapsedTime) {
        dc = new DataCollector(MainActivity.context);
        calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(elapsedTime), null);
    }


    public ArrayList<Roadhouse> getAlternativeRoadhouses() {
        return alternativeRoadhouses;
    }

    public void setAlternativeRoadhouses(ArrayList<Roadhouse> alternativeRoadhouses) {
        this.alternativeRoadhouses = alternativeRoadhouses;
    }

    public Roadhouse getMainRoadhouse() {
        return mainRoadhouse;
    }

    public void setMainRoadhouse(Roadhouse mainRoadhouse) {
        this.mainRoadhouse = mainRoadhouse;
    }

    public void calculateRoadhouses(LatLng loc, final AsyncResponse<Break> callback) {
        dc.getPlacesNearby(loc.latitude, loc.longitude, MAX_SEARCHRADIUS, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                alternativeRoadhouses = new ArrayList<>();
                JSONArray results;
                try {
                    results = result.getJSONArray("results");
                    int n_alternatives = MAX_ALTERNATIVES;
                    if (results != null) {
                        if (results.length() < MAX_ALTERNATIVES) {
                            n_alternatives = results.length();
                        }

                        for (int i = 0; i <= n_alternatives; i++) {
                            JSONObject roadhouse = results.getJSONObject(i);
                            // TODO: prioritize correctly
                            if (i == 0) {
                                // main roadhouse
                                setMainRoadhouse(produceNewRoadhouse(roadhouse));
                            } else {
                                // alternative roadhouse
                                alternativeRoadhouses.add(produceNewRoadhouse(roadhouse));
                            }
                        }
                        if (callback != null) {
                            callback.processFinish(Break.this);

                        }
                    }
                } catch (JSONException e) {
                    Log.e("TAC", e.getMessage());
                }
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    private Roadhouse produceNewRoadhouse(JSONObject roadhouse) throws JSONException {
        JSONObject location = roadhouse.getJSONObject("geometry").getJSONObject("location");
        JSONArray jTypes = roadhouse.getJSONArray("types");
        ArrayList<String> types = new ArrayList<>();
        for (int j = 0; j < jTypes.length(); j++) {
            types.add(jTypes.get(j).toString());
        }
        return new Roadhouse(
                roadhouse.getString("id"),
                roadhouse.getString("place_id"),
                roadhouse.getString("icon"),
                roadhouse.getString("name"),
                roadhouse.has("rating") ? roadhouse.getString("rating") : null,
                new LatLng(location.getDouble("lat"), location.getDouble("lng")),
                types
        );
    }
}
