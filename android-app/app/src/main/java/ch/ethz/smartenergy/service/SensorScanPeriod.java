package ch.ethz.smartenergy.service;


import java.util.concurrent.TimeUnit;

/**
 * Created by Oscar on 2018-04-01.
 */

public class SensorScanPeriod {

    public static final long WIFI_SENSOR_PERIOD = TimeUnit.SECONDS.toMillis(30);

    public static final long DATA_COLLECTION_WINDOW_SIZE = TimeUnit.SECONDS.toMillis(20);

    public static final long GPS_SENSOR_PERIOD = TimeUnit.SECONDS.toMillis(1);

    public static final int HARDWARE_SENSOR_PERIOD = 20000;
}