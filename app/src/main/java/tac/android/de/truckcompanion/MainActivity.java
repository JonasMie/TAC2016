package tac.android.de.truckcompanion;

import android.graphics.Color;
import android.graphics.PointF;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.PointD;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    // View references
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;

    private RelativeLayout mRelativeLayout;
    private PieChart mChart;


    // Resources
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    private float mStartAngle = 0;
    private ArrayList<ImageView> symbols = new ArrayList<>();
    private PointF mTouchStartPoint = new PointF();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Toolbar and settring it as the action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.left_drawer);
        mTitle = mDrawerTitle = getTitle();

        // Set drawer toggle
        // TODO: Set drawer icon (ic_drawer.png)
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


        mChart = (PieChart)

                findViewById(R.id.chart);

        mRelativeLayout = (RelativeLayout)

                findViewById(R.id.rLayout);

//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
//                width,
////                (int) (height * .5)
//                0
//        );
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
//                width,
////                (int) (height * 1.5)
//                height
//        );
//
//        textView.setLayoutParams(params1);
//        relativeLayout.setLayoutParams(params2);

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        yVals1.add(new

                Entry((float)

                (450 / 24), 0));
        yVals1.add(new

                Entry((float)

                (75 / 24), 1));
        yVals1.add(new

                Entry((float)

                (450 / 24), 2));
        yVals1.add(new

                Entry((float)

                (75 / 24), 3));
        yVals1.add(new

                Entry((float)

                (100 / 24), 4));
        yVals1.add(new

                Entry((float)

                (1250 / 24), 5));

        PieDataSet dataSet = new PieDataSet(yVals1, "Fahrtzeiten");

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("1. Lenkzeit");
        xVals.add("1. Pausenzeit");
        xVals.add("2. Lenkzeit");
        xVals.add("2. Pausenzeit");
        xVals.add("3. Lenkzeit");
        xVals.add("Ruhezeit");

        PieData data = new PieData(xVals, dataSet);
        mChart.setData(data);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(80f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setLogEnabled(true);
//        mChart.setMaxAngle(270f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.BLACK);
        dataSet.setColors(colors);

        // Listener
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
        float x = me.getX();
        float y = me.getY();

        float angle = mChart.getAngleForPoint(x,y);
        int index = mChart.getIndexForAngle(angle);
        Entry longPressedEntry = mChart.getEntriesAtIndex(index).get(0);
        longPressedEntry.setVal(50f);
        mChart.notifyDataSetChanged();
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
//        mChart.setRotationEnabled(false);
//        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
//        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
        mChart.setRotationEnabled(true);
    }
}
