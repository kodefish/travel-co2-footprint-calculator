package ch.ethz.smartenergy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.ui.adapters.LegAdapter;

public class TripCompletedActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP = "extra_trip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_completed);

        Bundle extras = getIntent().getExtras();
        if (extras == null) return; // This activity can't be started without a trip

        // Get serialized completed trip from extras
        String serializedCompletedTrip = extras.getString(EXTRA_TRIP);
        if (serializedCompletedTrip == null) return;

        // Deserialize trip
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Trip completedTrip = gson.fromJson(serializedCompletedTrip, Trip.class);

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
}
