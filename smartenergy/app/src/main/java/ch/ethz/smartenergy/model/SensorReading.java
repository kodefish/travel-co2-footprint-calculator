package ch.ethz.smartenergy.model;

import java.io.Serializable;
import java.util.Date;


public class SensorReading implements Serializable {

    private long timeOfReadingSinceStart;

    private Date readingTime;

    private double valueOnXAxis;

    private double valueOnYAxis;

    private double valueOnZAxis;

    private int accuracy;

    private int sensorType;

    public SensorReading(long timeOfReadingSinceStart, Date readingTime, double valueOnXAxis,
                         double valueOnYAxis, double valueOnZAxis, int accuracy, int sensorType) {
        this.timeOfReadingSinceStart = timeOfReadingSinceStart;
        this.readingTime = readingTime;
        this.valueOnXAxis = valueOnXAxis;
        this.valueOnYAxis = valueOnYAxis;
        this.valueOnZAxis = valueOnZAxis;
        this.accuracy = accuracy;
        this.sensorType = sensorType;
    }

    public long getTimeOfReadingSinceStart() {
        return timeOfReadingSinceStart;
    }

    public void setTimeOfReadingSinceStart(long timeOfReadingSinceStart) {
        this.timeOfReadingSinceStart = timeOfReadingSinceStart;
    }

    public Date getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(Date readingTime) {
        this.readingTime = readingTime;
    }

    public double getValueOnXAxis() {
        return valueOnXAxis;
    }

    public void setValueOnXAxis(double valueOnXAxis) {
        this.valueOnXAxis = valueOnXAxis;
    }

    public double getValueOnYAxis() {
        return valueOnYAxis;
    }

    public void setValueOnYAxis(double valueOnYAxis) {
        this.valueOnYAxis = valueOnYAxis;
    }

    public double getValueOnZAxis() {
        return valueOnZAxis;
    }

    public void setValueOnZAxis(double valueOnZAxis) {
        this.valueOnZAxis = valueOnZAxis;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }
}
