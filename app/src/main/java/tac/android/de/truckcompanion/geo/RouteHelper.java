package tac.android.de.truckcompanion.geo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import com.skobbler.ngx.*;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.Helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by Jonas Miederer.
 * Date: 28.06.2016
 * Time: 17:18
 * Project: MAD
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class RouteHelper implements SKPrepareMapTextureListener, SKMapUpdateListener {

    public static final String TAG = "TAC";

    private static RouteHelper instance;
    private Context context;
    private AsyncResponse<RouteHelper> callback;
    // Skobbler-related stuff
    private String mapResourcesDirPath;
    private String mapCreatorFilePath;

    private static final String SKMAPS_DIR = "/SKMaps/";
    private static final String MARK_PNG = ".Common/logo.png";
    private static final int MARK_PNG_SIZE = 32;

    private RouteHelper(Context context, AsyncResponse<RouteHelper> callback) {
        this.context = context;
        this.callback = callback;
        init();
    }

    public static void getInstance(AsyncResponse<RouteHelper> callback) {
        if (instance == null) {
            new RouteHelper(MainActivity.context, callback);
        } else {
            callback.processFinish(instance);
        }

    }


    private void init() {
        File externalDir = context.getExternalFilesDir(null);

        // determine path where map resources should be copied on the device
        mapResourcesDirPath = (externalDir != null ? externalDir : context.getFilesDir()) + SKMAPS_DIR;

        if (!new File(mapResourcesDirPath).exists()) {
            // copy some other resource needed
            new SKPrepareMapTextureThread(context, mapResourcesDirPath, "SKMaps.zip", this).start();
            copyOtherResources();
            prepareMapCreatorFile();
        } else {
            // map resources have already been copied
            prepareMapCreatorFile();
            initializeLibrary();
            SKVersioningManager.getInstance().setMapUpdateListener(this);
            instance = this;
            callback.processFinish(instance);
        }
    }

    /**
     * Copies the map creator file from assets to a storage.
     */
    private void prepareMapCreatorFile() {

        final Thread prepareGPXFileThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    final String mapCreatorFolderPath = mapResourcesDirPath + "MapCreator";
                    final File mapCreatorFolder = new File(mapCreatorFolderPath);
                    // create the folder where you want to copy the json file
                    if (!mapCreatorFolder.exists()) {
                        boolean mkdirsOk = mapCreatorFolder.mkdirs();
                        if (!mkdirsOk)
                            Log.d(TAG, "Error creating mapCreator folder");
                    }
                    mapCreatorFilePath = mapCreatorFolderPath + "/mapcreatorFile.json";
                    Helper.copyAsset(context.getAssets(), "MapCreator", mapCreatorFolderPath, "mapcreatorFile.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        prepareGPXFileThread.start();
    }

    /**
     * Copy some additional resources from assets
     */
    private void copyOtherResources() {
        new Thread() {
            public void run() {
                try {
                    String tracksPath = mapResourcesDirPath + "GPXTracks";
                    File tracksDir = new File(tracksPath);
                    if (!tracksDir.exists()) {
                        boolean mkdirsOk = tracksDir.mkdirs();
                        if (!mkdirsOk)
                            Log.d(TAG, "Error making dirs");
                    }
                    Helper.copyAssetsToFolder(context.getAssets(), "GPXTracks", mapResourcesDirPath + "GPXTracks");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * This method restores the image used for marking route from app resources when it is
     * unavailable due to former extraction of SKMap resources by an app with the same package name
     * <p/>
     * TODO
     */
    public void copyMarkImage() {
        File markImage = new File(mapResourcesDirPath + MARK_PNG);

        if (!markImage.exists()) {
            Bitmap bm = null; //BitmapFactory.decodeResource(context.getResources(), R.drawable.test);
            bm = Bitmap.createScaledBitmap(bm, MARK_PNG_SIZE, MARK_PNG_SIZE, false);
            try {
                FileOutputStream outStream = new FileOutputStream(markImage);
                bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void launchRouteCalculation(Route route, LatLng start, LatLng end) { //, ArrayList<LatLng> via
        // get a route settings object and populate it with the desired properties
        SKRouteSettings routeSettings = new SKRouteSettings();
        // set start and destination points
        // wtf, why do they use Long,Lat and not Lat,Long?


        routeSettings.setStartCoordinate(new SKCoordinate(start.longitude, start.latitude));
        routeSettings.setDestinationCoordinate(new SKCoordinate(end.longitude, end.latitude));


        // set the number of routes to be calculated
        routeSettings.setNoOfRoutes(1);
        // set the route mode
        routeSettings.setRouteMode(SKRouteSettings.SKRouteMode.CAR_FASTEST);
        // set whether the route should be shown on the map after it's computed
        routeSettings.setRouteExposed(false);

        routeSettings.setRequestAdvices(true);
        routeSettings.setExtendedPointsReturned(true);

        // set the route listener to be notified of route calculation
        // events
        SKRouteManager.getInstance().setRouteListener(route);
        // pass the route to the calculation routine
        SKRouteManager.getInstance().calculateRoute(routeSettings);
    }


    /**
     * Initializes the SKMaps framework
     */
    private boolean initializeLibrary() {
        SKLogging.enableLogs(true);

        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();

        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(mapResourcesDirPath,
                new SKMapViewStyle(mapResourcesDirPath + "daystyle/", "daystyle.json"));

        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setAdvisorConfigPath(mapResourcesDirPath+"/Advisor");
        advisorSettings.setResourcePath(mapResourcesDirPath+"Advisor/Languages");
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_DE);
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);

        try {
            SKMaps.getInstance().initializeSKMaps(context, initMapSettings);
            return true;
        } catch (SKDeveloperKeyException exception){
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public void onNewVersionDetected(int i) {

    }

    @Override
    public void onMapVersionSet(int i) {

    }

    @Override
    public void onVersionFileDownloadTimeout() {

    }

    @Override
    public void onNoNewVersionDetected() {

    }

    @Override
    public void onMapTexturesPrepared(boolean b) {
        initializeLibrary();
        callback.processFinish(instance);
    }
}
