package tac.android.de.truckcompanion.geo;

import android.app.ProgressDialog;
import android.util.Log;
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

    public static final int DISTANCE_INTERVAL = 10;
    private static final String TAG = "TAC";
    private static CoreRouter routeManager = new CoreRouter();
    private RoutePlan routePlan;
    private RouteOptions routeOptions;
    private RouteResult routeResult;
    private AsyncResponse<RouteWrapper> callback;
    private Route route;
    private Date departureTime;
    private Map map;
    private MapRoute mapRoute;
    private tac.android.de.truckcompanion.fragment.MapFragment mapFragment;
    private Image marker;
    private int duration;
    private int distance;

    private ArrayList<ArrayList> legs = new ArrayList<>();

    public RouteWrapper() {
        mapFragment = (tac.android.de.truckcompanion.fragment.MapFragment) MainActivity.mViewPagerAdapter.getRegisteredFragment(1);
        map = mapFragment.getMap();
        marker = new Image();
        try {
            marker.setImageResource(R.drawable.marker_main);
        } catch (IOException e) {
            Log.e(TAG, "Marker image not found");
        }
        //routeManager.setTrafficPenaltyMode(); TODO
        routePlan = new RoutePlan();
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.TRUCK);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);
    }

    public void requestRoute(DispoInformation.StartPoint startPoint, final ArrayList<DispoInformation.DestinationPoint> destinationPoints, final ProgressDialog progressDialog, final AsyncResponse<RouteWrapper> callback) {
        this.callback = callback;
        routePlan.removeAllWaypoints();
        final List<MapObject> markers = new ArrayList<>();
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(startPoint.getCoordinate().latitude, startPoint.getCoordinate().longitude)));


        for (DispoInformation.DestinationPoint destinationPoint :
                destinationPoints) {
            GeoCoordinate coordinate = new GeoCoordinate(destinationPoint.getCoordinate().latitude, destinationPoint.getCoordinate().longitude);
            routePlan.addWaypoint(new RouteWaypoint(coordinate));
        }
        routeManager.calculateRoute(routePlan, new CoreRouter.Listener() {
            @Override
            public void onProgress(int i) {
                if (progressDialog != null) {
                    progressDialog.setMessage(MainActivity.context.getString(R.string.loading_route_data_msg) + " (" + i + "%)");
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
                        departureTime = new Date();
                        map.removeMapObject(mapRoute);
                        mapRoute = new MapRoute(route);
                        map.addMapObject(mapRoute);
                        map.setCenter(GeoHelper.LatLngToGeoCoordinate(destinationPoints.get(0).getCoordinate()), Map.Animation.BOW);
                        mapRoute.setRenderType(MapRoute.RenderType.PRIMARY);
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

    public int getDuration() {
        return duration;
    }

    public int getDistance() {
        return distance;
    }

    public ArrayList<ArrayList> getLegs() {
        return legs;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public void runSearch(GeoCoordinate loc, String searchTerm, ResultListener resultListener) {
        DiscoveryRequest request = new SearchRequest(searchTerm).setSearchCenter(loc);

        // limit number of items in each result page to 10
        request.setCollectionSize(10);
        request.execute(resultListener);
    }

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

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }
}
