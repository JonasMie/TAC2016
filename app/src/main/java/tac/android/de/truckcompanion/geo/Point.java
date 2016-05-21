package tac.android.de.truckcompanion.geo;

import java.util.Date;

/**
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 17:38
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Point {
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    protected double lat;
    protected double lng;
}
