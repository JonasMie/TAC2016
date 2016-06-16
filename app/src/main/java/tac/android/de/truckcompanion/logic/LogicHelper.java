package tac.android.de.truckcompanion.logic;

import org.json.JSONObject;

import java.util.Vector;

import tac.android.de.truckcompanion.data.TruckStateEventListener;
import tac.android.de.truckcompanion.simulator.SimulationEventListener;


//in minutes



public class LogicHelper implements SimulationEventListener, TruckStateEventListener {

    public static final int MAX_WEEK_DRIVE_MINUTES = 3360;        // 56 hours
    public static final int MAX_DAY_DRIVE_MINUTES = 540;           // 9 hours
    public static final int MAX_DAY_DRIVE_MINUTES_EXCEPTION = 600 ;  // 10 hours
    public static final int MAX_SESSION_DRIVE_MINUTES = 270;             // 4.5 hours
    public static final int MAX_SESSION_WORK_MINUTES_COMBINED = 360;      // 6 hours
    public static final int MAX_DAY_WORK_MINUTES = 480;                   // 8 hours

    public static final int MIN_WEEK_REST_MINUTES = 2700 ;            // 45 hours
    public static final int MIN_WEEK_REST_MINUTES_EXCEPTION = 1440 ;    // 24 hours
    public static final int MIN_SESSION_REST_MINUTES = 45 ;                    // 0.75 hours
    public static final int MIN_SESSION_REST_MINUTES_SPLIT_P1 = 15 ;                    // 0.25 hours
    public static final int MIN_SESSION_REST_MINUTES_SPLIT_P2 = 30 ;                    // 0.5 hours
    public static final int MIN_DAY_REST_MINUTES = 660 ;                    // 11 hours
    public static final int MIN_DAY_REST_MINUTES_SPLIT_P1 = 180 ;           //3 hours
    public static final int MIN_DAY_REST_MINUTES_SPLIT_P2 = 540 ;           //9 hours
    public static final int MIN_DAY_REST_MINUTES_EXCEPTION = 540 ;             // 9 hours

    public static final int DAY_TIME = 1440;                //24 hours

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
        private boolean driveMinutesException;

        private float dayTime;
        private DriverActivity lastActivity;
        private DriverActivity currActivity;

        public Day() {
            maxSessionDriveMinutes = MAX_SESSION_DRIVE_MINUTES;
            minSessionRestTime = MIN_SESSION_REST_MINUTES;
            splitSessionRestTime = false;
            splitSessionP1Done = false;

            maxDriveMinutes = MAX_DAY_DRIVE_MINUTES;
            maxWorkMinutes = MAX_DAY_WORK_MINUTES;
            minRestMinutes = MIN_DAY_REST_MINUTES;
            splitRestTime = false;
            driveMinutesException = false;

            dayTime = DAY_TIME;
            lastActivity = DriverActivity.DRIVING;
            currActivity = DriverActivity.LAST;

        }

        public void Clear() {
            maxSessionDriveMinutes = MAX_SESSION_DRIVE_MINUTES;
            minSessionRestTime = MIN_SESSION_REST_MINUTES;
            splitSessionRestTime = false;
            splitSessionP1Done = false;

            maxDriveMinutes = MAX_DAY_DRIVE_MINUTES;
            maxWorkMinutes = MAX_DAY_WORK_MINUTES;
            minRestMinutes = MIN_DAY_REST_MINUTES;
            splitRestTime = false;
            driveMinutesException = false;

            dayTime = DAY_TIME;
            lastActivity = DriverActivity.DRIVING;
            currActivity = DriverActivity.LAST;

        }

        public void SetActivity(DriverActivity newActivity) {
            currActivity = newActivity;
        }

        public boolean IsDayOver() {
            return dayTime <= 0;
        }

