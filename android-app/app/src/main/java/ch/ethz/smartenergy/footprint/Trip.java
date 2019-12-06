package ch.ethz.smartenergy.footprint;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Trip {
    private int id;
    private List<Leg> legs;
    private Double totalFootprint = null;
    private Double totalDistance = null;
    private Long totalTime = null;
    private Map<TripType, Integer> modesUsed = new HashMap<>();
    private List<TripType> modesUsedDescOrder = null;
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
        return getFootprintAsString(this.getTotalFootprint());
    }

    public static String getFootprintAsString(double footprint) {
        return Integer.toString((int)Math.round(footprint));
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
        return getDistanceAsString(this.getTotalDistance());
    }

    public static String getDistanceAsString(double distance) {
        return Integer.toString((int)Math.round(distance));
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
        long millis = this.getTotalTime();

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
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
        return getModesUsed().size();
    }

    public Map<TripType, Integer> getModesUsed() {
        return modesUsed;
    }

    /**
     * Returns the list of transportation modes used (by descending order)
     * @return the list of transportation modes used
     */
    public List<TripType> getModesUsedDescOrder() {
        if (modesUsedDescOrder == null || modesUsedDescOrder.size() != getNumModesUsed()) {
            // Getting the entrySet
            Set<Map.Entry<TripType, Integer>> numUsedSet = this.getModesUsed().entrySet();
            // Converting HashMap to List of Map entries
            List<Map.Entry<TripType, Integer>> numUsedListEntry = new ArrayList<>(numUsedSet);

            // Sorting by descending use
            Collections.sort(numUsedListEntry,
                    (mode1, mode2) -> mode2.getValue().compareTo(mode1.getValue()));

            // Storing
            modesUsedDescOrder = new ArrayList<TripType>();
            for (Map.Entry<TripType, Integer> map : numUsedListEntry) {
                modesUsedDescOrder.add(map.getKey());
            }
        }
        return modesUsedDescOrder;
    }

    /**
     * Returns a string representation of the transportation modes used (by descending order)
     * @return a string representation of the transportation modes used
     */
    public String getModesAsString() {
        if (!this.getModesUsedDescOrder().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (TripType mode: this.getModesUsedDescOrder()) {
                sb.append(mode.toString().toLowerCase()).append(", ");
            }

            // Removing the last ", "
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);

            return sb.toString();
        }
        return "";
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
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
        return df.format(this.getDate());
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
