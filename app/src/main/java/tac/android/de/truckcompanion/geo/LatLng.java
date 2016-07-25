package tac.android.de.truckcompanion.geo;

/**
 * Created by Jonas Miederer.
 * Date: 02.07.2016
 * Time: 17:35
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class LatLng {
    /**
     * The Latitude.
     */
    public double latitude;
    /**
     * The Longitude.
     */
    public double longitude;

    /**
     * Instantiates a new Lat lng.
     *
     * @param latitude  the latitude
     * @param longitude the longitude
     */
    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
