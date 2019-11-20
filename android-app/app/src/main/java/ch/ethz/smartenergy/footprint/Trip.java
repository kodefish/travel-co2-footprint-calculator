package ch.ethz.smartenergy.footprint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class Trip {
    List<Leg> legs;
    Double totalFootprint = null;
    Double totalDistance = null;
    Map<TripType, Integer> modesUsed;
    List<TripType> modesUsedDescOrder = null;
    Integer numModesUsed = null;
    Date date;

    /**
     * Constructs a trip with one or more legs
     * @param legs the legs to add to the trip
     */
    public Trip(List<Leg> legs) {
        this.legs = new ArrayList<>(legs);
        this.modesUsed = new HashMap<TripType, Integer>();
        this.date = Calendar.getInstance().getTime();

        for (Leg leg: legs) {
            TripType mode = leg.getMostProbableLegType();
            this.modesUsed.put(mode, modesUsed.get(mode) + 1);
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
     * Returns the number of transportation modes used
     * @return the number of transportation modes used
     */
    public int getNumModesUsed() {
        if (numModesUsed == null) {
            numModesUsed = 0;
            for (Leg leg: this.legs) {
                if (this.modesUsed.get(leg.getMostProbableLegType()) != 0) {
                    numModesUsed++;
                }
            }
        }
        return numModesUsed;
    }

    /**
     * Returns the list of transportation modes used (by descending order)
     * @return the list of transportation modes used
     */
    public List<TripType> getModesUsed() {
        if (modesUsedDescOrder == null) {
            // Getting the entrySet
            Set<Map.Entry<TripType, Integer>> numUsedSet = this.modesUsed.entrySet();
            // Converting HashMap to List of Map entries
            List<Map.Entry<TripType, Integer>> numUsedListEntry = new ArrayList<Map.Entry<TripType, Integer>>(numUsedSet);

            // Sorting by descending use
            Collections.sort(numUsedListEntry,
                    new Comparator<Map.Entry<TripType, Integer>>() {

                        @Override
                        public int compare(Map.Entry<TripType, Integer> mode1,
                                           Map.Entry<TripType, Integer> mode2) {
                            return mode2.getValue().compareTo(mode1.getValue());
                        }
                    });

            // Storing
            List<TripType> asList = new ArrayList<TripType>();
            for (Map.Entry<TripType, Integer> map : numUsedListEntry) {
                asList.add(map.getKey());
            }

            this.modesUsedDescOrder = asList;
        }
        return this.modesUsedDescOrder;
    }

    /**
     * Returns a string representation of the transportation modes used (by descending order)
     * @return a string representation of the transportation modes used
     */
    public String getModesAsString() {
        StringBuilder sb = new StringBuilder();
        for (TripType mode: this.modesUsedDescOrder) {
            sb.append(mode.toString().toLowerCase()).append(", ");
        }

        // Removing the last ", "
        sb.deleteCharAt(sb.length() - 1);
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Returns the date on which the trip was made
     * @return the date on which the trip was made
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Returns a dd-MM-yyyy string representation of this trip's date
     * @return a dd-MM-yyyy string representation of this trip's date
     */
    public String getDateAsString() {
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(this.date);
    }
}
