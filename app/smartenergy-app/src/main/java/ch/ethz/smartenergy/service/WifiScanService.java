package ch.ethz.smartenergy.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.ethz.smartenergy.Constants;
import ch.ethz.smartenergy.model.DiscoveredWifi;
import ch.ethz.smartenergy.model.WifiScan;

public class WifiScanService extends Service {
    private final String WIFI_DEBUG = "WIFI_SCAN_SERVICE";
    private Handler handler;
    private long recorded_global_time;
    private long recorded_sensor_time;
    private WifiManager wifiManager;
    private boolean isWifiRecieverRegistered;
    private HandlerThread handlerThread;

    private final Runnable wifiScanningProcess = new Runnable() {
        @Override
        public void run() {
            wifiManager.startScan();
            //Log.d(WIFI_DEBUG, "Started scanning");
            if (handlerThread.isAlive()) {
                handler.postDelayed(wifiScanningProcess, SensorScanPeriod.WIFI_SENSOR_PERIOD);
            }
        }
    };

    private final BroadcastReceiver wifiScanner = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    processScanResults();
            }
        }
    };

    private void processScanResults() {
        //Log.d(WIFI_DEBUG, "Scan results available");
        List<ScanResult> scanResultList = ((WifiManager) getApplicationContext().
                getSystemService(WIFI_SERVICE)).getScanResults();

        WifiScan currentWifiScan = new WifiScan(new Date());
        for (ScanResult wifiDetected : scanResultList) {
            if (recorded_global_time == 0) {
                recorded_global_time = System.currentTimeMillis();
            }
            if (recorded_sensor_time == 0) {
                recorded_sensor_time = wifiDetected.timestamp;
                //do nothing, approxTime is set correctly
            }
            long approxTime = recorded_global_time + TimeUnit.NANOSECONDS.toMillis(
                    wifiDetected.timestamp - recorded_sensor_time);
            //Log.d(WIFI_DEBUG, "Wifi found with SSID " + wifiDetected.SSID);
            String hashedBssid = Hashing.encrypt(wifiDetected.BSSID);
            DiscoveredWifi wifi = new DiscoveredWifi(
                    wifiDetected.level,
                    hashedBssid,
                    wifiDetected.SSID,
                    wifiDetected.timestamp,
                    new Date(approxTime)
            );

            currentWifiScan.getDiscoveredDevices().add(wifi);
        }

        Intent broadCastIntent = new Intent();
        broadCastIntent.setAction(Constants.DataBroadcastActionName);
        broadCastIntent.putExtra(Constants.WifiScanExtraName, currentWifiScan);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadCastIntent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(WIFI_DEBUG, "---------STARTING--------");
        if (wifiManager != null) {
            IntentFilter scanResultIntent = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(wifiScanner, scanResultIntent);
            isWifiRecieverRegistered = true;
            handler.post(wifiScanningProcess);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null)
        {
            Log.i(WIFI_DEBUG, "No wifi manager available");
            return;
        }
        if (!wifiManager.isScanAlwaysAvailable() && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        handlerThread = new HandlerThread("WifiProcessThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isWifiRecieverRegistered) {
            unregisterReceiver(wifiScanner);
            handler.removeCallbacks(wifiScanningProcess);
        }
        try {
            handlerThread.quitSafely();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(WIFI_DEBUG, "---------DESTROYING--------");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
