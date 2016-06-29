package tac.android.de.truckcompanion;

import android.app.ProgressDialog;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import tac.android.de.truckcompanion.adapter.ViewPagerAdapter;
import tac.android.de.truckcompanion.data.DataCollector;
import tac.android.de.truckcompanion.data.Journey;
import tac.android.de.truckcompanion.data.TruckState;
import tac.android.de.truckcompanion.data.TruckStateEventListener;
import tac.android.de.truckcompanion.fragment.MyMapFragment;
import tac.android.de.truckcompanion.utils.AsyncResponse;

public class MainActivity extends AppCompatActivity implements TruckStateEventListener {

    private static final int DRIVER_ID = 1;
    private static final int TOUR_ID = 1;
    private static final int TRUCK_ID = 1;

    // View references
    private ProgressDialog mProgressDialog;
    private TabLayout mTabLayout;
    private ViewPager mViewPger;
    private ViewPagerAdapter mViewPagerAdapter;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    // Resources
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Data
    private Journey mCurrentJourney;
    private TruckState mCurrentTruckState;
    public DataCollector dataCollector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataCollector = new DataCollector(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPger = (ViewPager) findViewById(R.id.viewpager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.left_drawer);

        /*
        ViewPager / Tabs related stuff starts here
         */
        // Creating adapter and setting that adapter to the viewPager
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPger.setAdapter(mViewPagerAdapter);

        setSupportActionBar(toolbar);

        // Create the tabs
        final TabLayout.Tab home = mTabLayout.newTab();
        final TabLayout.Tab map = mTabLayout.newTab();
        final TabLayout.Tab stats = mTabLayout.newTab();

        // Set the tab titles
        home.setText(R.string.main_view);
        map.setText(R.string.map);
        stats.setText(R.string.stats);

        // Add the tabs to the layout
        mTabLayout.addTab(home, 0);
        mTabLayout.addTab(map, 1);
        mTabLayout.addTab(stats, 2);

        // Set tab text colors
        mTabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

        // Add the onPageChangeListener to the view pager
        mViewPger.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

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
        new Journey.LoadJourneyData(this, mProgressDialog, new AsyncResponse<Journey>() {
            @Override
            public void processFinish(Journey journey) {
                if (journey == null) {
                    Toast.makeText(getApplicationContext(), R.string.no_journey_found_toast, Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentJourney = journey;
                    Log.d("TAC", journey.toString());
                }
            }
        }, dataCollector).execute(DRIVER_ID, TOUR_ID, TRUCK_ID);



    }

//    @Override
//    public void onSimulationEvent(JSONObject event) {
//        try {
//            Log.d("TACSimulation", "New event: " + event.getDouble("lat") + ", " + event.getDouble("lng") + ", Speed: " + event.getInt("speed"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

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
            case R.id.start_simulation_menu_item:
                // Start simulation
                try {
//                    JourneySimulation simulation = JourneySimulation.Builder(this);
//                    simulation.addOnSimulationEventListener(this);
//                    simulation.startSimulation();

                    // add journey event listener
                    mCurrentTruckState = new TruckState(this);
                    mCurrentTruckState.addTruckStateEventListener(this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onTruckStationaryStateChange(int state) {
        Log.d("TAC", "State changed: " + state);
    }




}