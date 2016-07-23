package tac.android.de.truckcompanion.data;

import android.util.Log;
import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.search.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.fragment.MainFragment;
import tac.android.de.truckcompanion.geo.LatLng;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.wheel.WheelEntry;

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
    private WheelEntry wheelEntry;
    private int index;
    private Roadhouse mainRoadhouse;
    private ArrayList<Roadhouse> alternativeRoadhouses = new ArrayList<>();
    private int elapsedTime;

    private DispoInformation.DestinationPoint destinationPoint;

    private static ArrayList<Break> breaks = new ArrayList<>();
    private int nTry = 1;
    private ClusterLayer clusterLayer;
    private MapObject mainRoadhouseMarker;
    private double meanGasPrice;

    public Break(int elapsedTime, int index, WheelEntry wheelEntry) {
        this.elapsedTime = elapsedTime;
        this.index = index;
        this.wheelEntry = wheelEntry;
        breaks.add(index, this);
    }

    public Break(Break pause) {
        this.wheelEntry = pause.getWheelEntry();
        this.index = pause.getIndex();
        this.mainRoadhouse = pause.getMainRoadhouse();
        this.alternativeRoadhouses = pause.getAlternativeRoadhouses();
        this.elapsedTime = pause.getElapsedTime();
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

    public void calculateRoadhouses(final GeoCoordinate loc, final GeoCoordinate refPoint, final int pauseIndex, final AsyncResponse<Break> callback) {
        MainActivity.getmCurrentJourney().getRouteWrapper().runSearch(loc, "Raststätte", new ResultListener<DiscoveryResultPage>() {
            @Override
            public void onCompleted(final DiscoveryResultPage discoveryResultPage, ErrorCode errorCode) {
                if (errorCode == ErrorCode.NONE) {
                    RouteWrapper.getWaypointMatrix(refPoint, (ArrayList) discoveryResultPage.getItems(), new AsyncResponse<JSONObject>() {

                        @Override
                        public void processFinish(JSONObject output) {
                            if (output == null) {
                                // Error
                            } else {
                                try {
                                    JSONArray entries = output.getJSONObject("response").getJSONArray("matrixEntry");
                                    for (int i = 0; i < entries.length(); i++) {
                                        if (entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime") <= MainFragment.MAX_DRIVE_VAL + MainFragment.MAX_DRIVER_TOLERANCE) {
                                            if (mainRoadhouse == null) {
                                                Break.this.mainRoadhouse = new Roadhouse((PlaceLink) discoveryResultPage.getItems().get(entries.getJSONObject(i).getInt("destinationIndex")));
                                                Break.this.mainRoadhouse.setDurationFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime"));
                                                Break.this.mainRoadhouse.setDistanceFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("distance"));
                                                // here-maps actually delivers ratings, but they are all set to zero.. so we fake them
                                                Break.this.mainRoadhouse.setRating(Math.random() * 5);
                                            } else {
                                                Roadhouse altRoadhouse = new Roadhouse((PlaceLink) discoveryResultPage.getItems().get(entries.getJSONObject(i).getInt("destinationIndex")));
                                                altRoadhouse.setDurationFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime"));
                                                altRoadhouse.setRating(Math.random() * 5);
                                                Break.this.alternativeRoadhouses.add(altRoadhouse);
                                            }
                                            Break.this.destinationPoint = new DispoInformation.DestinationPoint(new LatLng(Break.this.mainRoadhouse.getPlaceLink().getPosition().getLatitude(), Break.this.mainRoadhouse.getPlaceLink().getPosition().getLongitude()), 15);
                                        }
                                    }
                                    if (Break.this.getMainRoadhouse() == null) {
                                        nTry++;
                                        calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(breaks.get(pauseIndex).getElapsedTime() - 30 * 60 * nTry), refPoint, pauseIndex, callback);
                                    } else {
                                        nTry = 0;
                                        callback.processFinish(Break.this);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Place query failed with " + errorCode.toString());
                }
            }
        });
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

    public static void removeBreak(Break pause) {
        breaks.remove(pause);
    }

    public static void addBreak(Break pause) {
        breaks.add(pause);
    }

    public void update(int elapsedTime, GeoCoordinate refPoint, int pauseIndex, AsyncResponse<Break> callback) {
        setElapsedTime(elapsedTime);
        this.setMainRoadhouse(null);
        this.alternativeRoadhouses.clear();
        this.calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(elapsedTime), refPoint, pauseIndex, callback);
    }

    public DispoInformation.DestinationPoint getDestinationPoint() {
        return destinationPoint;
    }

    public void setDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    public WheelEntry getWheelEntry() {
        return wheelEntry;
    }

    public void setWheelEntry(WheelEntry wheelEntry) {
        this.wheelEntry = wheelEntry;
    }

    public void setClusterLayer(ClusterLayer clusterLayer) {
        this.clusterLayer = clusterLayer;
    }

    public ClusterLayer getClusterLayer() {
        return clusterLayer;
    }

    public MapObject getMainRoadhouseMarker() {
        return mainRoadhouseMarker;
    }

    public void setMainRoadhouseMarker(MapObject mainRoadhouseMarker) {
        this.mainRoadhouseMarker = mainRoadhouseMarker;
    }

    public void setMeanGasPrice(double meanGasPrice) {
        this.meanGasPrice = meanGasPrice;
    }

    public double getMeanGasPrice() {
        return meanGasPrice;
    }
}
