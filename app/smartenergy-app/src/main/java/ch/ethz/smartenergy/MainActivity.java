package ch.ethz.smartenergy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;
import ch.ethz.smartenergy.model.LocationScan;
import ch.ethz.smartenergy.model.ScanResult;
import ch.ethz.smartenergy.model.SensorReading;
import ch.ethz.smartenergy.service.DataCollectionService;
import ch.ethz.smartenergy.service.SensorScanPeriod;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int PERMISSION_ALL = 4242;
    private int locationRequestCount = 0;

    private Predictor predictor;
    private Interpreter tflite_model;

    private List<Double> train_mean;
    private List<Double> train_std;


    private TextView probabilityOnFoot;
    private TextView probabilityTrain;
    private TextView probabilityTramway;
    private TextView probabilityBus;
    private TextView probabilityCar;
    private TextView probabilityBicycle;
    private TextView probabilityEbike;
    private TextView probabilityMotorcycle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        probabilityOnFoot = findViewById(R.id.text_predicted_foot);
        probabilityTrain = findViewById(R.id.text_predicted_train);
        probabilityTramway = findViewById(R.id.text_predicted_tramway);
        probabilityBus = findViewById(R.id.text_predicted_bus);
        probabilityCar = findViewById(R.id.text_predicted_car);
        probabilityBicycle = findViewById(R.id.text_predicted_bicycle);
        probabilityEbike = findViewById(R.id.text_predicted_ebike);
        probabilityMotorcycle = findViewById(R.id.text_predicted_motorcycle);

        probabilityOnFoot.setText(getString(R.string.on_foot, 0.00));
        probabilityTrain.setText(getString(R.string.train, 0.00));
        probabilityBus.setText(getString(R.string.bus, 0.00));
        probabilityCar.setText(getString(R.string.car, 0.00));
        probabilityTramway.setText(getString(R.string.tramway, 0.00));
        probabilityBicycle.setText(getString(R.string.bicycle, 0.00));
        probabilityEbike.setText(getString(R.string.ebike, 0.00));
        probabilityMotorcycle.setText(getString(R.string.motorcycle, 0.00));

        Log.d("CREATED MODEL", "TEST");
        try {
            // load pretrained predictor
            InputStream model = getResources().openRawResource(R.raw.xgboost);
            predictor = new Predictor(model);

            // load pretrained nn predictor
            load_assets();


        } catch (IOException ex) {
            ex.printStackTrace();
        }

        registerReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        askPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if (data == null) return;

            if (data.containsKey(Constants.WindowBroadcastExtraName)) {
                ScanResult scan = (ScanResult) data.getSerializable(Constants.WindowBroadcastExtraName);
                if (scan != null) {
                    double meanMagnitude = calculateMeanMagnitude(scan.getAccReadings());
                    double maxSpeed = calculateMaxSpead(scan.getLocationScans());
                    predict(meanMagnitude, maxSpeed);

                    predict_NN(scan.getAccReadings());
                }
            }
        }
    };


    private void load_assets() {
        try {
            MappedByteBuffer tfliteModel = loadModelFile(MainActivity.this, "example_nn.tflite");
            Interpreter.Options options = new Interpreter.Options();
            this.tflite_model = new Interpreter(tfliteModel, options);


            train_mean = new ArrayList<>();
            train_std = new ArrayList<>();

            try {
                InputStream is = getAssets().open("train_mean.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    train_mean.add(Double.valueOf(line));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            try {
                InputStream is = getAssets().open("train_std.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    train_std.add(Double.valueOf(line));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void predict_NN(List<SensorReading> acc_readings) {
        List<Double> magnitudes = new ArrayList<>();
        List<Long> times = new ArrayList<>();
        for (SensorReading reading : acc_readings) {
            double sumOfPows = reading.getValueOnXAxis() * reading.getValueOnXAxis() +
                    reading.getValueOnYAxis() * reading.getValueOnYAxis() +
                    reading.getValueOnZAxis() * reading.getValueOnZAxis();

            magnitudes.add(sumOfPows);
            times.add(reading.getReadingTime().getTime());
        }

        // Do nearest neighbor interpolation
        List<Double> magnitudes_interpolated = new ArrayList<>();
        double abs_sum = 0;
        long windowStart = times.get(0);
        for (int i = 0; i < 5000; i += 20) {
            long timeNode = windowStart + i;
            long last_diff = Math.abs(windowStart - timeNode);
            for (int j = 0; j < times.size(); j++) {
                long current_diff = Math.abs(times.get(j) - timeNode);
                if (current_diff <= last_diff) {
                    last_diff = current_diff;
                    if (j + 1 == times.size()) {
                        double value = magnitudes.get(j);
                        magnitudes_interpolated.add(value);
                        abs_sum += Math.abs(value);
                    }
                } else {
                    double value = magnitudes.get(j);
                    magnitudes_interpolated.add(value);
                    abs_sum += Math.abs(value);
                    break;
                }

            }
        }

        List<Double> magnitudes_normalized = new ArrayList<>();

        // Scale with l1 norm
        for (int i = 0; i < magnitudes_interpolated.size(); i++)
            magnitudes_normalized.add(magnitudes_interpolated.get(i) / abs_sum);


        // TODO: Scale to 0 mean and unit variance


        float[][] inputs = new float[250][1];
        for (int i = 0; i < magnitudes_normalized.size(); i++) {
            inputs[i][0] = (magnitudes_normalized.get(i).floatValue() - train_mean.get(i).floatValue()) / train_std.get(i).floatValue();
        }
        Log.d("INPUTS: ", magnitudes_normalized.toString());
        float[][] outputs = new float[1][8];

        this.tflite_model.run(inputs, outputs);


        float[] predictions = new float[outputs[0].length];

        for (int i = 0; i < outputs[0].length; i++) {
            predictions[i] = outputs[0][i];
        }

        appendResult(predictions);
    }

    private void predict(double meanMagnitude, double maxSpeed) {
        // build features vector
        double[] features = {meanMagnitude, maxSpeed};
        FVec features_vector = FVec.Transformer.fromArray(features, false);

        //predict
        float[] predictions = predictor.predict(features_vector);
        showResult(predictions);
    }

    private void appendResult(float[] predictions) {
        Log.d("PROBABILITIES: ", Arrays.toString(predictions));
        probabilityOnFoot.append(String.format("%s %.2f %s", " vs" , predictions[0] * 100, " %"));
        probabilityTrain.append(String.format("%s %.2f %s", " vs", predictions[1] * 100, " %"));
        probabilityBus.append(String.format("%s %.2f %s", " vs", predictions[2] * 100," %"));
        probabilityCar.append(String.format("%s %.2f %s", " vs", predictions[3] * 100," %"));
        probabilityTramway.append(String.format("%s %.2f %s", " vs", predictions[4] * 100," %"));
        probabilityBicycle.append(String.format("%s %.2f %s", " vs", predictions[5] * 100," %"));
        probabilityEbike.append(String.format("%s %.2f %s", " vs", predictions[6] * 100," %"));
        probabilityMotorcycle.append(String.format("%s %.2f %s", " vs", predictions[7] * 100," %"));
    }


    private void showResult(float[] predictions) {

        probabilityOnFoot.setText(getString(R.string.on_foot, predictions[0] * 100));
        probabilityTrain.setText(getString(R.string.train, predictions[1] * 100));
        probabilityBus.setText(getString(R.string.bus, predictions[2] * 100));
        probabilityCar.setText(getString(R.string.car, predictions[3] * 100));
        probabilityTramway.setText(getString(R.string.tramway, predictions[4] * 100));
        probabilityBicycle.setText(getString(R.string.bicycle, predictions[5] * 100));
        probabilityEbike.setText(getString(R.string.ebike, predictions[6] * 100));
        probabilityMotorcycle.setText(getString(R.string.motorcycle, predictions[7] * 100));
    }

    private double calculateMaxSpead(ArrayList<LocationScan> locationScans) {
        double maxSpeed = 0;
        for (LocationScan locationScan : locationScans) {
            if (locationScan.getSpeed() > maxSpeed) {
                maxSpeed = locationScan.getSpeed();
            }
        }

        return maxSpeed;
    }

    private double calculateMeanMagnitude(ArrayList<SensorReading> accReadings) {
        if (accReadings.size() == 0) return 0;

        double sumOfMagnitudes = 0;

        for (SensorReading reading : accReadings) {
            double sumOfPows = reading.getValueOnXAxis() * reading.getValueOnXAxis() +
                    reading.getValueOnYAxis() * reading.getValueOnYAxis() +
                    reading.getValueOnZAxis() * reading.getValueOnZAxis();
            sumOfMagnitudes += Math.sqrt(sumOfPows);
        }

        return sumOfMagnitudes / accReadings.size();
    }

    private MappedByteBuffer loadModelFile(Context context, String fileName) throws IOException {

        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("example_nn.tflite");
        FileInputStream inputStream = fileDescriptor.createInputStream();
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

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
     * Ask for fine location permissions
     */
    private void askPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_ALL);
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
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.locations_required_title));
        alertDialog.setMessage(getString(R.string.locations_required_goodbye));
        alertDialog.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.create().show();
    }

    /**
     * Start collecting data on button click
     *
     * @param view
     */
    public void startScanning(View view) {
        view.setEnabled(false);
        Intent serviceIntent = new Intent(this, DataCollectionService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent);
        } else {
            this.startService(serviceIntent);
        }
    }
}
