package tac.android.de.truckcompanion.geo;

import android.location.Location;
import com.here.android.mpa.common.GeoCoordinate;

/**
 * Created by Jonas Miederer.
 * Date: 26.05.2016
 * Time: 16:29
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class GeoHelper {

    public static Location getLocation(String title, double lat, double lng) {
        Location loc = new Location(title);
        loc.setLatitude(lat);
        loc.setLongitude(lng);

        return loc;
    }
    public static GeoCoordinate LatLngToGeoCoordinate(LatLng coord) {
        return new GeoCoordinate(coord.latitude, coord.longitude);
    }
}
