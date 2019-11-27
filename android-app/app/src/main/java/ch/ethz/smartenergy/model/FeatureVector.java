package ch.ethz.smartenergy.model;

import android.location.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.smartenergy.features.FeatureExtractor;
import ch.ethz.smartenergy.footprint.Footprint;
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.service.SensorScanPeriod;

public class FeatureVector {

    // Constants used to reference features
    public static final String FEATURE_KEY_MEAN_MAGNITUDE = "meanMagnitude";
    public static final String FEATURE_KEY_MAX_SPEED = "maxSpeed";
    public static final String FEATURE_KEY_DISTANCE_COVERED = "distanceCovered";

    // Computed features for decison tree
    private transient final Map<String, Double> features;

    // Represents how sure we are that prediction[i] is of type TripType[i]
    private float[] predictions;

    private final double totalDistanceCovered;

    // Time when scan result was started and finalized
    private final long startTime;
    private final long endTime;

    /**
     * Construct a feature vector based on raw sensor values
     * @param scanResult resulting scan values
     */
    public FeatureVector(ScanResult scanResult) {
        this.startTime = scanResult.getStartTime();
        this.endTime = scanResult.getEndTime();

        features = new HashMap<>();

        // Accelerator magnitude mean
        double meanMagnitude = calculateMeanMagnitude(scanResult.getAccReadings());
        features.put(FeatureVector.FEATURE_KEY_MEAN_MAGNITUDE, meanMagnitude);

        // peaks of FFT (5x and 5y = 10 features)

        ArrayList<ArrayList<Double>> accAxis = new ArrayList<>(Collections.nCopies(3, new ArrayList<>()));
        extractXYZ(scanResult.getAccReadings(), accAxis);

        for (ArrayList<Double> axis : accAxis) {
            ArrayList<Double> fft;
            fft = FeatureExtractor.extract_features(axis, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE); // winsize in milliseconds! should be 20'000!!
            for (int i = 0; i < fft.size(); i++)
                features.put("gyro_" + axis + "_" + i, fft.get(i));
        }

        // TODO: average connected bluetooth devices (for each scanID within this window, look at #devices and then take average over that)

        // TODO: Gyro magnitude mean

        // TODO: FFT for gyro

        // max speed
        double maxSpeed = calculateMaxSpeed(scanResult.getLocationScans());
        features.put(FeatureVector.FEATURE_KEY_MAX_SPEED, maxSpeed);

        // TODO: average speed

        // altitude speed (let's skip this for simplicity..)

        // TODO: magnetic field magnitude mean

        // distance covered (this is not implemented in the ML model [yet])
        totalDistanceCovered = calculateDistanceCovered(scanResult.getLocationScans());
        features.put(FeatureVector.FEATURE_KEY_DISTANCE_COVERED, totalDistanceCovered);
    }


    private double calculateMaxSpeed(ArrayList<LocationScan> locationScans) {
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

    private double calculateDistanceCovered(ArrayList<LocationScan> locationScans) {
        double distanceCovered = 0;
        for (int i = 0; i < locationScans.size() - 1; i++) {
            LocationScan locationScan1 = locationScans.get(i);
            LocationScan locationScan2 = locationScans.get(i+1);
            double lat1 = locationScan1.getLatitude(), lon1 = locationScan1.getLongitude();
            double lat2 = locationScan2.getLatitude(), lon2 = locationScan2.getLongitude();

            Location loc1 = new Location("");
            loc1.setLatitude(lat1);
            loc1.setLongitude(lon1);

            Location loc2 = new Location("");
            loc2.setLatitude(lat2);
            loc2.setLongitude(lon2);

            distanceCovered += loc1.distanceTo(loc2);
        }

        return distanceCovered;
    }

    private void extractXYZ(ArrayList<SensorReading> readings, ArrayList<ArrayList<Double>> axis) {
        for (SensorReading reading : readings) {
            axis.get(0).add(reading.getValueOnXAxis());
            axis.get(1).add(reading.getValueOnYAxis());
            axis.get(2).add(reading.getValueOnZAxis());
        }
    }

    /**
     * Returns a vector containing the features
     * @return
     */
    public double[] getFeatureVec(String... keys) {
        double[] featureArr = new double[keys.length];
        int idx = 0;
        for (String s : keys) {
            Double feature = features.get(s);
            featureArr[idx++] =  feature == null ? 0 : feature;
        }
        return featureArr;
    }

    /**
     * Set prediction vector
     * @param predictions
     */
    public void setPredictions(float[] predictions) {
        this.predictions = predictions;
    }

    /**
     * Return most likely trip type based on prediction
     * @return most likely trip type
     */
    public TripType mostProbableTripType() {
        TripType mostProbableType = null;
        float maxProbability = -1;
        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] > maxProbability) {
                maxProbability = predictions[i];
                mostProbableType = TripType.values()[i];
            }
        }
        return mostProbableType;
    }

    /**
     * Get distance covered by during the scan period
     * @return
     */
    public double getDistanceCovered() {
        return totalDistanceCovered;
    }

    /**
     * Get CO2 emissions of during the scan period
     * @return CO2 emissions
     */
    public double getFootprint() {
        return Footprint.getEFof(mostProbableTripType()) * getDistanceCovered();
    }

    /**
     * Get start time of the scan
     * @return start time of the scan
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Get end time of the scan
     * @return end time of the scan
     */
    public long getEndTime() {
        return endTime;
    }

    public float[] getPredictions() {
        return predictions;
    }
}
