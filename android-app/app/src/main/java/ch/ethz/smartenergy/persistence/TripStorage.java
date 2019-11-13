package ch.ethz.smartenergy.persistence;

import android.content.Context;
import android.widget.Button;

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
import java.util.List;

import ch.ethz.smartenergy.footprint.Trip;

public class TripStorage {
    // Filename of json file that stores all the tips made by the user
    private final String JSON_TRIP_STORAGE_FILENAME = "trips.json";

    private final File jsonFile;

    /**
     * Creates a TripStorage object which can be used to interact with the persisted trip data
     * Trip data is stored in JSON format in the internal memory of the device, which is private
     * @param context
     */
    public TripStorage(Context context) {
        // Gets or creates file for trips in internal private storage
        jsonFile = new File(context.getFilesDir(), JSON_TRIP_STORAGE_FILENAME);

        // Create the file if it doesn't already exist
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
                // Write an empty list to the the json file
                List<Trip> emptyList = new ArrayList<>();
                writeTrips(emptyList);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fetches the list of stored trips
     * @return List of trips stored on the internal memory
     * @throws FileNotFoundException
     */
    public List<Trip> getAllStoredTrips() throws FileNotFoundException {
        // Read json file to get list of trips
        Type listOfTripsType = new TypeToken<ArrayList<Trip>>() {}.getType();
        Gson gson = new Gson();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(jsonFile));
        List<Trip> storedTrips = gson.fromJson(bufferedReader, listOfTripsType);
        if (storedTrips == null) storedTrips = new ArrayList<>();
        return storedTrips;
    }

    /**
     * Writes the list of trips, replacing the contents of whatever was there before
     * @param trips to be written to memory
     * @throws IOException
     */
    private void writeTrips(List<Trip> trips) throws IOException {
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(jsonFile);
        writer.write(gson.toJson(trips));
        writer.close();
    }

    /**
     * Adds a trip to a the list of persisted trips
     * @param trip
     * @throws IOException
     */
    public void persistTrip(Trip trip) throws IOException {
        // Read json file to get list of trips
        List<Trip> storedTrips = getAllStoredTrips();

        // Append trip to deserialized list
        storedTrips.add(trip);

        // Serialize updated list and write back to storage
        writeTrips(storedTrips);
    }
}
