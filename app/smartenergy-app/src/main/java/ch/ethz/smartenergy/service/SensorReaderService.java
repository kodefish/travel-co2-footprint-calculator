package ch.ethz.smartenergy.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.ethz.smartenergy.Constants;
import ch.ethz.smartenergy.model.SensorReading;

public class SensorReaderService extends Service implements Runnable {
    private SensorManager mSensorManager;
    private static final String SENSOR_DEBUG = "SENSOR_READER_SERVICE";
    private Handler handler;
    private HandlerThread handlerThread;
    private long timeReference;
    private long sensorTimeReference;

    private final int[] sensorTypes = new int[] {
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
    };
    private List<Sensor> availableSensors;

    private final SensorEventListener mListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            long approxTime = timeReference + TimeUnit.NANOSECONDS.toMillis(
                    sensorEvent.timestamp - sensorTimeReference);
//            Log.d(SENSOR_DEBUG, "Sensor event for type " +
//                    sensorEvent.sensor.getType());
            SensorReading sensorReading = new SensorReading(
                    sensorEvent.timestamp,
                    new Date(approxTime),
                    sensorEvent.values[0],
                    sensorEvent.values[1],
                    sensorEvent.values[2],
                    sensorEvent.accuracy,
                    sensorEvent.sensor.getType()
            );

            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(Constants.DataBroadcastActionName);

            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    broadCastIntent.putExtra(Constants.AccReadingExtraName, sensorReading);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    broadCastIntent.putExtra(Constants.GyroReadingExtraName, sensorReading);
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    broadCastIntent.putExtra(Constants.MagnReadingExtraName, sensorReading);
                    break;
            }
            LocalBroadcastManager.getInstance(SensorReaderService.this).sendBroadcast(broadCastIntent);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initAvailableSensors();
        handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    private void initAvailableSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        availableSensors = new ArrayList<>();
        for (int sensorType: sensorTypes) {
            Sensor mSensor = mSensorManager.getDefaultSensor(sensorType);
            if(mSensor != null) {
                availableSensors.add(mSensor);
            }
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(SENSOR_DEBUG, "---------STARTING--------");
        // calculate time references,
        // since sensor event timestamps are nanoseconds since last boot
        timeReference = System.currentTimeMillis();
        sensorTimeReference = SystemClock.elapsedRealtimeNanos();
        handler.post(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(this);
        try {
            mSensorManager.unregisterListener(mListener);
            handlerThread.quitSafely();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(SENSOR_DEBUG, "---------DESTROYING--------");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        //Maybe define max scan frequency to save battery.
        registerSensors();
    }

    private void registerSensors() {
        for (Sensor sensor: availableSensors) {
            mSensorManager.registerListener(mListener, sensor,
                    SensorScanPeriod.HARDWARE_SENSOR_PERIOD);
        }
    }
}
