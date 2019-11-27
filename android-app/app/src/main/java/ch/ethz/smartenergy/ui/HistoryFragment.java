package ch.ethz.smartenergy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.util.List;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.TripSummaryActivity;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;

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

        TripStorage storage = TripStorage.getInstance(getContext());
        pastTrips = storage.getAllStoredTrips();

        final TripAdapter adapter = new TripAdapter(getContext(),-1, pastTrips);
        ListView listView = root.findViewById(R.id.history_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent tripSummaryIntent = new Intent(HistoryFragment.this.getContext(), TripSummaryActivity.class);
            tripSummaryIntent.putExtra(TripSummaryActivity.EXTRA_TRIP_ID, position);
            startActivity(tripSummaryIntent);
        });
        return root;
    }
}