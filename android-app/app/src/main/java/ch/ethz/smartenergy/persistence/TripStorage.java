package ch.ethz.smartenergy.persistence;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.ethz.smartenergy.footprint.Trip;

public class TripStorage {

    private static TripStorage tripStorage = null;

    // Filename of json file that stores all the tips made by the user
    private final String JSON_TRIP_STORAGE_FILENAME = "trips.json";

    private final File jsonFile;
    private BufferedReader bufferedReader;

    private List<Trip> storedTrips = null;

    public static TripStorage getInstance(Context context) {
        if (tripStorage == null) tripStorage = new TripStorage(context);
        return tripStorage;
    }

    /**
     * Creates a TripStorage object which can be used to interact with the persisted trip data
     * Trip data is stored in JSON format in the internal memory of the device, which is private
     *
     * @param context
     */
    private TripStorage(Context context) {
        // Gets or creates file for trips in internal private storage
        jsonFile = new File(context.getFilesDir(), JSON_TRIP_STORAGE_FILENAME);

        // Create the file if it doesn't already exist
        try {
            if (!jsonFile.exists()) {
                boolean s = jsonFile.createNewFile();

                // Write an empty list to the the json file
                List<Trip> emptyList = new ArrayList<>();
                writeTrips(emptyList);
            }

            bufferedReader = new BufferedReader(new FileReader(jsonFile));

            // Load stored trips
            storedTrips = getAllStoredTrips();

        } catch (IOException e) {
            e.printStackTrace();
            bufferedReader = null;
        }
    }

    /**
     * Fetches the list of stored trips
     *
     * @return List of trips stored on the internal memory
     * @throws FileNotFoundException
     */
    public List<Trip> getAllStoredTrips() {
        if (storedTrips == null) {
            // Read json file to get list of trips
            Type listOfTripsType = new TypeToken<ArrayList<Trip>>() {}.getType();
            Gson gson = new Gson();
            storedTrips = gson.fromJson(bufferedReader, listOfTripsType);
            if (storedTrips == null) storedTrips = new ArrayList<>();
            Log.i("TripStorage", "read " + storedTrips.size() + " trips from storage");

            // close the file as the trips are now in memory
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return storedTrips;
    }

    /**
     * Writes the list of trips, replacing the contents of whatever was there before
     *
     * @param trips to be written to memory
     * @throws IOException
     */
    private void writeTrips(List<Trip> trips) throws IOException {
        for (int i = 0; i < trips.size(); i++) {
            trips.get(i).setId(i);
        }
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(jsonFile);
        writer.write(gson.toJson(trips));
        writer.close();
    }

    /**
     * Adds a trip to a the list of persisted trips
     *
     * @param trip
     * @throws IOException
     */
    public void persistTrip(Trip trip) throws IOException {
        // Append trip to deserialized list
        storedTrips.add(trip);

        // Serialize updated list and write back to storage
        writeTrips(storedTrips);
    }

    /**
     * Get last stored trip
     *
     * @return last stored trip
     */
    public Trip getLastTrip() {
        return getTripById(getAllStoredTrips().size() - 1);
    }

    public Trip getTripById(int id) {
        List<Trip> storedTrips = getAllStoredTrips();
        return storedTrips.get(id);
    }

    public List<Trip> deleteTrip(int i) throws IOException {
        this.storedTrips.remove(i);
        writeTrips(this.storedTrips);
        return this.storedTrips;
    }

    public List<Trip> getTripByDate(long timeInMillis) {
        List<Trip> sameDayTrips = new ArrayList<>();
        for (Trip t : getAllStoredTrips()) {
            Date date = new Date();
            date.setTime(timeInMillis);

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date);
            cal2.setTime(t.getDate());

            boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                    cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);

            if (sameDay)
                sameDayTrips.add(t);
        }
        return sameDayTrips;
    }
}
