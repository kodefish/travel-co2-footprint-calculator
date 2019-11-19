package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


import ch.ethz.smartenergy.R;

public class StatisticsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_statistics, container, false);
        final TextView textView = root.findViewById(R.id.text_statistics);

        String[] timeframe = {"By day", "By week", "By month"};
        final Spinner spinnerTimeframe = root.findViewById(R.id.spinner1);
        ArrayAdapter<String> adapterTimeframe = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeframe);
        spinnerTimeframe.setAdapter(adapterTimeframe);

        String[] mode = {"Foot", "Train", "Bus", "Car", "Tram", "bike", "E-bike", "Motorcycle", "All"};
        final Spinner spinnerMode = root.findViewById(R.id.spinner2);
        ArrayAdapter<String> adapterMode = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mode);
        spinnerTimeframe.setAdapter(adapterMode);

        return root;
    }
}