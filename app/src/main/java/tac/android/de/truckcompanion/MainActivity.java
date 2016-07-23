package tac.android.de.truckcompanion;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
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
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TruckStateEventListener, OnRoadhouseSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static FragmentManager fm;

    private static final int DRIVER_ID = 1;
    private static final int TOUR_ID = 1;
    private static final int TRUCK_ID = 1;
    public static final double NAVIGATION_DRIVING_SPEED = 22.222;
    public static final double VELOCITY_FACTOR = 13.5;

    // View references
    private ProgressDialog mProgressDialog;
    private TabLayout mTabLayout;
    private CustomViewPager mViewPager;
    public static ViewPagerAdapter mViewPagerAdapter;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    // Resources
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Data
    private static Journey mCurrentJourney;
    private TruckState mCurrentTruckState;
    public DataCollector dataCollector;

    // Fragments
    private MainFragment mainFragment;
    private MapFragment mapFragment;
    private StatsFragment statsFragment;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        dataCollector = new DataCollector(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (CustomViewPager) findViewById(R.id.viewpager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.left_drawer);

        fm = getFragmentManager();
        setSupportActionBar(toolbar);

        mViewPager.setPagingEnabled(false);
        mViewPager.setOffscreenPageLimit(2);

        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.main_view));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.map));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.stats));

        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Creating adapter and setting that adapter to the viewPager
        mViewPagerAdapter = new ViewPagerAdapter(fm);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Set tab text colors
        mTabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        /*
        Drawer related stuff starts here
         */
        mTitle = mDrawerTitle = getTitle();

        // Set drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            // Called when a drawer has settled in a completely open state
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

            // Called when a drawer has settled in a completely closed state
            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Set the navigation's list click listener
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }

                //Closing drawer on item click
                mDrawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.main_view:
                        Toast.makeText(getApplicationContext(), "Main View Selected", Toast.LENGTH_SHORT).show();
                        //                        ContentFragment fragment = new ContentFragment();
                        //                        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        //                        fragmentTransaction.replace(R.id.frame, fragment);
                        //                        fragmentTransaction.commit();
                        return true;

                    // For rest of the options we just show a toast on click
                    default:
                        Toast.makeText(getApplicationContext(), "Item Selected", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });

        // Setup/load journey data
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.loading_journey_data_title);
        mProgressDialog.setMessage(getString(R.string.loading_journey_data_msg));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        new Journey.LoadJourneyData(this, new AsyncResponse<Journey>() {
            @Override
            public void processFinish(Journey journey) {
                if (journey == null) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), R.string.no_journey_found_toast, Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentJourney = journey;

                    statsFragment = (StatsFragment) mViewPagerAdapter.getRegisteredFragment(2);

                    mProgressDialog.setMessage(getString(R.string.loading_route_data_msg));
                    mapFragment = (MapFragment) mViewPagerAdapter.getRegisteredFragment(1);
                    mapFragment.init(new OnEngineInitListener() {
                        @Override
                        public void onEngineInitializationCompleted(Error error) {
                            if (error == Error.NONE) {
                                mapFragment.setMap(mapFragment.getMapFragment().getMap());
                                mapFragment.getMapFragment().getMapGesture().addOnGestureListener(mapFragment);

                                // set the map for navigation
                                NavigationWrapper.getInstance().getNavigationManager().setMap(mapFragment.getMap());
                                mCurrentJourney.initRoute();

                                // Calculate first route
                                calculateRoute(mCurrentJourney.getStartPoint(), mCurrentJourney.getDestinationPoints(), mProgressDialog, new AsyncResponse<RouteWrapper>() {
                                    @Override
                                    public void processFinish(final RouteWrapper routeWrapper) {
                                        // Setup Main-Fragment (wheel)
                                        mainFragment = (MainFragment) mViewPagerAdapter.getRegisteredFragment(0);
                                        mainFragment.setBreaks(routeWrapper, mProgressDialog, new AsyncResponse<ArrayList>() {

                                            @Override
                                            public void processFinish(ArrayList breaks) {
                                                // Calculate second route (with breaks)
                                                calculateRoute(mCurrentJourney.getStartPoint(), mCurrentJourney.getDestinationPoints(), mProgressDialog, new AsyncResponse<RouteWrapper>() {
                                                    @Override
                                                    public void processFinish(RouteWrapper updatedRouteWrapper) {
                                                        if (updatedRouteWrapper == null) {
                                                            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                                                            if (mProgressDialog.isShowing()) {
                                                                mProgressDialog.dismiss();
                                                            }
                                                        } else {
                                                            // TODO: change this to a listener interface
                                                            onStartupTaskReady();
                                                            mainFragment.onStartupTaskReady(updatedRouteWrapper);
                                                            mapFragment.onStartupTaskReady();
                                                            if (mProgressDialog.isShowing()) {
                                                                mProgressDialog.dismiss();
                                                            }
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
                NavigationManager.Error error = NavigationWrapper.getInstance().getNavigationManager().simulate(mCurrentJourney.getRouteWrapper().getRoute(), (long) (NAVIGATION_DRIVING_SPEED * VELOCITY_FACTOR));
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
        menu.getItem(0).setEnabled(mCurrentJourney != null && mCurrentJourney.getRouteWrapper() != null && mCurrentJourney.getRouteWrapper().getCalculationFinished() && mCurrentJourney.getRouteWrapper().getRoute() != null);
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

    public static Journey getmCurrentJourney() {
        return mCurrentJourney;
    }

    public void calculateRoute(DispoInformation.StartPoint startPoint, ArrayList<DispoInformation.DestinationPoint> destinationPoints, final ProgressDialog progressDialog, final AsyncResponse<RouteWrapper> callback) {
        mCurrentJourney.getRouteWrapper().requestRoute(startPoint, destinationPoints, progressDialog, callback);
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

    public WheelEntry getNextBreak() {
        return mainFragment.getNextBreak();
    }

    public void doPositiveClick() {
    }

    public void doNegativeClick() {
    }

    public void doRateMore() {
    }
}
