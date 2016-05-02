package tac.android.de.truckcompanion;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;


import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    private PieChart mChart;
    private TextView textView;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mChart = (PieChart) findViewById(R.id.chart);
        textView = (TextView) findViewById(R.id.text);
        relativeLayout = (RelativeLayout) findViewById(R.id.rLayout);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                width,
//                (int) (height * .5)
                0
        );
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                width,
//                (int) (height * 1.5)
                height
        );

        textView.setLayoutParams(params1);
        relativeLayout.setLayoutParams(params2);

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        yVals1.add(new Entry((float) (450 / 24), 0));
        yVals1.add(new Entry((float) (75 / 24), 1));
        yVals1.add(new Entry((float) (450 / 24), 2));
        yVals1.add(new Entry((float) (75 / 24), 3));
        yVals1.add(new Entry((float) (100 / 24), 4));
        yVals1.add(new Entry((float) (1250 / 24), 5));

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
