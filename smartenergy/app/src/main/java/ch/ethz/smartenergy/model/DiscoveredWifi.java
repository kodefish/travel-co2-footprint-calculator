package ch.ethz.smartenergy.model;

import java.io.Serializable;
import java.util.Date;

public class DiscoveredWifi implements Serializable {

    private int signalStrength;

    private String bssid;

    private String ssid;

    private long timeOfReadingSinceStart;

    private Date timeOfReading;

    public DiscoveredWifi(int signalStrength, String bssid, String ssid,
                          long timeOfReadingSinceStart, Date timeOfReading) {
        this.signalStrength = signalStrength;
        this.bssid = bssid;
        this.ssid = ssid;
        this.timeOfReadingSinceStart = timeOfReadingSinceStart;
        this.timeOfReading = timeOfReading;
    }


    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public long getTimeOfReadingSinceStart() {
        return timeOfReadingSinceStart;
    }

    public void setTimeOfReadingSinceStart(long timeOfReadingSinceStart) {
        this.timeOfReadingSinceStart = timeOfReadingSinceStart;
    }

    public Date getTimeOfReading() {
        return timeOfReading;
    }

    public void setTimeOfReading(Date timeOfReading) {
        this.timeOfReading = timeOfReading;
    }
}
