package ch.ethz.smartenergy.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;
import ch.ethz.smartenergy.Constants;
import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.TripSummaryActivity;
import ch.ethz.smartenergy.footprint.Leg;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.model.FeatureVector;
import ch.ethz.smartenergy.model.ScanResult;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.service.DataCollectionService;
import ch.ethz.smartenergy.service.SensorScanPeriod;
import ch.ethz.smartenergy.ui.adapters.PredictionAdapter;

public class HomeFragment extends Fragment {

    // Constants
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PERMISSION_ALL = 4242;
    private int locationRequestCount = 0;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Setup display
        tripCurrentMode = root.findViewById(R.id.home_current_mode);
        tripEmissions = root.findViewById(R.id.home_emissions);
        tripDistanceTravelled = root.findViewById(R.id.home_distance_travelled);
        tripTimeChronometer = root.findViewById(R.id.home_chronometer);
        TextView currentDateTv = root.findViewById(R.id.home_text_view_date);
        Date date = new Date();
        date.setTime(Calendar.getInstance().getTimeInMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM");
        currentDateTv.setText(sdf.format(date));

        /*
        GridView predictionGridView = root.findViewById(R.id.home_predictions);
        predictionAdapter = new PredictionAdapter(getContext(), -1);
        predictionGridView.setAdapter(predictionAdapter);
         */

        // Register button clicks to start scanning
        ((ToggleButton)root.findViewById(R.id.button_start)).setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        startScanning();
                    } else {
                        // If trip isn't empty, show summary
                        if (stopScanning())
                            startActivity(new Intent(getActivity(), TripSummaryActivity.class));
                    }});

        // Load ML stuff
        try {
            // load pretrained predictor
            InputStream model = getResources().openRawResource(R.raw.xgboost);
            predictor = new Predictor(model);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Load trip storage utility
        tripStorage = TripStorage.getInstance(getContext());

        // Start listening for sensor scans
        registerReceiver();
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        askPermissions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Stop service if it was launched
        if (serviceIntent != null) stopScanning();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
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
                            askStopScanning();
                        }
                    }
                }
            }
        }
    };

    private void askStopScanning() {
        AlertDialog stopTripDialog = new AlertDialog.Builder(getContext())
                // set message, title, and icon
                .setTitle("End Trip")
                .setMessage("It seems you haven't moved in a while, would you like to stop your trip?")
                .setIcon(R.drawable.icon_trash)

                .setPositiveButton("Stop", (dialog, whichButton) -> stopScanning())
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
        tripCurrentMode.setText(featureVector.mostProbableTripType().toString());

        // Update emissions
        tripEmissionsCounter += featureVector.getFootprint();
        tripEmissions.setText(Integer.toString((int)Math.round(tripEmissionsCounter)));

        // Update distance
        tripDistanceCounter += featureVector.getDistanceCovered();
        tripDistanceTravelled.setText(Integer.toString((int)Math.round(tripDistanceCounter)));

        // Update predictions
        // predictionAdapter.setPredictions(featureVector.getPredictions());
    }

    /**
     * Register broadcast receiver
     */
    private void registerReceiver() {
        IntentFilter it = new IntentFilter();
        it.addAction(Constants.WindowBroadcastActionName);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, it);
    }

    /**
     * Ask for fine location permissions
     */
    private void askPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_ALL);
        locationRequestCount = 0;
        locationSettingsRequest();
    }

    /**
     * Build location settings request
     */
    private void locationSettingsRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(SensorScanPeriod.GPS_SENSOR_PERIOD);
        //Highest possible accuracy => high battery usage
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //We can handle fast location updates, this limit is to not stress
        mLocationRequest.setFastestInterval(100);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(getActivity()).checkLocationSettings(builder.build());

        result.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(getActivity(),
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    /**
     * Handle activity result from location settings request
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_CANCELED) {
            locationRequestCount++;
            if (locationRequestCount >= 3) {
                showLocationsRequiredGoodbyeDialog();
            } else {
                showLocationsRequiredDialog();
            }
        }
    }

    /**
     * Show dialog that we really need the location
     */
    private void showLocationsRequiredDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getString(R.string.locations_required_title));
        alertDialog.setMessage(getString(R.string.locations_required_text));
        alertDialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                locationSettingsRequest();
            }
        });
        alertDialog.create().show();
    }

    /**
     * Location permission not given. Close application
     */
    private void showLocationsRequiredGoodbyeDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle(getString(R.string.locations_required_title));
        alertDialog.setMessage(getString(R.string.locations_required_goodbye));
        alertDialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        alertDialog.create().show();
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
        serviceIntent = new Intent(getContext(), DataCollectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(serviceIntent);
        } else {
            getActivity().startService(serviceIntent);
        }

        // Start new trip reading (collect all the feature vectors)
        tripReadings = new ArrayList<>();
    }

    /**
     * Stops the scanning service, computes the trip from feature vectors, persists the trip
     * @return true if trip not empty
     */
    public boolean stopScanning() {
        // Stop timer
        tripTimeChronometer.stop();

        getActivity().stopService(serviceIntent);
        serviceIntent = null;

        // Convert trip's sensor readings into a proper trip
        Trip trip = computeTripFromReadings();

        if (!trip.getLegs().isEmpty()) {
            // Persist the trip
            try {
                tripStorage.persistTrip(trip);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.empty_trip), Toast.LENGTH_SHORT).show();
        }

        return !trip.getLegs().isEmpty();

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
}