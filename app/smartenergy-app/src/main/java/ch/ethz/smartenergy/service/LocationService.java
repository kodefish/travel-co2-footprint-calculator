package ch.ethz.smartenergy.service;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Date;

import ch.ethz.smartenergy.Constants;
import ch.ethz.smartenergy.model.LocationScan;


public class LocationService extends IntentService {
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private static final String GPS_DEBUG = "LOCATION_SERVICE";

    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
//                    Log.d(GPS_DEBUG, "Location result " + location.getLatitude() + ", " +
//                            location.getLongitude());
                    LocationScan locationScan = new LocationScan(
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getAltitude(),
                            location.getAccuracy(),
                            new Date(location.getTime()),
                            location.getBearing(),
                            location.getSpeed(),
                            location.getElapsedRealtimeNanos()
                    );

                    Intent broadCastIntent = new Intent();
                    broadCastIntent.setAction(Constants.DataBroadcastActionName);
                    broadCastIntent.putExtra(Constants.LocationScanExtraName, locationScan);
                    LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(broadCastIntent);
                }
            }
        };
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    private LocationRequest createLocationRequest() {
        Log.d(GPS_DEBUG, "Create location request");
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(SensorScanPeriod.GPS_SENSOR_PERIOD);
        //Highest possible accuracy => high battery usage
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return mLocationRequest;
    }

    private void startLocationUpdates() {
        Log.d(GPS_DEBUG, "---------STARTING--------");
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(GPS_DEBUG, "Permission denied");
            return;
        }
        mFusedLocationClient.requestLocationUpdates(createLocationRequest(),
                mLocationCallback,
                null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(GPS_DEBUG, "---------DESTROYING--------");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
