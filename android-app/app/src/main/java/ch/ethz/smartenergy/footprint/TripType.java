package ch.ethz.smartenergy.footprint;

import ch.ethz.smartenergy.R;

public enum TripType {
    FOOT,
    TRAIN,
    BUS,
    CAR,
    TRAM,
    BIKE,
    EBIKE,
    MOTORCYCLE;


    /**
     * Get icon resource based on trip type
     * @param type of trip
     * @return resource id of icon trip
     */
    public static int getTripTypeIconResource(TripType type) {
        int iconResource = 0;
        switch (type) {
            case FOOT:
                iconResource = R.drawable.icon_foot; break;
            case TRAIN:
                iconResource = R.drawable.icon_train; break;
            case BUS:
                iconResource = R.drawable.icon_bus; break;
            case CAR:
                iconResource = R.drawable.icon_car; break;
            case TRAM:
                iconResource = R.drawable.icon_tram; break;
            case BIKE:
                iconResource = R.drawable.icon_bike; break;
            case EBIKE:
                iconResource = R.drawable.icon_ebike; break;
            case MOTORCYCLE:
                iconResource = R.drawable.icon_motorcycle; break;
        }
        return iconResource;
    }

}
