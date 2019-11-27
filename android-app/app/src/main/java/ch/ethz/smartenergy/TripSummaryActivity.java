package ch.ethz.smartenergy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;

import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.ui.adapters.LegAdapter;

public class TripSummaryActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_completed);

        // Get latest trip from persistence, if none then show latest trip
        TripStorage tripStorage = TripStorage.getInstance(this);

        Trip completedTrip;

        // Get id of trip to display
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt(EXTRA_TRIP_ID);
            completedTrip = tripStorage.getTripById(id);
        } else {
            completedTrip = tripStorage.getLastTrip();
        }


        // Display trip summary
        TextView textViewLength = findViewById(R.id.trip_completed_distance_travelled);
        TextView textViewFootprint = findViewById(R.id.trip_completed_emissions);
        TextView textViewDuration = findViewById(R.id.trip_completed_duration);

        textViewLength.setText(completedTrip.getTotalDistanceAsString());
        textViewFootprint.setText(completedTrip.getTotalFootprintAsString());
        textViewDuration.setText(completedTrip.getTotalTimeAsString());

        LegAdapter legAdapter = new LegAdapter(this, -1, completedTrip.getLegs());
        ListView legsListView = findViewById(R.id.trip_completed_legs_list);
        legsListView.setAdapter(legAdapter);

    }

    public void onDoneButtonPressed(View v) {
        finish();
    }
}

