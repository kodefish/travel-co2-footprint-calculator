package ch.ethz.smartenergy.footprint;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.smartenergy.model.FeatureVector;

public class Leg {
    private List<FeatureVector> featureVectorList;

    private Double totalLegDistance = null;
    private TripType mostProbableLegType = null;
    private Long legStartTime = null;
    private Long legEndTime = null;
    private Long totalLegTime = null;
    private Double totalLegFootprint = null;

    public Leg(List<FeatureVector> featureVectors) {
        this.featureVectorList = new ArrayList<>(featureVectors);
    }

    /**
     * Computes total distance covered by leg by summing the distance between each
     * sensor reading
     * @return distance covered during the leg
     */
    public double getLegDistance() {
        if (totalLegDistance == null) {
            totalLegDistance = 0.;
            for (FeatureVector featureVector : featureVectorList)
                totalLegDistance += featureVector.getDistanceCovered();
        }
        return totalLegDistance;
    }

    /**
     * Returns the leg length as a string
     * @return the leg length as a string
     */
    public String getLegDistanceAsString() {
        double distance = this.getLegDistance();
        int value;
        String unit;
        if (distance > 1_000) {
            // In kilometers
            value = ((int) (distance * 100)) / 100; // Diplay 2 decimals
            unit = "km";
        } else {
            value = (int) distance;
            unit = "m";
        }
        return value + " " + unit;
    }

    /**
     * Returns the starting time of the leg
     * @return the starting time of the leg
     */
    public long getLegStartTime() {
        return featureVectorList.get(0).getStartTime();
    }

    /**
     * Returns the end time of the leg
     * @return the end time of the leg
     */
    public long getLegEndTime() {
        return featureVectorList.get(featureVectorList.size()-1).getEndTime();
    }

    /**
     * Computes total time elapsed by leg by summing the time between each
     * sensor reading  (in millis)
     * @return time elapsed during the leg (in millis)
     */
    public long getLegTime() {
        if (totalLegTime == null) {
            totalLegTime = this.getLegEndTime() - this.getLegStartTime();
        }
        return totalLegTime;
    }

    /**
     * Returns the leg time as a string (in seconds)
     * @return the leg time as a string (in seconds)
     */
    public String getLegTimeAsString() {
        int time = (int) this.getLegTime();

        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = (time / 60) / 60;

        StringBuilder sb = new StringBuilder();
        if (hours != 0) {
            sb.append(hours).append(" h, ");
        }
        if (minutes != 0) {
            sb.append(minutes).append(" min, ");
        }
        if (seconds != 0) {
            sb.append(seconds).append(" s");
        } else {
            // Removing the last ", "
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    /**
     * Returns the footprint of this leg in g cO2 / km
     * @return the footprint of this leg
     */
    public double getLegFootprint() {
        if (this.totalLegFootprint == null) {
            this.totalLegFootprint = Footprint.getEFof(getMostProbableLegType()) * getLegDistance();
        }
        return this.totalLegFootprint;
    }

    /**
     * Returns the leg emissions as a string
     * @return the leg emissions as a string
     */
    public String getLegFootprintAsString() {
        double footprint = this.getLegFootprint();
        int value;
        String unit;
        if (footprint > 1_000) {
            // In kilograms
            value = ((int) (footprint * 100)) / 100; // Diplay 2 decimals
            unit = "kg";
        } else {
            value = (int) footprint;
            unit = "g";
        }
        return value + " " + unit;
    }

    public TripType getMostProbableLegType() {
        // TODO more sophisticated way of computing the trip type (most frequent)
        if (mostProbableLegType == null)
            mostProbableLegType = featureVectorList.get(0).mostProbableTripType();
        return mostProbableLegType;
    }

    public List<FeatureVector> getFeatureVectorList() {
        return featureVectorList;
    }
}
