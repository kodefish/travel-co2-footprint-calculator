package ch.ethz.smartenergy.model;

import java.io.Serializable;

public class DiscoveredBluetooth implements Serializable {

    private String mac;

    private int signalStrength;

    private int minorClass;

    private int majorClass;

    private String deviceName;

    private int deviceType;

    private int bondState;

    public DiscoveredBluetooth(String mac, int signalStrength, int minorClass, int majorClass,
                               String deviceName, int deviceType, int bondState) {
        this.mac = mac;
        this.signalStrength = signalStrength;
        this.minorClass = minorClass;
        this.majorClass = majorClass;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.bondState = bondState;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public int getMinorClass() {
        return minorClass;
    }

    public void setMinorClass(int minorClass) {
        this.minorClass = minorClass;
    }

    public int getMajorClass() {
        return majorClass;
    }

    public void setMajorClass(int majorClass) {
        this.majorClass = majorClass;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }
}
