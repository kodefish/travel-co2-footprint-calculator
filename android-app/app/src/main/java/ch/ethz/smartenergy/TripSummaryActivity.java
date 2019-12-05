package ch.ethz.smartenergy;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.smartenergy.footprint.Leg;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.model.FeatureVector;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.ui.adapters.LegAdapter;
import ch.ethz.smartenergy.ui.util.OnItemClickListener;

public class TripSummaryActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final int MAP_BOUNDS_PADDING_AMOUNT = 375;
    private static final float PATTERN_GAP_LENGTH_PX = 20;

    private Trip completedTrip;

    private GoogleMap mMap;
    private List<Polyline> legPolylines = new ArrayList<>();
    private Polyline selectedPolyline;

    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    //
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);


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

        OnItemClickListener onItemClickListener = position -> {
            if (mMap != null) {

                // Flip previously selected selectedPolyline from previous solid stroke to dotted stroke pattern.
                toggleDottedPattern(selectedPolyline);

                // Flip from solid stroke to dotted stroke pattern (only if different polyline was selected)
                if (selectedPolyline != legPolylines.get(position)) {
                    toggleDottedPattern(selectedPolyline);

                    // Update selected polyline
                    selectedPolyline = legPolylines.get(position);

                    // Focus on selected leg
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                    for (LatLng pt : selectedPolyline.getPoints())
                        bounds.include(pt);

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), MAP_BOUNDS_PADDING_AMOUNT));

                    // Close bottom sheet
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }

            }
        };

        LegAdapter legAdapter = new LegAdapter(this, -1, completedTrip.getLegs(), onItemClickListener);
        ListView legsListView = findViewById(R.id.trip_completed_legs_list);
        legsListView.setAdapter(legAdapter);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void toggleDottedPattern(Polyline polyline) {
        if (polyline != null) {
            if ((polyline.getPattern() == null)
                    || (!polyline.getPattern().contains(DOT))) {
                polyline.setPattern(PATTERN_POLYLINE_DOTTED);
            } else {
                // The default pattern is a solid stroke.
                polyline.setPattern(null);
            }
        }
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
        mMap = googleMap;

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
            Polyline polyline = mMap.addPolyline(polylineOptions);
            legPolylines.add(polyline);
        }

        // Set the camera to the greatest possible zoom level that includes the bounds
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), MAP_BOUNDS_PADDING_AMOUNT));
    }
}

