package ch.ethz.smartenergy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.smartenergy.footprint.TripType;

public class FeatureVector {

    // Constants used to reference features
    public static final String FEATURE_KEY_MEAN_MAGNITUDE = "meanMagnitude";
    public static final String FEATURE_KEY_MAX_SPEED = "maxSpeed";
    public static final String FEATURE_KEY_DISTANCE_COVERED = "distanceCovered";

    // Raw scan results from the sensor
    private ScanResult scanResult;

    // Computed features for decison tree
    private Map<String, Double> features;

    // Represents how sure we are that prediction[i] is of type TripType[i]
    private float[] predictions;

    /**
     * Construct a feature vector based on raw sensor values
     * @param scanResult resulting scan values
     */
    public FeatureVector(ScanResult scanResult) {
        this.scanResult = scanResult;
        features = new HashMap<>();
    }

    /**
     * Add feature to the feature vector
     * @param name name of the feature
     * @param value value of the feature
     */
    public void addFeature(String name, Double value) {
        features.put(name, value);
    }

    /**
     * Get a list of the feature names, in the same order as if it were values
     * @return list of feature names
     */
    public List<String> getFeatureNames() {
        List<String> featureNames = new ArrayList<>();
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            featureNames.add(entry.getKey());
        }
        return featureNames;
    }

    /**
     * Returns a vector containing the features
     * @return
     */
    public double[] getFeatureVec() {
        // TODO specify order in which the feature vec should be given to the decision tree
        double[] featureArr = new double[features.size()];
        int idx = 0;
        for (Map.Entry<String, Double> entry : features.entrySet()) {
            featureArr[idx++] = entry.getValue();
        }

        return featureArr;
    }

    public void setPredictions(float[] predictions) {
        this.predictions = predictions;
    }

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

    public double getDistanceCovered() {
        Double distance = features.get(this.FEATURE_KEY_DISTANCE_COVERED);
        return distance;
    }
}