package tac.android.de.truckcompanion.geo;

import android.app.ProgressDialog;
import android.util.Log;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.customlocation.Request;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.*;
import com.here.android.mpa.search.DiscoveryRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.SearchRequest;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.utils.AsyncResponse;

import java.util.ArrayList;
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
    private static RouteManager routeManager = new RouteManager();
    private RoutePlan routePlan;
    private RouteOptions routeOptions;
    private RouteResult routeResult;
    private AsyncResponse<RouteWrapper> callback;
    private Route route;
    private int duration;
    private int distance;

    private ArrayList<ArrayList> legs = new ArrayList<>();

    public RouteWrapper() {
        //routeManager.setTrafficPenaltyMode(); TODO
        routePlan = new RoutePlan();
        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.TRUCK);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);
    }

    public void requestRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, final ProgressDialog progressDialog, final AsyncResponse<RouteWrapper> callback) {
        this.callback = callback;
        routePlan.addWaypoint(new GeoCoordinate(startPoint.getCoordinate().latitude, startPoint.getCoordinate().longitude));
        for (DispoInformation.DestinationPoint destinationPoint :
                destinationPoints) {
            routePlan.addWaypoint(new GeoCoordinate(destinationPoint.getCoordinate().latitude, destinationPoint.getCoordinate().longitude));
        }
        routePlan.addWaypoint(new GeoCoordinate(startPoint.getCoordinate().latitude, startPoint.getCoordinate().longitude));
        RouteManager.Error error = routeManager.calculateRoute(routePlan, new RouteManager.Listener() {
            @Override
            public void onProgress(int i) {
                if (progressDialog != null) {
                    progressDialog.setMessage(MainActivity.context.getString(R.string.loading_route_data_msg) + " (" + i + "%)");
                }
                Log.d(TAG, "Routing calculation " + i + "% done");
            }

            @Override
            public void onCalculateRouteFinished(RouteManager.Error error, List<RouteResult> routeResults) {
                if (error == RouteManager.Error.NONE) {
                    if (routeResults.size() < 1) {
                        Log.e(TAG, "No route found");
                    } else {
                        Log.i(TAG, "Routing successful");
                        route = routeResults.get(0).getRoute();
                        MapRoute mapRoute = new MapRoute(route);
                        tac.android.de.truckcompanion.fragment.MapFragment mapFragment = (tac.android.de.truckcompanion.fragment.MapFragment) MainActivity.mViewPagerAdapter.getRegisteredFragment(1);
                        mapFragment.getMap().addMapObject(mapRoute);
                        callback.processFinish(RouteWrapper.this);
                    }
                } else {
                    Log.e(TAG, "Route calculation failed with: " + error.toString());
                }

            }
        });

        if (error != RouteManager.Error.NONE) {
            Log.e(TAG, "Route calculation failed with: " + error.toString());
        }
    }

//    public void setup(JSONObject result) throws JSONException {
//        googleRoute = result.getJSONArray("routes").getJSONObject(0);
//        waypoints = getWaypoints(new JSONArray(Helper.getJsonStringFromAssets(MainActivity.context, "live.json")));
//        fillLegs(googleRoute);
//    }

//    private void fillLegs(JSONObject googleRoute) throws JSONException {
//        JSONArray legs = googleRoute.getJSONArray("legs");
//
//        for (int i = 0; i < legs.length(); i++) {
//            ArrayList<HashMap<String, Integer>> stepsCont = new ArrayList<>();
//            JSONObject leg = legs.getJSONObject(i);
//            JSONArray steps = leg.getJSONArray("steps");
//
//            for (int j = 0; j < steps.length(); j++) {
//                HashMap<String, Integer> stepsMap = new HashMap<>();
//                JSONObject step = steps.getJSONObject(j);
//                int duration = step.getJSONObject("duration").getInt("value");
//                int distance = step.getJSONObject("distance").getInt("value");
//                stepsMap.put("duration", duration);
//                stepsMap.put("distance", distance);
//                this.duration += duration;
//                this.distance += distance;
//                stepsCont.add(stepsMap);
//            }
//            this.legs.add(stepsCont);
//        }
//    }

//    private static ArrayList<LatLng> getWaypoints(JSONArray json) {
//        ArrayList<LatLng> arrayList = new ArrayList<>();
//        for (int i = 0; i < json.length(); i++) {
//            try {
//                JSONObject obj = json.getJSONObject(i);
//                arrayList.add(new LatLng(obj.getDouble("lat"), obj.getDouble("lng")));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return arrayList;
//    }

//    public JSONObject getGoogleRoute() {
//        return googleRoute;
//    }

//    public ArrayList<LatLng> getWaypoints() {
//        return waypoints;
//    }

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
}
