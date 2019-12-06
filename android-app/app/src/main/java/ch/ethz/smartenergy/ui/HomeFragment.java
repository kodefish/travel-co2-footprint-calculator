package ch.ethz.smartenergy.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.List;

import ch.ethz.smartenergy.R;

import ch.ethz.smartenergy.RecordTrip;
import ch.ethz.smartenergy.TripSummaryActivity;
import ch.ethz.smartenergy.footprint.Trip;
import ch.ethz.smartenergy.persistence.TripStorage;
import ch.ethz.smartenergy.service.SensorScanPeriod;
import ch.ethz.smartenergy.ui.adapters.TripAdapter;

public class HomeFragment extends Fragment {

    // Constants
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PERMISSION_ALL = 4242;
    private int locationRequestCount = 0;

    // Progress bar
    RoundCornerProgressBar roundCornerProgressBar;

    // Trips
    TripStorage tripStorage;
    TripAdapter tripAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        roundCornerProgressBar = root.findViewById(R.id.home_progress_day);

        // TODO: let user set this in onboarding, for now Paris agreement max per day in grams
        float maxDailyFootprint = 6301.36986f;
        roundCornerProgressBar.setMax(maxDailyFootprint);

        ListView listDayTrips = root.findViewById(R.id.home_day_trips);
        View emptyView = inflater.inflate(R.layout.empty_list_view, container, false);
        listDayTrips.setEmptyView(emptyView);

        View fab = root.findViewById(R.id.home_fab);
        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), RecordTrip.class)));

        tripStorage = TripStorage.getInstance(getContext());

        tripAdapter = new TripAdapter(getContext(), -1);
        tripAdapter.setOnItemClickListener(position -> {
            Intent openSummary = new Intent(getActivity(), TripSummaryActivity.class);
            openSummary.putExtra(TripSummaryActivity.EXTRA_TRIP_ID,
                    tripAdapter.getItem(position).getId());
            startActivity(openSummary);
        });
        listDayTrips.setAdapter(tripAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Trip> todaysTrips = tripStorage.getTripByDate(Calendar.getInstance().getTimeInMillis());
        tripAdapter.setTrips(todaysTrips);
        tripAdapter.notifyDataSetChanged();

        // Compute daily footprint
        double totalFootprint = 0;
        for (Trip t : todaysTrips) {
            totalFootprint += t.getTotalFootprint();
        }
        roundCornerProgressBar.setProgress((float) totalFootprint);
    }

    @Override
    public void onStart() {
        super.onStart();
        askPermissions();
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

}