package ch.ethz.smartenergy.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.ChipGroup;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.persistence.TripStorage;

public class TransportationModeStatsFragment extends Fragment {

    private PieChart pieChart;
    private Date today, minusOneMonth, minusThreeMonths, minusSixMonths, minusOneYear;
    private TripStorage tripStorage;

    // newInstance constructor for creating fragment with arguments
    public static TransportationModeStatsFragment newInstance() {
        TransportationModeStatsFragment fragmentFirst = new TransportationModeStatsFragment();
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
        View view = inflater.inflate(R.layout.fragment_transportation_modes_stats, container, false);

        // Get the views
        pieChart = view.findViewById(R.id.transportation_modes_pie_chart);

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

        // Init with data from last month
        setupChart(minusOneMonth, today);

        return view;
    }

    private void setupChart(Date start, Date end) {
        // Get trips
        List<Trip> trips = tripStorage.getTripsBetween(start, end);

        // Fill with information
        TripType[] tripTypes = TripType.values();
        int[] modeCount = new int[tripTypes.length];
        for (Trip t : trips) {
            Map<TripType, Integer> types = t.getModesUsed();
            for (Map.Entry <TripType, Integer> type : types.entrySet()) {
                modeCount[type.getKey().ordinal()] += type.getValue();
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < modeCount.length; i++) {
            if (modeCount[i] > 0) {
                PieEntry entry = new PieEntry(modeCount[i], tripTypes[i].name());
                entries.add(entry);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Transportation Modes");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(13f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());

        Description d = new Description();
        d.setText("");
        pieChart.setDescription(d);
        pieChart.setUsePercentValues(true);
        pieChart.setRotationEnabled(false);
        pieChart.animateXY(1000, 1000);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
}
