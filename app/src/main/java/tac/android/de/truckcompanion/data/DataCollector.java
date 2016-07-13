package tac.android.de.truckcompanion.data;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.here.android.mpa.common.GeoCoordinate;
import org.json.JSONArray;
import org.json.JSONObject;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.LatLng;
import tac.android.de.truckcompanion.utils.Helper;
import tac.android.de.truckcompanion.utils.ResponseCallback;

import java.util.ArrayList;

/**
 * Created by Jonas Miederer.
 * Date: 07.05.16
 * Time: 18:13
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class DataCollector {

    public static final int ORDER_BY_DISTANCE_ASC = 0;
    public static final int ORDER_BY_DISTANCE_DESC = 1;
    public static final int ORDER_BY_PRICE_ASC = 2;
    public static final int ORDER_BY_PRICE_DESC = 3;
    public static final int ORDER_BY_ETA_ASC = 4;
    public static final int ORDER_BY_ETA_DESC = 5;

    private static final String GAS_API_BASE_URL = "https://creativecommons.tankerkoenig.de/json/";
    private static final String WEATHER_API_BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    private static final String GOOGLE_PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final String GOOGLE_DIRECTIONS_API_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String HERE_MATRIX_API_BASE_URL = "https://matrix.route.cit.api.here.com/routing/7.2/calculatematrix.json";

    private static final String GAS_API_KEY = "d807f7d7-3b4a-ade3-c086-7785718692d5";
    private static final String WEATHER_API_KEY = "a3318c093e5004906c70c9fc0da06def";
    private static final String GOOGLE_PLACES_API_KEY = "AIzaSyDQNm6h0XUY5UjvBJSDgZj-ORQ1CHhDyFs";
    private static final String GOOGLE_ANDROID_API_KEY = "AIzaSyBgddqXsREV4deEqia0D0Rmlpc7ckrbgKM";
    private static final String HERE_APP_ID = "lt922LLJOd0gYtIfJxSv";
    private static final String HERE_APP_CODE = "wMoF_ey5b3Vp_4ClFFO_2Q";

    private RequestQueue queue;

    public DataCollector(Context context) {
        queue = Volley.newRequestQueue(context);
    }


    public void getRestingPlace(float lat, float lng, int within, int ordered_by, int limit) {

    }

    public void getGasStation(float lat, float lng, int within, int ordered_by, int limit) {

    }

    /**
     * Gets details for specified gas station
     *
     * @param id       ID of the corresponding gas station
     * @param callback the success and error callback@
     */
    public void getGasPrice(String id, final ResponseCallback callback) {
        String url = GAS_API_BASE_URL +
                "detail.php" +
                "?id=" + id +
                "&apikey=" + GAS_API_KEY;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        queue.add(req);
    }

    /**
     * Gets gas station information for specified location
     *
     * @param lat        latitude coordinate
     * @param lng        longitude coordinate
     * @param within     the search radius (between 1 and 25 km)
     * @param ordered_by result ordering
     * @param limit      results limit
     * @param callback   the success and error callback
     */
    public void getGasPrices(double lat, double lng, int within, int ordered_by, int limit, final ResponseCallback callback) {
        String sort;
        /*
         The API allows searching in radius between 1km >= x <= 25km, so we have to clip the value.
         A negative given radius means we actually don't want to limit the radius, so we set it to the
         highest possible value (25)
        */
        within = within < 0 ? 25 : Helper.clip(within, 1, 25);

        /*
        The API allows us to order the results either by price (desc) or distance (asc)
         */
        if (ordered_by == ORDER_BY_DISTANCE_ASC) {
            sort = "dist";
        } else {
            sort = "price";
        }

        String url = GAS_API_BASE_URL +
                "list.php" +
                "?lat=" + lat +
                "&lng=" + lng +
                "&rad=" + within +
                "&sort=" + sort +
                "&type=diesel" +
                "&apikey=" + GAS_API_KEY;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        queue.add(req);

    }

    public void getWeather(double lat, double lng, int time, final ResponseCallback callback) {
        String url = WEATHER_API_BASE_URL +
                "?lat=" + lat +
                "&lon=" + lng +
                "&APPID=" + WEATHER_API_KEY;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        queue.add(req);

    }

    public void getPlacesNearby(double lat, double lng, int within, final ResponseCallback callback) {
        // Google proccesses radius in meters
        // TODO handle negative radius
        within *= 1000;

        String url = GOOGLE_PLACES_API_BASE_URL +
                "?location=" + lat + "," + lng +
                "&radius=" + within +
                "&keyword=" + "rastplatz" +
                "&key=" + GOOGLE_PLACES_API_KEY;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        queue.add(req);
    }

    public void getRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, final ResponseCallback callback) {
        String waypointsString = "";
        DispoInformation.DestinationPoint destinationPoint = destinationPoints.remove(destinationPoints.size() - 1);
        for (DispoInformation.DestinationPoint wayPoint : destinationPoints) {
            waypointsString += wayPoint.getCoordinate().latitude + "," + wayPoint.getCoordinate().longitude + "|";
        }
        if (waypointsString.length() > 0) {
            waypointsString = waypointsString.substring(0, waypointsString.length() - 1);
        }
        String url = GOOGLE_DIRECTIONS_API_BASE_URL +
                "?origin=" + startPoint.getCoordinate().latitude + "," + startPoint.getCoordinate().longitude +
                "&destination=" + destinationPoint.getCoordinate().latitude + "," + destinationPoint.getCoordinate().longitude +
                "&waypoints=" + waypointsString +
//                "&departure_time=" + startPoint.getDate().getTime() / 1000 +
                "&key=" + GOOGLE_PLACES_API_KEY;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });
        queue.add(req);
    }

    public void getWaypointMatrix(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, final ResponseCallback callback) {
        String url = HERE_MATRIX_API_BASE_URL +
                "?app_id=" + HERE_APP_ID +
                "&app_code=" + HERE_APP_CODE +
                "&start0=" + startPoint.getCoordinate().latitude + "," + startPoint.getCoordinate().longitude;
        // TODO: ignore last destinationPoint if its a circuit
        for (int i = 0; i < destinationPoints.size(); i++) {
            url += "&destination" + i + "=" + destinationPoints.get(i).getCoordinate().latitude + "," + destinationPoints.get(i).getCoordinate().longitude;
        }
        url += "&mode=fastest;truck;traffic:enabled";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });

        // Matrix calculation can take a few seconds since it's quite complex.
        // To prevent Volley-Timeouts, increase its accepted time
        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(req);
    }

    public void getWaypointMatrix(GeoCoordinate startPoint, ArrayList<GeoCoordinate> destinationPoints, final ResponseCallback callback) {
        String url = HERE_MATRIX_API_BASE_URL +
                "?app_id=" + HERE_APP_ID +
                "&app_code=" + HERE_APP_CODE +
                "&start0=" + startPoint.getLatitude() + "," + startPoint.getLongitude();
        // TODO: ignore last destinationPoint if its a circuit
        for (int i = 0; i < destinationPoints.size(); i++) {
                url += "&destination" + i + "=" + destinationPoints.get(i).getLatitude() + "," + destinationPoints.get(i).getLongitude();
        }
        url += "&mode=fastest;truck;traffic:disabled" +
                "&summaryAttributes=traveltime,costfactor,distance";

        // TODO: enable traffic

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error);
            }
        });

        // Matrix calculation can take a few seconds since it's quite complex.
        // To prevent Volley-Timeouts, increase its accepted time
        req.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(req);
    }
}
