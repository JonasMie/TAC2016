package tac.android.de.truckcompanion.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.NavigationManager.NewInstructionEventListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.search.PlaceLink;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.Roadhouse;
import tac.android.de.truckcompanion.data.TruckStateEventListener;
import tac.android.de.truckcompanion.geo.NavigationWrapper;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 17:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MapFragment extends Fragment implements MapGesture.OnGestureListener {

    // View references
    private Map map;
    private com.here.android.mpa.mapping.MapFragment mapFragment;

    private TextView rh_title;
    private TextView rh_address;
    private TextView rh_eta;
    private TextView rh_distance;
    private TextView rh_breaktime;
    private RatingBar rh_rating;
    private FloatingActionButton rh_choose;
    private ImageView rh_gas_image;
    private TextView rh_gas_price;
    private ImageView map_relocate;

    private MainActivity activity;

    // Listeners
    private List<TruckStateEventListener> listeners = new ArrayList<>();
    private OnRoadhouseSelectedListener listener;
    private NewInstructionEventListener newInstructionEventListener;
    private NavigationManager.NavigationManagerEventListener navigationManagerEventListener;
    private NavigationManager.PositionListener positionListener;

    // Map marker stuff
    private MapMarker currentPositionMarker;
    private HashMap<MapMarker, EntryRoadhouseStruct> markerWheelEntryMap;
    private Image icon_main;
    private Image icon_alt;
    private Image icon_pos;
    private Image icon_start;
    private Image icon_finish;
    private PointF anchorPoint;
    private PointF anchorPointPos;

    //Misc
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    private Timer timer;
    private TimerTask timerTask;

    // Constants
    private static final String TAG = MapFragment.class.getSimpleName();
    private static final long SIMULATION_RATIO = 15;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Get view references
        rh_title = (TextView) view.findViewById(R.id.map_rec_title);
        rh_address = (TextView) view.findViewById(R.id.map_rec_address);
        rh_eta = (TextView) view.findViewById(R.id.map_rec_info_eta);
        rh_distance = (TextView) view.findViewById(R.id.map_rec_info_distance);
        rh_breaktime = (TextView) view.findViewById(R.id.map_rec_info_breaktime);
        rh_rating = (RatingBar) view.findViewById(R.id.map_rec_rating);
        rh_choose = (FloatingActionButton) view.findViewById(R.id.map_rec_choose);
        rh_gas_image = (ImageView) view.findViewById(R.id.map_rec_img_gas);
        rh_gas_price = (TextView) view.findViewById(R.id.map_rec_price_gas);
        map_relocate = (ImageView) view.findViewById(R.id.map_relocate);

        activity = (MainActivity) getActivity();


        // Set HashMap, timer
        markerWheelEntryMap = new HashMap<>();
        timer = new Timer();

        // get map fragment
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mapFragment = (com.here.android.mpa.mapping.MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        } else {
            mapFragment = (com.here.android.mpa.mapping.MapFragment) getFragmentManager().findFragmentById(R.id.map);
        }

        // set map relocation listener
        map_relocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnRoadhouseSelectedListener) context;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (OnRoadhouseSelectedListener) activity;
    }

    /**
     * Initializes the map for HERE Maps
     *
     * @param callback the callback
     */
    public void init(OnEngineInitListener callback) {
        mapFragment.init(callback);
    }

    /**
     * On startup task ready. Called when the routes and roadhouses were initially calculated and the app is ready.
     */
    public void onStartupTaskReady() {
        prepareImages();

        // set the marker for the current navigation position
        currentPositionMarker = new MapMarker();
        currentPositionMarker.setIcon(icon_pos);
        currentPositionMarker.setAnchorPoint(anchorPointPos);
        map.addMapObject(currentPositionMarker);

        // listen for new events in navigation
        newInstructionEventListener = new NewInstructionEventListener() {
            @Override
            public void onNewInstructionEvent() {
                Maneuver maneuver = NavigationWrapper.getInstance().getNavigationManager().getNextManeuver();
                if (maneuver != null) {
                    Log.d(TAG, maneuver.getAction().name());
                    if (maneuver.getAction() == Maneuver.Action.STOPOVER) {
                        // the driver reached one of his waypoints along the route
                        NavigationWrapper.getInstance().getNavigationManager().pause();

                        // simulate the break with the duration of 60 sec
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NavigationWrapper.getInstance().getNavigationManager().resume();
                                        // notify the activity that driver has finished his break
                                        listener.onBreakFinished();
                                    }
                                });
                            }
                        }, (long) (((MainActivity) getActivity()).getNextBreak().getVal() * 1000) / SIMULATION_RATIO);
                    }
                }
            }
        };

        // listen for changes on running state (started, finished, ...)
        navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {
            @Override
            public void onRunningStateChanged() {
                // if the navigation starts, set the corresponding map view and emit truck moved events to the main fragment and stats fragment every sec
                if (NavigationWrapper.getInstance().getNavigationManager().getRunningState() == NavigationManager.NavigationState.RUNNING) {
                    NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);

                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (NavigationWrapper.getInstance().getNavigationManager().getRunningState() == NavigationManager.NavigationState.RUNNING) {
                                for (TruckStateEventListener listener : listeners) {
                                    listener.onTruckMoved();
                                }
                            }
                        }
                    };
                    timer.schedule(timerTask, 0, 1000);
                }
                for (TruckStateEventListener listener : listeners) {
                    listener.onTruckStationaryStateChange(NavigationWrapper.getInstance().getNavigationManager().getRunningState() != NavigationManager.NavigationState.RUNNING ? 1 : 0);
                }

                super.onRunningStateChanged();
            }

            @Override
            public void onNavigationModeChanged() {
                super.onNavigationModeChanged();
            }

            @Override
            public void onEnded(NavigationManager.NavigationMode navigationMode) {
                // notify the listeners, that navigation finished
                NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);

                for (TruckStateEventListener listener : listeners) {
                    listener.onJourneyFinished();
                }
                timerTask.cancel();
                super.onEnded(navigationMode);
            }

            @Override
            public void onMapUpdateModeChanged(NavigationManager.MapUpdateMode mapUpdateMode) {
                super.onMapUpdateModeChanged(mapUpdateMode);
            }

            @Override
            public void onRouteUpdated(Route route) {
                super.onRouteUpdated(route);
            }

            @Override
            public void onCountryInfo(String s, String s1) {
                super.onCountryInfo(s, s1);
            }
        };

        // listen for new position updates while navigation simulation
        positionListener = new NavigationManager.PositionListener() {
            @Override
            public void onPositionUpdated(GeoPosition geoPosition) {
                currentPositionMarker.setCoordinate(geoPosition.getCoordinate());
                if (NavigationWrapper.getInstance().getNavigationManager().getRunningState() == NavigationManager.NavigationState.RUNNING) {

                }
            }
        };

        NavigationWrapper.getInstance().getNavigationManager().addNewInstructionEventListener(new WeakReference<>(newInstructionEventListener));
        NavigationWrapper.getInstance().getNavigationManager().addNavigationManagerEventListener(new WeakReference<>(navigationManagerEventListener));
        NavigationWrapper.getInstance().getNavigationManager().addPositionListener(new WeakReference<>(positionListener));
    }

    /**
     * Gets map.
     *
     * @return the map
     */
    public Map getMap() {
        return map;
    }

    /**
     * Sets map.
     *
     * @param map the map
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * Gets map fragment.
     *
     * @return the map fragment
     */
    public com.here.android.mpa.mapping.MapFragment getMapFragment() {
        return mapFragment;
    }

    /**
     * Sets map fragment.
     *
     * @param mapFragment the map fragment
     */
    public void setMapFragment(com.here.android.mpa.mapping.MapFragment mapFragment) {
        this.mapFragment = mapFragment;
    }

    /**
     * Sets main roadhouse.
     *
     * @param entry the entry
     */
    public void setMainRoadhouse(WheelEntry entry) {
        setSidebarInfo(entry);
        addMarkerCluster(entry);
    }

    /**
     * Sets sidebar info.
     *
     * @param entry the entry
     */
    public void setSidebarInfo(WheelEntry entry) {
        setSidebarInfo(entry, entry.getPause().getMainRoadhouse());
    }

    /**
     * Sets sidebar info.
     *
     * @param entry     the entry
     * @param roadhouse the roadhouse
     */
    public void setSidebarInfo(final WheelEntry entry, final Roadhouse roadhouse) {
        PlaceLink placeLink = roadhouse.getPlaceLink();

        // center the map at the selected roadhouse
        map.setCenter(placeLink.getPosition(), Map.Animation.BOW);

        rh_title.setText(placeLink.getTitle());
        rh_address.setText(placeLink.getVicinity().replace("<br/>", "\n"));
        rh_rating.setRating((float) roadhouse.getRating());
        if (roadhouse.getETA() != null) {
            rh_eta.setText(dateFormat.format(roadhouse.getETA()));
        } else {
            rh_eta.setText("n/a");
        }
        if (roadhouse.getDistanceFromStart() != 0) {
            rh_distance.setText(String.format(Locale.GERMAN, "%.1f", roadhouse.getDistanceFromStart() / 1000f));
        } else {
            rh_distance.setText("n/a");
        }
        rh_breaktime.setText(((int) entry.getVal() / 60) + " min");

        if (roadhouse.getGasPrice() > 0) {
            rh_gas_price.setText(String.format(Locale.GERMAN, "%1.3f €", roadhouse.getGasPrice()));
            if (entry.getPause().getMeanGasPrice() > 0) {
                if (entry.getPause().getMeanGasPrice() > roadhouse.getGasPrice()) {
                    rh_gas_image.setImageResource(R.drawable.icon_gas_green);
                } else {
                    rh_gas_image.setImageResource(R.drawable.icon_gas_red);
                }
            }
        } else {
            rh_gas_price.setText("");
            rh_gas_image.setImageResource(R.drawable.icon_gas_normal);
        }

        // set listener on the floating action button
        rh_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // notify main fragment that roadhouse was selected
                listener.onMapFragmentRoadhouseChanged(entry, roadhouse);
            }
        });


    }

    /**
     * Add marker cluster for alternative roadhouses.
     *
     * @param entry the entry
     */
    public void addMarkerCluster(WheelEntry entry) {
        Break pause = entry.getPause();
        // TODO: remove from hashmap
        // remove previously added cluster and marker
        if (pause.getClusterLayer() != null) {
            map.removeClusterLayer(pause.getClusterLayer());
            for (MapMarker marker : pause.getClusterLayer().getMarkers()) {
                markerWheelEntryMap.remove(marker);
            }
        }
        if (pause.getMainRoadhouseMarker() != null) {
            map.removeMapObject(pause.getMainRoadhouseMarker());
            markerWheelEntryMap.remove(pause.getMainRoadhouseMarker());
        }

        prepareImages();

        ClusterLayer cl = new ClusterLayer();

        // set the marker for the main roadhouse
        if (pause.getMainRoadhouse() != null) {
            MapMarker marker = new MapMarker();
            marker.setIcon(icon_main);
            marker.setCoordinate(pause.getMainRoadhouse().getPlaceLink().getPosition());
            marker.setAnchorPoint(anchorPoint);
            markerWheelEntryMap.put(marker, new EntryRoadhouseStruct(entry, pause.getMainRoadhouse()));
            pause.setMainRoadhouseMarker(marker);
            map.addMapObject(marker);
        }
        // set the markers for the alternative roadhouses as a cluster (better performance)
        if (pause.getAlternativeRoadhouses() != null) {
            for (Roadhouse rh : pause.getAlternativeRoadhouses()) {
                MapMarker marker = new MapMarker();
                marker.setIcon(icon_alt);
                marker.setCoordinate(rh.getPlaceLink().getPosition());
                marker.setAnchorPoint(anchorPoint);
                markerWheelEntryMap.put(marker, new EntryRoadhouseStruct(entry, rh));
                cl.addMarker(marker);
            }
        }
        pause.setClusterLayer(cl);
        map.addClusterLayer(cl);
    }

    @Override
    public void onPanStart() {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
    }

    @Override
    public void onPanEnd() {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
    }

    @Override
    public void onMultiFingerManipulationStart() {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
    }

    @Override
    public void onMultiFingerManipulationEnd() {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
    }

    @Override
    public boolean onMapObjectsSelected(List<ViewObject> objects) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        for (ViewObject viewObj : objects) {
            if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {
                if (((MapObject) viewObj).getType() == MapObject.Type.MARKER) {
                    EntryRoadhouseStruct struct = markerWheelEntryMap.get(viewObj);
                    setSidebarInfo(struct.entry, struct.rh);
                }
            }
        }
        return false;
    }

    @Override
    public boolean onTapEvent(PointF pointF) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(PointF pointF) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    @Override
    public void onPinchLocked() {

    }

    @Override
    public boolean onPinchZoomEvent(float v, PointF pointF) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    @Override
    public void onRotateLocked() {

    }

    @Override
    public boolean onRotateEvent(float v) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    @Override
    public boolean onTiltEvent(float v) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    @Override
    public boolean onLongPressEvent(PointF pointF) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    @Override
    public void onLongPressRelease() {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
    }

    @Override
    public boolean onTwoFingerTapEvent(PointF pointF) {
        NavigationWrapper.getInstance().getNavigationManager().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
        return false;
    }

    /**
     * On route changed. Receives event when the route was (re-)calculated
     *
     * @param routeWrapper the route wrapper
     */
    public void onRouteChanged(RouteWrapper routeWrapper) {
        NavigationWrapper.getInstance().getNavigationManager().setRoute(routeWrapper.getRoute());
    }

    /**
     * Add truck state event listener.
     *
     * @param listener the listener
     */
    public void addTruckStateEventListener(TruckStateEventListener listener) {
        listeners.add(listener);
    }

    private void prepareImages() {
        if (icon_main == null || icon_alt == null || icon_pos == null || icon_start == null || icon_finish == null) {
            icon_main = new Image();
            icon_alt = new Image();
            icon_pos = new Image();
            icon_start = new Image();
            icon_finish = new Image();
            try {
                icon_main.setImageResource(R.drawable.marker_alt);
                icon_alt.setImageResource(R.drawable.marker_unselected);
                icon_pos.setImageResource(R.drawable.marker_truck);
                icon_start.setImageResource(R.drawable.marker_main);
                icon_finish.setImageResource(R.drawable.ic_flag_black_32dp);
                anchorPoint = new PointF(icon_main.getWidth() / 2, icon_main.getHeight());
                anchorPointPos = new PointF(icon_pos.getWidth() / 2, icon_pos.getHeight());
            } catch (IOException e) {
                Log.e(TAG, "Marker image not found");
            }
        }
    }

    private class EntryRoadhouseStruct {
        /**
         * The Entry.
         */
        public WheelEntry entry;
        /**
         * The Rh.
         */
        public Roadhouse rh;


        /**
         * Instantiates a new Entry roadhouse struct.
         *
         * @param entry the entry
         * @param rh    the rh
         */
        public EntryRoadhouseStruct(WheelEntry entry, Roadhouse rh) {
            this.entry = entry;
            this.rh = rh;
        }
    }
}
