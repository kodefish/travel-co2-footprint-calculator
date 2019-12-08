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
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;

public class CarbonConsumptionStatsFragment extends Fragment {

    private BarChart barChart;
    private TextView distanceTravelledTv, carbonEmittedTv;
    private Date today, minusOneMonth, minusThreeMonths, minusSixMonths, minusOneYear;
    private TripStorage tripStorage;

    // newInstance constructor for creating fragment with arguments
    public static CarbonConsumptionStatsFragment newInstance() {
        CarbonConsumptionStatsFragment fragmentFirst = new CarbonConsumptionStatsFragment();
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup timeframes
        ZonedDateTime now = ZonedDateTime.now();
        today = Date.from(now.toInstant());
        minusOneMonth = Date.from(now.minusMonths(1).toInstant());
        minusThreeMonths = Date.from(now.minusMonths(3).toInstant());
        minusSixMonths = Date.from(now.minusMonths(6).toInstant());
        minusOneYear = Date.from(now.minusYears(1).toInstant());


        // Init trip storage
        tripStorage = TripStorage.getInstance(getContext());
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carbon_consumption_stats, container, false);

        // Get the views
        barChart = view.findViewById(R.id.carbon_consumption_chart);
        distanceTravelledTv = view.findViewById(R.id.carbon_consumption_distance_travelled);
        carbonEmittedTv = view.findViewById(R.id.carbon_consumption_co2_emitted);

        ChipGroup chipGroup = view.findViewById(R.id.statistics_date_filter);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.statistics_one_month_button:
                    setupChart(minusOneMonth, today);
                    break;
                case R.id.statistics_three_months_button:
                    setupChart(minusThreeMonths, today);
                    break;
                case R.id.statistics_six_months_button:
                    setupChart(minusSixMonths, today);
                    break;
                case R.id.statistics_one_year_button:
                    setupChart(minusOneYear, today);
                    break;
            }
        });

        setupChart(minusOneMonth, today);

        return view;
    }

    private void setupChart(Date start, Date end) {
        // Get trips
        List<Trip> trips = tripStorage.getTripsBetween(start, end);

        // Fill with information
        double totalDistance = 0;
        double totalFootprint = 0;
        List<BarEntry> entries = new ArrayList<>();
        for (Trip t : trips) {
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
                return trips.get((int) value).getDateAsString();
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
    }
}
