package tac.android.de.truckcompanion.fragment;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
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
import com.google.android.gms.common.api.GoogleApiClient;
import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.wheel.WheelEntry;
import java.util.ArrayList;


/**
 * Created by Jonas Miederer.
 * Date: 06.05.16
 * Time: 17:37
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class MainFragment extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener, GoogleApiClient.ConnectionCallbacks {

    // Chart-related members
    private PieChart mChart;
    private PieDataSet dataSet;
    private float mStartAngle = 0;
    private PointF mTouchStartPoint = new PointF();

    // Logic data
    private WheelEntry selectedEntry;


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
        xVals.add("2. Lenkzeit");
        xVals.add("2. Pausenzeit");
        xVals.add("3. Lenkzeit");
        xVals.add("Ruhezeit");

        PieData data = new PieData(xVals, dataSet);
        mChart.setData(data);

        // Layout + appearance
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(80f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setLogEnabled(true);
        dataSet.setColors( WheelEntry.getColors(entries));

        // Listener
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);

        return view;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
        mStartAngle = mChart.getRawRotationAngle();
        mTouchStartPoint.x = me.getX();
        mTouchStartPoint.y = me.getY();
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        if (lastPerformedGesture == ChartTouchListener.ChartGesture.ROTATE) {
            if (distance(me.getX(), mTouchStartPoint.x, me.getY(), mTouchStartPoint.y)
                    > Utils.convertDpToPixel(8f)) {
                rotateIcons(me.getX(), me.getY());
            }

        }
        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP) {
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
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
        Log.i("LongPress", "Chart longpressed.");
        float x = me.getX();
        float y = me.getY();

        float angle = mChart.getAngleForPoint(x, y);
        int index = mChart.getIndexForAngle(angle);
        WheelEntry longPressedEntry = (WheelEntry) mChart.getEntriesAtIndex(index).get(0);
        dataSet.getColors().set(index, Color.BLUE);

        PointF center = mChart.getCenter();
        PointD point = getPoint(center, mChart.getRadius(), angle);

//        ImageView icon = new ImageView(getContext());
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(30, 30);
//        params.leftMargin = (int) (point.x);
//        params.topMargin = (int) (point.y);
//        icon.setLayoutParams(params);
//        icon.setImageResource(R.drawable.fuel);
//        symbols.add(icon);


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
        selectedEntry = (WheelEntry) mChart.getEntriesAtIndex(dataSetIndex);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
        mChart.setRotationEnabled(true);
    }

    public PointD getPoint(PointF origin, float radius, float angle) {
        double angle_ = (angle * Math.PI / 180) + 270;
        radius += 50;
        double x = origin.x + radius * Math.cos(angle_);
        double y = origin.y + radius * Math.sin(angle_);

        return new PointD(x, y);
    }

    protected static float distance(float eventX, float startX, float eventY, float startY) {
        float dx = eventX - startX;
        float dy = eventY - startY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
