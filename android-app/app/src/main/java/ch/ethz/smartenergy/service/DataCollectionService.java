package ch.ethz.smartenergy.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ch.ethz.smartenergy.Constants;
import ch.ethz.smartenergy.MainActivity;
import ch.ethz.smartenergy.R;
import ch.ethz.smartenergy.model.BluetoothScan;
import ch.ethz.smartenergy.model.LocationScan;
import ch.ethz.smartenergy.model.ScanResult;
import ch.ethz.smartenergy.model.SensorReading;
import ch.ethz.smartenergy.model.WifiScan;

import static android.app.Notification.VISIBILITY_SECRET;

public class DataCollectionService extends Service {

    private static final String DEBUG_STRING = "DATA_COLLECTION_SERVICE";
    private PowerManager.WakeLock wakeLock;
    ScanResult scanResult;
    private Handler handler;

    private final Runnable broadcastCollectedData = new Runnable() {
        @Override
        public void run() {
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(Constants.WindowBroadcastActionName);
            broadCastIntent.putExtra(Constants.WindowBroadcastExtraName, scanResult.copy());
            LocalBroadcastManager.getInstance(DataCollectionService.this).sendBroadcast(broadCastIntent);
            scanResult.clear();

            handler.postDelayed(this, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE);
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if (data == null) return;

            if (data.containsKey(Constants.BluetoothScanExtraName)){
                BluetoothScan scan = (BluetoothScan) data.getSerializable(Constants.BluetoothScanExtraName);
                scanResult.getBluetoothScans().add(scan);
            }

            if (data.containsKey(Constants.WifiScanExtraName)) {
                WifiScan scan = (WifiScan) data.getSerializable(Constants.WifiScanExtraName);
                scanResult.getWifiScans().add(scan);
            }

            if (data.containsKey(Constants.LocationScanExtraName)) {
                LocationScan scan = (LocationScan) data.getSerializable(Constants.LocationScanExtraName);
                scanResult.getLocationScans().add(scan);
            }

            if (data.containsKey(Constants.AccReadingExtraName)) {
                SensorReading scan = (SensorReading) data.getSerializable(Constants.AccReadingExtraName);
                scanResult.getAccReadings().add(scan);
            }

            if (data.containsKey(Constants.GyroReadingExtraName)) {
                SensorReading scan = (SensorReading) data.getSerializable(Constants.GyroReadingExtraName);
                scanResult.getGyroReadings().add(scan);
            }

            if (data.containsKey(Constants.MagnReadingExtraName)) {
                SensorReading scan = (SensorReading) data.getSerializable(Constants.MagnReadingExtraName);
                scanResult.getMagnReadings().add(scan);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(DEBUG_STRING, "---------STARTING--------");
        scanResult = new ScanResult();
        PowerManager powerManager =
                (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "smart-energy::DataCollectionService");
        wakeLock.acquire();
        showNotification();
        registerReceiver();
        startScanningServices();
        handler = new Handler();
        handler.postDelayed(broadcastCollectedData, SensorScanPeriod.DATA_COLLECTION_WINDOW_SIZE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        handler.removeCallbacks(broadcastCollectedData);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        Log.d(DEBUG_STRING, "---------DESTROYING--------");
    }

    private void showNotification() {
        int icon = R.drawable.ic_launcher_foreground;
        String text = getString(R.string.notification_service_text);

        // Create mandatory notification channel
        String channelId = "data_collection_service";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(VISIBILITY_SECRET);
            channel.setSound(null, null);
            channel.enableVibration(false);
            channel.enableLights(false);
            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setPackage(getPackageName())
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,
                channelId)
                .setSmallIcon(icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        startForeground(123, mBuilder.build());
    }

    private void startScanningServices() {
        Hashing.generateKey();
        startService(new Intent(this, BluetoothPullService.class));
        startService(new Intent(this, LocationService.class));
        startService(new Intent(this, SensorReaderService.class));
        startService(new Intent(this, WifiScanService.class));
    }

    private void registerReceiver() {
        Log.d(DEBUG_STRING, "Register receiver");
        IntentFilter it = new IntentFilter();
        it.addAction(Constants.DataBroadcastActionName);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,  it);
    }
}
