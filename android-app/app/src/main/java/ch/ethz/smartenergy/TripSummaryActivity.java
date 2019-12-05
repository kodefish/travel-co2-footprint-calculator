package ch.ethz.smartenergy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;

import ch.ethz.smartenergy.footprint.Leg;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.model.FeatureVector;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.ui.adapters.LegAdapter;

public class TripSummaryActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private Trip completedTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_completed);

        // Get latest trip from persistence, if none then show latest trip
        TripStorage tripStorage = TripStorage.getInstance(this);


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

        // Set peak height of bottom sheet to only reveal trip summary info
        final LinearLayout quickInfoContainer = findViewById(R.id.trip_quick_info_container);
        final LinearLayout bottomSheet = findViewById(R.id.trip_summary_bottom_sheet);
        final BottomSheetBehavior<LinearLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        quickInfoContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                behavior.setPeekHeight(quickInfoContainer.getHeight());
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                // Only need to set the height once -> remove observer when done
                quickInfoContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        LegAdapter legAdapter = new LegAdapter(this, -1, completedTrip.getLegs());
        ListView legsListView = findViewById(R.id.trip_completed_legs_list);
        legsListView.setAdapter(legAdapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        // Draw trip
        // Compute bounds (to display the entire trip)
        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        for (Leg l : completedTrip.getLegs()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(ResourcesCompat.getColor(getResources(), R.color.primaryColor, null));
            // Only add start, since end is the start of the next
            for (FeatureVector p : l.getFeatureVectorList()) {
                LatLng start = new LatLng(p.getStartLat(), p.getStartLon());
                polylineOptions.add(start);
                latLngBounds.include(start);
            }
            // Add just the last end
            FeatureVector lastFeatureVec = l.getFeatureVectorList().get(l.getFeatureVectorList().size()-1);
            LatLng end = new LatLng(lastFeatureVec.getEndLat(), lastFeatureVec.getEndLon());
            polylineOptions.add(end);
            latLngBounds.include(end);

            // Draw the leg
            mMap.addPolyline(polylineOptions);
        }

        // Set the camera to the greatest possible zoom level that includes the bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), 0));
    }
}

