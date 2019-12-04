package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.persistence.TripStorage;

public class TransportationModeStatsFragment extends Fragment {

    // newInstance constructor for creating fragment with arguments
    public static TransportationModeStatsFragment newInstance() {
        TransportationModeStatsFragment fragmentFirst = new TransportationModeStatsFragment();
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
        View view = inflater.inflate(R.layout.fragment_transportation_modes_stats, container, false);

        // Get the views
        PieChart pieChart = view.findViewById(R.id.transportation_modes_pie_chart);

        // Get trips
        TripStorage tripStorage = TripStorage.getInstance(getContext());
        List<Trip> allTrips = tripStorage.getAllStoredTrips();

        // Fill with information
        TripType[] tripTypes = TripType.values();
        int[] modeCount = new int[tripTypes.length];
        for (Trip t : allTrips) {
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

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();

        return view;
    }
}
