package ch.ethz.smartenergy.footprint;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.smartenergy.model.FeatureVector;

public class Leg {
    private List<FeatureVector> featureVectorList;

    private Double totalLegDistance = null;
    private TripType mostProbableLegType = null;

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
        return "200m";
    }

    /**
     * TODO Return leg emissions as pretty string (kg or g)
     * @return leg emissions as pretty string (kg or g)
     */
    public String getLegEmissionsAsString() {
        return "200g";
    }
}
