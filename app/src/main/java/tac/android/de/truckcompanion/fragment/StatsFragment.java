package tac.android.de.truckcompanion.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Vector;

import tac.android.de.truckcompanion.R;
import tac.android.de.truckcompanion.logic.LogicHelper;
import tac.android.de.truckcompanion.simulator.SimulationEventListener;

/**
 * Created by Jonas Miederer.
 * Date: 21.05.16
 * Time: 17:50
 * Project: TruckCompanion
 * We're even wrong about which mistakes we're making. // Carl Winfield
 */
public class StatsFragment extends Fragment {
    private LogicHelper logHelp;
    private Vector<HorizontalBarChart> worktimeCharts;
    private Vector<HorizontalBarChart> restTimeCharts;
    private Vector<PieChart> headerCharts;
    private View view ;
    private final int updateCycle = 600;
    private int currUpdateCycle = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stats, container, false);
        logHelp = new LogicHelper();

        InitHeaderCharts();
        InitWorkTimeCharts();
        InitRestTimeCharts();
        InitCurrentDayChart();

        return view;
    }

    void InitHeaderCharts()
    {
        headerCharts = new Vector<>();
        headerCharts.add((PieChart)view.findViewById(R.id.week_stats_drivetime));
        headerCharts.add((PieChart)view.findViewById(R.id.week_stats_resttime));
        headerCharts.add((PieChart)view.findViewById(R.id.week_stats_drivetime_double));


        float degree = LogicHelper.MAX_WEEK_DRIVE_MINUTES / 360;
        degree = ((logHelp.GetDriveTimeSum()+logHelp.GetCurrentDay().GetDriveTime()) / degree);

        ArrayList<Entry> entriesPie = new ArrayList<>();
        entriesPie.add(new Entry(1f, 0));

        PieDataSet testDataSetPie = new PieDataSet(entriesPie,"");
        testDataSetPie.setColor(ColorTemplate.JOYFUL_COLORS[0]);
        testDataSetPie.setDrawValues(false);

        ArrayList<String> labelsPie = new ArrayList<>();
        labelsPie.add("");

        PieData testDataPie = new PieData(labelsPie,testDataSetPie);
        headerCharts.elementAt(0).setData(testDataPie);
        headerCharts.elementAt(0).setMinimumWidth(40);
        headerCharts.elementAt(0).setMinimumHeight(40);
        headerCharts.elementAt(0).setDescription("Wochenlenkzeit");
        headerCharts.elementAt(0).setCenterText((int)(logHelp.GetDriveTimeSum()  + logHelp.GetCurrentDay().GetDriveTime()) / 60 +" / "+ LogicHelper.MAX_WEEK_DRIVE_MINUTES / 60);
        headerCharts.elementAt(0).setDrawSliceText(false);
        headerCharts.elementAt(0).setMaxAngle(degree);
        headerCharts.elementAt(0).getLegend().setEnabled(false);
        headerCharts.elementAt(0).setTouchEnabled(false);


        entriesPie = new ArrayList<>();
        entriesPie.add(new Entry(0f, 0));

        testDataSetPie = new PieDataSet(entriesPie,"");
        testDataSetPie.setColor(ColorTemplate.JOYFUL_COLORS[0]);
        testDataSetPie.setDrawValues(false);

        labelsPie = new ArrayList<>();
        labelsPie.add("");

        testDataPie = new PieData(labelsPie,testDataSetPie);
        headerCharts.elementAt(1).setData(testDataPie);
        headerCharts.elementAt(1).setMinimumWidth(40);
        headerCharts.elementAt(1).setMinimumHeight(40);
        headerCharts.elementAt(1).setDescription("Wochenruhezeit");
        headerCharts.elementAt(1).setCenterText(0 +" / "+ 45);
        headerCharts.elementAt(1).setDrawSliceText(false);
        headerCharts.elementAt(1).setMaxAngle(0);
        headerCharts.elementAt(1).getLegend().setEnabled(false);
        headerCharts.elementAt(1).setTouchEnabled(false);

        degree = LogicHelper.MAX_DOUBLE_WEEK_DRIVE_MINUTES / 360;
        degree = logHelp.GetDriveTimeSum() / degree;

        entriesPie = new ArrayList<>();
        entriesPie.add(new Entry(1f, 0));

        testDataSetPie = new PieDataSet(entriesPie,"");
        testDataSetPie.setColor(ColorTemplate.JOYFUL_COLORS[0]);
        testDataSetPie.setDrawValues(false);

        labelsPie = new ArrayList<>();
        labelsPie.add("");

        testDataPie = new PieData(labelsPie,testDataSetPie);
        headerCharts.elementAt(2).setData(testDataPie);
        headerCharts.elementAt(2).setMinimumWidth(40);
        headerCharts.elementAt(2).setMinimumHeight(40);
        headerCharts.elementAt(2).setDescription("Doppelwochenlenkzeit");
        headerCharts.elementAt(2).setCenterText((int)(logHelp.GetDriveTimeSum() + logHelp.GetCurrentDay().GetDriveTime()) / 60 +" / "+ LogicHelper.MAX_DOUBLE_WEEK_DRIVE_MINUTES / 60);
        headerCharts.elementAt(2).setDrawSliceText(false);
        headerCharts.elementAt(2).setMaxAngle(degree);
        headerCharts.elementAt(2).getLegend().setEnabled(false);
        headerCharts.elementAt(2).setTouchEnabled(false);
    }
    void InitCurrentDayChart()
    {

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(logHelp.GetCurrentDay().GetDriveTime() / 60, 0));

        BarDataSet testDataSet = new BarDataSet(entries,"Zeit");
        testDataSet.setColor(ColorTemplate.JOYFUL_COLORS[4]);
        testDataSet.setDrawValues(false);

        //add label
        ArrayList<String> labels = new ArrayList<>();
        labels.add("Lenkzeit");


        //create dataset
        BarData testData = new BarData(labels,testDataSet);

        //set data
        worktimeCharts.elementAt(0).setData(testData);

        //design stuff
        worktimeCharts.elementAt(0).setDrawBorders(false);
        worktimeCharts.elementAt(0).setDrawGridBackground(false);

        worktimeCharts.elementAt(0).setDescription("");
        worktimeCharts.elementAt(0).setDrawValueAboveBar(false);
        worktimeCharts.elementAt(0).getAxisLeft().setDrawLabels(false);
        worktimeCharts.elementAt(0).setDrawGridBackground(false);

        worktimeCharts.elementAt(0).setAutoScaleMinMaxEnabled(false);
        worktimeCharts.elementAt(0).getAxisLeft().setAxisMaxValue(11);
        worktimeCharts.elementAt(0).getAxisLeft().setAxisMinValue(0);
        worktimeCharts.elementAt(0).getAxisRight().setDrawLabels(false);

        worktimeCharts.elementAt(0).getAxisLeft().setDrawGridLines(false);
        worktimeCharts.elementAt(0).getXAxis().setDrawGridLines(false);
        worktimeCharts.elementAt(0).getAxisRight().setDrawGridLines(false);
        worktimeCharts.elementAt(0).setDrawBorders(false);
        worktimeCharts.elementAt(0).getAxisLeft().setDrawAxisLine(false);
        worktimeCharts.elementAt(0).getAxisRight().setDrawAxisLine(false);
        worktimeCharts.elementAt(0).getLegend().setEnabled(false);
        worktimeCharts.elementAt(0).setTouchEnabled(false);
        //add limit line
        LimitLine limit = new LimitLine(LogicHelper.MAX_DAY_DRIVE_MINUTES / 60);
        limit.setLineColor(ColorTemplate.JOYFUL_COLORS[1]);
        limit.setLabel(logHelp.GetCurrentDay().GetDriveTime() / 60 +" / "+ LogicHelper.MAX_DAY_DRIVE_MINUTES / 60);
        limit.setTextSize(11);
        worktimeCharts.elementAt(0).getAxisLeft().addLimitLine(limit);

        entries = new ArrayList<>();
        entries.add(new BarEntry((logHelp.GetCurrentDay().GetRestTime()) / 60, 0));

        testDataSet = new BarDataSet(entries,"Zeit");
        testDataSet.setColor(ColorTemplate.JOYFUL_COLORS[3]);
        testDataSet.setDrawValues(false);

        //add label
        labels = new ArrayList<>();
        labels.add("Ruhezeit");


        //create dataset
        testData = new BarData(labels,testDataSet);

        //set data
        restTimeCharts.elementAt(0).setData(testData);

        //design stuff
       restTimeCharts.elementAt(0).setDrawBorders(false);
       restTimeCharts.elementAt(0).setDrawGridBackground(false);

       restTimeCharts.elementAt(0).setDescription("");
       restTimeCharts.elementAt(0).setDrawValueAboveBar(false);
       restTimeCharts.elementAt(0).getAxisLeft().setDrawLabels(false);
       restTimeCharts.elementAt(0).setDrawGridBackground(false);

       restTimeCharts.elementAt(0).setAutoScaleMinMaxEnabled(false);
       restTimeCharts.elementAt(0).getAxisLeft().setAxisMaxValue(13);
       restTimeCharts.elementAt(0).getAxisLeft().setAxisMinValue(0);
       restTimeCharts.elementAt(0).getAxisRight().setDrawLabels(false);

       restTimeCharts.elementAt(0).getAxisLeft().setDrawGridLines(false);
       restTimeCharts.elementAt(0).getXAxis().setDrawGridLines(false);
       restTimeCharts.elementAt(0).getAxisRight().setDrawGridLines(false);
       restTimeCharts.elementAt(0).setDrawBorders(false);
       restTimeCharts.elementAt(0).getAxisLeft().setDrawAxisLine(false);
       restTimeCharts.elementAt(0).getAxisRight().setDrawAxisLine(false);
       restTimeCharts.elementAt(0).getLegend().setEnabled(false);
        restTimeCharts.elementAt(0).setTouchEnabled(false);
        //add limit line
        limit = new LimitLine(11);
        limit.setLineColor(ColorTemplate.JOYFUL_COLORS[1]);
        limit.setLabel(logHelp.GetCurrentDay().GetRestTime() / 60 +" / "+ LogicHelper.MIN_DAY_REST_MINUTES / 60 );
        limit.setTextSize(11);
        restTimeCharts.elementAt(0).getAxisLeft().addLimitLine(limit);


    }
    void InitWorkTimeCharts()
    {
        worktimeCharts = new Vector<HorizontalBarChart>();
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_0_worktime));
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_1_worktime));
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_2_worktime));
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_3_worktime));
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_4_worktime));
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_5_worktime));
        worktimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_6_worktime));

        int counter = logHelp.workingDays.size() -1;
        boolean skippedFirst = false;
        for(HorizontalBarChart mChart : worktimeCharts)
        {
            if(!skippedFirst) {
                skippedFirst = true;  continue;
            }
            if(counter < 0 )
            {
                break;
            }

            ArrayList<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(logHelp.workingDays.elementAt(counter).GetDriveTime() / 60, 0));

            BarDataSet testDataSet = new BarDataSet(entries,"Zeit");
            testDataSet.setColor(ColorTemplate.JOYFUL_COLORS[4]);
            testDataSet.setDrawValues(false);

            //add label
            ArrayList<String> labels = new ArrayList<>();
            labels.add("Lenkzeit");


            //create dataset
            BarData testData = new BarData(labels,testDataSet);

            //set data
            mChart.setData(testData);

            //design stuff
            mChart.setDrawBorders(false);
            mChart.setDrawGridBackground(false);

            mChart.setDescription("");
            mChart.setDrawValueAboveBar(false);
            mChart.getAxisLeft().setDrawLabels(false);
            mChart.setDrawGridBackground(false);

            mChart.setAutoScaleMinMaxEnabled(false);
            mChart.getAxisLeft().setAxisMaxValue(11);
            mChart.getAxisLeft().setAxisMinValue(0);
            mChart.getAxisRight().setDrawLabels(false);

            mChart.getAxisLeft().setDrawGridLines(false);
            mChart.getXAxis().setDrawGridLines(false);
            mChart.getAxisRight().setDrawGridLines(false);
            mChart.setDrawBorders(false);
            mChart.getAxisLeft().setDrawAxisLine(false);
            mChart.getAxisRight().setDrawAxisLine(false);
            mChart.getLegend().setEnabled(false);
            mChart.setTouchEnabled(false);
            //add limit line
            LimitLine limit = new LimitLine(LogicHelper.MAX_DAY_DRIVE_MINUTES / 60);
            limit.setLineColor(ColorTemplate.JOYFUL_COLORS[1]);
            limit.setLabel(logHelp.workingDays.elementAt(counter).GetDriveTime() / 60 +" / "+ LogicHelper.MAX_DAY_DRIVE_MINUTES / 60);
            limit.setTextSize(11);
            mChart.getAxisLeft().addLimitLine(limit);


            --counter;
        }


    }
    void InitRestTimeCharts()
    {
        restTimeCharts = new Vector<HorizontalBarChart>();
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_0_resttime));
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_1_resttime));
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_2_resttime));
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_3_resttime));
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_4_resttime));
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_5_resttime));
        restTimeCharts.add((HorizontalBarChart) view.findViewById(R.id.day_stats_card_6_resttime));

        int counter = logHelp.workingDays.size() -1;
        boolean skippedFirst = false;
        for(HorizontalBarChart mChart : restTimeCharts)
        {
            if(!skippedFirst) {
                skippedFirst = true;  continue;
            }
            if(counter < 0 )
            {
                break;
            }

            ArrayList<BarEntry> entries = new ArrayList<>();
            entries.add(new BarEntry(logHelp.workingDays.elementAt(counter).GetRestTime() / 60, 0));

            BarDataSet testDataSet = new BarDataSet(entries,"Zeit");
            testDataSet.setColor(ColorTemplate.JOYFUL_COLORS[3]);
            testDataSet.setDrawValues(false);

            //add label
            ArrayList<String> labels = new ArrayList<>();
            labels.add("Ruhezeit");


            //create dataset
            BarData testData = new BarData(labels,testDataSet);

            //set data
            mChart.setData(testData);

            //design stuff
            mChart.setDrawBorders(false);
            mChart.setDrawGridBackground(false);

            mChart.setDescription("");
            mChart.setDrawValueAboveBar(false);
            mChart.getAxisLeft().setDrawLabels(false);
            mChart.setDrawGridBackground(false);

            mChart.setAutoScaleMinMaxEnabled(false);
            mChart.getAxisLeft().setAxisMaxValue(13);
            mChart.getAxisLeft().setAxisMinValue(0);
            mChart.getAxisRight().setDrawLabels(false);

            mChart.getAxisLeft().setDrawGridLines(false);
            mChart.getXAxis().setDrawGridLines(false);
            mChart.getAxisRight().setDrawGridLines(false);
            mChart.setDrawBorders(false);
            mChart.getAxisLeft().setDrawAxisLine(false);
            mChart.getAxisRight().setDrawAxisLine(false);
            mChart.getLegend().setEnabled(false);
            mChart.setTouchEnabled(false);
            //add limit line
            LimitLine limit = new LimitLine(11);
            limit.setLineColor(ColorTemplate.JOYFUL_COLORS[1]);
            limit.setLabel(logHelp.workingDays.elementAt(counter).GetRestTime() / 60 +" / "+ LogicHelper.MIN_DAY_REST_MINUTES / 60);
            limit.setTextSize(11);
            mChart.getAxisLeft().addLimitLine(limit);


            --counter;
        }


    }
       public void onTruckStationaryStateChange(int state)
       {
           logHelp.onTruckStationaryStateChange(state);
       }


       public void onTruckMoved()
       {
           if(currUpdateCycle >= updateCycle)
           {
               //update header charts
               float degree = LogicHelper.MAX_WEEK_DRIVE_MINUTES / 360;
               degree = ((logHelp.GetDriveTimeSum()+logHelp.GetCurrentDay().GetDriveTime()) / degree);

               headerCharts.elementAt(0).setCenterText((int)(logHelp.GetDriveTimeSum()  + logHelp.GetCurrentDay().GetDriveTime()) / 60 +" / "+ LogicHelper.MAX_WEEK_DRIVE_MINUTES / 60);
               headerCharts.elementAt(0).setMaxAngle(degree);

               degree = LogicHelper.MAX_DOUBLE_WEEK_DRIVE_MINUTES / 360;
               degree = logHelp.GetDriveTimeSum() / degree;

               headerCharts.elementAt(2).setCenterText((int)(logHelp.GetDriveTimeSum() + logHelp.GetCurrentDay().GetDriveTime()) / 60 +" / "+ LogicHelper.MAX_DOUBLE_WEEK_DRIVE_MINUTES / 60);
               headerCharts.elementAt(2).setMaxAngle(degree);

                //update current day worktime

               BarEntry entry = new BarEntry(logHelp.GetCurrentDay().GetDriveTime() / 60, 0);

               //set data
               worktimeCharts.elementAt(0).getData().removeEntry(0,0);
               worktimeCharts.elementAt(0).getData().addEntry(entry,0);
               worktimeCharts.elementAt(0).getAxisLeft().getLimitLines().get(0).setLabel(logHelp.GetCurrentDay().GetDriveTime() / 60 +" / "+ LogicHelper.MAX_DAY_DRIVE_MINUTES / 60);
               worktimeCharts.elementAt(0).notifyDataSetChanged();
               worktimeCharts.elementAt(0).invalidate();

               entry = new BarEntry(logHelp.GetCurrentDay().GetRestTime() / 60, 0);

               //set data
               restTimeCharts.elementAt(0).getData().removeEntry(0,0);
               restTimeCharts.elementAt(0).getData().addEntry(entry,0);
               restTimeCharts.elementAt(0).getAxisLeft().getLimitLines().get(0).setLabel(logHelp.GetCurrentDay().GetRestTime() / 60 +" / "+ LogicHelper.MIN_DAY_REST_MINUTES / 60 );
               restTimeCharts.elementAt(0).notifyDataSetChanged();
               restTimeCharts.elementAt(0).invalidate();

               currUpdateCycle = 0;
           }
           else
           {
            ++currUpdateCycle;
           }

            logHelp.onSimulationEvent();
       }

}
