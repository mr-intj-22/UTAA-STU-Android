package com.msl.utaastu.Dialogs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.msl.utaastu.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Malek Shefat on 6/15/2017.
 */

public class CalculationsResultDialog extends DialogFragment {

    private float[] results;

    private TextView total, change;
    private float gpa_total, gpa_change;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_calculation_result, container, false);
        Bundle args = getArguments();
        if (args != null) {
            results = args.getFloatArray("results");
            gpa_total = args.getFloat("total");
        }

        setChart(root);

        total = root.findViewById(R.id.total);
        change = root.findViewById(R.id.change);
        if (results.length < 3) {
            change.setVisibility(View.INVISIBLE);
        } else {
            gpa_change = results[results.length - 1] - results[results.length - 2];
            if (gpa_change == 0) {
                change.setText(getString(R.string.last_semester_grade, results[results.length - 1]));
                change.setTextColor(Color.WHITE);
            } else {
                if (gpa_change > 0) {
                    change.setText(getString(R.string.gpa_increase, gpa_change));
                    change.setTextColor(Color.GREEN);
                } else {
                    change.setText(getString(R.string.gpa_decrease, Math.abs(gpa_change)));
                    change.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
                }
            }
        }
        total.setText(String.valueOf(roundTwoDecimals(gpa_total)));
        return root;
    }

    private void setChart(View root) {
        LineChart chart = root.findViewById(R.id.chart);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setTouchEnabled(false);
        chart.setExtraOffsets(16, 8, 16, 8);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);


        List<Entry> semesterEntries = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            // turn your data into Entry objects
            semesterEntries.add(new Entry(i, results[i]));
        }

        //  semesters
        LineDataSet semestersDataSet = new LineDataSet(semesterEntries, null); // add entries to dataset
        semestersDataSet.setLineWidth(4f);
        semestersDataSet.setCircleColorHole(Color.WHITE);
        semestersDataSet.setValueTextSize(14f);
        semestersDataSet.setCircleRadius(6f);
        semestersDataSet.setCircleHoleRadius(4f);
        semestersDataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.color_accent));
        semestersDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.primary_light));
        semestersDataSet.setValueTextColor(ContextCompat.getColor(getActivity(), R.color.color_primary_dark)); // styling, ...
        LineData lineData = new LineData(semestersDataSet);
        // xAxis
        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(true);
        xAxis.setEnabled(true);
        // right - yAxis
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
        // left - yAxis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawLabels(false);
        yAxis.setDrawGridLines(true);
        yAxis.setEnabled(true);

        chart.setData(lineData);
        chart.animateY(3000, Easing.EasingOption.EaseOutBack);
        chart.animateX(3000, Easing.EasingOption.EaseOutBack);
        chart.invalidate(); // refresh
    }

    @Override
    public void onStart() {
        getDialog().getWindow().setWindowAnimations(
                R.style.DialogAnimationSlide);
        super.onStart();
    }

    private float roundTwoDecimals(float num) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Float.valueOf(twoDForm.format(num));
    }
}
