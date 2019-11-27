package ch.ethz.smartenergy.footprint;

import java.text.DecimalFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class Trip {
    private List<Leg> legs;
    private Double totalFootprint = null;
    private Double totalDistance = null;
    private Long totalTime = null;
    private Map<TripType, Integer> modesUsed = new HashMap<>();
    private List<TripType> modesUsedDescOrder = new ArrayList<>();
    private Integer numModesUsed = null;
    private Date date;

    /**
     * Constructs a trip with one or more legs
     * @param legs the legs to add to the trip
     */
    public Trip(List<Leg> legs) {
        addLegs(legs);
        this.date = Calendar.getInstance().getTime();
    }

    /**
     * Get legs in the trip
     * @return legs
     */
    public List<Leg> getLegs() {
        return legs;
    }

    /**
     * Adds one or more legs to the trip
     * @param legs the legs to add to the trip
     */
    public void addLegs(List<Leg> legs) {
        this.legs = new ArrayList<>(legs);
        for (Leg leg: legs) {
            TripType mode = leg.getMostProbableLegType();
            this.modesUsed.put(mode, modesUsed.getOrDefault(mode, 0) + 1);
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
                totalFootprint += leg.getLegFootprint();
        }
        return totalFootprint;
    }

    /**
     * Returns the string representation of the total footprint
     * @return String representation of distance travelled
     */
    public String getTotalFootprintAsString() {
        double footprint = this.getTotalFootprint();
        String value;
        String unit;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        if (footprint > 1_000) {
            // In kilograms
            value = df.format(footprint); // Diplay 2 decimals
            unit = "kg";
        } else {
            value = df.format((int) footprint);
            unit = "g";
        }
        return value + " " + unit;
    }

    /**
     * Returns the total distance covered by the trip
     * @return distance covered by the trip
     */
    public double getTotalDistance() {
        if (totalDistance == null) {
            totalDistance = 0.;
            for (Leg leg : this.legs)
                totalDistance += leg.getLegDistance();
        }
        return totalDistance;
    }

    /**
     * Returns the string representation of the total distance travelled
     * @return String representation of distance travelled
     */
    public String getTotalDistanceAsString() {
        double distance = this.getTotalDistance();
        String value;
        String unit;

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        if (distance > 1_000) {
            // In kilometers
            value = df.format(distance); // Diplay 2 decimals
            unit = "km";
        } else {
            value = df.format((int) distance);
            unit = "m";
        }
        return value + " " + unit;
    }

    /**
     * Returns the total time of the trip
     * @return distance time of the trip
     */
    public Long getTotalTime() {
        if (totalTime == null) {
            totalTime = 0L;
            for (Leg leg: this.legs)
                totalTime += leg.getLegTime();
        }
        return totalTime;
    }

    /**
     * Returns the string representation of the total duration of trip
     * @return String representation of distance travelled
     */
    public String getTotalTimeAsString() {
        int time = this.getTotalTime().intValue();

        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = (time / 60) / 60;

        StringBuilder sb = new StringBuilder();
        if (hours != 0) {
            sb.append(hours).append(" h, ");
        }
        if (minutes != 0) {
            sb.append(minutes).append(" min, ");
        }
        if (seconds != 0) {
            sb.append(seconds).append(" s");
        } else if (hours != 0 || minutes != 0) {
            // Removing the last ", "
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
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
            List<Map.Entry<TripType, Integer>> numUsedListEntry = new ArrayList<>(numUsedSet);

            // Sorting by descending use
            Collections.sort(numUsedListEntry,
                    (mode1, mode2) -> mode2.getValue().compareTo(mode1.getValue()));

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
        for (TripType mode: this.getModesUsed()) {
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
        if (this.getDate() == null) return "";
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(this.getDate());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
