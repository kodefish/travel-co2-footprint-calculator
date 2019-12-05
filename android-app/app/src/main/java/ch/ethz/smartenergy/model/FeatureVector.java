package ch.ethz.smartenergy.model;

import android.location.Location;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.smartenergy.features.FeatureExtractor;
import ch.ethz.smartenergy.footprint.Footprint;
import ch.ethz.smartenergy.footprint.TripType;
import ch.ethz.smartenergy.service.SensorScanPeriod;

public class FeatureVector {

    // Constants used to reference features
    public static final String FEATURE_KEY_ACC_MEAN_MAGNITUDE = "acc_mean";
    public static final String FEATURE_KEY_AVG_CON_BT = "avg_con_bt";
    public static final String FEATURE_KEY_GYRO_MEAN_MAGNITUDE = "gyro_mean";
    public static final String FEATURE_KEY_MAX_SPEED = "max_speed";
    public static final String FEATURE_KEY_AVG_SPEED = "avg_speed";
    public static final String FEATURE_KEY_DISTANCE_COVERED = "distance_travelled";
    public static final String FEATURE_KEY_MAG_MEAN_MAGNITUDE = "mag_mean";

    public static final String FEATURE_PREFIX_ACC_MIXED = "acc_mixed_";
    public static final String FEATURE_PREFIX_GYRO_MIXED = "gyro_mixed_";

    // Hard coded feature labels to ensure correct order
    public static final String[] FeatureLabels = {
            FEATURE_KEY_ACC_MEAN_MAGNITUDE,
            FEATURE_KEY_AVG_CON_BT,
            FEATURE_KEY_GYRO_MEAN_MAGNITUDE,
            FEATURE_KEY_MAX_SPEED,
            FEATURE_KEY_AVG_SPEED,
            FEATURE_KEY_DISTANCE_COVERED,
            FEATURE_KEY_MAG_MEAN_MAGNITUDE,
            "acc_mixed_0",
            "acc_mixed_1",
            "acc_mixed_2",
            "acc_mixed_3",
            "acc_mixed_4",
            "acc_mixed_5",
            "acc_mixed_6",
            "acc_mixed_7",
            "acc_mixed_8",
            "acc_mixed_9",
            "acc_mixed_10",
            "acc_mixed_11",
            "acc_mixed_12",
            "acc_mixed_13",
            "acc_mixed_14",
            "acc_mixed_15",
            "acc_mixed_16",
            "acc_mixed_17",
            "acc_mixed_18",
            "acc_mixed_19",
            "acc_mixed_20",
            "acc_mixed_21",
            "acc_mixed_22",
            "acc_mixed_23",
            "acc_mixed_24",
            "acc_mixed_25",
            "acc_mixed_26",
            "acc_mixed_27",
            "acc_mixed_28",
            "acc_mixed_29",
            "gyro_mixed_0",
            "gyro_mixed_1",
            "gyro_mixed_2",
            "gyro_mixed_3",
            "gyro_mixed_4",
            "gyro_mixed_5",
            "gyro_mixed_6",
            "gyro_mixed_7",
            "gyro_mixed_8",
            "gyro_mixed_9",
            "gyro_mixed_10",
            "gyro_mixed_11",
            "gyro_mixed_12",
            "gyro_mixed_13",
            "gyro_mixed_14",
            "gyro_mixed_15",
            "gyro_mixed_16",
            "gyro_mixed_17",
            "gyro_mixed_18",
            "gyro_mixed_19",
            "gyro_mixed_20",
            "gyro_mixed_21",
            "gyro_mixed_22",
            "gyro_mixed_23",
            "gyro_mixed_24",
            "gyro_mixed_25",
            "gyro_mixed_26",
            "gyro_mixed_27",
            "gyro_mixed_28",
            "gyro_mixed_29"
    };

    // Computed features for decison tree
    private transient final Map<String, Double> features;

    // Represents how sure we are that prediction[i] is of type TripType[i]
    private float[] predictions;

    private final double totalDistanceCovered;

    // Time when scan result was started and finalized
    private final long startTime;
    private final long endTime;

    // Save all locations of feature vec
    List<Pair<Double, Double>> locations;

