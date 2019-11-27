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

public class TripCompletedActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP = "extra_trip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_completed);

        try {
            // Get latest trip from persistence
            TripStorage tripStorage = TripStorage.getInstance(this);
            Trip completedTrip = tripStorage.getLastTrip();

            // Display trip summary
            TextView textViewLength = findViewById(R.id.trip_completed_distance_travelled);
            TextView textViewFootprint = findViewById(R.id.trip_completed_emissions);
            TextView textViewDuration = findViewById(R.id.trip_completed_duration);

            textViewLength.setText(completedTrip.getTotalDistanceAsString());
            textViewFootprint.setText(completedTrip.getTotalFootprintAsString());
            // textViewDuration.setText(completedTrip.getTotalTimeAsString());

            LegAdapter legAdapter = new LegAdapter(this, -1, completedTrip.getLegs());
            ListView legsListView = findViewById(R.id.trip_completed_legs_list);
            legsListView.setAdapter(legAdapter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void onDoneButtonPressed(View v) {
        finish();
    }
}

