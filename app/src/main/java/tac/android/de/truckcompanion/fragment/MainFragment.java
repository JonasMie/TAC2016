package tac.android.de.truckcompanion.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import tac.android.de.truckcompanion.MainActivity;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.data.Break;
import tac.android.de.truckcompanion.data.Journey;
import tac.android.de.truckcompanion.utils.AsyncResponse;
import tac.android.de.truckcompanion.wheel.OnEntryGestureListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static tac.android.de.truckcompanion.wheel.WheelEntry.COLORS;


/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:37
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MainFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener, OnEntryGestureListener {

    // Chart-related members
    private PieChart mChart;
    private PieDataSet dataSet;
    private PieData data;
    private ArrayList<Entry> entries;
    private float mStartAngle = 0;
    private PointF mTouchStartPoint = new PointF();

    // Logic data
    private WheelEntry selectedEntry;
    private int processedBreaks = 0;
    private int totalBreaks;

    // Misc
    Vibrator vibrator;
    // Constants
    private static final double ENTRY_LONGPRESS_TOLERANCE = .2;
    private static final float MINUTES_PER_DAY = 24 * 60;
    private static final int MIN_TIME_BETWEEN_BREAKS = 10;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mChart = (PieChart) view.findViewById(R.id.chart);

        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

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

    public void setupFragment(final ProgressDialog mProgressDialog) {
        mProgressDialog.setMessage(getString(R.string.loading_pause_data_msg));

        entries = WheelEntry.getEntries(mProgressDialog);

        dataSet = new PieDataSet(entries, "Fahrtzeiten");

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < 11; i++) {
            xVals.add("");
        }

        data = new PieData(xVals, dataSet);
        mChart.setData(data);

        dataSet.setColors(WheelEntry.getColors(entries));
        data.setDrawValues(false);

        mChart.notifyDataSetChanged();
        mChart.invalidate();

        ArrayList<Break> breaks = Break.getBreaks();
        totalBreaks = breaks.size();

        // WTF, fucking callbacks
        for (int i = 0; i < totalBreaks; i++) {
            breaks.get(i).calculateRoadhouses(MainActivity.getmCurrentJourney().getPositionOnRouteByTime(breaks.get(i).getElapsedTime()), i, new AsyncResponse<Break>() {
                @Override
                public void processFinish(Break output) {
                }

                @Override
                public void processFinish(Break output, Integer index) {
                    if (index + 1 == totalBreaks) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        processedBreaks = 0;
                    }
                }
            });
        }
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
//            if (distance(me.getX(), mTouchStartPoint.x, me.getY(), mTouchStartPoint.y)
//                    > Utils.convertDpToPixel(8f)) {
//                rotateIcons(me.getX(), me.getY());
//            }
            float diffAngle = mChart.getAngleForPoint(me.getX(), me.getY()) - mStartAngle;

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
        WheelEntry longPressedEntry = (WheelEntry) mChart.getEntriesAtIndex(index).get(0);

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

        updateColor(index, Color.GREEN);

        mChart.highlightValue(index, 0);
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
        selectedEntry = (WheelEntry) mChart.getEntriesAtIndex(dataSetIndex).get(0);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
        mChart.setRotationEnabled(true);
        mChart.setEditModeEnabled(true);
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
        float maxBufferVal = 270;
        float maxDriveVal = 270;

        if (entry != null) {
            int nEntries = mChart.getXValCount();
            int entryIndex = entry.getXIndex();

            // split the break
            if (entry.getVal() == 45.0) {
                if (diffAngle < 0) {
                    addEntry(entryIndex, 15, WheelEntry.PAUSE_ENTRY);
                    ((WheelEntry) (mChart.getEntriesAtIndex(entryIndex).get(0))).setEditModeActive(true);
                    mChart.highlightValue(entryIndex, 0);
                    entry.setVal(30);
                    addEntry(entryIndex + 1, 0, WheelEntry.BUFFER_ENTRY);
                } else {
                    return;
                }
            }

            WheelEntry bufferEntry = (WheelEntry) mChart.getEntriesAtIndex(entryIndex + 1).get(0);
            WheelEntry driveEntry = (WheelEntry) mChart.getEntriesAtIndex(entryIndex - 1).get(0);

            double ratio = diffAngle / 360;

            float newBufferVal = (float) (bufferEntry.getVal() - MINUTES_PER_DAY * ratio);
            float newDriveVal = (float) (driveEntry.getVal() + MINUTES_PER_DAY * ratio);

            if (newBufferVal < 0) {
                bufferEntry.setVal(0);
            } else if (newBufferVal > maxBufferVal) {
                bufferEntry.setVal(maxBufferVal);
            } else {
                bufferEntry.setVal(newBufferVal);
            }

            if (newDriveVal <= MIN_TIME_BETWEEN_BREAKS) {
                driveEntry.setVal(MIN_TIME_BETWEEN_BREAKS);
            } else if (newDriveVal > maxDriveVal) {
                driveEntry.setVal(maxDriveVal);
            } else {
                driveEntry.setVal(newDriveVal);
            }

            //  merge the breaks
            if (bufferEntry.getVal() == 0 && driveEntry.getVal() == maxDriveVal && entry.getVal() == 15) {
                entry.setVal(45);
                removeEntry(entryIndex + 1);
                removeEntry(entryIndex + 1);
                // Vibrate to provide haptic feedback
                vibrator.vibrate(50);
            }

            mChart.notifyDataSetChanged();
            mChart.invalidate();

            // Calculate breaks

        }
    }

    private double getArc(double radius, double diffAngle) {
        return radius * Math.PI * (diffAngle / 180);
    }

    @Override
    public void onEntryResized(MotionEvent me) {

    }

    private void addEntry(int index, int size, int type) {
        int nBreaks = 0;
        for (int i = index; i < entries.size(); i++) {
            WheelEntry prevEntry = (WheelEntry) entries.get(i);
            if (prevEntry.getEntryType() == WheelEntry.PAUSE_ENTRY) {
                nBreaks++;
            }
            prevEntry.setXIndex(i + 1);
        }
        entries.add(index, new WheelEntry(size, index, type, nBreaks));
        dataSet.getColors().add(index, COLORS.get(type));
        data.notifyDataChanged();
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void removeEntry(int index) {
        data.removeEntry(mChart.getEntriesAtIndex(index).get(0), 0);
        dataSet.getColors().remove(index);
        data.notifyDataChanged();
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
}