        public void UpdateActivity(float elapsedTime) {

            if (lastActivity != currActivity) {

                if (currActivity == DriverActivity.DRIVING) {

                    // war die letzte pause zu kurz?
                    if (minSessionRestTime >= 0) {
                        // notify view
                        //"Du hast zu wenig pause zwischen den Arbeitsschichten gemacht!"
                    }
                    float daySplitThreshold = MIN_DAY_REST_MINUTES_SPLIT_P2;
                    daySplitThreshold -= 60;

                    //war die letzte pause ein teil der tagespause?
                    if (minRestMinutes <= daySplitThreshold && !splitRestTime && minRestMinutes > 0) {
                        float dayExceptionThreshold = MIN_DAY_REST_MINUTES_SPLIT_P1;
                        dayExceptionThreshold -= 60;
                        //war die letzte pause die verkürzte tagespause?
                        if (minRestMinutes <= dayExceptionThreshold) {
                            minRestMinutes = 0;
                            //Pause wurde verkürzt
                        } else {
                            splitRestTime = true;
                            minRestMinutes += 60;
                            //Pause wird aufgeteilt
                        }
                    }

                    float tempRestMin = MIN_SESSION_REST_MINUTES_SPLIT_P2;

                    minSessionRestTime = splitSessionRestTime ? tempRestMin : MIN_SESSION_REST_MINUTES;
                    splitSessionRestTime = false;
                    lastActivity = currActivity;

                    UpdateDrivingTime(elapsedTime);
                }


                //set new session drive minutes
                if (currActivity == DriverActivity.RESTING) {
                    //session rest timt split start
                    if (maxSessionDriveMinutes > 10 && !splitSessionRestTime && minSessionRestTime > 30) {
                        splitSessionRestTime = true;
                        minSessionRestTime = MIN_SESSION_REST_MINUTES_SPLIT_P1;
                    }

                    maxSessionDriveMinutes = splitSessionRestTime ? maxSessionDriveMinutes : MAX_SESSION_DRIVE_MINUTES;
                    lastActivity = currActivity;

                    UpdateRestingTime(elapsedTime);
                }

            } else {
                switch (currActivity) {
                    case WORKING:
                        UpdateWorkingTime(elapsedTime);
                        break;
                    case DRIVING:
                        UpdateDrivingTime(elapsedTime);
                        break;
                    case RESTING:
                        UpdateRestingTime(elapsedTime);
                        break;


                }

            }

            UpdateDayTime(elapsedTime);

        }

        void UpdateDayTime(float elapsedTime) {
            dayTime -= elapsedTime;

            if (dayTime < minRestMinutes && dayTime > minRestMinutes - 5) {
                //Du musst deine Tagespause machen !
            }
        }

        void UpdateDrivingTime(float elapsedTime) {
            maxSessionDriveMinutes -= elapsedTime;
            maxDriveMinutes -= elapsedTime;

            if (maxSessionDriveMinutes < 0 && maxSessionDriveMinutes > -2) {

                // send notification to viewmodel and display warning ?
                // "Du musst Pause machen"

            }

            if (maxDriveMinutes < 0) {
                driveMinutesException = true;

                float driveExceptionCurr = Math.abs(maxDriveMinutes) + MAX_DAY_DRIVE_MINUTES;
                float driveExceptionThreshold = MAX_DAY_DRIVE_MINUTES_EXCEPTION;

                if (driveExceptionCurr > driveExceptionThreshold && driveExceptionCurr < driveExceptionThreshold + 5) {
                    //Deine erlaubte Arbeitszeit für heute ist erreicht !
                }
            }
        }

        public void UpdateWorkingTime(float elapsedTime) {
            maxWorkMinutes -= elapsedTime;
        }

        void UpdateRestingTime(float elapsedTime) {

            if ((minSessionRestTime <= 0 && minSessionRestTime > -2) || (minRestMinutes <= 0 && minRestMinutes > -2)) {
                // send notification to viewmodel
                // "Du kannst weiterfahren"

            }
            minSessionRestTime -= elapsedTime;
            minRestMinutes -= elapsedTime;

        }
    }



    public  enum DriverActivity
    {
        WORKING,DRIVING,RESTING,LAST;

    }

    private Day currDay;
    public Vector<Day> workingDays;

    public LogicHelper()
    {

        workingDays = new Vector<Day>();
        currDay = new Day();
    }


    void UpdateElapsedTime(float elapsedTime)
    {
        currDay.UpdateActivity(elapsedTime);

        if(currDay.IsDayOver())
        {
            workingDays.add(currDay);
            currDay.Clear();
        }
    }

    void UpdateElapsedTime()
    {
        currDay.UpdateActivity(1);

        if (currDay.IsDayOver())
        {
            workingDays.add(currDay);
            currDay.Clear();
        }
    }
    void SetActivity(DriverActivity newActivity)
    {
        currDay.SetActivity(newActivity);


    }

    @Override
    public void onSimulationEvent(JSONObject event)
    {
        UpdateElapsedTime();
    }


    @Override
   public void onTruckStationaryStateChange(int state)
    {
        SetActivity(DriverActivity.values()[state]);
    }
}
