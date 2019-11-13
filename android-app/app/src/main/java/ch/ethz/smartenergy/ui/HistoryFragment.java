package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.util.List;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;

public class HistoryFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        final TextView textView = root.findViewById(R.id.text_history);

        TripStorage tripStorage = new TripStorage(getActivity());
        try {
            List<Trip> storedTrips = tripStorage.getAllStoredTrips();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            textView.setText(gson.toJson(storedTrips, storedTrips.getClass()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return root;
    }
}