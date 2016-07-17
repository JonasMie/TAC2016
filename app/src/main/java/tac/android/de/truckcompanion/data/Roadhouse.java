package tac.android.de.truckcompanion.data;

import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.Place;
import com.here.android.mpa.search.PlaceLink;
import com.nokia.maps.PlacesLink;
import tac.android.de.truckcompanion.geo.LatLng;

import java.util.ArrayList;
import java.util.Date;

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
    private PlaceLink placeLink;
    private Place place;
    private Date ETA;
    private long durationFromStart;
    private boolean detailsLoading = false;

    public Roadhouse(String id, String place_id, String icon, String name, String rating, LatLng location, ArrayList<String> types) {
        this.id = id;
        this.place_id = place_id;
        this.icon = icon;
        this.name = name;
        this.rating = rating;
        this.location = location;
        this.types = types;
    }

    public Roadhouse(PlaceLink placeLink) {
        this.placeLink = placeLink;
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

    public Date getETA() {
        return ETA;
    }

    public void setETA(Date ETA) {
        this.ETA = ETA;
    }

    public PlaceLink getPlaceLink() {
        return placeLink;
    }

    public void setPlaceLink(PlaceLink placeLink) {
        this.placeLink = placeLink;
    }

    public long getDurationFromStart() {
        return durationFromStart;
    }

    public void setDurationFromStart(long durationFromStart) {
        this.durationFromStart = durationFromStart;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public void onDetailsLoaded() {
        detailsLoading = false;
    }

    public boolean isDetailsLoading() {
        return detailsLoading;
    }

    public void setDetailsLoading(boolean detailsLoading) {
        this.detailsLoading = detailsLoading;
    }
}
