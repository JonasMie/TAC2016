package tac.android.de.truckcompanion.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.*;
import android.widget.*;
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
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.Journey;
import tac.android.de.truckcompanion.data.Roadhouse;
import tac.android.de.truckcompanion.dispo.DispoInformation;
import tac.android.de.truckcompanion.geo.GeoHelper;
import tac.android.de.truckcompanion.geo.RouteWrapper;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.utils.OnRoadhouseSelectedListener;
import tac.android.de.truckcompanion.wheel.OnEntryGestureListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static tac.android.de.truckcompanion.wheel.WheelEntry.COLORS;
import static tac.android.de.truckcompanion.wheel.WheelEntry.PAUSE_ENTRY;


/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:37
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MainFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener, OnEntryGestureListener {

    // Chart-related members
    private LinearLayout recommendationsWrapper;
    private RelativeLayout chartWrapper;
    private PieChart mChart;
    private PieDataSet dataSet;
    private PieData data;
    private ArrayList<Entry> entries;
    private float mStartAngle = 0;
    private PointF mTouchStartPoint = new PointF();

    // View-realted members
    private RelativeLayout mainRecWrapper;
    RelativeLayout altRecwrapper;
    private TextView mainRecTitle;
    private TextView mainRecRatingLabel;
    private TextView mainRecETA;
    private TextView mainRecDistance;
    private TextView mainRecBreaktime;
    private RatingBar mainRecRating;
    private CarouselView carouselView;


    // Logic data
    private WheelEntry selectedEntry;
    private int totalBreaks;

    // Misc
    OnRoadhouseSelectedListener listener;
    Vibrator vibrator;
    MainActivity activity;
    ProgressDialog progressDialog;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.GERMAN);
    int NUMBER_OF_PAGES = 5;

    // Constants
    private static final String LOG = "TAC";
    private static final double ENTRY_LONGPRESS_TOLERANCE = .2;
    public static final float SECONDS_PER_DAY = 24 * 60 * 60;
    public static final int FIRST_SPLIT = 15 * 60;
    public static final int SECOND_SPLIT = 30 * 60;
    public static final int COMPLETE_BREAK = 45 * 60;
    public static final int MIN_TIME_BETWEEN_BREAKS = 10 * 60;
    public static final float MAX_BUFFER_VAL = 270 * 60 - MIN_TIME_BETWEEN_BREAKS;
    public static final float MAX_DRIVE_VAL = 270 * 60;
    public static final int RECALCULATION_STEP = 5;
    public static final int MAX_DRIVER_TOLERANCE = 10 * 60;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        mainRecWrapper = (RelativeLayout) view.findViewById(R.id.recommendations_main_wrapper);
        altRecwrapper = (RelativeLayout) view.findViewById(R.id.recommendations_alternatives_wrapper);

        chartWrapper = (RelativeLayout) view.findViewById(R.id.chartWrapper);

        recommendationsWrapper = (LinearLayout) view.findViewById(R.id.recommendationsWrapper);
        mainRecTitle = (TextView) recommendationsWrapper.findViewById(R.id.recommendations_main_title);
        mainRecETA = (TextView) recommendationsWrapper.findViewById(R.id.recommendations_main_info_eta);
        mainRecDistance = (TextView) recommendationsWrapper.findViewById(R.id.recommendations_main_info_distance);
        mainRecBreaktime = (TextView) recommendationsWrapper.findViewById(R.id.recommendations_main_info_breaktime);
        mainRecRatingLabel = (TextView) recommendationsWrapper.findViewById(R.id.recommendations_main_rating_label);
        mainRecRating = (RatingBar) recommendationsWrapper.findViewById(R.id.recommendations_main_rating);


        mChart = (PieChart) view.findViewById(R.id.chart);


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                width,
                (int) (height * .5)
        );
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                width,
                (int) (height * 1.5)
        );

        recommendationsWrapper.setLayoutParams(params1);
        chartWrapper.setLayoutParams(params2);

        activity = ((MainActivity) getActivity());
        progressDialog = new ProgressDialog(activity);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        setupFragment();

        // Layout + appearance
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(80f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setLogEnabled(true);

        // Listener
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        // Hide Lables, Legend, ...
        mChart.setDescription("");
        mChart.setDrawSliceText(false);
        mChart.getLegend().setEnabled(false);
        return view;
    }

    public void setupFragment() {
        entries = WheelEntry.getEntries();

        dataSet = new PieDataSet(entries, "Fahrtzeiten");

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < 11; i++) {
            xVals.add("");
        }

        data = new PieData(xVals, dataSet);
        mChart.setData(data);

        dataSet.setColors(WheelEntry.getColors(entries));
        data.setDrawValues(false);

        // Highlight 1st pause by default
        mChart.highlightValue(1, 0);
        selectedEntry = (WheelEntry) dataSet.getEntryForIndex(1);

        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    public void setBreaks(final RouteWrapper routeWrapper, final ProgressDialog mProgressDialog, final AsyncResponse<ArrayList> callback) {
        mProgressDialog.setMessage(getString(R.string.loading_pause_data_msg));
        final ArrayList<Break> breaks = Break.getBreaks();
        totalBreaks = breaks.size();
        final ArrayList<DispoInformation.DestinationPoint> destinationPoints = MainActivity.getmCurrentJourney().getDestinationPoints();
        breaks.get(0).calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(breaks.get(0).getElapsedTime()), routeWrapper.getRoute().getStart(), 0, new AsyncResponse<Break>() {
            @Override
            public void processFinish(Break pause) {
                PlaceLink pauseLink = pause.getMainRoadhouse().getPlaceLink();
                GeoCoordinate pos = pauseLink.getPosition();
                destinationPoints.add(new DispoInformation.DestinationPoint(GeoHelper.GeoCoordinateToLatLng(pos), 15));

                breaks.get(1).calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(breaks.get(1).getElapsedTime()), pos, 1, new AsyncResponse<Break>() {
                    @Override
                    public void processFinish(Break output) {
                        PlaceLink pauseLink = output.getMainRoadhouse().getPlaceLink();
                        GeoCoordinate pos = pauseLink.getPosition();
                        destinationPoints.add(new DispoInformation.DestinationPoint(GeoHelper.GeoCoordinateToLatLng(pos), 15));

                        RouteWrapper.getOrderedWaypoints(MainActivity.getmCurrentJourney().getStartPoint(), MainActivity.getmCurrentJourney().getDestinationPoints(), null, new AsyncResponse<ArrayList>() {
                            @Override
                            public void processFinish(ArrayList orderedDestinationPoints) {
                                MainActivity.getmCurrentJourney().setDestinationPoints(orderedDestinationPoints);
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

        if (lastPerformedGesture == ChartTouchListener.ChartGesture.ROTATE) {
            float pointAngle = mChart.getAngleForPoint(me.getX(), me.getY());
            float diffAngle = pointAngle - mStartAngle;
            // this is fixing the undesired behaviour that the pause jumps around when the entry crosses the 0°-mark
            if (diffAngle > 180) {
                diffAngle = -360;
            }
            if (mChart.isEditModeEnabled()) {
                if (lastPerformedGesture == ChartTouchListener.ChartGesture.ROTATE) {
                    onEntryDragged(me, diffAngle);
                }
            }
            // diff-angle is incremental, but we only need the difference regarding the last change, so adapt startAngle
            mStartAngle += diffAngle;
        }
        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP && lastPerformedGesture != ChartTouchListener.ChartGesture.LONG_PRESS) {
            if (!mChart.isEditModeEnabled()) {
                mChart.highlightValues(null);
                mChart.setRotationEnabled(true);
                if (selectedEntry != null) {
                    selectedEntry.setEditModeActive(false);
                    selectedEntry = null;
                }
            }
        }
    }

    public void rotateIcons(float x, float y) {
//        float dif = mChart.getAngleForPoint(x, y) - mStartAngle;
//        mStartAngle = mChart.getAngleForPoint(x, y);
//        for (ImageView icon : symbols) {
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) icon.getLayoutParams();
//            float oldAngle = mChart.getAngleForPoint(icon.getX(), icon.getY());
//            float newAngle = oldAngle + dif;
//            PointD point = getPoint(mChart.getCenter(), mChart.getRadius(), newAngle);
//            params.leftMargin = (int) (point.x);
//            params.topMargin = (int) (point.y);
//            icon.setLayoutParams(params);
//        }
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

        // If another element is still selected, deselect it
//        this.onNothingSelected();

        // The user can only modify entries, which represent a pause. Modifying a driving period is not possible.
        if (!(longPressedEntry.getEntryType() == WheelEntry.PAUSE_ENTRY)) {
            return;
        }
        /*
         Since the listener listens for events on the whole chart, it is possible, to select the entries within a much
         larger radius. But this is not desired, it would confuse the user and lead to user-unexpected behaviour.
         So the longpress-event is limited to the dimensions of the entry plus a tolerance of eg 10%
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

//        updateColor(index, Color.GREEN);
        mChart.setRotationEnabled(false);
        mChart.setEditModeEnabled(true);
        /*
        Visuals: Indicate, that the user can actually modify this entry.
        TODO: how to visualize the selected entry, so that the user understands he can modify it (intuitively)
        e.g.
        Animation shake = AnimationUtils.loadAnimation(this.getContext(), R.anim.shake);
        mChart.setAnimation(shake);
         */

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
        mChart.setRotationEnabled(false);
        selectedEntry = (WheelEntry) dataSet.getEntryForIndex(dataSetIndex);

        if (((WheelEntry) e).getEntryType() == PAUSE_ENTRY) {
            listener.onMainFragmentRoadhouseChanged(selectedEntry);
        }
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
        mChart.setRotationEnabled(true);
        mChart.setEditModeEnabled(false);
    }

    private boolean isInToleratedDistance(float x, float y, PointF center, float radius) {
        float holeRadius = mChart.getHoleRadius();
        // Compute actual angle (in radian, considering MPAndroidChart begins drawing charts at 90°)
        //double angle_ = (angle * Math.PI / 180) + 270;

        radius *= (holeRadius + (100 - mChart.getHoleRadius()) / 2) / 100;
        // Add touch tolerance
        double innerRadius = radius * (1 - ENTRY_LONGPRESS_TOLERANCE);
        double outerRadius = radius * (1 + ENTRY_LONGPRESS_TOLERANCE);

        // Get distance of touched point to center with pythagoras
        double dist = Math.sqrt(Math.pow(center.x - x, 2) + Math.pow(center.y - y, 2));

        return dist >= innerRadius && dist <= outerRadius;
    }

    private static float distance(float eventX, float startX, float eventY, float startY) {
        float dx = eventX - startX;
        float dy = eventY - startY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void onEntryDragged(MotionEvent me, float diffAngle) {
        Log.i("ENTRY:DRAG", "Entry dragged");
        WheelEntry entry = WheelEntry.getActiveEntry();
        boolean splitBreak = false;
        boolean mergeBreak = false;

        if (entry != null) {
            int nEntries = mChart.getXValCount();
            int entryIndex = entry.getXIndex();

            // split the break
            if (entry.getVal() == COMPLETE_BREAK) {
                if (diffAngle < 0) {
                    splitBreak = true;
                    addEntry(entryIndex, FIRST_SPLIT, WheelEntry.PAUSE_ENTRY);
                    ((WheelEntry) (dataSet.getEntryForIndex(entryIndex))).setEditModeActive(true);
                    mChart.highlightValue(entryIndex, 0);
                    entry.setVal(SECOND_SPLIT);
                    addEntry(entryIndex + 1, 0, WheelEntry.BUFFER_ENTRY);
                } else {
                    return;
                }
            }

            WheelEntry bufferEntry = (WheelEntry) dataSet.getEntryForIndex(entryIndex + 1);
            WheelEntry driveEntry = (WheelEntry) dataSet.getEntryForIndex(entryIndex - 1);
            WheelEntry pauseEntry = (WheelEntry) dataSet.getEntryForIndex(entryIndex);

            if (splitBreak) {
                MainActivity.getmCurrentJourney().addDestinationPoint(pauseEntry.getPause().getDestinationPoint());
                pauseEntry.setStepAngle(RECALCULATION_STEP * (Math.round(mStartAngle / RECALCULATION_STEP)));
            }
            double ratio = diffAngle / 360;

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
                mergeBreak = true;
                pauseEntry.setVal(COMPLETE_BREAK);
                removeEntry(entryIndex + 1);
                removeEntry(entryIndex + 1);
                // Vibrate to provide haptic feedback
                vibrator.vibrate(50);
            }

            mChart.notifyDataSetChanged();
            mChart.invalidate();

            // (Re-)Calculate breaks
            float roundedDiff = RECALCULATION_STEP * (Math.round(mStartAngle / RECALCULATION_STEP));
            if (Math.abs(mStartAngle - pauseEntry.getStepAngle()) > RECALCULATION_STEP) {
//                activity.showDialog(R.string.loading_journey_data_title, R.string.updating_pause_data_msg, ProgressDialog.STYLE_SPINNER, false);
                progressDialog.setTitle(R.string.loading_journey_data_title);
                progressDialog.setMessage(getString(R.string.updating_pause_data_msg));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.show();
                Break pause = pauseEntry.getPause();
                pauseEntry.setStepAngle(RECALCULATION_STEP * Math.round(mStartAngle / RECALCULATION_STEP));
                final boolean finalSplitBreak = splitBreak;
                final boolean finalMergeBreak = mergeBreak;
                final DispoInformation.DestinationPoint formerDestinationPoint = pause.getDestinationPoint();

                GeoCoordinate refPoint;

                if (pause.getIndex() == 0) {
                    // entry is first entry
                    refPoint = MainActivity.getmCurrentJourney().getRouteWrapper().getRoute().getStart();
                } else {
                    // TODO
                    refPoint = ((WheelEntry) mChart.getEntriesAtIndex(entryIndex).get(entryIndex - 2)).getPause().getMainRoadhouse().getPlaceLink().getPosition();
                }
                final Break[] formerPause = {pause};
                pause.update(getAccumulatedValue(entryIndex), refPoint, pauseEntry.getPause().getIndex(), new AsyncResponse<Break>() {
                    @Override
                    public void processFinish(Break pause) {
                        Log.i(LOG, "Roadhouse updated. New Roadhouse");
                        Break.removeBreak(formerPause[0]);
                        formerPause[0] = pause;
                        Break.addBreak(pause);

                        progressDialog.setMessage(getString(R.string.updating_route_data_msg));
                        final Journey journey = MainActivity.getmCurrentJourney();
                        PlaceLink mainRoadhouse = pause.getMainRoadhouse().getPlaceLink();
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
                });
            }
        }
    }

    private int getAccumulatedValue(int entryIndex) {
        int value = 0;
        for (int i = 0; i < entryIndex; i++) {
            value += dataSet.getEntryForIndex(i).getVal();
        }
        return value;
    }

    private double getArc(double radius, double diffAngle) {
        return radius * Math.PI * (diffAngle / 180);
    }

    @Override
    public void onEntryResized(MotionEvent me) {

    }

    private void addEntry(int index, int size, int type) {
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

    private int elapsedTimeForEntry(int index) {
        int elapsedTime = 0;
        for (int i = 0; i < index; i++) {
            elapsedTime += mChart.getEntriesAtIndex(i).get(0).getVal();
        }
        Break.removeBreak(index);
        return elapsedTime;
    }


    private void updateColor(int index, int green) {
        List<Integer> colors = dataSet.getColors();
        Integer color = colors.get(index);
        dataSet.setColors(colors);
    }

    private void updateProgressDialog(final ProgressDialog dialog, final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage(msg);
            }
        });
    }

    public void updateEntryPositions(RouteWrapper routeWrapper) {
        Route route = routeWrapper.getRoute();
        List<Maneuver> maneuvers = route.getManeuvers();
        ArrayList<DispoInformation.DestinationPoint> destinationPoints = MainActivity.getmCurrentJourney().getDestinationPoints();
        ArrayList<Break> breaks = Break.getBreaks();
        long timeSinceLastStop = routeWrapper.getDepartureTime().getTime();
        for (int i = 0; i < maneuvers.size(); i++) {
            if (maneuvers.get(i).getAction() == Maneuver.Action.STOPOVER || maneuvers.get(i).getAction() == Maneuver.Action.END) {
                for (Break pause : breaks) {
                    // Distance between these coordinates is less than 50 meters, so we can assume it's the same place
                    if (maneuvers.get(i).getCoordinate().distanceTo(pause.getMainRoadhouse().getPlaceLink().getPosition()) < 50) {
                        pause.getMainRoadhouse().setETA(maneuvers.get(i).getStartTime());
                        pause.getMainRoadhouse().setDurationFromStart((maneuvers.get(i).getStartTime().getTime() - timeSinceLastStop) / 1000);
//                        timeSinceLastStop = maneuvers.get(i).getStartTime().getTime();
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
            }
            prevWheelEntry = wheelEntry;
        }
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void setRecommendations(int index, Integer item) {
        Break pause = ((WheelEntry) dataSet.getEntryForIndex(index)).getPause();
        if (pause.getMainRoadhouse() != null) {
            listener.onMainFragmentRoadhouseChanged((WheelEntry) dataSet.getEntryForIndex(index));
            PlaceLink placeLink = pause.getMainRoadhouse().getPlaceLink();
            mainRecTitle.setText(placeLink.getTitle());
            mainRecETA.setText(dateFormat.format(pause.getMainRoadhouse().getETA()));
            mainRecDistance.setText("20 km"); // TODO
            mainRecBreaktime.setText((int) (dataSet.getEntryForIndex(index).getVal() / 60) + " min");
            mainRecRating.setRating((float) placeLink.getAverageRating());
        }
        setupCarousel(item);
    }

    private void setRecommendations(Integer item) {
        if (selectedEntry != null) {
            listener.onMainFragmentRoadhouseChanged(selectedEntry);
            Break pause = selectedEntry.getPause();
            if (pause.getMainRoadhouse() != null) {
                PlaceLink placeLink = pause.getMainRoadhouse().getPlaceLink();
                mainRecTitle.setText(placeLink.getTitle());
                if (pause.getMainRoadhouse().getETA() != null) {  // TODO
                    mainRecETA.setText(dateFormat.format(pause.getMainRoadhouse().getETA()));

                } else {
                    mainRecETA.setText("12:30");
                }
                mainRecDistance.setText("20 km"); // TODO
                mainRecBreaktime.setText((int) (selectedEntry.getVal() / 60) + " min");
                mainRecRating.setRating((float) placeLink.getAverageRating());
            }
            setupCarousel(item);
        }

    }

    public void onStartupTaskReady(RouteWrapper updatedRouteWrapper) {
        this.updateEntryPositions(updatedRouteWrapper);
        setRecommendations(1, null);
        loadAllDetailInfosInBackground();
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
        altRecwrapper.removeAllViews();
        altRecwrapper.addView(getActivity().getLayoutInflater().inflate(R.layout.carousel, null));
        carouselView = (CarouselView) altRecwrapper.findViewById(R.id.carouselView);
        carouselView.setViewListener(carouselViewListener);
        carouselView.setPageCount(selectedEntry != null ? selectedEntry.getPause().getAlternativeRoadhouses().size() : 0);
        if (item != null) {
            carouselView.setCurrentItem(item);
        }
    }

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
                Button choose = (Button) alternativeView.findViewById(R.id.recommendations_alternative_choose);

                title.setText(pauseLink.getTitle());
                eta.setText(dateFormat.format(rh.getDurationFromStart())); // TODO
                distance.setText("25 km"); // TODO
                rating.setRating((float) pauseLink.getAverageRating());
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

    private void chooseAlternativeRoadhouse(int index) {
        if (selectedEntry != null) {
            Roadhouse mainRoadhouse = selectedEntry.getPause().getMainRoadhouse();
            Roadhouse alternativeRoadhouse = selectedEntry.getPause().getAlternativeRoadhouses().get(index);
            selectedEntry.getPause().setMainRoadhouse(alternativeRoadhouse);
            selectedEntry.getPause().getAlternativeRoadhouses().set(index, mainRoadhouse);

            // update view
            setRecommendations(index);
        }
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

    public void setMainRoadhouse(WheelEntry entry) {
    }
}

