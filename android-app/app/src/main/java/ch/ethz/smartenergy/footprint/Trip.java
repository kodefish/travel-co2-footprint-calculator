package ch.ethz.smartenergy.footprint;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

public class Trip {
    List<Leg> legs;
    Double totalFootprint = null;
    Double totalDistance = null;
    List<TripType> modesUsed;
    Date date;

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
        this.modesUsed = new ArrayList<TripType>();
        for (Leg leg: legs) {
            if (!modesUsed.contains(leg.getMostProbableLegType())) {
                modesUsed.add(leg.getMostProbableLegType());
            }
        }

        this.date = Calendar.getInstance().getTime();
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

            if (!modesUsed.contains(leg.getMostProbableLegType())) {
                modesUsed.add(leg.getMostProbableLegType());
            }
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

    /**
     * Returns the list of transportation modes used
     * @return the list of transportation modes used
     */
    public List<TripType> getModesUsed() {
        return (List<TripType>) ((ArrayList<TripType>) this.modesUsed).clone();
    }

    /**
     * Returns the date on which the trip was made
     * @return the date on which the trip was made
     */
    public Date getDate() {
        return this.date;
    }
}
