package tac.android.de.truckcompanion;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.guidance.NavigationManager;
import tac.android.de.truckcompanion.adapter.ViewPagerAdapter;
import tac.android.de.truckcompanion.data.*;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.fragment.MainFragment;
import tac.android.de.truckcompanion.fragment.MapFragment;
import tac.android.de.truckcompanion.fragment.RatingDialogFragment;
import tac.android.de.truckcompanion.fragment.StatsFragment;
import tac.android.de.truckcompanion.geo.NavigationWrapper;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.CustomViewPager;
import tac.android.de.truckcompanion.utils.CustomViewPager;
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.util.ArrayList;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity implements TruckStateEventListener, OnRoadhouseSelectedListener {

    /**
     * The constant context.
     */
// View references
    public static Context context;
    /**
     * The constant fm.
     */
    public static FragmentManager fm;
    private TabLayout tabLayout;
    private CustomViewPager viewPager;
    /**
     * The constant viewPagerAdapter.
     */
    public static ViewPagerAdapter viewPagerAdapter;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private RelativeLayout splashScreen;
    private TextView splashScreenStatus;
    private ImageView splashScreenImage;

    // Resources
    private CharSequence drawerTitle;


    // Data
    private static Journey currentJourney;
    /**
     * The Data collector.
     */
    public DataCollector dataCollector;

    // Fragments
    private MainFragment mainFragment;
    private MapFragment mapFragment;
    private StatsFragment statsFragment;

    // Constants
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DRIVER_ID = 1;
    private static final int TOUR_ID = 1;
    private static final int TRUCK_ID = 1;

    /**
     * The constant NAVIGATION_DRIVING_SPEED. Defines the default driving speed of the truck in m/s
     */
    public static final double NAVIGATION_DRIVING_SPEED = 22.222;
    /**
     * The constant VELOCITY_FACTOR. The factor of which the simulation is speeded up.
     */
    public static final double VELOCITY_FACTOR = 13.5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        splashScreen = (RelativeLayout) findViewById(R.id.splash_screen);
        splashScreenStatus = (TextView) findViewById(R.id.splash_screen_status);
        splashScreenImage = (ImageView) findViewById(R.id.splash_screen_img);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        splashScreenImage.startAnimation(pulse);

        context = getApplicationContext();
        dataCollector = new DataCollector(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.left_drawer);
        fm = getFragmentManager();
        setSupportActionBar(toolbar);

        viewPager.setPagingEnabled(false);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.main_view));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.map));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.stats));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Creating adapter and setting that adapter to the viewPager
        viewPagerAdapter = new ViewPagerAdapter(fm);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Set tab text colors
        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));
        tabLayout.setSelectedTabIndicatorHeight(8);

        /*
        Drawer related stuff starts here
         */
        // Set drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has settled in a completely open state
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
            }

            // Called when a drawer has settled in a completely closed state
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Set the navigation's list click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.main_view:
                        Toast.makeText(getApplicationContext(), "Main View Selected", Toast.LENGTH_SHORT).show();
                        return true;

                    // For rest of the options we just show a toast on click
                    default:
                        Toast.makeText(getApplicationContext(), "Item Selected", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });

        // Setup/load journey data
        splashScreenStatus.setText(getString(R.string.loading_journey_data_msg));
        new Journey.LoadJourneyData(this, new AsyncResponse<Journey>() {
            @Override
            public void processFinish(Journey journey) {
                if (journey == null) {
                    Toast.makeText(getApplicationContext(), R.string.no_journey_found_toast, Toast.LENGTH_SHORT).show();
                } else {
                    currentJourney = journey;
                    statsFragment = (StatsFragment) viewPagerAdapter.getRegisteredFragment(2);
                    splashScreenStatus.setText(getString(R.string.loading_route_data_msg));
                    mapFragment = (MapFragment) viewPagerAdapter.getRegisteredFragment(1);
                    mapFragment.init(new OnEngineInitListener() {
                        @Override
                        public void onEngineInitializationCompleted(Error error) {
                            if (error == Error.NONE) {
                                mapFragment.setMap(mapFragment.getMapFragment().getMap());
                                mapFragment.getMapFragment().getMapGesture().addOnGestureListener(mapFragment);

                                // set the map for navigation
                                NavigationWrapper.getInstance().getNavigationManager().setMap(mapFragment.getMap());
                                currentJourney.initRoute();

                                // Calculate first route
                                calculateRoute(currentJourney.getStartPoint(), currentJourney.getDestinationPoints(), splashScreenStatus, new AsyncResponse<RouteWrapper>() {
                                    @Override
                                    public void processFinish(final RouteWrapper routeWrapper) {
                                        // Setup Main-Fragment (wheel)
                                        mainFragment = (MainFragment) viewPagerAdapter.getRegisteredFragment(0);
                                        mainFragment.setBreaks(routeWrapper, splashScreenStatus, new AsyncResponse<ArrayList>() {

                                            @Override
                                            public void processFinish(ArrayList breaks) {
                                                // Calculate second route (with breaks)
                                                calculateRoute(currentJourney.getStartPoint(), currentJourney.getDestinationPoints(), splashScreenStatus, new AsyncResponse<RouteWrapper>() {
                                                    @Override
                                                    public void processFinish(RouteWrapper updatedRouteWrapper) {
                                                        if (updatedRouteWrapper == null) {
                                                            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            // TODO: change this to a listener interface
                                                            onStartupTaskReady();
                                                            mainFragment.onStartupTaskReady(updatedRouteWrapper, splashScreen);
                                                            mapFragment.onStartupTaskReady();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            } else {
                                Log.e(TAG, "MapFragment initialization failed with: " + error.toString());
                            }
                        }
                    });
                }
            }
        }).execute(DRIVER_ID, TOUR_ID, TRUCK_ID);
    }

    private void onStartupTaskReady() {
        mapFragment.addTruckStateEventListener(this);
    }

    /**
     * Gets the current journey.
     *
     * @return the current journey
     */
    public static Journey getCurrentJourney() {
        return currentJourney;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu. This adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.main_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.start_navigation_menu_item:
                // start navigation
                NavigationManager.Error error = NavigationWrapper.getInstance().getNavigationManager().simulate(currentJourney.getRouteWrapper().getRoute(), (long) (NAVIGATION_DRIVING_SPEED * VELOCITY_FACTOR));
                if (error != NavigationManager.Error.NONE) {
                    Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.stop_navigation_menu_item:
                NavigationWrapper.getInstance().getNavigationManager().stop();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(currentJourney != null && currentJourney.getRouteWrapper() != null && currentJourney.getRouteWrapper().getCalculationFinished() && currentJourney.getRouteWrapper().getRoute() != null);
        return true;
    }

    @Override
    public void onTruckStationaryStateChange(int state) {
        Log.d("TAC", "State changed: " + state);
        statsFragment.onTruckStationaryStateChange(state);
    }

    @Override
    public void onTruckMoved() {
        mainFragment.onTruckMoved();
        statsFragment.onTruckMoved();
    }

    @Override
    public void onJourneyFinished() {

    }

    /**
     * Wrapper for route calculation.
     *
     * @param startPoint        the start point
     * @param destinationPoints the destination points
     * @param textToUpdate      The text to show during updating/calculating the route
     * @param callback          the callback to be executed after route calculation finished
     */
    public void calculateRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, final Object textToUpdate, final AsyncResponse<RouteWrapper> callback) {
        currentJourney.getRouteWrapper().requestRoute(startPoint, destinationPoints, textToUpdate, callback);
    }

    @Override
    public void onMainFragmentRoadhouseChanged(WheelEntry entry) {
        mapFragment.setMainRoadhouse(entry);
    }

    @Override
    public void onMapFragmentRoadhouseChanged(WheelEntry entry, Roadhouse roadhouse) {
        mainFragment.setMainRoadhouse(entry, roadhouse);
    }

    @Override
    public void onPauseDataChanged(WheelEntry entry) {
        mapFragment.addMarkerCluster(entry);
    }

    @Override
    public void onBreakFinished() {
        mainFragment.onBreakFinished();
        WheelEntry lastBreakEntry = mainFragment.getPrevBreak();
        DialogFragment ratingDialog = RatingDialogFragment.newInstance(R.string.alert_dialog_rate_title, lastBreakEntry.getPause().getMainRoadhouse().getPlaceLink().getTitle());

        ratingDialog.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onRouteChanged(RouteWrapper routeWrapper) {
        mapFragment.onRouteChanged(routeWrapper);
    }

    /**
     * Gets next break according to the route.
     *
     * @return the next break
     */
    public WheelEntry getNextBreak() {
        return mainFragment.getNextBreak();
    }

    /**
     * User clicked "OK" in rating dialog.
     */
    public void doPositiveClick() {
    }

    /**
     * User clicked "Cancel" in rating dialog.
     */
    public void doNegativeClick() {
    }

    /**
     * User clicked "Rate More" in rating dialog.
     */
    public void doRateMore() {
    }
}
