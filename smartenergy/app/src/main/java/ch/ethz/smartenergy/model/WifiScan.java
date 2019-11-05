package ch.ethz.smartenergy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class WifiScan implements Serializable {

    private Date timeOfReading;

    private ArrayList<DiscoveredWifi> discoveredDevices;

    public WifiScan(Date timeOfReading) {
        this.timeOfReading = timeOfReading;
        this.discoveredDevices = new ArrayList<>();
    }

    public Date getTimeOfReading() {
        return timeOfReading;
    }

    public void setTimeOfReading(Date timeOfReading) {
        this.timeOfReading = timeOfReading;
    }

    public ArrayList<DiscoveredWifi> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void setDiscoveredDevices(ArrayList<DiscoveredWifi> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }
}
