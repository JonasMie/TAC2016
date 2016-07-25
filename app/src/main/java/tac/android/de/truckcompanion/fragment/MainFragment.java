package tac.android.de.truckcompanion.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import com.android.volley.VolleyError;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.Place;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.ResultListener;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.DataCollector;
import tac.android.de.truckcompanion.data.Journey;
import tac.android.de.truckcompanion.data.Roadhouse;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.GeoHelper;
import tac.android.de.truckcompanion.geo.NavigationWrapper;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.utils.ResponseCallback;
import tac.android.de.truckcompanion.wheel.OnEntryGestureListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.text.SimpleDateFormat;
import java.util.*;

import static tac.android.de.truckcompanion.wheel.WheelEntry.COLORS;
import static tac.android.de.truckcompanion.wheel.WheelEntry.PAUSE_ENTRY;


/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:37
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MainFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener, OnEntryGestureListener, View.OnLayoutChangeListener {

    // Chart-related members
    private PieChart mChart;
    private PieDataSet dataSet;
    private PieData data;
    private ArrayList<Entry> entries;
    private WheelEntry selectedEntry;
    private float mStartAngle = 0;
    private PointF mTouchStartPoint = new PointF();
    private float currentTimeAngle;

    // View-realted members
    private MainActivity activity;
    private ProgressDialog progressDialog;
    private LinearLayout recWrapper;
    private RelativeLayout mainRecWrapper;
    private RelativeLayout altRecwrapper;
    private TextView mainRecTitle;
    private TextView mainRecETA;
    private TextView mainRecDistance;
    private TextView mainRecBreaktime;
    private RatingBar mainRecRating;
    private TextView mainRecGasPrice;
    private ImageView mainRecGasImg;
    private CarouselView carouselView;
    private FrameLayout chartWrapper;
    private CustomCanvas canvas;
    private ImageView clock;


    // Logic data
    private DataCollector dc;
    private float autoUpdateArcAngle;
    private boolean autoUpdateWheelAngle = true;
    private float clockOffsetAngle = 0;
    private WheelEntry previousBreakEntry;
    /**
     * The constant ROUTE_RECALCULATION_DELAY. Defines the time delay (in ms), after which the route-recalculation is
     * executed after an chart-entry was dragged
     */
    public static final long ROUTE_RECALCULATION_DELAY = 2000;


    // Misc
    private OnRoadhouseSelectedListener listener;
    private Vibrator vibrator;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    private Timer timer = new Timer();
    private TimerTask entryDraggedTimerTask;
    private TimerTask wheelMovedTimerTask;
    private Handler refresh;
    private LinearInterpolator interpolator;

    // Constants
    private static final String TAG = "TAC";
    private static final double ENTRY_LONGPRESS_TOLERANCE = .2;
    /**
     * The constant SECONDS_PER_DAY. Total amount of seconds within one day.
     */
    public static final float SECONDS_PER_DAY = 24 * 60 * 60;
    /**
     * The constant FIRST_SPLIT. Amount of seconds of the first pause split (15 min)
     */
    public static final int FIRST_SPLIT = 15 * 60;
    /**
     * The constant SECOND_SPLIT. Amount of seconds of the second pause split (30 min)
     */
    public static final int SECOND_SPLIT = 30 * 60;
    /**
     * The constant COMPLETE_BREAK. Amount of seconds of the complete pause (45 min)
     */
    public static final int COMPLETE_BREAK = 45 * 60;
    /**
     * The constant MIN_TIME_BETWEEN_BREAKS. Minimum amount of time between two seperate breaks (10 min)
     */
    public static final int MIN_TIME_BETWEEN_BREAKS = 10 * 60;

    private static final float MAX_BUFFER_VAL = 270 * 60 - MIN_TIME_BETWEEN_BREAKS;

    /**
     * The constant MAX_DRIVE_VAL. Maximum amount of time the driver is allowed to drive (4.5 hours)
     */
    public static final float MAX_DRIVE_VAL = 270 * 60;

    /**
     * The constant MAX_DRIVER_TOLERANCE. The time tolerance which is given since the driver cannot have his break
     * after exactly 4.5 hours always.
     */
    public static final int MAX_DRIVE_TOLERANCE = 10 * 60;
    private static final long AUTO_UPDATE_WHEEL_MOVED_DELAY = 2000;
    private static final int CHART_ANGLE_OFFSET = 55;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Get all view references
        recWrapper = (LinearLayout) view.findViewById(R.id.recommendationsWrapper);
        mainRecWrapper = (RelativeLayout) view.findViewById(R.id.recommendations_main_wrapper);
        altRecwrapper = (RelativeLayout) view.findViewById(R.id.recommendations_alternatives_wrapper);

        chartWrapper = (FrameLayout) view.findViewById(R.id.chartWrapper);
        canvas = (CustomCanvas) view.findViewById(R.id.canvas);
        clock = (ImageView) view.findViewById(R.id.clock);

        mainRecTitle = (TextView) mainRecWrapper.findViewById(R.id.recommendations_main_title);
        mainRecETA = (TextView) mainRecWrapper.findViewById(R.id.recommendations_main_info_eta);
        mainRecDistance = (TextView) mainRecWrapper.findViewById(R.id.recommendations_main_info_distance);
        mainRecBreaktime = (TextView) mainRecWrapper.findViewById(R.id.recommendations_main_info_breaktime);
        mainRecRating = (RatingBar) mainRecWrapper.findViewById(R.id.recommendations_main_rating);
        mainRecGasPrice = (TextView) mainRecWrapper.findViewById(R.id.recommendations_main_misc_gas_price);
        mainRecGasImg = (ImageView) mainRecWrapper.findViewById(R.id.recommendations_main_misc_gas_img);

        mChart = (PieChart) view.findViewById(R.id.chart);

        activity = ((MainActivity) getActivity());

        /*
         The chart is partly (~50%) hidden below the bottom of the screen, so only half of the wheel is shown.
         I didn't find a way to do this in layout, so here is the programatically solved code.
         The top part of the screen (recommendations) is given 40% of the width, while the chart gets 150%, which makes
         it overlap the bottom.
          */

        dc = new DataCollector(getActivity());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                width,
                (int) (height * .4)
        );
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                width,
                (int) (height * 1.5)
        );

        recWrapper.setLayoutParams(params1);
        chartWrapper.setLayoutParams(params2);

        // set progress dialog , vibrator , looper & interpolator (for animations)
        progressDialog = new ProgressDialog(activity);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        refresh = new Handler(Looper.getMainLooper());
        interpolator = new LinearInterpolator();


        setupFragment();

        // Layout + appearance
        mChart.setDrawHoleEnabled(true);
        mChart.setTransparentCircleColor(Color.TRANSPARENT);
        mChart.setTransparentCircleAlpha(110);
        // make the whole of the chart 87% of the total radius.
        mChart.setHoleRadius(87f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setDragDecelerationEnabled(false);
        // set the initial offset of the chart, so it starts at the bottom-left corner
        mChart.setRotationAngle(mChart.getRotationAngle() - CHART_ANGLE_OFFSET);
        /* the library allows highlighting either for all chart segement or for none.
         But we want the chart to only highlight the pause entries.
         That's why we disable the highlighting per tab here and override the original code
         */
        mChart.setHighlightPerTapEnabled(false);

        // Listener
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        // the canvas overlay and the clock image can be set, when the layout of the chart is completely set.
        mChart.addOnLayoutChangeListener(this);

        // Hide Lables, Legend, ...
        mChart.setDescription("");
        mChart.setDrawSliceText(false);
        mChart.getLegend().setEnabled(false);

        // set initial rotation angle
        autoUpdateArcAngle = mChart.getRotationAngle();

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
     * On startup task ready. Called when the routes and roadhouses were initially calculated and the app is ready.
     *
     * @param updatedRouteWrapper the updated route wrapper
     * @param splashScreen        the splash screen
     */
    public void onStartupTaskReady(RouteWrapper updatedRouteWrapper, RelativeLayout splashScreen) {
        // move the pause entries to their correct positions
        this.updateEntryPositions(updatedRouteWrapper);
        // set the recommendations for the first break by default
        setRecommendations(1, null);
        // load detail infos for the breaks in background
        loadAllDetailInfosInBackground();

        currentTimeAngle = mChart.getRotationAngle();

        // set clock to current time (relative to the current chart angle)
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long passed = ((now - cal.getTimeInMillis()) / 1000) % (60 * 60 * 12);
        long angle = (long) ((passed / (SECONDS_PER_DAY * 0.5f) * 360));
        clockOffsetAngle = (-angle - CHART_ANGLE_OFFSET) % 360;
        if (clockOffsetAngle < 0) {
            clockOffsetAngle = 360 + clockOffsetAngle;
        }
        clockOffsetAngle -= (mChart.getRotationAngle() + 90);
        RotateAnimation a = new RotateAnimation(0, mChart.getRotationAngle() + 90 + clockOffsetAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setInterpolator(interpolator);
        a.setFillAfter(true);
        clock.startAnimation(a);

        // remove "splash screen"
        ((ViewGroup) splashScreen.getParent()).removeView(splashScreen);
    }

    /**
     * Setup the main fragment. It contains the chart-related stuff, like setting the breaks,...
     */
    public void setupFragment() {
        // load default entries
        entries = WheelEntry.getEntries();
        dataSet = new PieDataSet(entries, "Fahrtzeiten");

        // the library needs descriptions for each wheel entry. But we don't want to show them, so we set an empty string
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 11; i++) {
            xVals.add("");
        }

        data = new PieData(xVals, dataSet);
        // fill the chart with the entries
        mChart.setData(data);


        // set the colors of the wheel entries
        dataSet.setColors(WheelEntry.getColors(entries));
        // don't show the percentages for each slice in the chart
        data.setDrawValues(false);

        // Highlight 1st pause by default
        mChart.highlightValue(1, 0);
        selectedEntry = (WheelEntry) dataSet.getEntryForIndex(1);

        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    /**
     * Sets the breaks to the corresponding position, depending on the time in the route.
     *
     * @param routeWrapper       the route wrapper
     * @param splashScreenStatus the splash screen status text view
     * @param callback           the callback
     */
    public void setBreaks(final RouteWrapper routeWrapper, final TextView splashScreenStatus, final AsyncResponse<ArrayList> callback) {
        splashScreenStatus.setText(getString(R.string.loading_pause_data_msg));

        // retrieve all breaks
        final ArrayList<Break> breaks = Break.getBreaks();
        // retrieve all destination points of the route
        final ArrayList<DispoInformation.DestinationPoint> destinationPoints = MainActivity.getCurrentJourney().getDestinationPoints();

        // calculate all roadhouses for the first break entry
        breaks.get(0).calculateRoadhouses(MainActivity.getCurrentJourney().getPositionOnRouteByTime(breaks.get(0).getElapsedTime()), routeWrapper.getRoute().getStart(), 0, new AsyncResponse<Break>() {
            @Override
            public void processFinish(Break pause) {
                PlaceLink pauseLink = pause.getMainRoadhouse().getPlaceLink();
                GeoCoordinate pos = pauseLink.getPosition();
                destinationPoints.add(new DispoInformation.DestinationPoint(GeoHelper.GeoCoordinateToLatLng(pos), 15));

                // calculate all roadhouses for the second break entry
                breaks.get(1).calculateRoadhouses(MainActivity.getCurrentJourney().getPositionOnRouteByTime(breaks.get(1).getElapsedTime()), pos, 1, new AsyncResponse<Break>() {
                    @Override
                    public void processFinish(Break output) {
                        PlaceLink pauseLink = output.getMainRoadhouse().getPlaceLink();
                        GeoCoordinate pos = pauseLink.getPosition();
                        destinationPoints.add(new DispoInformation.DestinationPoint(GeoHelper.GeoCoordinateToLatLng(pos), 15));

                        // get all waypoints in the correct order (ordered by distance)
                        RouteWrapper.getOrderedWaypoints(MainActivity.getCurrentJourney().getStartPoint(), MainActivity.getCurrentJourney().getDestinationPoints(), null, new AsyncResponse<ArrayList>() {
                            @Override
                            public void processFinish(ArrayList orderedDestinationPoints) {
                                MainActivity.getCurrentJourney().setDestinationPoints(orderedDestinationPoints);
                                callback.processFinish(breaks);
                            }
                        });
                    }
                });
            }
        });
    }


    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
        mStartAngle = mChart.getAngleForPoint(me.getX(), me.getY());
        mTouchStartPoint.x = me.getX();
        mTouchStartPoint.y = me.getY();
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        autoUpdateWheelAngle = false;

        // Cancel the timer to reset the wheel angle, if the wheel was moved before the defined delay
        if (wheelMovedTimerTask != null) {
            wheelMovedTimerTask.cancel();
        }

        // The chart library detected a rotation gesture
        if (lastPerformedGesture == ChartTouchListener.ChartGesture.ROTATE) {
            float pointAngle = mChart.getAngleForPoint(me.getX(), me.getY());
            // how many degrees was the chart rotated?
            float diffAngle = pointAngle - mStartAngle;
            // this is fixing the undesired behaviour that the pause jumps around when the entry crosses the 0°-mark
            if (diffAngle > 180) {
                diffAngle -= 360;
            }

            if (mChart.isEditModeEnabled()) {
                // if the edit mode is enabled (after longpress on a pause entry), move the entry
                onEntryDragged(me, diffAngle);
            }

            // Synchronize the canvas overlay and the clock, if the wheel was rotated
            rotateViews(diffAngle);

            // diff-angle is incremental, but we only need the difference regarding the last change, so adapt startAngle
            mStartAngle += diffAngle;

            wheelMovedTimerTask = new TimerTask() {
                @Override
                public void run() {
                    autoUpdateWheelAngle = true;
                }
            };
            // Reset the wheel angle to the current one, if it wasn't rotated for 2 secs
            timer.schedule(wheelMovedTimerTask, AUTO_UPDATE_WHEEL_MOVED_DELAY);
        }
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        /*
        The Longpress-event for the chart.
        Problem: The longpress-event does exist for the whole chart exclusively, there is no built-in function to
        detect longpresses on single wheel entries.
        So that's basically covered here.
         */

        Log.i("LongPress", "Chart longpressed.");
        PointF center = mChart.getCenter();
        float radius = mChart.getRadius();

        // Get coordinates of longpress-event
        float x = me.getX();
        float y = me.getY();

        // Get the angle of these coordinates according to the wheel
        float angle = mChart.getAngleForPoint(x, y);

        // Get the index belonging to the corresponding wheel entry at this angle
        int index = mChart.getIndexForAngle(angle);
        // Finally get the entry
        WheelEntry longPressedEntry = (WheelEntry) dataSet.getEntryForIndex(index);

        // The user can only modify entries, which represent a pause. Modifying a driving period is not possible.
        if (!(longPressedEntry.getEntryType() == WheelEntry.PAUSE_ENTRY)) {
            return;
        }

        /*
         Since the listener listens for events on the whole chart, it is possible, to select the entries within a much
         larger radius. But this is not desired, it would confuse the user and lead to user-unexpected behaviour.
         So the longpress-event is limited to the dimensions of the entry plus a tolerance of 10%
         */
        if (!isInToleratedDistance(x, y, center, radius)) {
            return;
        }

        // Now we know, that a chart sector was longpressed

        // Vibrate to provide haptic feedback
        vibrator.vibrate(50);

        // Since the pause-entry is now selected, set it as current selected element (same behaviour as single-tap)
        WheelEntry prevActiveEntry = WheelEntry.getActiveEntry();
        if (prevActiveEntry != null) {
            prevActiveEntry.setEditModeActive(false);
        }
        selectedEntry = longPressedEntry;
        longPressedEntry.setEditModeActive(true);

        // if chart entry is longpressed, rotation is not possible, but editing is.
        mChart.setRotationEnabled(false);
        mChart.setEditModeEnabled(true);

        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");

        // Override the functionality, to allow selection for pause entries only.


        float distance = mChart.distanceToCenter(me.getX(), me.getY());

        // check if a slice was touched
        if (distance > mChart.getRadius()) {

            // if no slice was touched, do nothing
            selectedEntry.setEditModeActive(false);
            mChart.setEditModeEnabled(false);
            mChart.setRotationEnabled(true);
        } else {
            float angle = mChart.getAngleForPoint(me.getX(), me.getY());
            angle /= mChart.getAnimator().getPhaseY();

            int index = mChart.getIndexForAngle(angle);

            // check if the index could be found
            if (index < 0) {
                return;
            } else {
                // check if wheel entry is pause. if not, do nothing
                WheelEntry entry = (WheelEntry) dataSet.getEntryForIndex(index);
                if (entry.getEntryType() != PAUSE_ENTRY) {
                    return;
                } else {
                    if (entry == selectedEntry) {
                        entry.setEditModeActive(false);
                    } else {
                        Highlight h = new Highlight(index, 0);
                        mChart.highlightValue(h);
                        onValueSelected(entry, 0, h);
                    }
                }
            }
        }
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
        WheelEntry entry = (WheelEntry) e;
        if (entry.getEntryType() == PAUSE_ENTRY) {
            if (entry != selectedEntry) {
                selectedEntry = (WheelEntry) e;
                // a pause is selected, so set the recommendations
                setRecommendations(null);
                // notify the map fragment, that another break was selected
                listener.onMainFragmentRoadhouseChanged(selectedEntry);
            }
        }
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
        // unselect entry and re-enable rotation
        mChart.setRotationEnabled(true);
        mChart.setEditModeEnabled(false);
    }

    @Override
    public void onEntryDragged(MotionEvent me, float diffAngle) {
        Log.i("ENTRY:DRAG", "Entry dragged");
        WheelEntry entry = WheelEntry.getActiveEntry();
        if (entryDraggedTimerTask != null) {
            entryDraggedTimerTask.cancel();
        }

        boolean splitBreak = false;

        if (entry != null) {
            final int entryIndex = entry.getXIndex();

            // split the break (only for complete breaks)
            if (entry.getVal() == COMPLETE_BREAK) {
                if (diffAngle < 0) {
                    splitBreak = true;
                    // add new break, called the 'first split' (15 min)
                    addEntry(entryIndex, FIRST_SPLIT, WheelEntry.PAUSE_ENTRY);
                    ((WheelEntry) (dataSet.getEntryForIndex(entryIndex))).setEditModeActive(true);
                    mChart.highlightValue(entryIndex, 0);

                    // set the former complete break to second break
                    entry.setVal(SECOND_SPLIT);

                    // add driving time between first and second split
                    addEntry(entryIndex + 1, 0, WheelEntry.BUFFER_ENTRY);
                } else {
                    return;
                }
            }

            WheelEntry bufferEntry = (WheelEntry) dataSet.getEntryForIndex(entryIndex + 1);
            WheelEntry driveEntry = (WheelEntry) dataSet.getEntryForIndex(entryIndex - 1);
            final WheelEntry pauseEntry = (WheelEntry) dataSet.getEntryForIndex(entryIndex);

            if (splitBreak) {
                // if break was splitted, add new break to destination points
                MainActivity.getCurrentJourney().addDestinationPoint(pauseEntry.getPause().getDestinationPoint());
            }

            // fix undesired behaviour (wheel entry jumping around)
            while (diffAngle < -180) {
                diffAngle += 360;
            }

            // get the ratio the entry was moved
            double ratio = (diffAngle % 360) / 360;

            /*
            set the values of the driving entries before and after the first break split (instead of actually moving
            the first split, we decrease the size of the previous driving entry and increase the size of the following
            driving entry to mock the entry dragging
           */
            float newBufferVal = (float) (bufferEntry.getVal() - SECONDS_PER_DAY * ratio);
            float newDriveVal = (float) (driveEntry.getVal() + SECONDS_PER_DAY * ratio);

            if (newBufferVal < 0) {
                bufferEntry.setVal(0);
            } else if (newBufferVal > MAX_BUFFER_VAL) {
                bufferEntry.setVal(MAX_BUFFER_VAL);
            } else {
                bufferEntry.setVal(newBufferVal);
            }

            if (newDriveVal <= MIN_TIME_BETWEEN_BREAKS) {
                driveEntry.setVal(MIN_TIME_BETWEEN_BREAKS);
            } else if (newDriveVal > MAX_DRIVE_VAL) {
                driveEntry.setVal(MAX_DRIVE_VAL);
            } else {
                driveEntry.setVal(newDriveVal);
            }

            //  merge the breaks
            if (bufferEntry.getVal() == 0 && driveEntry.getVal() == MAX_DRIVE_VAL && entry.getVal() == FIRST_SPLIT) {
                pauseEntry.setVal(COMPLETE_BREAK);
                removeEntry(entryIndex + 1);
                removeEntry(entryIndex + 1);
                // Vibrate to provide haptic feedback
                vibrator.vibrate(50);
            }

            mChart.notifyDataSetChanged();
            mChart.invalidate();

            // (Re-)Calculate breaks
            entryDraggedTimerTask = new TimerTask() {
                @Override
                public void run() {
                    progressDialog.setTitle(R.string.loading_journey_data_title);
                    progressDialog.setMessage(getString(R.string.updating_pause_data_msg));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setCancelable(false);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.show();
                        }
                    });

                    Break pause = pauseEntry.getPause();
                    final DispoInformation.DestinationPoint formerDestinationPoint = pause.getDestinationPoint();

                    GeoCoordinate refPoint;


                    // get the reference points, which are used to calculate the allowed driving times (for example
                    // for the second break the first break is used as ref point, since he is allowed to drive 4.5 hours
                    // after the first break
                    if (pause.getIndex() == 0) {
                        // entry is first entry
                        refPoint = MainActivity.getCurrentJourney().getRouteWrapper().getRoute().getStart();
                    } else {
                        refPoint = ((WheelEntry) dataSet.getEntryForIndex(entryIndex - 2)).getPause().getMainRoadhouse().getPlaceLink().getPosition();
                    }
                    final Break[] formerPause = {pause};
                    // Update the break (search new roadhouses etc.)
                    pause.update(getAccumulatedValue(entryIndex), refPoint, pauseEntry.getPause().getIndex(), new AsyncResponse<Break>() {
                        @Override
                        public void processFinish(Break pause) {
                            Log.i(TAG, "Roadhouse updated. New Roadhouse");
                            Break.removeBreak(formerPause[0]);
                            formerPause[0] = pause;
                            Break.addBreak(pause);

                            progressDialog.setMessage(getString(R.string.updating_route_data_msg));
                            final Journey journey = MainActivity.getCurrentJourney();
                            // remove old destination point and add newly calculated point to the list
                            if (!journey.getDestinationPoints().contains(pause.getDestinationPoint())) {
                                journey.addDestinationPoint(pause.getDestinationPoint());
                                journey.removeDestinationPoint(formerDestinationPoint);
                            }

                            // UPDATE ROUTE!
                            RouteWrapper.getOrderedWaypoints(journey.getStartPoint(), journey.getDestinationPoints(), null, new AsyncResponse<ArrayList>() {
                                @Override
                                public void processFinish(ArrayList orderedDestinationPoints) {
                                    journey.setDestinationPoints(orderedDestinationPoints);
                                    activity.calculateRoute(journey.getStartPoint(), journey.getDestinationPoints(), progressDialog, new AsyncResponse<RouteWrapper>() {
                                        @Override
                                        public void processFinish(RouteWrapper routeWrapper) {
                                            // update the position of the pause entries in the wheel
                                            updateEntryPositions(routeWrapper);
                                            // update recommendations
                                            setRecommendations(null);
                                            // unselect wheel entry
                                            onNothingSelected();
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressDialog.dismiss();
                                                }
                                            });
                                            // notify map fragment that the route has changed
                                            listener.onRouteChanged(routeWrapper);
                                            progressDialog.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    });
//                }
                }
            };
            timer.schedule(entryDraggedTimerTask, ROUTE_RECALCULATION_DELAY);
        }
    }

    @Override
    public void onEntryResized(MotionEvent me) {

    }

    /**
     * Set the positions of the wheel entries to the correct position concerning the actual route
     *
     * @param routeWrapper the route wrapper
     */
    public void updateEntryPositions(RouteWrapper routeWrapper) {
        Route route = routeWrapper.getRoute();
        List<Maneuver> maneuvers = route.getManeuvers();
        ArrayList<Break> breaks = Break.getBreaks();
        long timeSinceLastStop = routeWrapper.getDepartureTime().getTime();

        // HERE Maps provides no possibility to get all stopover waypoints at once, so we have to iterate through all
        // maneuvers along the route
        for (int i = 0; i < maneuvers.size(); i++) {
            if (maneuvers.get(i).getAction() == Maneuver.Action.STOPOVER || maneuvers.get(i).getAction() == Maneuver.Action.END) {
                for (Break pause : breaks) {
                    // search the matching break for each waypoint

                    // Distance between these coordinates is less than 50 meters, so we can assume it's the same place
                    if (maneuvers.get(i).getCoordinate().distanceTo(pause.getMainRoadhouse().getPlaceLink().getPosition()) < 50) {
                        pause.getMainRoadhouse().setETA(maneuvers.get(i).getStartTime());
                        pause.getMainRoadhouse().setDurationFromStart((maneuvers.get(i).getStartTime().getTime() - timeSinceLastStop) / 1000);

                        if (pause.getIndex() > 0) {
                            // pause is not the first break
                            WheelEntry entry = (WheelEntry) dataSet.getEntryForIndex(1);
                            if (entry.getEntryType() == PAUSE_ENTRY && (entry.getVal() == SECOND_SPLIT || entry.getVal() == COMPLETE_BREAK)) {
                                pause.getMainRoadhouse().setDistanceFromStart(pause.getMainRoadhouse().getDistanceFromStart() + entry.getPause().getMainRoadhouse().getDistanceFromStart());
                            } else {
                                entry = (WheelEntry) dataSet.getEntryForIndex(3);
                                if (entry.getEntryType() == PAUSE_ENTRY && (entry.getVal() == SECOND_SPLIT || entry.getVal() == COMPLETE_BREAK)) {
                                    pause.getMainRoadhouse().setDistanceFromStart(pause.getMainRoadhouse().getDistanceFromStart() + entry.getPause().getMainRoadhouse().getDistanceFromStart());
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        WheelEntry prevWheelEntry = null;
        long prevVal = 0;
        for (int i = 0; i < dataSet.getEntryCount(); i++) {
            WheelEntry wheelEntry = (WheelEntry) dataSet.getEntryForIndex(i);

            if (wheelEntry.getEntryType() == PAUSE_ENTRY) {
                prevWheelEntry.setVal(wheelEntry.getPause().getMainRoadhouse().getDurationFromStart() - prevVal);
                prevVal = wheelEntry.getPause().getMainRoadhouse().getDurationFromStart();
                listener.onPauseDataChanged(wheelEntry);
            }
            prevWheelEntry = wheelEntry;
        }
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    /**
     * The Carousel view listener. Called each time when the carousel is recreated for each item
     */
    ViewListener carouselViewListener = new ViewListener() {
        @Override
        public View setViewForPosition(final int index) {
            View alternativeView = getActivity().getLayoutInflater().inflate(R.layout.recommendation_alternative, null);
            //set view attributes here

            if (selectedEntry != null && selectedEntry.getEntryType() == PAUSE_ENTRY) {
                Roadhouse rh = selectedEntry.getPause().getAlternativeRoadhouses().get(index);
                PlaceLink pauseLink = rh.getPlaceLink();
                TextView title = (TextView) alternativeView.findViewById(R.id.recommendations_alternative_title);
                TextView eta = (TextView) alternativeView.findViewById(R.id.recommendations_alternative_eta);
                TextView distance = (TextView) alternativeView.findViewById(R.id.recommendations_alternative_distance);
                RatingBar rating = (RatingBar) alternativeView.findViewById(R.id.recommendations_alternative_rating);
                FloatingActionButton choose = (FloatingActionButton) alternativeView.findViewById(R.id.recommendations_alternative_choose);

                title.setText(pauseLink.getTitle());
                if (rh.getETA() != null) {
                    eta.setText(dateFormat.format(rh.getETA()));
                }
                if (rh.getDistanceFromStart() != 0) {
                    distance.setText(String.format(Locale.GERMAN, "%.1f", rh.getDistanceFromStart() / 1000f));
                }
                rating.setRating((float) rh.getRating());
                choose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        chooseAlternativeRoadhouse(index);
                    }
                });
            }
            return alternativeView;
        }
    };

    /**
     * Sets main roadhouse. Called when the user chose one of the recommendations either in the main or in the map fragment
     *
     * @param entry the entry
     * @param rh    the rh
     */
    public void setMainRoadhouse(WheelEntry entry, Roadhouse rh) {
        highlightEntry(entry);
        // if the user chose the main roadhouse, a recalculation is not necessary (nothing changes), otherwise: recalculate route
        if (rh != entry.getPause().getMainRoadhouse()) {
            Break pause = entry.getPause();
            Roadhouse prevMainRh = pause.getMainRoadhouse();
            int prevAltRhIndex = pause.getAlternativeRoadhouses().indexOf(rh);
            pause.setMainRoadhouse(pause.getAlternativeRoadhouses().get(prevAltRhIndex));
            pause.getAlternativeRoadhouses().set(prevAltRhIndex, prevMainRh);

            // notify map fragment that the main roadhouse changed
            listener.onMainFragmentRoadhouseChanged(entry);
            // update recommendation views
            setRecommendations(null);

            DispoInformation.DestinationPoint formerDestinationPoint = pause.getDestinationPoint();
            pause.setDestinationPoint(new DispoInformation.DestinationPoint(GeoHelper.GeoCoordinateToLatLng(pause.getMainRoadhouse().getPlaceLink().getPosition()), 0));

            progressDialog.setTitle(R.string.loading_journey_data_title);
            progressDialog.setMessage(getString(R.string.updating_route_data_msg));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });

            final Journey journey = MainActivity.getCurrentJourney();
            if (!journey.getDestinationPoints().contains(pause.getDestinationPoint())) {
                journey.addDestinationPoint(pause.getDestinationPoint());
                journey.removeDestinationPoint(formerDestinationPoint);
            }
            // UPDATE ROUTE!
            RouteWrapper.getOrderedWaypoints(journey.getStartPoint(), journey.getDestinationPoints(), null, new AsyncResponse<ArrayList>() {
                @Override
                public void processFinish(ArrayList orderedDestinationPoints) {
                    journey.setDestinationPoints(orderedDestinationPoints);
                    activity.calculateRoute(journey.getStartPoint(), journey.getDestinationPoints(), progressDialog, new AsyncResponse<RouteWrapper>() {
                        @Override
                        public void processFinish(RouteWrapper routeWrapper) {
                            updateEntryPositions(routeWrapper);
                            setRecommendations(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            });
                            listener.onRouteChanged(routeWrapper);
                            progressDialog.dismiss();
                        }
                    });
                }
            });

        }

    }

    /**
     * onTruckMoved-Event is called each second, while the truck is driving. It sets the updated rotation angle of the
     * chart, of the canvas overlay and of the inner clock image.
     */
    public void onTruckMoved() {
        float diffAngle = getAngle() - autoUpdateArcAngle;
        autoUpdateArcAngle -= diffAngle;

        // rotate the chart and the clock
        mChart.setRotationAngle(autoUpdateArcAngle);
        canvas.setRotation(autoUpdateArcAngle);

        // rotate the clock
        final RotateAnimation a = new RotateAnimation(mChart.getRotationAngle() + 90 + clockOffsetAngle, mChart.getRotationAngle() + 90 + clockOffsetAngle - diffAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        a.setFillEnabled(true);
        a.setFillAfter(true);
        a.setInterpolator(interpolator);

        canvas.setArcAngle((float) (canvas.getArcAngle() + (MainActivity.VELOCITY_FACTOR * 10 / SECONDS_PER_DAY) * 360));
        refresh.post(new Runnable() {
            @Override
            public void run() {
                if (autoUpdateWheelAngle) {
                    mChart.invalidate();
                    canvas.invalidate();
                    clock.startAnimation(a);
                }
            }
        });
    }

    private void setRecommendations(int index, final Integer item) {
        WheelEntry entry = (WheelEntry) dataSet.getEntryForIndex(index);
        final Break pause = entry.getPause();

        // set main recommendation
        if (pause.getMainRoadhouse() != null) {
            listener.onMainFragmentRoadhouseChanged((WheelEntry) dataSet.getEntryForIndex(index));
            PlaceLink placeLink = pause.getMainRoadhouse().getPlaceLink();
            mainRecTitle.setText(placeLink.getTitle());
            mainRecETA.setText(dateFormat.format(pause.getMainRoadhouse().getETA()));
            mainRecDistance.setText(String.format(Locale.GERMAN, "%.1f", pause.getMainRoadhouse().getDistanceFromStart() / 1000f));
            mainRecBreaktime.setText((int) (dataSet.getEntryForIndex(index).getVal() / 60) + " min");
            mainRecRating.setRating((float) pause.getMainRoadhouse().getRating());
        }

        final ArrayList<GeoCoordinate> waypoints = new ArrayList<>();
        for (Roadhouse rh : pause.getAlternativeRoadhouses()) {
            waypoints.add(rh.getPlaceLink().getPosition());
        }

        // calculate distances and ETA of alternative waypoints asynchronously (using a waypoint matrix)
        dc.getWaypointMatrix(GeoHelper.LatLngToGeoCoordinate(MainActivity.getCurrentJourney().getStartPoint().getCoordinate()), waypoints, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject output) {

                try {
                    JSONArray entries = output.getJSONObject("response").getJSONArray("matrixEntry");
                    for (int i = 0; i < entries.length(); i++) {
                        int index = entries.getJSONObject(i).getInt("destinationIndex");
                        pause.getAlternativeRoadhouses().get(index).setDurationFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime"));
                        pause.getAlternativeRoadhouses().get(index).setDistanceFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("distance"));
                        pause.getAlternativeRoadhouses().get(index).setETA(new Date(System.currentTimeMillis() + entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime") * 1000));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // update the alternative recommendations view
                setupCarousel(item);
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        });

        // calculate mean gasprices for each gas station near the position of the main roadhouse, and also retrieve
        // the gas price of the corresponding main roadhouse
        dc.getGasPrices(pause.getMainRoadhouse().getPlaceLink().getPosition().getLatitude(), pause.getMainRoadhouse().getPlaceLink().getPosition().getLongitude(), 20, DataCollector.ORDER_BY_DISTANCE_DESC, -1, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    JSONArray stations = result.getJSONArray("stations");
                    double totalPrices = 0;
                    for (int i = 0; i < stations.length(); i++) {
                        totalPrices += stations.getJSONObject(i).getDouble("price");
                        if (stations.getJSONObject(i).getDouble("dist") == 0) {
                            pause.getMainRoadhouse().setGasPrice(stations.getJSONObject(i).getDouble("price"));
                        }
                    }
                    pause.setMeanGasPrice(totalPrices / stations.length());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // set main roadhouse gas properties
                if (pause.getMainRoadhouse().getGasPrice() != 0) {
                    mainRecGasPrice.setText(String.format(Locale.GERMAN, "%1.3f €", pause.getMainRoadhouse().getGasPrice()));
                    if (pause.getMainRoadhouse().getGasPrice() > pause.getMeanGasPrice()) {
                        mainRecGasImg.setImageResource(R.drawable.icon_gas_red);
                    } else {
                        mainRecGasImg.setImageResource(R.drawable.icon_gas_green);
                    }
                    // notify map that main roadhouse updated
                    listener.onMainFragmentRoadhouseChanged(selectedEntry);
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(TAG, error.getMessage());
            }
        });

        setupCarousel(item);
    }

    private void setRecommendations(final Integer item) {
        if (selectedEntry != null) {
            listener.onMainFragmentRoadhouseChanged(selectedEntry);
            final Break pause = selectedEntry.getPause();
            if (pause.getMainRoadhouse() != null) {
                PlaceLink placeLink = pause.getMainRoadhouse().getPlaceLink();
                mainRecTitle.setText(placeLink.getTitle());
                if (pause.getMainRoadhouse().getETA() != null) {
                    mainRecETA.setText(dateFormat.format(pause.getMainRoadhouse().getETA()));
                } else {
                    mainRecETA.setText("n/a");
                }
                if (pause.getMainRoadhouse().getDistanceFromStart() != 0) {
                    mainRecDistance.setText(String.format(Locale.GERMAN, "%.1f", pause.getMainRoadhouse().getDistanceFromStart() / 1000f));
                } else {
                    mainRecDistance.setText("n/a"); // TODO
                }
                mainRecBreaktime.setText((int) (selectedEntry.getVal() / 60) + " min");
                mainRecRating.setRating((float) pause.getMainRoadhouse().getRating());
            }


            final ArrayList<GeoCoordinate> waypoints = new ArrayList<>();
            for (Roadhouse rh : pause.getAlternativeRoadhouses()) {
                waypoints.add(rh.getPlaceLink().getPosition());
            }

            dc.getWaypointMatrix(GeoHelper.LatLngToGeoCoordinate(MainActivity.getCurrentJourney().getStartPoint().getCoordinate()), waypoints, new ResponseCallback() {
                @Override
                public void onSuccess(JSONObject output) {

                    try {
                        JSONArray entries = output.getJSONObject("response").getJSONArray("matrixEntry");
                        for (int i = 0; i < entries.length(); i++) {
                            int index = entries.getJSONObject(i).getInt("destinationIndex");
                            pause.getAlternativeRoadhouses().get(index).setDurationFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime"));
                            pause.getAlternativeRoadhouses().get(index).setDistanceFromStart(entries.getJSONObject(i).getJSONObject("summary").getInt("distance"));
                            pause.getAlternativeRoadhouses().get(index).setETA(new Date(System.currentTimeMillis() + entries.getJSONObject(i).getJSONObject("summary").getInt("travelTime") * 1000));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setupCarousel(item);
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e(TAG, error.getMessage());
                }
            });

            setupCarousel(item);
        }

    }

    private void loadAllDetailInfosInBackground() {
        // load details for first and second break (1st & 3rd wheel entry)
        loadDetailInfosInBackground(1);
        loadDetailInfosInBackground(3);
    }

    private void loadDetailInfosInBackground(int index) {
        WheelEntry entry = (WheelEntry) dataSet.getEntryForIndex(index);
        final Roadhouse mainRoadhouse = entry.getPause().getMainRoadhouse();
        mainRoadhouse.setDetailsLoading(true);
        mainRoadhouse.getPlaceLink().getDetailsRequest().execute(new ResultListener<Place>() {
            @Override
            public void onCompleted(Place place, ErrorCode errorCode) {
                mainRoadhouse.setPlace(place);
                mainRoadhouse.onDetailsLoaded();
            }
        });
    }

    private void setupCarousel(Integer item) {
        /*
        set alternative recommendations view
        The carousel is based on the viewpager, but the problem of the viewpager is, that it's very hard to dynamically
        change the contents. So each time we want to update it, we remove it and create it again.  ¯\_(ツ)_/¯
        */
        altRecwrapper.removeAllViews();
        altRecwrapper.addView(getActivity().getLayoutInflater().inflate(R.layout.carousel, null));
        carouselView = (CarouselView) altRecwrapper.findViewById(R.id.carouselView);
        carouselView.setStrokeColor(ContextCompat.getColor(activity, R.color.colorAccent));
        carouselView.setFillColor(ContextCompat.getColor(activity, R.color.colorAccent));
        carouselView.setViewListener(carouselViewListener);
        carouselView.setPageCount(selectedEntry != null ? selectedEntry.getPause().getAlternativeRoadhouses().size() : 0);
        if (item != null) {
            carouselView.setCurrentItem(item);
        }
    }

    private void chooseAlternativeRoadhouse(int index) {
        // called when the user chose a roadhouse from the alternatives,replacing the former main roadhouse (which moves
        // into the alternatives list
        if (selectedEntry != null) {
            Roadhouse mainRoadhouse = selectedEntry.getPause().getMainRoadhouse();
            Roadhouse alternativeRoadhouse = selectedEntry.getPause().getAlternativeRoadhouses().get(index);
            selectedEntry.getPause().setMainRoadhouse(alternativeRoadhouse);
            selectedEntry.getPause().getAlternativeRoadhouses().set(index, mainRoadhouse);

            // update view
            setRecommendations(index);

            // Update Route
            Break pause = selectedEntry.getPause();
            DispoInformation.DestinationPoint formerDestinationPoint = pause.getDestinationPoint();
            pause.setDestinationPoint(new DispoInformation.DestinationPoint(GeoHelper.GeoCoordinateToLatLng(pause.getMainRoadhouse().getPlaceLink().getPosition()), 0));

            progressDialog.setTitle(R.string.loading_journey_data_title);
            progressDialog.setMessage(getString(R.string.updating_route_data_msg));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });

            final Journey journey = MainActivity.getCurrentJourney();
            if (!journey.getDestinationPoints().contains(pause.getDestinationPoint())) {
                journey.addDestinationPoint(pause.getDestinationPoint());
                journey.removeDestinationPoint(formerDestinationPoint);
            }
            // UPDATE ROUTE!
            RouteWrapper.getOrderedWaypoints(journey.getStartPoint(), journey.getDestinationPoints(), null, new AsyncResponse<ArrayList>() {
                @Override
                public void processFinish(ArrayList orderedDestinationPoints) {
                    journey.setDestinationPoints(orderedDestinationPoints);
                    activity.calculateRoute(journey.getStartPoint(), journey.getDestinationPoints(), progressDialog, new AsyncResponse<RouteWrapper>() {
                        @Override
                        public void processFinish(RouteWrapper routeWrapper) {
                            updateEntryPositions(routeWrapper);
                            setRecommendations(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                }
                            });
                            progressDialog.dismiss();
                        }
                    });
                }
            });
        }
    }

    private void rotateViews(float diffAngle) {
        canvas.setRotation(mChart.getRawRotationAngle());

        Animation canvasRotation = new RotateAnimation(0, diffAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        canvasRotation.setInterpolator(interpolator);
        canvasRotation.setFillAfter(true);
        canvasRotation.setFillEnabled(true);
        canvas.startAnimation(canvasRotation);

        Animation clockRotation = new RotateAnimation(mChart.getRotationAngle() + 90 + clockOffsetAngle, mChart.getRotationAngle() + 90 + clockOffsetAngle + diffAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        clockRotation.setInterpolator(interpolator);
        clockRotation.setFillAfter(true);
        clock.startAnimation(clockRotation);
    }

    private boolean isInToleratedDistance(float x, float y, PointF center, float radius) {
        float holeRadius = mChart.getHoleRadius();

        radius *= (holeRadius + (100 - mChart.getHoleRadius()) / 2) / 100;
        // Add touch tolerance
        double innerRadius = radius * (1 - ENTRY_LONGPRESS_TOLERANCE);
        double outerRadius = radius * (1 + ENTRY_LONGPRESS_TOLERANCE);

        // Get distance of touched point to center with pythagoras
        double dist = Math.sqrt(Math.pow(center.x - x, 2) + Math.pow(center.y - y, 2));
        return dist >= innerRadius && dist <= outerRadius;
    }

    private int getAccumulatedValue(int entryIndex) {
        int value = 0;
        for (int i = 0; i < entryIndex; i++) {
            value += dataSet.getEntryForIndex(i).getVal();
        }
        return value;
    }

    private void addEntry(int index, int size, int type) {
        // add entry to the piechart and update all datasets
        int nBreaks = 0;
        int elapsedTime = 0;

        for (int i = 0; i < entries.size(); i++) {
            WheelEntry prevEntry = (WheelEntry) entries.get(i);

            if (i < index) {
                elapsedTime += prevEntry.getVal();
                if (prevEntry.getEntryType() == WheelEntry.PAUSE_ENTRY) {
                    nBreaks++;
                }
            } else {
                prevEntry.setXIndex(i + 1);
                if (prevEntry.getEntryType() == WheelEntry.PAUSE_ENTRY && i > index) {
                    prevEntry.getPause().setIndex(prevEntry.getPause().getIndex() + 1);
                }
            }
        }
        if (type == WheelEntry.PAUSE_ENTRY) {
            WheelEntry entry = new WheelEntry(size, index, type, elapsedTime, nBreaks, false);
            entry.setPause(new Break(((WheelEntry) (dataSet.getEntryForIndex(index))).getPause()));
            entry.getPause().setWheelEntry(entry);
            entry.getPause().setIndex(nBreaks);
            Break.addBreak(entry.getPause());
            entries.add(index, entry);
        } else {
            entries.add(index, new WheelEntry(size, index, type, elapsedTime));
        }

        dataSet.getColors().add(index, COLORS.get(type));
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void removeEntry(int index) {
        // remove entry from piechart and update all datasets
        dataSet.removeEntry(dataSet.getEntryForIndex(index));
        dataSet.getColors().remove(index);

        for (int i = index; i < entries.size(); i++) {
            WheelEntry entry = (WheelEntry) dataSet.getEntryForIndex(i);
            entry.setXIndex(i);
            if (entry.getEntryType() == PAUSE_ENTRY) {
                entry.getPause().setIndex(i - 1);
            }
        }
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private float getAngle() {
        return (float) (((((autoUpdateArcAngle / 360) * SECONDS_PER_DAY) + MainActivity.VELOCITY_FACTOR * 10) / SECONDS_PER_DAY) * 360);
    }

    private void highlightEntry(WheelEntry entry) {
        selectedEntry = entry;
        mChart.highlightValue(entry.getXIndex(), 0);
    }

    /**
     * Gets next break.
     *
     * @return the next break
     * @// FIXME: 24.07.16 always returns the first break by default
     */
    public WheelEntry getNextBreak() {
        return (WheelEntry) dataSet.getEntryForIndex(1);
    }

    /**
     * Sets previous break.
     */
    public void setPrevBreak() {
        int index = mChart.getIndexForAngle(currentTimeAngle);
        for (int i = index; i >= 0; i--) {
            WheelEntry entry = (WheelEntry) dataSet.getEntryForIndex(i);
            if (entry.getEntryType() == PAUSE_ENTRY) {
                previousBreakEntry = entry;
                return;
            }
        }
    }

    /**
     * Gets previous break.
     *
     * @return the prev break
     * @// FIXME: 24.07.16 always returns the first break by default
     */
    public WheelEntry getPrevBreak() {
        // return previousBreakEntry;
        return (WheelEntry) dataSet.getEntryForIndex(1);
    }

    /**
     * Event, which is called after the driver finished his break
     */
    public void onBreakFinished() {
        setPrevBreak();
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        // when the piechart is finally layouted, we can set the canvas overlay and the inncer clock.

        // indicates the ratio of the 5-minute indicators in the clock image relative to the total clock image radius 
        float minuteIndicatorRatio = 27f / 403f;
        // the radius of the inner circle of the pie chart
        double innerCircleRadius = mChart.getHoleRadius() * .01 * mChart.getRadius();
        // the width of the piechart entries
        double pieElementsWidth = mChart.getRadius() - innerCircleRadius;
        // the radius of the canvas overlay circle
        float radius = (float) (mChart.getRadius() - pieElementsWidth / 2);

        // set canvas overlay
        canvas.setBoundingBox(new RectF(mChart.getCenter().x - radius, mChart.getCenter().y - radius, mChart.getCenter().x + radius, mChart.getCenter().y + radius));
        canvas.setStrokeWidth(pieElementsWidth);
        canvas.invalidate();

        // set clock dimenions
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int) (2 * innerCircleRadius + 2 * minuteIndicatorRatio * innerCircleRadius), (int) (2 * innerCircleRadius + 2 * minuteIndicatorRatio * innerCircleRadius));
        params.setMargins((int) (mChart.getCenter().x - innerCircleRadius - (minuteIndicatorRatio * innerCircleRadius)), (int) (mChart.getCenter().y - innerCircleRadius - (minuteIndicatorRatio * innerCircleRadius)), (int) (mChart.getCenter().x + innerCircleRadius + (minuteIndicatorRatio * innerCircleRadius)), (int) (mChart.getCenter().y + innerCircleRadius + (minuteIndicatorRatio * innerCircleRadius)));
        clock.setLayoutParams(params);
        clock.setScaleType(ImageView.ScaleType.FIT_CENTER);
        clock.invalidate();
    }

}

