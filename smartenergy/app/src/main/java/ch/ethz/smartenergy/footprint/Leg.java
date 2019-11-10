package ch.ethz.smartenergy.footprint;

public class Leg {
    double distance; // Given in km
    TripType type;

    public Leg(double distance, TripType type) {
        this.distance = distance;
        this.type = type;
    }

    /**
     * Returns the footprint of this leg in g cO2 / km
     * @return the footprint of this leg
     */
    public double getFootprint() {
        return Footprint.getEFof(this.type) * this.distance;
    }
}
