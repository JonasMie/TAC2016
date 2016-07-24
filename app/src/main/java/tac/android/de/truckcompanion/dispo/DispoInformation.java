package tac.android.de.truckcompanion.dispo;

import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.geo.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The Dispo Information Parser
 * <p/>
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 17:44
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public abstract class DispoInformation {

    private static final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", Locale.ENGLISH);

    /**
     * Gets coordinate.
     *
     * @return the coordinate
     */
    abstract public LatLng getCoordinate();

    @Override
    public boolean equals(Object obj) {
        DispoInformation o = (DispoInformation) obj;
        return o.getCoordinate().latitude == this.getCoordinate().latitude && o.getCoordinate().longitude == this.getCoordinate().longitude;
    }

    /**
     * The type Start point.
     */
    public static class StartPoint {
        private Date date;
        private LatLng coordinate;

        /**
         * Gets date.
         *
         * @return the date
         */
        public Date getDate() {
            return date;
        }

        /**
         * Sets date.
         *
         * @param date the date
         */
        public void setDate(Date date) {
            this.date = date;
        }

        /**
         * Gets coordinate.
         *
         * @return the coordinate
         */
        public LatLng getCoordinate() {
            return coordinate;
        }

        /**
         * Instantiates a new Start point.
         *
         * @param start the start
         * @throws JSONException  the json exception
         * @throws ParseException the parse exception
         */
        public StartPoint(JSONObject start) throws JSONException, ParseException {
            this.coordinate = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
            this.date = df.parse(start.getString("date"));
        }
    }

    /**
     * The type Destination point.
     */
    public static class DestinationPoint {
        private int time;
        private LatLng coordinate;

        /**
         * Gets time.
         *
         * @return the time
         */
        public int getTime() {
            return time;
        }

        /**
         * Sets time.
         *
         * @param time the time
         */
        public void setTime(int time) {
            this.time = time;
        }

        /**
         * Gets coordinate.
         *
         * @return the coordinate
         */
        public LatLng getCoordinate() {
            return coordinate;
        }

        /**
         * Instantiates a new Destination point.
         *
         * @param dest the dest
         * @throws JSONException the json exception
         */
        public DestinationPoint(JSONObject dest) throws JSONException {
            this.coordinate = new LatLng(dest.getDouble("lat"), dest.getDouble("lng"));
            try {
                this.time = dest.getInt("time");
            } catch (JSONException e) {
                // no time means driver finished journey
            }
        }

        /**
         * Instantiates a new Destination point.
         *
         * @param dest the dest
         * @param time the time
         */
        public DestinationPoint(LatLng dest, int time) {
            this.coordinate = dest;
            this.time = time;

        }

        @Override
        public boolean equals(Object obj) {
            DestinationPoint o = (DestinationPoint) obj;
            return obj != null && o.getCoordinate().latitude == this.getCoordinate().latitude && o.getCoordinate().longitude == this.getCoordinate().longitude;
        }
    }

}
