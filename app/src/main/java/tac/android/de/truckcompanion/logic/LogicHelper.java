package tac.android.de.truckcompanion.logic;

import org.json.JSONObject;

import java.util.Vector;

import tac.android.de.truckcompanion.data.TruckStateEventListener;
import tac.android.de.truckcompanion.simulator.SimulationEventListener;


//in minutes



public class LogicHelper {

    public static final int MAX_WEEK_DRIVE_MINUTES = 3360;        // 56 hours
    public static final int MAX_DAY_DRIVE_MINUTES = 540;           // 9 hours
    public static final int MAX_DAY_DRIVE_MINUTES_EXCEPTION = 600 ;  // 10 hours
    public static final int MAX_SESSION_DRIVE_MINUTES = 270;             // 4.5 hours
    public static final int MAX_SESSION_WORK_MINUTES_COMBINED = 360;      // 6 hours
    public static final int MAX_DAY_WORK_MINUTES = 480;// 8 hours

    public static final int MAX_DOUBLE_WEEK_DRIVE_MINUTES = 5400;                //90 hours

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


    private final int updateCycle = 60;
    private int currUpdateCycle = 0;

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

        /**
         * Clears day values.
         */
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

        /**
         * Sets day values.
         *
         * @param driveTime drive time of the day
         * @param restTime rest time of the day
         * @param driveMinutesEx set true if the drive time is longer than 9 hours
         */
        public void SetDayValues(float driveTime, float restTime, boolean driveMinutesEx)
        {
            maxDriveMinutes = MAX_DAY_DRIVE_MINUTES - driveTime;
            minRestMinutes = MIN_DAY_REST_MINUTES - restTime;
            driveMinutesException = driveMinutesEx;
        }

        /**
         * returns the current drive time of the day
         * @return the drive time
         */
        public float GetDriveTime()
        {
            return driveMinutesException ? MAX_DAY_DRIVE_MINUTES_EXCEPTION - maxDriveMinutes : MAX_DAY_DRIVE_MINUTES - maxDriveMinutes;
        }
        /**
         * returns the current rest time of the day
         * @return the rest time
         */
        public float GetRestTime()
        {
            return MIN_DAY_REST_MINUTES - minRestMinutes;
        }

        /**
         * checks if there is drive time exception for that day
         * @return returns true if drive time is longer than 9 hours
         */
        public boolean HasDriveTimeException()
        {return driveMinutesException;}

        /**
         * sets a new driver activity
         * @@param newActivity  the new activity
         */
        public void SetActivity(DriverActivity newActivity) {
            currActivity = newActivity;
        }
        /**
         * checks if the current day is over
         * @return true if day is over
         */
        public boolean IsDayOver() {
            return dayTime <= 0;
        }

        /**
         * updates all times
         * @param elapsedTime the elpased time since the last call
         */
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


        /**
         * updates the day time
         * @param elapsedTime the elpased time since the last call
         */
        void UpdateDayTime(float elapsedTime) {
            dayTime -= elapsedTime;

            if (dayTime < minRestMinutes && dayTime > minRestMinutes - 5) {
                //Du musst deine Tagespause machen !
            }
        }

        /**
         * updates the drive time
         * @param elapsedTime the elpased time since the last call
         */
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

        /**
         * updates the working time
         * @param elapsedTime the elpased time since the last call
         */
        void UpdateWorkingTime(float elapsedTime) {
            maxWorkMinutes -= elapsedTime;
        }

        /**
         * updates the resting time
         * @param elapsedTime the elpased time since the last call
         */
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
        currDay.SetDayValues(6*60,MIN_DAY_REST_MINUTES,false);
        workingDays.add(currDay);
        currDay = new Day();
        currDay.SetDayValues(6*60,MIN_DAY_REST_MINUTES,false);
        workingDays.add(currDay);
        currDay = new Day();
        currDay.SetDayValues(6*60,MIN_DAY_REST_MINUTES,false);
        workingDays.add(currDay);
        currDay = new Day();
        currDay.SetDayValues(8*60,MIN_DAY_REST_MINUTES,false);
        workingDays.add(currDay);
        currDay = new Day();
        currDay.SetDayValues(8*60,MIN_DAY_REST_MINUTES,false);
        workingDays.add(currDay);
        currDay = new Day();
        currDay.SetDayValues(8*60,MIN_DAY_REST_MINUTES,false);
        workingDays.add(currDay);
        currDay = new Day();

    }

    /**
     * returns the current day object
     * @return the current day object
     */
    public Day GetCurrentDay()
    {return currDay;}

    /**
     * calculates sum of the drive time of each stored day
     * @return drive time sum
     */
    public float GetDriveTimeSum()
    { float driveTime = 0;
        for(Day day : workingDays)
        {
            driveTime+= day.GetDriveTime();
        }
        return driveTime;}

    /**
     * calculates sum of the rest time of each stored day
     * @return rest time sum
     */
    public float GetRestTimeSum()
    { float driveTime = 0;
        for(Day day : workingDays)
        {
            driveTime+= day.GetRestTime();
        }
        return driveTime;}

    /**
     * calculates the number of drive time exceptions
     * @return number of drive time exceptions
     */
    public int GetDriveTimeExNum()
    {
        int exNum = 0;
        for(Day day : workingDays) {
            if (day.HasDriveTimeException()) {
                ++exNum;
            }
        }
        return exNum;
    }

    /**
     * calls update function on current day objecz
     * @param elapsedTime  time since the last call
     */
    void UpdateElapsedTime(float elapsedTime)
    {
        currDay.UpdateActivity(elapsedTime);

        if(currDay.IsDayOver())
        {
            workingDays.add(currDay);
            currDay.Clear();
        }
    }

    /**
     * calls update function on current day objecz, fixed time step of one minute
     */
    void UpdateElapsedTime()
    {
        currDay.UpdateActivity(1);

        if (currDay.IsDayOver())
        {
            workingDays.add(currDay);
            currDay.Clear();
        }
    }

    /**
     * sets new activity for current day
     * @param newActivity the new activity
     */
    void SetActivity(DriverActivity newActivity)
    {
        currDay.SetActivity(newActivity);
    }


    public void onSimulationEvent()
    {
        if(currUpdateCycle >= updateCycle)
        {
            UpdateElapsedTime();
            currUpdateCycle = 0;
        }
        else
        {
             ++currUpdateCycle;
        }
    }



   public void onTruckStationaryStateChange(int state)
    {
        SetActivity(DriverActivity.values()[state]);
    }
}
