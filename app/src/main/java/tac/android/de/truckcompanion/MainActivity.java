package tac.android.de.truckcompanion;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.adapter.ViewPagerAdapter;
import tac.android.de.truckcompanion.data.*;
import tac.android.de.truckcompanion.fragment.MainFragment;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.ResponseCallback;

public class MainActivity extends AppCompatActivity implements TruckStateEventListener {

    public static FragmentManager fm;

    private static final int DRIVER_ID = 1;
    private static final int TOUR_ID = 1;
    private static final int TRUCK_ID = 1;

    // View references
    private ProgressDialog mProgressDialog;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
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

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        dataCollector = new DataCollector(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.left_drawer);

        fm = getSupportFragmentManager();
        setSupportActionBar(toolbar);

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
        mProgressDialog.show();
        new Journey.LoadJourneyData(this, new AsyncResponse<Journey>() {
            @Override
            public void processFinish(Journey journey) {
                if (journey == null) {
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), R.string.no_journey_found_toast, Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentJourney = journey;
                    mProgressDialog.setMessage(getString(R.string.loading_route_data_msg));
                    mCurrentJourney.getRoute().requestRoute(mCurrentJourney.getStartPoint(), mCurrentJourney.getDestinationPoints(), dataCollector, new ResponseCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            try {
                                mCurrentJourney.getRoute().setup(result);

                                // Setup Main-Fragment (wheel)
                                MainFragment fragment = (MainFragment) mViewPagerAdapter.getRegisteredFragment(0);
                                fragment.setupFragment(mProgressDialog);
                            } catch (JSONException e) {
                                mProgressDialog.dismiss();
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            Log.e("TAC", error.getMessage());
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        }).execute(DRIVER_ID, TOUR_ID, TRUCK_ID);
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
            case R.id.start_simulation_menu_item:
                // add journey event listener
                try {
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