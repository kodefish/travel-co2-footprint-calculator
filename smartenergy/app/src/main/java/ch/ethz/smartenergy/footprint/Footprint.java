package ch.ethz.smartenergy.footprint;

public class Footprint {

    /**
     * Returns the Emission Factor (EF) of the type of transport (in g CO2 / km)
     *
     * Data comes from:
     * Car: https://www.eea.europa.eu/highlights/average-co2-emissions-from-new
     * Bike: https://ecf.com/news-and-events/news/how-much-co2-does-cycling-really-save
     * E-Bikes: Same as bikes (for lack of additional data
     * Motorcycles: https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=10&cad=rja&uact=8&ved=2ahUKEwjswZ_d6eblAhXWw8QBHdSzAuIQFjAJegQIBBAC&url=https%3A%2F%2Fwww.lowcvp.org.uk%2Fassets%2Fpresentations%2FMCI%2520-%2520Greg%2520Archer.pdf&usg=AOvVaw3jXJ9ZdEBkZ0JHN3ikOLXE
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
            case EBIKE: // Same as bike
            case BIKE: return 21;
            case MOTORCYCLE: return 110;
            default: return -1; // Should not happen, but default is needed
        }
    }
}
