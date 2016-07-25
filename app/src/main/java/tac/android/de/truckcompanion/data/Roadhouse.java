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

    // Members
    private double rating;
    private PlaceLink placeLink;
    private Place place;
    private Date ETA;
    private long durationFromStart;
    private boolean detailsLoading = false;
    private int distanceFromStart;
    private double gasPrice;

    /**
     * Instantiates a new Roadhouse.
     *
     * @param placeLink the place link
     */
    public Roadhouse(PlaceLink placeLink) {
        this.placeLink = placeLink;
    }


    /**
     * Gets rating.
     *
     * @return the rating
     */
    public double getRating() {
        return rating;
    }

    /**
     * Gets the estimated time of arrival
     *
     * @return the eta
     */
    public Date getETA() {
        return ETA;
    }

    /**
     * Sets the estimated time of arrival.
     *
     * @param ETA the eta
     */
    public void setETA(Date ETA) {
        this.ETA = ETA;
    }

    /**
     * Gets place link.
     *
     * @return the place link
     */
    public PlaceLink getPlaceLink() {
        return placeLink;
    }

    /**
     * Sets place link.
     *
     * @param placeLink the place link
     */
    public void setPlaceLink(PlaceLink placeLink) {
        this.placeLink = placeLink;
    }

    /**
     * Gets duration from start.
     *
     * @return the duration from start
     */
    public long getDurationFromStart() {
        return durationFromStart;
    }

    /**
     * Sets duration from start.
     *
     * @param durationFromStart the duration from start
     */
    public void setDurationFromStart(long durationFromStart) {
        this.durationFromStart = durationFromStart;
    }


    /**
     * On details loaded.
     */
    public void onDetailsLoaded() {
        detailsLoading = false;
    }


    /**
     * Sets details loading.
     *
     * @param detailsLoading the details loading
     */
    public void setDetailsLoading(boolean detailsLoading) {
        this.detailsLoading = detailsLoading;
    }

    /**
     * Sets distance from start.
     *
     * @param distanceFromStart the distance from start
     */
    public void setDistanceFromStart(int distanceFromStart) {
        this.distanceFromStart = distanceFromStart;
    }

    /**
     * Gets distance from start.
     *
     * @return the distance from start
     */
    public int getDistanceFromStart() {
        return distanceFromStart;
    }

    /**
     * Sets gas price.
     *
     * @param gasPrice the gas price
     */
    public void setGasPrice(double gasPrice) {
        this.gasPrice = gasPrice;
    }

    /**
     * Gets gas price.
     *
     * @return the gas price
     */
    public double getGasPrice() {
        return gasPrice;
    }

    /**
     * Sets rating.
     *
     * @param rating the rating
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * Sets place.
     *
     * @param place the place
     */
    public void setPlace(Place place) {
        this.place = place;
    }

    /**
     * Gets place.
     *
     * @return the place
     */
    public Place getPlace() {
        return place;
    }
}
