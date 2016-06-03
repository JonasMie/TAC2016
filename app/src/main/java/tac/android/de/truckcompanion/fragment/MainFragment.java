package tac.android.de.truckcompanion.fragment;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.wheel.OnEntryGestureListener;
import tac.android.de.truckcompanion.wheel.WheelEntry;

import java.util.ArrayList;


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
    private float mStartAngle = 0;
    private PointF mTouchStartPoint = new PointF();

    // Logic data
    private WheelEntry selectedEntry;

    // Constants
    private static final double ENTRY_LONGPRESS_TOLERANCE = .2;
    private static final float MINUTES_PER_DAY = 24 * 60;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mChart = (PieChart) view.findViewById(R.id.chart);


//        mRelativeLayout = (RelativeLayout) view.findViewById(R.id.rLayout);

//        Display display =  getActivity().getWindowManager().getDefaultDisplay();
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
//        mRelativeLayout.setLayoutParams(params2);

        ArrayList<Entry> entries = WheelEntry.getEntries();
        dataSet = new PieDataSet(entries, "Fahrtzeiten");

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("1. Lenkzeit");
        xVals.add("1. Pausenzeit");
        xVals.add("Buffer");
        xVals.add("2. Lenkzeit");
        xVals.add("2. Pausenzeit");
        xVals.add("Buffer");
        xVals.add("3. Lenkzeit");
        xVals.add("Ruhezeit");

        data = new PieData(xVals, dataSet);
        mChart.setData(data);

        // Layout + appearance
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(80f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setLogEnabled(true);
        dataSet.setColors(WheelEntry.getColors(entries));

        // Listener
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
//mChart.setRotationEnabled(false);
        return view;
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

        // Since the pause-entry is now selected, set it as current selected element (same behaviour as single-tap)
        WheelEntry prevActiveEntry = WheelEntry.getActiveEntry();
        if (prevActiveEntry != null) {
            prevActiveEntry.setEditModeActive(false);
        }
        selectedEntry = longPressedEntry;
        longPressedEntry.setEditModeActive(true);
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

            if (newDriveVal < 0) {
                driveEntry.setVal(0);
            } else if (newDriveVal > maxDriveVal) {
                driveEntry.setVal(maxDriveVal);
            } else {
                driveEntry.setVal(newDriveVal);
            }
            
            mChart.notifyDataSetChanged();
            mChart.invalidate();
        }
    }

    private double getArc(double radius, double diffAngle) {
        return radius * Math.PI * (diffAngle / 180);
    }

    @Override
    public void onEntryResized(MotionEvent me) {

    }
}
