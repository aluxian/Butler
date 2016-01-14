package com.aluxian.behavioursimulator;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.Locale;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends Activity {

    @SuppressWarnings("UnusedDeclaration") private static final String TAG = MainActivity.class.getSimpleName();

    // Constants
    private static final int HOURS_RADIUS = 2;
    private static final double LEARNING_FACTOR = 5.0;
    private static final double DAMPING_FACTOR_HARD = 5.0;
    private static final double DAMPING_FACTOR_SOFT = 2.5;

    // Views
    private RelativeLayout mainLayout;
    private Button trackButton;
    private Button nextHourButton;
    private Button nextDayButton;
    private TextView timeText;

    // Track time
    private int day = 1;
    private int hour = 0;
    private int expected;

    // Graph
    private GraphViewSeries series;
    private GraphView.GraphViewData[] data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        trackButton = (Button) findViewById(R.id.btn_track);
        nextHourButton = (Button) findViewById(R.id.btn_next_hour);
        nextDayButton = (Button) findViewById(R.id.btn_next_day);
        timeText = (TextView) findViewById(R.id.time_text);

        // Data
        data = new GraphView.GraphViewData[24];
        String[] labels = new String[24];

        for (int i = 0; i < 24; i++) {
            data[i] = new GraphView.GraphViewData(i, 0);
            labels[i] = String.valueOf(i);
        }

        series = new GraphViewSeries(data);
        GraphViewSeries placeholderSeries = new GraphViewSeries(new GraphView.GraphViewData[] {
                new GraphView.GraphViewData(0, 100),
                new GraphView.GraphViewData(0, -100)
        });

        // GraphView
        GraphView graphView = new LineGraphView(this, "Behaviour Prediction Simulator");
        graphView.getGraphViewStyle().setNumVerticalLabels(21);
        graphView.setHorizontalLabels(labels);

        graphView.addSeries(series);
        graphView.addSeries(placeholderSeries);

        mainLayout.addView(graphView);

        // Set button callbacks
        trackButton.setOnClickListener(v -> trackEvent());
        nextHourButton.setOnClickListener(v -> advanceHour());
        nextDayButton.setOnClickListener(v -> advanceDay());
    }

    private void advanceHour() {
        hour++;

        if (hour > 23) {
            hour = 0;
            advanceDay();
        } else {
            updateTimeText();
        }
    }

    private void advanceDay() {
        day++;
        damp(DAMPING_FACTOR_HARD);
        updateTimeText();
    }

    private void updateTimeText() {
        timeText.setText(String.format(Locale.US, "Day: %02d Hour: %02d Expected: %02d", day, hour, expected));
    }

    private void damp(double factor) {
        for (int i = 0; i < data.length; i++) {
            double acceleration = data[i].valueY / 100;
            double newY = data[i].valueY - factor * acceleration;

            data[i] = new GraphView.GraphViewData(data[i].valueX, newY);
        }

        series.resetData(data);
        predict();
    }

    private void trackEvent() {
        update(hour, HOURS_RADIUS, +1);
        update(hour - 1, HOURS_RADIUS - 1, -1);
        series.resetData(data);
        damp(DAMPING_FACTOR_SOFT);
    }

    private void update(int hour, int remaining, int direction) {
        if (remaining == 0) return;
        if (hour > 23) hour = 0;
        if (hour < 0) hour = 23;

        double acceleration = 1 - Math.abs(data[hour].valueY) * Math.abs(data[hour].valueY) / 10000;
        double addition = LEARNING_FACTOR * remaining;
        double newY = data[hour].valueY + addition * acceleration;

        data[hour] = new GraphView.GraphViewData(data[hour].valueX, newY);
        update(hour + direction, --remaining, direction);
    }

    private void predict() {
        double highest = 0;
        int hour = 0;

        for (int i = 0; i < data.length; i++) {
            if (data[i].valueY > highest) {
                highest = data[i].valueY;
                hour = i;
            }
        }

        expected = hour;
    }

}
