package ch.ethz.smartenergy.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Date;

import ch.ethz.smartenergy.Constants;
import ch.ethz.smartenergy.model.BluetoothScan;
import ch.ethz.smartenergy.model.DiscoveredBluetooth;


/**
 * Created by Oscar on 2018-03-30.
 */
public class BluetoothPullService extends Service {
    private static final String BLUETOOTH_DEBUG_STRING = "BLUETOOTH_PULL_SERVICE";

    private BluetoothScan currentBluetoothScan;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean bluetoothAdapterEnabledBeforeStart;
    private Handler handler;

    private final Runnable bluetoothScanProcess = new Runnable() {
        @Override
        public void run() {
            currentBluetoothScan = new BluetoothScan(new Date());
            mBluetoothAdapter.startDiscovery();
            //Log.d(BLUETOOTH_DEBUG_STRING, "Started discovery");
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //check if we are in a new scan or not.
                if (currentBluetoothScan == null) {
                    return;
                }

                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName = device.getName();

//                Log.d(BLUETOOTH_DEBUG_STRING,
//                        "Scanned Bluetooth device: " +  device.getAddress());

                String hashedAddress = Hashing.encrypt(device.getAddress());
                DiscoveredBluetooth discoveredBluetooth = new DiscoveredBluetooth(
                        hashedAddress,
                        intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE),
                        device.getBluetoothClass().getDeviceClass(),
                        device.getBluetoothClass().getMajorDeviceClass(),
                        deviceName,
                        device.getType(),
                        device.getBondState()
                );

                currentBluetoothScan.getDiscoveredDevices().add(discoveredBluetooth);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Intent broadCastIntent = new Intent();
                broadCastIntent.setAction(Constants.DataBroadcastActionName);
                broadCastIntent.putExtra(Constants.BluetoothScanExtraName, currentBluetoothScan);
                LocalBroadcastManager.getInstance(BluetoothPullService.this).sendBroadcast(broadCastIntent);
                currentBluetoothScan = null;
                if (handlerThread.isAlive()) handler.post(bluetoothScanProcess);
            }
        }
    };
    private HandlerThread handlerThread;

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            Log.i(BLUETOOTH_DEBUG_STRING, "No bluetooth adapter found");
            return;
        }
        bluetoothAdapterEnabledBeforeStart = mBluetoothAdapter.isEnabled();
        if (!bluetoothAdapterEnabledBeforeStart) {
            mBluetoothAdapter.enable();
        }
        handlerThread = new HandlerThread("BluetoothThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            handler.removeCallbacks(bluetoothScanProcess);
            try {
                unregisterReceiver(mReceiver);
                handlerThread.quitSafely();

            } catch (IllegalArgumentException e) {
                Log.d(BLUETOOTH_DEBUG_STRING,
                        "Failed to unregister receiver, probably never registered");
            }
            // disable bluetooth adapter if it has been disabled at start and
            // if no device is connected
            boolean hasConnectedDevices = mBluetoothAdapter.getBondedDevices() != null &&
                    mBluetoothAdapter.getBondedDevices().size() > 0;
            if (!bluetoothAdapterEnabledBeforeStart && !hasConnectedDevices) {
                mBluetoothAdapter.disable();
            }
        }
        Log.d(BLUETOOTH_DEBUG_STRING, "---------DESTROYING--------");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(BLUETOOTH_DEBUG_STRING, "---------STARTING--------");
        if (mBluetoothAdapter != null) {
            //Bluetooth
            IntentFilter filterActionFoundDiscovery = new IntentFilter();
            filterActionFoundDiscovery.addAction(BluetoothDevice.ACTION_FOUND);
            filterActionFoundDiscovery.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filterActionFoundDiscovery);

            handler.post(bluetoothScanProcess);
        }
        return START_STICKY;
    }
}

