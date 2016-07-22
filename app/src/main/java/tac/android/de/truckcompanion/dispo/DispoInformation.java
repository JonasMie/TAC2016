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
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 17:44
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public abstract class DispoInformation {
    @Override
    public boolean equals(Object obj) {
        DispoInformation o = (DispoInformation) obj;
        return o.getCoordinate().latitude == this.getCoordinate().latitude && o.getCoordinate().longitude == this.getCoordinate().longitude;
    }

    abstract public LatLng getCoordinate();

    private static final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", Locale.ENGLISH);

    public static class StartPoint {
        private Date date;

        private LatLng coordinate;

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public LatLng getCoordinate() {
            return coordinate;
        }

        public StartPoint(JSONObject start) throws JSONException, ParseException {
            this.coordinate = new LatLng(start.getDouble("lat"), start.getDouble("lng"));
            this.date = df.parse(start.getString("date"));
        }
    }

    public static class DestinationPoint {
        private int time;
        private LatLng coordinate;

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public LatLng getCoordinate() {
            return coordinate;
        }

        public DestinationPoint(JSONObject dest) throws JSONException {
            this.coordinate = new LatLng(dest.getDouble("lat"), dest.getDouble("lng"));
            try {
                this.time = dest.getInt("time");
            } catch (JSONException e) {
                // no time means driver finished journey
            }
        }

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
