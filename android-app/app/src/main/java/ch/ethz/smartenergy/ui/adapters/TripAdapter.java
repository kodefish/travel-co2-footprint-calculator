package ch.ethz.smartenergy.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;

import java.util.List;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.ui.util.OnDeleteListener;

public class TripAdapter extends ArraySwipeAdapter<Trip> {

    private List<Trip> trips;
    private OnDeleteListener onDeleteClickListener;

    public TripAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips;
    }

    public void setOnDeleteClickListener(OnDeleteListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.item_trip_swipe_layout;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);

        // Init surface view with all the info
        TextView dateView = v.findViewById(R.id.item_trip_date);
        TextView distanceView = v.findViewById(R.id.item_trip_distance);
        TextView modesView = v.findViewById(R.id.item_trip_modes);
        TextView footprintView = v.findViewById(R.id.item_trip_footprint);
        TextView durationView = v.findViewById(R.id.item_trip_duration);

        Trip t = trips.get(position);

        dateView.setText(t.getDateAsString());
        distanceView.setText(t.getTotalDistanceAsString());
        modesView.setText(t.getModesAsString());
        footprintView.setText(t.getTotalFootprintAsString());
        durationView.setText(t.getTotalTimeAsString());

        // Init swipe layout (swipe to reveal delete)
        SwipeLayout swipeLayout = v.findViewById(R.id.item_trip_swipe_layout);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.addDrag(SwipeLayout.DragEdge.Right, v.findViewById(R.id.bottom_wrapper));

        // Add delete functionality
        ImageButton deleteButton = v.findViewById(R.id.item_trip_delete);
        deleteButton.setOnClickListener(view -> onDeleteClickListener.onDeleteClick(position));

        return v;
    }

    @Override
    public int getCount() {
        return trips.size();
    }

}
