package ch.ethz.smartenergy.footprint;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    List<Leg> legs;
    Double totalFootprint = null;
    Double totalDistance = null;

    /**
     * Constructs a trip with no legs
     */
    public Trip() {
        legs = new ArrayList<Leg>();
    }

    /**
     * Constructs a trip with one or more legs
     * @param legs the legs to add to the trip
     */
    public Trip(List<Leg> legs) {
        this.legs = new ArrayList<>(legs);
    }

    /**
     * Constructs a trip with one or more legs
     * @param legs the legs to add to the trip
     */
    public Trip(Leg... legs) {
        this();
        this.addLegs(legs);
    }

    /**
     * Adds one or more legs to the trip
     * @param legs the legs to add to the trip
     */
    public void addLegs(Leg... legs) {
        for (Leg leg: legs) {
            this.legs.add(leg);
        }
    }

    /**
     * Returns the cumulated footprint of all the legs of this trip
     * @return the footprint of this trip
     */
    public double getTotalFootprint() {
        if (totalFootprint == null) {
            totalFootprint = 0.;
            for (Leg leg: this.legs)
                totalFootprint += leg.getFootprint();
        }
        return totalFootprint;
    }

    /**
     * Returns the total distance covered by the trip
     * @return distance covered by the trip
     */
    public double getTotalDistance() {
        if (totalDistance == null) {
            totalDistance = 0.;
            for (Leg leg : this.legs)
                totalDistance += leg.getLegLength();
        }
        return totalDistance;
    }

    /**
     * Get number of legs in the trip
     * @return number of legs in the trip
     */
    public int getNumLegs() {
        return this.legs.size();
    }


}
