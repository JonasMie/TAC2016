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

    private static ArrayList<Break> breaks = new ArrayList<>();

    private WheelEntry wheelEntry;
    private Roadhouse mainRoadhouse;
    private ArrayList<Roadhouse> alternativeRoadhouses = new ArrayList<>();
    private DispoInformation.DestinationPoint destinationPoint;
    private ClusterLayer clusterLayer;
    private MapObject mainRoadhouseMarker;

    private int elapsedTime;
    private int index;
    private int nTry = 1;
    private double meanGasPrice;

    // Constants
    private static final String TAG = Break.class.getSimpleName();

    /**
     * Instantiates a new Break.
     *
     * @param elapsedTime the elapsed time
     * @param index       the index
     * @param wheelEntry  the wheel entry
     */
    public Break(int elapsedTime, int index, WheelEntry wheelEntry) {
        this.elapsedTime = elapsedTime;
        this.index = index;
        this.wheelEntry = wheelEntry;
        breaks.add(index, this);
    }

    /**
     * Instantiates a new Break. Used to create a shallow copy
     *
     * @param pause the pause
     */
    public Break(Break pause) {
        this.wheelEntry = pause.getWheelEntry();
        this.index = pause.getIndex();
        this.mainRoadhouse = pause.getMainRoadhouse();
        this.alternativeRoadhouses = pause.getAlternativeRoadhouses();
        this.elapsedTime = pause.getElapsedTime();
    }

    /**
     * Gets alternative roadhouses.
     *
     * @return the alternative roadhouses
     */
    public ArrayList<Roadhouse> getAlternativeRoadhouses() {
        return alternativeRoadhouses;
    }

    /**
     * Sets alternative roadhouses.
     *
     * @param alternativeRoadhouses the alternative roadhouses
     */
    public void setAlternativeRoadhouses(ArrayList<Roadhouse> alternativeRoadhouses) {
        this.alternativeRoadhouses = alternativeRoadhouses;
    }

    /**
     * Gets main roadhouse.
     *
     * @return the main roadhouse
     */
    public Roadhouse getMainRoadhouse() {
        return mainRoadhouse;
    }

    /**
     * Sets main roadhouse.
     *
     * @param mainRoadhouse the main roadhouse
     */
    public void setMainRoadhouse(Roadhouse mainRoadhouse) {
        this.mainRoadhouse = mainRoadhouse;
    }

    /**
     * Gets elapsed time.
     *
     * @return the elapsed time
     */
    public int getElapsedTime() {
        return elapsedTime;
    }

    /**
     * Sets elapsed time.
     *
     * @param elapsedTime the elapsed time
     */
    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * Calculate the roadhouses for the break.
     *
     * @param loc        the loc
     * @param refPoint   the ref point
     * @param pauseIndex the pause index
     * @param callback   the callback
     */
    public void calculateRoadhouses(final GeoCoordinate loc, final GeoCoordinate refPoint, final int pauseIndex, final AsyncResponse<Break> callback) {
        // Search HERE Maps for "Rastsätte" whitin the default radius
        MainActivity.getCurrentJourney().getRouteWrapper().runSearch(loc, "Raststätte", new ResultListener<DiscoveryResultPage>() {
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
                                    // order roadhouses by distance
                                    JSONArray entries = output.getJSONObject("response").getJSONArray("matrixEntry");
                                    for (int i = 0; i < entries.length(); i++) {
                                        // check if roadhouse is located within the allowed driving time (taking the tolerance into acocunt)
                                        if (entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime") <= MainFragment.MAX_DRIVE_VAL + MainFragment.MAX_DRIVE_TOLERANCE) {
                                            if (mainRoadhouse == null) {
                                                // set new main roadhouse if none exists yet
                                                Break.this.mainRoadhouse = new Roadhouse((PlaceLink) discoveryResultPage.getItems().get(entries.getJSONObject(i).getInt("destinationIndex")));
                                                Break.this.mainRoadhouse.setDurationFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime"));
                                                Break.this.mainRoadhouse.setDistanceFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("distance"));
                                                // here-maps actually delivers ratings, but they are all set to zero.. so we fake them
                                                Break.this.mainRoadhouse.setRating(Math.random() * 5);
                                            } else {
                                                // if another main roadhouse already exists for the break, set the current roadhouse as alternative
                                                Roadhouse altRoadhouse = new Roadhouse((PlaceLink) discoveryResultPage.getItems().get(entries.getJSONObject(i).getInt("destinationIndex")));
                                                altRoadhouse.setDurationFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime"));
                                                altRoadhouse.setRating(Math.random() * 5);
                                                Break.this.alternativeRoadhouses.add(altRoadhouse);
                                            }
                                            Break.this.destinationPoint = new DispoInformation.DestinationPoint(new LatLng(Break.this.mainRoadhouse.getPlaceLink().getPosition().getLatitude(), Break.this.mainRoadhouse.getPlaceLink().getPosition().getLongitude()), 15);
                                        }
                                    }
                                    // check if we actually found some roadhouses. If we found none, start the recalculation for the position 30 min earlier
                                    if (Break.this.getMainRoadhouse() == null) {
                                        nTry++;
                                        calculateRoadhouses(MainActivity.getCurrentJourney().getPositionOnRouteByTime(breaks.get(pauseIndex).getElapsedTime() - 30 * 60 * nTry), refPoint, pauseIndex, callback);
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


    /**
     * Gets breaks.
     *
     * @return the breaks
     */
    public static ArrayList<Break> getBreaks() {
        return breaks;
    }

    /**
     * Sets breaks.
     *
     * @param breaks the breaks
     */
    public static void setBreaks(ArrayList<Break> breaks) {
        Break.breaks = breaks;
    }

    /**
     * Remove break.
     *
     * @param index the index
     */
    public static void removeBreak(int index) {
        MainActivity.getCurrentJourney().removeDestinationPoint(breaks.get(index).destinationPoint);
        breaks.remove(index);
    }

    /**
     * Remove break.
     *
     * @param pause the pause
     */
    public static void removeBreak(Break pause) {
        breaks.remove(pause);
    }

    /**
     * Add break.
     *
     * @param pause the pause
     */
    public static void addBreak(Break pause) {
        breaks.add(pause);
    }

    /**
     * Update the roadhouse for the corresponding break.
     *
     * @param elapsedTime the elapsed time
     * @param refPoint    the reference point
     * @param pauseIndex  the pause index
     * @param callback    the callback
     */
    public void update(int elapsedTime, GeoCoordinate refPoint, int pauseIndex, AsyncResponse<Break> callback) {
        setElapsedTime(elapsedTime);
        this.setMainRoadhouse(null);
        this.alternativeRoadhouses.clear();
        this.calculateRoadhouses(MainActivity.getCurrentJourney().getPositionOnRouteByTime(elapsedTime), refPoint, pauseIndex, callback);
    }

    /**
     * Gets destination point.
     *
     * @return the destination point
     */
    public DispoInformation.DestinationPoint getDestinationPoint() {
        return destinationPoint;
    }

    /**
     * Sets destination point.
     *
     * @param destinationPoint the destination point
     */
    public void setDestinationPoint(DispoInformation.DestinationPoint destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    /**
     * Gets pause index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets pause index.
     *
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }


    /**
     * Gets the wheel entry.
     *
     * @return the wheel entry
     */
    public WheelEntry getWheelEntry() {
        return wheelEntry;
    }

    /**
     * Sets the wheel entry.
     *
     * @param wheelEntry the wheel entry
     */
    public void setWheelEntry(WheelEntry wheelEntry) {
        this.wheelEntry = wheelEntry;
    }

    /**
     * Sets the cluster layer.
     *
     * @param clusterLayer the cluster layer
     */
    public void setClusterLayer(ClusterLayer clusterLayer) {
        this.clusterLayer = clusterLayer;
    }

    /**
     * Gets the cluster layer.
     *
     * @return the cluster layer
     */
    public ClusterLayer getClusterLayer() {
        return clusterLayer;
    }

    /**
     * Gets the main roadhouse marker.
     *
     * @return the main roadhouse marker
     */
    public MapObject getMainRoadhouseMarker() {
        return mainRoadhouseMarker;
    }

    /**
     * Sets the main roadhouse marker.
     *
     * @param mainRoadhouseMarker the main roadhouse marker
     */
    public void setMainRoadhouseMarker(MapObject mainRoadhouseMarker) {
        this.mainRoadhouseMarker = mainRoadhouseMarker;
    }

    /**
     * Sets the mean gas price.
     *
     * @param meanGasPrice the mean gas price
     */
    public void setMeanGasPrice(double meanGasPrice) {
        this.meanGasPrice = meanGasPrice;
    }

    /**
     * Gets the mean gas price.
     *
     * @return the mean gas price
     */
    public double getMeanGasPrice() {
        return meanGasPrice;
    }
}
