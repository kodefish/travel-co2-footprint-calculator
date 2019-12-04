package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ch.ethz.smartenergy.R;

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
        TextView tvLabel = (TextView) view.findViewById(R.id.tvLabel);
        tvLabel.setText("Transportation modes");
        return view;
    }
}
