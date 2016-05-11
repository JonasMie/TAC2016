package tac.android.de.truckcompanion.dispo;

import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.geo.Point;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Jonas Miederer.
 * Date: 09.05.16
 * Time: 17:44
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public abstract class DispoInformation extends Point {

    private static final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss yyyy", Locale.ENGLISH);

    public static class StartPoint extends Point {
        protected Date date;

        public StartPoint(JSONObject start) throws JSONException, ParseException {
            this.lat = start.getDouble("lat");
            this.lng = start.getDouble("lng");
            this.date = df.parse(start.getString("date"));
        }
    }

    public static class DestinationPoint extends Point {
        protected int time;

        public DestinationPoint(JSONObject dest) throws JSONException {
            this.lat = dest.getDouble("lat");
            this.lng = dest.getDouble("lng");
            try {
                this.time = dest.getInt("time");
            } catch (JSONException e) {
                // no time means driver finished journey
            }
        }
    }

}
