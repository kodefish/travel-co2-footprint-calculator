package ch.ethz.smartenergy.footprint;

import java.util.ArrayList;

public class Trip {
    ArrayList<Leg> legs;

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
    public double getFootprint() {
        // Doesn't work because Android is bad and doesn't recognise funprog
        // this.legs.stream().forEach(leg -> leg.getFootprint()).reduce((double a, double b) -> a + b);

        double footprint = 0;
        for (Leg leg: this.legs) {
            footprint += leg.getFootprint();
        }
        return footprint;
    }
}
