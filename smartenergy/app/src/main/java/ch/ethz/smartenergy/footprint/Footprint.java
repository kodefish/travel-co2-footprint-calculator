package ch.ethz.smartenergy.footprint;

public class Footprint {

    /**
     * Returns the Emission Factor (EF) of the type of transport (in g CO2 / km)
     *
     * Data comes from:
     * Car: https://www.eea.europa.eu/highlights/average-co2-emissions-from-new
     * Bike: https://ecf.com/news-and-events/news/how-much-co2-does-cycling-really-save
     * E-Bikes: TODO
     * Motorcycles: TODO
     * Everything else: https://jamesrivertrans.com/wp-content/uploads/2012/05/ComparativeEnergy.pdf
     *
     * @param tripType the type of transport
     * @return the emission factor of the type of transport (in g CO2 / km)
     */
    public static double getEFof(TripType tripType) {
        switch (tripType) {
            case FOOT: return 0;
            case TRAIN: return 111.18;
            case BUS: return 144.72;
            case CAR: return 123.4;
            case TRAM: return 125.47;
            case BIKE: return 21;
            case EBIKE: return 0; // TODO
            case MOTORCYCLE: return 0; // TODO
            default: return -1; // Should not happen, but default is needed
        }
    }
}
