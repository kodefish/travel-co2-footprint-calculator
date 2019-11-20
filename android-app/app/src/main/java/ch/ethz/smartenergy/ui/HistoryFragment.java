package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ch.ethz.smartenergy.R;

import ch.ethz.smartenergy.footprint.*;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.ui.adapters.TripAdapter;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class HistoryFragment extends Fragment {

    private List<Trip> pastTrips;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        final TextView textView = root.findViewById(R.id.text_history);

        TripStorage storage = new TripStorage(getContext());
        try {
            pastTrips = storage.getAllStoredTrips();
        } catch (FileNotFoundException e) {
            // Do nothing
        }

        final TripAdapter adapter = new TripAdapter(getContext(),-1, pastTrips);
        ListView listView = root.findViewById(R.id.history_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            // TODO: stuff
        });

        return root;
    }
}