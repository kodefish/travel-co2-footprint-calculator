package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;

public class CarbonConsumptionStatsFragment extends Fragment {

    // newInstance constructor for creating fragment with arguments
    public static CarbonConsumptionStatsFragment newInstance() {
        CarbonConsumptionStatsFragment fragmentFirst = new CarbonConsumptionStatsFragment();
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carbon_consumption_stats, container, false);

        // Get the views
        BarChart barChart = view.findViewById(R.id.carbon_consumption_chart);
        TextView distanceTravelledTv = view.findViewById(R.id.carbon_consumption_distance_travelled);
        TextView carbonEmittedTv = view.findViewById(R.id.carbon_consumption_co2_emitted);

        // Get trips
        TripStorage tripStorage = TripStorage.getInstance(getContext());
        List<Trip> allTrips = tripStorage.getAllStoredTrips();

        // Fill with information
        double totalDistance = 0;
        double totalFootprint = 0;
        List<BarEntry> entries = new ArrayList<>();
        for (Trip t : allTrips) {
            totalDistance += t.getTotalDistance();
            totalFootprint += t.getTotalFootprint();
            BarEntry entry = new BarEntry(entries.size(), (float) t.getTotalFootprint());
            entries.add(entry) ;
        }

        // Set total sums
        distanceTravelledTv.setText(Trip.getDistanceAsString(totalDistance));
        carbonEmittedTv.setText(Trip.getFootprintAsString(totalFootprint));

        // Create data set
        BarDataSet dataSet = new BarDataSet(entries, "C02 Consumption");
        dataSet.setColor(ColorTemplate.PASTEL_COLORS[0]);

        // Format x-axis data as string date
        IndexAxisValueFormatter xAxisValueFormatter = new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return allTrips.get((int) value).getDateAsString();
            }
        };
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(xAxisValueFormatter);

        // Load data into chart
        BarData barData = new BarData(dataSet);

        Description d = new Description();
        d.setText("");
        barChart.setDescription(d);
        barChart.setData(barData);
        barChart.setPinchZoom(true);
        barChart.invalidate();

        return view;
    }
}
