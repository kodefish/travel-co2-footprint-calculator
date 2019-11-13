package ch.ethz.smartenergy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ch.ethz.smartenergy.footprint.Trip;

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
        TextView textViewLength = (TextView) findViewById(R.id.trip_completed_length);
        TextView textViewFootprint = (TextView) findViewById(R.id.trip_completed_footprint);
        TextView textViewNumLegs = (TextView) findViewById(R.id.trip_completed_num_legs);
        TextView textViewJson = (TextView) findViewById(R.id.trip_completed_json);

        textViewLength.setText("Total length: " + completedTrip.getTotalDistance());
        textViewFootprint.setText("Total footprint: " + completedTrip.getTotalFootprint());
        textViewNumLegs.setText("Num legs in trip: " + completedTrip.getNumLegs());
        textViewJson.setText(gson.toJson(completedTrip));
    }
}
