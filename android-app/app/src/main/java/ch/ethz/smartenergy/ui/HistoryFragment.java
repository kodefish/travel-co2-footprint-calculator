package ch.ethz.smartenergy.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.TripSummaryActivity;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;

import ch.ethz.smartenergy.ui.adapters.TripAdapter;


public class HistoryFragment extends Fragment {

    private List<Trip> pastTrips;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        TripStorage storage = TripStorage.getInstance(getContext());
        pastTrips = storage.getAllStoredTrips();

        Collections.reverse(pastTrips);

        // Setup adapter
        final TripAdapter adapter = new TripAdapter(getContext(),-1);
        adapter.setTrips(pastTrips);
        adapter.setOnDeleteClickListener(position -> {
            AlertDialog deleteConfirmDialog = new AlertDialog.Builder(getContext())
                    // set message, title, and icon
                    .setTitle("Delete")
                    .setMessage("Do you want to Delete")
                    .setIcon(R.drawable.icon_trash)

                    .setPositiveButton("Delete", (dialog, whichButton) -> {
                        try {
                            pastTrips = storage.deleteTrip(pastTrips.size() - 1 - position);
                            adapter.setTrips(pastTrips);
                            adapter.notifyDataSetChanged();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create();

            deleteConfirmDialog.show();
        });

        ListView listView = root.findViewById(R.id.history_list_view);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent tripSummaryIntent = new Intent(HistoryFragment.this.getContext(), TripSummaryActivity.class);
            tripSummaryIntent.putExtra(TripSummaryActivity.EXTRA_TRIP_ID, pastTrips.size() - 1 - position);
            startActivity(tripSummaryIntent);
        });
        return root;
    }
}