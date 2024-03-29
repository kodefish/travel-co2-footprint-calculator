package ch.ethz.smartenergy;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
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

public class TripSummaryActivity extends FragmentActivity {

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


    @SuppressLint("ClickableViewAccessibility")
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

        textViewFootprint.setText(completedTrip.getTotalFootprintAsString());
        textViewLength.setText(completedTrip.getTotalDistanceAsString());
        textViewDuration.setText(completedTrip.getTotalTimeAsString());

        // Get bottom sheet to toggle state
        final LinearLayout bottomSheet = findViewById(R.id.trip_summary_bottom_sheet);
        final BottomSheetBehavior<LinearLayout> behavior = BottomSheetBehavior.from(bottomSheet);

        OnItemClickListener onItemClickListener = position -> {
            if (mMap != null) {

                // Flip previously selected selectedPolyline from previous dotted stroke to solid stroke pattern.
                if (selectedPolyline != null) {
                    selectedPolyline.setPattern(null);
                }

                // Flip from solid stroke to dotted stroke pattern (only if different polyline was selected)
                if (selectedPolyline != legPolylines.get(position)) {
                    // Update selected polyline
                    selectedPolyline = legPolylines.get(position);

                    selectedPolyline.setPattern(PATTERN_POLYLINE_DOTTED);

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

        // Setup toggle bottom sheet button
        ImageButton toggleBottomSheet = findViewById(R.id.trip_completed_toggle_bottom_sheet);
        toggleBottomSheet.setOnClickListener(v -> {
            if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                // Expand bottom sheet
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                // Expand bottom sheet
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        // Set close button
        findViewById(R.id.trip_completed_close).setOnClickListener(v -> finish());

        LegAdapter legAdapter = new LegAdapter(this, -1, completedTrip.getLegs(), onItemClickListener);
        ListView legsListView = findViewById(R.id.trip_completed_legs_list);
        legsListView.setAdapter(legAdapter);
        // Make sure we scroll listview instead of bottom sheet
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_DRAGGING:
                        boolean listIsAtTop = legsListView.getChildCount() == 0
                                || legsListView.getChildAt(0).getTop() == 0 && legsListView.getFirstVisiblePosition() == 0;
                        if (!listIsAtTop) {
                            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        // Set icon to down arrow
                        toggleBottomSheet.setImageResource(R.drawable.icon_expand_less);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        // Set icon to up arrow
                        toggleBottomSheet.setImageResource(R.drawable.icon_expand_more);
                        break;
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> googleMap.setOnMapLoadedCallback(() -> onMapReady(googleMap)));
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Draw trip
        // Compute bounds (to display the entire trip)
        LatLngBounds.Builder latLngBounds = new LatLngBounds.Builder();
        for (Leg l : completedTrip.getLegs()) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(ResourcesCompat.getColor(getResources(), R.color.primaryColor, null));
            // Only all location scan points for higher precision tracing
            for (FeatureVector fv : l.getFeatureVectorList()) {
                for (Pair<Double, Double> latlng : fv.getLocations()) {
                    LatLng start = new LatLng(latlng.first, latlng.second);
                    polylineOptions.add(start);
                    latLngBounds.include(start);
                }
            }

            // Draw the leg
            Polyline polyline = mMap.addPolyline(polylineOptions);
            legPolylines.add(polyline);
        }

        // Set the camera to the greatest possible zoom level that includes the bounds
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds.build(), MAP_BOUNDS_PADDING_AMOUNT));
    }
}

