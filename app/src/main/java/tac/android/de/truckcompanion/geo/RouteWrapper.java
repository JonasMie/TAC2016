package tac.android.de.truckcompanion.geo;

import android.app.ProgressDialog;
import android.graphics.PointF;
import android.util.Log;
import android.widget.TextView;
import com.android.volley.VolleyError;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.customlocation.Request;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.*;
import com.here.android.mpa.search.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.DataCollector;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.ResponseCallback;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jonas Miederer.
 * Date: 11.05.16
 * Time: 16:17
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class RouteWrapper {

    // Routing related stuff
    private Route route;
    private RoutePlan routePlan;
    private static CoreRouter routeManager = new CoreRouter();

    // Map related stuff
    private Map map;
    private MapRoute mapRoute;
    private tac.android.de.truckcompanion.fragment.MapFragment mapFragment;
    private Image marker_start_img;
    private MapMarker marker_start;
    private PointF anchor_point_start;
    private Image marker_finish_img;
    private MapMarker marker_finish;
    private PointF anchor_point_finish;

    // Misc
    private Date departureTime;
    private boolean calculationFinished = false;

    // Constants
    private static final String TAG = "TAC";

    /**
     * Instantiates a new Route wrapper.
     */
    public RouteWrapper() {
        mapFragment = (tac.android.de.truckcompanion.fragment.MapFragment) MainActivity.viewPagerAdapter.getRegisteredFragment(1);
        map = mapFragment.getMap();

        // set map marker for start and destination
        marker_start_img = new Image();
        marker_finish_img = new Image();
        try {
            marker_start_img.setImageResource(R.drawable.marker_main);
            anchor_point_start = new PointF(marker_start_img.getWidth() / 2, marker_start_img.getHeight());
            marker_finish_img.setImageResource(R.drawable.ic_flag_black_32dp);
            anchor_point_finish = new PointF(marker_finish_img.getWidth() / 2, marker_finish_img.getHeight());
        } catch (IOException e) {
            Log.e(TAG, "Marker image not found");
        }

        // set route related stuff
        routePlan = new RoutePlan();
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.TRUCK);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);
    }

    /**
     * Request route from HERE Maps.
     *
     * @param startPoint        the start point
     * @param destinationPoints the destination points
     * @param textToUpdate      the text to update
     * @param callback          the callback
     */
    public void requestRoute(DispoInformation.StartPoint startPoint, final ArrayList<DispoInformation.DestinationPoint> destinationPoints, final Object textToUpdate, final AsyncResponse<RouteWrapper> callback) {
        calculationFinished = false;
        routePlan.removeAllWaypoints();
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(startPoint.getCoordinate().latitude, startPoint.getCoordinate().longitude)));

        for (DispoInformation.DestinationPoint destinationPoint :
                destinationPoints) {
            GeoCoordinate coordinate = new GeoCoordinate(destinationPoint.getCoordinate().latitude, destinationPoint.getCoordinate().longitude);
            routePlan.addWaypoint(new RouteWaypoint(coordinate));
        }
        routeManager.calculateRoute(routePlan, new CoreRouter.Listener() {
            @Override
            public void onProgress(int i) {
                if (textToUpdate != null) {
                    if (textToUpdate instanceof ProgressDialog) {
                        ((ProgressDialog) textToUpdate).setMessage(MainActivity.context.getString(R.string.loading_route_data_msg) + " (" + i + "%)");
                    } else if (textToUpdate instanceof TextView) {
                        ((TextView) textToUpdate).setText(MainActivity.context.getString(R.string.loading_route_data_msg) + " (" + i + "%)");
                    }
                }
                Log.d(TAG, "Routing calculation " + i + "% done");
            }

            @Override
            public void onCalculateRouteFinished(List<RouteResult> routeResults, RoutingError error) {
                if (error == RoutingError.NONE) {
                    if (routeResults.size() < 1) {
                        Log.e(TAG, "No route found");
                    } else {
                        Log.i(TAG, "Routing successful");
                        route = routeResults.get(0).getRoute();
                        MainActivity.getCurrentJourney().getRouteWrapper().setRoute(route);
                        departureTime = new Date();
                        map.removeMapObject(mapRoute);
                        map.removeMapObject(marker_start);
                        map.removeMapObject(marker_finish);

                        mapRoute = new MapRoute(route);

                        // Set marker, route, ... in map
                        map.addMapObject(mapRoute);
                        marker_start = new MapMarker(route.getStart(), marker_start_img);
                        marker_start.setAnchorPoint(anchor_point_start);
                        marker_finish = new MapMarker(route.getDestination(), marker_finish_img);
                        marker_finish.setAnchorPoint(anchor_point_finish);
                        map.addMapObject(marker_start);
                        map.addMapObject(marker_finish);
                        map.setCenter(route.getStart(), Map.Animation.BOW);

                        calculationFinished = true;

                        callback.processFinish(RouteWrapper.this);
                    }
                } else {
                    if (error == RoutingError.REQUEST_TIMEOUT) {
                        callback.processFinish(null);
                    }
                    Log.e(TAG, "Route calculation failed with: " + error.toString());
                }

            }
        });

    }

    /**
     * Gets route.
     *
     * @return the route
     */
    public Route getRoute() {
        return route;
    }

    /**
     * Sets route.
     *
     * @param route the route
     */
    public void setRoute(Route route) {
        this.route = route;
    }

    /**
     * Run HERE Maps search.
     *
     * @param loc            the loc
     * @param searchTerm     the search term
     * @param resultListener the result listener
     */
    public void runSearch(GeoCoordinate loc, String searchTerm, ResultListener resultListener) {
        DiscoveryRequest request = new SearchRequest(searchTerm).setSearchCenter(loc);

        // limit number of items in each result page to 10
        request.setCollectionSize(10);
        request.execute(resultListener);
    }

    /**
     * Gets ordered waypoints.
     *
     * @param startPoint        the start point
     * @param destinationPoints the destination points
     * @param _key              the key
     * @param callback          the callback
     */
    public static void getOrderedWaypoints(DispoInformation.StartPoint startPoint, final ArrayList<DispoInformation.DestinationPoint> destinationPoints, String _key, final AsyncResponse<ArrayList> callback) {
        final String key;
        if (_key == null) {
            key = "costFactor";
        } else {
            key = _key;
        }

        DataCollector dc = new DataCollector(MainActivity.context);
        final ArrayList<DispoInformation.DestinationPoint> orderedDestPoints = new ArrayList<>();
        dc.getWaypointMatrix(startPoint, destinationPoints, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray entries = result.getJSONObject("response").getJSONArray("matrixEntry");
                    ArrayList<Integer> costs = new ArrayList<>();

                    for (int i = 0; i < entries.length(); i++) {
                        JSONObject entry = entries.getJSONObject(i);
                        int index = entry.getInt("destinationIndex");
                        int cost = entry.getJSONObject("summary").getInt(key);
                        if (costs.size() == 0) {
                            costs.add(0, cost);
                            orderedDestPoints.add(0, destinationPoints.get(0));
                        } else {
                            for (int j = 0; j < costs.size(); j++) {
                                if (costs.get(j) > cost) {
                                    costs.add(j, cost);
                                    orderedDestPoints.add(j, destinationPoints.get(index));
                                    break;
                                } else if (costs.get(j) <= cost && j == costs.size() - 1) {
                                    costs.add(cost);
                                    orderedDestPoints.add(destinationPoints.get(index));
                                    break;
                                }
                            }
                        }

                    }
                    callback.processFinish(orderedDestPoints);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Route matrix calculation failed: " + error.getMessage());
            }
        });
    }

    /**
     * Gets ordered waypoints.
     *
     * @param startPoint the start point
     * @param points     the points
     * @param _key       the key
     * @param callback   the callback
     */
    public static void getOrderedWaypoints(GeoCoordinate startPoint, final ArrayList<DiscoveryResult> points, String _key, final AsyncResponse<ArrayList> callback) {
        final String key;
        if (_key == null) {
            key = "costFactor";
        } else {
            key = _key;
        }

        DataCollector dc = new DataCollector(MainActivity.context);
        final ArrayList<DiscoveryResult> orderedDestPoints = new ArrayList<>();

        ArrayList<GeoCoordinate> destinationPoints = new ArrayList<>();
        for (DiscoveryResult res : points) {
            PlaceLink placeLink = (PlaceLink) res;
            destinationPoints.add(new GeoCoordinate(placeLink.getPosition().getLatitude(), placeLink.getPosition().getLongitude()));
        }

        dc.getWaypointMatrix(startPoint, destinationPoints, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray entries = result.getJSONObject("response").getJSONArray("matrixEntry");
                    ArrayList<Integer> costs = new ArrayList<>();

                    for (int i = 0; i < entries.length(); i++) {
                        JSONObject entry = entries.getJSONObject(i);
                        int index = entry.getInt("destinationIndex");
                        int cost = entry.getJSONObject("summary").getInt(key);
                        if (costs.size() == 0) {
                            costs.add(0, cost);
                            orderedDestPoints.add(0, points.get(0));
                        } else {
                            for (int j = 0; j < costs.size(); j++) {
                                if (costs.get(j) > cost) {
                                    costs.add(j, cost);
                                    orderedDestPoints.add(j, points.get(index));
                                    break;
                                } else if (costs.get(j) <= cost && j == costs.size() - 1) {
                                    costs.add(cost);
                                    orderedDestPoints.add(points.get(index));
                                    break;
                                }
                            }
                        }

                    }
                    callback.processFinish(orderedDestPoints);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, "Route matrix calculation failed: " + error.getMessage());
            }
        });
    }

    /**
     * Gets waypoint matrix.
     *
     * @param startPoint the start point
     * @param points     the points
     * @param callback   the callback
     */
    public static void getWaypointMatrix(GeoCoordinate startPoint, final ArrayList<DiscoveryResult> points, final AsyncResponse<JSONObject> callback) {
        DataCollector dc = new DataCollector(MainActivity.context);
        final ArrayList<DiscoveryResult> orderedDestPoints = new ArrayList<>();

        ArrayList<GeoCoordinate> destinationPoints = new ArrayList<>();
        for (DiscoveryResult res : points) {
            PlaceLink placeLink = (PlaceLink) res;
            destinationPoints.add(new GeoCoordinate(placeLink.getPosition().getLatitude(), placeLink.getPosition().getLongitude()));
        }

        dc.getWaypointMatrix(startPoint, destinationPoints, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                callback.processFinish(result);
            }

            @Override
            public void onError(VolleyError error) {
                callback.processFinish(null);
            }
        });
    }

    /**
     * Gets departure time.
     *
     * @return the departure time
     */
    public Date getDepartureTime() {
        return departureTime;
    }

    /**
     * Sets departure time.
     *
     * @param departureTime the departure time
     */
    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    /**
     * Gets calculation finished.
     *
     * @return the calculation finished
     */
    public boolean getCalculationFinished() {
        return calculationFinished;
    }

    /**
     * Sets calculation finished.
     *
     * @param calculationFinished the calculation finished
     */
    public void setCalculationFinished(boolean calculationFinished) {
        this.calculationFinished = calculationFinished;
    }
}
