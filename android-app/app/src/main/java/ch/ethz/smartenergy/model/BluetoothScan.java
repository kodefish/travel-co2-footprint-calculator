package ch.ethz.smartenergy.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class BluetoothScan implements Serializable {
    private Date timeOfReading;

    private ArrayList<DiscoveredBluetooth> discoveredDevices;

    public BluetoothScan(Date timeOfReading) {
        this.timeOfReading = timeOfReading;
        discoveredDevices = new ArrayList<>();
    }

    public Date getTimeOfReading() {
        return timeOfReading;
    }

    public void setTimeOfReading(Date timeOfReading) {
        this.timeOfReading = timeOfReading;
    }

    public ArrayList<DiscoveredBluetooth> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void setDiscoveredDevices(ArrayList<DiscoveredBluetooth> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }
}
