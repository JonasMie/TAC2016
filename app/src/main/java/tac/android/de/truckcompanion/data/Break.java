package tac.android.de.truckcompanion.data;

import android.util.Log;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.search.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.LatLng;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.utils.AsyncResponse;

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
    private static final String TAG = Break.class.getSimpleName();
    private Roadhouse mainRoadhouse;
    private ArrayList<Roadhouse> alternativeRoadhouses;
    private DataCollector dc;
    private int elapsedTime;
    private AsyncResponse<Break> callback;

    private DispoInformation.DestinationPoint destinationPoint;

    private static ArrayList<Break> breaks = new ArrayList<>();

    public Break() {
        dc = new DataCollector(MainActivity.context);
    }

    public Break(GeoCoordinate loc) {
        dc = new DataCollector(MainActivity.context);
        calculateRoadhouses(loc, null, null);
        breaks.add(this);
    }

    public Break(int elapsedTime, int index) {
        dc = new DataCollector(MainActivity.context);
        this.elapsedTime = elapsedTime;
        breaks.add(index, this);
    }

    public Break(int elapsedTime, AsyncResponse callback) {
        dc = new DataCollector(MainActivity.context);
        calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(elapsedTime * 60), null, callback);
        breaks.add(this);
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

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void calculateRoadhouses(final GeoCoordinate loc, final Integer index, final AsyncResponse<Break> callback) {
        MainActivity.getmCurrentJourney().getRouteWrapper().runSearch(loc, "Raststätte", new ResultListener<DiscoveryResultPage>() {
            @Override
            public void onCompleted(DiscoveryResultPage discoveryResultPage, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    Break.this.mainRoadhouse = new Roadhouse((PlaceLink)discoveryResultPage.getItems().get(0));

//                    mainRoadhouseLink.getDetailsRequest().execute(new ResultListener<Place>() {
//                        @Override
//                        public void onCompleted(Place place, ErrorCode errorCode) {
//                            Log.d("test", "test");
//                        }
//                    });
                    Break.this.destinationPoint = new DispoInformation.DestinationPoint(new LatLng(Break.this.mainRoadhouse.getPlaceLink().getPosition().getLatitude(), Break.this.mainRoadhouse.getPlaceLink().getPosition().getLongitude()), 15);
                    callback.processFinish(Break.this, index);
                } else {
                    Log.e(TAG, "Place query failed with " + errorCode.toString());
                }
            }
        });
    

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

    public static ArrayList<Break> getBreaks() {
        return breaks;
    }

    public static void setBreaks(ArrayList<Break> breaks) {
        Break.breaks = breaks;
    }

    public static void removeBreak(int index) {
        MainActivity.getmCurrentJourney().removeDestinationPoint(breaks.get(index).destinationPoint);
        breaks.remove(index);
    }

    public void update(int elapsedTime, AsyncResponse<Break> callback) {
        setElapsedTime(elapsedTime);
        this.calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(elapsedTime), null, callback);
    }

    public DispoInformation.DestinationPoint getDestinationPoint() {
        return destinationPoint;
    }

    public void setDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoint = destinationPoint;
    }
}
