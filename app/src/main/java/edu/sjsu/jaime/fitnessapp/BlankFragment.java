package edu.sjsu.jaime.fitnessapp;


import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {


    public static LineChart chart;
    public static LineChart chart2;
    public static TextView average;
    public static TextView minimum;
    public static TextView maximum;


    public static ArrayList<caloriesData> caloriesDataset;



    public BlankFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        chart = view.findViewById(R.id.chart);
        chart2 = view.findViewById(R.id.chart2);

        average = view.findViewById(R.id.textView4);
        minimum = view.findViewById(R.id.textView5);
        maximum = view.findViewById(R.id.textView6);


        Log.d("late", "In oncreaterview");
        return view;

    }

    public static void setChartUp(ArrayList<caloriesData> data)
    {
        List<Entry> entries = new ArrayList<>();

        for(caloriesData d : data) {
            entries.add(new Entry(d.getValueX(), (float) d.getValueY()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setVisibleXRangeMaximum(300);
        chart.invalidate();
        chart.moveViewToX(data.get(data.size()-1).getValueX());

    }
    public static void setChart2Up(ArrayList<stepsData> data)
    {
        List<Entry> entries = new ArrayList<>();

        for(stepsData d : data) {
            entries.add(new Entry(d.getValueX(), (float) d.getValueY()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        dataSet.setColor(R.color.colorAccent);

        LineData lineData = new LineData(dataSet);

        chart2.setData(lineData);

        chart2.setVisibleXRangeMaximum(100);
        chart2.invalidate();
        chart2.moveViewToX(data.get(data.size()-1).getValueX());
    }

}
