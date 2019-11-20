package ch.ethz.smartenergy.footprint;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.smartenergy.model.FeatureVector;

public class Leg {
    private List<FeatureVector> featureVectorList;

    private Double totalLegDistance = null;
    private TripType mostProbableLegType = null;
    private Double totalLegTime = null;

    public Leg(List<FeatureVector> featureVectors) {
        this.featureVectorList = new ArrayList<>(featureVectors);
    }

    /**
     * Computes total distance covered by leg by summing the distance between each
     * sensor reading
     * @return distance covered during the leg
     */
    public double getLegLength() {
        if (totalLegDistance == null) {
            totalLegDistance = 0.;
            for (FeatureVector featureVector : featureVectorList)
                totalLegDistance += featureVector.getDistanceCovered();
        }
        return totalLegDistance;
    }

    /**
     * Computes total time elapsed by leg by summing the time between each
     * sensor reading
     * @return time elapsed during the leg
     */
    public double getLegTime() {
        if (totalLegTime == null) {
            totalLegTime = 0.;
            // TODO: Get time from featureVector
        }
        return totalLegTime;
    }


    public TripType getMostProbableLegType() {
        // TODO more sophisticated way of computing the trip type (most frequent)
        if (mostProbableLegType == null)
            mostProbableLegType = featureVectorList.get(0).mostProbableTripType();
        return mostProbableLegType;
    }

    /**
     * Returns the footprint of this leg in g cO2 / km
     * @return the footprint of this leg
     */
    public double getFootprint() {
        return Footprint.getEFof(getMostProbableLegType()) * getLegLength();
    }

    /**
     * TODO Return leg length as pretty string (km or m)
     * @return length as pretty string (km or m)
     */
    public String getLegLengthAsString() {
        int value;
        String unit;
        if (this.totalLegDistance > 1_000) {
            // In kilometers
            value = (this.totalLegDistance.intValue() * 100) / 100; // Diplay 2 decimals
            unit = "km";
        } else {
            value = this.totalLegDistance.intValue();
            unit = "m";
        }
        return value + " " + unit;
    }

    /**
     * TODO Return leg emissions as pretty string (kg or g)
     * @return leg emissions as pretty string (kg or g)
     */
    public String getLegEmissionsAsString() {
        return "200g";
    }
}
