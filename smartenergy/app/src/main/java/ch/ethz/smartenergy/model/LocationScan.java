package ch.ethz.smartenergy.model;

import java.io.Serializable;
import java.util.Date;

public class LocationScan implements Serializable {

    private Double latitude;

    private Double longitude;

    private Double altitude;

    private float accuracy;

    private Date timeOfReading;

    private float bearing;

    private float speed;

    private long timeSinceBoot;

    public LocationScan(double latitude, double longitude, double altitude, float accuracy,
                        Date date, float bearing, float speed, long elapsedRealtimeNanos) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.timeOfReading = date;
        this.bearing = bearing;
        this.speed = speed;
        this.timeSinceBoot = elapsedRealtimeNanos;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Date getTimeOfReading() {
        return timeOfReading;
    }

    public void setTimeOfReading(Date timeOfReading) {
        this.timeOfReading = timeOfReading;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTimeSinceBoot() {
        return timeSinceBoot;
    }

    public void setTimeSinceBoot(long timeSinceBoot) {
        this.timeSinceBoot = timeSinceBoot;
    }
}
