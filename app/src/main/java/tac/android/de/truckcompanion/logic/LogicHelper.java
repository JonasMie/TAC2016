package tac.android.de.truckcompanion.logic;

import java.util.Vector;

/**
 * Created by Michael on 19.05.2016.
 Oh my gawd.
 */
//in minutes



public class LogicHelper {

    public static final int MAX_WEEK_DRIVE_MINUTES = 3360;        // 56 hours
    public static final int MAX_DAY_DRIVE_MINUTES = 540;           // 9 hours
    public static final int MAX_DAY_DRIVE_MINUTES_EXCEPTION = 600 ;  // 10 hours
    public static final int MAX_SESSION_DRIVE_MINUTES = 270;             // 4.5 hours
    public static final int MAX_SESSION_WORK_MINUTES_COMBINED = 360;      // 6 hours
    public static final int MAX_DAY_WORK_MINUTES = 480;                   // 8 hours

    public static final int MIN_WEEK_REST_MINUTES = 2700 ;            // 45 hours
    public static final int MIN_WEEK_REST_MINUTES_EXCEPTION = 1440 ;    // 24 hours
    public static final int MIN_SESSION_REST_MINUTES = 45 ;                    // 0.75 hours
    public static final int MIN_DAY_REST_MINUTES = 660 ;                    // 11 hours


    //not a 24h day. values are set to MIN and are decreased. example: if drivingMinutes is zero, driver has no driving time left for this day (only twice a week 10hours)
    public class Day
    {
        //session related
     private  float maxSessionDriveMinutes;
     private  float minSessionRestTime;
     private  boolean splitSessionRestTime;

       //day related
     private  float maxDriveMinutes;
     private  float maxWorkMinutes;
     private  float minRestMinutes;
     private  boolean splitRestTime;

        public Day()
        {
            maxSessionDriveMinutes = MAX_SESSION_DRIVE_MINUTES;
            minSessionRestTime = MIN_SESSION_REST_MINUTES;
            splitSessionRestTime = false;

            maxDriveMinutes = MAX_DAY_DRIVE_MINUTES;
            maxWorkMinutes = MAX_DAY_WORK_MINUTES;
            minRestMinutes = MIN_DAY_REST_MINUTES;
            splitRestTime = false;
        }

        public void UpdateDrivingTime(float elapsedTime)
        {
             maxSessionDriveMinutes -= elapsedTime;

            if(maxSessionDriveMinutes <= 0)
            {


            }

        }
        public void UpdateWorkingTime(float elapsedTime)
        {
            maxWorkMinutes -= elapsedTime;
        }

        public void UpdateSessionRestingTime(float elapsedTime)
        {
            minSessionRestTime -= elapsedTime;
        }

        public void UpdateDayRestingTime(float elapsedTime)
        {
            minRestMinutes -=elapsedTime;
        }

    }



    public  enum DriverActivity
    {
        WORKING,DRIVING,RESTING,END;

    }

    private Day currDay;
    public Vector<Day> workingDays;

    public LogicHelper()
    {

        workingDays = new Vector<Day>();
        currDay = new Day();
    }


    //elapsed time since this functions was called the last time
    public void UpdateElapsedTime(float elapsedTime,DriverActivity currActivity)
    {
        switch (currActivity)
        {
            case WORKING:
                currDay.UpdateWorkingTime((elapsedTime));
                break;
            case DRIVING:
                currDay.UpdateDrivingTime(elapsedTime);
                break;
            case RESTING:
                currDay.UpdateDayRestingTime(elapsedTime);
                break;

        }




    }

    
}
