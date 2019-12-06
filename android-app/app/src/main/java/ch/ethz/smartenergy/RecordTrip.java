package ch.ethz.smartenergy;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.ActivityOptions;
import androidx.appcompat.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;
import ch.ethz.smartenergy.footprint.Leg;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.model.FeatureVector;
import ch.ethz.smartenergy.model.ScanResult;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.service.DataCollectionService;
import ch.ethz.smartenergy.ui.adapters.PredictionAdapter;

public class RecordTrip extends Activity {

    // Constants
    private static final int RESULT_SUMMARY = 0;

    // ML classifier
    private Predictor predictor;
    private List<FeatureVector> tripReadings;

    // UI
    private TextView tripCurrentMode;
    private TextView tripEmissions;
    private double tripEmissionsCounter = 0;
    private TextView tripDistanceTravelled;
    private double tripDistanceCounter = 0;
    private Chronometer tripTimeChronometer;
    private PredictionAdapter predictionAdapter;

    // App background
    private Intent serviceIntent;
    private TripStorage tripStorage;

    // Counts how many feature vectors in a row the user hasn't moved
    private int immobileFeatureVecCounter = 0;
    private View tripInfoWrapper, tripEmissionsLabel, tripDistanceTravelledLabel, tripTimeChronometerLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_trip);

        // Setup display
        tripCurrentMode = findViewById(R.id.home_current_mode);
        tripEmissions = findViewById(R.id.home_emissions);
        tripDistanceTravelled = findViewById(R.id.home_distance_travelled);
        tripTimeChronometer = findViewById(R.id.home_chronometer);

        // Load shared elems
        tripInfoWrapper = findViewById(R.id.home_trip_info);
        tripEmissionsLabel = findViewById(R.id.home_emissions_label);
        tripDistanceTravelledLabel = findViewById(R.id.home_distance_travelled_label);
        tripTimeChronometerLabel = findViewById(R.id.home_chronometer_label);

        /*
        GridView predictionGridView = root.findViewById(R.id.home_predictions);
        predictionAdapter = new PredictionAdapter(getContext(), -1);
        predictionGridView.setAdapter(predictionAdapter);
         */

        // Register button clicks to stop scanning
        findViewById(R.id.button_start).setOnClickListener(v -> finalizeTrip());
        /*
        ((ToggleButton) findViewById(R.id.button_start)).setOnCheckedChangeListener(
                (buttonView, checked) -> {
                    if (checked) {
                        startScanning();
                    } else {
                        stopScanning();
                    }
                });
         */

        // Load ML stuff
        try {
            // load pretrained predictor
            InputStream model = getResources().openRawResource(R.raw.xgboost);
            predictor = new Predictor(model);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Load trip storage utility
        tripStorage = TripStorage.getInstance(this);

        // Start listening for sensor scans
        registerReceiver();

        // Start the serice
        startScanning();
    }

    @Override
    public void onDestroy() {
        // Stop service if it was launched (without saving the trip)
        if (serviceIntent != null) stopScanning();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.cancel_trip)
                .setMessage(R.string.cancel_trip_text)
                .setPositiveButton(R.string.cancel_trip_ok, (dialog, which) -> {
                    dialog.dismiss();
                    super.onBackPressed();
                })
                .setNegativeButton(R.string.cancel_trip_negative, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if (data == null) return;

            if (data.containsKey(Constants.WindowBroadcastExtraName)) {
                ScanResult scan = (ScanResult) data.getSerializable(Constants.WindowBroadcastExtraName);
                if (scan != null) {
                    // Build feature vector
                    FeatureVector featureVec = new FeatureVector(scan);
                    if (featureVec.isMoving()) {
                        // Reset the counter
                        immobileFeatureVecCounter = 0;

                        // Get prediction results
                        float[] predictions = predict(featureVec);
                        featureVec.setPredictions(predictions);

                        // Show live info
                        updateUI(featureVec);

                        tripReadings.add(featureVec);
                    } else {
                        immobileFeatureVecCounter++;

                        if (immobileFeatureVecCounter > 3) {
                            immobileFeatureVecCounter = 0;
                            askStopScanning();
                        }
                    }
                }
            }
        }
    };

    private void askStopScanning() {
        AlertDialog stopTripDialog = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("End Trip")
                .setMessage("It seems you haven't moved in a while, would you like to stop your trip?")
                .setIcon(R.drawable.icon_trash)

                .setPositiveButton("Stop", (dialog, whichButton) -> finalizeTrip())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();

        stopTripDialog.show();
    }

    private float[] predict(FeatureVector features) {
        // Build features vector
        FVec features_vector = FVec.Transformer.fromArray(features.getFeatureVec(), false);

        // Predict
        float[] predictions = predictor.predict(features_vector);
        return predictions;
    }

    private void updateUI(FeatureVector featureVector) {
        // Update mode
        tripCurrentMode.setVisibility(View.VISIBLE);
        tripCurrentMode.setText(featureVector.mostProbableTripType().toString());

        // Update emissions
        tripEmissionsCounter += featureVector.getFootprint();
        tripEmissions.setText(Trip.getFootprintAsString(tripEmissionsCounter));

        // Update distance
        tripDistanceCounter += featureVector.getDistanceCovered();
        tripDistanceTravelled.setText(Trip.getFootprintAsString(tripDistanceCounter));

        // Update predictions
        // predictionAdapter.setPredictions(featureVector.getPredictions());
    }

    /**
     * Register broadcast receiver
     */
    private void registerReceiver() {
        IntentFilter it = new IntentFilter();
        it.addAction(Constants.WindowBroadcastActionName);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, it);
    }


    /**
     * Start collecting data on button click
     */
    public void startScanning() {
        // Start timer, set emissions and distance counters to 0
        tripTimeChronometer.setBase(SystemClock.elapsedRealtime());
        tripTimeChronometer.start();

        tripEmissions.setText(getString(R.string.home_emissions));
        tripEmissionsCounter = 0;
        tripDistanceTravelled.setText(R.string.home_distance_travelled);
        tripDistanceCounter = 0;

        // Start data collection service
        serviceIntent = new Intent(this, DataCollectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }

        // Start new trip reading (collect all the feature vectors)
        tripReadings = new ArrayList<>();
    }

    /**
     * Stops the scanning service, computes the trip from feature vectors, persists the trip
     * @return true if trip not empty
     */
    public Trip stopScanning() {
        // Stop timer
        tripTimeChronometer.stop();

        this.stopService(serviceIntent);
        serviceIntent = null;

        // Convert trip's sensor readings into a proper trip
        return computeTripFromReadings();

    }

    private Trip computeTripFromReadings() {
        List<Leg> legs = new ArrayList<>();
        if (!tripReadings.isEmpty()) {
            List<FeatureVector> legFeatures = new ArrayList<>();
            TripType previousTripType = tripReadings.get(0).mostProbableTripType();

            for (int i = 0; i < tripReadings.size(); i++) {
                FeatureVector featureVec = tripReadings.get(i);
                TripType currentTripType = featureVec.mostProbableTripType();

                // TODO more sophisticated way of computing a leg
                if (currentTripType.equals(previousTripType)) {
                    // If the current window is of the same type as the previous, then the leg is
                    // probably the same
                    legFeatures.add(featureVec);

                    // If last feature vec, then create leg and add it
                    if (i == tripReadings.size() - 1) legs.add(new Leg(legFeatures));
                } else {
                    // Finalize current leg
                    legs.add(new Leg(legFeatures));

                    // Reset leg and add current features to new leg
                    legFeatures.clear();
                    previousTripType = currentTripType;
                    legFeatures.add(featureVec);

                }
            }
        }
        return new Trip(legs);
    }

    private void finalizeTrip() {
        Trip trip = stopScanning();

        if (!trip.getLegs().isEmpty()) {
            // Persist the trip
            try {
                tripStorage.persistTrip(trip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getString(R.string.empty_trip), Toast.LENGTH_SHORT).show();
        }


        if (!trip.getLegs().isEmpty()) {
            Intent startSummary = new Intent(this, TripSummaryActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    new Pair<>(tripInfoWrapper, getString(R.string.transition_trip_info)),
                    new Pair<>(tripEmissions, getString(R.string.transition_trip_info_emissions_value)),
                    new Pair<>(tripEmissionsLabel, getString(R.string.transition_trip_info_emissions_label)),
                    new Pair<>(tripDistanceTravelled, getString(R.string.transition_trip_info_distance_value)),
                    new Pair<>(tripDistanceTravelledLabel, getString(R.string.transition_trip_info_distance_label)),
                    new Pair<>(tripTimeChronometer, getString(R.string.transition_trip_info_duration_value)),
                    new Pair<>(tripTimeChronometerLabel, getString(R.string.transition_trip_info_duration_label))
            );

            startActivityForResult(startSummary, RESULT_SUMMARY, options.toBundle());
        } else {
            Toast.makeText(this, getText(R.string.empty_trip), Toast.LENGTH_SHORT).show();
            finishAfterTransition();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SUMMARY) {
            // Summary shown and we can close this activity
            finish();
        }
    }
}
