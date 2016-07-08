package tac.android.de.truckcompanion.geo;

import android.app.ProgressDialog;
import android.util.Log;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.customlocation.Request;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.routing.*;
import com.here.android.mpa.search.DiscoveryRequest;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.SearchRequest;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.utils.AsyncResponse;

import java.io.IOException;
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
    private static CoreRouter routeManager = new CoreRouter();
    private RoutePlan routePlan;
    private RouteOptions routeOptions;
    private RouteResult routeResult;
    private AsyncResponse<RouteWrapper> callback;
    private Route route;
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
            marker.setImageResource(R.drawable.marker);
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

    public void requestRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, final ProgressDialog progressDialog, final AsyncResponse<RouteWrapper> callback) {
        this.callback = callback;
        routePlan.removeAllWaypoints();
        final List<MapObject> markers = new ArrayList<>();
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(startPoint.getCoordinate().latitude, startPoint.getCoordinate().longitude)));
        for (DispoInformation.DestinationPoint destinationPoint :
                destinationPoints) {
            GeoCoordinate coordinate = new GeoCoordinate(destinationPoint.getCoordinate().latitude, destinationPoint.getCoordinate().longitude);
            routePlan.addWaypoint(new RouteWaypoint(coordinate));
            markers.add(new MapMarker(coordinate,marker));
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
                        map.removeMapObject(mapRoute);
                        mapRoute = new MapRoute(route);
                        map.addMapObject(mapRoute);
                        map.addMapObjects(markers);
                        callback.processFinish(RouteWrapper.this);
                    }
                } else {
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
}
