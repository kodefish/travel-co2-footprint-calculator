package ch.ethz.smartenergy.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;

public class TripAdapter extends ArrayAdapter<Trip> {

    private final List<Trip> trips;

    public TripAdapter(@NonNull Context context, int resource, List<Trip> trips) {
        super(context, resource);
        this.trips = trips;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);

        TextView dateView = v.findViewById(R.id.item_trip_date);
        TextView distanceView = v.findViewById(R.id.item_trip_distance);
        TextView modesView = v.findViewById(R.id.item_trip_modes);
        TextView footprintView = v.findViewById(R.id.item_trip_footprint);
        TextView durationView = v.findViewById(R.id.item_trip_duration);

        Trip t = trips.get(position);

        dateView.setText(t.getDateAsString());
        distanceView.setText(t.getTotalDistanceAsString());
        // modesView.setText(t.getModesAsString());
        footprintView.setText(t.getTotalFootprintAsString());
        durationView.setText(t.getTotalTimeAsString());

        return v;
    }

    @Override
    public int getCount() {
        return trips.size();
    }
}
