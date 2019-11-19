package ch.ethz.smartenergy.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ch.ethz.smartenergy.R;

import ch.ethz.smartenergy.footprint.*;
import ch.ethz.smartenergy.persistence.TripStorage;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

// Container for information about a trip
class TripInfo {
    protected double distance;
    protected List<TripType> modesUsed;
    protected double footprint;
    protected Date date;

    public TripInfo(Trip t) {
        this.distance = t.getTotalDistance();
        this.modesUsed = t.getModesUsed();
        this.footprint = t.getTotalFootprint();
        this.date = t.getDate();
    }
}

public class HistoryFragment extends Fragment {

    private List<Trip> pastTrips;
    private List<TripInfo> tripsInfo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        final TextView textView = root.findViewById(R.id.text_history);

        // TODO: why is that not working ?
        TripStorage storage = TripStorage(this.getApplicationContext());
        try {
            pastTrips = storage.getAllStoredTrips();
        } catch (FileNotFoundException e) {
            // Do nothing
        }

        for (Trip t: pastTrips) {
            tripsInfo.add(new TripInfo(t));
        }

        populateList();

        return root;
    }

    // TODO: This is supposed to write all the information about the trips' informations on the list of past trips
    // TODO: Right now, all it does is put those informations in variables
    // TODO: I don't know how to made it work
    public void populateList() {
        for (TripInfo info: tripsInfo) {
            String date = dateToString(info.date);
            double distance = info.distance;
            double footprint = info.footprint;
            String modesOfTransport = modesToString(info.modesUsed);
        }
    }

    private String dateToString(Date d) {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(d);
    }

    private String modesToString(List<TripType> modes) {
        StringBuilder sb = new StringBuilder();
        for (TripType mode: modes) {
            sb.append(mode.toString().toLowerCase());
        }

        // Removing the last ", "
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}