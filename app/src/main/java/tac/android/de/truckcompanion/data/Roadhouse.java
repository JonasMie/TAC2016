package tac.android.de.truckcompanion.data;

import tac.android.de.truckcompanion.geo.LatLng;

import java.util.ArrayList;

/**
 * Created by Jonas Miederer.
 * Date: 09.06.16
 * Time: 14:35
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class Roadhouse {

    private String id;
    private String place_id;
    private String icon;
    private String name;
    private String rating;
    private LatLng location;
    private ArrayList<String> types;

    public Roadhouse(String id, String place_id, String icon, String name, String rating, LatLng location, ArrayList<String> types) {
        this.id = id;
        this.place_id = place_id;
        this.icon = icon;
        this.name = name;
        this.rating = rating;
        this.location = location;
        this.types = types;
    }

    public String getId() {
        return id;
    }

    public String getPlace_id() {
        return place_id;
    }

    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public LatLng getLocation() {
        return location;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

}
