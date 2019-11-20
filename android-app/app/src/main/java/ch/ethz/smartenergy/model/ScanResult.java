package ch.ethz.smartenergy.model;

import android.os.SystemClock;

import java.io.Serializable;
import java.util.ArrayList;

public class ScanResult implements Serializable {

    private ArrayList<BluetoothScan> bluetoothScans;

    private ArrayList<WifiScan> wifiScans;

    private ArrayList<LocationScan> locationScans;

    private ArrayList<SensorReading> accReadings;

    private ArrayList<SensorReading> gyroReadings;

    private ArrayList<SensorReading> magnReadings;

    private long startTime;
    private long endTime;

    public ScanResult() {
        this.bluetoothScans = new ArrayList<>();
        this.wifiScans = new ArrayList<>();
        this.locationScans = new ArrayList<>();
        this.accReadings = new ArrayList<>();
        this.gyroReadings = new ArrayList<>();
        this.magnReadings = new ArrayList<>();
        this.startTime = SystemClock.elapsedRealtime();
        this.endTime = SystemClock.elapsedRealtime();
    }

    public void clear() {
        bluetoothScans.clear();
        wifiScans.clear();
        locationScans.clear();
        accReadings.clear();
        gyroReadings.clear();
        magnReadings.clear();
        startTime = SystemClock.elapsedRealtime();
        endTime = SystemClock.elapsedRealtime();
    }

    public ArrayList<BluetoothScan> getBluetoothScans() {
        return bluetoothScans;
    }

    public void setBluetoothScans(ArrayList<BluetoothScan> bluetoothScans) {
        this.bluetoothScans = bluetoothScans;
    }

    public ArrayList<WifiScan> getWifiScans() {
        return wifiScans;
    }

    public void setWifiScans(ArrayList<WifiScan> wifiScans) {
        this.wifiScans = wifiScans;
    }

    public ArrayList<LocationScan> getLocationScans() {
        return locationScans;
    }

    public void setLocationScans(ArrayList<LocationScan> locationScans) {
        this.locationScans = locationScans;
    }

    public ArrayList<SensorReading> getAccReadings() {
        return accReadings;
    }

    public void setAccReadings(ArrayList<SensorReading> accReadings) {
        this.accReadings = accReadings;
    }

    public ArrayList<SensorReading> getGyroReadings() {
        return gyroReadings;
    }

    public void setGyroReadings(ArrayList<SensorReading> gyroReadings) {
        this.gyroReadings = gyroReadings;
    }

    public ArrayList<SensorReading> getMagnReadings() {
        return magnReadings;
    }

    public void setMagnReadings(ArrayList<SensorReading> magnReadings) {
        this.magnReadings = magnReadings;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public ScanResult copy() {
        ScanResult copiedResult = new ScanResult();
        copiedResult.setAccReadings((ArrayList<SensorReading>) this.getAccReadings().clone());
        copiedResult.setGyroReadings((ArrayList<SensorReading>) this.getGyroReadings().clone());
        copiedResult.setMagnReadings((ArrayList<SensorReading>) this.getMagnReadings().clone());
        copiedResult.setBluetoothScans((ArrayList<BluetoothScan>) this.getBluetoothScans().clone());
        copiedResult.setWifiScans((ArrayList<WifiScan>) this.getWifiScans().clone());
        copiedResult.setLocationScans((ArrayList<LocationScan>) this.getLocationScans().clone());
        copiedResult.setStartTime(this.startTime);
        copiedResult.setEndTime(this.endTime);
        return copiedResult;
    }
}
