package tac.android.de.truckcompanion.logic;

import android.app.Activity;
import org.json.JSONObject;
import tac.android.de.truckcompanion.data.TruckStateEventListener;
import tac.android.de.truckcompanion.simulator.SimulationEventListener;

import java.util.Vector;

/**
 * Created by Michael on 19.05.2016.
 * Oh my gawd.
 */
//in minutes


public class LogicHelper implements TruckStateEventListener, SimulationEventListener {

    public static final int MAX_WEEK_DRIVE_MINUTES = 3360;        // 56 hours
    public static final int MAX_DAY_DRIVE_MINUTES = 540;           // 9 hours
    public static final int MAX_DAY_DRIVE_MINUTES_EXCEPTION = 600;  // 10 hours
    public static final int MAX_SESSION_DRIVE_MINUTES = 270;             // 4.5 hours
    public static final int MAX_SESSION_WORK_MINUTES_COMBINED = 360;      // 6 hours
    public static final int MAX_DAY_WORK_MINUTES = 480;                   // 8 hours

    public static final int MIN_WEEK_REST_MINUTES = 2700;            // 45 hours
    public static final int MIN_WEEK_REST_MINUTES_EXCEPTION = 1440;    // 24 hours
    public static final int MIN_SESSION_REST_MINUTES = 45;                    // 0.75 hours
    public static final int MIN_SESSION_REST_MINUTES_SPLIT_P1 = 15;                    // 0.25 hours
    public static final int MIN_SESSION_REST_MINUTES_SPLIT_P2 = 30;                    // 0.5 hours
    public static final int MIN_DAY_REST_MINUTES = 660;                    // 11 hours

    @Override
    public void onTruckStationaryStateChange(int state) {

    }

    @Override
    public void onSimulationEvent(JSONObject event) {

    }


    //not a 24h day. values are set to MIN and are decreased. example: if drivingMinutes is zero, driver has no driving time left for this day (only twice a week 10hours)
    public class Day {
        //session related
        private float maxSessionDriveMinutes;
        private float minSessionRestTime;
        private boolean splitSessionRestTime;
        private boolean splitSessionP1Done;
        //day related
        private float maxDriveMinutes;
        private float maxWorkMinutes;
        private float minRestMinutes;
        private boolean splitRestTime;
        private DriverActivity lastActivity;

        public Day() {
            maxSessionDriveMinutes = MAX_SESSION_DRIVE_MINUTES;
            minSessionRestTime = MIN_SESSION_REST_MINUTES;
            splitSessionRestTime = false;
            splitSessionP1Done = false;

            maxDriveMinutes = MAX_DAY_DRIVE_MINUTES;
            maxWorkMinutes = MAX_DAY_WORK_MINUTES;
            minRestMinutes = MIN_DAY_REST_MINUTES;
            splitRestTime = false;

            lastActivity = DriverActivity.RESTING;
        }

        public void UpdateActivity(float elapsedTime, DriverActivity currActivity) {

            if (lastActivity != currActivity) {

                if (currActivity == DriverActivity.DRIVING) {

                    if (minSessionRestTime >= 0) {
                        // notify view
                        //"Du hast zu wenig pause zwischen den Arbeitsschichten gemacht!"
                    }

                    minSessionRestTime = splitSessionRestTime ? MIN_SESSION_REST_MINUTES_SPLIT_P2 : MIN_SESSION_REST_MINUTES;
                    splitSessionRestTime = false;
                    lastActivity = currActivity;

                    UpdateDrivingTime(elapsedTime);
                }


                //set new session drive minutes
                if (currActivity == DriverActivity.RESTING) {
                    //session rest timt split start
                    if (maxSessionDriveMinutes > 10 && !splitSessionRestTime) {
                        splitSessionRestTime = true;
                        minSessionRestTime = MIN_SESSION_REST_MINUTES_SPLIT_P1;
                    }

                    maxSessionDriveMinutes = splitSessionRestTime ? maxSessionDriveMinutes : MAX_SESSION_DRIVE_MINUTES;
                    lastActivity = currActivity;

                    UpdateSessionRestingTime(elapsedTime);
                }

            } else {
                switch (lastActivity) {
                    case WORKING:
                        UpdateWorkingTime(elapsedTime);
                        break;
                    case DRIVING:
                        UpdateDrivingTime(elapsedTime);
                        break;
                    case RESTING:
                        UpdateSessionRestingTime(elapsedTime);
                        break;


                }

            }
        }


        public void UpdateDrivingTime(float elapsedTime) {
            maxSessionDriveMinutes -= elapsedTime;

            if (maxSessionDriveMinutes < 0) {

                // send notification to viewmodel and display warning ?
                // "Du musst Pause machen"

            }


        }

        public void UpdateWorkingTime(float elapsedTime) {
            maxWorkMinutes -= elapsedTime;
        }

        public void UpdateSessionRestingTime(float elapsedTime) {

            if (minSessionRestTime <= 0) {
                // send notification to viewmodel
                // "Du kannst weiterfahren"

            }
            minSessionRestTime -= elapsedTime;

        }

        public void UpdateDayRestingTime(float elapsedTime) {
            minRestMinutes -= elapsedTime;
        }

    }


    public enum DriverActivity {
        WORKING, DRIVING, RESTING, LAST;

    }

    private Day currDay;
    public Vector<Day> workingDays;

    public LogicHelper() {

        workingDays = new Vector<Day>();
        currDay = new Day();
    }


    //elapsed time since this functions was called the last time
    public void UpdateElapsedTime(float elapsedTime, DriverActivity currActivity) {
        currDay.UpdateActivity(elapsedTime, currActivity);

    }


}