    /**
     * Construct a feature vector based on raw sensor values
     * @param scanResult resulting scan values
     */
    public FeatureVector(ScanResult scanResult) {
        // Save start and end time
        this.startTime = scanResult.getStartTime();
        this.endTime = scanResult.getEndTime();

        // Save start and end locations
        locations = getLocationsFromLocationScan(scanResult);

        features = new HashMap<>();

        // Accelerator magnitude mean
        double meanMagnitude = calculateMeanMagnitude(scanResult.getAccReadings());
        features.put(FEATURE_KEY_ACC_MEAN_MAGNITUDE, meanMagnitude);

        // average connected bluetooth devices (for each scanID within this window, look at #devices and then take average over that)
        double avgConBT = calculateAvgConBT(scanResult.getBluetoothScans());
        features.put(FEATURE_KEY_AVG_CON_BT, avgConBT);

        // Gyro magnitude mean
        double gyroMeanMagnitude = calculateMeanMagnitude(scanResult.getGyroReadings());
        features.put(FEATURE_KEY_GYRO_MEAN_MAGNITUDE, gyroMeanMagnitude);

        // max speed
        Pair<Double, Double> maxAndAvgSpeed = calculateMaxAndAvgSpeed(scanResult.getLocationScans());
        features.put(FEATURE_KEY_MAX_SPEED, maxAndAvgSpeed.first);

        // average speed
        features.put(FEATURE_KEY_AVG_SPEED, maxAndAvgSpeed.second);

        // magnetic field magnitude mean
        double magnMeanMagnitude = calculateMeanMagnitude(scanResult.getMagnReadings());
        features.put(FEATURE_KEY_MAG_MEAN_MAGNITUDE, magnMeanMagnitude);

        // distance covered (this is not implemented in the ML model [yet])
        totalDistanceCovered = calculateDistanceCovered(scanResult.getLocationScans());
        features.put(FEATURE_KEY_DISTANCE_COVERED, totalDistanceCovered);

        // acc_mixed
        ArrayList<SensorReading> acc = scanResult.getAccReadings();
        ArrayList<Double> accx = new ArrayList<Double>();
        ArrayList<Double> accy = new ArrayList<Double>();
        ArrayList<Double> accz = new ArrayList<Double>();
        for (SensorReading reading : acc) {
            accx.add(reading.getValueOnXAxis());
            accy.add(reading.getValueOnYAxis());
            accz.add(reading.getValueOnZAxis());
        }

        ArrayList<Double> acc_mixed = new ArrayList<Double>();
        acc_mixed.addAll(FeatureExtractor.extract_features(accx, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE));
        acc_mixed.addAll(FeatureExtractor.extract_features(accy, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE));
        acc_mixed.addAll(FeatureExtractor.extract_features(accz, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE));


        int cur = 7; // Start at 7 (index of acc_mixed_0), work our way up to 37
        for (Double val : acc_mixed) {
            features.put(FeatureLabels[cur++], val);
        }

        // gyro_mixed
        ArrayList<SensorReading> gyro = scanResult.getGyroReadings();
        ArrayList<Double> gyrox = new ArrayList<Double>();
        ArrayList<Double> gyroy = new ArrayList<Double>();
        ArrayList<Double> gyroz = new ArrayList<Double>();
        for (SensorReading reading : gyro) {
            gyrox.add(reading.getValueOnXAxis());
            gyroy.add(reading.getValueOnYAxis());
            gyroz.add(reading.getValueOnZAxis());
        }

        ArrayList<Double> gyro_mixed = new ArrayList<Double>();
        gyro_mixed.addAll(FeatureExtractor.extract_features(gyrox, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE));
        gyro_mixed.addAll(FeatureExtractor.extract_features(gyroy, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE));
        gyro_mixed.addAll(FeatureExtractor.extract_features(gyroz, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE));

        // cur is still valid
        for (Double val : gyro_mixed) {
            features.put(FeatureLabels[cur++], val);
        }
    }

    private List<Pair<Double, Double>> getLocationsFromLocationScan(ScanResult scanResult) {
        List<Pair<Double, Double>> locations = new ArrayList<>();
        for (LocationScan l : scanResult.getLocationScans()) {
            locations.add(new Pair<>(l.getLatitude(), l.getLongitude()));
        }
        return locations;
    }


    private Pair<Double, Double> calculateMaxAndAvgSpeed(ArrayList<LocationScan> locationScans) {
        double maxSpeed = 0;
        double avgSpeed = 0;
        for (LocationScan locationScan : locationScans) {
            if (locationScan.getSpeed() > maxSpeed) {
                maxSpeed = locationScan.getSpeed();
            }
            avgSpeed += locationScan.getSpeed();
        }
        avgSpeed /= locationScans.size();

        return new Pair<>(maxSpeed, avgSpeed);
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

    private double calculateAvgConBT(ArrayList<BluetoothScan> btScans) {
        double avg = 0;
        for (BluetoothScan reading : btScans)
            avg += reading.getDiscoveredDevices().size();
        avg /= btScans.size();
        return avg;
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
    public double[] getFeatureVec() {
        double[] featureArr = new double[FeatureLabels.length];
        int idx = 0;
        for (String s : FeatureLabels) {
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

    /**
     * Checks if the user is even moving (ie his speed is greater than 0.5m/s
     * @return true if the user is moving
     */
    public boolean isMoving() {
        return features.getOrDefault(FEATURE_KEY_AVG_SPEED, 0.) > 0.5;
    }

    public List<Pair<Double, Double>> getLocations() {
        return this.locations;
    }
}
